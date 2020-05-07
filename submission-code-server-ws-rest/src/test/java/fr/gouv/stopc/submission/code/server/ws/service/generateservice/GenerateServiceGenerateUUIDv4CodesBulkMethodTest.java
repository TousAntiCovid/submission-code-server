package fr.gouv.stopc.submission.code.server.ws.service.generateservice;

import fr.gouv.stopc.submission.code.server.ws.dto.GenerateResponseDto;
import fr.gouv.stopc.submission.code.server.ws.service.GenerateServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;

import static fr.gouv.stopc.submission.code.server.ws.service.generateservice.GenerateServiceTestHelper.assertingUUIDv4Code;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
class GenerateServiceGenerateUUIDv4CodesBulkMethodTest {

    @Autowired
    private GenerateServiceImpl gsi;

    @Before
    public void init(){
        log.info("Initialize mokito injection in services...");
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Persisted codes are uniques
     */
    @Test
    void uniqueCodeWithValidFromAndLotTest()
    {
        // asserting gsi is available
        final long size = Long.parseLong("10");
        final OffsetDateTime validFrom = OffsetDateTime.now();
        final long lot = Long.parseLong("1");

        ReflectionTestUtils.setField(this.gsi, "NUMBER_OF_UUIDv4_PER_CALL", size);
        ReflectionTestUtils.setField(this.gsi, "TARGET_ZONE_ID", "Europe/Paris");



        final List<GenerateResponseDto> generateResponseDtoList = this.gsi.generateUUIDv4CodesBulk(validFrom, lot);

        //list should not be null
        assertNotNull(generateResponseDtoList);
        // list should be at size
        assertEquals(size, generateResponseDtoList.size());

        final HashMap<String, Integer> map = new HashMap<>();
        for (int i = 0; i < generateResponseDtoList.size(); i++) {
            GenerateResponseDto gr = generateResponseDtoList.get(i);
            map.put(gr.getCode(), 0);
            assertingUUIDv4Code(gr);

        }

        assertEquals(size, map.size());
    }

    /**
     * Persisted codes are uniques
     */
    @Test
    void uniqueCodeWithValidFromTest()
    {
        // asserting gsi is available
        final long size = Long.parseLong("10");
        final OffsetDateTime validFrom = OffsetDateTime.now();

        ReflectionTestUtils.setField(this.gsi, "NUMBER_OF_UUIDv4_PER_CALL", size);
        ReflectionTestUtils.setField(this.gsi, "TARGET_ZONE_ID", "Europe/Paris");

        final List<GenerateResponseDto> generateResponseDtoList = this.gsi.generateUUIDv4CodesBulk(validFrom);

        //list should not be null
        assertNotNull(generateResponseDtoList);
        // list should be at size
        assertEquals(size, generateResponseDtoList.size());

        final HashMap<String, Integer> map = new HashMap<>();
        for (int i = 0; i < generateResponseDtoList.size(); i++) {
            GenerateResponseDto gr = generateResponseDtoList.get(i);
            map.put(gr.getCode(), 0);
            assertingUUIDv4Code(gr);

        }

        assertEquals(size, map.size());
    }

    /**
     * Persisted codes are uniques
     */
    @Test
    void uniqueCodeTest()
    {
        // asserting gsi is available
        final long size = Long.parseLong("3");

        ReflectionTestUtils.setField(this.gsi, "NUMBER_OF_UUIDv4_PER_CALL", size);
        ReflectionTestUtils.setField(this.gsi, "TARGET_ZONE_ID", "Europe/Paris");

        final List<GenerateResponseDto> generateResponseDtoList = this.gsi.generateUUIDv4CodesBulk();

        //list should not be null
        assertNotNull(generateResponseDtoList);
        // list should be at size
        assertEquals(size, generateResponseDtoList.size());

        final HashMap<String, Integer> map = new HashMap<>();
        for (int i = 0; i < generateResponseDtoList.size(); i++) {
            GenerateResponseDto gr = generateResponseDtoList.get(i);

            assertingUUIDv4Code(gr);
            map.put(gr.getCode(), 0);
        }

        assertEquals(size, map.size());
    }

}