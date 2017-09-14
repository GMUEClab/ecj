/*
  Copyright 2015 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.eda.dovs;

import ec.util.*;
import ec.*;

/**
 * DOVSDefaults is the basic defaults class for the dovs package.
 *
 * @author Ermo Wei and David Freelan
 */

public final class DOVSDefaults implements DefaultsForm
    {
    public static final String P_DOVS = "dovs";

    /** Returns the default base. */
    public static final Parameter base()
        {
        return new Parameter(P_DOVS);
        }
    }
