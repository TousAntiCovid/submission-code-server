package fr.gouv.stopc.submission.code.server.business.vo;

import fr.gouv.stopc.submission.code.server.domain.annotations.CodePerDay;
import fr.gouv.stopc.submission.code.server.domain.annotations.PresentOrFutureTruncateDay;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public class ViewVo {

    private ViewVo() {
        super();
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @Valid
    public static class CodeGenerationRequestBody {

        @NotNull
        @PresentOrFutureTruncateDay
        private OffsetDateTime from;

        @NotNull
        @PresentOrFutureTruncateDay
        private OffsetDateTime to;

        @NotNull
        @CodePerDay
        private long dailyAmount;
    }
}
