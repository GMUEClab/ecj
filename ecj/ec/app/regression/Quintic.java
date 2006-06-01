/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.regression;

/* 
 * Quintic.java
 * 
 * Created: Fri Nov 30 21:38:13 EST 2001
 * By: Sean Luke
 */

/**
 * Quintic implements a Symbolic Regression problem.
 *
 * <p>The equation to be regressed is y = x^5 - 2x^3 + x, {x in [-1,1]}
 * <p>This equation was introduced in J. R. Koza, GP II, 1994.
 *
 */
public class Quintic extends Regression
    {
    public double func(double x)
        { return x*x*x*x*x - 2.0*x*x*x + x; }
    }
