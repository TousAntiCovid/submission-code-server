package fr.gouv.stopc.submission.code.server.ws.utils;

import java.time.*;

public final class FormatDatesKPI {

    public static OffsetDateTime normaliseDateTo(LocalDate dateTmpTo, String targetZoneId) {
        LocalTime time= LocalTime.of(23,59,59,999000000);
        ZoneOffset zoneOffset= OffsetDateTime.now(ZoneId.of(targetZoneId)).getOffset();
        OffsetDateTime dateToZone = OffsetDateTime.of(dateTmpTo,time, zoneOffset);
        return dateToZone.withOffsetSameInstant(ZoneOffset.UTC);
    }

    public static OffsetDateTime normaliseDateFrom(LocalDate dateFrom, String targetZoneId) {
        LocalTime time= LocalTime.MIN;
        LocalDateTime.of(dateFrom,time).atZone(ZoneId.systemDefault());
        ZoneOffset zoneOffset= OffsetDateTime.now(ZoneId.of(targetZoneId)).getOffset();
        OffsetDateTime dateBeginZone = OffsetDateTime.of(dateFrom,time, zoneOffset);
        return dateBeginZone.withOffsetSameInstant(ZoneOffset.UTC);
    }
}
