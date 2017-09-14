/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.vector;

import ec.*;
import ec.util.*;

/* 
 * VectorDefaults.java
 * 
 * Created: Thu Mar 22 13:22:30 2001
 * By: Liviu Panait
 */

/**
 * Vector defaults is the basic defaults class for the Vector package.
 *
 * @author Liviu Panait
 * @version 1.0 
 */

public final class VectorDefaults implements DefaultsForm
    {
    public static final String P_VECTOR = "vector";

    /** Returns the default base. */
    public static final Parameter base()
        {
        return new Parameter(P_VECTOR);
        }    
    }
