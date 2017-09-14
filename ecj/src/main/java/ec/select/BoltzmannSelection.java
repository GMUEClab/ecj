/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.select;
import ec.util.*;
import ec.*;

/* 
 * BoltzmannSelection.java
 * 
 * Created: Thu May 14 2009
 * By: Jack Compton
 */

/**
 * Similar to FitProportionateSelection, but with a Simulated Annealing style twist. BoltzmannSelection picks individuals of a population in 
 * proportion to an adjusted version of their fitnesses instead of their actual fitnesses as returned by fitness(). The adjusted fitness is 
 * calculated by e^(fitness/current_temperature) where current_temperature is a temperature value that decreases by a constant cooling rate as 
 * generations of evolution pass. The current_temperature is calculated by starting-temperature - (cooling-rate * the_current_generation_number). 
 * When the temperature dips below 1.0, annealing ceases and BoltzmannSelection reverts to normal FitProportionateSelection behavior.
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
 <tr><td valign=top><i>base.</i><tt>starting-temperature</tt><br>
 <font size=-1>double = some large number (defaults to 1.0)</font></td>
 <td valign=top>(the starting temperature for our simulated annealing style adjusted fitness proportions)</td></tr>
 
 <tr><td valign=top><i>base.</i><tt>cooling-rate</tt><br>
 <font size=-1> double = some smaller number (defaults to 0.0 which causes BoltzmannSelection to behave just as FitProportionateSelection would)</font></td>
 <td valign=top>(how slow, or fast, do you want to cool the annealing fitness proportions?)</td></tr>
 
 </table> 

 <p><b>Default Base</b><br>
 select.boltzmann

 *
 * @author Jack Compton
 * @version 1.0 
 */

public class BoltzmannSelection extends FitProportionateSelection
    {
    /** Default base */
    public static final String P_BOLTZMANN = "boltzmann";
                
    /** Starting temperature parameter */
    public static final String P_STARTING_TEMPERATURE = "starting-temperature";
                
    /** Cooling rate parameter */
    public static final String P_COOLING_RATE = "cooling-rate";
                
    /** Starting temperature **/
    private double startingTemperature;
                
    /** Cooling rate */
    private double coolingRate;
                
    public Parameter defaultBase()
        {
        return SelectDefaults.base().push(P_BOLTZMANN);
        }
                
    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
                        
        Parameter def = defaultBase();
                        
        coolingRate = state.parameters.getDouble(base.push(P_COOLING_RATE),def.push(P_COOLING_RATE)); // default cooling rate of 1.0 per generation
        startingTemperature = state.parameters.getDouble(base.push(P_STARTING_TEMPERATURE),def.push(P_STARTING_TEMPERATURE)); // default starting temp is 0.0/completely cooled - will act as normal fit proportionate selection
                        
        if (coolingRate <= 0)
            {
            //Hey! you gotta cool! Set your cooling rate to a positive value!
            state.output.fatal("Cooling rate should be a positive value.",base.push(P_COOLING_RATE),def.push(P_COOLING_RATE));
            }
                        
        if ((startingTemperature - coolingRate) <= 0) {
            // C'mon, you should cool slowly if you want boltzmann selection to be effective.
            state.output.fatal("For best results, try to set your temperature to cool to 0 a more slowly. This can be acheived by increasing your starting-temperature and/or decreasing your cooling rate.\nstarting-temperatire/cooling-rate: " + startingTemperature + " / " + coolingRate);                             
            }
                        
        int total_generations = state.numGenerations;
        if (total_generations == 0)
            {
            //Load from parameter database!!
            state.output.fatal("Hey now, we gotta load the total_generations from the param DB");
            }
                        
        if ((startingTemperature - (coolingRate * total_generations)) > 0)
            {
            //Either your cooling rate is to low, or your starting temp is too high, because at this rate you will never cool to 0! (kind of essential to reaping the benefits of boltzmann selection)
            state.output.warning("If you want BoltzmannnSelection to be effective, your temperature should cool to 0 before all generations have passed. Make sure that (starting-temperature - (cooling-rate * generations)) <= 0.");
            }
        
        }

    // completely override FitProportionateSelection.prepareToProduce
    public void prepareToProduce(final EvolutionState s,
        final int subpopulation,
        final int thread)
        {
        super.prepareToProduce(s, subpopulation, thread);

        // load fitnesses
        fitnesses = new double[s.population.subpops.get(subpopulation).individuals.size()];
        for(int x=0;x<fitnesses.length;x++)
            {
            fitnesses[x] = (double) boltzmannExpectedValue(
                ((Individual)(s.population.subpops.get(subpopulation).individuals.get(x))).fitness.fitness(),
                s); // adjust the fitness proportion according to current temperature.
            if (fitnesses[x] < 0) // uh oh
                s.output.fatal("Discovered a negative fitness value.  BoltzmannnSelection requires that all fitness values be non-negative(offending subpopulation #" + subpopulation + ")");
            }
        
        // organize the distribution.  All zeros in fitness is fine
        RandomChoice.organizeDistribution(fitnesses, true);
        }

    double boltzmannExpectedValue(double fitness, final EvolutionState s)
        {
        double current_temperature = startingTemperature - (coolingRate * s.generation);
        if (current_temperature < 1.0)
            return fitness;
        return Math.exp(fitness/current_temperature);
        }
        
    }
