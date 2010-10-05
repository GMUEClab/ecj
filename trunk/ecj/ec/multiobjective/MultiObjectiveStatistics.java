/*
  Copyright 2010 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.multiobjective;

import java.util.ArrayList;
import java.util.Arrays;
import ec.EvolutionState;
import ec.Individual;
import ec.Subpopulation;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.SimpleStatistics;
import ec.util.QuickSort;
import ec.util.SortComparator;

/* 
 * MultiObjectiveStatistics.java
 * 
 * Created: Thu Feb 04 2010
 * By: Faisal Abidi
 */

/**
 * 
 * @author Faisal Abidi
 */
public class MultiObjectiveStatistics extends SimpleStatistics
    {   
    /** Logs the best individual of the run. */
    public void finalStatistics(final EvolutionState state, final int result)
        {
        // super.finalStatistics(state,result);
        // I don't want just a single best fitness

		state.output.println("\n\n\n PARETO FRONTS", statisticslog);
        for (int s = 0; s < state.population.subpops.length; s++)
			{
			MultiObjectiveFitness typicalFitness = (MultiObjectiveFitness)(state.population.subpops[s].individuals[0].fitness);
			state.output.println("\n\nPareto Front of Subpopulation " + s, statisticslog);

			// build front
			ArrayList front = typicalFitness.partitionIntoParetoFront(state.population.subpops[s].individuals, null, null);

            // sort by objective[0]
			Object[] sortedFront = front.toArray();
			QuickSort.qsort(sortedFront, new SortComparator()
                {
                public boolean lt(Object a, Object b)
                    {
                    return (((MultiObjectiveFitness) (((Individual) a).fitness)).getObjective(0) < 
							(((MultiObjectiveFitness) ((Individual) b).fitness)).getObjective(0));
                    }
                
                public boolean gt(Object a, Object b)
                    {
                    return (((MultiObjectiveFitness) (((Individual) a).fitness)).getObjective(0) > 
							((MultiObjectiveFitness) (((Individual) b).fitness)).getObjective(0));
                    }
                });
			
			// print out header
			state.output.message("Pareto Front Summary: " + sortedFront.length + " Individuals");
			String message = "Ind";
			int numObjectives = typicalFitness.getObjectives().length;
			for(int i = 0; i < numObjectives; i++) 
				message += ("\t" + "Objective " + i);
			String[] names = typicalFitness.getAuxilliaryFitnessNames();
			for(int i = 0; i < names.length; i++) 
				message += ("\t" + names[i]);
            state.output.message(message);
			
			// write front to screen
            for (int i = 0; i < sortedFront.length; i++)
				{
                Individual individual = (Individual) (sortedFront[i]);

                float[] objectives = ((MultiObjectiveFitness) individual.fitness).getObjectives();
                String line = "" + i;
                for (int f = 0; f < objectives.length; f++)
					line += ("\t" + objectives[f]);

				double[] vals = ((MultiObjectiveFitness) individual.fitness).getAuxilliaryFitnessValues();
				for(int f = 0; f < vals.length; f++) 
					line += ("\t" + vals[f]);
				state.output.message(line);
				}
			
			// print out front to statistics log
            for (int i = 0; i < sortedFront.length; i++)
                ((Individual)(sortedFront[i])).printIndividualForHumans(state, statisticslog);
            }
        }
	}
