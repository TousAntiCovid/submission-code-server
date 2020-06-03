package fr.gouv.stopc.submission.code.server.ws.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class SubmissionCodeServerKpi {

        private LocalDate date;

        private Long nbShortCodesUsed;

        private Long nbLongCodesUsed;

        private Long nbLongExpiredCodes;

        private Long nbShortExpiredCodes;

}


