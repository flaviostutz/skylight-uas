package br.skylight.commons.plugin;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import br.skylight.commons.infra.Worker;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ManagedMember;
import br.skylight.commons.plugin.annotations.PluginDefinition;
import br.skylight.commons.plugin.annotations.ServiceImplementation;

public abstract class Plugin extends Worker {

	private static final Logger logger = Logger.getLogger(Plugin.class.getName());
	
	private ArrayList<Class> pluginMembers;
	
	public ArrayList<Class> getPluginMembers(boolean readClasspathDirs, boolean readClasspathJars, String jarsFileNamePrefix, String jarsFileNameSufix) {
		if(pluginMembers==null) {
			pluginMembers = new ArrayList<Class>();
			
			//add plugin itself as member
			pluginMembers.add(getClass());
			
			PluginDefinition pd = getClass().getAnnotation(PluginDefinition.class);
			if(pd!=null) {
				//add members explicitly set in PluginDefinition annotation
				for (Class member : pd.members()) {
					if(!member.equals(Object.class)//default value when not set 
						&& isValidMemberOnInit(member)) {
						pluginMembers.add(member);
					}
				}
			}

			//add all members from plugin package
			if(pd==null || pd.scanPackageMembers()) {
				List<Class> classes = ClasspathInspector.getMatchingClasses(getClass().getPackage().getName(), new Class[]{Object.class}, readClasspathDirs, readClasspathJars, jarsFileNamePrefix, jarsFileNameSufix);
				for (Class class1 : classes) {
					if(isValidMemberOnInit(class1)) {
						if(!pluginMembers.contains(class1)) {
							pluginMembers.add(class1);
						}
					}
				}
			}
			
		}
		return pluginMembers;
	}

	private boolean isValidMemberOnInit(Class<?> type) {
		if(!Modifier.isAbstract(type.getModifiers())) {
			if(type.getAnnotation(ServiceImplementation.class)!=null) {
				return type.getAnnotation(ServiceImplementation.class).loadOnStartup();
				
			} else if(type.getAnnotation(ExtensionPointImplementation.class)!=null) {
				return true;
				
			} else if(type.getAnnotation(ManagedMember.class)!=null) {
				return type.getAnnotation(ManagedMember.class).loadOnStartup();
				
			} else if(type.getAnnotation(PluginDefinition.class)!=null) {
				return true;
			}
		}
		return false;
	}
	
	public String getName() {
		return getClass().getSimpleName();
	}
	
}
