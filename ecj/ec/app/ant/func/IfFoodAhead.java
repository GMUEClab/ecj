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
 * IfFoodAhead.java
 * 
 * Created: Wed Nov  3 18:26:37 1999
 * By: Sean Luke
 */

/**
 * @author Sean Luke
 * @version 1.0 
 */

public class IfFoodAhead extends GPNode implements EvalPrint
    {
    public String toString() { return "if-food-ahead"; }

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
        Ant p = (Ant)problem;
        switch (p.orientation)
            {
            case Ant.O_UP:
                if (p.map[p.posx][(p.posy-1+p.maxy)%p.maxy]==Ant.FOOD)
                    children[0].eval(state,thread,input,stack,individual,problem);
                else children[1].eval(state,thread,input,stack,individual,problem);
                break;
            case Ant.O_LEFT:
                if (p.map[(p.posx-1+p.maxx)%p.maxx][p.posy]==Ant.FOOD)
                    children[0].eval(state,thread,input,stack,individual,problem);
                else children[1].eval(state,thread,input,stack,individual,problem);
                break;
            case Ant.O_DOWN:
                if (p.map[p.posx][(p.posy+1)%p.maxy]==Ant.FOOD)
                    children[0].eval(state,thread,input,stack,individual,problem);
                else children[1].eval(state,thread,input,stack,individual,problem);
                break;
            case Ant.O_RIGHT:
                if (p.map[(p.posx+1)%p.maxx][p.posy]==Ant.FOOD)
                    children[0].eval(state,thread,input,stack,individual,problem);
                else children[1].eval(state,thread,input,stack,individual,problem);
                break;
            default:  // whoa!
                state.output.fatal("Whoa, somehow I got a bad orientation! (" + p.orientation + ")");
                break;
            }
        }


    public void evalPrint(final EvolutionState state,
                          final int thread,
                          final GPData input,
                          final ADFStack stack,
                          final GPIndividual individual,
                          final Problem problem,
                          final int[][] map2)
        {
        Ant p = (Ant)problem;
        switch (p.orientation)
            {
            case Ant.O_UP:
                if (p.map[p.posx][(p.posy-1+p.maxy)%p.maxy]==Ant.FOOD)
                    ((EvalPrint)children[0]).evalPrint(state,thread,input,stack,individual,problem,map2);
                else ((EvalPrint)children[1]).evalPrint(state,thread,input,stack,individual,problem,map2);
                break;
            case Ant.O_LEFT:
                if (p.map[(p.posx-1+p.maxx)%p.maxx][p.posy]==Ant.FOOD)
                    ((EvalPrint)children[0]).evalPrint(state,thread,input,stack,individual,problem,map2);
                else ((EvalPrint)children[1]).evalPrint(state,thread,input,stack,individual,problem,map2);
                break;
            case Ant.O_DOWN:
                if (p.map[p.posx][(p.posy+1)%p.maxy]==Ant.FOOD)
                    ((EvalPrint)children[0]).evalPrint(state,thread,input,stack,individual,problem,map2);
                else ((EvalPrint)children[1]).evalPrint(state,thread,input,stack,individual,problem,map2);
                break;
            case Ant.O_RIGHT:
                if (p.map[(p.posx+1)%p.maxx][p.posy]==Ant.FOOD)
                    ((EvalPrint)children[0]).evalPrint(state,thread,input,stack,individual,problem,map2);
                else ((EvalPrint)children[1]).evalPrint(state,thread,input,stack,individual,problem,map2);
                break;
            default:  // whoa!
                state.output.fatal("Whoa, somehow I got a bad orientation! (" + p.orientation + ")");
                break;
            }
        }
    }



