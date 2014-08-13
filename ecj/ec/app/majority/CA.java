/*
  Copyright 2013 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.app.majority;

import ec.util.*;
import ec.*;

/**
   CA.java
        
   Implements a 1-dimensional toroidal CA for purposes of doing the binary majority classification problem.
   You need to supply the automaton size and the rule neighborhood size at constructor time.
   The CA itself consists of an array of integers, each 0 or 1.   You can clear this array, set it to
   preset values, or randomize it.
   You provide the rule as an array of ints, all 0 or 1 as well.  The rules are specified in the following
   order.  Let's say that you have a neighborhood of 3, consiting of cells LCR, where L is the cell "left"
   of the target cell (left is less than, right is greater than).  Then the order of the rules are for the
   neighborhoods 0: 000, 1: 001, 2: 010, 3: 011, 4: 100, 5: 101, 6: 110, 7: 111.   In other words, the
   neighborhood is interpreted as a binary number and that's the index into the rule.
*/



public class CA implements java.io.Serializable
    {
    private static final long serialVersionUID = 1;
    
    public CA(int width, int neighborhood)
        {
        ca = new int[width];
        ca2 = new int[width];
        this.neighborhood = neighborhood;
        rule = new int[1 << neighborhood];
        }

    int[] ca;
    int[] ca2;
    int[] rule;
    int neighborhood;

    public int[] getVals() { return ca; }
    public int[] getRule() { return rule; }

    public void setRule(int[] r)
        {
        if (r.length != rule.length)
            throw new RuntimeException("Rule length invalid given neighborhood size.");
        rule = r;
        } 

    public void setVals(int[] vals) 
        {
        if (vals.length != ca.length)
            throw new RuntimeException("CA length invalid given prespecified size.");
        ca = (int[])(vals.clone());
        }

    public void clear(boolean toOnes) 
        { 
        if (toOnes)
            for(int i = 0; i < ca.length; i++) 
                ca[i] = 1;
        else
            for(int i = 0; i < ca.length; i++) 
                ca[i] = 0; 
        }
        
    public final boolean converged()
        {
        int t = ca[0];
        //int len = ca.length;
        for(int i = 1; i < ca.length; i++)
            if (ca[i] != t) return false;
        return true;
        }
                
    public void randomize(EvolutionState state, int thread)
        {
        MersenneTwisterFast random = state.random[thread];
        for(int i = 0; i < ca.length; i++) 
            ca[i] = random.nextBoolean() ? 0 : 1;
        }
        
    public void step(int steps, boolean stopWhenConverged)
        {
        final int len = ca.length;
        final int halfhood = neighborhood / 2;                  // this is the size of one side of the neighborhood
        final int mask = (1 << neighborhood) - 1;               // this masks out the state to the neighborhod length
        
        for(int q = 0; q < steps; q++)
            {
            int state = 0;                                                                  // the current neighborhood state.  Rotates through.
                        
            // initialize state to right toroidal values
            for(int i = len - halfhood; i < len; i++)
                state = (state << 1 ) | ca[i];
            // initialize state to left values
            for(int i = 0; i < halfhood + 1; i++)
                state = (state << 1 ) | ca[i];
        
            // scan with current state
            for(int i = 0; i < (len - halfhood) - 1; i++)
                {
                ca2[i] = rule[state];
                state = ((state << 1) | ca[i + halfhood + 1]) & mask;
                }
                        
            // continue to scan toroidally
            int j = 0;
            for(int i = len - halfhood - 1; i < len; i++)
                {
                ca2[i] = rule[state];
                state = ((state << 1) | ca[j++]) & mask;
                }

            // swap
            int[] tmp = ca;
            ca = ca2;
            ca2 = tmp;      
                        
            // did we converge?
            if (stopWhenConverged && converged())
                {
                //System.err.println("converged at " + q);
                return; 
                }
            }
        }
    }
