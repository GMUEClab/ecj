/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.neat;

import java.util.*;
import ec.*;
import ec.neat.NEATSpecies.*;
import ec.util.*;

/**
 * NEATSubspecies is the actual Species in original code. However, since we
 * already have Species in ECJ, we name Species in original code as Subspecies
 * in our implementation. The creation of the Subspecies is done in the speciate
 * method.
 * 
 * @author Ermo Wei and David Freelan
 *
 */
public class NEATSubspecies implements Prototype
    {
    public static final String P_SUBSPECIES = "subspecies";



    /** Age of the current subspecies. */
    public int age;

    /**
     * Record the last time the best fitness improved within the individuals of
     * this subspecies If this is too long ago, the subspecies will goes extinct
     */
    public int ageOfLastImprovement;

    /** The max fitness the an individual in this subspecies ever achieved. */
    public double maxFitnessEver;

    /** The individuals within this species */
    public ArrayList<Individual> individuals;

    /** The next generation individuals within this species */
    public ArrayList<Individual> newGenIndividuals;

    /** Expected Offspring for next generation for this subspecies */
    public int expectedOffspring;

    @Override
    public void setup(EvolutionState state, Parameter base)
        {
        age = 1;
        ageOfLastImprovement = 0;
        maxFitnessEver = 0;
        individuals = new ArrayList<Individual>();
        newGenIndividuals = new ArrayList<Individual>();
        }

    /**
     * Return a clone of this subspecies, but with a empty individuals and
     * newGenIndividuals list.
     */
    public Object emptyClone()
        {
        NEATSubspecies myobj = (NEATSubspecies) clone();
        individuals = new ArrayList<Individual>();
        newGenIndividuals = new ArrayList<Individual>();
        return myobj;
        }

    public Object clone()
        {
        NEATSubspecies myobj = null;
        try
            {
            myobj = (NEATSubspecies) (super.clone());
            myobj.age = age;
            myobj.ageOfLastImprovement = ageOfLastImprovement;
            myobj.maxFitnessEver = maxFitnessEver;
            myobj.expectedOffspring = expectedOffspring;
            

            } catch (CloneNotSupportedException e)// never happens
            {
            throw new InternalError();
            }
        return myobj;
        }

    /** Reset the status of the current subspecies. */
    public void reset()
        {
        age = 1;
        expectedOffspring = 0;
        ageOfLastImprovement = 0;
        maxFitnessEver = 0;
        }

    /** Return the first individual in this subspecies */
    public Individual first()
        {
        if (individuals.size() > 0)
            return individuals.get(0);
        return null;
        }

    /** Return the first individual in newGenIndividuals list. */
    public Individual newGenerationFirst()
        {
        if (newGenIndividuals.size() > 0)
            return newGenIndividuals.get(0);
        return null;
        }

    @Override
    public Parameter defaultBase()
        {
        return NEATDefaults.base().push(P_SUBSPECIES);
        }

    /**
     * Adjust the fitness of the individuals within this subspecies. We will use
     * the adjusted fitness to determine the expected offsprings within each
     * subspecies.
     */
    public void adjustFitness(EvolutionState state, int dropoffAge, double ageSignificance)
        {
        int ageDebt = (age - ageOfLastImprovement + 1) - dropoffAge;
        if (ageDebt == 0)
            ageDebt = 1;

        for (int i = 0; i < individuals.size(); ++i)
            {
            NEATIndividual ind = (NEATIndividual) individuals.get(i);

            // start to adjust the fitness with age information
            ind.adjustedFitness = ind.fitness.fitness();

            // Make fitness decrease after a stagnation point dropoffAge
            // Added an if to keep species pristine until the dropoff point
            if (ageDebt >= 1)
                {
                ind.adjustedFitness = ind.adjustedFitness * 0.01;
                }

            // Give a fitness boost up to some young age (niching)
            // The age-significance parameter is a system parameter
            // If it is 1, then young species get no fitness boost
            if (age <= 10)
                ind.adjustedFitness = ind.adjustedFitness * ageSignificance;

            // Do not allow negative fitness
            if (ind.adjustedFitness < 0.0)
                ind.adjustedFitness = 0.0001;

            // Share fitness with the species
            // This is the explicit fitness sharing, where the the original
            // fitness
            // are dividing by the number of individuals in the species.
            // By using this, a species cannot afford to become too big even if
            // many of its
            // individual perform well
            ind.adjustedFitness = ind.adjustedFitness / individuals.size();

            }
        }

    /**
     * Sort the individuals in this subspecies, the one with highest fitness
     * comes first.
     */
    public void sortIndividuals()
        {
        // Sort the individuals
        // sorting is based on adjusted fitness, descending order
        Collections.sort(individuals, new Comparator<Individual>()
                {

                @Override
                public int compare(Individual i1, Individual i2)
                    {
                    NEATIndividual ind1 = (NEATIndividual) i1;
                    NEATIndividual ind2 = (NEATIndividual) i2;

                    if (ind1.adjustedFitness < ind2.adjustedFitness)
                        return 1;

                    if (ind1.adjustedFitness > ind2.adjustedFitness)
                        return -1;

                    return 0;
                    }
            });
        }

    /** Update the maxFitnessEver variable. */
    public void updateSubspeciesMaxFitness()
        {
        // Update ageOfLastImprovement here, assume the individuals are
        // already sorted
        // (the first Individual has the best fitness)
        if (individuals.get(0).fitness.fitness() > maxFitnessEver)
            {
            ageOfLastImprovement = age;
            maxFitnessEver = individuals.get(0).fitness.fitness();
            }

        }

    /** Mark the individual who can reproduce for this generation. */
    public void markReproducableIndividuals(double survivalThreshold)
        {
        // Decide how many get to reproduce based on survivalThreshold *
        // individuals.size()
        // mark for death those after survivalThreshold * individuals.size()
        // Adding 1.0 ensures that at least one will survive
        // floor is the largest (closest to positive infinity) double value that
        // is not greater than the argument and is equal to a mathematical
        // integer

        int numParents = (int) Math.floor(survivalThreshold * ((double) individuals.size()) + 1.0);

        // Mark the champion as such
        ((NEATIndividual) first()).champion = true;

        // Mark for death those who are ranked too low to be parents
        for (int i = 0; i < individuals.size(); ++i)
            {
            NEATIndividual ind = (NEATIndividual) individuals.get(i);
            if (i >= numParents)
                {
                ind.eliminate = true;
                }
            }
        }

    /** Test if newGenIndividuals list is empty. */
    public boolean hasNewGeneration()
        {
        return !newGenIndividuals.isEmpty();
        }

    /**
     * Compute the collective offspring the entire species (the sum of all
     * individual's offspring) is assigned skim is fractional offspring left
     * over from a previous subspecies that was counted. These fractional parts
     * are kept until they add up to 1
     */


    public double countOffspring(double skim)
        {
        expectedOffspring = 0;
        double x1 = 0.0, y1 = 1.0;
        double r1 = 0.0, r2 = skim;
        int n1 = 0, n2 = 0;

        for (int i = 0; i < individuals.size(); ++i)
            {
            x1 = ((NEATIndividual) individuals.get(i)).expectedOffspring;
            n1 = (int) (x1 / y1);
            r1 = x1 - ((int) (x1 / y1) * y1);
            n2 = n2 + n1;
            r2 = r2 + r1;

            if (r2 >= 1.0)
                {
                n2 = n2 + 1;
                r2 = r2 - 1.0;
                }
            }

        expectedOffspring = n2;
        return r2;
        }

    /**
     * Where the actual reproduce is happening, it will grab the candidate
     * parents, and calls the crossover or mutation method on these parents
     * individuals.
     */
    public boolean reproduce(EvolutionState state, int thread, int subpop, ArrayList<NEATSubspecies> sortedSubspecies)
        {
        if (expectedOffspring > 0 && individuals.size() == 0)
            {
            state.output.fatal("Attempt to reproduce out of empty subspecies");
            return false;
            }

        if (expectedOffspring > state.population.subpops.get(subpop).initialSize)
            {
            state.output.fatal("Attempt to reproduce too many individuals");
            return false;
            }

        NEATSpecies species = (NEATSpecies) state.population.subpops.get(subpop).species;

        // bestIndividual of the 'this' specie is the first element of the
        // species
        // note, we already sort the individuals based on the fitness (not sure
        // if this is still correct to say)
        NEATIndividual bestIndividual = (NEATIndividual) first();



       
        // create the designated number of offspring for the Species one at a
        // time
        boolean bestIndividualDone = false;

        for (int i = 0; i < expectedOffspring; ++i)
            {

            NEATIndividual newInd = null;

            if (bestIndividual.superChampionOffspring > 0)
                {

                newInd = (NEATIndividual) bestIndividual.clone();

                // Most super champion offspring will have their connection
                // weights mutated only
                // The last offspring will be an exact duplicate of this super
                // champion
                // Note: Super champion offspring only occur with stolen babies!
                // Settings used for published experiments did not use this

                if (bestIndividual.superChampionOffspring > 1)
                    {
                    if (state.random[thread].nextBoolean( 0.8) || species.mutateAddLinkProb == 0.0)
                        {
                        newInd.mutateLinkWeights(state, thread, species, species.weightMutationPower, 1.0,
                            MutationType.GAUSSIAN);
                        }
                    else
                        {
                        // Sometime we add a link to a superchamp
                        newInd.createNetwork(); // make sure we have the network
                        newInd.mutateAddLink(state,thread);
                        }
                    }
                if (bestIndividual.superChampionOffspring == 1)
                    {
                    if (bestIndividual.popChampion)
                        {
                        newInd.popChampionChild = true;
                        newInd.highFit = bestIndividual.fitness.fitness();
                        }
                    }

                bestIndividual.superChampionOffspring--;
                }
            else if ((!bestIndividualDone) && (expectedOffspring > 5))
                {

                newInd = (NEATIndividual) bestIndividual.clone();
                bestIndividualDone = true;
                }
            // Decide whether to mate or mutate
            // If there is only one individual, then always mutate
            else if (state.random[thread].nextBoolean(species.mutateOnlyProb) || individuals.size() == 1)
                {
                // Choose the random parent
                int parentIndex = state.random[thread].nextInt(individuals.size());
                Individual parent = individuals.get(parentIndex);
                newInd = (NEATIndividual) parent.clone();
                

                newInd.defaultMutate(state, thread);

               
                }
            else // Otherwise we should mate
                {
             
                // random choose the first parent
                int parentIndex = state.random[thread].nextInt(individuals.size());
                NEATIndividual firstParent = (NEATIndividual) individuals.get(parentIndex);
                NEATIndividual secondParent = null;
                // Mate within subspecies, choose random second parent
                if (state.random[thread].nextBoolean(1.0-species.interspeciesMateRate))
                    {
                    parentIndex = state.random[thread].nextInt(individuals.size());
                    secondParent = (NEATIndividual) individuals.get(parentIndex);
                    

                    }
                else // Mate outside subspecies
                    {
                    
                    // Select a random species
                    NEATSubspecies randomSubspecies = this;
                    // Give up if you cant find a different Species
                    int giveUp = 0;
                    while (randomSubspecies == this && giveUp < 5)
                        {
                        // Choose a random species tending towards better
                        // species
                        double value = state.random[thread].nextGaussian() / 4;
                        if (value > 1.0)
                            value = 1.0;
                        // This tends to select better species
                        
                        int upperBound = (int) Math.floor((value * (sortedSubspecies.size() - 1.0)) + 0.5);
                        int index = 0;
                        while (index < upperBound)
                            index++;
                        randomSubspecies = sortedSubspecies.get(index);
                        giveUp++;
                        }

                    secondParent = (NEATIndividual) randomSubspecies.first();

                    }

                newInd = firstParent.crossover(state, thread, secondParent);


                // Determine whether to mutate the baby's Genome
                // This is done randomly or if the parents are the same
                // individual
                if (state.random[thread].nextBoolean(1.0-species.mateOnlyProb) || firstParent== secondParent
                    || species.compatibility(firstParent, secondParent) == 0.0)
                    {
                    newInd.defaultMutate(state, thread);
                    }
                }

           

            newInd.setGeneration(state);
            newInd.createNetwork();

            // Add the new individual to its proper subspecies
            // this could create new subspecies
            species.speciate(state, newInd);
            }
 
          

        return true;

        }

    /**
     * Compute generations gap since last improvement
     */
    public int timeSinceLastImproved()
        {
        return age - ageOfLastImprovement;
        }

    /** Add the individual to the next generation of this subspecies */
    public void addNewGenIndividual(NEATIndividual neatInd)
        {
        newGenIndividuals.add(neatInd);
        neatInd.subspecies = this;
        }

    /**
     * Remove the individuals from current subspecies who have been mark as
     * eliminate the remain individuals will be allow to reproduce
     */
    public void removePoorFitnessIndividuals()
        {
        // create a new list, contain the non eliminate individuals
        ArrayList<Individual> remainIndividuals = new ArrayList<Individual>();
        for (int i = 0; i < individuals.size(); ++i)
            {
            NEATIndividual ind = (NEATIndividual) individuals.get(i);
            if (!ind.eliminate)
                {
                remainIndividuals.add(ind);
                }
            }
        individuals = remainIndividuals;
        }

    /**
     * After we finish the reproduce, the newGenIndividual list has the all the
     * individuals that is ready for evalution in next generation. Let's switch
     * to it.
     */
    public void toNewGeneration()
        {
        individuals = newGenIndividuals;
        // create a new ArrayList
        newGenIndividuals = new ArrayList<Individual>();
        }

    }
