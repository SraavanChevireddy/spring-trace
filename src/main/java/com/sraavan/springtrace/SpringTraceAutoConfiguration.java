package com.sraavan.springtrace;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration: dropping this starter on the classpath wires up the
 * request filter and the {@link Traced} aspect with zero code. Disable with
 * {@code spring-trace.enabled=false}.
 */
@AutoConfiguration
@ConditionalOnWebApplication
@EnableConfigurationProperties(SpringTraceProperties.class)
@ConditionalOnProperty(prefix = "spring-trace", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SpringTraceAutoConfiguration {

    @Bean
    public TraceFilter springTraceFilter(SpringTraceProperties properties) {
        return new TraceFilter(properties);
    }

    @Bean
    public TracedAspect springTracedAspect() {
        return new TracedAspect();
    }
}
