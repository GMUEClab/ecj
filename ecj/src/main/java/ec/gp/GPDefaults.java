/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp;
import ec.util.Parameter;
import ec.*;

/* 
 * GPDefaults.java
 * 
 * Created: Tue Oct 12 17:44:47 1999
 * By: Sean Luke
 */

/**
 * A static class that returns the base for "default values" which GP-style
 * operators use, rather than making the user specify them all on a per-
 * species basis.
 *
 * @author Sean Luke
 * @version 1.0 
 */

public final class GPDefaults implements DefaultsForm
    {
    public static final String P_GP = "gp";

    /** Returns the default base. */
    public static final Parameter base()
        {
        return new Parameter(P_GP);
        }
    }
