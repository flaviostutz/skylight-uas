package br.skylight.commons.plugin.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExtensionPointImplementation {
	/**
	 * Extension point definition that is implemented by this class.
	 * This class must implement/extend the class defined by the extension point.
	 */
	Class extensionPointDefinition();

	/**
	 * When activating this element, include dependencies from other object
	 * This is normally used when a object explictly call pluginManager.manageObject(..) to dynamically 
	 * activate another element.
	 */
	Class[] useDependenciesFrom() default Object.class;

	/**
	 * Create only one instance of this extension point and use it for all injections.
	 * Defaults to false - each injection point will create new instances of extension point impl
	 */
//	boolean singleton() default false;
}
