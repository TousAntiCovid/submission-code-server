package fr.gouv.stopc.submission.code.server.ws.vo;

import fr.gouv.stopc.submission.code.server.ws.dto.GenerateResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Requests parameters for /verify endpoint
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class VerifyRequestVo {

    /**
     * The code value to verify
     */
    private String code;

    /**
     * The type of the provided code
     */
    private String type;

}
