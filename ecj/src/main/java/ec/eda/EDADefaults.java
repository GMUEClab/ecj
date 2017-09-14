/*
  Copyright 2015 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.eda;

import ec.util.*;
import ec.*;

/* 
 * EDADefaults.java
 * 
 * Created: Wed Jul  8 12:28:04 EDT 2015
 * By: Sean Luke
 */

/**
 * EDADefaults is the basic defaults class for the eda package.
 *
 * @author Sean Luke
 * @version 1.0 
 */

public final class EDADefaults implements DefaultsForm
    {
    public static final String P_EDA = "eda";

    /** Returns the default base. */
    public static final Parameter base()
        {
        return new Parameter(P_EDA);
        }    
    }
