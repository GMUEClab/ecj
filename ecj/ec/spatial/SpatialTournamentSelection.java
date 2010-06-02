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
 * When selecting an individual, the SpatialTournamentSelection method selects one from the neighbors
 * of a specific individual (as indicated by its index in the subpopulation).
 *
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base.</i><tt>size</tt><br>
 <font size=-1>int &gt;= 1 <b>or</b> 1.0 &lt; float &lt; 2.0</font></td>
 <td valign=top>(the tournament size)</td></tr>

 <tr><td valign=top><i>base.</i><tt>pick-worst</tt><br>
 <font size=-1> bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 <td valign=top>(should we pick the <i>worst</i> individual in the tournament instead of the <i>best</i>?)</td></tr>

 </table>

 Further parameters may be found in ec.select.TournamentSelection.

 <p><b>Default Base</b><br>
 spatial.tournament
 *
 * @author Liviu Panait
 * @version 1.0 
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

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
                
        Parameter defaultBase = defaultBase();

        neighborhoodSize = state.parameters.getInt( base.push(P_N_SIZE), defaultBase.push(P_N_SIZE), 1 );
        if( neighborhoodSize < 1 )
            state.output.fatal( "Parameter not found, or its value is < 1.", base.push(P_N_SIZE), defaultBase.push(P_N_SIZE));

        indCompetes = state.parameters.getBoolean(base.push(P_IND_COMPETES),defaultBase.push(P_IND_COMPETES),false);
        }


    public Parameter defaultBase()
        {
        return SpatialDefaults.base().push(P_TOURNAMENT);
        }

    public int getRandomIndividual(int number, int subpopulation, EvolutionState state, int thread)
        {
        try
            {
            Space space = (Space)(state.population.subpops[subpopulation]);
            int index = space.getIndex(thread);
            if (number==0 && indCompetes) return index;                                             // we're the first one
            else return space.getIndexRandomNeighbor(state,thread,index);
            }
        catch( Exception e )
            {
            state.output.fatal( "Subpopulation "+subpopulation+" is not a spatially-embedded subpopulation.\n"+e );
            }
        throw new InternalError("This should not be reachable");
        }


/*
// I hard-code both produce(...) methods for efficiency's sake
public int produce(final int subpopulation,
final EvolutionState state,
final int thread)
{
Space space = null;
try
{
space = (Space)(state.population.subpops[subpopulation]);
}
catch( Exception e )
{
state.output.fatal( "Subpopulation "+subpopulation+" is not a spatially-embedded subpopulation.\n"+e );
}

// pick size random individuals, then pick the best.
Individual[] oldinds = state.population.subpops[subpopulation].individuals;

int index = space.getIndex(thread);
int randomNeighbor = space.getIndexRandomNeighbor(state,thread,index);
int i = indCompetes ? index : randomNeighbor;
int bad = i;
        
for (int x=1;x<size;x++)
{
int j = space.getIndexRandomNeighbor(state,thread,index);
if (pickWorst)
{ if (!(oldinds[j].fitness.betterThan(oldinds[i].fitness))) { bad = i; i = j; } else bad = j; }
else
{ if (oldinds[j].fitness.betterThan(oldinds[i].fitness)) { bad = i; i = j;} else bad = j; }
}
            
if (probabilityOfSelection != 1.0 && !state.random[thread].nextBoolean(probabilityOfSelection))
i = bad;
return i;
}
*/
    }
