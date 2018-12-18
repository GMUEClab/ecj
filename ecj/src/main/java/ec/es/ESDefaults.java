/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.es;
import ec.util.Parameter;
import ec.*;

/* 
 * ESDefaults.java
 * 
 * Created: Thu Sep  7 19:08:19 2000
 * By: Sean Luke
 */

/**
 * @author Sean Luke
 * @version 1.0 
 */

public final class ESDefaults implements DefaultsForm 
    {
    public static final String P_ES = "es";

    /** Returns the default base. */
    public static final Parameter base()
        {
        return new Parameter(P_ES);
        }
    }
