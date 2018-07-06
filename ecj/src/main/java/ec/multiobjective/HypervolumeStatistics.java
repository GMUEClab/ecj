/*
  Copyright 2018 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.multiobjective;

import ec.EvolutionState;
import ec.Individual;
import ec.simple.SimpleStatistics;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Measures the hypervolume of a population's Pareto front.
 * 
 * Our implementation follows the WFG algorithm, described in
 * 
 * Lyndon While, Lucas Bradstreet, and Luigi Barone, "A Fast Way of Calculating
 * Exact Hypervolumes," IEEE Transactions on Evolutionary Computation, 16 (1),
 * February, 2012.
 * 
 * @author Eric O. Scott
 */
public class HypervolumeStatistics extends SimpleStatistics
    {
    public final static String P_REFERENCE_POINT = "reference-point";
    private double[] referencePoint;
    
    public double[] getReferencePoint()
        {
        return Arrays.copyOf(referencePoint, referencePoint.length);
        }
    
    @Override
    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        if (state.parameters.exists(base.push(P_DO_MESSAGE),null))
            state.output.warning("Messages are not printed out by " + this.getClass().getSimpleName(), base.push(P_DO_MESSAGE));
        if (state.parameters.exists(base.push(P_DO_DESCRIPTION),null))
            state.output.warning("Descriptions are not printed out by " + this.getClass().getSimpleName(), base.push(P_DO_DESCRIPTION));
        if (state.parameters.exists(base.push(P_DO_PER_GENERATION_DESCRIPTION),null))
            state.output.warning("Descriptions are not printed out by " + this.getClass().getSimpleName(), base.push(P_DO_PER_GENERATION_DESCRIPTION));
        
        referencePoint = state.parameters.getDoubles(base.push(P_REFERENCE_POINT), null, Double.NEGATIVE_INFINITY);
        if (referencePoint == null)
            state.output.fatal("Missing required parameter.", base.push(P_REFERENCE_POINT));
        if (doGeneration && referencePoint.length > 3)
            state.output.warnOnce(String.format("You calculating hypervolume on %d objectives at every generation.  Note that hypervolume calculation can very costly for more than a few objectives.", referencePoint.length), base.push(P_REFERENCE_POINT));
        }
    
    @Override
    public void postEvaluationStatistics(final EvolutionState state)
        {
        super.bypassPostEvaluationStatistics(state);
        
        state.output.print("" + state.generation, statisticslog);
        for(int s = 0; s < state.population.subpops.size(); s++)
            {
            if (doGeneration)
                {
                ArrayList<Individual> paretoFront = MultiObjectiveFitness.partitionIntoParetoFront(state.population.subpops.get(s).individuals, null, null);
                try
                    {
                    final double hv = hypervolume(paretoFront);
                    state.output.print(", " + hv, statisticslog);
                    }
                catch (final Exception e)
                    {
                    state.output.fatal(e.getMessage());
                    }
                }
            }
        state.output.print("\n", statisticslog);
        }
    
    /** Compute the hypervolume of the Pareto front induced by a collection of points. */
    public double hypervolume(final ArrayList<Individual> paretoFront)
        {
        assert(paretoFront != null);
        double exclusiveSum = 0.0;
        for (int i = 0; i < paretoFront.size(); i++)
            exclusiveSum += exclusiveHypervolume(paretoFront, i);
        return exclusiveSum;
        }
    
    /** Compute the exclusive hypervolume of the indIDth element of a collection 
     * of points against the elements whose ID is greater than indID.
     * 
     * Note that this does not compute the exclusive hypervolume with respect to
     * *every* other element in the collection, but only the elements that follow
     * indID!
     */
    private double exclusiveHypervolume(final ArrayList<Individual> paretoFront, final int indID)
        {
        assert(paretoFront != null);
        assert(indID >= 0);
        assert(indID < paretoFront.size());
        
        final Individual ind = paretoFront.get(indID);
        if (!(ind.fitness instanceof MultiObjectiveFitness))
            throw new IllegalStateException(String.format("%s: found an individual with a %s.  Hypervolume can only be computed for %s.", this.getClass().getSimpleName(), ind.fitness.getClass().getSimpleName(), MultiObjectiveFitness.class.getSimpleName()));
        
        final MultiObjectiveFitness indFitness = (MultiObjectiveFitness) ind.fitness;
        final MultiObjectiveFitness refFitness = (MultiObjectiveFitness) ind.fitness.clone();
        refFitness.objectives = referencePoint;
        
        if (!indFitness.paretoDominates(refFitness))
            throw new IllegalStateException(String.format("%s: found an individual (fitness: %s) that does not dominate the reference point (%s).  Cowardly refusing to compute a negative hypervolume contribution for this individual.  You probably need to choosing a different reference pointor check the maximization/minimization setting for the objectives.", this.getClass().getSimpleName(), Arrays.toString(indFitness.objectives), Arrays.toString(referencePoint)));
        
        final ArrayList<Individual> limitSet = limitSet(paretoFront, indID);
        final double ihv = inclusiveHypervolume(ind);
        assert(ihv >= 0);
        final double result = limitSet.isEmpty() ?
            ihv :
            ihv - hypervolume(MultiObjectiveFitness.partitionIntoParetoFront(limitSet, null, null));
        assert(result >= 0);
        return result;
        }
    
    /** Compute the hypervolume covered by a single individual. */
    public double inclusiveHypervolume(final Individual ind)
        {
        assert(ind != null);
        assert(ind.fitness instanceof MultiObjectiveFitness);
        final MultiObjectiveFitness fitness = (MultiObjectiveFitness) ind.fitness;
        if (fitness.objectives.length != referencePoint.length)
            throw new IllegalStateException(String.format("%s: %s has %d dimensions, but we encountered an individual with an %d-dimensional fitness.", this.getClass().getSimpleName(), P_REFERENCE_POINT, referencePoint.length, fitness.objectives.length));
        double product = 1.0;
        for (int i = 0; i < fitness.objectives.length; i++)
            product *= Math.abs(fitness.objectives[i] - referencePoint[i]);
        return product;
        }
    
    /** Compute a set of points that define the boundary of the *intersection* 
     * between an individual's inclusive hypervolume and the area dominated by
     * all the points in the front that follow indID (i.e. that have an index 
     * greater than indID). */
    private ArrayList<Individual> limitSet(final ArrayList<Individual> paretoFront, final int indID)
        {
        assert(paretoFront != null);
        assert(indID >= 0);
        assert(indID < paretoFront.size());
        
        final Individual contributingPoint = paretoFront.get(indID);
        assert(contributingPoint != null);
        assert(contributingPoint.fitness instanceof MultiObjectiveFitness);
        final MultiObjectiveFitness contributingFitness = (MultiObjectiveFitness) contributingPoint.fitness;
        if (contributingFitness.objectives.length != referencePoint.length)
            throw new IllegalStateException(String.format("%s: %s has %d dimensions, but we encountered an individual with an %d-dimensional fitness.", this.getClass().getSimpleName(), P_REFERENCE_POINT, referencePoint.length, contributingFitness.objectives.length));

        final ArrayList<Individual> set = new ArrayList<Individual>(paretoFront.size());
        for (int i = indID + 1; i < paretoFront.size(); i++)
            {
            final MultiObjectiveFitness refFitness = (MultiObjectiveFitness) paretoFront.get(i).fitness;
            final double[] newPoint = new double[referencePoint.length];
            for (int j = 0; j < newPoint.length; j++)
                {
                newPoint[j] = contributingFitness.isMaximizing(j) ? 
                    Math.min(contributingFitness.getObjective(j), refFitness.getObjective(j)) :
                    Math.max(contributingFitness.getObjective(j), refFitness.getObjective(j));
                }
            final Individual newPointInd = (Individual) contributingPoint.clone();
            newPointInd.fitness = (MultiObjectiveFitness) contributingFitness.clone();
            ((MultiObjectiveFitness)newPointInd.fitness).objectives = newPoint;
            set.add(newPointInd);
            }
        return set;
        }

    /** Logs the best individual of the run. */
    @Override
    public void finalStatistics(final EvolutionState state, final int result)
        {
        bypassFinalStatistics(state, result);  // just call super.super.finalStatistics(...)

        if (!doFinal)
            return;
        state.output.print("" + state.generation, statisticslog);
        for (int s = 0; s < state.population.subpops.size(); s++)
            {
            ArrayList<Individual> paretoFront = MultiObjectiveFitness.partitionIntoParetoFront(state.population.subpops.get(s).individuals, null, null);
            final double hv = hypervolume(paretoFront);
            state.output.println(", " + hv, statisticslog);
            }
        state.output.print("\n", statisticslog);
        }
    }
