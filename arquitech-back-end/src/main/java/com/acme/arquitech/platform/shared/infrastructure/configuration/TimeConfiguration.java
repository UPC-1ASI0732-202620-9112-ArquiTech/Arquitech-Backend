package com.acme.arquitech.platform.shared.infrastructure.configuration;
import org.springframework.context.annotation.*;
import java.time.Clock;
@Configuration
public class TimeConfiguration {
    @Bean
    public Clock clock() { return Clock.systemUTC(); }
}
