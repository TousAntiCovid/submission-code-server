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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import javax.activation.UnsupportedDataTypeException;
import javax.inject.Inject;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GenerateServiceImpl implements IGenerateService {
    private final IUUIDv4CodeService uuiDv4CodeService;
    private final ISubmissionCodeService submissionCodeService;
    private final IAlphaNumericCodeService alphaNumericCodeService;

    /**
     * Number of code that should be generated per days for a given lot.
     * it is set in application.properties file
     */
    @Value("${generation.code.bulk.num.of.code}")
    private long NUMBER_OF_UUIDv4_PER_CALL;

    /**
     * Number of try to generate a new code in case of the code is already in db
     * it is set in application.properties file
     */
    @Value("${generation.code.num.of.tries}")
    private long NUMBER_OF_TRY_IN_CASE_OF_ERROR;

    /**
     * Interval in minutes of the validity of an UUIDv4 code
     * it is set in application.properties file
     */
    @Value("${generation.code.uuid.validity.minutes}")
    private long TIME_VALIDITY_UUID;

    /**
     * Interval in minutes of the validity of a 6-alphanum code
     * it is set in application.properties file
     */
    @Value("${generation.code.alpha.num.6.validity.minutes}")
    private long TIME_VALIDITY_ALPHANUM;


    /**
     * Default constructor
     * @param uuiDv4CodeService Spring-injection of the uuiDv4CodeService generating the code of type UUIDv4
     * @param alphaNumericCodeService Spring-injection of the alphaNumericCodeService generating the code of type 6-alphanum
     * @param submissionCodeService Spring-injection of the alphaNumericCodeService giving access to persistence in db.
     */
    @Inject
    public GenerateServiceImpl(IUUIDv4CodeService uuiDv4CodeService,
                               IAlphaNumericCodeService alphaNumericCodeService,
                               ISubmissionCodeService submissionCodeService)
    {
        this.alphaNumericCodeService= alphaNumericCodeService;
        this.uuiDv4CodeService = uuiDv4CodeService;
        this.submissionCodeService = submissionCodeService;
    }

    @Override
    public List<GenerateResponseDto> generateUUIDv4Codes(long size)
    {
        //TODO: Verify that code don't exist in DB before returning
        return this.generateCodeGeneric(size, CodeTypeEnum.UUIDv4);
    }

    @Override
    public List<GenerateResponseDto> generateAlphaNumericCode()
    {
        //TODO: Verify that code don't exist in DB before returning
        return this.generateCodeGeneric(1, CodeTypeEnum.ALPHANUM_6);
    }

    @Override
    public List<GenerateResponseDto> generateCodeFromRequest(GenerateRequestVo generateRequestVo)
            throws UnsupportedDataTypeException
    {
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

    @Override
    public List<GenerateResponseDto> generateCodeGeneric(final long size,
                                                         final CodeTypeEnum cte)
    {
        long lot = 0;
        if(CodeTypeEnum.UUIDv4.equals(cte)) lot = this.submissionCodeService.nextLot();
        return generateCodeGeneric(size, cte, lot);
    }

    @Override
    public List<GenerateResponseDto> generateCodeGeneric(final long size,
                                                         final CodeTypeEnum cte,
                                                         final long lot)
    {
        return generateCodeGeneric(size, cte, OffsetDateTime.now(), lot);
    }

    @Override
    public List<GenerateResponseDto> generateCodeGeneric(final long size,
                                                         final CodeTypeEnum cte,
                                                         final OffsetDateTime validFrom)
    {
        return generateCodeGeneric(size, cte, validFrom, this.submissionCodeService.nextLot());
    }

    @Override
    public List<GenerateResponseDto> generateCodeGeneric(final long size,
                                                         final CodeTypeEnum cte,
                                                         final OffsetDateTime validFrom,
                                                         final long lot)
    {
        final ArrayList<GenerateResponseDto> generateResponseList = new ArrayList<>();

        long failCount = 1;
        /*
            The date available/validfrom is date of now in this time
         */
        OffsetDateTime validUntil;
        OffsetDateTime validGenDate= OffsetDateTime.now();

        for (int i = 0; i < size && failCount <= NUMBER_OF_TRY_IN_CASE_OF_ERROR; ) {
            String code;

            if(CodeTypeEnum.UUIDv4.equals(cte)) {
                code = this.uuiDv4CodeService.generateCode();
                validUntil= getValidityDateUUIDCode(validFrom);
            } else if (CodeTypeEnum.ALPHANUM_6.equals(cte)) {
                code = this.alphaNumericCodeService.generateCode();
                validUntil= getValidityDateAlphaNum6(validFrom);
            } else {
                return generateResponseList;
            }

            SubmissionCodeDto submissionCodeDto = SubmissionCodeDto.builder()
                    .code(code)
                    .type(cte.getTypeCode())
                    .dateAvailable(validFrom)
                    .dateEndValidity(validUntil)
                    .dateGeneration(validGenDate)
                    .used(false)
                    .lot(lot)
                    .build();

            try {
                final SubmissionCode sc = this.submissionCodeService.saveCode(submissionCodeDto);
                generateResponseList.add(GenerateResponseDto.builder()
                        .code(sc.getCode())
                        .typeAsString(cte.getType())
                        .typeAsInt(Integer.parseInt(cte.getTypeCode()))
                        .validFrom(sc.getDateAvailable() != null ? formatOffsetDateTime(sc.getDateAvailable()) : "")
                        .validUntil(sc.getDateAvailable() != null ? formatOffsetDateTime(sc.getDateEndValidity()) : "")
                        .build()
                );
                i++;
                failCount = 0;
            } catch (DataIntegrityViolationException divException) {
                // TODO: caught dedicated error here.
                // TODO: Handle only not unique error here.
                log.error("code generated is not unique try  -> {}/{}", failCount, NUMBER_OF_TRY_IN_CASE_OF_ERROR);
                failCount++;
            }
        }
        return generateResponseList;
    }

    @Override
    public List<GenerateResponseDto> generateUUIDv4CodesBulk()
    {
        return this.generateUUIDv4CodesBulk(OffsetDateTime.now());
    }

    @Override
    public List<GenerateResponseDto> generateUUIDv4CodesBulk(final OffsetDateTime validFrom)
    {
        long lot = this.submissionCodeService.nextLot();
        return generateUUIDv4CodesBulk(validFrom, lot);
    }

    @Override
    public List<GenerateResponseDto> generateUUIDv4CodesBulk(final OffsetDateTime validFrom,
                                                             final long lot)
    {
        OffsetDateTime validGenDate = OffsetDateTime.now();

        final List<SubmissionCodeDto> submissionCodeDtos = this.uuiDv4CodeService
                .generateCodes(NUMBER_OF_UUIDv4_PER_CALL)
                .stream()
                .map(code ->
                        SubmissionCodeDto.builder()
                                .code(code)
                                .type(CodeTypeEnum.UUIDv4.getTypeCode())
                                .dateGeneration(validGenDate)
                                .dateAvailable(validFrom)
                                .dateEndValidity(getValidityDateUUIDCode(validFrom))
                                .lot(lot)
                                .used(false)
                                .build()
                )
                .collect(Collectors.toList());

        final Iterable<SubmissionCode> submissionCodes = this.submissionCodeService.saveAllCodes(submissionCodeDtos);
        return IterableUtils.toList(submissionCodes).stream()
                .map(sc -> GenerateResponseDto.builder()
                        .code(sc.getCode())
                        .typeAsString(CodeTypeEnum.UUIDv4.getType())
                        .typeAsInt(Integer.parseInt(CodeTypeEnum.UUIDv4.getTypeCode()))
                        .validFrom(sc.getDateAvailable() != null ? formatOffsetDateTime(sc.getDateAvailable()) : "")
                        .validUntil(sc.getDateAvailable() != null ? formatOffsetDateTime(sc.getDateEndValidity()) : "")
                        .build()
                )
                .collect(Collectors.toList());
    }

    /**
     * Method formatting date with standard API date pattern "AAAA-MM-ddThh:mm:ssZ"
     * @param date value to be stringified.
     * @return ISO instant formatter of date in parameter
     */
    private String formatOffsetDateTime(OffsetDateTime date)
    {
        return date.format(DateTimeFormatter.ISO_INSTANT);
    }

    /**
     * Method gives the validUntil date of UUIDv4 code from the date given in parameter.
     * It calculates the validity end date of the code using value set in application.properties and inject by Spring. {@link #TIME_VALIDITY_UUID}
     * @param validFrom the OffsetDateTime start validity applied to calculate end of UUIDv4 code validity
     * @return OffsetDateTime corresponding to the "validFrom" plus minutes in {@link #TIME_VALIDITY_UUID}
     */
    private OffsetDateTime getValidityDateUUIDCode(OffsetDateTime validFrom)
    {
        return validFrom.plusMinutes(TIME_VALIDITY_UUID);
    }

    /**
     * Method gives the validUntil date of 6-alphanum code from the date given in parameter.
     * It calculates the validity end date of the code using value set in application.properties and inject by Spring. {@link #TIME_VALIDITY_ALPHANUM}
     * @param validFrom the OffsetDateTime start validity applied to calculate end of 6-alphanum code validity
     * @return OffsetDateTime corresponding to the "validFrom" plus minutes in {@link #TIME_VALIDITY_ALPHANUM}
     */
    private OffsetDateTime getValidityDateAlphaNum6(OffsetDateTime validFrom)
    {
        return validFrom.plusMinutes(TIME_VALIDITY_ALPHANUM);
    }

}
