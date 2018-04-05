/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.singlestate;

import ec.*;
import ec.util.*;

public class SingleStateEvolutionState extends EvolutionState
    {
    /** In how many iterations do we collect statistics */
    public int statisticsModulo = 1;
    
    /** In how many iterations do we perform an exchange */
    public int exchangeModulo = 1;
    
    public final static String P_STATISTICS_MODULO = "stats-modulo";
    public final static String P_EXCHANGE_MODULO = "exchange-modulo";

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state, base);

        Parameter p = new Parameter(P_STATISTICS_MODULO);
        statisticsModulo = parameters.getInt(p, null, 1);
        if (statisticsModulo == 0)
            output.fatal("The statistics modulo must be an integer > 0.", p);

        p = new Parameter(P_EXCHANGE_MODULO);
        exchangeModulo = parameters.getInt(p, null, 1);
        if (exchangeModulo == 0)
            output.fatal("The exchange modulo must be an integer > 0.", p);

        if (statisticsModulo > exchangeModulo ||
            exchangeModulo % statisticsModulo != 0)
            output.fatal("The exchange modulo should to be a multiple of the statistics modulo.", p);

        p = new Parameter(P_EXCHANGE_MODULO);
        if (statisticsModulo > checkpointModulo ||
            checkpointModulo % statisticsModulo != 0)
            output.fatal("The checkpoint modulo should to be a multiple of the statistics modulo.", p);
        }

    public void startFresh()
        {
        output.message("Setting up");
        setup(this, null);  // a garbage Parameter

        // POPULATION INITIALIZATION
        output.message("Initializing Generation 0");
        statistics.preInitializationStatistics(this);
        population = initializer.initialPopulation(this, 0); // unthreaded
        statistics.postInitializationStatistics(this);

        // Compute generations from evaluations if necessary
        if (numEvaluations > UNDEFINED)
            { 
            // compute a generation's number of individuals
            int generationSize = 0;
            for (int sub = 0; sub < population.subpops.size(); sub++)
                {
                generationSize += population.subpops.get(sub).individuals.size();  // so our sum total 'generationSize' will be the initial total number of individuals
                }

            if (numEvaluations < generationSize)
                {
                numEvaluations = generationSize;
                numGenerations = 1;
                output.warning("Using evaluations, but evaluations is less than the initial total population size ("
                    + generationSize + ").  Setting to the populatiion size.");
                }
            else
                {
                if (numEvaluations % generationSize != 0)
                    output.warning(
                        "Using evaluations, but initial total population size does not divide evenly into it.  Modifying evaluations to a smaller value ("
                        + ((numEvaluations / generationSize) * generationSize) + ") which divides evenly.");  // note integer division
                numGenerations = (int) (numEvaluations / generationSize);  // note integer division
                numEvaluations = numGenerations * generationSize;
                }
            output.message("Generations will be " + numGenerations);
            }

        // INITIALIZE CONTACTS -- done after initialization to allow
        // a hook for the user to do things in Initializer before
        // an attempt is made to connect to island models etc.
        exchanger.initializeContacts(this);
        evaluator.initializeContacts(this);
        }

    public int evolve()
        {
        boolean isExchangeBorder = false;
        boolean isStatisticsBorder = (generation % statisticsModulo == 0);
        if (isStatisticsBorder)
            {
            isExchangeBorder = (generation % exchangeModulo == 0);
            }

        if (isStatisticsBorder)
            output.message("Generation " + generation +"\tEvaluations So Far " + evaluations);

        // EVALUATION
        if (isStatisticsBorder)
            statistics.preEvaluationStatistics(this);
        evaluator.evaluatePopulation(this);
        if (isStatisticsBorder)
            statistics.postEvaluationStatistics(this);
        
        // SHOULD WE QUIT?
        String runCompleteMessage = evaluator.runComplete(this);
        if ((runCompleteMessage != null) && quitOnRunComplete)
            {
            output.message(runCompleteMessage);
            return R_SUCCESS;
            }

        // SHOULD WE QUIT?
        if ((numGenerations != UNDEFINED && generation >= numGenerations-1) ||
            (numEvaluations != UNDEFINED && evaluations >= numEvaluations))
            {
            return R_FAILURE;
            }

        // INCREMENT GENERATION AND CHECKPOINT
        generation++;

        // PRE-BREEDING EXCHANGING
        if (isExchangeBorder)
            {
            statistics.prePreBreedingExchangeStatistics(this);
            population = exchanger.preBreedingExchangePopulation(this);
            statistics.postPreBreedingExchangeStatistics(this);

            String exchangerWantsToShutdown = exchanger.runComplete(this);
            if (exchangerWantsToShutdown != null)
                {
                output.message(exchangerWantsToShutdown);
                return R_SUCCESS;
                }
            }

        // BREEDING
        if (isStatisticsBorder)
            statistics.preBreedingStatistics(this);
        population = breeder.breedPopulation(this);
        if (isStatisticsBorder)
            statistics.postBreedingStatistics(this);

        // POST-BREEDING EXCHANGING
        if (isExchangeBorder)
            {
            statistics.prePostBreedingExchangeStatistics(this);
            population = exchanger.postBreedingExchangePopulation(this);
            statistics.postPostBreedingExchangeStatistics(this);
            }
        
        if (isStatisticsBorder && checkpoint && (generation - 1) % checkpointModulo == 0)
            {
            output.message("Checkpointing");
            statistics.preCheckpointStatistics(this);
            Checkpoint.setCheckpoint(this);
            statistics.postCheckpointStatistics(this);
            }

        return R_NOTDONE;
        }

    /**
     * @param result
     */
    public void finish(int result)
        {
        output.message("Total Evaluations " + evaluations);
        statistics.finalStatistics(this, result);
        finisher.finishPopulation(this, result);
        exchanger.closeContacts(this, result);
        evaluator.closeContacts(this, result);
        }

    }
