package br.skylight.commons.plugin.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PluginDefinition {

	Class[] members() default Object.class;
	boolean scanPackageMembers() default true;
	Class[] useDependenciesFrom() default Object.class;
	
}
