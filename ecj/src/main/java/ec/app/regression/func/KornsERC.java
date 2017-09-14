/*
  Copyright 2012 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.regression.func;
import ec.*;
import ec.app.regression.*;
import ec.gp.*;
import ec.util.*;
import java.io.*;


/* 
 * KornsERC.java
 * 
 * Created: Wed Nov  3 18:26:37 1999
 * By: Sean Luke

 <p>This ERC appears in the Korns function set.  It is defined as a random finite 65-bit IEEE double.  We achieve this by drawing a random long, then converting it to a double, then rejecting results which are either NaN or infinite.
 
 <p>M. F. Korns. Accuracy in Symbolic Regression. In <i>Proc. GPTP.</i> 2011.
*/

/**
 * @author Sean Luke
 * @version 1.0 
 */

public class KornsERC extends RegERC
    {
    public String name() { return "KornsERC"; }

    public void resetNode(final EvolutionState state, final int thread)
        { 
        do
            {
            value = Double.longBitsToDouble(state.random[thread].nextLong()); 
            }
        while (Double.isNaN(value) || Double.isInfinite(value));
        }
    }



