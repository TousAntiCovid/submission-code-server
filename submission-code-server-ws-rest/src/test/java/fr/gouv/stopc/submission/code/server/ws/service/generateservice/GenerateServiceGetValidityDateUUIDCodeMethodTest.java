package fr.gouv.stopc.submission.code.server.ws.service.generateservice;

import fr.gouv.stopc.submission.code.server.ws.dto.GenerateResponseDto;
import fr.gouv.stopc.submission.code.server.commun.enums.CodeTypeEnum;
import fr.gouv.stopc.submission.code.server.ws.errors.NumberOfTryGenerateCodeExceededExcetion;
import fr.gouv.stopc.submission.code.server.ws.service.GenerateServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@Slf4j
@SpringBootTest
public class GenerateServiceGetValidityDateUUIDCodeMethodTest {


    @Value("${stop.covid.qr.code.target.zone}")
    private String targetZoneId;

    /**
     * GenerateServiceImpl No need of external services here.
     */
    @Autowired
    private GenerateServiceImpl gsi;

    @Test
    void checkValidUntilFormatTest() throws NumberOfTryGenerateCodeExceededExcetion {

        final long validityDays = 10;
        ReflectionTestUtils.setField(this.gsi, "TIME_VALIDITY_UUID", validityDays);
        ReflectionTestUtils.setField(this.gsi, "TARGET_ZONE_ID", "Europe/Paris");

        OffsetDateTime testedValidFrom = OffsetDateTime.now(ZoneId.of(this.targetZoneId));


        testedValidFrom = testedValidFrom.withMonth(01).withDayOfMonth(01).withHour(1).withMinute(12).truncatedTo(ChronoUnit.MINUTES);

        final List<GenerateResponseDto> grdList = this.gsi.generateCodeGeneric(1, CodeTypeEnum.UUIDv4, testedValidFrom);

        assertFalse(grdList.isEmpty());

        final GenerateResponseDto grd = grdList.get(0);

        log.info("GenerateResponseDto : {}", grd);

        final OffsetDateTime validUntil = OffsetDateTime.parse(grd.getValidUntil())
                .withOffsetSameInstant(
                        OffsetDateTime.now(ZoneId.of(this.targetZoneId)).getOffset()
                );

        final OffsetDateTime validFrom= OffsetDateTime.parse(grd.getValidFrom())
                .withOffsetSameInstant(
                        OffsetDateTime.now(ZoneId.of(this.targetZoneId)).getOffset()
                );

        // asserting Hours is 23
        assertEquals(23, validUntil.getHour());

        // asserting Minutes is 59
        assertEquals(59, validUntil.getMinute());

        // asserting truncate to minutes
        assertEquals(00, validUntil.getSecond());


        final long betweenSec = SECONDS.between(validFrom, validUntil);
        log.info("seconds between validFrom and validUntil : {}", betweenSec);
        final long betweenDays =  betweenSec / 60 / 60 / 24;
        log.info("days between validFrom and validUntil : {}", betweenDays);

        final int deltaMinutes = testedValidFrom.getHour() * 60 + testedValidFrom.getMinute();

        assertEquals((((validityDays + 1 )* 24 * 60) - deltaMinutes- 1 ) * 60 , betweenSec);



    }

}
