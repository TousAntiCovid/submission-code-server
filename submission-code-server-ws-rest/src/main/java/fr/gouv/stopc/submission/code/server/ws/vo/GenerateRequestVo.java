package fr.gouv.stopc.submission.code.server.ws.vo;

import fr.gouv.stopc.submission.code.server.ws.annotations.CodeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Requests parameters for /generate endpoint
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Valid
public class GenerateRequestVo {
    /**
     * The type of the provided code
     */
    @NotNull
    @CodeType
    private String type;

}
