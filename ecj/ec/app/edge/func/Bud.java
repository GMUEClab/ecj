/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.edge.func;
import ec.*;
import ec.app.edge.*;
import ec.gp.*;
import ec.util.*;

/* 
 * Bud.java
 * 
 * Created: Wed Nov  3 18:26:37 1999
 * By: Sean Luke
 */

/**
 * @author Sean Luke
 * @version 1.0 
 */

public class Bud extends GPNode
    {
    public String toString() { return "bud"; }

    public void checkConstraints(final EvolutionState state,
                                 final int tree,
                                 final GPIndividual typicalIndividual,
                                 final Parameter individualBase)
        {
        super.checkConstraints(state,tree,typicalIndividual,individualBase);
        if (children.length!=2)
            state.output.error("Incorrect number of children for node " + 
                               toStringForError() + " at " +
                               individualBase);
        }

    public void eval(final EvolutionState state,
                     final int thread,
                     final GPData input,
                     final ADFStack stack,
                     final GPIndividual individual,
                     final Problem problem)
        {
        int edge = ((EdgeData)(input)).edge;
        Edge prob = (Edge)problem;

        if (prob.from.length==prob.numEdges)  // we're full, need to expand
            {
            int[] from_ = new int[prob.numEdges*2];
            int[] to_ = new int[prob.numEdges*2];
            int[] reading_ = new int[prob.numEdges*2];
            System.arraycopy(prob.from,0,from_,0,prob.from.length);
            System.arraycopy(prob.to,0,to_,0,prob.to.length);
            System.arraycopy(prob.reading,0,reading_,0,prob.reading.length);
            prob.from = from_;
            prob.to = to_;
            prob.reading = reading_;
            }

        if (prob.start.length==prob.numNodes)  // we're full, need to expand
            {
            boolean[] start_ = new boolean[prob.numNodes*2];
            boolean[] accept_ = new boolean[prob.numNodes*2];
            System.arraycopy(prob.start,0,start_,0,prob.start.length);
            System.arraycopy(prob.accept,0,accept_,0,prob.accept.length);
            prob.start = start_;
            prob.accept = accept_;
            }

        int newedge = prob.numEdges;
        prob.numEdges++;
        int newnode = prob.numNodes;
        prob.numNodes++;

        // set up new node
        prob.accept[newnode] = false;
        prob.start[newnode] = false;

        // set up new edge
        prob.from[newedge] = prob.to[edge];
        prob.to[newedge] = newnode;
        prob.reading[newedge] = prob.reading[edge];

        // pass the original edge down the left child

        children[0].eval(state,thread,input,stack,individual,problem);

        // reset input for right child
        ((EdgeData)(input)).edge = newedge;

        // pass the new edge down the right child
        
        children[1].eval(state,thread,input,stack,individual,problem);
        }
    }



