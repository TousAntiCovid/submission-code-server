package fr.gouv.stopc.submission.code.server.ws.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public class CodeSimpleDto {
    /**
     *  code generated formatted as uuidv4 or 6-alphanum
     */
    private String code;

    /**
     *  Forrmat ISO date is : YYYY-MM-DDTHH:mm:ss.sssZ
     */
    private String validFrom;

    /**
     *  Forrmat ISO date is : YYYY-MM-DDTHH:mm:ss.sssZ
     */
    private String validUntil;


}
