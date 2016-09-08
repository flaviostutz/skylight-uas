package br.skylight.commons.plugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import br.skylight.commons.infra.Activable;
import br.skylight.commons.infra.ThreadWorker;
import br.skylight.commons.plugin.annotations.ExtensionPointDefinition;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ExtensionPointsInjection;
import br.skylight.commons.plugin.annotations.ManagedMember;
import br.skylight.commons.plugin.annotations.MemberInjection;
import br.skylight.commons.plugin.annotations.PluginDefinition;
import br.skylight.commons.plugin.annotations.ServiceDefinition;
import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;

public class PluginElement {

	private static final Logger logger = Logger.getLogger(PluginElement.class.getName());
	
	private Plugin plugin;
	private Object element;
	
	private Class[] implementedServiceDefinitions = new Class[0];
	private Class implementedExtensionPointDefinition;
	private Class implementedManagedMemberDefinition;
	
	private List<InjectionReference> injectedServices = new ArrayList<InjectionReference>();
	private List<InjectionReference> injectedExtensionPoints = new ArrayList<InjectionReference>();
	private List<InjectionReference> injectedManagedMembers = new ArrayList<InjectionReference>();
	
	private boolean initialized = false;
	private boolean failedToInitialize = false;

	private boolean activated = false;
	private boolean failedToActivate = false;
	
	private boolean functional = true;
	
	public PluginElement(Plugin plugin, Object element) {
		this.element = element;
		this.plugin = plugin;

		//PROCESS AND VALIDATE ANNOTATIONS
		
		//service definition of the service implemented by this element
		if(element.getClass().isAnnotationPresent(ServiceImplementation.class)) {
			implementedServiceDefinitions = element.getClass().getAnnotation(ServiceImplementation.class).serviceDefinition();
			for (Class implementedServiceDefinition : implementedServiceDefinitions) {
				if(!implementedServiceDefinition.isAnnotationPresent(ServiceDefinition.class)) {
					throw new IllegalArgumentException("Service implementation '"+ element.getClass() +"' declares implementation of service definition '"+ implementedServiceDefinition +"' which is not annotated with @ServiceDefinition.");
				} else if(!implementedServiceDefinition.isInstance(element)) {
					throw new IllegalArgumentException("Service implementation class '"+ element.getClass() +"' should implement (or extend) service definition class '"+ implementedServiceDefinition +"'.");
				}
			}
		}
		
		//managed member definition
		if(element.getClass().isAnnotationPresent(ManagedMember.class)) {
			implementedManagedMemberDefinition = element.getClass();
		}
		
		//extension point definition of the extension point implemented by this element
		if(element.getClass().isAnnotationPresent(ExtensionPointImplementation.class)) {
			implementedExtensionPointDefinition = element.getClass().getAnnotation(ExtensionPointImplementation.class).extensionPointDefinition();
			if(!implementedExtensionPointDefinition.isAnnotationPresent(ExtensionPointDefinition.class)) {
				throw new IllegalArgumentException("Extension point implementation '"+ element.getClass() +"' declares implementation of extension point definition '"+ implementedExtensionPointDefinition +"' which is not annotated with @ExtensionPointDefinition.");
			} else if(!implementedExtensionPointDefinition.isInstance(element)) {
				throw new IllegalArgumentException("Extension point implementation class '"+ element.getClass() +"' should implement (or extend) extension point definition class '"+ implementedExtensionPointDefinition +"'.");
			}
		}

		//PROCESS INJECTION ANNOTATIONS
		//injections directly from this element class
		addInjectionsFrom(new Class[]{element.getClass()}, false);

		//injections by referenced elements
		if(element.getClass().isAnnotationPresent(ServiceImplementation.class)) {
			ServiceImplementation ei = element.getClass().getAnnotation(ServiceImplementation.class);
			if(!ei.useDependenciesFrom().equals(Object.class)) {
				addInjectionsFrom(ei.useDependenciesFrom(), true);
			}
		}
		if(element.getClass().isAnnotationPresent(ExtensionPointImplementation.class)) {
			ExtensionPointImplementation ei = element.getClass().getAnnotation(ExtensionPointImplementation.class);
			if(!ei.useDependenciesFrom().equals(Object.class)) {
				addInjectionsFrom(ei.useDependenciesFrom(), true);
			}
		}
		if(element.getClass().isAnnotationPresent(ManagedMember.class)) {
			ManagedMember ei = element.getClass().getAnnotation(ManagedMember.class);
			if(!ei.useDependenciesFrom().equals(Object.class)) {
				addInjectionsFrom(ei.useDependenciesFrom(), true);
			}
		}
		if(element.getClass().isAnnotationPresent(PluginDefinition.class)) {
			PluginDefinition pd = element.getClass().getAnnotation(PluginDefinition.class);
			if(!pd.useDependenciesFrom().equals(Object.class)) {
				addInjectionsFrom(pd.useDependenciesFrom(), true);
			}
		}
		
	}

