// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/com/bbn/openmap/io/JarInputReader.java,v $
// $RCSfile: JarInputReader.java,v $
// $Revision: 1.3 $
// $Date: 2002/04/02 19:05:57 $
// $Author: dietrick $
// 
// **********************************************************************

package br.skylight.commons.infra.dted;

import java.io.IOException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * An InputReader to handle entries in a Jar file.
 */
public class JarInputReader extends StreamInputReader {

	private static final Logger logger = Logger.getLogger(JarInputReader.class.getName());
	
	/** Where to go to hook up with a resource. */
	protected URL inputURL = null;

	protected JarFile jarFile = null;
	protected String jarFileName = null;
	protected String jarEntryName = null;

	/**
	 * Create a JarInputReader win the path to a jar file, and the entry name.
	 * The entry name should be a path to the entry from the internal root of
	 * the jar file.
	 */
	public JarInputReader(String jarFilePath, String jarEntryName) throws IOException {
		logger.fine("JarInputReader created for " + jarEntryName + " in " + jarFilePath);
		this.jarFileName = jarFilePath;
		this.jarEntryName = jarEntryName;
		reopen();
		name = jarFilePath + "!" + jarEntryName;
	}

	/**
	 * Reset the InputStream to the beginning, by closing the current connection
	 * and reopening it.
	 */
	public void reopen() throws IOException {
		super.reopen();

		logger.info("JarInputReader: reopening jarFile " + jarFileName);
		if (jarFile != null)
			jarFile.close();
		jarFile = null;

		jarFile = new JarFile(jarFileName);
		JarEntry entry = jarFile.getJarEntry(jarEntryName);
		inputStream = jarFile.getInputStream(entry);
		if (inputStream == null) {
			logger.warning("JarInputReader: Problem getting input stream for " + jarEntryName + " in " + jarFileName);
		}
	}

	/**
	 * Closes the underlying file
	 * 
	 * @exception IOException
	 *                Any IO errors encountered in accessing the file
	 */
	public void close() throws IOException {
		jarFile.close();
		super.close();
	}

}
