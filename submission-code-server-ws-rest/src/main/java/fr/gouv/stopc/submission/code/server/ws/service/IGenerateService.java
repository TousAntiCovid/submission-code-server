package fr.gouv.stopc.submission.code.server.ws.service;

import fr.gouv.stopc.submission.code.server.ws.dto.GenerateResponseDto;
import fr.gouv.stopc.submission.code.server.ws.vo.GenerateRequestVo;

import javax.activation.UnsupportedDataTypeException;
import java.util.List;

public interface IGenerateService {
    /**
     * @return UUIDv4 code certified unique in DB
     */
    List<GenerateResponseDto> generateUUIDv4Codes(final long size);

    /**
     * @return alphanum-6 code certified unique in DB
     */
    List<GenerateResponseDto> generateAlphaNumericCode();

    /**
     * Method calls {@link #generateAlphaNumericCode()} if #TypeEnum.ALPHANUM_6
     * Method calls {@link #generateUUIDv4Codes(long)} )} if #TypeEnum.UUIDv4
     * @param generateRequestVo generatedRequestVo containing the type of code to be generated.
     * @return return a list of codes depending of the generateRequestVo given in parameter.
     * @throws UnsupportedDataTypeException in case of the GeneratedRequestVo is not processable.
     */
    List<GenerateResponseDto> generateCode(final GenerateRequestVo generateRequestVo) throws UnsupportedDataTypeException;
}