	private void addInjectionsFrom(Class[] elementClasses, boolean fromDependencies) {
		for (Class elementClass : elementClasses) {
			//service injections
			injectedServices.addAll(setFromDependencies(getInjectedServiceMethods(elementClass), fromDependencies));
			injectedServices.addAll(setFromDependencies(getInjectedServiceFields(elementClass), fromDependencies));
			
			//extension point injections
			injectedExtensionPoints.addAll(setFromDependencies(getInjectedExtensionPointMethods(elementClass), fromDependencies));
			injectedExtensionPoints.addAll(setFromDependencies(getInjectedExtensionPointFields(elementClass), fromDependencies));
	
			//managed member injections
			injectedManagedMembers.addAll(setFromDependencies(getInjectedManagedMemberMethods(elementClass), fromDependencies));
			injectedManagedMembers.addAll(setFromDependencies(getInjectedManagedMemberFields(elementClass), fromDependencies));
		}
	}

	private Collection<? extends InjectionReference> setFromDependencies(List<InjectionReference> injections, boolean fromDependencies) {
		for (InjectionReference ir : injections) {
			ir.setFromDependencies(fromDependencies);
		}
		return injections;
	}

	private static List<InjectionReference> getInjectedServiceMethods(Class member) {
		List<InjectionReference> r = new ArrayList<InjectionReference>();
		for (Method m : member.getMethods()) {
			if(m.isAnnotationPresent(ServiceInjection.class)) {
				if(m.getParameterTypes().length==1 && m.getParameterTypes()[0].isAnnotationPresent(ServiceDefinition.class)) {
					boolean optional = m.getAnnotation(ServiceInjection.class).optionalAtInitialization();
					r.add(new InjectionReference(m.getParameterTypes()[0], m, m.getDeclaringClass(), optional));
				} else {
					logger.warning("Wrong usage of annotation @ServiceInjection for method "+ m + ". The method should have a single argument whose class is annotated with @ServiceDefinition.");
				}
			}
		}
		return r;
	}

	private static List<InjectionReference> getInjectedManagedMemberMethods(Class member) {
		List<InjectionReference> r = new ArrayList<InjectionReference>();
		for (Method m : member.getMethods()) {
			if(m.isAnnotationPresent(MemberInjection.class)) {
				if(m.getParameterTypes().length==1 && m.getParameterTypes()[0].isAnnotationPresent(ManagedMember.class)) {
					boolean optional = m.getAnnotation(MemberInjection.class).optionalAtInitialization();
					r.add(new InjectionReference(m.getParameterTypes()[0], m, m.getDeclaringClass(), optional));
				} else {
					logger.warning("Wrong usage of annotation @MemberInjection for method "+ m + ". The method should have a single argument whose class is annotated with @ManagedMember.");
				}
			}
		}
		return r;
	}

