package br.skylight.commons.plugin;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Find classes in the classpath (reads JARs and classpath folders).
 * 
 * @author P&aring;l Brattberg, brattberg@gmail.com
 * @see http://gist.github.com/pal
 */
@SuppressWarnings("unchecked")
public class ClasspathInspector {

	/**
	 * @param readClassDirs Read classes from classpath directories
	 * @param readJars Read classes inside classpath jars
	 * @param jarsFileNamePrefix If null, returns all classes
	 * @return
	 */
	public static List<Class> getAllKnownClasses(boolean readClassDirs, boolean readJars, String jarsFileNamePrefix, String jarsFileNameSufix) {
		List<Class> classFiles = new ArrayList<Class>();
		List<File> classLocations = getClassLocationsForCurrentClasspath();
		for (File file : classLocations) {
			if(file.isDirectory()) {
				if(readClassDirs) {
					classFiles.addAll(getClassesFromPath(file));
				}
			} else if(file.isFile()) {
				if(readJars) {
					boolean readJarFile = false;
					if(jarsFileNamePrefix==null && jarsFileNameSufix==null) {
						readJarFile = true;
					} else {
						boolean prefixCriteria = (jarsFileNamePrefix==null || file.getName().toLowerCase().startsWith(jarsFileNamePrefix.toLowerCase()));
						boolean sufixCriteria = (jarsFileNameSufix==null || file.getName().toLowerCase().endsWith(jarsFileNameSufix.toLowerCase()));
						readJarFile = prefixCriteria && sufixCriteria;
					}
					if(readJarFile) {
						classFiles.addAll(getClassesFromPath(file));
					}
				}
			}
		}
		return classFiles;
	}

	/**
	 * @param packagePrefix If null, returns all classes
	 * @param interfacesOrSuperclasses Array of interfaces or superclasses for classes returned
	 * @param readClassDirs Read classes from classpath directories
	 * @param readJars Read classes inside classpath jars
	 * @param jarsFileNamePrefix If null, returns all classes
	 * @return
	 */
	public static List<Class> getMatchingClasses(String packagePrefix, Class[] interfacesOrSuperclasses, boolean readClassDirs, boolean readJars, String jarsFileNamePrefix, String jarsFileNameSufix) {
		List<Class> matchingClasses = new ArrayList<Class>();
		List<Class> classes = getAllKnownClasses(readClassDirs, readJars, jarsFileNamePrefix, jarsFileNameSufix);
		for (Class clazz : classes) {
			if(packagePrefix==null || clazz.getName().startsWith(packagePrefix)) {
				if(interfacesOrSuperclasses!=null) {
					for (Class interfaceOrSuperclass : interfacesOrSuperclasses) {
						if (interfaceOrSuperclass.isAssignableFrom(clazz)) {
							matchingClasses.add(clazz);
						}
					}
				} else {
					matchingClasses.add(clazz);
				}
			}
		}
		return matchingClasses;
	}

	public static List<Class> getMatchingClasses(String packagePrefix, String jarsFileNamePrefix, String jarsFileNameSufix) {
		return getMatchingClasses(packagePrefix, null, true, true, null, null);
	}

	public static List<Class> getMatchingClasses(Class[] interfacesOrSuperclasses) {
		return getMatchingClasses(null, interfacesOrSuperclasses, true, true, null, null);
	}
	
	private static Collection<? extends Class> getClassesFromPath(File path) {
		if (path.isDirectory()) {
			return getClassesFromDirectory(path);
		} else {
			return getClassesFromJarFile(path);
		}
	}

	private static String fromFileToClassName(final String fileName) {
		return fileName.substring(0, fileName.length() - 6).replaceAll("/|\\\\", "\\.");
	}

	private static List<Class> getClassesFromJarFile(File path) {
		List<Class> classes = new ArrayList<Class>();

		try {
			if (path.canRead()) {
				JarFile jar = new JarFile(path);
				Enumeration<JarEntry> en = jar.entries();
				while (en.hasMoreElements()) {
					JarEntry entry = en.nextElement();
					if (!entry.getName().startsWith("javax") && entry.getName().endsWith("class")) {
						String className = fromFileToClassName(entry.getName());
						loadClass(classes, className);
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to read classes from jar file: " + path, e);
		}

		return classes;
	}

	private static List<Class> getClassesFromDirectory(File path) {
		List<Class> classes = new ArrayList<Class>();

		// get jar files from top-level directory
		List<File> jarFiles = listFiles(path, new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		}, false);
		for (File file : jarFiles) {
			classes.addAll(getClassesFromJarFile(file));
		}

		// get all class-files
		List<File> classFiles = listFiles(path, new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".class");
			}
		}, true);

		// List<URL> urlList = new ArrayList<URL>();
		// List<String> classNameList = new ArrayList<String>();
		int substringBeginIndex = path.getAbsolutePath().length() + 1;
		for (File classfile : classFiles) {
			String className = classfile.getAbsolutePath().substring(substringBeginIndex);
			className = fromFileToClassName(className);
			loadClass(classes, className);
		}

		return classes;
	}

	private static List<File> listFiles(File directory, FilenameFilter filter, boolean recurse) {
		List<File> files = new ArrayList<File>();
		File[] entries = directory.listFiles();

		// Go over entries
		for (File entry : entries) {
			// If there is no filter or the filter accepts the
			// file / directory, add it to the list
			if (filter == null || filter.accept(directory, entry.getName())) {
				files.add(entry);
			}

			// If the file is a directory and the recurse flag
			// is set, recurse into the directory
			if (recurse && entry.isDirectory()) {
				files.addAll(listFiles(entry, filter, recurse));
			}
		}

		// Return collection of files
		return files;
	}

	public static List<File> getClassLocationsForCurrentClasspath() {
		List<File> urls = new ArrayList<File>();
		String javaClassPath = System.getProperty("java.class.path");
		if (javaClassPath != null) {
			for (String path : javaClassPath.split(File.pathSeparator)) {
				urls.add(new File(path));
			}
		}
		return urls;
	}

	// todo: this is only partial, probably
	public static URL normalize(URL url) throws MalformedURLException {
		String spec = url.getFile();

		// get url base - remove everything after ".jar!/??" , if exists
		final int i = spec.indexOf("!/");
		if (i != -1) {
			spec = spec.substring(0, spec.indexOf("!/"));
		}

		// uppercase windows drive
		url = new URL(url, spec);
		final String file = url.getFile();
		final int i1 = file.indexOf(':');
		if (i1 != -1) {
			String drive = file.substring(i1 - 1, 2).toUpperCase();
			url = new URL(url, file.substring(0, i1 - 1) + drive + file.substring(i1));
		}

		return url;
	}

	private static void loadClass(List<Class> classes, String className) {
		try {
			Class claz = Class.forName(className, false, ClassLoader.getSystemClassLoader());
			classes.add(claz);
		} catch (ClassNotFoundException cnfe) {
//			cnfe.printStackTrace();
		} catch (NoClassDefFoundError e) {
//			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// find all classes in classpath
		List<Class> allClasses = ClasspathInspector.getAllKnownClasses(true, true, null, null);
		System.out.printf("There are %s classes available in the classpath\n", allClasses.size());

		// find all classes that implement/subclass an interface/superclass
		List<Class> serializableClasses = ClasspathInspector.getMatchingClasses(new Class[]{Serializable.class});
		for (Class clazz : serializableClasses) {
			System.out.printf("%s is Serializable\n", clazz);
		}
		System.out.printf("There are %s Serializable classes available in the classpath\n", serializableClasses.size());
	}
}
