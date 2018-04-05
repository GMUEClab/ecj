/*
  Copyright 2017 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.eda.cmaes;

import ec.util.*;
import ec.*;

/* 
 * CMAESDefaults.java
 * 
 * Created: Sun Jul  9 16:26:01 CEST 2017
 * By: Sean Luke
 */

/**
 * CMAESDefaults is the basic defaults class for the cmaes package.
 *
 * @author Sean Luke
 * @version 1.0 
 */

public final class CMAESDefaults implements DefaultsForm
    {
    public static final String P_CMAES = "cmaes";

    /** Returns the default base. */
    public static final Parameter base()
        {
        return new Parameter(P_CMAES);
        }    
    }
