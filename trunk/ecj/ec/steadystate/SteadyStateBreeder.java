/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.steadystate;
import ec.simple.*;
import ec.*;
import ec.util.*;
import java.util.*;

public class SteadyStateBreeder extends SimpleBreeder
    {
    /** If st.firstTimeAround, this acts exactly like SimpleBreeder.
        Else, it only breeds one new individual per subpopulation, to 
        place in position 0 of the subpopulation.  
    */
    BreedingPipeline[] bp;
    
    public static final String P_DESELECTOR = "deselector";
    public static final String P_RETRIES = "duplicate-retries";
        
    /** Loaded during the first iteration of breedPopulation */
    SelectionMethod deselectors[];
        
    /** Do we allow duplicates? */
    public int numDuplicateRetries;
    
    public SteadyStateBreeder() { bp = null; deselectors = null; }
        
    public void setup(final EvolutionState state, final Parameter base) 
        {
        super.setup(state,base);
                
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
        
        // How often do we retry if we find a duplicate?
        numDuplicateRetries = state.parameters.getInt(
            SteadyStateDefaults.base().push(P_RETRIES),null,0);
        if (numDuplicateRetries < 0) state.output.fatal(
            "The number of retries for duplicates must be an integer >= 0.\n",
            base.push(P_RETRIES),null);
                
        }
        
    /** Called to check to see if the breeding sources are correct -- if you
        use this method, you must call state.output.exitIfErrors() immediately 
        afterwards. */
    public void sourcesAreProperForm(final SteadyStateEvolutionState state,
        final BreedingPipeline[] breedingPipelines)
        {
        for(int x=0;x<breedingPipelines.length;x++)
            {
            if (!(breedingPipelines[x] instanceof SteadyStateBSourceForm))
                state.output.error("Breeding Pipeline of subpopulation " + x + " is not of SteadyStateBSourceForm");
            ((SteadyStateBSourceForm)(breedingPipelines[x])).sourcesAreProperForm(state);
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
        bp = new BreedingPipeline[st.population.subpops.length];
        for(int pop=0;pop<bp.length;pop++)
            {
            bp[pop] = (BreedingPipeline)st.population.subpops[pop].species.pipe_prototype.clone();
            if (!bp[pop].produces(st,st.population,pop,0))
                st.output.error("The Breeding Pipeline of subpopulation " + pop + " does not produce individuals of the expected species " + st.population.subpops[pop].species.getClass().getName() + " and with the expected Fitness class " + st.population.subpops[pop].species.f_prototype.getClass().getName());
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
        final SteadyStateEvolutionState st = (SteadyStateEvolutionState) state;
        Individual[] newind = new Individual[1]; 
                
        // breed a single individual 
        bp[subpop].produce(1,1,0,subpop,newind,state,thread);
        return newind[0]; 
        }
    }
