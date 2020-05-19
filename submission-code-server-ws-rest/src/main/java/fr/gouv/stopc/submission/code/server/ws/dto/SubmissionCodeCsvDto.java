package fr.gouv.stopc.submission.code.server.ws.dto;

import fr.gouv.stopc.submission.code.server.database.dto.SubmissionCodeDto;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

public class SubmissionCodeCsvDto extends SubmissionCodeDto {

    @Getter
    @Setter
    private String qrcode;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubmissionCodeCsvDto)) return false;
        if (!super.equals(o)) return false;
        SubmissionCodeCsvDto that = (SubmissionCodeCsvDto) o;
        return Objects.equals(getQrcode(), that.getQrcode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getQrcode());
    }
}
