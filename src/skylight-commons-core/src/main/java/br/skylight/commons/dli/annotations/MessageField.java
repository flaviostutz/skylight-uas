package br.skylight.commons.dli.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.skylight.commons.MeasureType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MessageField {

	int number();
	MeasureType measureType() default MeasureType.UNDEFINED;

}
