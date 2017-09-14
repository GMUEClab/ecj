/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.rule.breed;

import ec.rule.*;
import ec.*;
import ec.util.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/* 
 * RuleCrossoverPipeline.java
 * 
 * Created: Tue Mar 13 15:03:12 EST 2001
 * By: Sean Luke
 */


/**
 *
 RuleCrossoverPipeline is a BreedingPipeline which implements a simple default crossover
 for RuleIndividuals.  Normally it takes two individuals and returns two crossed-over 
 child individuals.  Optionally, it can take two individuals, cross them over, but throw
 away the second child (a one-child crossover).  RuleCrossoverPipeline works by iteratively taking rulesets
 from each individual, and migrating rules from either to the other with a certain
 per-rule probability.  Rule crossover preserves the min and max rule restrictions.
  
 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 1 or 2

 <p><b>Number of Sources</b><br>
 2

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>toss</tt><br>
 <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</font>/td>
 <td valign=top>(after crossing over with the first new individual, should its second sibling individual be thrown away instead of adding it to the population?)</td></tr>
 <tr><td valign=top><i>base</i>.<tt>prob</tt><br>
 <font size=-1>0.0 &lt;= double &lt; 1.0, or 0.5 (default)</font>/td>
 <td valign=top>(probability that a rule will cross over from one individual to the other)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 rule.xover

 * @author Sean Luke
 * @version 1.0
 */

