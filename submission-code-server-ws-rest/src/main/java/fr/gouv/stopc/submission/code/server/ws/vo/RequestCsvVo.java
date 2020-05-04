package fr.gouv.stopc.submission.code.server.ws.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Valid
public class RequestCsvVo {

    /*
    mettre validation numerique
     */
    @NotNull
    String lot;
}
