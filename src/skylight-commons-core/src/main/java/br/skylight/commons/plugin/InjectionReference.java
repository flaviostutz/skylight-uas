package br.skylight.commons.plugin;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InjectionReference {

	private Class definitionType;
	private AnnotatedElement classElement;
	private Class declaringClass;
	private boolean optional;
	private boolean fromDependencies;

	public InjectionReference(Class definitionType, AnnotatedElement classElement, Class declaringClass, boolean optional) {
		this.definitionType = definitionType;
		this.classElement = classElement;
		this.declaringClass = declaringClass;
		this.optional = optional;
	}
	
	/**
	 * The class that defines a key for what is being injected (ex.: Service definition, extension point definition).
	 * @return
	 */
	public Class getDefinitionType() {
		return definitionType;
	}
	public void setDefinitionType(Class definitionType) {
		this.definitionType = definitionType;
	}
	public AnnotatedElement getClassElement() {
		return classElement;
	}
	/**
	 * Set Method of Field being annotated
	 * @param classElement
	 */
	public void setClassElement(AnnotatedElement classElement) {
		this.classElement = classElement;
	}
	
	/**
	 * The class whose 'classElement' is related to
	 * @return
	 */
	public Class getDeclaringClass() {
		return declaringClass;
	}

	public void performInjection(Object element, Object value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if(!fromDependencies) {//don't process injections found in dependencies
			if(classElement instanceof Method) {
				((Method)classElement).invoke(element, value);
			} else if(classElement instanceof Field) {
//				System.out.println(classElement + " " + element + " " + value);
				((Field)classElement).set(element, value);
			}
		}
	}
	
	public boolean isOptional() {
		return optional;
	}

	public void setFromDependencies(boolean fromDependencies) {
		this.fromDependencies = fromDependencies;
	}

	public boolean isFromDependencies() {
		return fromDependencies;
	}
	
}