public class RuleCrossoverPipeline extends BreedingPipeline
    {
    public static final String P_TOSS = "toss";
    public static final String P_CROSSOVER = "xover";
    public static final String P_CROSSOVERPROB = "crossover-prob";
    public static final int INDS_PRODUCED = 2;
    public static final int NUM_SOURCES = 2;
    public static final String KEY_PARENTS = "parents";

    /** Should the pipeline discard the second parent after crossing over? */
    public boolean tossSecondParent;
    
    /** What is the probability of a rule migrating? */
    public double ruleCrossProbability;

    /** Temporary holding place for parents */
    ArrayList<Individual> parents;

    public RuleCrossoverPipeline() 
        {
        // by Ermo. get rid of asList
        //parents = new ArrayList<Individual>(Arrays.asList(new RuleIndividual[2]));
        parents = new ArrayList<Individual>();
        }
    public Parameter defaultBase() { return RuleDefaults.base().push(P_CROSSOVER); }

    /** Returns 2 */
    public int numSources() { return NUM_SOURCES; }

    public Object clone()
        {
        RuleCrossoverPipeline c = (RuleCrossoverPipeline)(super.clone());

        // deep-cloned stuff
        c.parents = new ArrayList<Individual>(parents);

        return c;
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        Parameter def = defaultBase();
        tossSecondParent = state.parameters.getBoolean(base.push(P_TOSS),
            def.push(P_TOSS),false);
        ruleCrossProbability = state.parameters.getDoubleWithDefault(base.push(P_CROSSOVERPROB),
            def.push(P_CROSSOVERPROB),0.5f);
        if (ruleCrossProbability > 1.0 || ruleCrossProbability < 0.0) 
            state.output.fatal("Rule cross probability must be between 0 and 1",base.push(P_CROSSOVERPROB),
                def.push(P_CROSSOVERPROB));
        }
        
    /** Returns 2 (unless tossing the second sibling, in which case it returns 1) */
    public int typicalIndsProduced() { return (tossSecondParent? 1: INDS_PRODUCED); }

    public int produce(final int min,
        final int max,
        final int subpopulation,
        final ArrayList<Individual> inds,
        final EvolutionState state,
        final int thread, HashMap<String, Object> misc)

        {
        int start = inds.size();
        
        // how many individuals should we make?
        int n = (tossSecondParent? 1 : INDS_PRODUCED);
        if (n < min) n = min;
        if (n > max) n = max;

        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            {
            // just load from source 0 and clone 'em
            sources[0].produce(n,n,subpopulation,inds, state,thread,misc);
            return n;
            }


        IntBag[] parentparents = null;
        IntBag[] preserveParents = null;
        if (misc!=null && misc.get(KEY_PARENTS) != null)
            {
            preserveParents = (IntBag[])misc.get(KEY_PARENTS);
            parentparents = new IntBag[2];
            misc.put(KEY_PARENTS, parentparents);
            }

        RuleInitializer initializer = ((RuleInitializer)state.initializer);
    
        for(int q=start;q<n+start; /* no increment */)  // keep on going until we're filled up
            {
            parents.clear();
            
            // grab two individuals from our sources
            if (sources[0]==sources[1])  // grab from the same source
                {
                sources[0].produce(2,2,subpopulation, parents, state,thread, misc);
                }
            else // grab from different sources
                {
                sources[0].produce(1,1,subpopulation, parents, state,thread, misc);
                sources[1].produce(1,1,subpopulation, parents, state,thread, misc);
                }

            // at this point, parents[] contains our two selected individuals,
            // AND they're copied so we own them and can make whatever modifications
            // we like on them.

            // so we'll cross them over now.

            ((RuleIndividual) parents.get(0)).preprocessIndividual(state,thread);
            ((RuleIndividual) parents.get(1)).preprocessIndividual(state,thread);

            if( ((RuleIndividual) parents.get(0)).rulesets.length != ((RuleIndividual) parents.get(1)).rulesets.length )
                {
                state.output.fatal( "The number of rule sets should be identical in both parents ( " +
                    ((RuleIndividual) parents.get(0)).rulesets.length + " : " +
                    ((RuleIndividual) parents.get(1)).rulesets.length + " )." );
                }

            // for each set of rules (assume both individuals have the same number of rule sets)
            for(int x = 0; x < ((RuleIndividual) parents.get(0)).rulesets.length ; x++ )
                {
                RuleSet[] temp = new RuleSet[2];
                while(true)
                    {
                    // create two new rulesets (initially empty)
                    for( int i = 0 ; i < 2 ; i++ )
                        temp[i] = new RuleSet();
                    // split the ruleset indexed x in parent 1
                    temp = ((RuleIndividual) parents.get(0)).rulesets[x].splitIntoTwo( state, thread, temp,ruleCrossProbability);
                    // now temp[0] contains rules to that must go to parent[1]
                                        
                    // split the ruleset indexed x in parent 2 (append after the split results from previous operation)
                    temp = ((RuleIndividual) parents.get(1)).rulesets[x].splitIntoTwo( state, thread, temp, 1 - ruleCrossProbability);
                    // now temp[1] contains rules that must go to parent[0]
                    
                    // ensure that there are enough rules
                    if (temp[0].numRules >= ((RuleIndividual) parents.get(0)).rulesets[x].constraints(initializer).minSize &&
                        temp[0].numRules <= ((RuleIndividual) parents.get(0)).rulesets[x].constraints(initializer).maxSize &&
                        temp[1].numRules >= ((RuleIndividual) parents.get(1)).rulesets[x].constraints(initializer).minSize &&
                        temp[1].numRules <= ((RuleIndividual) parents.get(1)).rulesets[x].constraints(initializer).maxSize)
                        break;
                        
                    temp = new RuleSet[2];
                    }
                    
                // copy the results in the rulesets of the parents
                ((RuleIndividual) parents.get(0)).rulesets[x].copyNoClone(temp[1]);
                ((RuleIndividual) parents.get(1)).rulesets[x].copyNoClone(temp[0]);
                }
            
            ((RuleIndividual) parents.get(0)).postprocessIndividual(state,thread);
            ((RuleIndividual) parents.get(1)).postprocessIndividual(state,thread);
    
            ((RuleIndividual) parents.get(0)).evaluated=false;
            ((RuleIndividual) parents.get(1)).evaluated=false;
            
            // add 'em to the population
            // by Ermo. This is use add instead of set as inds could be empty
            // Yes -- Sean
            inds.add(parents.get(0));
            if (preserveParents != null)
                {
                parentparents[0].addAll(parentparents[1]);
                preserveParents[q] = parentparents[0];
                }
            q++;
            if (q<n+start && !tossSecondParent)
                {
                // by Ermo. same reason, see comments above
                inds.add(parents.get(1));
                if (preserveParents != null)
                    {
                    parentparents[0].addAll(parentparents[1]);
                    preserveParents[q] = parentparents[0];
                    }
                q++;
                }
            }
        return n;
        }
    }
    
    
    
    
    
    
    
