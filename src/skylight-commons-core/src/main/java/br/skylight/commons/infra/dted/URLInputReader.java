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
// $Source: /cvs/distapps/openmap/com/bbn/openmap/io/URLInputReader.java,v $
// $RCSfile: URLInputReader.java,v $
// $Revision: 1.3 $
// $Date: 2002/04/02 19:05:57 $
// $Author: dietrick $
// 
// **********************************************************************

package br.skylight.commons.infra.dted;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

/**
 * An InputReader to handle files at a URL.
 */
public class URLInputReader extends StreamInputReader {

	private static final Logger logger = Logger.getLogger(URLInputReader.class.getName());

	/** Where to go to hook up with a resource. */
	protected URL inputURL = null;

	/**
	 * Construct a URLInputReader from a URL.
	 */
	public URLInputReader(java.net.URL url) throws IOException {
		logger.fine("URLInputReader created from URL ");
		inputURL = url;
		reopen();
		name = url.getProtocol() + "://" + url.getHost() + url.getFile();
	}

	/**
	 * Reset the InputStream to the beginning, by closing the current connection
	 * and reopening it.
	 */
	public void reopen() throws IOException {
		super.reopen();
		URLConnection urlc = inputURL.openConnection();
		inputStream = urlc.getInputStream();
	}
}
