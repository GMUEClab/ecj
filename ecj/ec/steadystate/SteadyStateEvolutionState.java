/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.steadystate;
import ec.*;
import ec.util.Parameter;
import ec.util.Checkpoint;
//import ec.eval.MasterProblem;
import java.util.*; 

/* 
 * SteadyStateEvolutionState.java
 * 
 */

/**
 * This subclass of EvolutionState implements basic Steady-State Evolution and (in distributed form)
 * Asynchronous Evolution. The procedure is as follows.  We begin with an empty Population and one by
 * one create new Indivdiuals and send them off to be evaluated.  In basic Steady-State Evolution the
 * individuals are immediately evaluated and we wait for them; but in Asynchronous Evolution the individuals are evaluated
 * for however long it takes and we don't wait for them to finish.  When individuals return they are
 * added to the Population until it is full.  No duplicate individuals are allowed.
 *
 * <p>At this point the system switches to its "steady state": individuals are bred from the population
 * one by one, and sent off to be evaluated.  Once again, in basic Steady-State Evolution the
 * individuals are immediately evaluated and we wait for them; but in Asynchronous Evolution the individuals are evaluated
 * for however long it takes and we don't wait for them to finish.  When an individual returns, we
 * mark an individual in the Population for death, then replace it with the new returning individual.
 * Note that during the steady-state, Asynchronous Evolution could be still sending back some "new" individuals
 * created during the initialization phase, not "bred" individuals.
 *
 * <p>The determination of how an individual is marked for death is done by the SteadyStateBreeder.
 *
 * <p>SteadyStateEvolutionState will run either for some N "generations" or for some M evaluations of
 * individuals.   A "generation" is defined as a Population's worth of evaluations.   If you do not
 * specify the number of evaluations (the M), then SteadyStateEvolutionState will use the standard
 * generations parameter defined in EvolutionState.
 *
 
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><tt>evaluations</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(maximal number of evaluations to run.)</td></tr>
 <tr><td valign=top><tt>steady.replacement-probability</tt><br>
 <font size=-1>0.0 &lt;= double &lt;= 1.0 (default is 1.0)</font></td>
 <td valign=top>(probability that an incoming individual will unilaterally replace the individual marked 
 for death, as opposed to replacing it only if the incoming individual is superior in fitness)</td></tr>
 </table>
 
 *
 * @author Sean Luke
 * @version 1.0 
 */


