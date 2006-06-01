/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.rule.breed;

import ec.rule.*;
import ec.*;
import ec.util.*;

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
 away the second child (a one-child crossover).  RuleCrossoverPipeline works by calling
 defaultCrossover(...) on the first parent individual.
 
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
 <font size=-1>0.0 &lt;= float &lt; 1.0, or 0.5 (default)</font>/td>
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

    /** Should the pipeline discard the second parent after crossing over? */
    public boolean tossSecondParent;
    
    /** What is the probability of a rule migrating? */
    public float ruleCrossProbability;

    /** Temporary holding place for parents */
    RuleIndividual parents[];

    public RuleCrossoverPipeline() { parents = new RuleIndividual[2]; }
    public Parameter defaultBase() { return RuleDefaults.base().push(P_CROSSOVER); }

    /** Returns 2 */
    public int numSources() { return NUM_SOURCES; }

    public Object clone()
        {
        RuleCrossoverPipeline c = (RuleCrossoverPipeline)(super.clone());

        // deep-cloned stuff
        c.parents = (RuleIndividual[]) parents.clone();

        return c;
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        Parameter def = defaultBase();
        tossSecondParent = state.parameters.getBoolean(base.push(P_TOSS),
                                                       def.push(P_TOSS),false);
        ruleCrossProbability = state.parameters.getFloatWithDefault(base.push(P_CROSSOVERPROB),
                                                                    def.push(P_CROSSOVERPROB),0.5f);
        if (ruleCrossProbability > 1.0 || ruleCrossProbability < 0.0) 
            state.output.fatal("Rule cross probability must be between 0 and 1",base.push(P_CROSSOVERPROB),
                               def.push(P_CROSSOVERPROB));
        }
        
    /** Returns 2 (unless tossing the second sibling, in which case it returns 1) */
    public int typicalIndsProduced() { return (tossSecondParent? 1: INDS_PRODUCED); }

    public int produce(final int min, 
                       final int max, 
                       final int start,
                       final int subpopulation,
                       final Individual[] inds,
                       final EvolutionState state,
                       final int thread) 

        {
        // how many individuals should we make?
        int n = (tossSecondParent? 1 : INDS_PRODUCED);
        if (n < min) n = min;
        if (n > max) n = max;
        RuleInitializer initializer = ((RuleInitializer)state.initializer);
    
        for(int q=start;q<n+start; /* no increment */)  // keep on going until we're filled up
            {
            // grab two individuals from our sources
            if (sources[0]==sources[1])  // grab from the same source
                {
                sources[0].produce(2,2,0,subpopulation,parents,state,thread);
                if (!(sources[0] instanceof BreedingPipeline))  // it's a selection method probably
                    { 
                    parents[0] = (RuleIndividual)(parents[0].clone());
                    parents[1] = (RuleIndividual)(parents[1].clone());
                    }
                }
            else // grab from different sources
                {
                sources[0].produce(1,1,0,subpopulation,parents,state,thread);
                sources[1].produce(1,1,1,subpopulation,parents,state,thread);
                if (!(sources[0] instanceof BreedingPipeline))  // it's a selection method probably
                    parents[0] = (RuleIndividual)(parents[0].clone());
                if (!(sources[1] instanceof BreedingPipeline)) // it's a selection method probably
                    parents[1] = (RuleIndividual)(parents[1].clone());
                }

            // at this point, parents[] contains our two selected individuals,
            // AND they're copied so we own them and can make whatever modifications
            // we like on them.

            // so we'll cross them over now.

            parents[0].preprocessIndividual(state,thread);
            parents[1].preprocessIndividual(state,thread);

            if( parents[0].rulesets.length != parents[1].rulesets.length )
                {
                state.output.fatal( "The number of rule sets should be identical in both parents ( " +
                                    parents[0].rulesets.length + " : " +
                                    parents[1].rulesets.length + " )." );
                }

            // for each set of rules (assume both individuals have the same number of rule sets)
            for( int x = 0 ; x < parents[0].rulesets.length ; x++ )
                {
                RuleSet[] temp = new RuleSet[2];
                while(true)
                    {
                    // create two new rulesets (initially empty)
                    for( int i = 0 ; i < 2 ; i++ )
                        temp[i] = new RuleSet();
                    // split the ruleset indexed x in parent 1
                    temp = parents[0].rulesets[x].splitIntoTwo( state, thread, temp,ruleCrossProbability);
                    // split the ruleset indexed x in parent 2 (append after the splitted result from previous operation)
                    temp = parents[1].rulesets[x].splitIntoTwo( state, thread, temp, ruleCrossProbability);
                    
                    // ensure that there are enough rules
                    if (temp[0].numRules >= parents[0].rulesets[x].constraints(initializer).minSize &&
                        temp[0].numRules <= parents[0].rulesets[x].constraints(initializer).maxSize &&
                        temp[1].numRules >= parents[1].rulesets[x].constraints(initializer).minSize &&
                        temp[1].numRules <= parents[1].rulesets[x].constraints(initializer).maxSize)
                        break;
                        
                    temp = new RuleSet[2];
                    }
                    
                // copy the results in the rulesets of the parents
                parents[0].rulesets[x].copyNoClone(temp[0]);
                parents[1].rulesets[x].copyNoClone(temp[1]);
                }
            
            parents[0].postprocessIndividual(state,thread);
            parents[1].postprocessIndividual(state,thread);
    
            parents[0].evaluated=false;
            parents[1].evaluated=false;
            
            // add 'em to the population
            inds[q] = parents[0];
            q++;
            if (q<n+start && !tossSecondParent)
                {
                inds[q] = parents[1];
                q++;
                }
            }
        return n;
        }
    }
    
    
    
    
    
    
    
