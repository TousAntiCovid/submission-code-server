package fr.gouv.stopc.submission.code.server.ws.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public class CodeDetailedDto extends CodeSimpleDto {

    /**
     *  UUIDv4 or 6-alphanum
     */
    private String typeAsString;

    /**
     * 1  - > UUIDv4
     * 2  - > 6-alphanum
     */
    private Integer typeAsInt;

}
