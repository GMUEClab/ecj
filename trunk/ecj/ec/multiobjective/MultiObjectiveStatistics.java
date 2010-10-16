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
import ec.util.*;
import java.io.*;

/* 
 * MultiObjectiveStatistics.java
 * 
 * Created: Thu Feb 04 2010
 * By: Faisal Abidi and Sean Luke
 *
 */

/*
 * MultiObjectiveStatistics are a SimpleStatistics subclass which overrides the finalStatistics
 * method to output the current Pareto Front in various ways:
 *
 * <ul>
 * <li><p>Every individual in the Pareto Front is written to the end of the statistics log.
 * <li><p>A summary of the objective values of the Pareto Front is written to stdout.
 * <li><p>The objective values of the Pareto Front are written in tabular form to a special
 * Pareto Front file specified with the parameters below.  This file can be easily read by
 * gnuplot or Excel etc. to display the Front (if it's 2D or perhaps 3D).
 * 
 * <p>
 * <b>Parameters</b><br>
 * <table>
 * <tr>
 * <td valign=top><i>base</i>.<tt>front</tt><br>
 * <font size=-1>String (a filename)</font></td>
 * <td valign=top>(The Pareto Front file, if any)</td>
 * </tr>
 * </table>
 */

public class MultiObjectiveStatistics extends SimpleStatistics
    {   
    /** front file parameter */
    public static final String P_PARETO_FRONT_FILE = "front";
        
    /** The pareto front log */
        
    public static final int NO_FRONT_LOG = -1;
        
    public int frontLog;

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        
        File frontFile = state.parameters.getFile(base.push(P_PARETO_FRONT_FILE),null);

        if (frontFile!=null)
            try
                {
                frontLog = state.output.addLog(frontFile, !compress, compress);
                }
            catch (IOException i)
                {
                state.output.fatal("An IOException occurred while trying to create the log " + frontFile + ":\n" + i);
                }
        else state.output.warning("No Pareto Front statistics file specified.", base.push(P_PARETO_FRONT_FILE));
        }



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
                
            // write short version of front out to disk
            if (frontLog >= 0)
                {
                if (state.population.subpops.length > 1)
                    state.output.println("Subpopulation " + s, frontLog);
                for (int i = 0; i < sortedFront.length; i++)
                    {
                    Individual ind = (Individual)(sortedFront[i]);
                    MultiObjectiveFitness mof = (MultiObjectiveFitness) (ind.fitness);
                    float[] objectives = mof.getObjectives();

                    String line = "";
                    for (int f = 0; f < objectives.length; f++)
                        line += (objectives[f] + " ");
                    state.output.println(line, frontLog);
                    }
                }
            }
        }
    }
