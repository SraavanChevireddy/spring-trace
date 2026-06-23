package com.sraavan.springtrace;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TraceFormatterTest {

    /** Build a span with fixed start/end so durations are deterministic. */
    private static Span span(String name, long startMs, long endMs) {
        Span s = new Span(name, startMs * 1_000_000);
        s.stop(endMs * 1_000_000);
        return s;
    }

    @Test
    void durationIsEndMinusStart() {
        assertEquals(150, span("x", 100, 250).durationMillis());
    }

    @Test
    void unfinishedSpanHasZeroDuration() {
        Span s = new Span("x", 0);
        assertFalse(s.isFinished());
        assertEquals(0, s.durationMillis());
    }

    @Test
    void selfTimeExcludesChildren() {
        Span root = span("root", 0, 100);
        root.addChild(span("a", 0, 30));
        root.addChild(span("b", 30, 60));
        // 100ms total - 60ms in children = 40ms self
        assertEquals(40, root.selfMillis());
    }

    @Test
    void formatIncludesNamesAndPercentages() {
        Span root = span("GET /orders", 0, 200);
        root.addChild(span("OrderService.load", 0, 180));
        String out = TraceFormatter.format(root);

        assertTrue(out.contains("GET /orders"));
        assertTrue(out.contains("200ms total"));
        assertTrue(out.contains("OrderService.load"));
        assertTrue(out.contains("180ms"));
        assertTrue(out.contains("[90%]"), out);
    }

    @Test
    void nestedSpansAreIndented() {
        Span root = span("root", 0, 100);
        Span child = span("child", 0, 50);
        child.addChild(span("grandchild", 0, 25));
        root.addChild(child);
        String out = TraceFormatter.format(root);
        // grandchild should be indented deeper than child
        assertTrue(out.contains("    - grandchild"), out);
    }
}
