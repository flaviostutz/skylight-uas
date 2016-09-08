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
// $Source: /cvs/distapps/openmap/com/bbn/openmap/io/InvalidCharException.java,v $
// $RCSfile: InvalidCharException.java,v $
// $Revision: 1.2 $
// $Date: 2002/04/02 19:05:56 $
// $Author: dietrick $
// 
// **********************************************************************


package br.skylight.commons.infra.dted;

/**
 * An invalid character occured on in input stream.
 */
public class InvalidCharException extends FormatException {

    /** the invalid character that we found */
    final public char c;

    /**
     * Construct an object with no detail message
     * @param val the character encountered
     */
    public InvalidCharException(char val) {
	super();
	c = val;
    }

    /**
     * Construct an object with a detail message
     * @param s the detail message
     * @param val the character encountered
     */
    public InvalidCharException(String s, char val) {
	super(s);
	c = val;
    }
}
