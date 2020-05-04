package fr.gouv.stopc.submission.code.server.ws.service;

import fr.gouv.stopc.submission.code.server.ws.dto.GenerateResponseDto;
import fr.gouv.stopc.submission.code.server.ws.enums.CodeTypeEnum;
import fr.gouv.stopc.submission.code.server.ws.errors.NumberOfTryGenerateCodeExceededExcetion;
import fr.gouv.stopc.submission.code.server.ws.vo.GenerateRequestVo;

import javax.activation.UnsupportedDataTypeException;
import java.time.OffsetDateTime;
import java.util.List;

public interface IGenerateService {
    /**
     * @return UUIDv4 code certified unique in DB
     */
    List<GenerateResponseDto> generateUUIDv4Codes(final long size) throws NumberOfTryGenerateCodeExceededExcetion;

    /**
     * @return alphanum-6 code certified unique in DB
     */
    List<GenerateResponseDto> generateAlphaNumericCode() throws NumberOfTryGenerateCodeExceededExcetion;

    /**
     * Method calls {@link #generateAlphaNumericCode()} if #TypeEnum.ALPHANUM_6
     * Method calls {@link #generateUUIDv4Codes(long)} )} if #TypeEnum.UUIDv4
     * @param generateRequestVo generatedRequestVo containing the type of code to be generated.
     * @return return a list of codes depending of the generateRequestVo given in parameter.
     * @throws UnsupportedDataTypeException in case of the GeneratedRequestVo is not processable.
     */
    List<GenerateResponseDto> generateCodeFromRequest(final GenerateRequestVo generateRequestVo) throws UnsupportedDataTypeException, NumberOfTryGenerateCodeExceededExcetion;

    /**
     * Method used to sequentially generate codes of codeType in parameter
     * Method used to sequentially generate codes of codeType in parameter
     * Calling method {@link #generateCodeGeneric(long, CodeTypeEnum, long)}
     * with lot value given by method nextLot() of db service.
     * @param size the desired number of code to be generated
     * @param cte the code type desired
     * @return list of unique persisted codes
     */
    List<GenerateResponseDto> generateCodeGeneric(final long size,
                                                  final CodeTypeEnum cte
    ) throws NumberOfTryGenerateCodeExceededExcetion;


    /**
     * Method used to sequentially generate codes of codeType in parameter
     * Method calling {@link #generateCodeGeneric(long, CodeTypeEnum, OffsetDateTime, long)}
     * with "validForm" parameter as actual date called in method.
     * @param size the desired number of code to be generated
     * @param cte the code type desired
     * @param lot identifier of the lot.
     * @return list of code generated and saved in db.
     */
    List<GenerateResponseDto> generateCodeGeneric(final long size,
                                                  final CodeTypeEnum cte,
                                                  final long lot
    ) throws NumberOfTryGenerateCodeExceededExcetion;

    /**
     * Method used to sequentially generate codes of codeType in parameter
     * Method calling {@link #generateCodeGeneric(long, CodeTypeEnum, OffsetDateTime, long)}
     * with "lot" parameter given by method nextLot() of db service.
     * @param size the desired number of code to be generated
     * @param cte the code type desired
     * @param validFrom date from the code should be valid.
     * @return list of code generated and saved in db.
     */
    List<GenerateResponseDto> generateCodeGeneric(final long size,
                                                  final CodeTypeEnum cte,
                                                  final OffsetDateTime validFrom
    ) throws NumberOfTryGenerateCodeExceededExcetion;

    /**
     * Method used to sequentially generate codes of codeType in parameter
     * @param size the desired number of code to be generated
     * @param cte the code type desired
     * @param validFrom date from the code should be valid.
     * @param lot identifier of the lot.
     * @return list of unique persisted codes
     */
    List<GenerateResponseDto> generateCodeGeneric(final long size,
                                                  final CodeTypeEnum cte,
                                                  final OffsetDateTime validFrom,
                                                  final long lot
    ) throws NumberOfTryGenerateCodeExceededExcetion;

    /**
     * Method calling {@link #generateUUIDv4CodesBulk(OffsetDateTime)} with validForm parameter as actual date called in method.
     * @return list of code generated and saved in db.
     */
    List<GenerateResponseDto> generateUUIDv4CodesBulk();

    /**
     * Method calling {@link #generateUUIDv4CodesBulk(OffsetDateTime, long)} with next lot identifier calculated
     * with the last one given by database service.
     * @param validFrom date from the code should be valid.
     * @return list of code generated and saved in db.
     */
    List<GenerateResponseDto> generateUUIDv4CodesBulk(final OffsetDateTime validFrom);

    /**
     * Method generates UUIDv4 code with saveAll method of repository
     * It generate a list of UUIDv4 in one shot and save it as a bulk.
     * @param validFrom date from the code should be valid.
     * @param lot identifier of the lot.
     * @return list of code generated and saved in db.
     */
    List<GenerateResponseDto> generateUUIDv4CodesBulk(final OffsetDateTime validFrom, final long lot);

    /**
     * Method return List of OffsetDateTime increment by day and truncate to day
     * @param size give size of the list to be returned included validFromFirstValue
     * @param validFromFirstValue seed time from the list should be generated from.
     * @return List of OffsetDateTime increment by day and truncate to day.
     */
     List<OffsetDateTime> getValidFromList(int size, OffsetDateTime validFromFirstValue);
}
