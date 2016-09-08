package br.skylight.commons.plugin.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.FIELD})
public @interface ServiceInjection {
	/**
	 * If this is optional, then this reference may be null when 
	 * this element is initiated (onActivate()).
	 * This was created for resolving cyclic dependencies between members.
	 */
	boolean optionalAtInitialization() default false;
}
