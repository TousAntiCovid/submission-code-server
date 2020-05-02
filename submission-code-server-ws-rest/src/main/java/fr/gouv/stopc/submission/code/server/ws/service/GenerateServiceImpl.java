package fr.gouv.stopc.submission.code.server.ws.service;

import fr.gouv.stopc.submission.code.server.commun.service.IAlphaNumericCodeService;
import fr.gouv.stopc.submission.code.server.commun.service.IUUIDv4CodeService;
import fr.gouv.stopc.submission.code.server.database.dto.SubmissionCodeDto;
import fr.gouv.stopc.submission.code.server.database.entity.SubmissionCode;
import fr.gouv.stopc.submission.code.server.database.service.ISubmissionCodeService;
import fr.gouv.stopc.submission.code.server.ws.dto.GenerateResponseDto;
import fr.gouv.stopc.submission.code.server.ws.enums.CodeTypeEnum;
import fr.gouv.stopc.submission.code.server.ws.vo.GenerateRequestVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.activation.UnsupportedDataTypeException;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GenerateServiceImpl implements IGenerateService {
    private final IUUIDv4CodeService uuiDv4CodeService;
    private final ISubmissionCodeService submissionCodeService;
    private final IAlphaNumericCodeService alphaNumericCodeService;

    @Value("${generation.code.bulk.num.of.code}")
    private long NUMBER_OF_UUIDv4_PER_CALL;

    @Inject
    public GenerateServiceImpl(
            IUUIDv4CodeService uuiDv4CodeService,
            IAlphaNumericCodeService alphaNumericCodeService,
            ISubmissionCodeService submissionCodeService
    ){
        this.alphaNumericCodeService= alphaNumericCodeService;
        this.uuiDv4CodeService = uuiDv4CodeService;
        this.submissionCodeService = submissionCodeService;
    }

    private List<GenerateResponseDto> test(final long size) {
        final List<SubmissionCodeDto> submissionCodeDtos = this.uuiDv4CodeService.generateCodes(size)
                .stream()
                .map(code ->
                        SubmissionCodeDto.builder()
                                .code(code)
                                .type("1")
                                .build()
                )
                .collect(Collectors.toList());

        final Iterable<SubmissionCode> submissionCodes = this.submissionCodeService.saveAllCodeGenerateByBatch(submissionCodeDtos);
        return IterableUtils.toList(submissionCodes).stream()
                .map(sc -> GenerateResponseDto.builder()
                        .code(sc.getCode())
                        .typeAsString(sc.getCode())
                        .validFrom(sc.getDateAvailable() != null ? sc.getDateAvailable().toString() : "")
                        .validUntil(sc.getDateAvailable() != null ? sc.getDateEndValidity().toString() : "")
                        .build()
                )
                .collect(Collectors.toList());
    }

    @Override
    public List<GenerateResponseDto> generateUUIDv4Codes(long size) {
        //TODO: Verify that code don't exist in DB before returning
        final ArrayList<GenerateResponseDto> generateResponseList = new ArrayList<>();
        for (int i = 0; i < size; ) {

            final String code = this.uuiDv4CodeService.generateCode();
            SubmissionCodeDto submissionCodeDto = SubmissionCodeDto.builder()
                    .code(code)
                    .type("1")
                    .build();
            try {
                final SubmissionCode sc = this.submissionCodeService.saveCodeGenerate(submissionCodeDto);
                generateResponseList.add(GenerateResponseDto.builder()
                        .code(sc.getCode())
                        .typeAsString(sc.getCode())
                        .validFrom(sc.getDateAvailable() != null ? sc.getDateAvailable().toString() : "")
                        .validUntil(sc.getDateAvailable() != null ? sc.getDateEndValidity().toString() : "")
                        .build()
                );
                i++;
            } catch (Exception e) {
                log.error("Caught error : ", e);
            }
        }
        return generateResponseList;
    }




    @Override
    public List<GenerateResponseDto> generateAlphaNumericCode() {
        //TODO: Verify that code don't exist in DB before returning
        return Arrays.asList(GenerateResponseDto
                .builder()
                .code(this.alphaNumericCodeService.generateCode())
                .build()
        );
    }

    @Override
    public List<GenerateResponseDto> generateCode(GenerateRequestVo generateRequestVo) throws UnsupportedDataTypeException {
        if(generateRequestVo == null || generateRequestVo.getType() == null) {
            //TODO unsupportedError
            throw new UnsupportedDataTypeException();

        } else if (CodeTypeEnum.UUIDv4.equals(generateRequestVo.getType())) {

            return this.generateUUIDv4Codes(NUMBER_OF_UUIDv4_PER_CALL);

        } else if (CodeTypeEnum.ALPHANUM_6.equals(generateRequestVo.getType())) {

            return this.generateAlphaNumericCode();
        }

        //TODO unsupportedError
        throw new UnsupportedDataTypeException();
    }
}
