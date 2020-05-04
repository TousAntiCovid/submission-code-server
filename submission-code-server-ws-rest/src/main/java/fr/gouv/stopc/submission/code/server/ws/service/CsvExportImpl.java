package fr.gouv.stopc.submission.code.server.ws.service;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import fr.gouv.stopc.submission.code.server.database.dto.SubmissionCodeDto;
import fr.gouv.stopc.submission.code.server.database.service.ISubmissionCodeService;
import fr.gouv.stopc.submission.code.server.ws.enums.CodeTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;


@Slf4j
@Service
public class CsvExportImpl  implements ICsvService {
    public static final String HEADER_CSV = "Lot, Code, Type, DateEndValidity, DateAvailable \n";
    private ISubmissionCodeService submissionCodeService;

    @Inject
    public CsvExportImpl (ISubmissionCodeService submissionCodeService){
        this.submissionCodeService = submissionCodeService;
    }


    @Override
    public File csvExport(String lots) {
        List<SubmissionCodeDto> submissionCodeDtos = submissionCodeService.getCodeUUIDv4CodesForCsv(lots, CodeTypeEnum.UUIDv4.getTypeCode());
        if (submissionCodeDtos.isEmpty()){
            return null;
        }
        PrintWriter fileWriter = null;
        File file= null;
        try {
            final String csvFile = "lot"+lots+".csv";
            file= new File(csvFile);
            fileWriter = new PrintWriter(file);
            fileWriter.append(HEADER_CSV);
            ColumnPositionMappingStrategy mappingStrategy = new ColumnPositionMappingStrategy();
            mappingStrategy.setType(SubmissionCodeDto.class);

            String[] columns = new String[]{"lot", "code", "type", "dateEndValidity", "dateAvailable"};
            mappingStrategy.setColumnMapping(columns);

            StatefulBeanToCsvBuilder<SubmissionCodeDto> builder = new StatefulBeanToCsvBuilder<>(fileWriter);
            StatefulBeanToCsv statefulBeanToCsv = builder.withMappingStrategy(mappingStrategy).build();
            statefulBeanToCsv.write(submissionCodeDtos);


        }
        catch (Exception e) {
            e.printStackTrace();
        }finally {
            fileWriter.close();
        }
        return  file;
    }
}
