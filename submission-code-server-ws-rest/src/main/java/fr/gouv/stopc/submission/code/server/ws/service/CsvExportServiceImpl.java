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
import java.io.StringWriter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Slf4j
@Service
public class CsvExportServiceImpl implements ICsvService {
    public static final String HEADER_CSV = "Lot, Code, Type, DateEndValidity, DateAvailable \n";
    private ISubmissionCodeService submissionCodeService;

    @Inject
    public CsvExportServiceImpl(ISubmissionCodeService submissionCodeService){
        this.submissionCodeService = submissionCodeService;
    }


    @Override
    public Optional<StringWriter> csvExport(String lots) {
        List<SubmissionCodeDto> submissionCodeDtos = submissionCodeService.getCodeUUIDv4CodesForCsv(lots, CodeTypeEnum.UUIDv4.getTypeCode());
        if (Objects.isNull(submissionCodeDtos) || submissionCodeDtos.isEmpty()){
            return Optional.empty();
        }
        StringWriter fileWriter = new StringWriter();
        try {
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
            log.info("Problem create CSV");
        }
        return  Optional.of(fileWriter);
    }
}
