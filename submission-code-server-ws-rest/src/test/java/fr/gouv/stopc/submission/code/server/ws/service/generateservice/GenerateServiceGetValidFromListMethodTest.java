package fr.gouv.stopc.submission.code.server.ws.service.generateservice;

import fr.gouv.stopc.submission.code.server.ws.service.GenerateServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.Assert.assertEquals;

@Slf4j
@SpringBootTest
public class GenerateServiceGetValidFromListMethodTest {

    @Value("${stop.covid.qr.code.target.zone}")
    private String targetZoneId;

    /**
     * GenerateServiceImpl No need of external services here.
     */
    private GenerateServiceImpl gsi = new GenerateServiceImpl(null,
            null,
            null);


    /**
     * Test method generating list of validFrom OffSetTime.
     */
    @Test
    void validFromList() {
        final int size = 10;
        final ZoneId parisZoneId = ZoneId.of(this.targetZoneId);

        // to prevent days, month, and year changes this time has been specially chosen for the test.
        final OffsetDateTime validFromFirstValue = OffsetDateTime.now(parisZoneId)
                .withMonth(12)
                .withDayOfMonth(31)
                .withHour(21)
                .withMinute(00);

        log.info("initial time : {}", validFromFirstValue);

        ReflectionTestUtils.setField(this.gsi, "TARGET_ZONE_ID", "Europe/Paris");


        // Tested method call
        List<OffsetDateTime> validFromList = this.gsi.getValidFromList(size, validFromFirstValue);

        final ZoneOffset zuluZoneOffset = ZoneOffset.of("Z");

        // list returned should be at the desired size.
        assertEquals(size, validFromList.size());

        // first value of list should be validFromFirstValue at zulu time
        assertEquals(validFromFirstValue.withOffsetSameInstant(ZoneOffset.of("Z")), validFromList.get(0));


        int parisMidnightAtZuluHour = validFromFirstValue
                .withHour(0)
                .withOffsetSameInstant(ZoneOffset.of("Z"))
                .getHour();

        int parisMidnightAtZuluMinutes = validFromFirstValue
                .withHour(0)
                .withMinute(0)
                .withOffsetSameInstant(ZoneOffset.of("Z"))
                .getMinute();

        int parisMidnightAtZuluSeconds = validFromFirstValue
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withOffsetSameInstant(ZoneOffset.of("Z"))
                .getSecond();

        // dayOfMonth with zulu zoneoffset conversion
        log.info("generated time : {}", validFromList.get(0));
        for (int i = 1; i < size; i++) {

            final OffsetDateTime z = validFromFirstValue
                    .plusDays(i)
                    .withSecond(0)
                    .withMinute(0)
                    .withHour(0)
                    .withOffsetSameInstant(ZoneOffset.of("Z"));

            int dayOfMonth = z.getDayOfMonth();
            int year = z.getYear();
            int month = z.getMonthValue();

            final OffsetDateTime vf = validFromList.get(i);
            log.info("generated time : {}", vf);

            // asserting that days are incremental.
            assertEquals(dayOfMonth, vf.getDayOfMonth());
            assertEquals(month, vf.getMonthValue());
            assertEquals(year, vf.getYear());

            // asserting hours, minutes, and seconds are set at Zulu offset
            assertEquals(parisMidnightAtZuluHour, vf.getHour());
            assertEquals(parisMidnightAtZuluMinutes, vf.getMinute());
            assertEquals(parisMidnightAtZuluSeconds, vf.getSecond());

            // asserting that Offset is ZULU
            assertEquals(zuluZoneOffset, vf.getOffset());

        }

    }
}
