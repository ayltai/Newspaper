package com.github.ayltai.newspaper.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicate to FindBugs that a warning should be ignored.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.CONSTRUCTOR, ElementType.PACKAGE})
public @interface SuppressFBWarnings {
    /** Names of the warnings to ignore */
    String[] value();
    String justification() default "";
}
