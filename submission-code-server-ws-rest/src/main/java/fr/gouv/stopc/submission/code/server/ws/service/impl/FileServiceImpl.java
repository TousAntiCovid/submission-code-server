package fr.gouv.stopc.submission.code.server.ws.service.impl;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import fr.gouv.stopc.submission.code.server.commun.enums.CodeTypeEnum;
import fr.gouv.stopc.submission.code.server.database.dto.SubmissionCodeDto;
import fr.gouv.stopc.submission.code.server.database.entity.Lot;
import fr.gouv.stopc.submission.code.server.database.service.ISubmissionCodeService;
import fr.gouv.stopc.submission.code.server.ws.controller.error.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.ws.dto.CodeDetailedDto;
import fr.gouv.stopc.submission.code.server.ws.dto.SubmissionCodeCsvDto;
import fr.gouv.stopc.submission.code.server.ws.service.IFileService;
import fr.gouv.stopc.submission.code.server.ws.service.IGenerateService;
import fr.gouv.stopc.submission.code.server.ws.service.ISFTPService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;


@Slf4j
@Service
@Transactional
public class FileServiceImpl implements IFileService {
    private String HEADER_CSV = "code_pour_qr%s code_brut%s validite_debut%s validite_fin\n";
    private ISubmissionCodeService submissionCodeService;
    private IGenerateService generateService;
    private ISFTPService sftpService;

    @Value("${stop.covid.qr.code.url}")
    private String qrCodeBaseUrlToBeFormatted;

    /**TargetZoneId is the time zone id (in the java.time.ZoneId way) on which the submission code server should deliver the codes.
     * eg.: for France is "Europe/Paris"*/
    @Value("${stop.covid.qr.code.targetzone}")
    private String targetZoneId;

    /**The separator is the character uses to separate the columns.*/
    @Value("${csv.separator}")
    private Character csvSeparator;

    /**The delimiter is the character uses to enclose the strings.*/
    @Value("${csv.delimiter}")
    private Character csvDelimiter;

    @Value("${csv.filename.formatter}")
    private String csvFilenameFormat;

    @Value("${submission.code.server.sftp.enableautotransfer}")
    private boolean transferFile;



    @Inject
    public FileServiceImpl(ISubmissionCodeService submissionCodeService, IGenerateService generateService, ISFTPService sftpService){
        this.submissionCodeService = submissionCodeService;
        this.generateService=generateService;
        this.sftpService=sftpService;
    }

    @Async
    @Override
    public Optional<ByteArrayOutputStream> zipExportAsync(Long numberCodeDay, Lot lotObject, String dateFrom, String dateTo)
            throws SubmissionCodeServerException
    {
        return this.zipExport(numberCodeDay, lotObject, dateFrom, dateTo);
    }

