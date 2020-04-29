package fr.gouv.stopc.submission.code.server.database.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CodePositiveDto {
    private long lot;

    private String code;

    private char type;

    private OffsetDateTime dateEndValidity;

    private OffsetDateTime dateAvailable;

    private OffsetDateTime dateUse;

    private OffsetDateTime dateGeneration;

    private Boolean used;
}
