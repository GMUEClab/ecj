/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.util;

/* 
 * RandomChoiceChooserD.java
 * 
 * Created: Sat Jan 13 22:40:09 EST 2001
 * By: Sean Luke
 */

/**
 * Used by RandomChoice to pick objects by probability from a distribution.
 *
 * @author Sean Luke
 * @version 1.0 
 */

public interface RandomChoiceChooserD
    {
    /** Returns obj's probability */
    public double getProbability(final Object obj);
    /** Sets obj's probability */
    public void setProbability(final Object obj, final double prob);
    }
