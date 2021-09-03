package fr.gouv.stopc.submission.code.server.business.service.schedule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public class GenerationConfig implements Comparable<GenerationConfig> {
    OffsetDateTime startdate;
    long dailyProduction;

    @Override
    public int compareTo(GenerationConfig o) {
        return getStartdate().compareTo(o.getStartdate());
    }
}