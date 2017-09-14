/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.parsimony;

import ec.*;
import ec.util.*;

/**
   This Statistics subclass implements Poli's "Tarpeian" method of parsimony control, whereby some
   <i>kill-proportion</i> of above-average-sized individuals in each subpopulation have their fitnesses
   set to a very bad value, and marks them as already evaluated (so the Evaluator can skip them).  
   The specific individuals in this proportion is determined at random.
        
   <p>Different Fitnesses have different meanings of the word "bad".  At present, we set the fitness
   to -Double.MAX_VALUE if it's a SimpleFitness, and set it to Double.MAX_VALUE if it's a KozaFitnesss.
   If it's any other kind of Fitness, an error is reported.  You can override the "bad-setter" function
   setMinimumFitness(...) to make other kinds of fitness bad in different ways.  In the future we may
   revisit how to set Fitnesses to "bad" in a more general way if this becomes an issue.
        
   <p>Tarpeian is implemented as a Statistics.  Why?  Because we need to mark individuals as evaluated
   prior to the Evaluator getting to them, and also need to keep track of the total proportion marked
   as such.  We considered doing this as a SelectionMethod, as a BreedingPipeline, as a Breeder, and
   as an Evaluator.  None are good options really -- Evaluator is the best approach but it means we
   have special Tarpeian Evaluators, so it's no longer orthogonal with other Evaluators.  Eventually
   we settled on the one object which has the right hooks and can be easily stuck onto the system without
   modifying anything in a special-purpose way: a Statistics object.
        
   <p>All you need to do is add TarpeianStatistics as a child to your existing Statistics chain.  If you
   have one existing Statistics, then you just add the parameters <tt>stat.num-children=1</tt> and
   <tt>stat.child.0=ec.parsimony.TarpeianStatistics</tt>  You'll also need to specify the kill proportion
   (for example, <tt>stat.child.0.kill-proportion=0.2</tt> )
        
   <p><b>Parameters</b><br>
   <table>
   <tr><td valign=top><i>base</i>.<tt>kill-proportion</tt><br>
   <font size=-1>0 &lt; int &lt; 1</font></td>
   <td valign=top>(proportion of above-average-sized individuals killed)</td></tr>
   </table>

*/
 
public class TarpeianStatistics extends Statistics
    {
    /** one in n individuals are killed */
    public static final String P_KILL_PROPORTION = "kill-proportion";
    double killProportion;

    public void setup( final EvolutionState state, final Parameter base )
        {
        super.setup (state, base);

        killProportion = state.parameters.getDouble( base.push(P_KILL_PROPORTION), null, 0.0 );
        if( killProportion < 0 || killProportion > 1 )
            state.output.fatal( "Parameter not found, or it has an invalid value (<0 or >1).", base.push(P_KILL_PROPORTION) );
        }

    /**
       Marks a proportion (killProportion) of individuals with above-average size (within their own subpopulation) to a minimum value.
    */
    public void preEvaluationStatistics(final EvolutionState state)
        {
        for(int subpopulation = 0; subpopulation < state.population.subpops.size(); subpopulation++ )
            {
            double averageSize = 0;

            for(int i = 0; i < state.population.subpops.get(subpopulation).individuals.size() ; i++ )
                averageSize += state.population.subpops.get(subpopulation).individuals.get(i).size();

            averageSize /= state.population.subpops.get(subpopulation).individuals.size();

            for(int i = 0; i < state.population.subpops.get(subpopulation).individuals.size() ; i++ )
                {
                if( ( state.population.subpops.get(subpopulation).individuals.get(i).size() > averageSize ) &&
                    ( state.random[0].nextDouble() < killProportion ) )
                    {
                    Individual ind = state.population.subpops.get(subpopulation).individuals.get(i);
                    setMinimumFitness( state, subpopulation, ind );
                    ind.evaluated = true;
                    }
                }
            }
        }

    /**
       Sets the fitness of an individual to the minimum fitness possible.
       If the fitness is of type ec.simple.SimpleFitness, that minimum value is -Double.MAX_VALUE;
       If the fitness is of type ec.gp.koza.KozaFitness, that minimum value is Double.MAX_VALUE;
       Else, a fatal error is reported.

       You need to override this method if you're using any other type of fitness.
    */
    public void setMinimumFitness( final EvolutionState state, int subpopulation, Individual ind )
        {
        Fitness fitness = ind.fitness;
        if( fitness instanceof ec.gp.koza.KozaFitness )
            ((ec.gp.koza.KozaFitness)fitness).setStandardizedFitness( state, Double.MAX_VALUE );
        else if( fitness instanceof ec.simple.SimpleFitness )
            ((ec.simple.SimpleFitness)fitness).setFitness(state,-Double.MAX_VALUE,false);
        else
            state.output.fatal( "TarpeianStatistics only accepts individuals with fitness of type ec.simple.SimpleFitness or ec.gp.koza.KozaFitness." );
        }

    }
