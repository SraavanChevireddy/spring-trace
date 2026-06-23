package com.sraavan.springtrace;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Times every {@link Traced} method and nests it under the active request span.
 * If there is no active trace (e.g. a background thread), the method runs
 * untouched.
 */
@Aspect
public class TracedAspect {

    @Around("@annotation(traced)")
    public Object trace(ProceedingJoinPoint pjp, Traced traced) throws Throwable {
        if (!TraceContext.isActive()) {
            return pjp.proceed();
        }
        TraceContext ctx = TraceContext.current();
        Span span = ctx.openSpan(spanName(pjp, traced));
        try {
            return pjp.proceed();
        } finally {
            ctx.closeSpan(span);
        }
    }

    private String spanName(ProceedingJoinPoint pjp, Traced traced) {
        if (!traced.value().isBlank()) {
            return traced.value();
        }
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        return sig.getDeclaringType().getSimpleName() + "." + sig.getName();
    }
}
