package fr.gouv.stopc.submission.code.server.ws.vo;

import fr.gouv.stopc.submission.code.server.ws.annotations.CodeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


/**
 * Requests parameters of "/verify" endpoint
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
   @NotNull
   private String code;

    /**
     * The type of the provided code
     */
    @NotNull
    @CodeType
    private String type;

}
