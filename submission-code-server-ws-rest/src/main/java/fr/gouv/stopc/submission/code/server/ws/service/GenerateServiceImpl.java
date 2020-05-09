package fr.gouv.stopc.submission.code.server.ws.service;

import fr.gouv.stopc.submission.code.server.commun.service.IAlphaNumericCodeService;
import fr.gouv.stopc.submission.code.server.commun.service.IUUIDv4CodeService;
import fr.gouv.stopc.submission.code.server.database.dto.SubmissionCodeDto;
import fr.gouv.stopc.submission.code.server.database.entity.Lot;
import fr.gouv.stopc.submission.code.server.database.entity.SubmissionCode;
import fr.gouv.stopc.submission.code.server.database.service.ISubmissionCodeService;
import fr.gouv.stopc.submission.code.server.ws.dto.GenerateResponseDto;
import fr.gouv.stopc.submission.code.server.ws.enums.CodeTypeEnum;
import fr.gouv.stopc.submission.code.server.ws.errors.NumberOfTryGenerateCodeExceededExcetion;
import fr.gouv.stopc.submission.code.server.ws.vo.GenerateRequestVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import javax.activation.UnsupportedDataTypeException;
import javax.inject.Inject;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GenerateServiceImpl implements IGenerateService {
    private final IUUIDv4CodeService uuiDv4CodeService;
    private final ISubmissionCodeService submissionCodeService;
    private final IAlphaNumericCodeService alphaNumericCodeService;

    @Value("${stop.covid.qr.code.target.zone}")
    private String TARGET_ZONE_ID;
    
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
     * Interval in days of the validity of an UUIDv4 code
     * it is set in application.properties file
     */
    @Value("${generation.code.uuid.validity.days}")
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
            throws NumberOfTryGenerateCodeExceededExcetion
    {
        //TODO: Verify that code don't exist in DB before returning
        return this.generateCodeGeneric(size, CodeTypeEnum.UUIDv4);
    }

    @Override
    public List<GenerateResponseDto> generateAlphaNumericCode()
            throws NumberOfTryGenerateCodeExceededExcetion
    {
        //TODO: Verify that code don't exist in DB before returning
        return this.generateCodeGeneric(1, CodeTypeEnum.ALPHANUM_6);
    }

    @Override
    public List<GenerateResponseDto> generateCodeFromRequest(GenerateRequestVo generateRequestVo)
            throws UnsupportedDataTypeException,
            NumberOfTryGenerateCodeExceededExcetion
    {
        if(generateRequestVo == null || Strings.isBlank(generateRequestVo.getType())) {
            throw new UnsupportedDataTypeException();

        } else if (CodeTypeEnum.UUIDv4.equals(generateRequestVo.getType())) {

            return this.generateUUIDv4Codes(NUMBER_OF_UUIDv4_PER_CALL);

        } else if (CodeTypeEnum.ALPHANUM_6.equals(generateRequestVo.getType())) {

            return this.generateAlphaNumericCode();
        }
        throw new UnsupportedDataTypeException();
    }

    @Override
    public List<GenerateResponseDto> generateCodeGeneric(final long size,
                                                         final CodeTypeEnum cte)
            throws NumberOfTryGenerateCodeExceededExcetion
    {
        return generateCodeGeneric(size, cte, OffsetDateTime.now());
    }

    @Override
    public List<GenerateResponseDto> generateCodeGeneric(final long size,
                                                         final CodeTypeEnum cte,
                                                         final OffsetDateTime validFrom)
            throws NumberOfTryGenerateCodeExceededExcetion
    {
        return this.generateCodeGeneric(size, cte, validFrom, new Lot());
    }

    @Override
    public List<GenerateResponseDto> generateCodeGeneric(final long size,
                                                         final CodeTypeEnum cte,
                                                         final OffsetDateTime validFrom,
                                                         final Lot lotObject)
            throws NumberOfTryGenerateCodeExceededExcetion
    {
        final ArrayList<GenerateResponseDto> generateResponseList = new ArrayList<>();

        long failCount = 1;
        /*
            The date available/validfrom is date of now in this time
         */
        OffsetDateTime validUntil;
        OffsetDateTime validGenDate= OffsetDateTime.now();

        for (int i = 0; i < size && failCount <= NUMBER_OF_TRY_IN_CASE_OF_ERROR + 1; ) {
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

            log.info("generating code {}", code);

            SubmissionCodeDto submissionCodeDto = SubmissionCodeDto.builder()
                    .code(code)
                    .type(cte.getTypeCode())
                    .dateAvailable(validFrom)
                    .dateEndValidity(validUntil)
                    .dateGeneration(validGenDate)
                    .used(false)
                    .build();

            try {
                final SubmissionCode sc = this.submissionCodeService.saveCode(submissionCodeDto, lotObject).get();
                lotObject.setId(sc.getLotkey().getId());
                generateResponseList.add(GenerateResponseDto.builder()
                        .code(sc.getCode())
                        .typeAsString(cte.getType())
                        .typeAsInt(Integer.parseInt(cte.getTypeCode()))
                        .validFrom(sc.getDateAvailable() != null ? formatOffsetDateTime(sc.getDateAvailable()) : "")
                        .validUntil(sc.getDateAvailable() != null ? formatOffsetDateTime(sc.getDateEndValidity()) : "")
                        .build()
                );
                i++;
                failCount = 1;
            } catch (DataIntegrityViolationException divException) {
                log.error("code generated is not unique try  -> {}/{}", failCount, NUMBER_OF_TRY_IN_CASE_OF_ERROR);
                failCount++;
            }
            // In case of tries of generating code were exceeded an error should be raised.
            log.info("20200305 -- failCount {}", failCount);
            if(failCount > 1 ) throw new NumberOfTryGenerateCodeExceededExcetion();
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
                                .used(false)
                                .build()
                )
                .collect(Collectors.toList());

        final Iterable<SubmissionCode> submissionCodes = this.submissionCodeService.saveAllCodes(submissionCodeDtos, new Lot());
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

    @Override
    public List<OffsetDateTime> getValidFromList(int size, OffsetDateTime validFromFirstValue)
    {
        final ArrayList<OffsetDateTime> validFromList = new ArrayList<>();


        // convert to zulu zoneoffset
        validFromList.add(validFromFirstValue.withOffsetSameInstant(ZoneOffset.of("Z")));

        final OffsetDateTime nowInParis = OffsetDateTime.now(ZoneId.of(this.TARGET_ZONE_ID));
        final ZoneOffset offsetInParis = nowInParis.getOffset();

        // assuring validFromFirstValue is OffsetInParis
        validFromFirstValue = validFromFirstValue.withOffsetSameInstant(offsetInParis);

        for (int i = 1; i < size; i++) {
            // OffsetDateTime is immutable so it can be copy
            OffsetDateTime oft = validFromFirstValue;

            // set 00 after day (2020-05-03T23:48:24.830+02:00 -> 2020-05-03T00:00+02:00)
            oft = oft.truncatedTo(ChronoUnit.DAYS);

            // add incremental day  e.g. : 2020-05-03T00:00+02:00 + (i = 2) -> 2020-05-05T00:00+02:00 + 2
            oft = oft.plusDays(i);

            // converting to zulu zone offset
            oft = oft.withOffsetSameInstant(ZoneOffset.of("Z"));

            validFromList.add(oft);
        }
        return validFromList;
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
     * The ValidUntil should be hh:mm formatted as 23:59 in paris.
     * @param validFrom the OffsetDateTime start validity applied to calculate end of UUIDv4 code validity
     * @return OffsetDateTime corresponding to the "validFrom" plus days in {@link #TIME_VALIDITY_UUID} at Zulu Offset
     */
    private OffsetDateTime getValidityDateUUIDCode(OffsetDateTime validFrom)
    {
        // ensuring that validFrom is based
        validFrom =  validFrom.withOffsetSameInstant(this.getParisOffset());
        return validFrom
                .withOffsetSameInstant(this.getParisOffset())
                .plusDays(TIME_VALIDITY_UUID + 1)
                .truncatedTo(ChronoUnit.DAYS)
                .minusMinutes(1)
                .withOffsetSameInstant(this.getZuluOffset());
    }

    /**
     * @return return current offset of paris
     */
    private ZoneOffset getParisOffset() {
        return OffsetDateTime.now(ZoneId.of(this.TARGET_ZONE_ID)).getOffset();
    }

    /**
     * @return return current offset of zulu
     */
    private ZoneOffset getZuluOffset() {
        return ZoneOffset.of("Z");
    }


    /**
     * Method gives the validUntil date of 6-alphanum code from the date given in parameter.
     * It calculates the validity end date of the code using value set in application.properties and inject by Spring. {@link #TIME_VALIDITY_ALPHANUM}
     * @param validFrom the OffsetDateTime start validity applied to calculate end of 6-alphanum code validity
     * @return OffsetDateTime corresponding to the "validFrom" plus minutes in {@link #TIME_VALIDITY_ALPHANUM} at Zulu Offset
     */
    private OffsetDateTime getValidityDateAlphaNum6(OffsetDateTime validFrom)
    {
        return validFrom
                .plusMinutes(TIME_VALIDITY_ALPHANUM)
                .truncatedTo(ChronoUnit.MINUTES)
                .withOffsetSameInstant(this.getZuluOffset());
    }

}
