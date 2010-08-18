/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.steadystate;
import ec.*;
import ec.util.Parameter;
import ec.util.Checkpoint;
import ec.util.Output;
import ec.simple.*;
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
 </table>
 
 *
 * @author Sean Luke
 * @version 1.0 
 */


public class SteadyStateEvolutionState extends EvolutionState
    {
    /** base parameter for steady-state */
    public static final String P_NUMEVALUATIONS = "evaluations";
        
    /** Did we just start a new generation? */
    public boolean generationBoundary;
    /** How many evaluations should we run for?  If set to UNDEFINED (0), we run for the number of generations instead. */
    public long numEvaluations;
    public static long UNDEFINED = 0;
    /** how big is a generation? Set to the size of subpopulation 0 of the initial population. */
    public int generationSize;
    /** How many evaluations have we run so far? */
    public long evaluations;
        
    /** How many individuals have we added to the initial population? */ 
    int[] individualCount; 
        
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
        
        checkStatistics(state, statistics, base);
        
        numEvaluations = parameters.getLong(new Parameter(P_NUMEVALUATIONS),null,1);
        if (numEvaluations == 0)
            output.message("Number of evaluations not defined; using number of generations");
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
        if (numEvaluations > 0 && numEvaluations < population.subpops[0].individuals.length)
            output.fatal("Number of evaluations desired is smaller than the initial population of individuals");
        generationSize = 0;
        generationBoundary = false;
        firstTime = true; 
        evaluations=0; 
        whichSubpop=-1; 
                
        individualHash = new HashMap[population.subpops.length];
        for(int i=0;i<population.subpops.length; i++) individualHash[i] = new HashMap();
                
        individualCount = new int[population.subpops.length];
        for (int sub=0; sub < population.subpops.length; sub++)  
            { 
            individualCount[sub]=0;
            generationSize += population.subpops[sub].individuals.length;  // so our sum total 'generationSize' will be the initial total number of individuals
            }

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
            output.message("Generation " + generation +"\tEvaluations " + evaluations);
            statistics.generationBoundaryStatistics(this); 
            statistics.postEvaluationStatistics(this); 
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
                
        whichSubpop = (whichSubpop+1)%population.subpops.length;  // round robin selection
                
        // is the current subpop full? 
        boolean partiallyFullSubpop = (individualCount[whichSubpop] < population.subpops[whichSubpop].individuals.length);  
                
        // MAIN EVOLVE LOOP 
        if (((SteadyStateEvaluator) evaluator).canEvaluate())   // are we ready to evaluate? 
            {
            Individual ind=null; 
            int numDuplicateRetries = population.subpops[whichSubpop].numDuplicateRetries; 

            for (int tries=0; tries <= numDuplicateRetries; tries++)  // see Subpopulation
                { 
                if ( partiallyFullSubpop )   // is population full?
                    {
                    ind = population.subpops[whichSubpop].species.newIndividual(this, 0);  // unthreaded 
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
        
        Individual ind = ((SteadyStateEvaluator)evaluator).getNextEvaluatedIndividual();
        if (ind != null)   // do we have an evaluated individual? 
            {
            int subpop = ((SteadyStateEvaluator)evaluator).getSubpopulationOfEvaluatedIndividual(); 
                                                
            if ( partiallyFullSubpop ) // is subpopulation full? 
                {  
                population.subpops[subpop].individuals[individualCount[subpop]++]=ind; 
                                
                // STATISTICS FOR GENERATION ZERO 
                if ( individualCount[subpop] == population.subpops[subpop].individuals.length ) 
                    if (statistics instanceof SteadyStateStatisticsForm)
                        ((SteadyStateStatisticsForm)statistics).enteringSteadyStateStatistics(subpop, this); 
                }
            else 
                { 
                // mark individual for death 
                int deadIndividual = ((SteadyStateBreeder)breeder).deselectors[subpop].produce(subpop,this,0);
                Individual deadInd = population.subpops[subpop].individuals[deadIndividual];
                                
                // replace dead individual with new individual 
                population.subpops[subpop].individuals[deadIndividual] = ind; 
                                
                // update duplicate hash table 
                individualHash[subpop].remove(deadInd); 
                                
                if (statistics instanceof SteadyStateStatisticsForm) 
                    ((SteadyStateStatisticsForm)statistics).individualsEvaluatedStatistics(this, 
                        new Individual[]{ind}, new Individual[]{deadInd}, new int[]{subpop}, new int[]{deadIndividual}); 
                }
                                                
            // INCREMENT NUMBER OF COMPLETED EVALUATIONS
            evaluations++;
            
            // COMPUTE GENERATION BOUNDARY
            generationBoundary = (evaluations % generationSize == 0);
            }
        else
            {
            generationBoundary = false; 
            }

        // SHOULD WE QUIT?
        if (!partiallyFullSubpop && evaluator.runComplete(this) && quitOnRunComplete)
            { 
            output.message("Found Ideal Individual"); 
            return R_SUCCESS;
            }
                
        if ((numEvaluations > 0 && evaluations >= numEvaluations) ||  // using numEvaluations
            (numEvaluations <= 0 && generationBoundary && generation == numGenerations -1))  // not using numEvaluations
            {
            return R_FAILURE;
            }
                
                
        // EXCHANGING
        if (generationBoundary)
            {
            // PRE-BREED EXCHANGE 
            statistics.prePreBreedingExchangeStatistics(this);
            population = exchanger.preBreedingExchangePopulation(this);
            statistics.postPreBreedingExchangeStatistics(this);
            String exchangerWantsToShutdown = exchanger.runComplete(this);
            if (exchangerWantsToShutdown!=null)
                { 
                output.message(exchangerWantsToShutdown); 
                return R_SUCCESS;
                }
                        
            // POST BREED EXCHANGE
            statistics.prePostBreedingExchangeStatistics(this);
            population = exchanger.postBreedingExchangePopulation(this);
            statistics.postPostBreedingExchangeStatistics(this);
                        
            // INCREMENT GENERATION AND CHECKPOINT
            generation++;
            if (checkpoint && generation%checkpointModulo == 0) 
                {
                output.message("Checkpointing");
                statistics.preCheckpointStatistics(this);
                Checkpoint.setCheckpoint(this);
                statistics.postCheckpointStatistics(this);
                }
            }
        return R_NOTDONE;
        }
        
    /**
     * @param result
     */
    public void finish(int result)
        {
        /* finish up -- we completed. */
        ((SteadyStateBreeder)breeder).finishPipelines(this);
        statistics.finalStatistics(this,result);
        finisher.finishPopulation(this,result);
        exchanger.closeContacts(this,result);
        evaluator.closeContacts(this,result);
        }
    }
