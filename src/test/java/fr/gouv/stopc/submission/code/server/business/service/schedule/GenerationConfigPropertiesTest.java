package fr.gouv.stopc.submission.code.server.business.service.schedule;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class GenerationConfigPropertiesTest {

    private final String targetZoneId = "Europe/Paris";

    @Test
    void decreasingVolumetry() {
        Map<Integer, Integer> dayAndVolumeMap = Map.of(0, 300, 8, 40, 9, 0);
        List<Integer> returnedValues = getDailyProductionTargetList(dayAndVolumeMap);
        List<Integer> expectedValues = List.of(300, 300, 300, 300, 300, 300, 300, 300, 40, 0, 0);
        Assertions.assertEquals(expectedValues, returnedValues);
    }

    @Test
    void increasingVolumetryAndStop() {
        Map<Integer, Integer> dayAndVolumeMap = Map.of(0, 300, 5, 800, 10, 0);
        List<Integer> returnedValues = getDailyProductionTargetList(dayAndVolumeMap);
        List<Integer> expectedValues = List.of(300, 300, 300, 300, 300, 800, 800, 800, 800, 800, 0);
        Assertions.assertEquals(expectedValues, returnedValues);
    }

    @Test
    void simulateInvertedDates() {
        OffsetDateTime todayOff = OffsetDateTime.now(ZoneId.of(targetZoneId)).truncatedTo(ChronoUnit.DAYS);
        List<GenerationConfigProperties.GenerationConfig> scheduling = new ArrayList<>();

        OffsetDateTime targetDate = todayOff;
        GenerationConfigProperties.GenerationConfig conf = GenerationConfigProperties.GenerationConfig
                .builder()
                .dailyProduction(300)
                .startDate(targetDate)
                .build();
        scheduling.add(conf);

        targetDate = todayOff.plusDays(8);
        conf = GenerationConfigProperties.GenerationConfig
                .builder()
                .dailyProduction(800)
                .startDate(targetDate)
                .build();
        scheduling.add(conf);

        targetDate = todayOff.plusDays(5);
        conf = GenerationConfigProperties.GenerationConfig
                .builder()
                .dailyProduction(500)
                .startDate(targetDate)
                .build();
        scheduling.add(conf);

        GenerationConfigProperties generationConfig = new GenerationConfigProperties();
        generationConfig.setScheduling(scheduling);

        OffsetDateTime currentDate = OffsetDateTime.now();
        List<Integer> returnedValues = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            returnedValues.add(generationConfig.getDailyProductionTarget(currentDate));
            currentDate = currentDate.plusDays(1);
        }

        List<Integer> expectedValues = List.of(300, 300, 300, 300, 300, 500, 500, 500, 800, 800, 800);
        Assertions.assertEquals(expectedValues, returnedValues);
    }

    private List<Integer> getDailyProductionTargetList(Map<Integer, Integer> dayAndVolumeMap) {
        OffsetDateTime todayOff = OffsetDateTime.now(ZoneId.of(targetZoneId)).truncatedTo(ChronoUnit.DAYS);
        List<GenerationConfigProperties.GenerationConfig> scheduling = new ArrayList<>();
        dayAndVolumeMap.forEach((day, volume) -> {
            OffsetDateTime currentDate = todayOff.plusDays(day);
            GenerationConfigProperties.GenerationConfig conf = GenerationConfigProperties.GenerationConfig.builder()
                    .dailyProduction(volume).startDate(currentDate)
                    .build();
            scheduling.add(conf);
        }
        );

        GenerationConfigProperties generationConfig = new GenerationConfigProperties();
        generationConfig.setScheduling(scheduling);

        OffsetDateTime currentDate = OffsetDateTime.now();
        List<Integer> schedules = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            schedules.add(generationConfig.getDailyProductionTarget(currentDate));
            currentDate = currentDate.plusDays(1);
        }
        return schedules;
    }
}
