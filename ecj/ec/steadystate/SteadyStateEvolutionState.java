/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.steadystate;
import ec.*;
import ec.util.Parameter;
import ec.util.Checkpoint;

/* 
 * SteadyStateEvolutionState.java
 * 
 * Created: Tue Aug 10 22:14:46 1999
 * By: Sean Luke
 */

/**
 * A SteadyStateEvolutionState is an EvolutionState which implements a simple
 * form of steady-state evolution.
 *
 * <p>First, all the individuals in the population are created and evaluated.
 * <b>(A)</b> Then 1 individual is selected by the breder for removal from the
 * population.  They are replaced by the result of breeding the other
 * individuals in the population.  Then just those newly-bred individuals are
 * evaluted.  Goto <b>(A)</b>.
 *
 * <p>The individual selected for removal is done so by calling the <i>deselector</i>
 * for that subpopulation.  Deselectors are stored and defined in SteadyStateBreeder.
 *
 * <p>A generation is defined as the interval in which N indidivuals are
 * evaluated, where N is set to the size of subpopulation 0.  SteadyStateEvolutioState
 * keeps track of both generations and number of evaluations performed so far.
 * 
 * <p>Exchanges and checkpointing occur each generation.
 *
 * <p>Evolution stops in any of the following conditions:
 * <ul><li> An ideal individual is found (if quitOnRunComplete is set to true)
 * <li> The number of generations has been exceeded (if evaluations is not set)
 * <li> The number of evaluations has been exceeded (if evaluations is set)
 * </ul>

 <p><b>Additional constraints:</b>
 <ul>
 <li> The breeder must be SteadyStateBreeder, or a subclass of it.
 <li> All breeding sources (pipelines, selection methods) must adhere to SteadyStateBSourceForm.
 <li> The breeder's deselectors must adhere to SteadyStateBSourceForm.
 <li> The evaluator must be a SteadyStateEvaluator, or a subclass of it.
 <li> The exchanger must be of SteadyStateExchangerForm.
 </ul>
 
 <p>If your statistics object adheres to SteadyStateStatisticsForm, it will receive
 additional statistics as specified in that form.  If it does <i>not</i> adhere to this
 form, you should be aware that the evaluation and breeding statistics hooks are called
 oddly: they wrap the evaluation of the first individual of a generation and the
 subsequent breeding of the population immediately thereafter.  Thus because it's
 steady-state and fine-grained, a lot of evaluations and breedings occur for which the
 statistics hooks are NOT called.  Furthermore, exchanges only occur on the generation
 boundary, and are wrapped with statistics hooks there.
 
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><tt>breed</tt><br>
 <font size=-1>classname, inherits or = ec.steadystate.SteadyStateBreeder</font></td>
 <td valign=top>(the class for breeder)</td></tr>
 <tr><td valign=top><tt>eval</tt><br>
 <font size=-1>classname, inherits or = ec.steadystate.SteadyStateEvaluator</font></td>
 <td valign=top>(the class for evaluator)</td></tr>
 <tr><td valign=top><tt>evaluations</tt><br>
 <font size=-1>int >= 1</font> or undefined (the default)</td>
 <td valign=top>Number of evaluations to perform.  If this parameter is undefined, number of generations is used instead</td></tr>
 </table>


 * @author Sean Luke
 * @version 1.0 
 */

public class SteadyStateEvolutionState extends EvolutionState
    {
    /** base parameter for steady-state */
    public static final String P_STEADYSTATE = "steady";
    public static final String P_NUMEVALUATIONS = "evaluations";

    /** The breeder puts the index of the newly-bred individuals in this
        array for the Evaluator to find them, one per subpopulation */
    public int newIndividuals[];

    /** Did we just start a new generation? */
    public boolean generationBoundary;
    /** Is this the first time the population is being evaluated, and so the *entire* population must be evaluated? */
    public boolean firstTimeAround;
    /** How many evaluations should we run for?  If set to UNDEFINED (0), we run for the number of generations instead. */
    public long numEvaluations;
    public static long UNDEFINED = 0;
    /** how big is a generation? Set to the size of subpopulation 0 of the initial population. */
    public int generationSize;
    /** How many evaluations have we run so far? */
    public long evaluations;
    
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
    public void startFresh() {
        output.message("Setting up");
        setup(this,null);  // a garbage Parameter

        // POPULATION INITIALIZATION
        output.message("Initializing Generation 0");
        statistics.preInitializationStatistics(this);
        population = initializer.initialPopulation(this, 0);  // unthreaded
        statistics.postInitializationStatistics(this);

        // INITIALIZE VARIABLES
        if (numEvaluations > 0 && numEvaluations < population.subpops[0].individuals.length)
            output.fatal("Number of evaluations desired is smaller than the initial population of individuals");
        else newIndividuals = new int[population.subpops.length];
        generationSize = population.subpops[0].individuals.length;
        generationBoundary = (evaluations % generationSize == 0);
        firstTimeAround=true;

        // INITIALIZE CONTACTS -- done after initialization to allow
        // a hook for the user to do things in Initializer before
        // an attempt is made to connect to island models etc.
        exchanger.initializeContacts(this);
        evaluator.initializeContacts(this);
    }


    /** Performs the evolutionary run.  Garbage collection and checkpointing are done only once every <i>generation</i> evaluations.  The only Statistics calls made are preInitializationStatistics(), postInitializationStatistics(), occasional postEvaluationStatistics (done once every <i>generation</i> evaluations), and finalStatistics(). */

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
        evaluator.evaluatePopulation(this);
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
