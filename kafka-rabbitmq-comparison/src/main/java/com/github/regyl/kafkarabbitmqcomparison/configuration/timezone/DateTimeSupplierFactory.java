package com.github.regyl.kafkarabbitmqcomparison.configuration.timezone;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.TimeZone;
import java.util.function.Supplier;

@Configuration
public class DateTimeSupplierFactory {

    public static final ZoneOffset SYSTEM_ZONE_OFFSET = ZoneOffset.of("+03:00");
    public static final String SYSTEM_TIMEZONE = "Europe/Moscow";
    public static final ZoneId SYSTEM_ZONE_ID = ZoneId.of(SYSTEM_TIMEZONE);
    public static final Clock SYSTEM_CLOCK = Clock.system(SYSTEM_ZONE_ID);

    static {
        TimeZone.setDefault(TimeZone.getTimeZone(SYSTEM_TIMEZONE));
    }

    @Bean
    public Supplier<OffsetDateTime> offsetDateTimeSupplier() {
        return () -> OffsetDateTime.ofInstant(SYSTEM_CLOCK.instant(), SYSTEM_ZONE_ID);
    }

    @Bean
    public Supplier<Instant> instantSupplier() {
        return SYSTEM_CLOCK::instant;
    }

    @Bean
    public Supplier<Timestamp> timestampSupplier() {
        return () -> Timestamp.from(SYSTEM_CLOCK.instant());
    }
}
