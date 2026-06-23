package com.sraavan.springtrace;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a Spring bean method to have its execution timed and added to the
 * current request's call tree. Put it on service/repository methods you want
 * to see in the timeline.
 *
 * <pre>
 * &#64;Traced
 * public Order loadOrder(String id) { ... }
 *
 * &#64;Traced("db:findCustomer")   // custom span name
 * public Customer find(String id) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Traced {
    /** Optional span name; defaults to {@code ClassName.methodName}. */
    String value() default "";
}
