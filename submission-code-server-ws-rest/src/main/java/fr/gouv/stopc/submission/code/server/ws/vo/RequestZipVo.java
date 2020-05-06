package fr.gouv.stopc.submission.code.server.ws.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Valid
public class RequestZipVo {
    //TODO
    private String lot;

    private String dateFrom;

    private String dateTo;



}