	private static List<InjectionReference> getInjectedServiceFields(Class member) {
		List<InjectionReference> r = new ArrayList<InjectionReference>();
		for (Field f : member.getFields()) {
			if(f.isAnnotationPresent(ServiceInjection.class)) {
				if(Modifier.isPublic(f.getModifiers()) && f.getType().isAnnotationPresent(ServiceDefinition.class)) {
					boolean optional = f.getAnnotation(ServiceInjection.class).optionalAtInitialization();
					r.add(new InjectionReference(f.getType(), f, f.getDeclaringClass(), optional));
				} else {
					throw new IllegalArgumentException("Wrong usage of annotation @ServiceInjection for field "+ f + ". The field should be 'public' and declare a type whose class is annotated with @ServiceDefinition.");
				}
			}
		}
		return r;
	}

	private static List<InjectionReference> getInjectedManagedMemberFields(Class member) {
		List<InjectionReference> r = new ArrayList<InjectionReference>();
		for (Field f : member.getFields()) {
			if(f.isAnnotationPresent(MemberInjection.class)) {
				if(Modifier.isPublic(f.getModifiers()) && f.getType().isAnnotationPresent(ManagedMember.class)) {
					boolean optional = f.getAnnotation(MemberInjection.class).optionalAtInitialization();
					r.add(new InjectionReference(f.getType(), f, f.getDeclaringClass(), optional));
				} else {
					throw new IllegalArgumentException("Wrong usage of annotation @MemberInjection for field "+ f + ". The field should be 'public' and declare a type whose class is annotated with @ManagedMember.");
				}
			}
		}
		return r;
	}

	private static List<InjectionReference> getInjectedExtensionPointMethods(Class member) {
		List<InjectionReference> r = new ArrayList<InjectionReference>();
		for (Method m : member.getMethods()) {
			if(m.isAnnotationPresent(ExtensionPointsInjection.class)) {
				Class extensionPointDef = null;
				if(m.isAnnotationPresent(ExtensionPointsInjection.class)) {
					if(Modifier.isPublic(m.getModifiers())) {
						if(m.getGenericParameterTypes().length==1) {
							if(m.getGenericParameterTypes()[0] instanceof ParameterizedType) {
								ParameterizedType pt = (ParameterizedType)m.getGenericParameterTypes()[0];
								if(pt.getRawType().equals(List.class)) {
									if(pt.getActualTypeArguments().length==1) {
										Class c = ((Class)pt.getActualTypeArguments()[0]);
										if(c.isAnnotationPresent(ExtensionPointDefinition.class)) {
											extensionPointDef = c;
										}
									}
								}
							}
						}
					}
				}
				if(extensionPointDef==null) {
					throw new IllegalArgumentException("Wrong usage of annotation @ExtensionPointsInjection for method "+ m + ". The method should be 'public' and have a single parameter whose class is 'List' with a generics argument pointing to a class annotated with @ExtensionPointDefinition. Ex.: public void setMyExtensionPointsImpl(List<MyExtensionPoint> ep) {...}");
				} else {
					r.add(new InjectionReference(extensionPointDef, m, m.getDeclaringClass(), false));
				}
			}
		}
		return r;
	}

	private static List<InjectionReference> getInjectedExtensionPointFields(Class member) {
		List<InjectionReference> r = new ArrayList<InjectionReference>();
		for (Field f : member.getFields()) {
			if(f.isAnnotationPresent(ExtensionPointsInjection.class)) {
				Class extensionPointDef = null;
				if(f.isAnnotationPresent(ExtensionPointsInjection.class)) {
					if(f.getGenericType() instanceof ParameterizedType) {
						ParameterizedType pt = (ParameterizedType)f.getGenericType();
						if(pt.getRawType().equals(List.class)) {
							if(pt.getActualTypeArguments().length==1) {
								Class c = ((Class)pt.getActualTypeArguments()[0]);
								if(c.isAnnotationPresent(ExtensionPointDefinition.class)) {
									extensionPointDef = c;
								}
							}
						}
					}
				}
				if(extensionPointDef==null) {
					throw new IllegalArgumentException("Wrong usage of annotation @ExtensionPointsInjection for field "+ f + ". The field should be 'public' and declare a type whose class is 'List' with a generics argument pointing to a class annotated with @ExtensionPointDefinition. Ex.: public List<MyExtensionPoint> myExtensionPoints;");
				} else {
					r.add(new InjectionReference(extensionPointDef, f, f.getDeclaringClass(), false));
				}
			}
		}
		return r;
	}

