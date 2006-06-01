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
 * Frog.java
 * 
 * Created: Wed Nov  3 18:26:37 1999
 * By: Sean Luke
 */

/**
 * @author Sean Luke
 * @version 1.0 
 */

public class Frog extends GPNode
    {
    public String toString() { return "frog"; }

    public void checkConstraints(final EvolutionState state,
                                 final int tree,
                                 final GPIndividual typicalIndividual,
                                 final Parameter individualBase)
        {
        super.checkConstraints(state,tree,typicalIndividual,individualBase);
        if (children.length!=1)
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
        
        // we follow the Koza-II example, not the lil-gp example.
        // that is, we "assume" that in our orientation the X axis
        // is moving out away from us, and the Y axis is moving
        // out to the left.  In lil-gp, the assumption is that the Y axis
        // axis is moving out away from us, and the X axis is moving out
        // to the right.

        switch (p.orientation)
            {
            case Lawnmower.O_UP:
                // counter-clockwise rotation
                p.posx -= d.y;
                p.posy += d.x;
                break;
            case Lawnmower.O_LEFT:
                // flipped orientation
                p.posx -= d.x;
                p.posy -= d.y;
                break;
            case Lawnmower.O_DOWN:
                // clockwise rotation
                p.posx += d.y;
                p.posy -= d.x;
                break;
            case Lawnmower.O_RIGHT:
                // proper orientation
                p.posx += d.x;
                p.posy += d.y;
                break;
            default:  // whoa!
                state.output.fatal("Whoa, somehow I got a bad orientation! (" + p.orientation + ")");
                break;
            }

        // shift back into the lawn frame.
        // because Java's % on negative numbers preserves the
        // minus sign, we have to mod twice with an addition.
        // C has to do this too.
        p.posx = ((p.posx % p.maxx) + p.maxx ) % p.maxx ; 
        p.posy = ((p.posy % p.maxy) + p.maxy ) % p.maxy ;

        p.moves++;
        if (p.map[p.posx][p.posy]==Lawnmower.UNMOWED)
            {
            p.sum++;
            p.map[p.posx][p.posy] = p.moves;
            }

        // return [x,y] -- to do this, simply don't modify input
        }
    }



