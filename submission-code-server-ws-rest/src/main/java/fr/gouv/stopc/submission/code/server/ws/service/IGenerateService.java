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

    List<GenerateResponseDto> generateCode(final GenerateRequestVo generateRequestVo) throws UnsupportedDataTypeException;
}
