package com.cyanspring.common.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ClassEnumerator {
	private static final Logger log = LoggerFactory
			.getLogger(ClassEnumerator.class);

	private static Class<?> loadClass(String className, boolean init, ClassLoader cl) {
		try {
			if(null == cl)
				return Class.forName(className);
			else
				return Class.forName(className, true, cl);
		} 
		catch (ClassNotFoundException e) {
			throw new RuntimeException("Unexpected ClassNotFoundException loading class '" + className + "'");
		}
	}

	public static List<Class<?>> processDirectory(File directory, String pkgname, List<Class<?>> classes, ClassLoader cl) {
		log.info("Reading Directory '" + directory + "'");
		// Get the list of the files contained in the package
		if(null == classes)
			classes = new ArrayList<Class<?>>();
		String[] files = directory.list();
		for (int i = 0; i < files.length; i++) {
			String fileName = files[i];
			String className = null;
			// we are only interested in .class files
			if (fileName.endsWith(".class")) {
				// removes the .class extension
				className = pkgname + '.' + fileName.substring(0, fileName.length() - 6);
			}
			log.debug("FileName '" + fileName + "'  =>  class '" + className + "'");
			if (className != null) {
				classes.add(loadClass(className, true, cl));
			}
			File subdir = new File(directory, fileName);
			if (subdir.isDirectory()) {
				processDirectory(subdir, pkgname + '.' + fileName, classes, cl);
			}
		}
		return classes;
	}

	public static List<Class<?>> processJarfile(URL resource, String pkgname, ClassLoader cl) throws IOException {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		String relPath = pkgname.replace('.', '/');
		String resPath = resource.getPath();
		String jarPath = resPath.replaceFirst("[.]jar[!].*", ".jar").replaceFirst("file:", "");
		log.info("Reading JAR file: '" + jarPath + "'");
		JarFile jarFile;
		try {
			jarFile = new JarFile(jarPath);         
		} catch (IOException e) {
			throw new RuntimeException("Unexpected IOException reading JAR File '" + jarPath + "'", e);
		}
		Enumeration<JarEntry> entries = jarFile.entries();
		while(entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			String entryName = entry.getName();
			String className = null;
			if(entryName.endsWith(".class") && entryName.startsWith(relPath) && entryName.length() > (relPath.length() + "/".length())) {
				className = entryName.replace('/', '.').replace('\\', '.').replace(".class", "");
			}
			//log.debug("JarEntry '" + entryName + "'  =>  class '" + className + "'");
			if (className != null) {
				classes.add(loadClass(className, true, cl));
			}
		}
		jarFile.close();
		return classes;
	}
	
	public static List<Class<?>> getClassesForPackage(Package pkg, ClassLoader cl) throws IOException {
		String pkgname = pkg.getName();
		String relPath = pkgname.replace('.', '/');
	
		// Get a File object for the package
		URL resource = ClassLoader.getSystemClassLoader().getResource(relPath);
		if (resource == null) {
			throw new RuntimeException("Unexpected problem: No resource for " + relPath);
		}
		log.info("Package: '" + pkgname + "' becomes Resource: '" + resource.toString() + "'");

		resource.getPath();
		if(resource.toString().startsWith("jar:")) {
			return processJarfile(resource, pkgname, cl);
		} else {
			return processDirectory(new File(resource.getPath()), pkgname, null, cl);
		}

	}
}
