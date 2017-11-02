/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.singlestate;

import ec.*;
import ec.simple.*;
import ec.util.*;
import ec.vector.*;
import java.util.*;


/* 
 * SingleStateBreeder.java
 * 
 * Created: Tue May  9 15:08:53 CEST 2017
 * By: Ermo Wei and David Freelan
 */

/**
 * A very simple single-threaded breeder with optional elitism.
 *
 
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><tt><i>base</i>.elite.<i>i</i></tt><br>
 <font size=-1>int >= 0 (default=0)</font></td>
 <td valign=top>(will subpopulation <i>i</i> include ONE elite individual?)</td></tr>
 <tr><td valign=top><tt><i>base</i>.expanded-subpop-size.<i>i</i></tt><br>
 <font size=-1>int >= 1 (default=not resized)</font></td>
 <td valign=top>What is the expanded size of the subpopulation after generation 0?</td></tr>
 </table>


 <p><b>Default Base</b><br>
 ec.subpop

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>species</tt></td>
 <td>species (the subpopulations' species)</td></tr>

 *
 *
 * @author Ermo Wei and David Freelan
 * @version 1.0 
 */



public class SingleStateBreeder extends Breeder
    {
    public static final String P_ELITE = "elite";
    public static final String P_EXPANDED_SUBPOP_SIZE = "expanded-subpop-size";
    public static final int V_SUBPOP_NOT_RESIZED = -1;
    public boolean[] elite;
    public int[] expandedSubpopSize;
    public boolean[] stubsFilled;

    public Population breedPopulation(final EvolutionState state)
        {
        Population pop = state.population;
        for (int x = 0; x < pop.subpops.size(); x++)            
            breedSubpop(state, pop.subpops.get(x), x);
        return pop;
        }

    public void setup(final EvolutionState state, final Parameter base) 
        {
        Parameter p = new Parameter(Initializer.P_POP).push(Population.P_SIZE);
        int size = state.parameters.getInt(p,null,1);  // if size is wrong, we'll let Population complain about it -- for us, we'll just make 0-sized arrays and drop out.

        int defaultSubpop = state.parameters.getInt(new Parameter(Initializer.P_POP).push(Population.P_DEFAULT_SUBPOP), null, 0);
        
        elite = new boolean[size];
        
        for(int x=0;x<size;x++)
            {
            // get elites
            if (state.parameters.exists(base.push(P_ELITE).push(""+x),null))
                {
                elite[x] = state.parameters.getBoolean(base.push(P_ELITE).push(""+x),null,false);
                }
            else if (defaultSubpop >= 0 && state.parameters.exists(base.push(P_ELITE).push(""+defaultSubpop),null))
                {
                elite[x] = state.parameters.getBoolean(base.push(P_ELITE).push(""+defaultSubpop),null,false);
                }
            else  // no elitism
                {
                state.output.warning("Elites not defined for subpopulation " + x + ".  Assuming false.");
                elite[x] = false;
                }
            }

        expandedSubpopSize = new int[size];
        
        for(int x=0;x<size;x++)
            {
            // get expanded subpops
            if (state.parameters.exists(base.push(P_EXPANDED_SUBPOP_SIZE).push(""+x),null))
                {
                expandedSubpopSize[x] = state.parameters.getInt(base.push(P_EXPANDED_SUBPOP_SIZE).push(""+x),null,1);
                }
            else if (defaultSubpop >= 0 && state.parameters.exists(base.push(P_EXPANDED_SUBPOP_SIZE).push(""+defaultSubpop),null))
                {
                expandedSubpopSize[x] = state.parameters.getInt(base.push(P_EXPANDED_SUBPOP_SIZE).push(""+defaultSubpop),null,1);
                }
            else
                {
                state.output.warning("Expanded subpopulation size not defined for subpopulation " + x + ".  Assuming populations are not changed.");
                expandedSubpopSize[x] = V_SUBPOP_NOT_RESIZED;
                }
            }

        stubsFilled = new boolean[size];

        }

    public void breedSubpop(EvolutionState state, Subpopulation subpop, int index)
        {
        BreedingSource bp = (BreedingSource) subpop.species.pipe_prototype;
        if (!stubsFilled[index]) 
            bp.fillStubs(state, null);
        stubsFilled[index] = true;

        bp.prepareToProduce(state, index, 0);
        
        // maybe resize?
        ArrayList<Individual> newIndividuals = null;
        int newlen = subpop.individuals.size();
        if (expandedSubpopSize[index] != V_SUBPOP_NOT_RESIZED)
            {
            newlen = expandedSubpopSize[index];
            }
        
        newIndividuals = new ArrayList();
        
        ArrayList<Individual> individuals = subpop.individuals;
        int len = individuals.size();
        
        if (elite[index])
            {
            // We need to do some elitism: we put the BEST individual in the first slot
            Individual best = individuals.get(0);
            for(int i = 1; i < len ; i++)
                {
                Individual ind = individuals.get(i);
                if (ind.fitness.betterThan(best.fitness))
                    best = ind;
                }
            newIndividuals.add(best);
            }

        // start breedin'!
        while(newIndividuals.size() < newlen)
            {
            // we don't allocate a hash table every time, so we pass in null
            bp.produce(1,newlen-newIndividuals.size(), index, newIndividuals, state, 0,  null);
            }
            
        subpop.individuals = newIndividuals;
        bp.finishProducing(state, index, 0);
        }
    }
