package com.sraavan.springtrace;

/**
 * Renders a finished span tree into a compact, human-readable timeline.
 *
 * Pure function (tree in, string out) so it can be unit-tested without any
 * Spring or HTTP machinery.
 */
public final class TraceFormatter {

    private TraceFormatter() {
    }

    public static String format(Span root) {
        StringBuilder sb = new StringBuilder();
        sb.append("Trace: ").append(root.name())
          .append("  (").append(root.durationMillis()).append("ms total)\n");
        append(sb, root, 0, root.durationMillis());
        return sb.toString();
    }

    private static void append(StringBuilder sb, Span span, int depth, long totalMillis) {
        String indent = "  ".repeat(depth);
        long pct = totalMillis == 0 ? 0 : (span.durationMillis() * 100) / totalMillis;
        sb.append(indent)
          .append("- ").append(span.name())
          .append(": ").append(span.durationMillis()).append("ms");
        if (!span.children().isEmpty()) {
            sb.append(" (self ").append(span.selfMillis()).append("ms)");
        }
        sb.append("  [").append(pct).append("%]\n");
        for (Span child : span.children()) {
            append(sb, child, depth + 1, totalMillis);
        }
    }
}
