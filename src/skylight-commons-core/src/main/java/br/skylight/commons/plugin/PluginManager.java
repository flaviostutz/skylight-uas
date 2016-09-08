package br.skylight.commons.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import br.skylight.commons.infra.Activable;
import br.skylight.commons.infra.ReflectionHelper;
import br.skylight.commons.plugin.annotations.ServiceDefinition;
import br.skylight.commons.plugin.annotations.ServiceImplementation;

@ServiceDefinition
@ServiceImplementation(serviceDefinition = PluginManager.class)
public class PluginManager {

	private static final Logger logger = Logger.getLogger(PluginManager.class.getName());

	private static final Map<String, PluginManager> instances = new HashMap<String, PluginManager>();
	private List<Runnable> executeAfterStartup = new CopyOnWriteArrayList<Runnable>();

	private boolean loadPluginsFromServiceLoader = true;
	private String name;
	private Set<Class<? extends Plugin>> pluginTypes = new HashSet<Class<? extends Plugin>>();
	private Map<Class<? extends Plugin>, List<Plugin>> pluginInstances = null;

	private float currentPercent = 0F;
	private List<PluginManagerListener> listeners = new CopyOnWriteArrayList<PluginManagerListener>();

	private List<PluginElement> pluginElements = new ArrayList<PluginElement>();
	private List<PluginElement> pluginElementsDynamic = new ArrayList<PluginElement>();
	private List<PluginElement> initializedElements = new CopyOnWriteArrayList<PluginElement>();

	private Map<Class, Object> initializedServiceImplementations = new HashMap<Class, Object>();// definition,implementation
//	private Map<Class, List<Object>> initializedExtensionPointImplementations = new HashMap<Class, List<Object>>();// definition,implementation
	// efault list of elements. If injection annotation has 'createNewInstance==true', a new instance will be created and will not put here
	private Map<Class, Object> initializedManagedMembers = new HashMap<Class, Object>();

	private boolean pluginsStarted;

	// startup options
	private boolean startupReadClasspathDirs = true;
	private boolean startupReadClasspathJars = true;
	private String startupJarsFileNamePrefix = null;
	private String startupJarsFileNameSufix = null;
	private boolean startupAllPluginsMustBeActivated = true;
	private boolean useCachedIndexForPluginElements = false;

	private PluginManager(String name) {
		this.name = name;
		pluginTypes.add(Plugin.class);

		// always register itself as an available service
		initializedServiceImplementations.put(PluginManager.class, this);
	}

	public void registerPluginInstance(Class<? extends Plugin> pluginType, Plugin plugimImpl) {
		if (!pluginTypes.contains(pluginType)) {
			throw new IllegalArgumentException("Plugin type '" + pluginType + "' is not registered in PluginManager");
		}
		getPluginInstances().get(pluginType).add(plugimImpl);
	}

	public void registerPluginType(Class<? extends Plugin> pluginType) {
		if (pluginInstances != null) {
			throw new IllegalStateException("Plugins already loaded. Cannot set additional plugin types now.");
		}
		pluginTypes.add(pluginType);
	}

	public Map<Class<? extends Plugin>, List<Plugin>> getPluginInstances() {
		if (pluginInstances == null) {
			pluginInstances = new HashMap<Class<? extends Plugin>, List<Plugin>>();
			for (Class<? extends Plugin> pt : pluginTypes) {
				// prepare map for plugin type
				if (pluginInstances.get(pt) == null) {
					pluginInstances.put(pt, new ArrayList<Plugin>());
				}

				if (loadPluginsFromServiceLoader) {
					// load services into map
					List<Plugin> pi = (List<Plugin>) pluginInstances.get(pt);
					ServiceLoader<? extends Plugin> result = ServiceLoader.load(pt);
					for (Plugin plugin : result) {
						pi.add(plugin);
					}
				}
			}
		}
		return pluginInstances;
	}

