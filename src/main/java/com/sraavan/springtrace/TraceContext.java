package com.sraavan.springtrace;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Holds the in-progress call tree for the current request on a thread-local.
 *
 * Spans are pushed/popped as methods enter and exit, so the active span is
 * always the top of the stack and new spans attach to their real parent.
 */
public final class TraceContext {

    private static final ThreadLocal<TraceContext> CURRENT = new ThreadLocal<>();

    private final Span root;
    private final Deque<Span> stack = new ArrayDeque<>();

    private TraceContext(Span root) {
        this.root = root;
        this.stack.push(root);
    }

    public static TraceContext start(String rootName) {
        TraceContext ctx = new TraceContext(new Span(rootName, System.nanoTime()));
        CURRENT.set(ctx);
        return ctx;
    }

    public static TraceContext current() {
        return CURRENT.get();
    }

    public static boolean isActive() {
        return CURRENT.get() != null;
    }

    /** Open a child span under the currently active span. */
    public Span openSpan(String name) {
        Span span = new Span(name, System.nanoTime());
        stack.peek().addChild(span);
        stack.push(span);
        return span;
    }

    /** Close the most recently opened span. */
    public void closeSpan(Span span) {
        span.stop(System.nanoTime());
        if (stack.peek() == span) {
            stack.pop();
        }
    }

    public Span finish() {
        root.stop(System.nanoTime());
        CURRENT.remove();
        return root;
    }

    public Span root() {
        return root;
    }
}
