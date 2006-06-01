/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


/*
 * Created on Apr 9, 2005 8:28:11 AM
 * 
 * By: spaus
 */
package ec.util;

import java.util.EventListener;

/**
 * @author spaus
 */
public interface ParameterDatabaseListener
    extends EventListener {
    /**
     * @param evt
     */
    public void parameterSet(ParameterDatabaseEvent evt);
    
    /**
     * @param evt
     */
    public void parameterAccessed(ParameterDatabaseEvent evt);
    }
