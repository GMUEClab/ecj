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
 * Move.java
 * 
 * Created: Wed Nov  3 18:26:37 1999
 * By: Sean Luke
 */

/**
 * @author Sean Luke
 * @version 1.0 
 */

public class Move extends GPNode implements EvalPrint
    {
    public String toString() { return "move"; }

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
                p.posy--;
                if (p.posy<0) p.posy = p.maxy-1;
                break;
            case Ant.O_LEFT:
                p.posx--;
                if (p.posx<0) p.posx = p.maxx-1;
                break;
            case Ant.O_DOWN:
                p.posy++;
                if (p.posy>=p.maxy) p.posy=0;
                break;
            case Ant.O_RIGHT:
                p.posx++;
                if (p.posx>=p.maxx) p.posx=0;
                break;
            default:  // whoa!
                state.output.fatal("Whoa, somehow I got a bad orientation! (" + p.orientation + ")");
                break;
            }

        p.moves++;
        if (p.map[p.posx][p.posy]==Ant.FOOD && p.moves < p.maxMoves )
            {
            p.sum++;
            p.map[p.posx][p.posy]=Ant.ATE;
            }
        }

    /** Just like eval, but it retraces the map and prints out info */
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
                p.posy--;
                if (p.posy<0) p.posy = p.maxy-1;
                break;
            case Ant.O_LEFT:
                p.posx--;
                if (p.posx<0) p.posx = p.maxx-1;
                break;
            case Ant.O_DOWN:
                p.posy++;
                if (p.posy>=p.maxy) p.posy=0;
                break;
            case Ant.O_RIGHT:
                p.posx++;
                if (p.posx>=p.maxx) p.posx=0;
                break;
            default:  // whoa!
                state.output.fatal("Whoa, somehow I got a bad orientation! (" + p.orientation + ")");
                break;
            }

        p.moves++;
        if (p.map[p.posx][p.posy]==Ant.FOOD && p.moves < p.maxMoves)
            {
            p.sum++;
            p.map[p.posx][p.posy]=Ant.ATE;
            }

        if (p.moves<p.maxMoves)
            {
            if (++p.pmod > 122 /* ascii z */) p.pmod=97; /* ascii a */
            map2[p.posx][p.posy]=p.pmod;
            }
        }
    }



