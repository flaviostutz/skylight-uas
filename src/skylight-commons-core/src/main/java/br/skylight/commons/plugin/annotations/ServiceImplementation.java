package br.skylight.commons.plugin.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ServiceImplementation {

	/**
	 * Service definitions that are implemented by this ServiceImplementation
	 * This class must implement/extend the service definition interface/class
	 * @return
	 */
	Class[] serviceDefinition();

	/**
	 * When activating this element, include dependencies from other object
	 * This is normally used when a object explictly call pluginManager.manageObject(..) to dynamically 
	 * activate another element.
	 */
	Class[] useDependenciesFrom() default Object.class;

	/**
	 * Load and activate element on startup
	 */
	boolean loadOnStartup() default true;
}
