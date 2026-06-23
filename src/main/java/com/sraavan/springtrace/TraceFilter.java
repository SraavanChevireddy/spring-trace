package com.sraavan.springtrace;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Opens a trace for each HTTP request and, when the request finishes, logs the
 * full call tree if it was slower than the configured threshold.
 */
public class TraceFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger("spring-trace");

    private final SpringTraceProperties properties;

    public TraceFilter(SpringTraceProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String name = request.getMethod() + " " + request.getRequestURI();
        TraceContext ctx = TraceContext.start(name);
        try {
            chain.doFilter(request, response);
        } finally {
            Span root = ctx.finish();
            if (root.durationMillis() >= properties.getSlowRequestThresholdMillis()) {
                log.warn("SLOW REQUEST ({}ms >= {}ms threshold):\n{}",
                        root.durationMillis(),
                        properties.getSlowRequestThresholdMillis(),
                        TraceFormatter.format(root));
            }
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !properties.isEnabled();
    }
}
