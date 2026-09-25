package com.newsplatform.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class TimeConfig {
    @Bean
    public Clock utcClock() {
        return Clock.systemUTC();
    }

    @Bean
    public ZoneId applicationZone(@Value("${app.timezone:UTC}") String timezone) {
        return ZoneId.of(timezone);
    }
}
