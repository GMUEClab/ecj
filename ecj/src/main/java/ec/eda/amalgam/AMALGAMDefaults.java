/*
  Copyright 2017 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.eda.amalgam;

import ec.util.*;
import ec.*;

public final class AMALGAMDefaults implements DefaultsForm
    {
    public static final String P_AMALGAM = "amalgam";

    /** Returns the default base. */
    public static final Parameter base()
        {
        return new Parameter(P_AMALGAM);
        }    
    }
