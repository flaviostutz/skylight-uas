package br.skylight.commons.plugin.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.FIELD})
public @interface ExtensionPointsInjection {

	/**
	 * Create new extension point instances for each point of injection.
	 * If the injected element is inside a Service implementation or a ManagedMember, a
	 * singleton instance will be used because those elements are singleton too.
	 * Defaults to true. 
	 */
	boolean createNewInstances() default true;
	
}
