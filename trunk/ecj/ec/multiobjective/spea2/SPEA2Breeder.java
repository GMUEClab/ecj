package ec.multiobjective.spea2;

/*
  Copyright 2006 by Robert Hubley
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

import ec.*;
import ec.util.*;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.*;
import java.util.*;

/* 
 * SPEA2Breeder.java
 * 
 * Created: Wed Jun 26 11:20:32 PDT 2002
 * By: Robert Hubley, Institute for Systems Biology
 *     (Based on Breeder.java by Sean Luke)
 */

/**
 * Breeds each subpopulation separately, with no inter-population exchange, and
 * using the SPEA2 approach. A SPEA2Breeder may have multiple threads; it divvys
 * up a subpopulation into chunks and hands one chunk to each thread to
 * populate. One array of BreedingPipelines is obtained from a population's
 * Species for each operating breeding thread.
 * 
 * Prior to breeding a subpopulation, a SPEA2Breeder will first fill part of the
 * new subpopulation (the archive) with the individuals with an SPEA2 fitness of
 * less than 1.0 from the old subpopulation. If there are less individuals with
 * this cutoff than can fit in the archive the free slots are filled with the
 * lowest scoring customFitnessMetric individuals. If there are more individuals
 * with an customFitnessMetric less than 1 than can fit in the archive then a
 * density metric is used to truncate the archive and remove individuals which
 * are close to others.
 * 
 * The archive filling step is performed by a single thread.
 * 
 * <p>
 * This is actually a re-write of SPEA2Breeder.java by Robert Hubley, to make it
 * more modular so it's easier for me to extend it. Namely:
 * <ol>
 * <li>I isolated the following functionality:
 * "Out of N individuals, give me a front of size M < N."
 * 
 * I need this at the last generation, when one createad a bunch of individuals,
 * evaluated them, but not give them a chance to enter the archive, as the
 * breeder is not called on the last generation!
 * 
 * <li>Additonally, I made <code>double[][] distances</code> and
 * <code>int[][] sortedIndex</code> static and then reused them to reduce GC!!!
 * For more advanced cases where the the number of individuals is not fixed
 * and/or known in advance I chose to have these arrays extended when needed.
 * <ul>
 * <li>Note that they need not be shrunk.
 * <li>Note that in the usual case when the number of individuals from the old
 * population is always the same, the arrays are only allocated once, so there's
 * no efficiency loss here. (except I keep asking `are they big enough?' each
 * time <code>loadElites()</code> is called, but that's not a big deal given
 * that <code>loadElites()</code> is O(n^3)).
 * </ul>
 * 
 * <li>Lastly, I do fewer iterations in the loop that compacts surviving
 * individuals and copies them in the new population (i.e. previous version was
 * visiting a bunch of nulls at the end of the array).
 * </ol>
 * 
 * @author Robert Hubley (based on Breeder.java by Sean Luke), Gabriel Balan,
 *         Keith Sullivan
 * @version 1.1
 */

public class SPEA2Breeder extends SimpleBreeder
    {
    protected void loadElites(EvolutionState state, Population newpop)
        {
        // are our elites small enough?
        for(int x=0;x<state.population.subpops.length;x++)
            if (elite[x]>state.population.subpops[x].individuals.length)
                state.output.error("The number of elites for subpopulation " + x + " exceeds the actual size of the subpopulation", new Parameter(EvolutionState.P_BREEDER).push(P_ELITE).push(""+x));
        state.output.exitIfErrors();

		// do it
        for (int sub = 0; sub < state.population.subpops.length; sub++)
            {
            Individual[] newInds = newpop.subpops[sub].individuals;  // The new population after we are done picking the elites			
            Individual[] oldInds = state.population.subpops[sub].individuals;   // The old population from which to pick elites
			
            loadElites(state, oldInds, newInds, elite[sub]);
            }

		// optionally force reevaluation
		unmarkElitesEvaluated(newpop);
        }

	double[] calculateDistancesFromIndividual(Individual ind, Individual[] inds)
		{
		double[] d = new double[inds.length];
		for(int i = 0; i < inds.length; i++)
			d[i] = ((SPEA2MultiObjectiveFitness)ind.fitness).sumSquaredObjectiveDistance((SPEA2MultiObjectiveFitness)inds[i].fitness);
		// now sort
		Arrays.sort(d);
		return d;
		}


	void loadElites(EvolutionState state, Individual[] oldInds, Individual[] newInds, int archiveSize)
		{
		Individual[] dummy = new Individual[0];
		
		// step 1: load the archive with the pareto-nondominated front
		ArrayList archive = new ArrayList();
		ArrayList nonFront = new ArrayList();
		MultiObjectiveFitness.partitionIntoParetoFront(oldInds, archive, nonFront);
		int currentArchiveSize = archive.size();
		
		// step 2: if the archive isn't full, load the remainder with the fittest individuals (using customFitnessMetric) that aren't in the archive yet
		if (currentArchiveSize < archiveSize)
			{
			Collections.sort(nonFront);  // the fitter individuals will be earlier
			int len = (archiveSize - currentArchiveSize);
			for(int i = 0; i < len; i++)
				{
				archive.add(nonFront.get(i));
				currentArchiveSize++;
				}
			}
			

		// step 3: if the archive is OVERFULL, iterate as follows:
		//		step 3a: remove the k-closest individual in the archive
		SPEA2Evaluator evaluator = ((SPEA2Evaluator)(state.evaluator));
		Individual[] inds = (Individual[])(archive.toArray(dummy));
		
		while(currentArchiveSize > archiveSize)
			{
			Individual closest = (Individual)(archive.get(0));
			int closestIndex = 0;
			double[] closestD = calculateDistancesFromIndividual(closest, oldInds);
			
			for(int i = 1; i < currentArchiveSize; i++)
				{
				Individual competitor = (Individual)(archive.get(i));
				double[] competitorD = calculateDistancesFromIndividual(competitor, oldInds);
				
				for(int k = 0; k < oldInds.length; k++)
					{
					if (closestD[i] > competitorD[i])
						{ closest = competitor ; closestD = competitorD;  closestIndex = k; break; }
					else if (closestD[i] < competitorD[i])
						{ break; }
					}
				}
			
			// remove him destructively -- put the top guy in his place and remove the top guy.  This is O(1)
			archive.set(closestIndex, archive.get(archive.size()-1));
			archive.remove(archive.size()-1);
			
			currentArchiveSize--;
			}
						
		// step 4: put clones of the archive in the new individuals
		Object[] obj = archive.toArray();
		for(int i = 0; i < archiveSize; i++)
			newInds[newInds.length - archiveSize + i] = (Individual)(((Individual)obj[i]).clone());
		}
    }
