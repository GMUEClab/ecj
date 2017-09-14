/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.neat;

import ec.util.*;
import ec.*;

/**
 * NEATDefaults is the basic defaults class for the neat package.
 *
 * @author Ermo Wei and David Freelan
 */

public final class NEATDefaults implements DefaultsForm
    {
    public static final String P_NEAT = "neat";

    public static final Parameter base()
        {
        return new Parameter(P_NEAT);
        }
    }
