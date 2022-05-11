package fr.gouv.stopc.submission.code.server.business.service.schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Data
@Component
@ConfigurationProperties(prefix = "submission.code.server.cron")
public class GenerationConfigProperties {

    List<GenerationConfig> scheduling;

    Integer maxbatchsize;

    /**
     * Return the daily production target of current startDateTime from the
     * properties files
     *
     * @param startDateTime current startDateTime
     * @return long dailyProductionTarget
     */
    public Integer getDailyProductionTarget(OffsetDateTime startDateTime) {
        Collections.sort(getScheduling());
        Integer dailyProductionTarget = 0;
        Optional<GenerationConfig> config = getScheduling().stream().filter(
                c -> c.getStartDate().isBefore(startDateTime) || c.getStartDate().isEqual(startDateTime)
        ).reduce((f, s) -> s);

        if (config.isPresent()) {
            dailyProductionTarget = config.get().getDailyProduction();
        }
        return dailyProductionTarget;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class GenerationConfig implements Comparable<GenerationConfig> {

        OffsetDateTime startDate;

        Integer dailyProduction;

        @Override
        public int compareTo(GenerationConfig o) {
            return getStartDate().compareTo(o.getStartDate());
        }
    }
}
