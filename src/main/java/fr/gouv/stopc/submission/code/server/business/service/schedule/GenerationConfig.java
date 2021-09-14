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

    OffsetDateTime startDate;

    long dailyProduction;

    @Override
    public int compareTo(GenerationConfig o) {
        return getStartDate().compareTo(o.getStartDate());
    }
}
