/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.simple;
import ec.util.Parameter;
import ec.*;

/* 
 * SimpleDefaults.java
 * 
 * Created: Thu Jan 20 17:19:12 2000
 * By: Sean Luke
 */

/**
 * @author Sean Luke
 * @version 1.0 
 */

public final class SimpleDefaults implements DefaultsForm 
    {
    public static final String P_SIMPLE = "simple";

    /** Returns the default base. */
    public static final Parameter base()
        {
        return new Parameter(P_SIMPLE);
        }
    }
