package fr.gouv.stopc.submission.code.server.sftp.dto;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

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
    @CsvDate(value = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @CsvBindByPosition(position = 2) // , locale = "Europe/Paris"
    private OffsetDateTime dateAvailable;

    @NotNull
    @CsvDate(value = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @CsvBindByPosition(position = 3)
    private OffsetDateTime dateEndValidity;

    public CsvRowDto(String qrcode, String code, String dateAvailable, String dateEndValidity) {
        this.qrcode = qrcode;
        this.code = code;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .withZone(ZoneId.of("Europe/Paris"));
        ZonedDateTime zdt = ZonedDateTime.parse(dateAvailable, dtf);
        OffsetDateTime odt = zdt.toOffsetDateTime();
        this.dateAvailable = odt;
        zdt = ZonedDateTime.parse(dateEndValidity, dtf);
        odt = zdt.toOffsetDateTime();
        this.dateEndValidity = odt;
    }
}
