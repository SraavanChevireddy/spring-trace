package com.sraavan.springtrace;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for spring-trace.
 *
 * <pre>
 * spring-trace:
 *   enabled: true
 *   slow-request-threshold-millis: 500   # only log traces slower than this
 * </pre>
 */
@ConfigurationProperties(prefix = "spring-trace")
public class SpringTraceProperties {

    /** Master switch. */
    private boolean enabled = true;

    /** Only requests at least this slow get their trace logged. */
    private long slowRequestThresholdMillis = 500;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getSlowRequestThresholdMillis() {
        return slowRequestThresholdMillis;
    }

    public void setSlowRequestThresholdMillis(long slowRequestThresholdMillis) {
        this.slowRequestThresholdMillis = slowRequestThresholdMillis;
    }
}
