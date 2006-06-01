/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp.breed;
import ec.gp.GPDefaults;
import ec.util.Parameter;
import ec.*;

/* 
 * GPBreedDefaults.java
 * 
 * Created: Tue Oct 12 17:44:47 1999
 * By: Sean Luke
 */

/**
 * A static class that returns the base for "default values" which various
 * GP breeding
 * operators use, rather than making the user specify them all on a per-
 * species basis.
 *
 * @author Sean Luke
 * @version 1.0 
 */

public final class GPBreedDefaults implements DefaultsForm
    {
    public static final String P_BREED = "breed";

    /** Returns the default base, which is built off of the GPDefaults base. */
    public static final Parameter base()
        {
        return GPDefaults.base().push(P_BREED);
        }
    }
