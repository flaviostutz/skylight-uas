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
// $Source: /cvs/distapps/openmap/com/bbn/openmap/io/Closable.java,v $
// $RCSfile: Closable.java,v $
// $Revision: 1.2 $
// $Date: 2002/04/02 19:05:55 $
// $Author: dietrick $
// 
// **********************************************************************


package br.skylight.commons.infra.dted;

/** 
 * Objects that implement this interface can be registered with
 * BinaryFile to have associated file resources closed when file limits
 * are hit.  
 */
public interface Closeable {

    /**
     * close/reclaim associated resources.
     *
     * @param done <code>true</code> indicates that this is a permanent
     * closure.  <code>false</code> indicates that the object may be used
     * again later, as this is only an attempt to temporarily reclaim resources
     * @return <code>true</code> indicates the object is still usable.
     * <code>false</code> indicates that the object is now unusable, and
     * any references to it should be released so the garbage collector
     * can do its job. 
     */
    public boolean close(boolean done);
}
