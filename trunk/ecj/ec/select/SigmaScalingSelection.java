/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.select;
import ec.util.*;
import ec.*;

/* 
 * SigmaScalingSelection.java
 * 
 * Created: Fri Jun 5 2009
 * By: Jack Compton
 */

/**
 * Similar to FitProportionateSelection, but with adjustments to scale up/exaggerate differences in fitness for selection when true fitness values are very close to 
 * eachother across the population. This addreses a common problem with FitProportionateSelection wherein selection approaches random selection during 
 * late runs when fitness values do not differ by much.
 *
 * <p>
 * Like FitProportionateSelection this is not appropriate for steady-state evolution.
 * If you're not familiar with the relative advantages of 
 * selection methods and just want a good one,
 * use TournamentSelection instead. Not appropriate for
 * multiobjective fitnesses.
 *
 * <p><b><font color=red>
 * Note: Fitnesses must be non-negative.  0 is assumed to be the worst fitness.
 * </font></b>

 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 Always 1.
 
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base.</i><tt>scaled-fitness-floor</tt><br>
 <font size=-1>double = some small number (defaults to 0.1)</font></td>
 <td valign=top>(The sigma scaling formula sometimes returns negative values. This is unacceptable for fitness proportionate style selection so we must substitute 
 the fitnessFloor (some value >= 0) for the sigma scaled fitness when that sigma scaled fitness <= fitnessFloor.)</td></tr>
 </table> 
 

 <p><b>Default Base</b><br>
 select.sigma-scaling

 *
 * @author Jack Compton
 * @version 1.0 
 */

public class SigmaScalingSelection extends FitProportionateSelection
    {
    /** Default base */
    public static final String P_SIGMA_SCALING = "sigma-scaling";
                
    /** Scaled fitness floor */
    // Used as a cut-off point when negative valued scaled fitnesses are encountered (negative fitness values are not compatible with fitness proportionate style selection methods)
    public static final String P_SCALED_FITNESS_FLOOR = "scaled-fitness-floor";     
                
    /** Floor for sigma scaled fitnesses **/
    double fitnessFloor;
                
    public Parameter defaultBase()
        {
        return SelectDefaults.base().push(P_SIGMA_SCALING);
        }
        
    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
                        
        Parameter def = defaultBase();
                        
        fitnessFloor = state.parameters.getDoubleWithDefault(base.push(P_SCALED_FITNESS_FLOOR),def.push(P_SCALED_FITNESS_FLOOR),0.1); // default scaled fitness floor of 0.1 according to Tanese (1989)
                        
        if (fitnessFloor < 0)
            {
            //Hey! you gotta cool!  Set your cooling rate to a positive value!
            state.output.fatal("The scaled-fitness-floor must be a non-negative value.",base.push(P_SCALED_FITNESS_FLOOR),def.push(P_SCALED_FITNESS_FLOOR));
            }
        }
                
    // completely override FitProportionateSelection.prepareToProduce
    public void prepareToProduce(final EvolutionState s,
        final int subpopulation,
        final int thread)
        {
        // load fitnesses
        fitnesses = new double[s.population.subpops[subpopulation].individuals.length];
        
        double sigma;
        double meanFitness;
        double meanSum = 0;
        double squaredDeviationsSum = 0;
                
        for(int x=0;x<fitnesses.length;x++)
            {
            fitnesses[x] = ((Individual)(s.population.subpops[subpopulation].individuals[x])).fitness.fitness();
            if (fitnesses[x] < 0) // uh oh
                s.output.fatal("Discovered a negative fitness value.  SigmaScalingSelection requires that all fitness values be non-negative(offending subpopulation #" + subpopulation + ")");
            }
                        
        // Calculate meanFitness
        for(int x=0;x<fitnesses.length;x++)
            {
            meanSum = meanSum + fitnesses[x];
            }
        meanFitness = meanSum/fitnesses.length;
                        
        // Calculate sum of squared deviations
        for(int x=0;x<fitnesses.length;x++)
            {
            squaredDeviationsSum = squaredDeviationsSum + Math.pow(fitnesses[x]-meanFitness,2);
            }
        sigma = Math.sqrt(squaredDeviationsSum/(fitnesses.length-1));
                
        // Fill fitnesses[] with sigma scaled fitness values
        for(int x=0;x<fitnesses.length;x++)
            {
            fitnesses[x] = (double)sigmaScaledValue(fitnesses[x], meanFitness, sigma, s); // adjust the fitness proportion according to sigma scaling.
                                
            // Sigma scaling formula can return negative values, this is unacceptable for fitness proportionate style selection...
            // so we must substitute the fitnessFloor (some value >= 0) when a sigma scaled fitness <= fitnessFloor is encountered.
            if (fitnesses[x] < fitnessFloor)  
                fitnesses[x] = fitnessFloor; 
            }
        
        // organize the distribution.  All zeros in fitness is fine
        RandomChoice.organizeDistribution(fitnesses, true);
        }

    private double sigmaScaledValue(double fitness, double meanFitness, double sigma, final EvolutionState s)
        {
        if (sigma != 0)
            return 1+(fitness-meanFitness)/(2*sigma);
        return 1.0;
        }
        
    }
