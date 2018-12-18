/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.spatial;

import ec.*;
import ec.util.*;
import ec.select.TournamentSelection;

/* 
 * SpatialTournamentSelection.java
 * 
 * By: Liviu Panait
 */

/**
 * A slight modification of the tournament selection procedure for use with spatially-embedded EAs.
 *
 * When selecting an individual, the SpatialTournamentSelection is told a specific individual.
 * It then picks N individuals at random which are within a certain distance (the <i>neighborhood size</i>) of that individual.  These
 * individuals then enter a tournament a-la standard Tournament Selection.  
 *
 * <p>The method of picking individuals is either <tt>uniform</tt> (picking individuals using the Space interface's
 * getRandomIndividual(...)) or <tt>random-walk</tt> (wandering <i>distance</i> steps at random).  You can also
 * stipulate whether the original individual must be in the tournament.
 *
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base.</i><tt>neighborhood-size</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(the neighborhood size)</td></tr>

 <tr><td valign=top><i>base.</i><tt>ind-competes</tt><br>
 <font size=-1> bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 <td valign=top>(Do we include the base individual in the tournament?)</td></tr>

 <tr><td valign=top><i>base.</i><tt>type</tt><br>
 <font size=-1>String: uniform (default) or random-walk</font></td>
 <td valign=top>Method for selecting individuals in neighborhood</td></tr>

 </table>

 Further parameters may be found in ec.select.TournamentSelection.

 <p><b>Default Base</b><br>
 spatial.tournament
 *
 * @author Liviu Panait and Sean Luke
 * @version 2.0
 */
public class SpatialTournamentSelection extends TournamentSelection
    {
    /**
       The size of the neighborhood from where parents are selected.  Small neighborhood sizes
       enforce a local selection pressure, while larger values for this parameters allow further-away
       individuals to compete for breeding as well.
    */
    public static final String P_N_SIZE = "neighborhood-size";
    int neighborhoodSize;

    /**
       Some models assume an individual is always selected to compete for breeding a child that would
       take its location in space.  Other models don't make this assumption.  This parameter allows one
       to specify whether an individual will be selected to compete with others for breeding a child that
       will take its location in space.  If the parameter value is not specified, it is assumed to be false
       by default.
    */
    public static final String P_IND_COMPETES = "ind-competes";
    boolean indCompetes;


    /**
       Selection procedure.
    */
    public static final String P_TYPE = "type";
    public static final String V_UNIFORM = "uniform";
    public static final String V_RANDOM_WALK = "random-walk";
    public static final int TYPE_UNIFORM = 0;
    public static final int TYPE_RANDOM_WALK = 1;
    int type;

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
                
        Parameter defaultBase = defaultBase();

        neighborhoodSize = state.parameters.getInt( base.push(P_N_SIZE), defaultBase.push(P_N_SIZE), 1 );
        if( neighborhoodSize < 1 )
            state.output.fatal( "Parameter not found, or its value is < 1.", base.push(P_N_SIZE), defaultBase.push(P_N_SIZE));

        if (!state.parameters.exists(base.push(P_TYPE), defaultBase.push(P_TYPE)) ||
            state.parameters.getString( base.push(P_TYPE), defaultBase.push(P_TYPE)).equals(V_UNIFORM))
            type = TYPE_UNIFORM;
        else if (state.parameters.getString( base.push(P_TYPE), defaultBase.push(P_TYPE)).equals(V_RANDOM_WALK))
            type = TYPE_RANDOM_WALK;
        else state.output.fatal("Invalid parameter, must be either " + V_RANDOM_WALK + " or " + V_UNIFORM + ".",
            base.push(P_TYPE), defaultBase.push(P_TYPE));
                
        indCompetes = state.parameters.getBoolean(base.push(P_IND_COMPETES), defaultBase.push(P_IND_COMPETES), false);
        }


    public Parameter defaultBase()
        {
        return SpatialDefaults.base().push(P_TOURNAMENT);
        }

    public int getRandomIndividual(int number, int subpopulation, EvolutionState state, int thread)
        {
        Subpopulation subpop = state.population.subpops.get(subpopulation);
        if (!(subpop instanceof Space))
            state.output.fatal( "Subpopulation "+subpopulation+" is not a spatially-embedded subpopulation.\n");
        Space space = (Space)(state.population.subpops.get(subpopulation));
        int index = space.getIndex(thread);
                
        if (number==0 && indCompetes)           // Should we just return the individual?
            return index;
        else if (type == TYPE_UNIFORM)          // Should we pick randomly in the space up to the given distance?
            return space.getIndexRandomNeighbor(state,thread,neighborhoodSize);
        else // if (type == TYPE_RANDOM_WALK)  // Should we do a random walk?
            {
            int oldIndex = index;
            for(int x=0; x < neighborhoodSize; x++)
                space.setIndex(thread, space.getIndexRandomNeighbor(state, thread, 1));
            int val = space.getIndex(thread);
            space.setIndex(thread,oldIndex);  // just in case we weren't supposed to mess around with that
            return val;
            }
        }
    }
