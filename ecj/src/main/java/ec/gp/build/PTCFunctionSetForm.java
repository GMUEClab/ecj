/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp.build;

/* 
 * PTCFunctionSetForm.java
 * 
 * Created: Mon Oct 16 17:21:44 2000
 * By: Sean Luke
 */

/**
 * PTCFunctionSetForm defines the methods that the PTC1 and PTC2 tree-creation
 * algorithms require of function sets.  Your GPFunctionSet must adhere to
 * this form in order to be used by these algorithms; the PTCFunctionSet
 * class is provided to simplify matters for you (it's a direct subclass of
 * GPFunctionSet which adheres to this form).
 *
 * @author Sean Luke
 * @version 1.0 
 */

public interface PTCFunctionSetForm  
    {
    /** Returns an organized distribution (see ec.util.RandomChoice) of likelihoods
        that various terminals in the function set will be chosen over other terminals
        with the same return type.  The ordering of the array is the same as
        the terminals[type][...] array in GPFunctionSet.  */
    public double[] terminalProbabilities(final int type);

    /** Returns an organized distribution (see ec.util.RandomChoice) of likelihoods
        that various nonterminals in the function set will be chosen over other nonterminals
        with the same return type. The ordering of the array is the same as
        the nonterminals[type][...] array in GPFunctionSet. */
    public double[] nonterminalProbabilities(final int type);
    
    /** Returns an array (by return type) of the probability that PTC1 must pick a
        nonterminal over a terminal in order to guarantee the expectedTreeSize.
        Only used by PTC1, not by PTC2. */
    public double[] nonterminalSelectionProbabilities(final int expectedTreeSize);
    }
