/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.spatial;
import ec.*;
import ec.util.*;

/* 
 * SpatialDefaults.java
 * 
 * Created: Thu Feb  2 13:39:52 EST 2006
 * By: Sean Luke
 */

/**
 * @author Sean Luke
 * @version 1.0 
 */

public final class SpatialDefaults implements DefaultsForm
    {
    public static final String P_SPATIAL = "spatial";

    /** Returns the default base. */
    public static final Parameter base()
        {
        return new Parameter(P_SPATIAL);
        }    
    }
