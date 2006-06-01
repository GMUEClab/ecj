/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


/*
 * Created on Apr 9, 2005 9:03:58 AM
 * 
 * By: spaus
 */
package ec.util;

/**
 * @author spaus
 */
public class ParameterDatabaseAdapter
    implements ParameterDatabaseListener {

    /**
     * 
     */
    public ParameterDatabaseAdapter() {
        super();
    }

    /* (non-Javadoc)
     * @see ec.util.ParameterDatabaseListener#parameterSet(ec.util.ParameterDatabaseEvent)
     */
    public void parameterSet(ParameterDatabaseEvent evt) {
    }

    /* (non-Javadoc)
     * @see ec.util.ParameterDatabaseListener#parameterAccessed(ec.util.ParameterDatabaseEvent)
     */
    public void parameterAccessed(ParameterDatabaseEvent evt) {
    }

    }
