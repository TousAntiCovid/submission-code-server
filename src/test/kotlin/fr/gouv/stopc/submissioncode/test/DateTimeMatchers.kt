package fr.gouv.stopc.submissioncode.test

import org.exparity.hamcrest.date.core.IsWithin
import org.exparity.hamcrest.date.core.TemporalFunctions
import org.exparity.hamcrest.date.core.TemporalMatcher
import org.exparity.hamcrest.date.core.TemporalProviders
import org.exparity.hamcrest.date.core.types.Interval
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Optional

fun isoDateTimeWithin(period: Long, unit: ChronoUnit, date: Instant): TemporalMatcher<String> {
    return IsWithin(
        Interval.of(period, unit),
        { isoDateTime: String, _: Optional<ZoneId> -> Instant.parse(isoDateTime) },
        TemporalProviders.instant(date),
        TemporalFunctions.INSTANT
    )
}
