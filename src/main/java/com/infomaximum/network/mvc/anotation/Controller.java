package com.infomaximum.network.mvc.anotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * User: Vladimir Ulitin
 * Date: 08.09.12
 * Time: 13:03
 * To change this template use File | Settings | File Templates.
 */
@Target(ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Controller {
    String value();
}
