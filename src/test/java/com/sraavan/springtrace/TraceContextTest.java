package com.sraavan.springtrace;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TraceContextTest {

    @AfterEach
    void cleanup() {
        if (TraceContext.isActive()) {
            TraceContext.current().finish();
        }
    }

    @Test
    void startsInactive() {
        assertFalse(TraceContext.isActive());
    }

    @Test
    void buildsNestedTreeMatchingCallStructure() {
        TraceContext ctx = TraceContext.start("GET /x");
        Span a = ctx.openSpan("a");
        Span b = ctx.openSpan("b");   // nested under a
        ctx.closeSpan(b);
        ctx.closeSpan(a);
        Span sibling = ctx.openSpan("c");   // back at root level
        ctx.closeSpan(sibling);
        Span root = ctx.finish();

        assertEquals(2, root.children().size());        // a and c
        assertEquals("a", root.children().get(0).name());
        assertEquals(1, root.children().get(0).children().size()); // b under a
        assertEquals("b", root.children().get(0).children().get(0).name());
        assertEquals("c", root.children().get(1).name());
    }

    @Test
    void finishClearsThreadLocal() {
        TraceContext.start("r").finish();
        assertFalse(TraceContext.isActive());
    }
}
