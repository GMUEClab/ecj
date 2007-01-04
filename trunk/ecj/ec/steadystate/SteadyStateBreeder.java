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

/* 
 * SteadyStateBreeder.java
 * 
 * Created: Tue Aug 10 21:00:11 1999
 * By: Sean Luke
 */

/**
 * A SteadyStateBreeder is an extension of SimpleBreeder which works in conjunction
 * with SteadyStateEvolutionState to breed individuals using a steady-state breeding
 * method.
 *
 * <p>SteadyStateBreeder marks 1 individual for death in each
 * subpopulation.  It then replaces those individuals in a subpopulation
 * with new individuals bred from the rest of the subpopulation.
 *
 * <p>The selection method used to determine which individual to mark for death is called
 * the <i>deselector</i>.  There is one deselector for each subpopulation.
 *
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><tt>steady.deselector.<i>n</i></tt><br>
 <font size=-1>classname, inherits from ec.select.SelectionMethod, adheres to ec.steadystate.SteadyStateBSourceForm</font></td>
 <td valign=top>The deselector for subpopulation <i>n</i> </td></tr>
 <tr><td valign=top><i>base</i>.<tt>duplicate-retries</tt><br>
 <font size=-1>int &gt;= 0</font></td>
 <td valign=top>(during breeding, when we produce an individual which already exists in the subpopulation, the number of times we try to replace it with something unique.)</td></tr>
 </table>
 *
 * @author Sean Luke
 * @version 1.0 
 */

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
    
    /** Hashed version of population for duplicate-retries.  Should this be in
        the EvolutionState maybe? */
    public HashMap[] populationHash;

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


    public Population breedPopulation(EvolutionState state) 
        {
        final SteadyStateEvolutionState st = (SteadyStateEvolutionState) state;

        if (st.firstTimeAround) // first time
            {
            super.breedPopulation(st);
            
            // Load the hash tables with the initial population
            if (numDuplicateRetries > 0)  // we're checking for duplicates
                {
                populationHash = new HashMap[st.population.subpops.length];
                for(int subpop=0;subpop<st.population.subpops.length;subpop++)
                    {
                    populationHash[subpop] = new HashMap();
                    for(int x=0;x<st.population.subpops[subpop].individuals.length;x++)
                        changeHash(state.population.subpops[subpop].individuals,subpop,x,1);
                    }
                }
            
            // Load my steady-state breeding pipelines
            
            if (bp == null)
                {
                // set up the breeding pipelines
                bp = new BreedingPipeline[st.population.subpops.length];
                for(int pop=0;pop<bp.length;pop++)
                    {
                    bp[pop] = (BreedingPipeline)st.population.subpops[pop].species.
                        pipe_prototype.clone();
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
            }

        // yes, yes, this is after creating bp, so it's less efficient,
        // but safer because the sourcesAreProperForm() check is done before this


        // mark individuals for death

        for (int pop = 0; pop<st.population.subpops.length;pop++)
            {
            st.newIndividuals[pop] = deselectors[pop].produce(pop,st,0);
            }

        // create new individuals

        Individual[] newind = new Individual[1];
        for(int pop=0;pop < st.population.subpops.length;pop++)
            {
            // make sure we don't make any duplicates
            
            for(int tries=0;tries <= /* yes, I see that */ numDuplicateRetries; tries++)
                {
                bp[pop].produce(1,1,0,pop,newind,st,0);
                if (tries < numDuplicateRetries && // only bother testing if you're not the last try
                    !populationHash[pop].containsKey(newind[0]))  // no duplicate exists in pop
                    break;
                }
            
            // at this point we have a valid individual
            
            if (numDuplicateRetries > 0)
                {
                // remove dead individual from hash
                changeHash(st.population.subpops[pop].individuals,pop,st.newIndividuals[pop],-1);
                // add new individual to hash
                changeHash(newind,pop,0,1);
                }
                
            // individual enters population
            st.population.subpops[pop].individuals[st.newIndividuals[pop]] = newind[0];
            }
        
        return st.population;
        }
    
    // adds increment to the hashed integer stored in 
    // populationHash[pop].get(pop.subpops[pop].individuals[index])
    // if no one's there, the individual is added with the increment.
    // if someone's there and the resulting value gets changed to 0,
    // the individual is removed.  increment should be non-zero for
    // this to work right.
    void changeHash(Individual[] inds, int subpop, int index, int increment)
        {
        Individual ind = inds[index];
        Integer val = (Integer) (populationHash[subpop].get(ind));
        if (val==null)  // not there, add him
            populationHash[subpop].put(ind,new Integer(1));
        else 
            {
            int i = ((Integer)val).intValue();
            if (i == -increment)  // will go to 0 upon applying increment
                populationHash[subpop].remove(ind);
            else
                {
                populationHash[subpop].put(ind, new Integer(i+increment));
                }
            }
        }
    
    public void finishPipelines(EvolutionState state)
        {
        for(int x = 0 ; x < bp.length; x++)
            {
            bp[x].finishProducing(state,x,0);
            deselectors[x].finishProducing(state,x,0);
            }
        }
    }
