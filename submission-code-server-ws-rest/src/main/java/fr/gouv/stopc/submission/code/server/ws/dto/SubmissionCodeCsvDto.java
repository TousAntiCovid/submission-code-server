package fr.gouv.stopc.submission.code.server.ws.dto;

import fr.gouv.stopc.submission.code.server.database.dto.SubmissionCodeDto;
import lombok.Getter;
import lombok.Setter;

public class SubmissionCodeCsvDto extends SubmissionCodeDto {

    @Getter
    @Setter
    private String qrcode;
}
