package fr.gouv.stopc.submission.code.server.ws.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Constraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Valid
public class RequestCsvVo {

    /*
    mettre validation numerique
     */
    @Pattern(regexp = "[0-9]+")
    @NotNull
    String lot;
}
