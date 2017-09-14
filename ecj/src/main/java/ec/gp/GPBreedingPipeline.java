/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp;
import ec.*;

/* 
 * GPBreedingPipeline.java
 * 
 * Created: Tue Sep 14 19:38:09 1999
 * By: Sean Luke
 */

/**
 * A GPBreedingPipeline is a BreedingPipeline which produces only
 * members of some subclass of GPSpecies.   This is just a convenience
 * superclass for many of the breeding pipelines here; you don't have
 * to be a GPBreedingPipeline in order to breed GPSpecies or anything. 
 *
 * @author Sean Luke
 * @version 1.0 
 */

public abstract class GPBreedingPipeline extends BreedingPipeline 
    {
    /** Standard parameter for node-selectors associated with a GPBreedingPipeline */
    public static final String P_NODESELECTOR = "ns";

    /** Standard parameter for tree fixing */
    public static final String P_TREE = "tree";

    /** Standard value for an unfixed tree */
    public static final int TREE_UNFIXED = -1;


    /** Returns true if <i>s</i> is a GPSpecies. */
    public boolean produces(final EvolutionState state,
        final Population newpop,
        final int subpopulation,
        final int thread)
        {
        if (!super.produces(state,newpop,subpopulation,thread)) return false;

        // we produce individuals which are owned by subclasses of GPSpecies
        if (newpop.subpops[subpopulation].species instanceof GPSpecies)
            return true;
        return false;
        }

    }
