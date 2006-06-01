/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.lawnmower.func;
import ec.*;
import ec.app.lawnmower.*;
import ec.gp.*;
import ec.util.*;

/* 
 * Mow.java
 * 
 * Created: Wed Nov  3 18:26:37 1999
 * By: Sean Luke
 */

/**
 * @author Sean Luke
 * @version 1.0 
 */

public class Mow extends GPNode
    {
    public String toString() { return "mow"; }

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
        Lawnmower p = (Lawnmower)problem;
        LawnmowerData d = (LawnmowerData)input;

        switch (p.orientation)
            {
            case Lawnmower.O_UP:
                p.posy--;
                if (p.posy<0) p.posy = p.maxy-1;
                break;
            case Lawnmower.O_LEFT:
                p.posx--;
                if (p.posx<0) p.posx = p.maxx-1;
                break;
            case Lawnmower.O_DOWN:
                p.posy++;
                if (p.posy>=p.maxy) p.posy=0;
                break;
            case Lawnmower.O_RIGHT:
                p.posx++;
                if (p.posx>=p.maxx) p.posx=0;
                break;
            default:  // whoa!
                state.output.fatal("Whoa, somehow I got a bad orientation! (" + p.orientation + ")");
                break;
            }

        p.moves++;
        if (p.map[p.posx][p.posy]==Lawnmower.UNMOWED)
            {
            p.sum++;
            p.map[p.posx][p.posy] = p.moves;
            }

        // return [0,0]
        d.x = 0;
        d.y = 0;
        }
    }



