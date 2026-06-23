package com.sraavan.springtrace;

import java.util.ArrayList;
import java.util.List;

/**
 * A single timed node in a request's call tree. Spans nest: an HTTP request is
 * the root, and each {@code @Traced} method (or manually opened span) becomes a
 * child, forming a tree that mirrors the actual call structure.
 */
public class Span {

    private final String name;
    private final long startNanos;
    private long endNanos = -1;
    private final List<Span> children = new ArrayList<>();

    public Span(String name, long startNanos) {
        this.name = name;
        this.startNanos = startNanos;
    }

    public void stop(long endNanos) {
        this.endNanos = endNanos;
    }

    public void addChild(Span child) {
        children.add(child);
    }

    public String name() {
        return name;
    }

    public List<Span> children() {
        return children;
    }

    public boolean isFinished() {
        return endNanos >= 0;
    }

    /** Total duration of this span in milliseconds (0 if not yet stopped). */
    public long durationMillis() {
        if (endNanos < 0) {
            return 0;
        }
        return (endNanos - startNanos) / 1_000_000;
    }

    /**
     * Time spent in this span that is NOT accounted for by children, in millis.
     * This is the "self time" — useful for spotting where the wall-clock
     * actually went.
     */
    public long selfMillis() {
        long childTotal = 0;
        for (Span child : children) {
            childTotal += child.durationMillis();
        }
        return Math.max(0, durationMillis() - childTotal);
    }
}
