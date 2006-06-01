/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.ant.func;
import ec.*;
import ec.app.ant.*;
import ec.gp.*;
import ec.util.*;

/* 
 * Left.java
 * 
 * Created: Wed Nov  3 18:26:37 1999
 * By: Sean Luke
 */

/**
 * @author Sean Luke
 * @version 1.0 
 */

public class Left extends GPNode implements EvalPrint
    {
    public String toString() { return "left"; }

    public void checkConstraints(final EvolutionState state,
                                 final int tree,
                                 final GPIndividual typicalIndividual,
                                 final Parameter individualBase)
        {
        super.checkConstraints(state,tree,typicalIndividual,individualBase);
        if (children.length!=0)
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
        Ant p = (Ant)problem;
        switch (p.orientation)
            {
            case Ant.O_UP:
                p.orientation = Ant.O_LEFT; 
                break;
            case Ant.O_LEFT:
                p.orientation = Ant.O_DOWN;
                break;
            case Ant.O_DOWN:
                p.orientation = Ant.O_RIGHT;
                break;
            case Ant.O_RIGHT:
                p.orientation = Ant.O_UP;
                break;
            default:  // whoa!
                state.output.fatal("Whoa, somehow I got a bad orientation! (" + p.orientation + ")");
                break;
            }
        p.moves++;
        }


    public void evalPrint(final EvolutionState state,
                          final int thread,
                          final GPData input,
                          final ADFStack stack,
                          final GPIndividual individual,
                          final Problem problem,
                          final int[][] map2)
        {
        eval(state,thread,input,stack,individual,problem);
        }
    }



