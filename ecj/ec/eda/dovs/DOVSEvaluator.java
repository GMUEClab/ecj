package ec.eda.dovs;

import java.util.*;
import ec.*;
import ec.simple.*;

/**
 * The DOVSEvaluator is a SimpleEvaluator to evaluate the Individual. Due to
 * the stochastic property of the problem. An individual may not to be evaluate
 * several times so that we can have a good assessment of it. This evaluator
 * will make use of the statistics of fitness of each individual and determine
 * how many evaluation are needed for a individual where we can have high
 * confidence about its fitness value.
 *
 * @author Ermo Wei and David Freelan
 */

public class DOVSEvaluator extends SimpleEvaluator
    {
    /**
     * For each of the iteration, we are not just evaluate the individuals in
     * current population but also current best individual and individuals in
     * activeSolutions. Their number of evaluation is determined by there
     * fitness statistics.
     */
    protected void evalPopChunk(EvolutionState state, int[] numinds, int[] from, int threadnum, SimpleProblemForm p)
        {
        // so far the evaluator only support when evalthread is 1
        ((ec.Problem) p).prepareToEvaluate(state, threadnum);

        ArrayList<Subpopulation> subpops = state.population.subpops;
        int len = subpops.size();

        for (int pop = 0; pop < len; pop++)
            {
            // start evaluatin'!
            int fp = from[pop];
            int upperbound = fp + numinds[pop];
            ArrayList<Individual> inds = subpops.get(pop).individuals;
            if (subpops.get(pop).species instanceof DOVSSpecies)
                {
                DOVSSpecies species = (DOVSSpecies) subpops.get(pop).species;

                // Evaluator need to evaluate individual from two set: Sk
                // (individuals) and activeSolution
                // Original comment: To avoid unnecessary complication with
                // stopping test
                // procedure, require that Sk has at least 2 reps.
                // Although we do not have stopping test here, we still do 2
                // reps
                for (int i = 0; i < inds.size(); ++i)
                    {
                    DOVSFitness fit = (DOVSFitness)(inds.get(i).fitness);
                    int addrep = 2 - fit.numOfObservations();
                    for (int rep = 0; rep < addrep; ++rep)
                        {
                        p.evaluate(state, inds.get(i), pop, threadnum);
                        species.numOfTotalSamples++;
                        }
                    }

                // This is a special treat for activeSolutions when
                // certain criteria have met
                if (//species.ocba && 
                    species.stochastic)
                    {
                    // ocba only makes sense when it is a stoc simulation
                    // allocate some reps to active solutions and sample
                    // best according to an ocba like heuristic
                    // if ocba option is turned on.
                    // There are deltan more reps to allocate, where deltan
                    // = sizeof(activesolutions).
                    int deltan = species.activeSolutions.size();
                    // Always add two more reps to current sample best
                    for (int i = 0; i < 2; i++)
                        p.evaluate(state, species.visited.get(species.optimalIndex), pop, threadnum);
                    species.numOfTotalSamples += 2;
                    deltan -= 2;
                    if (deltan > 0)
                        {
                        // get R
                        double R = 0;
                        for (int i = 0; i < species.activeSolutions.size(); ++i)
                            {
                            Individual ind = species.activeSolutions.get(i);
                            DOVSFitness fit = (DOVSFitness)(ind.fitness);
                            Individual bestInd = species.visited.get(species.optimalIndex);
                            DOVSFitness bestFit = (DOVSFitness)(bestInd.fitness);
                            R += (fit.variance
                                / Math.max(1e-10, Math.abs(fit.mean - bestFit.mean)));
                            }
                        for (int i = 0; i < species.activeSolutions.size(); ++i)
                            {
                            Individual ind = (Individual) species.activeSolutions.get(i);
                            DOVSFitness fit = (DOVSFitness)(ind.fitness);
                            Individual bestInd = (Individual) species.visited.get(species.optimalIndex);
                            DOVSFitness bestFit = (DOVSFitness)(bestInd.fitness);

                            double fraction = fit.variance
                                / Math.max(1e-10, Math.abs(fit.mean - bestFit.mean)) / R;
                            double tempDeltan = fraction * deltan;
                            if (tempDeltan > 1)
                                {
                                long roundedDeltan = (long) tempDeltan;
                                for (int j = 0; j < roundedDeltan; ++j)
                                    p.evaluate(state, ind, pop, threadnum);
                                species.numOfTotalSamples += roundedDeltan;
                                }
                            }

                        }
                    }

                // If it is a deterministic simulation, only one rep

                // origial code start generation at 1, we start at 0
                // thus, we add 1 to computation of base of log
                int base = state.generation + 1;

                int newReps = (int) Math
                    .ceil(species.initialReps * Math.max(1, Math.pow(Math.log((double) base / 2), 1.01)));
                if (species.stochastic)
                    species.repetition = (species.repetition >= newReps) ? species.repetition : newReps;
                else
                    species.repetition = 1;

                // Now do the simulations for activeSolutions
                for (int count = 0; count < species.activeSolutions.size(); ++count)
                    {
                    Individual individual = (Individual) species.activeSolutions.get(count);
                    DOVSFitness fit = (DOVSFitness)(individual.fitness);
                    if (fit.numOfObservations() < species.repetition)
                        {
                        int newrep = species.repetition - fit.numOfObservations();
                        for (int rep = 0; rep < newrep; ++rep)
                            {
                            p.evaluate(state, individual, pop, threadnum);
                            }
                        species.numOfTotalSamples += newrep;
                        }
                    }

                // Simulate current sample best
                    {
                    Individual bestIndividual = (Individual) species.visited.get(species.optimalIndex);
                    DOVSFitness fit = (DOVSFitness)(bestIndividual.fitness);
                    if (fit.numOfObservations() < species.repetition)
                        {
                        int newrep = species.repetition - fit.numOfObservations();
                        for (int rep = 0; rep < newrep; ++rep)
                            {
                            p.evaluate(state, bestIndividual, pop, threadnum);
                            }
                        species.numOfTotalSamples += newrep;
                        }
                    }

                // Simulate current individuals
                // Since backtracking flag is always false, we always do this
                for (int i = 0; i < inds.size(); ++i)
                    {
                    DOVSFitness fit = (DOVSFitness)(inds.get(i).fitness);
                    if (fit.numOfObservations() < species.repetition)
                        {
                        int newRep = species.repetition - fit.numOfObservations();
                        for (int rep = 0; rep < newRep; ++rep)
                            {
                            p.evaluate(state, inds.get(i), pop, threadnum);
                            }
                        species.numOfTotalSamples += newRep;
                        }
                    }
                }
            else
                {
                for (int x = fp; x < upperbound; x++)
                    p.evaluate(state, inds.get(x), pop, threadnum);
                }
            }

        ((ec.Problem) p).finishEvaluating(state, threadnum);
        }

    }
