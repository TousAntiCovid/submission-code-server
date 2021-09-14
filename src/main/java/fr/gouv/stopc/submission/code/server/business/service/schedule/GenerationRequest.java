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
public class GenerationRequest {

    private OffsetDateTime startDateTime;

    private OffsetDateTime endDateTime;

    private Long numberOfCodeToGenerate;

    private Long iterationBatchNumber;
}
