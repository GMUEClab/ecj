/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.rule;
import ec.util.Parameter;
import ec.*;

/* 
 * RuleDefaults.java
 * 
 * Created: Tue Feb 20 1258:00 2001
 * By: Liviu Panait
 */

/**
 * A static class that returns the base for "default values" which rule-style
 * operators use, rather than making the user specify them all on a per-
 * species basis.
 *
 * @author Liviu Panait
 * @version 1.0 
 */

public final class RuleDefaults implements DefaultsForm
    {
    public static final String P_RULE = "rule";

    /** Returns the default base. */
    public static final Parameter base()
        {
        return new Parameter(P_RULE);
        }
    }
