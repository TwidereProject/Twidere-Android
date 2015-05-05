package org.mariotaku.simplerestapi.param;

import org.mariotaku.simplerestapi.http.BodyType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by mariotaku on 15/2/6.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Body {
    BodyType value();

    String encoding() default "";
}
