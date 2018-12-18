/*
  Copyright 2010 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp.ge;
import ec.util.Parameter;
import ec.*;

/*
 * GEDefaults.java
 *
 * Created: Fri May 28 14:29:41 EDT 2010
 * By: Sean Luke
 */

/**
 * A static class that returns the base for "default values" which GE-style
 * operators use, rather than making the user specify them all on a per-
 * species basis.
 *
 * @author Sean Luke
 * @version 1.0
 */

public final class GEDefaults implements DefaultsForm
    {
    public static final String P_GE = "ge";

    /** Returns the default base. */
    public static final Parameter base()
        {
        return new Parameter(P_GE);
        }
    }