	/**
	 * Will try to startup all plugins. The order is determined by the presence
	 * of registered services that are needed by getRequiredServiceTypes() on
	 * each plugin. Ex.: A plugin with no dependencies is loaded first, then
	 * another plugin with a declared dependency will verify if that dependency
	 * was satisfied during startup of the first plugin and so on. If after
	 * loading all possible plugins (whose dependencies were satisfied) a plugin
	 * was not loaded (due to missing dependencies), that plugin will not be
	 * loaded.
	 * 
	 * @param allPluginsMustBeLoaded
	 *            Throw an RuntimeException if not all plugins were loaded due
	 *            to missing dependencies
	 */
	public void startupPlugins() {
		logger.info("=== " + name + ": STARTING UP ===");

		if (initializedElements.size() > 0) {
			throw new IllegalStateException("Cannot activate PluginManager. It was already activated.");
		}
		List<Plugin> plugins = getAllPlugins();
		if (plugins.size() == 0) {
			log("No plugins found", 1);
			return;
		}

		// PROCESS AND VALIDATE ANNOTATIONS FOR ALL PLUGIN MEMBERS
		float currentPercent = 0.05F;
		log("Searching plugins...", currentPercent);
		long initialTime = System.currentTimeMillis();

		//TRY TO READ A PREVIOUSLY SAVED PLUGIN MEMBERS INDEX
		boolean saveIndex = false;
		HashMap<String,ArrayList<Class>> elementsIndex = new HashMap<String,ArrayList<Class>>();
		File indexFile = null;
		try {
			if(useCachedIndexForPluginElements) {
				indexFile = new File("PluginManager-"+ name +".idx");
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(indexFile));
				elementsIndex = (HashMap<String,ArrayList<Class>>)ois.readObject();
				ois.close();
				logger.info("Using a previous saved list of plugin elements");
			}
		} catch (Exception e2) {
			logger.warning("Couldn't load previous index file for plugin elements. Creating a new one. e=" + e2.toString());
		}
		
