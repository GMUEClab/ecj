/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.steadystate;
import ec.simple.*;

import java.util.ArrayList;

import ec.*;
import ec.util.*;

/* 
 * SteadyStateBreeder.java
 * 
 */

/**
 * This subclass of Breeder performs the evaluation portion of Steady-State Evolution and (in distributed form)
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
 * <p>The determination of how an individual is marked for death is done by the SteadyStateBreeder.  This is
 * a SelectionMethod.  Note that this SelectionMethod probably should <i>not</i> be selecting for the "fittest"
 * individuals, but rather for either random individuals (the standard approach) or for "bad" individuals.
 * 
 
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><tt>deselector</tt><br>
 <font size=-1>classname, inherits and != ec.SelectionMethod</font></td>
 <td valign=top>(The SelectionMethod used to pick individuals for death)</td></tr>
 </table>

 * @author Sean Luke
 * @version 1.0 
 */

public class SteadyStateBreeder extends SimpleBreeder
    {
    private static final long serialVersionUID = 1;

    /** If st.firstTimeAround, this acts exactly like SimpleBreeder.
        Else, it only breeds one new individual per subpopulation, to 
        place in position 0 of the subpopulation.  
    */
    BreedingSource[] bp;
    
    public static final String P_DESELECTOR = "deselector";
    // public static final String P_RETRIES = "duplicate-retries";
        
    /** Loaded during the first iteration of breedPopulation */
    SelectionMethod deselectors[];
        
    /** Do we allow duplicates? */
    // public int numDuplicateRetries;
    
    public SteadyStateBreeder() { bp = null; deselectors = null; }
        
    public void setup(final EvolutionState state, final Parameter base) 
        {
        super.setup(state,base);
                
        if (!clonePipelineAndPopulation)
            state.output.fatal("clonePipelineAndPopulation must be true for SteadyStateBreeder -- we'll use only one Pipeline anyway.");

        Parameter p = new Parameter(Initializer.P_POP).push(Population.P_SIZE);
        int size = state.parameters.getInt(p,null,1);  
        
        // if size is wrong, we'll let Population complain about it  -- for us, we'll just make 0-sized arrays and drop out.
        if (size > 0)
            deselectors = new SelectionMethod[size];
        
        // load the deselectors
        for(int x=0;x<deselectors.length;x++)
            {
            deselectors[x] = (SelectionMethod)(
                state.parameters.getInstanceForParameter(
                    SteadyStateDefaults.base().push(P_DESELECTOR).push(""+x),null,SelectionMethod.class));
            if (!(deselectors[x] instanceof SteadyStateBSourceForm))
                state.output.error("Deselector for subpopulation " + x + " is not of SteadyStateBSourceForm.");
            deselectors[x].setup(state,SteadyStateDefaults.base().push(P_DESELECTOR).push(""+x));
            }
        state.output.exitIfErrors();
        
        if (sequentialBreeding) // uh oh
            state.output.fatal("SteadyStateBreeder does not support sequential evaluation.",
                base.push(P_SEQUENTIAL_BREEDING));


        // How often do we retry if we find a duplicate?
        /*
          numDuplicateRetries = state.parameters.getInt(
          SteadyStateDefaults.base().push(P_RETRIES),null,0);
          if (numDuplicateRetries < 0) state.output.fatal(
          "The number of retries for duplicates must be an integer >= 0.\n",
          base.push(P_RETRIES),null);
        */
        }
        
    /** Called to check to see if the breeding sources are correct -- if you
        use this method, you must call state.output.exitIfErrors() immediately 
        afterwards. */
    public void sourcesAreProperForm(final SteadyStateEvolutionState state,
        final BreedingSource[] breedingSources)
        {
        for(int x=0;x<breedingSources.length;x++)
            {
            // all breeding pipelines are SteadyStateBSourceForm
            //if (!(breedingSources[x] instanceof SteadyStateBSourceForm))
            //    state.output.error("Breeding Pipeline of subpopulation " + x + " is not of SteadyStateBSourceForm");
            ((SteadyStateBSourceForm)(breedingSources[x])).sourcesAreProperForm(state);
            }
        }
    
    /** Called whenever individuals have been replaced by new
        individuals in the population. */
    public void individualReplaced(final SteadyStateEvolutionState state,
        final int subpopulation,
        final int thread,
        final int individual)
        {
        for(int x=0;x<bp.length;x++)
            ((SteadyStateBSourceForm)bp[x]).
                individualReplaced(state,subpopulation,thread,individual);
        // let the deselector know
        ((SteadyStateBSourceForm)deselectors[subpopulation]).individualReplaced(state,subpopulation,thread,individual);
        }
        
    public void finishPipelines(EvolutionState state)
        {
        for(int x = 0 ; x < deselectors.length; x++)
            {
            bp[x].finishProducing(state,x,0);
            deselectors[x].finishProducing(state,x,0);
            }
        }
        
    public void prepareToBreed(EvolutionState state, int thread)
        {
        final SteadyStateEvolutionState st = (SteadyStateEvolutionState) state;
        // set up the breeding pipelines
        bp = new BreedingSource[st.population.subpops.size()];
        for(int pop=0;pop<bp.length;pop++)
            {
            bp[pop] = (BreedingSource) st.population.subpops.get(pop).species.pipe_prototype.clone();
            if (!bp[pop].produces(st,st.population,pop,0))
                st.output.error("The Breeding Source of subpopulation " + pop + " does not produce individuals of the expected species " + st.population.subpops.get(pop).species.getClass().getName() + " and with the expected Fitness class " + st.population.subpops.get(pop).species.f_prototype.getClass().getName());
            bp[pop].fillStubs(state, null);
            }
        // are they of the proper form?
        sourcesAreProperForm(st,bp);
        // because I promised when calling sourcesAreProperForm
        st.output.exitIfErrors();
                
        // warm them up
        for(int pop=0;pop<bp.length;pop++)
            {
            bp[pop].prepareToProduce(state,pop,0);
            deselectors[pop].prepareToProduce(state,pop,0);
            }
        }
        
    public Individual breedIndividual(final EvolutionState state, int subpop, int thread)
        {
        // this is inefficient but whatever...
        
        ArrayList<Individual> newind = new ArrayList<Individual>();
        // breed a single individual 
        bp[subpop].produce(1,1,subpop,newind, state,thread, state.population.subpops.get(subpop).species.buildMisc(state, subpop, thread));
        return newind.get(0); 
        }
    }
