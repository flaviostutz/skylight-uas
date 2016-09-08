package br.skylight.commons.plugin.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface MemberInjection {

	/**
	 * Force the instantiation of a new object in this injection
	 * instead of using the default instance created for this member 
	 */
	boolean createNewInstance() default false;
	
	/**
	 * If this is optional, then this reference may be null when 
	 * this element is initiated (onActivate()).
	 * This was created for resolving cyclic dependencies between members.
	 */
	boolean optionalAtInitialization() default false;

}
