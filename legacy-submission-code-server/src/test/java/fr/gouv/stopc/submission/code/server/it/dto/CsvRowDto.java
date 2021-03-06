package fr.gouv.stopc.submission.code.server.it.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Valid
public class CsvRowDto {

    private String qrcode;

    private String code;

    private Instant dateAvailable;

    private Instant dateEndValidity;

    public CsvRowDto(String qrcode, String code, String dateAvailable, String dateEndValidity) {
        this.qrcode = qrcode;
        this.code = code;
        this.dateAvailable = Instant.parse(dateAvailable);
        this.dateEndValidity = Instant.parse(dateEndValidity);
    }
}
