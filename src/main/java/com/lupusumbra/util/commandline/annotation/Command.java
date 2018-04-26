package com.lupusumbra.util.commandline.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    String name();
    String[] descriptions() default {};
    String[] detailedDescription() default {};
    String[] usage() default {"-h","--help"};
}
