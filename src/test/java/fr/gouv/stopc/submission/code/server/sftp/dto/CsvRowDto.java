package fr.gouv.stopc.submission.code.server.sftp.dto;

import com.opencsv.bean.CsvBindByPosition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Valid
public class CsvRowDto {

    @NotNull
    @NotBlank
    @CsvBindByPosition(position = 0)
    private String qrcode;

    @NotNull
    @NotBlank
    @CsvBindByPosition(position = 1)
    private String code;

    @NotNull
    @CsvBindByPosition(position = 2)
    private Instant dateAvailable;

    @NotNull
    @CsvBindByPosition(position = 3)
    private Instant dateEndValidity;

    public CsvRowDto(String qrcode, String code, String dateAvailable, String dateEndValidity) {
        this.qrcode = qrcode;
        this.code = code;
        this.dateAvailable = Instant.parse(dateAvailable);
        this.dateEndValidity = Instant.parse(dateEndValidity);
    }
}
