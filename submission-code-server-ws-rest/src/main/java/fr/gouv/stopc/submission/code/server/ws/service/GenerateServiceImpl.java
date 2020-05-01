package fr.gouv.stopc.submission.code.server.ws.service;

import fr.gouv.stopc.submission.code.server.commun.service.IAlphaNumericCodeService;
import fr.gouv.stopc.submission.code.server.commun.service.IUUIDv4CodeService;
import fr.gouv.stopc.submission.code.server.ws.dto.GenerateResponseDto;
import fr.gouv.stopc.submission.code.server.ws.enums.CodeTypeEnum;
import fr.gouv.stopc.submission.code.server.ws.vo.GenerateRequestVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.activation.UnsupportedDataTypeException;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GenerateServiceImpl implements IGenerateService {
    private IUUIDv4CodeService uuiDv4CodeService;
    private IAlphaNumericCodeService alphaNumericCodeService;

    private static long NUMBER_OF_UUIDv4_PER_CALL = new Long("300");

    @Inject
    public GenerateServiceImpl(
            IUUIDv4CodeService uuiDv4CodeService,
            IAlphaNumericCodeService alphaNumericCodeService
    ){
        this.alphaNumericCodeService= alphaNumericCodeService;
        this.uuiDv4CodeService = uuiDv4CodeService;
    }

    @Override
    public List<GenerateResponseDto> generateUUIDv4Codes(long size) {
        //TODO: Verify that code don't exist in DB before returning
        return this.uuiDv4CodeService.generateCodes(size)
                .stream()
                .map(code ->
                        GenerateResponseDto.builder()
                                .code(code)
                                .build()
                )
                .collect(Collectors.toList());
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
