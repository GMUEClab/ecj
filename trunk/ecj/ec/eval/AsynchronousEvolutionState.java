package ec.eval;

import ec.steadystate.*;
import ec.*;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;
import ec.util.Checkpoint;

public class AsynchronousEvolutionState extends SteadyStateEvolutionState
    {
    public int evolve()
        {
        if (generationBoundary && generation > 0)
            output.message("Generation " + generation);
        
        // EVALUATION
        
        if (firstTimeAround)
            if (statistics instanceof SteadyStateStatisticsForm)
                ((SteadyStateStatisticsForm)statistics).preInitialEvaluationStatistics(this);
            
        if (generationBoundary)
            statistics.preEvaluationStatistics(this);

        ((MasterProblem)(evaluator.p_problem)).batchMode = true;
        evaluator.evaluatePopulation(this);

        //  new code for asynchronous evolution state:
        //    wait to receive back N individuals, put them back in the population, then create some more individuals to be evaluated
        if( firstTimeAround )
            {
            for( int i = 0 ; i < population.subpops[0].individuals.length ; i++ )
                {
                Individual ind = (((MasterProblem)(evaluator.p_problem)).server).slaveMonitor.waitForIndividual(this);
                population.subpops[0].individuals[i] = ind;
                ind = population.subpops[0].species.newIndividual( this, 0 );  // unthreaded
                ((SimpleProblemForm)evaluator.p_problem).evaluate(this, ind, 0);
                }
            }
        else
            {
            Individual ind = (((MasterProblem)(evaluator.p_problem)).server).slaveMonitor.waitForIndividual(this);
            population.subpops[0].individuals[newIndividuals[0]] = ind;
            ((SteadyStateBreeder)(breeder)).individualReplaced(this,0,0,newIndividuals[0]);
            }

        if (generationBoundary)
            statistics.postEvaluationStatistics(this);
        
        if (firstTimeAround)
            {
            if (statistics instanceof SteadyStateStatisticsForm)
                ((SteadyStateStatisticsForm)statistics).postInitialEvaluationStatistics(this);
            evaluations += population.subpops[0].individuals.length;
            }
        else
            {
            if (statistics instanceof SteadyStateStatisticsForm)
                ((SteadyStateStatisticsForm)statistics).individualsEvaluatedStatistics(this);
            evaluations++;
            }

        // COMPUTE GENERATION BOUNDARY
        generationBoundary = (evaluations % generationSize == 0);

        // SHOULD WE QUIT?
        if (evaluator.runComplete(this) && quitOnRunComplete)
            { 
            output.message("Found Ideal Individual"); 
            return R_SUCCESS;
            }
        
        if ((numEvaluations > 0 && evaluations >= numEvaluations) ||  // using numEvaluations
            (numEvaluations <= 0 && generationBoundary && generation == numGenerations -1))  // not using numEvaluations
            {
            return R_FAILURE;
            }
        
        // PRE-BREEDING EXCHANGING
        if (generationBoundary)
            {
            statistics.prePreBreedingExchangeStatistics(this);
            population = exchanger.preBreedingExchangePopulation(this);
            statistics.postPreBreedingExchangeStatistics(this);
            String exchangerWantsToShutdown = exchanger.runComplete(this);
            if (exchangerWantsToShutdown!=null)
                { 
                output.message(exchangerWantsToShutdown); 
                return R_SUCCESS;
                }
            }
            
        // BREEDING
        if (generationBoundary)
            statistics.preBreedingStatistics(this);
        population = breeder.breedPopulation(this);
        if (statistics instanceof SteadyStateStatisticsForm)
            ((SteadyStateStatisticsForm)statistics).individualsBredStatistics(this);
        if (generationBoundary)
            statistics.postBreedingStatistics(this);
        
        // POST-BREEDING EXCHANGE
        if (generationBoundary)
            {
            statistics.prePostBreedingExchangeStatistics(this);
            population = exchanger.postBreedingExchangePopulation(this);
            statistics.postPostBreedingExchangeStatistics(this);
            }
        
        // INCREMENT GENERATION 
        if (generationBoundary)
            generation++;
        firstTimeAround = false;

        // CHECKPOINTING
        if (checkpoint && generation%checkpointModulo == 0) 
            {
            output.message("Checkpointing");
            statistics.preCheckpointStatistics(this);
            Checkpoint.setCheckpoint(this);
            statistics.postCheckpointStatistics(this);
            }
        return R_NOTDONE;
        }
    
    }