		for (Plugin plugin : plugins) {
			ArrayList<Class> classes = null;
			
			//TRY TO LOAD FROM INDEX
			if(elementsIndex!=null) {
				classes = elementsIndex.get(plugin.getName());
			}

			//NOT FOUND IN INDEX. SEARCH PLUGIN ELEMENTS IN CLASSPATH
			if(classes==null) {
				classes = plugin.getPluginMembers(startupReadClasspathDirs, startupReadClasspathJars, startupJarsFileNamePrefix, startupJarsFileNameSufix);
				elementsIndex.put(plugin.getName(), classes);
				saveIndex = true;
			}

			// PROCESS AND VALIDATE ANNOTATION FOR ALL PLUGIN MEMBERS
			for (Class memberClass : classes) {
				try {
					Object obj = plugin;// use same plugin instance
					if (!memberClass.equals(plugin.getClass())) {
						if (ReflectionHelper.hasDefaultConstructor(memberClass)) {
							try {
								obj = memberClass.newInstance();
							} catch (Exception e1) {
								logger.severe("Couldn't instantiate element " + memberClass + ": " + e1);
								e1.printStackTrace();
								continue;
							}
						} else {
							logger.severe("Plugin member " + memberClass + " should have the default constructor. It won't be loaded.");
						}
					}
					if (obj != null) {
						PluginElement pe = new PluginElement(plugin, obj);
						//At this phase extension point only elements are not functional as they are used only for dependency verification
						//New functional elements will be created directly at extension point injections
						if(pe.isOnlyExtensionPointImplementation()) {
							pe.setFunctional(false);
						}
						pluginElements.add(pe);
					}
				} catch (Exception e) {
					logger.severe("There was a problem processing annotations for " + memberClass + ": " + e);
					e.printStackTrace();
					if (startupAllPluginsMustBeActivated) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		
		//SAVE PLUGIN ELEMENTS INDEX
		if(useCachedIndexForPluginElements && saveIndex) {
			try {
				ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(indexFile));
				ois.writeObject(elementsIndex);
				ois.flush();
				ois.close();
				logger.info("Index of plugin elements saved");
			} catch (Exception e) {
				logger.warning("There was a problem writing an index file with plugin elements. e=" + e.toString());
				e.printStackTrace();
			}
		}

		logger.info("Plugins discovered in " + (System.currentTimeMillis() - initialTime) + "ms");
		
		// INITIALIZE ALL PLUGIN ELEMENTS
		// INITIALIZE PLUGIN ELEMENTS IN ORDER OF INTER-DEPENDENCIES
		log("Initializing plugins...", currentPercent);
		float elementsPercent = 0.9F / (float) pluginElements.size();
		boolean atLeastOneWasInitialized = true;
		int initializedPluginElements = 0;
		while (initializedPluginElements < pluginElements.size() && atLeastOneWasInitialized) {
			atLeastOneWasInitialized = false;
			for (PluginElement pluginElement : pluginElements) {
				if (!pluginElement.isInitialized() && !pluginElement.isFailedToInitialize()) {
					if (pluginElement.canBeInitialized(this)) {
						try {
							log("Initializing " + pluginElement.getElement().getClass().getSimpleName() + "...", currentPercent);
							initializeElement(pluginElement, true);
							currentPercent += elementsPercent;
							atLeastOneWasInitialized = true;
							initializedPluginElements++;
						} catch (Exception e) {
							log("Initializing " + pluginElement.getElement().getClass().getSimpleName() + "... FAILED");
							logger.throwing(null, null, e);
							e.printStackTrace();
						} catch (Error e) {
							log("Initializing " + pluginElement.getElement().getClass().getSimpleName() + "... ERROR");
							logger.throwing(null, null, e);
							e.printStackTrace();
						}
					}
				}
			}
		}

		// REPROCESS INJECTIONS BECAUSE OPTIONAL DEPENDENCIES ON ACTIVATED
		// ELEMENTS COULD BE NULL DURING INITIALIZATION AND MAY EXIST NOW
		// (used for solving cyclic dependencies)
		List<PluginElement> allPluginElements = new ArrayList<PluginElement>();
		allPluginElements.addAll(pluginElements);
		allPluginElements.addAll(pluginElementsDynamic);
		for (PluginElement pluginElement : allPluginElements) {
			try {
				// INJECT SIMPLE MEMBER IMPLEMENTATIONS
				pluginElement.processManagedMemberInjections(this, true);
				// INJECT SERVICE IMPLEMENTATIONS
				pluginElement.processServiceInjections(initializedServiceImplementations, true);
				// INJECT EXTENSION POINT IMPLEMENTATIONS
				pluginElement.processExtensionPointInjections(this);
			} catch (Exception e) {
				log("Post injection on " + pluginElement.getElement().getClass().getSimpleName() + "... FAILED");
				logger.throwing(null, null, e);
				e.printStackTrace();
			} catch (Error e) {
				log("Post injection on " + pluginElement.getElement().getClass().getSimpleName() + "... ERROR");
				logger.throwing(null, null, e);
				e.printStackTrace();
			}
		}

		// SHOW INFO ABOUT ELEMENTS THAT COULD NOT BE LOADED
		//TODO FIND ROOT MISSING DEPENDENCIES CAUSES
		for (PluginElement pluginElement : pluginElements) {
			if (!pluginElement.isInitialized()) {
				// info about missing services
				List<Class> missingServices = new ArrayList<Class>();
				for (InjectionReference ir : pluginElement.getInjectedServices()) {
					if (!initializedServiceImplementations.containsKey(ir.getDefinitionType())) {
						missingServices.add(ir.getDefinitionType());
					}
				}
				// info about missing managed members
				List<Class> missingManagedMembers = new ArrayList<Class>();
				for (InjectionReference ir : pluginElement.getInjectedManagedMembers()) {
					if (!initializedManagedMembers.containsKey(ir.getDefinitionType())) {
						missingManagedMembers.add(ir.getDefinitionType());
					}
				}
				log("Plugin " + pluginElement.getPlugin() + " failed to load", currentPercent);
				logger.info(">>>>>>>>>>>>> Plugin element " + pluginElement.getElement().getClass().getName() + " was not initialized. missing services='" + Arrays.deepToString(missingServices.toArray(new Class[0])) + "'; missing managed members='" + Arrays.deepToString(missingManagedMembers.toArray(new Class[0])) + "'; canBeActivated=" + pluginElement.canBeInitialized(this));
			}
		}

		// warn about missing extension point usage
//		for (Entry<Class, List<Object>> rs : initializedExtensionPointImplementations.entrySet()) {
//			boolean isUsed = false;
//			for (PluginElement pluginElement : pluginElements) {
//				for (InjectionReference ir : pluginElement.getInjectedExtensionPoints()) {
//					if (ir.getDefinitionType().equals(rs.getKey())) {
//						isUsed = true;
//					}
//				}
//			}
//			if (!isUsed) {
//				logger.warning("There are implementations for extension point '" + rs.getKey() + "' but they won't used by any plugin (orphan)");
//			}
//		}

		// verify loading results
		currentPercent = 1F;
		if (initializedPluginElements < pluginElements.size()) {// failure
			// rollback system startup if needed
			if (startupAllPluginsMustBeActivated) {
				log("Startup failed...");
				shutdownPlugins();
				for (PluginManagerListener l : listeners) {
					l.onPluginsStartupFailed();
				}
				throw new IllegalStateException("Some plugin elements have failed to load. Check logs for instantiation or service dependency problems.");
			} else {
				log("Not all plugins were started successfuly", currentPercent);
				logger.info("=== " + name + ": " + initializedPluginElements + " OF " + pluginElements.size() + " ELEMENTS INITIALIZED ===");
				for (PluginManagerListener l : listeners) {
					l.onPluginsStartupFinished(true);
				}
				activateInitializedElements();
				executePostActions();
			}
		} else {
			log("All plugins were started successfuly", currentPercent);
			logger.info("=== " + name + ": " + initializedPluginElements + " OF " + pluginElements.size() + " ELEMENTS INITIALIZED IN ===");
			for (PluginManagerListener l : listeners) {
				l.onPluginsStartupFinished(false);
			}
			activateInitializedElements();
			executePostActions();
		}

		// shutdown plugins thread hook
		Thread t = new Thread() {
			public void run() {
				PluginManager.shutdownAllPluginManagers();
			};
		};
		Runtime.getRuntime().addShutdownHook(t);
		pluginsStarted = true;
		
		logger.info(">>>"+ name +": Startup finished in " + (System.currentTimeMillis() - initialTime) + "ms");
	}

	private void executePostActions() {
		for (Runnable runnable : executeAfterStartup) {
			try {
				runnable.run();
			} catch (Exception e) {
				logger.warning("Problem executing post startup run(). runnable="+ runnable +"; e=" + e.toString());
				e.printStackTrace();
			}
		}
	}

	// ACTIVATE INITIALIZED ELEMENTS
	private void activateInitializedElements() {
		logger.info("=== ALL INITIALIZED ELEMENTS WILL START WORKING NOW ==");
		for (PluginElement initializedElement : initializedElements) {
			initializedElement.activateElement();
		}
	}

	public static void shutdownAllPluginManagers() {
		for (Entry<String, PluginManager> pm : instances.entrySet()) {
			pm.getValue().shutdownPlugins();
		}
	}

	protected void initializeElement(PluginElement pluginElement, boolean registerInstanceAsGlobal) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
		// INJECT SIMPLE MEMBER IMPLEMENTATIONS
		pluginElement.processManagedMemberInjections(this, false);

		// INJECT SERVICE IMPLEMENTATIONS
		pluginElement.processServiceInjections(initializedServiceImplementations, false);

		// INITIALIZE ELEMENT
		pluginElement.initializeElement();

		// REGISTER NEW SERVICE/EXTENSION IMPLEMENTATIONS
		if(registerInstanceAsGlobal) {
			//services
			for (Class is : pluginElement.getImplementedServiceDefinitions()) {
				if (initializedServiceImplementations.containsKey(is)) {
					logger.warning("An existing service implementation for definition '" + is.getName() + "' will be replaced by '" + pluginElement.getElement().getClass().getName() + "'");
				}
				initializedServiceImplementations.put(is, pluginElement.getElement());
			}

			//extension points
//			if (pluginElement.getImplementedExtensionPointDefinition() != null) {
//				List<Object> eps = resolveInitializedExtensionPointImplementations(pluginElement.getImplementedExtensionPointDefinition());
//				eps.add(pluginElement.getElement());
//			}

			//managed members
			if (pluginElement.getImplementedManagedMemberDefinition() != null) {
				if (!initializedManagedMembers.containsKey(pluginElement.getImplementedManagedMemberDefinition())) {
					initializedManagedMembers.put(pluginElement.getImplementedManagedMemberDefinition(), pluginElement.getElement());
				}
			}
		}

		if (!initializedElements.contains(pluginElement)) {
			initializedElements.add(pluginElement);
		}
	}

