package fr.gouv.stopc.submission.code.server.business.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public class CodeDetailedDto {

    /**
     * TODO rename ? UUIDv4 or 6-alphanum
     */
    private String typeAsString;

    /**
     * 1 - > long code 2 - > short code
     */
    private Integer typeAsInt;

    /**
     * code generated formatted as long code or short code
     */
    private String code;

    /**
     * Format ISO date is : YYYY-MM-DDTHH:mm:ss.sssZ
     */
    private String validFrom;

    /**
     * Format ISO date is : YYYY-MM-DDTHH:mm:ss.sssZ
     */
    private String validUntil;

    /**
     * Format ISO date is : YYYY-MM-DDTHH:mm:ss.sssZ
     */
    private String dateGenerate;

}