public class SteadyStateEvolutionState extends EvolutionState
    {
    public static final String P_REPLACEMENT_PROBABILITY = "replacement-probability";
        
    /** Did we just start a new generation? */
    public boolean generationBoundary;
    
    /** how big is a generation? Set to the size of subpopulation 0 of the initial population. */
    public int generationSize;
    
    /** When a new individual arrives, with what probability should it directly replace the existing
        "marked for death" individual, as opposed to only replacing it if it's superior? */
    public double replacementProbability;
        
    //int[] individualCount; 
        
    /** Hash table to check for duplicate individuals */ 
    HashMap[] individualHash; 
        
    /** Holds which subpopulation we are currently operating on */
    int whichSubpop;
    
    /** First time calling evolve */
    protected boolean firstTime; 
        
    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
                
        // double check that we have valid evaluators and breeders and exchangers
        if (!(breeder instanceof SteadyStateBreeder))
            state.output.error("You've chosen to use Steady-State Evolution, but your breeder is not of the class SteadyStateBreeder.",base);
        if (!(evaluator instanceof SteadyStateEvaluator))
            state.output.error("You've chosen to use Steady-State Evolution, but your evaluator is not of the class SteadyStateEvaluator.",base);
        if (!(exchanger instanceof SteadyStateExchangerForm))
            state.output.error("You've chosen to use Steady-State Evolution, but your exchanger does not implement the SteadyStateExchangerForm.",base);
        
        checkStatistics(state, statistics, new Parameter(P_STATISTICS));
        
        if (parameters.exists(SteadyStateDefaults.base().push(P_REPLACEMENT_PROBABILITY),null))
            {
            replacementProbability = parameters.getDoubleWithMax(SteadyStateDefaults.base().push(P_REPLACEMENT_PROBABILITY),null,0.0, 1.0);
            if (replacementProbability < 0.0) // uh oh
                state.output.error("Replacement probability must be between 0.0 and 1.0 inclusive.",
                    SteadyStateDefaults.base().push(P_REPLACEMENT_PROBABILITY), null);
            }
        else
            {
            replacementProbability = 1.0;  // always replace
            state.output.message("Replacement probability not defined: using 1.0 (always replace)");
            }
        }
    
    // recursively prints out warnings for all statistics that are not
    // of steadystate statistics form
    void checkStatistics(final EvolutionState state, Statistics stat, final Parameter base)
        {
        if (!(stat instanceof SteadyStateStatisticsForm))
            state.output.warning("You've chosen to use Steady-State Evolution, but your statistics does not implement the SteadyStateStatisticsForm.",base);
        for(int x=0;x<stat.children.length;x++)
            if (stat.children[x]!=null)
                checkStatistics(state, stat.children[x], base.push("child").push(""+x));
        }
    
    
    /**
     * 
     */
    public void startFresh() 
        {
        output.message("Setting up");
        setup(this,null);  // a garbage Parameter

        // POPULATION INITIALIZATION
        output.message("Initializing Generation 0");
        statistics.preInitializationStatistics(this);
        population = initializer.setupPopulation(this, 0);  // unthreaded.  We're NOT initializing here, just setting up.

        // INITIALIZE VARIABLES
        generationSize = 0;
        generationBoundary = false;
        firstTime = true; 
        evaluations=0; 
        whichSubpop=-1; 
                
        individualHash = new HashMap[population.subpops.size()];
        for(int i = 0; i< population.subpops.size(); i++) individualHash[i] = new HashMap();
        
        for (int sub = 0; sub < population.subpops.size(); sub++)
            generationSize += population.subpops.get(sub).initialSize;

        if (numEvaluations > UNDEFINED && numEvaluations < generationSize)
            output.fatal("Number of evaluations desired is smaller than the initial population of individuals");

        // INITIALIZE CONTACTS -- done after initialization to allow
        // a hook for the user to do things in Initializer before
        // an attempt is made to connect to island models etc.
        exchanger.initializeContacts(this);
        evaluator.initializeContacts(this);
        }


    public int evolve()
        {
        if (generationBoundary && generation > 0)
            {
            output.message("Generation " + generation +"\tEvaluations So Far " + evaluations);
            statistics.generationBoundaryStatistics(this); 
            }
                
        if (firstTime) 
            {
            if (statistics instanceof SteadyStateStatisticsForm)
                ((SteadyStateStatisticsForm)statistics).enteringInitialPopulationStatistics(this);
            statistics.postInitializationStatistics(this); 
            ((SteadyStateBreeder)breeder).prepareToBreed(this, 0); // unthreaded 
            ((SteadyStateEvaluator)evaluator).prepareToEvaluate(this, 0); // unthreaded 
            firstTime=false; 
            } 
                
        whichSubpop = (whichSubpop+1)% population.subpops.size();  // round robin selection
                
        // is the current subpop full? 
        boolean partiallyFullSubpop = (population.subpops.get(whichSubpop).individuals.size() < population.subpops.get(whichSubpop).initialSize);
  
        // MAIN EVOLVE LOOP 
        if (((SteadyStateEvaluator) evaluator).canEvaluate())   // are we ready to evaluate? 
            {
            Individual ind=null; 
            int numDuplicateRetries = population.subpops.get(whichSubpop).numDuplicateRetries;

            for (int tries=0; tries <= numDuplicateRetries; tries++)  // see Subpopulation
                { 
                if ( partiallyFullSubpop )   // is population full?
                    {
                    ind = population.subpops.get(whichSubpop).species.newIndividual(this, 0);  // unthreaded
                    }
                else  
                    { 
                    ind = ((SteadyStateBreeder)breeder).breedIndividual(this, whichSubpop,0); 
                    statistics.individualsBredStatistics(this, new Individual[]{ind}); 
                    }
                                
                if (numDuplicateRetries >= 1)  
                    { 
                    Object o = individualHash[whichSubpop].get(ind); 
                    if (o == null) 
                        { 
                        individualHash[whichSubpop].put(ind, ind); 
                        break; 
                        }
                    }
                } // tried to cut down the duplicates 
                        
            // evaluate the new individual
            ((SteadyStateEvaluator)evaluator).evaluateIndividual(this, ind, whichSubpop);
            }
        
        Individual ind = ((SteadyStateEvaluator)evaluator).getNextEvaluatedIndividual(this);
        int whichIndIndex = -1;
        int whichSubpop = -1;
        if (ind != null)   // do we have an evaluated individual? 
            {
            // COMPUTE GENERATION BOUNDARY
            generationBoundary = (evaluations % generationSize == 0);
            
            if (generationBoundary)
                {
                statistics.preEvaluationStatistics(this);
                }

            int subpop = ((SteadyStateEvaluator)evaluator).getSubpopulationOfEvaluatedIndividual(); 
            whichSubpop = subpop;
                                             
            if ( partiallyFullSubpop ) // is subpopulation full? 
                {  
                population.subpops.get(subpop).individuals.add(ind);
                                
                // STATISTICS FOR GENERATION ZERO 
                if (population.subpops.get(subpop).individuals.size() == population.subpops.get(subpop).initialSize)
                    if (statistics instanceof SteadyStateStatisticsForm)
                        ((SteadyStateStatisticsForm)statistics).enteringSteadyStateStatistics(subpop, this); 
                }
            else 
                { 
                // mark individual for death 
                int deadIndividualIndex = ((SteadyStateBreeder)breeder).deselectors[subpop].produce(subpop,this,0);
                Individual deadInd = population.subpops.get(subpop).individuals.get(deadIndividualIndex);
                
                // maybe replace dead individual with new individual
                if (ind.fitness.betterThan(deadInd.fitness) ||         // it's better, we want it
                    random[0].nextDouble() < replacementProbability)      // it's not better but maybe we replace it directly anyway
                    {
                    population.subpops.get(subpop).individuals.set(deadIndividualIndex, ind);
                    whichIndIndex = deadIndividualIndex;
                    }
                                
                // update duplicate hash table 
                individualHash[subpop].remove(deadInd); 
                                
                if (statistics instanceof SteadyStateStatisticsForm) 
                    ((SteadyStateStatisticsForm)statistics).individualsEvaluatedStatistics(this, 
                        new Individual[]{ind}, new Individual[]{deadInd}, new int[]{subpop}, new int[]{deadIndividualIndex}); 
                }
                                                
            if (generationBoundary)
                {
                statistics.postEvaluationStatistics(this);
                }
            }
        else
            {
            generationBoundary = false; 
            }

        // SHOULD WE QUIT?
        if (!partiallyFullSubpop && 
            ind != null &&
            ((SteadyStateEvaluator)evaluator).isIdealFitness(this, ind) && 
            quitOnRunComplete)
            { 
            output.message("Individual " + whichIndIndex + " of subpopulation " + whichSubpop + " has an ideal fitness."); 
            finishEvaluationStatistics();
            return R_SUCCESS;
            }
        
        if (evaluator.runComplete != null)
            {
            output.message(evaluator.runComplete);
            finishEvaluationStatistics();
            return R_SUCCESS; 
            }
        
        if ((generationBoundary && numGenerations != UNDEFINED && generation >= numGenerations - 1) ||
            (numEvaluations != UNDEFINED && evaluations >= numEvaluations))
            {
            finishEvaluationStatistics();
            return R_FAILURE;
            }
            
        if (generationBoundary)
            {
            // INCREMENT GENERATION AND CHECKPOINT
            generation++;

            // PRE-BREEDING EXCHANGING
            statistics.prePreBreedingExchangeStatistics(this);
            population = exchanger.preBreedingExchangePopulation(this);
            statistics.postPreBreedingExchangeStatistics(this);
            String exchangerWantsToShutdown = exchanger.runComplete(this);
            if (exchangerWantsToShutdown!=null)
                { 
                output.message(exchangerWantsToShutdown); 
                finishEvaluationStatistics();
                return R_SUCCESS;
                }
                        
            // POST-BREEDING EXCHANGING
            statistics.prePostBreedingExchangeStatistics(this);
            population = exchanger.postBreedingExchangePopulation(this);
            statistics.postPostBreedingExchangeStatistics(this);
            }

        if (checkpoint && generationBoundary && (generation - 1) % checkpointModulo == 0) 
            {
            output.message("Checkpointing");
            statistics.preCheckpointStatistics(this);
            Checkpoint.setCheckpoint(this);
            statistics.postCheckpointStatistics(this);
            }

        return R_NOTDONE;
        }
        
    public void finishEvaluationStatistics()
        {
        boolean generationBoundary = (evaluations % generationSize == 0);
        if (!generationBoundary)
            {
            statistics.postEvaluationStatistics(this);
            output.message("Generation " + generation + " Was Partial");
            }
        }

    /**
     * @param result
     */
    public void finish(int result)
        {
        output.message("Total Evaluations " + evaluations);
        ((SteadyStateBreeder)breeder).finishPipelines(this);
        statistics.finalStatistics(this,result);
        finisher.finishPopulation(this,result);
        exchanger.closeContacts(this,result);
        evaluator.closeContacts(this,result);
        }
    }