    @Override
    public Optional<ByteArrayOutputStream> zipExport(Long numberCodeDay, Lot lotObject, String dateFrom, String dateTo)
            throws SubmissionCodeServerException
    {

        OffsetDateTime dateTimeFrom;
        OffsetDateTime dateTimeTo;

        try {
            dateTimeFrom = OffsetDateTime.parse(dateFrom, DateTimeFormatter.ISO_DATE_TIME);
            dateTimeTo = OffsetDateTime.parse(dateTo, DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException e) {
            log.error(SubmissionCodeServerException.ExceptionEnum.PARSE_STR_DATE_ERROR.getMessage());
            throw new SubmissionCodeServerException(
                    SubmissionCodeServerException.ExceptionEnum.PARSE_STR_DATE_ERROR
            );
        }

        if(!isDateValid(dateTimeFrom,dateTimeTo)) {
            log.error(SubmissionCodeServerException.ExceptionEnum.INVALID_DATE.getMessage());
            throw new SubmissionCodeServerException(
                    SubmissionCodeServerException.ExceptionEnum.INVALID_DATE
            );
        }


        // STEP 1 - create codes
        List<CodeDetailedDto> listUUIDv4Saves = this.persistUUIDv4CodesFor(numberCodeDay, lotObject, dateTimeFrom, dateTimeTo);

        if (CollectionUtils.isEmpty(listUUIDv4Saves)){
            return Optional.empty();
        }

        List<SubmissionCodeDto> submissionCodeDtos = listUUIDv4Saves.stream().map(codeDetailedDto-> mapToSubmissionCodeDto(codeDetailedDto,lotObject.getId())).collect(Collectors.toList());
        //get distinct dates
        final List<OffsetDateTime> availableDates = submissionCodeDtos
                .stream().map(SubmissionCodeDto::getDateAvailable).distinct().collect(Collectors.toList());

        // STEP 2 parsing codes to csv dataByFilename
        Map<String, byte[]> dataByFilename = serializeCodesToCsv(submissionCodeDtos, availableDates);


        // STEP 3 packaging csv data
        ByteArrayOutputStream zipOutputStream = null;
        try {
            zipOutputStream = packageCsvDataToZipFile(dataByFilename);
        } catch (IOException e) {
            log.error(SubmissionCodeServerException.ExceptionEnum.PACKAGING_CSV_FILE_ERROR.getMessage(), e);
            throw new SubmissionCodeServerException(
                    SubmissionCodeServerException.ExceptionEnum.PACKAGING_CSV_FILE_ERROR,
                    e
            );
        }
        if(transferFile){
            // async method is called here.
            log.info("SFTP transfer is about to be submitted.");
            sftpService.transferFileSFTP(zipOutputStream);

            log.info("SFTP transfer have been submitted.");
        } else {
            log.info("No SFTP transfer have been submitted.");
        }

        return  Optional.of(zipOutputStream);
    }


    @Override
    public List<CodeDetailedDto> persistUUIDv4CodesFor(Long codePerDays, Lot lotObject, OffsetDateTime from, OffsetDateTime to)
            throws SubmissionCodeServerException
    {
        List<CodeDetailedDto> listCodeDetailedDto = new ArrayList<>();
        OffsetDateTime fromWithoutHours = from.truncatedTo(ChronoUnit.DAYS);
        OffsetDateTime toWithoutHours = to.truncatedTo(ChronoUnit.DAYS);

        long diffDays= ChronoUnit.DAYS.between(fromWithoutHours, toWithoutHours) + 1;
        int diff = Integer.parseInt(Long.toString(diffDays));
        List<OffsetDateTime> datesFromList = generateService.getListOfValidDatesFor(diff, from);
        for(OffsetDateTime dateFromDay: datesFromList) {
            List<CodeDetailedDto> codeSaves = generateService.generateCodeGeneric(
                    codePerDays,
                    CodeTypeEnum.UUIDv4,
                    dateFromDay,
                    lotObject
            );
            if(CollectionUtils.isNotEmpty(codeSaves)){
                listCodeDetailedDto.addAll(codeSaves);
            }
        }
        return listCodeDetailedDto;
    }

    @Override
    public Map<String, byte[]> serializeCodesToCsv  (
            List<SubmissionCodeDto> submissionCodeDtos,
            List<OffsetDateTime> dates
    )
            throws SubmissionCodeServerException
    {
        Map<String, byte[]> dataByFilename = new HashMap<>();

        for(OffsetDateTime dateTime : dates){
            List<SubmissionCodeDto> listForDay= submissionCodeDtos
                    .stream().filter(tmp-> dateTime.isEqual(tmp.getDateAvailable()))
                    .collect(Collectors.toList());

            if(CollectionUtils.isNotEmpty(listForDay)){
                byte[] file = createCSV(listForDay, dateTime);
                dataByFilename.put(this.getCsvFilename(dateTime), file );
            }
        }
        return dataByFilename;
    }

    @Override
    public ByteArrayOutputStream packageCsvDataToZipFile(Map<String, byte[]> dataByFilename)
            throws SubmissionCodeServerException, IOException {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = null;
        TarArchiveOutputStream tarArchiveOutputStream = null;

    try{
        gzipOutputStream = new GZIPOutputStream(byteOutputStream);
        tarArchiveOutputStream = new TarArchiveOutputStream(gzipOutputStream);

        for (String filename: dataByFilename.keySet()){
            TarArchiveEntry entry = new TarArchiveEntry(filename);
            byte[] data = dataByFilename.get(filename);
            entry.setSize(data.length);
            tarArchiveOutputStream.putArchiveEntry(entry);
            final ByteArrayInputStream inputByteArray = new ByteArrayInputStream(dataByFilename.get(filename));
            IOUtils.copy(inputByteArray, tarArchiveOutputStream);
            inputByteArray.close();
            tarArchiveOutputStream.closeArchiveEntry();
        }

    } catch (IOException ioe) {
            log.error(SubmissionCodeServerException.ExceptionEnum.PACKAGING_CSV_FILE_ERROR.getMessage(), ioe);
            throw new SubmissionCodeServerException(
                    SubmissionCodeServerException.ExceptionEnum.PACKAGING_CSV_FILE_ERROR,
                    ioe
            );
        } finally {
        {
            if(byteOutputStream!= null){
                byteOutputStream.close();
            }
            if(tarArchiveOutputStream!=null){
                tarArchiveOutputStream.close();
            }
            if(gzipOutputStream!= null){
                gzipOutputStream.close();
            }
        }
    }
        return byteOutputStream;
    }

    /**
     * Create one file csv for the list of submissionCodes with date of available equal to dateTimeFrom
     * @param submissionCodeDtoList list of submissionCodes for a day specific
     * @param date the date of available of submissionCode
     * @return submissionCodeDtoList parsed into a csv file
     */
    private byte[] createCSV(List<SubmissionCodeDto> submissionCodeDtoList, OffsetDateTime date)
            throws SubmissionCodeServerException
    {
        String header= HEADER_CSV.replaceAll("%s",Character.toString(csvSeparator));
        // converting list SubmissionCodeDto to SubmissionCodeCsvDto to be proceeded in csv generator
        final List<SubmissionCodeCsvDto> submissionCodeCsvDtos = convert(submissionCodeDtoList);

        StringWriter fileWriter = new StringWriter();
        fileWriter.append(header);

        ColumnPositionMappingStrategy mappingStrategy = new ColumnPositionMappingStrategy();
        mappingStrategy.setType(SubmissionCodeCsvDto.class);

        String[] columns = new String[]{"qrcode", "code", "dateAvailable", "dateEndValidity"};
        mappingStrategy.setColumnMapping(columns);
        StatefulBeanToCsvBuilder<SubmissionCodeDto> builder = new StatefulBeanToCsvBuilder<SubmissionCodeDto>(fileWriter)
                .withSeparator(csvSeparator).withQuotechar(csvDelimiter);

        StatefulBeanToCsv statefulBeanToCsv = builder.withMappingStrategy(mappingStrategy).build();

        try {
            statefulBeanToCsv.write(submissionCodeCsvDtos);

            return fileWriter.toString().getBytes(StandardCharsets.UTF_8);

        } catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            log.error(SubmissionCodeServerException.ExceptionEnum.CODE_TO_CSV_PARSING_ERROR.getMessage(), e);
            throw new SubmissionCodeServerException(
                    SubmissionCodeServerException.ExceptionEnum.CODE_TO_CSV_PARSING_ERROR,
                    e
            );
        }
    }

