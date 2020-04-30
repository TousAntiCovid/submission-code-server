package fr.gouv.stopc.submission.code.server.ws.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GenerateResponseDto {


    /**
     *  UUIDv4 or 6-alphanum
     */
    private String typeAsString;

    /**
     * 1  - > UUIDv4
     * 2  - > 6-alphanum
     */
    private Integer typeAsInt;

    /**
     *  6-alphanum - > size = 6
     *  UUIDv4 - > size = 36
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
