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
import fr.gouv.stopc.submission.code.server.ws.dto.SubmissionCodeCsvDto;
import fr.gouv.stopc.submission.code.server.ws.service.IFileService;
import fr.gouv.stopc.submission.code.server.ws.service.IGenerateService;
import fr.gouv.stopc.submission.code.server.ws.service.ISFTPService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.*;
import java.time.DateTimeException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Slf4j
@Service
public class FileServiceImpl implements IFileService {
    private static final String HEADER_CSV = "code_pour_qr, code_brut, validite_debut, validite_fin \n";
    private ISubmissionCodeService submissionCodeService;
    private IGenerateService generateService;
    private ISFTPService sftpService;

    @Value("${stop.covid.qr.code.url}")
    private String qrCodeBaseUrlToBeFormatted;

    @Value("${stop.covid.qr.code.target.zone}")
    private String targetZoneId;

    @Value("${csv.separator}")
    private Character csvSeparator;

    @Value("${csv.delimiter}")
    private Character csvDelimiter;

    @Value("${csv.filename.formatter}")
    private String csvFilenameFormat;

    @Value("${submission.code.server.sftp.transfer}")
    private boolean transferFile;



    @Inject
    public FileServiceImpl(ISubmissionCodeService submissionCodeService, IGenerateService generateService, ISFTPService sftpService){
        this.submissionCodeService = submissionCodeService;
        this.generateService=generateService;
        this.sftpService=sftpService;
    }

    @Override
    @Async
    public Optional<ByteArrayOutputStream> zipExport(String numberCodeDay, Lot lotObject, String dateFrom, String dateTo)
            throws SubmissionCodeServerException
    {

        OffsetDateTime dateTimeFrom;
        OffsetDateTime dateTimeTo;
        dateTimeFrom= OffsetDateTime.parse(dateFrom, DateTimeFormatter.ISO_DATE_TIME);
        dateTimeTo= OffsetDateTime.parse(dateTo, DateTimeFormatter.ISO_DATE_TIME);

        if(!isDateValid(dateTimeFrom,dateTimeTo)) {
            log.error(SubmissionCodeServerException.ExceptionEnum.INVALID_DATE.getMessage());
            throw new SubmissionCodeServerException(
                    SubmissionCodeServerException.ExceptionEnum.INVALID_DATE
            );
        }


        // STEP 1 - create codes
        this.persistUUIDv4CodesFor(numberCodeDay, lotObject, dateTimeFrom, dateTimeTo);

        // STEP 1 BIS Retrieve data
        List<SubmissionCodeDto> submissionCodeDtos = submissionCodeService
                .getCodeUUIDv4CodesForCsv(Long.toString(lotObject.getId()), CodeTypeEnum.UUIDv4.getTypeCode());

        if (CollectionUtils.isEmpty(submissionCodeDtos)){
            return Optional.empty();
        }

        //get distinct dates
        final List<OffsetDateTime> availableDates = submissionCodeDtos
                .stream().map(s -> s.getDateAvailable()).distinct().collect(Collectors.toList());

        // STEP 2 parsing codes to csv dataByFilename
        Map<String, byte[]> dataByFilename = codeAsCsvData(submissionCodeDtos, availableDates);


        // STEP 3 packaging csv data
        ByteArrayOutputStream zipOutputStream = packagingCsvDataToZipFile(dataByFilename);

        if(transferFile){
            ByteArrayInputStream inputStream = new ByteArrayInputStream(zipOutputStream.toByteArray());
            // async method is called here.
            log.info("SFTP transfer is about to be submitted.");
            sftpService.transferFileSFTP(inputStream);
            log.info("SFTP transfer have been submitted.");
        } else {
            log.info("No SFTP transfer have been submitted.");
        }

        return  Optional.of(zipOutputStream);
    }


    @Override
    public void persistUUIDv4CodesFor(String codePerDays, Lot lotObject, OffsetDateTime from, OffsetDateTime to)
            throws SubmissionCodeServerException
    {
        OffsetDateTime fromWithoutHours = from.truncatedTo(ChronoUnit.DAYS);
        OffsetDateTime toWithoutHours = to.truncatedTo(ChronoUnit.DAYS);

        long diffDays= ChronoUnit.DAYS.between(fromWithoutHours, toWithoutHours) + 1;
        int diff = Integer.parseInt(Long.toString(diffDays));
        List<OffsetDateTime> datesFromList = generateService.getValidFromList(diff, from);
        for(OffsetDateTime dateFromDay: datesFromList) {
            generateService.generateCodeGeneric(
                    Long.parseLong(codePerDays),
                    CodeTypeEnum.UUIDv4,
                    dateFromDay,
                    lotObject
            );
        }
    }