	protected boolean canBeInitialized(PluginManager pluginManager) {
		//SERVICE DEPENDENCIES
		for (InjectionReference ir : injectedServices) {
			Object mm = pluginManager.getInitializedServiceImplementations().get(ir.getDefinitionType());
			if(mm==null) {
//				System.out.println(element.getClass().getName() + "->" + ir.getDefinitionType() + " " + ir.isOptional());
				if(!ir.isOptional()) {
					return false;
				}
			}
		}
		
		//MANAGED MEMBERS DEPENDENCIES
		for (InjectionReference ir : injectedManagedMembers) {
			Object mm = pluginManager.getInitializedManagedMembers().get(ir.getDefinitionType());
			if(mm==null) {
//				System.out.println(element.getClass().getName() + "->" + ir.getDefinitionType() + " " + ir.isOptional());
				if(!ir.isOptional()) {
					return false;
				}
			}
		}

		//ALL REFERENCING EXTENSION POINT IMPLEMENTATIONS WERE LOADED
//		for (InjectionReference ie : injectedExtensionPoints) {
//			for (PluginElement pe : pluginManager.getPluginElements()) {
//				if(pe.getImplementedExtensionPointDefinition()!=null) {
//					if(pe.getImplementedExtensionPointDefinition().equals(ie.getClassElement())) {
//						if(!pe.isActivated()) {
//							return false;
//						}
//					}
//				}
//			}
//		}
		
		return true;
	}

	public boolean isOnlyExtensionPointImplementation() {
		return getImplementedExtensionPointDefinition()!=null && getImplementedManagedMemberDefinition()==null && getImplementedServiceDefinitions().length==0;
	}

