package org.dieschnittstelle.ess.basics.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface DisplayAs {
    // "value" is convention to allow to omit the name and equal sign in the annotation
    String value();
}
