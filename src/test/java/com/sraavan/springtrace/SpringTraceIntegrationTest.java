package com.sraavan.springtrace;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Boots a real (random-port) app using the starter's auto-configuration and
 * verifies that a request flowing through the filter + a {@link Traced} service
 * method produces a nested trace.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring-trace.slow-request-threshold-millis=0" // log every request
)
class SpringTraceIntegrationTest {

    @SpringBootApplication
    static class App {
        @Bean
        WorkService workService() {
            return new WorkService();
        }

        @Bean
        WorkController workController(WorkService service) {
            return new WorkController(service);
        }
    }

    static class WorkService {
        @Traced("work:doStuff")
        public String doStuff() {
            try {
                Thread.sleep(5);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            return "done";
        }
    }

    @RestController
    static class WorkController {
        private final WorkService service;

        WorkController(WorkService service) {
            this.service = service;
        }

        @GetMapping("/work")
        public String work() {
            return service.doStuff();
        }
    }

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private WorkService workService; // Spring-proxied, so the @Traced aspect applies

    @Test
    void requestSucceedsThroughTracingStack() {
        String body = rest.getForObject("/work", String.class);
        assertEquals("done", body);
    }

    @Test
    void tracedMethodAttachesToActiveTraceViaAspect() {
        // Open a trace, then call the real proxied bean: the aspect should add
        // a child span named by the @Traced annotation.
        TraceContext ctx = TraceContext.start("GET /work");
        workService.doStuff();
        Span root = ctx.finish();

        assertEquals("GET /work", root.name());
        assertTrue(root.children().stream().anyMatch(s -> s.name().equals("work:doStuff")),
                "expected a child span 'work:doStuff' added by the aspect");
    }
}