	public void log(String message) {
		log(message, currentPercent);
	}

	public void log(String message, float percent) {
		currentPercent = percent;
		logger.info(message);
		for (PluginManagerListener l : listeners) {
			l.onStartupStatusChanged(message, (int) (percent * 100F));
		}
	}

	public List<Plugin> getAllPlugins() {
		List<Plugin> plugins = new ArrayList<Plugin>();
		for (List<Plugin> pit : getPluginInstances().values()) {
			for (Plugin plugin : pit) {
				plugins.add(plugin);
			}
		}
		return plugins;
	}

	public void shutdownPlugins() {
		logger.info(">>> INITIATING " + name + " SHUTDOWN. Initialized elements: " + initializedElements.size());
		if (initializedElements.size() > 0) {
			// order deactivation in reverse order of activation
			List<PluginElement> reverse = new ArrayList<PluginElement>(initializedElements);
			Collections.reverse(reverse);
			for (PluginElement pluginElement : reverse) {
				logger.info("Deactivating " + pluginElement.getElement().getClass().getSimpleName() + "...");
				System.out.println("Deactivating " + pluginElement.getElement().getClass().getSimpleName() + "...");
				try {
					pluginElement.deactivateElement();
					initializedElements.remove(pluginElement);
					// unregister services/extension points
					for (Class sd : pluginElement.getImplementedServiceDefinitions()) {
						initializedServiceImplementations.remove(sd);
					}
					if (pluginElement.getImplementedManagedMemberDefinition() != null) {
						initializedManagedMembers.remove(pluginElement.getImplementedManagedMemberDefinition());
					}
//					if (pluginElement.getImplementedExtensionPointDefinition() != null) {
//						resolveInitializedExtensionPointImplementations(pluginElement.getImplementedExtensionPointDefinition()).remove(pluginElement.getElement());
//					}
				} catch (Exception e) {
					logger.warning("Failed to deactivate plugin element " + pluginElement.getElement().getClass().getName() + ". e=" + e);
					logger.throwing(null, null, e);
					e.printStackTrace();
				}
			}

			logger.info("=== "+ name +" SHUTDOWN FINISHED ===");
			System.out.println("=== "+ name +" SHUTDOWN FINISHED ===");
		}
		pluginsStarted = false;
	}

//	protected List<Object> resolveInitializedExtensionPointImplementations(Class extensionPointDefinition) {
//		synchronized (initializedExtensionPointImplementations) {
//			List<Object> ep = (List<Object>) initializedExtensionPointImplementations.get(extensionPointDefinition);
//			if (ep == null) {
//				ep = new CopyOnWriteArrayList<Object>();
//				initializedExtensionPointImplementations.put(extensionPointDefinition, ep);
//			}
//			return ep;
//		}
//	}

