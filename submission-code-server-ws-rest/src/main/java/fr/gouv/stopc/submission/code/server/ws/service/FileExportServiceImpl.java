package fr.gouv.stopc.submission.code.server.ws.service;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import fr.gouv.stopc.submission.code.server.database.dto.SubmissionCodeDto;
import fr.gouv.stopc.submission.code.server.database.service.ISubmissionCodeService;
import fr.gouv.stopc.submission.code.server.ws.dto.SubmissionCodeCsvDto;
import fr.gouv.stopc.submission.code.server.ws.enums.CodeTypeEnum;
import fr.gouv.stopc.submission.code.server.ws.errors.NumberOfTryGenerateCodeExceededExcetion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Slf4j
@Service
public class FileExportServiceImpl implements IFileService {
    private static final String HEADER_CSV = "code_pour_qr, code_brut, validite_debut, validite_fin \n";
    private ISubmissionCodeService submissionCodeService;
    private IGenerateService generateService;

    @Value("${stop.covid.qr.code.url}")
    private String qrCodeBaseUrlToBeFormatted;

    @Value("${stop.covid.qr.code.target.zone}")
    private String targetZoneId;

    @Inject
    public FileExportServiceImpl(ISubmissionCodeService submissionCodeService, IGenerateService generateService){
        this.submissionCodeService = submissionCodeService;
        this.generateService=generateService;
    }

    @Override
    public Optional<ByteArrayOutputStream> zipExport(String numberCodeDay, String lot, String dateFrom, String dateTo) throws Exception {

        OffsetDateTime dateTimeFrom;
        OffsetDateTime dateTimeTo;
        dateTimeFrom= OffsetDateTime.parse(dateFrom, DateTimeFormatter.ISO_DATE_TIME);
        dateTimeTo= OffsetDateTime.parse(dateTo, DateTimeFormatter.ISO_DATE_TIME);

        validationDates(dateTimeFrom,dateTimeTo);

        // STEP 1 - create codes
        this.persistUUIDv4CodesFor(numberCodeDay, lot, dateTimeFrom, dateTimeTo);

        // STEP 1 BIS Retrieve data
        List<SubmissionCodeDto> submissionCodeDtos = submissionCodeService
                .getCodeUUIDv4CodesForCsv(lot, CodeTypeEnum.UUIDv4.getTypeCode());

        // TODO: Throw error here instead
        if (CollectionUtils.isEmpty(submissionCodeDtos)){
            return Optional.empty();
        }

        //get distinct dates
        final List<@NotNull OffsetDateTime> availableDates = submissionCodeDtos
                .stream().map(s -> s.getDateAvailable()).distinct().collect(Collectors.toList());

        // STEP 2 parsing codes to csv files
        List<File> files = codeAsCsvFiles(submissionCodeDtos, availableDates);


        // STEP 3 packaging csv files
        ByteArrayOutputStream zipOutputStream = packagingCsvFilesToZipFile(files);

        return  Optional.of(zipOutputStream);
    }


    @Override
    public void persistUUIDv4CodesFor(String codePerDays, String lotIdentifier, OffsetDateTime from, OffsetDateTime to) throws NumberOfTryGenerateCodeExceededExcetion {
        int diffDays= Math.toIntExact(ChronoUnit.DAYS.between(from, to))+1;
        List<OffsetDateTime> datesFromList = generateService.getValidFromList(diffDays, from);

        for(OffsetDateTime dateFromDay: datesFromList) {
            generateService.generateCodeGeneric(Long.parseLong(codePerDays), CodeTypeEnum.UUIDv4, dateFromDay, Long.parseLong(lotIdentifier));
        }
    }

    @Override
    public List<File> codeAsCsvFiles(List<SubmissionCodeDto> submissionCodeDtos, List<@NotNull OffsetDateTime> dates) throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, IOException {
        List<File> files = new ArrayList<>();

        for(OffsetDateTime dateTime : dates){
            List<SubmissionCodeDto> listForDay= submissionCodeDtos
                    .stream().filter(tmp-> dateTime.isEqual(tmp.getDateAvailable()))
                    .collect(Collectors.toList());

            File file = transformInFile(listForDay, dateTime);
            files.add(file);
        }
        return files;
    }

    @Override
    public ByteArrayOutputStream packagingCsvFilesToZipFile(List<File> files) throws IOException {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteOutputStream);

        for (File file: files){
            zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
            FileInputStream fileInputStream = new FileInputStream(file);
            IOUtils.copy(fileInputStream, zipOutputStream);
            fileInputStream.close();
            zipOutputStream.closeEntry();
            file.deleteOnExit();
        }
        zipOutputStream.close();
        return byteOutputStream;
    }

    /**
     * Create one file csv for the list of submissionCodes with date of available equal to dateTimeFrom
     * @param submissionCodeDtoList list of submissionCodes for a day specific
     * @param date the date of available of submissionCode
     * @return submissionCodeDtoList parsed into a csv file
     */
    private File transformInFile(List<SubmissionCodeDto> submissionCodeDtoList, OffsetDateTime date) throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, IOException {

        // name of the file should be built from the date at target Zone publication
        final OffsetDateTime nowInParis = OffsetDateTime.now(ZoneId.of(this.targetZoneId));
        final ZoneOffset offsetInParis = nowInParis.getOffset();
        String fileName = date.withOffsetSameInstant(offsetInParis).format(DateTimeFormatter.ISO_LOCAL_DATE) + ".csv";

        // converting list SubmissionCodeDto to SubmissionCodeCsvDto to be proceeded in csv generator
        final List<SubmissionCodeCsvDto> submissionCodeCsvDtos = convert(submissionCodeDtoList);

        File file = new File(fileName);
        StringWriter fileWriter = new StringWriter();
        fileWriter.append(HEADER_CSV);
        ColumnPositionMappingStrategy mappingStrategy = new ColumnPositionMappingStrategy();
        mappingStrategy.setType(SubmissionCodeCsvDto.class);

        String[] columns = new String[]{"qrcode", "code", "dateAvailable", "dateEndValidity"};
        mappingStrategy.setColumnMapping(columns);
        StatefulBeanToCsvBuilder<SubmissionCodeDto> builder = new StatefulBeanToCsvBuilder<>(fileWriter);
        StatefulBeanToCsv statefulBeanToCsv = builder.withMappingStrategy(mappingStrategy).build();
        statefulBeanToCsv.write(submissionCodeCsvDtos);

        byte[] bytesArray = fileWriter.toString().getBytes("UTF-8");
        OutputStream os = new FileOutputStream(file);
        os.write(bytesArray);
        os.close();
        return file;
    }

    /**
     * @param dateFrom
     * @param dateTo
     * @throws Exception
     */
    private void validationDates(OffsetDateTime dateFrom, OffsetDateTime dateTo) throws Exception {

        if(OffsetDateTime.now().toLocalDate().compareTo(dateFrom.toLocalDate()) == 1 || dateFrom.isAfter(dateTo)){
            throw new Exception("Dates not Corrects");
        }
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
}
