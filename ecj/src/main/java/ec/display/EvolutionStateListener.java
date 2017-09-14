/*
  Copyright 2006 by Sean Paus
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


/*
 * Created on Apr 14, 2005 9:08:47 PM
 * 
 * By: spaus
 */
package ec.display;

/**
 * @author spaus
 */
public interface EvolutionStateListener 
    {
    /**
     * @param evt
     */
    public void postEvolution(EvolutionStateEvent evt);
    }
