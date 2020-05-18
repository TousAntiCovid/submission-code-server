package fr.gouv.stopc.submission.code.server.ws.service;

import fr.gouv.stopc.submission.code.server.commun.enums.CodeTypeEnum;
import fr.gouv.stopc.submission.code.server.database.entity.Lot;
import fr.gouv.stopc.submission.code.server.ws.controller.error.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.ws.dto.CodeDetailedDto;
import fr.gouv.stopc.submission.code.server.ws.dto.CodeSimpleDto;
import fr.gouv.stopc.submission.code.server.ws.vo.GenerateRequestVo;

import java.time.OffsetDateTime;
import java.util.List;

public interface IGenerateService {
    /**
     * @return UUIDv4 code certified unique in DB
     */
    List<CodeDetailedDto> generateUUIDv4Codes(final long size) throws SubmissionCodeServerException;

    /**
     * @return alphanum-6 code certified unique in DB
     */
    CodeSimpleDto generateAlphaNumericDetailedCode() throws SubmissionCodeServerException;

    CodeSimpleDto generateAlphaNumericShortCode()
            throws SubmissionCodeServerException;

    /**
     * Method calls {@link #generateAlphaNumericDetailedCode()} ()} if #TypeEnum.ALPHANUM_6
     * Method calls {@link #generateUUIDv4Codes(long)} )} if #TypeEnum.UUIDv4
     * @param generateRequestVo generatedRequestVo containing the type of code to be generated.
     * @return return a list of codes depending of the generateRequestVo given in parameter.
     * @throws SubmissionCodeServerException in case of the GeneratedRequestVo is not processable.
     */
    List<CodeDetailedDto> generateCodeFromRequest(final GenerateRequestVo generateRequestVo)
            throws SubmissionCodeServerException;


    /**
     * Method used to sequentially generate codes of codeType in parameter
     * Method used to sequentially generate codes of codeType in parameter
     * Calling method {@link #generateCodeGeneric(long, CodeTypeEnum)}
     * with lot value given by method nextLot() of db service.
     * @param size the desired number of code to be generated
     * @param cte the code type desired
     * @return list of unique persisted codes
     */
    List<CodeDetailedDto> generateCodeGeneric(final long size,
                                              final CodeTypeEnum cte
    ) throws SubmissionCodeServerException;


    /**
     * Method used to sequentially generate codes of codeType in parameter
     * @param size the desired number of code to be generated
     * @param cte the code type desired
     * @param validFrom date from the code should be valid.
     * @return list of unique persisted codes
     */
    List<CodeDetailedDto> generateCodeGeneric(final long size,
                                              final CodeTypeEnum cte,
                                              final OffsetDateTime validFrom
    ) throws SubmissionCodeServerException;


    /**
     * Method used to sequentially generate codes of codeType in parameter
     * @param size the desired number of code to be generated
     * @param cte the code type desired
     * @param validFrom date from the code should be valid.
     * @return list of unique persisted codes
     */
    List<CodeDetailedDto> generateCodeGeneric(final long size,
                                              final CodeTypeEnum cte,
                                              final OffsetDateTime validFrom,
                                              final Lot lotObject
    ) throws SubmissionCodeServerException;

    /**
     * Method calling {@link #generateUUIDv4CodesBulk(OffsetDateTime)} with validForm parameter as actual date called in method.
     * @return list of code generated and saved in db.
     */
    List<CodeDetailedDto> generateUUIDv4CodesBulk();

    /**
     * Method calling  with new lot identifier calculated
     * with the last one given by database service.
     * @param validFrom date from the code should be valid.
     * @return list of code generated and saved in db.
     */
    List<CodeDetailedDto> generateUUIDv4CodesBulk(final OffsetDateTime validFrom);

    /**
     * Method return List of OffsetDateTime increment by day and truncate to day
     * @param size give size of the list to be returned included validFromFirstValue
     * @param validFromFirstValue seed time from the list should be generated from.
     * @return List of OffsetDateTime increment by day and truncate to day.
     */
    List<OffsetDateTime> getValidFromList(int size, OffsetDateTime validFromFirstValue);
}
