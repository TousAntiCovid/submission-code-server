package fr.gouv.stopc.submission.code.server.ws.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Requests parameters for /generate endpoint
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GenerateRequestVo {

    /**
     * The code value to verify
     */
    private String code;

    /**
     * The type of the provided code
     */
    private String type;
}
