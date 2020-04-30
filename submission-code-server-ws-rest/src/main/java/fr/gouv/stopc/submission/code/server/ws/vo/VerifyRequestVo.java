package fr.gouv.stopc.submission.code.server.ws.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Requests parameters for /verify endpoint
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Valid
public class VerifyRequestVo {

    /**
     * The code value to verify
     */
   @Size(max = 6)
   @Pattern(regexp = "[^[a-zA-Z0-9]+$]")
   private String code;

    /**
     * The type of the provided code
     */
    @Pattern(regexp = "[1-2]")
    private String type;

}