    @Override
    public Map<String, byte[]> codeAsCsvData (
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
                byte[] file = transformInFile(listForDay, dateTime);
                dataByFilename.put(this.getCsvFilename(dateTime), file );
            }
        }
        return dataByFilename;
    }

    @Override
    public ByteArrayOutputStream packagingCsvDataToZipFile(Map<String, byte[]> dataByFilename)
            throws SubmissionCodeServerException
    {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteOutputStream);
        try {
            for (String filename: dataByFilename.keySet()){
                zipOutputStream.putNextEntry(new ZipEntry(filename));

                final ByteArrayInputStream inputByteArray = new ByteArrayInputStream(dataByFilename.get(filename));
                IOUtils.copy(inputByteArray, zipOutputStream);
                inputByteArray.close();
                zipOutputStream.closeEntry();
            }
            zipOutputStream.close();
        } catch (IOException ioe) {
            log.error(SubmissionCodeServerException.ExceptionEnum.PACKAGING_CSV_FILE_ERROR.getMessage(), ioe);
            throw new SubmissionCodeServerException(
                    SubmissionCodeServerException.ExceptionEnum.PACKAGING_CSV_FILE_ERROR,
                    ioe
            );
        }

        return byteOutputStream;
    }

    /**
     * Create one file csv for the list of submissionCodes with date of available equal to dateTimeFrom
     * @param submissionCodeDtoList list of submissionCodes for a day specific
     * @param date the date of available of submissionCode
     * @return submissionCodeDtoList parsed into a csv file
     */
    private byte[] transformInFile(List<SubmissionCodeDto> submissionCodeDtoList, OffsetDateTime date)
            throws SubmissionCodeServerException
    {

        // name of the file should be built from the date at target Zone publication
        String fileName = this.getCsvFilename(date);

        // converting list SubmissionCodeDto to SubmissionCodeCsvDto to be proceeded in csv generator
        final List<SubmissionCodeCsvDto> submissionCodeCsvDtos = convert(submissionCodeDtoList);

        File file = new File(fileName);
        StringWriter fileWriter = new StringWriter();
        fileWriter.append(HEADER_CSV);

        ColumnPositionMappingStrategy mappingStrategy = new ColumnPositionMappingStrategy();
        mappingStrategy.setType(SubmissionCodeCsvDto.class);

        String[] columns = new String[]{"qrcode", "code", "dateAvailable", "dateEndValidity"};
        mappingStrategy.setColumnMapping(columns);
        StatefulBeanToCsvBuilder<SubmissionCodeDto> builder = new StatefulBeanToCsvBuilder<SubmissionCodeDto>(fileWriter)
                .withSeparator(csvSeparator).withQuotechar(csvDelimiter);

        StatefulBeanToCsv statefulBeanToCsv = builder.withMappingStrategy(mappingStrategy).build();

        try {
            statefulBeanToCsv.write(submissionCodeCsvDtos);

            return fileWriter.toString().getBytes("UTF-8");

        } catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            log.error(SubmissionCodeServerException.ExceptionEnum.CODE_TO_CSV_PARSING_ERROR.getMessage(), e);
            throw new SubmissionCodeServerException(
                    SubmissionCodeServerException.ExceptionEnum.CODE_TO_CSV_PARSING_ERROR,
                    e
            );
        } catch (UnsupportedEncodingException uee) {
            log.error(SubmissionCodeServerException.ExceptionEnum.CODE_TO_CSV_UTF8_ENCODING_ERROR.getMessage(), uee);
            throw new SubmissionCodeServerException(
                    SubmissionCodeServerException.ExceptionEnum.CODE_TO_CSV_UTF8_ENCODING_ERROR,
                    uee
            );
        }
    }

    /**
     *
     * @param from start date
     * @param to end date
     * @throws DateTimeException when end date is less than start date. or if start date is inferior to current time.
     */

    private Boolean isDateValid(OffsetDateTime from, OffsetDateTime to)
            throws DateTimeException
    {
        return !(OffsetDateTime.now().toLocalDate().compareTo(from.toLocalDate()) == 1 || from.isAfter(to));
    }



    /**
     * Method convert list of dao SubmissionCodeDto to csv data SubmissionCodeCsvDto
     * @param listForDay list provided by dao service to be converted to a list of csv DTO
     * @return list of SubmissionCodeCsvDto
     */
    private List<SubmissionCodeCsvDto> convert(List<SubmissionCodeDto> listForDay) {
        final ModelMapper modelMapper = new ModelMapper();
        return listForDay.stream().map(s -> {
            final SubmissionCodeCsvDto csvDto = modelMapper.map(s, SubmissionCodeCsvDto.class);
            csvDto.setQrcode(String.format(this.qrCodeBaseUrlToBeFormatted, csvDto.getCode(), csvDto.getType()));
            return csvDto;
        }).collect(Collectors.toList());
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

}