	protected void processServiceInjections(Map<Class, Object> initializedServiceImplementations, boolean onlyOptional) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		for (InjectionReference ir : injectedServices) {
			if(!ir.isFromDependencies()) {
				Object value = initializedServiceImplementations.get(ir.getDefinitionType());
	//			if(value==null && !enablePartialInjections) {
	//				throw new IllegalStateException("Element '" + getElement().getClass() + "' depends on service '" + ir.getDefinitionType() + "' and it could not be found");
	//			}
				if(!onlyOptional || ir.isOptional()) {
					ir.performInjection(element, value);
				}
			}
		}
	}

	protected void processExtensionPointInjections(PluginManager pluginManager) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
		for (InjectionReference ir : injectedExtensionPoints) {
			if(!ir.isFromDependencies()) {
				List<Object> extensionPointImpls = new ArrayList<Object>();
				//look for extension points implementations, activate them and inject to element
				for (PluginElement pe : pluginManager.getPluginElements()) {
					if(pe.getImplementedExtensionPointDefinition()!=null) {
						if(pe.getImplementedExtensionPointDefinition().equals(ir.getDefinitionType())) {
							ExtensionPointsInjection ei = ir.getClassElement().getAnnotation(ExtensionPointsInjection.class);
							//if this extension point impl was annotated with Service, ManagedMember or explicitly set to use singleton, use singleton instance
							if(!ei.createNewInstances() || pe.getImplementedManagedMemberDefinition()!=null || pe.getImplementedServiceDefinitions().length>0) {
								logger.fine("Using singleton extension point implementation instance for " + ir.getClassElement());
								extensionPointImpls.add(pe.getElement());
								
							//create and manage a new instance of the extension point
							} else {
								Object o = pe.getElement().getClass().newInstance();
								pluginManager.manageObject(o, false);
								extensionPointImpls.add(o);
							}
						}
					}
				}
				
				//create new instances if needed
//				List<Object> injectedExtensionPointImpls = new ArrayList<Object>();
//				ExtensionPointsInjection ei = ir.getClassElement().getAnnotation(ExtensionPointsInjection.class);
//				if(ei.createNewInstances()) {
//					//create and manage new instances for each extension point found
//					for (Object obj : extensionPointImpls) {
//						pluginManager.manageObject(obj.getClass().newInstance(), false);
//						injectedExtensionPointImpls.add(obj);
//					}
//				} else {
//					injectedExtensionPointImpls.addAll(extensionPointImpls);
//				}
				
//				System.out.println("---- INJECTING " + element + " " + ir.getClassElement() + "->" + extensionPointImpls);
				ir.performInjection(element, extensionPointImpls);
			}
		}
	}

	protected void processManagedMemberInjections(PluginManager pluginManager, boolean onlyOptional) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
		for (InjectionReference ir : injectedManagedMembers) {
			if(!ir.isFromDependencies()) {
				//DETERMINE CORRECT MANAGED MEMBER REFERENCE TO BE INJECTED
				MemberInjection mi = ir.getClassElement().getAnnotation(MemberInjection.class);
				Object value;
				if(mi.createNewInstance()) {
					value = ir.getDefinitionType().newInstance();
					PluginElement pe = new PluginElement(getPlugin(), value);
					//FIXME the newly created element may have inconsistent dependencies on initialization 
					pluginManager.getPluginElementsDynamic().add(pe);
					pluginManager.initializeElement(pe, false);
				} else {
					value = pluginManager.getInitializedManagedMembers().get(ir.getDefinitionType());
				}
	//			if(value==null && !enablePartialInjections) {
	//				throw new IllegalStateException("Element '" + getElement().getClass() + "' depends on managed member '" + ir.getDefinitionType() + "' and it could not be found");
	//			}
	//			
				//INJECT REFERENCE
				if(!onlyOptional || ir.isOptional()) {
					ir.performInjection(element, value);
				}
			}
		}
	}

	public void initializeElement() {
		if(functional) {
			if(element instanceof Activable) {
				Activable a = (Activable)element;
				try {
					if(!a.isInitialized()) {
						a.init();
					}
				} catch (Exception e) {
					failedToInitialize = true;
					throw new RuntimeException("Failed to initialize " + element.getClass().getName(),e);
				}
			}
		}
		initialized = true;
	}

	public void activateElement() {
		if(functional) {
			if(element instanceof Activable) {
				Activable a = (Activable)element;
				try {
					a.activate();
				} catch (Exception e) {
					failedToActivate = true;
					throw new RuntimeException("Failed to activate " + element.getClass().getName(),e);
				}
			}
		}
		activated = true;
	}

	public void deactivateElement() throws Exception {
		if(element instanceof ThreadWorker) {
			ThreadWorker a = (ThreadWorker)element;
			if(a.isActive()) {
				a.forceDeactivation(3000);
			}
		} else if(element instanceof Activable) {
			Activable a = (Activable)element;
			if(a.isActive()) {
				a.deactivate();
			}
		}
		initialized = false;
		activated = false;
	}
	
	public boolean isActivated() {
		return activated;
	}
	
	public boolean isFailedToActivate() {
		return failedToActivate;
	}
	
	public boolean isInitialized() {
		return initialized;
	}
	
	public boolean isFailedToInitialize() {
		return failedToInitialize;
	}
	
	public Object getElement() {
		return element;
	}
	
	public Plugin getPlugin() {
		return plugin;
	}

	public List<InjectionReference> getInjectedServices() {
		return injectedServices;
	}
	
	public List<InjectionReference> getInjectedManagedMembers() {
		return injectedManagedMembers;
	}
	
	public List<InjectionReference> getInjectedExtensionPoints() {
		return injectedExtensionPoints;
	}
	
	public Class getImplementedExtensionPointDefinition() {
		return implementedExtensionPointDefinition;
	}
	
	public Class getImplementedManagedMemberDefinition() {
		return implementedManagedMemberDefinition;
	}
	
	public Class[] getImplementedServiceDefinitions() {
		return implementedServiceDefinitions;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((element == null) ? 0 : element.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PluginElement other = (PluginElement) obj;
		if (element == null) {
			if (other.element != null)
				return false;
		} else if (!element.equals(other.element))
			return false;
		return true;
	}
	
	public void setFunctional(boolean functional) {
		this.functional = functional;
	}
	
}
