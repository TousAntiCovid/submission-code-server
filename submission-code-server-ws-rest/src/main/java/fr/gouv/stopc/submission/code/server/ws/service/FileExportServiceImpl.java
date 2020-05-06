package fr.gouv.stopc.submission.code.server.ws.service;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import fr.gouv.stopc.submission.code.server.database.dto.SubmissionCodeDto;
import fr.gouv.stopc.submission.code.server.database.service.ISubmissionCodeService;
import fr.gouv.stopc.submission.code.server.ws.enums.CodeTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.*;
import java.time.OffsetDateTime;
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
    public static final String HEADER_CSV = "Lot, Code, Type, DateEndValidity, DateAvailable \n";
    private ISubmissionCodeService submissionCodeService;
    private IGenerateService generateService;

    @Inject
    public FileExportServiceImpl(ISubmissionCodeService submissionCodeService, IGenerateService generateService){
        this.submissionCodeService = submissionCodeService;
        this.generateService=generateService;
    }


    @Override
    public Optional<ZipOutputStream> zipExport(String numberCodeDay, String lot, String dateFrom, String dateTo) {


        OffsetDateTime dateTimeFrom;
        OffsetDateTime dateTimeTo = null;
        List<File> files = new ArrayList<>();
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteOutputStream);

        try {

            dateTimeFrom= OffsetDateTime.parse(dateFrom, DateTimeFormatter.ISO_DATE_TIME);
            dateTimeTo= OffsetDateTime.parse(dateTo, DateTimeFormatter.ISO_DATE_TIME);
            validationDates(dateTimeFrom,dateTimeTo);

            //create codes
            int diffDays= Math.toIntExact(ChronoUnit.DAYS.between(dateTimeFrom,dateTimeTo));
            List<OffsetDateTime> datesFromList = generateService.getValidFromList(diffDays, dateTimeFrom);

            for(OffsetDateTime dateFromDay: datesFromList) {
                generateService.generateCodeGeneric(Long.parseLong(numberCodeDay), CodeTypeEnum.UUIDv4, dateFromDay, Long.parseLong(lot));
            }

            //get codes
            List<SubmissionCodeDto> submissionCodeDtos = submissionCodeService.getCodeUUIDv4CodesForCsv(lot, CodeTypeEnum.UUIDv4.getTypeCode());
            if (CollectionUtils.isEmpty(submissionCodeDtos)){
                return Optional.empty();
            }
             while(dateTimeFrom.isBefore(dateTimeTo)){
                OffsetDateTime finalDateTimeFrom = dateTimeFrom;
                List<SubmissionCodeDto> listForDay= submissionCodeDtos.stream().filter(tmp-> tmp.getDateAvailable().isEqual(finalDateTimeFrom)).collect(Collectors.toList());
                File file= transformInFile(listForDay, dateTimeFrom);
                files.add(file);
                dateTimeFrom=dateTimeFrom.plusDays(1L);
            }

            for (File file: files){
                zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
                FileInputStream fileInputStream = new FileInputStream(file);
                IOUtils.copy(fileInputStream, zipOutputStream);
                fileInputStream.close();
                zipOutputStream.closeEntry();
            }
            zipOutputStream.close();
        }
        catch (IOException i){
            log.info("Problem de conversion et creation file");
        }
        catch (Exception e) {
            log.info("Problem create CSV");
        }
        return  Optional.of(zipOutputStream);
    }

    private File transformInFile(List<SubmissionCodeDto> listForDay, OffsetDateTime dateTimeFrom) throws IOException {

        String fileName = dateTimeFrom.format(DateTimeFormatter.ISO_DATE) + ".csv";
        File file = new File(fileName);
        OutputStream os = null;
        try {

            StringWriter fileWriter = new StringWriter();

            fileWriter.append(HEADER_CSV);
            ColumnPositionMappingStrategy mappingStrategy = new ColumnPositionMappingStrategy();
            mappingStrategy.setType(SubmissionCodeDto.class);

            String[] columns = new String[]{"lot", "code", "type", "dateEndValidity", "dateAvailable"};
            mappingStrategy.setColumnMapping(columns);

            StatefulBeanToCsvBuilder<SubmissionCodeDto> builder = new StatefulBeanToCsvBuilder<>(fileWriter);
            StatefulBeanToCsv statefulBeanToCsv = builder.withMappingStrategy(mappingStrategy).build();
            statefulBeanToCsv.write(listForDay);
            byte[] bytesArray = fileWriter.toString().getBytes("UTF-8");
            os = new FileOutputStream(file);
            os.write(bytesArray);
            os.close();
        }
        catch (Exception e){
            throw new IOException();
        }
        return file;
    }

    private void validationDates(OffsetDateTime dateFrom, OffsetDateTime dateTo) throws Exception {
        if(OffsetDateTime.now().compareTo(dateFrom) ==1 || dateFrom.isAfter(dateTo)){
            throw new Exception("Dates not Corrects");
        }


    }
}