	public void setLoadPluginsFromServiceLoader(boolean loadPluginsFromServiceLoader) {
		this.loadPluginsFromServiceLoader = loadPluginsFromServiceLoader;
	}

	public void addPluginManagerListener(PluginManagerListener listener) {
		listeners.add(listener);
	}

	public PluginElement manageObject(Object obj) {
		return manageObject(obj, true);
	}
	
	public PluginElement manageObject(Object obj, boolean registerInstanceAsGlobal) {
		if (obj == null) {
			throw new IllegalArgumentException("Cannot manage a null object");
		}
		PluginElement pe = new PluginElement(null, obj);
		try {
			initializeElement(pe, registerInstanceAsGlobal);
			pe.processExtensionPointInjections(this);
			pe.activateElement();
			return pe;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void unmanageObject(Object obj) {
		if (obj instanceof Activable) {
			try {
				((Activable) obj).deactivate();
			} catch (Exception e) {
				logger.warning("Problem deactivating object. e=" + e);
				e.printStackTrace();
			}
		}
		initializedElements.remove(obj);
	}

//	public <T> T createAndManage(Class<T> type) {
//		if (ReflectionHelper.hasDefaultConstructor(type)) {
//			try {
//				return (T) manageObject(type.newInstance(), true, false);
//			} catch (Exception e) {
//				throw new RuntimeException(e);
//			}
//		} else {
//			throw new IllegalArgumentException("Class '" + type + "' should have the default constructor to be managed");
//		}
//	}

	public void restartAll() {
		shutdownPlugins();
		startupPlugins();
	}

	public List<PluginElement> getInitializedElements() {
		return initializedElements;
	}

	public static PluginManager getInstance(String name) {
		PluginManager pm = instances.get(name);
		if (pm == null) {
			pm = new PluginManager(name);
			instances.put(name, pm);
		}
		return pm;
	}

	public static PluginManager getInstance() {
		return getInstance("default");
	}

	public Map<Class, Object> getInitializedServiceImplementations() {
		return initializedServiceImplementations;
	}

	public Map<Class, Object> getInitializedManagedMembers() {
		return initializedManagedMembers;
	}

//	public Map<Class, List<Object>> getInitializedExtensionPointImplementations() {
//		return initializedExtensionPointImplementations;
//	}

	public List<PluginElement> getPluginElements() {
		return pluginElements;
	}

	public boolean isPluginsStarted() {
		return pluginsStarted;
	}

	public boolean isStartupReadClasspathDirs() {
		return startupReadClasspathDirs;
	}

	public void setStartupReadClasspathDirs(boolean startupReadClasspathDirs) {
		this.startupReadClasspathDirs = startupReadClasspathDirs;
	}

	public boolean isStartupReadClasspathJars() {
		return startupReadClasspathJars;
	}

	public void setStartupReadClasspathJars(boolean startupReadClasspathJars) {
		this.startupReadClasspathJars = startupReadClasspathJars;
	}

	public String getStartupJarsFileNamePrefix() {
		return startupJarsFileNamePrefix;
	}

	public void setStartupJarsFileNamePrefix(String startupJarsFileNamePrefix) {
		this.startupJarsFileNamePrefix = startupJarsFileNamePrefix;
	}

	public boolean isStartupAllPluginsMustBeActivated() {
		return startupAllPluginsMustBeActivated;
	}

	public void setStartupAllPluginsMustBeActivated(boolean startupAllPluginsMustBeActivated) {
		this.startupAllPluginsMustBeActivated = startupAllPluginsMustBeActivated;
	}
	
	protected List<PluginElement> getPluginElementsDynamic() {
		return pluginElementsDynamic;
	}

	public void executeAfterStartup(Runnable runnable) {
		if(pluginsStarted) {
			runnable.run();
		} else {
			executeAfterStartup.add(runnable);
		}
	}
	
	public void setStartupJarsFileNameSufix(String startupJarsFileNameSufix) {
		this.startupJarsFileNameSufix = startupJarsFileNameSufix;
	}
	
	public void setUseCachedIndexForPluginElements(boolean useCachedIndexForPluginElements) {
		this.useCachedIndexForPluginElements = useCachedIndexForPluginElements;
	}

}