    /**
     *
     * @param from start date
     * @param to end date
     * @throws DateTimeException when end date is less than start date. or if start date is inferior to current time.
     */

    protected Boolean isDateValid(OffsetDateTime from, OffsetDateTime to)
            throws DateTimeException
    {
        return !(OffsetDateTime.now().toLocalDate().compareTo(from.toLocalDate()) > 0 || from.isAfter(to));
    }



    /**
     * Method convert list of dao SubmissionCodeDto to csv data SubmissionCodeCsvDto
     * @param listForDay list provided by dao service to be converted to a list of csv DTO
     * @return list of SubmissionCodeCsvDto
     */
    private List<SubmissionCodeCsvDto> convert(List<SubmissionCodeDto> listForDay) throws SubmissionCodeServerException {
        final ModelMapper modelMapper = new ModelMapper();

        final List<SubmissionCodeCsvDto> submissionCodeCsvDtos = listForDay.stream().map(s -> {
            final SubmissionCodeCsvDto csvDto = modelMapper.map(s, SubmissionCodeCsvDto.class);
            try {
                csvDto.setQrcode(String.format(
                        this.qrCodeBaseUrlToBeFormatted,
                        URLEncoder.encode(csvDto.getCode(), "UTF-8"),
                        URLEncoder.encode(csvDto.getType(), "UTF-8")
                ));
                return csvDto;
            } catch (UnsupportedEncodingException e) {
                return null;
            }
        }).collect(Collectors.toList());

        if(submissionCodeCsvDtos.size() != listForDay.size()) {
            log.error(SubmissionCodeServerException.ExceptionEnum.MAPPING_CODE_FOR_CSV_FILE_ERROR.getMessage());
            throw new SubmissionCodeServerException(
                    SubmissionCodeServerException.ExceptionEnum.MAPPING_CODE_FOR_CSV_FILE_ERROR
            );
        }

        return submissionCodeCsvDtos;
    }

    /**
     * Formats csv file name from date and pattern set in application.properties.
     * @param date date of the file to generate
     * @return  formatted csv file name from date and pattern set in application.properties.
     */
    private String getCsvFilename(OffsetDateTime date) {
        date = date.withOffsetSameInstant(OffsetDateTime.now(ZoneId.of(this.targetZoneId)).getOffset());
        return  String.format(csvFilenameFormat,date.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
    }

    private SubmissionCodeDto mapToSubmissionCodeDto(CodeDetailedDto codeDetailedDto, Long idLot ){
        SubmissionCodeDto submissionCodeDto = new SubmissionCodeDto();
        submissionCodeDto.setLot(idLot);
        submissionCodeDto.setUsed(false);
        submissionCodeDto.setType(CodeTypeEnum.UUIDv4.getTypeCode());
        submissionCodeDto.setDateGeneration(OffsetDateTime.parse(codeDetailedDto.getValidFrom(), DateTimeFormatter.ISO_DATE_TIME));
        submissionCodeDto.setDateEndValidity(OffsetDateTime.parse(codeDetailedDto.getValidUntil(), DateTimeFormatter.ISO_DATE_TIME));
        submissionCodeDto.setCode(codeDetailedDto.getCode());
        submissionCodeDto.setDateAvailable(OffsetDateTime.parse(codeDetailedDto.getValidFrom(), DateTimeFormatter.ISO_DATE_TIME));
        return submissionCodeDto;
    }

}
