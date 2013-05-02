package ec.pso;

import ec.* ;
import ec.util.* ;
import ec.vector.* ;


/*
 * PSOBreeder.java
 * Created: Thu May  2 17:09:40 EDT 2013
 */

/**
 * PSOBreeder is a simple single-threaded Breeder which performs 
 * Particle Swarm Optimization using the Particle class as individuals. 
 * PSOBreeder relies on a number of parameters which define weights for
 * various vectors computed during Particle Swarm Optimization, plus
 * a few flags:
 *
 * <ul>
 * <li> Neighborhoods for particles have a size S determined by the parameter neighborhood-size.  It's best if S were even.
 * <li> Neighborhoods for particles are constructed in one of three ways:
 * <ul>
 * <li> random: pick S informants randomly without replacement within the subpopulation, not including the particle itself, once at the beginning of the run.
 * <li> random-each-time: pick S informants randomly without replacement within the subpopulation, not including the particle itself, every single generation.
 * <li> toroidal: pick the floor(S/2) informants to the left of the particle's location within the subpopulation and the ceiling(S/2) informants to the right of the particle's location in the subpopulation, once at the beginning of the run.
 * </ul>
 * <li> To this you can add the particle itself to the neighborhood, with include-self. 
 * <li> The basic velocity update equation is VELOCITY <-- (VELOCITY * velocity-coefficent) + (VECTOR-TO-GLOBAL-BEST * global-coefficient) + (VECTOR-TO-NEIGHBORHOOD-BEST * informant-coefficient) + (VECTOR-TO-PERSONAL-BEST * personal-coefficient)
 * <li> The basic particle update equation is PARTICLE <-- PARTICLE + VELOCITY
 * </ul>
 *
 * <p>
 * <b>Parameters</b><br>
 * <table>
 * <tr>
 * <td valign=top><i>base</i>.<tt>velocity-coefficient</tt><br>
 *  <font size=-1>float &ge; 0</font></td>
 *  <td valign=top>(The weight for the velocity)</td>
 * </tr><tr>
 * <td valign=top><i>base</i>.<tt>personal-coefficient</tt><br>
 *  <font size=-1>float &ge; 0</font></td>
 *  <td valign=top>(The weight for the personal-best vector)</td>
 * </tr><tr>
 * <td valign=top><i>base</i>.<tt>informant-coefficient</tt><br>
 *  <font size=-1>float &ge; 0</font></td>
 *  <td valign=top>(The weight for the neighborhood/informant-best vector)</td>
 * </tr><tr>
 * <td valign=top><i>base</i>.<tt>global-coefficient</tt><br>
 *  <font size=-1>float &ge; 0</font></td>
 *  <td valign=top>(The weight for the global-best vector)</td>
 * </tr><tr>
 * <td valign=top><i>base</i>.<tt>neighborhood-size</tt><br>
 *  <font size=-1>int &gt; 0</font></td>
 *  <td valign=top>(The size of the neighborhood of informants, not including the particle)</td>
 * </tr><tr>
 * <td valign=top><i>base</i>.<tt>neighborhood-style</tt><br>
 *  <font size=-1>String, one of: random toroidal random-each-time</font></td>
 *  <td valign=top>(The method of generating the neighborhood of informants, not including the particle)</td>
 * </tr><tr>
 * <td valign=top><i>base</i>.<tt>include-self</tt><br>
 *  <font size=-1>true or false (default)</font></td>
 *  <td valign=top>(Whether to include the particle itself as a member of the neighborhood after building the neighborhood)</td>
 * </tr>
 *
 * </table>
 *
 * @author Khaled Ahsan Talukder
 */


public class PSOBreeder extends Breeder
    {
    public static final int C_NEIGHBORHOOD_RANDOM = 0;
    public static final int C_NEIGHBORHOOD_TOROIDAL = 1;
    public static final int C_NEIGHBORHOOD_RANDOM_EACH_TIME = 2;

    public static final String P_VELOCITY_COEFFICIENT = "velocity-coefficient" ;
    public static final String P_PERSONAL_COEFFICIENT = "personal-coefficient" ;
    public static final String P_INFORMANT_COEFFICIENT = "informant-coefficient" ;
    public static final String P_GLOBAL_COEFFICIENT = "global-coefficient" ;
    public static final String P_INCLUDE_SELF = "include-self" ;
    public static final String P_NEIGHBORHOOD = "neighborhood-style" ;
    public static final String P_NEIGHBORHOOD_SIZE = "neighborhood-size" ;
    public static final String V_NEIGHBORHOOD_RANDOM = "random";
    public static final String V_NEIGHBORHOOD_TOROIDAL = "toroidal";
    public static final String V_NEIGHBORHOOD_RANDOM_EACH_TIME = "random-each-time";

    public int neighborhood = C_NEIGHBORHOOD_RANDOM;        // default neighborhood scheme
    public double velCoeff = 0.5 ;          //  coefficient for the velocity
    public double personalCoeff = 0.5 ;             //  coefficient for self
    public double informantCoeff = 0.5 ;            //  coefficient for informants/neighbours
    public double globalCoeff = 0.5 ;               //  coefficient for global best, this is not done in the standard PSO
    public int neighborhoodSize = 3 ; 
    public boolean includeSelf = false;         

    public double[][] globalBest = null ; // one for each subpopulation
    public Fitness[] globalBestFitness = null;

    public void setup(final EvolutionState state, final Parameter base)
        {
        velCoeff = state.parameters.getDouble(base.push(P_VELOCITY_COEFFICIENT),null,0.0);
        if ( velCoeff < 0.0 )
            state.output.fatal( "Parameter not found, or its value is less than 0.", base.push(P_VELOCITY_COEFFICIENT), null );

        personalCoeff = state.parameters.getDouble(base.push(P_PERSONAL_COEFFICIENT),null,0.0);
        if ( personalCoeff < 0.0)
            state.output.fatal( "Parameter not found, or its value is less than 0.", base.push(P_PERSONAL_COEFFICIENT), null );

        informantCoeff = state.parameters.getDouble(base.push(P_INFORMANT_COEFFICIENT),null,0.0);
        if ( informantCoeff < 0.0)
            state.output.fatal( "Parameter not found, or its value is less than 0.", base.push(P_INFORMANT_COEFFICIENT), null );

        globalCoeff = state.parameters.getDouble(base.push(P_GLOBAL_COEFFICIENT),null,0.0);
        if ( globalCoeff < 0.0 )
            state.output.fatal( "Parameter not found, or its value is less than 0.", base.push(P_GLOBAL_COEFFICIENT), null );

        neighborhoodSize = state.parameters.getInt(base.push(P_NEIGHBORHOOD_SIZE), null, 1);               
        if (neighborhoodSize <= 0 )
            state.output.fatal("Neighbourhood size must be a value >= 1.", base.push(P_NEIGHBORHOOD_SIZE), null);
                        
        String sch = state.parameters.getString(base.push(P_NEIGHBORHOOD), null);
        if (V_NEIGHBORHOOD_RANDOM.equals(sch))
            {
            neighborhood = C_NEIGHBORHOOD_RANDOM; // default anyway
            }
        else if (V_NEIGHBORHOOD_TOROIDAL.equals(sch))
            {
            neighborhood = C_NEIGHBORHOOD_TOROIDAL;
            }
        else if (V_NEIGHBORHOOD_RANDOM_EACH_TIME.equals(sch))
            {
            neighborhood = C_NEIGHBORHOOD_RANDOM_EACH_TIME;
            }
        else state.output.fatal( "Neighborhood style must be either 'random', 'toroidal', or 'random-each-time'.", base.push(P_NEIGHBORHOOD), null );

        includeSelf = state.parameters.getBoolean(base.push(P_INCLUDE_SELF), null, false);               
        }

    public Population breedPopulation(EvolutionState state)
        {
        // initialize the global best
        if (globalBest == null)
            {
            globalBest = new double[state.population.subpops.length][];
            globalBestFitness = new Fitness[state.population.subpops.length];
            }
                
        // update global best, neighborhood best, and personal best 
        for(int subpop = 0 ; subpop < state.population.subpops.length ; subpop++)
            {
            for(int ind = 0 ; ind < state.population.subpops[subpop].individuals.length ; ind++)
                {
                if (globalBestFitness[subpop] == null ||
                    state.population.subpops[subpop].individuals[ind].fitness.betterThan(globalBestFitness[subpop]))
                    {
                    globalBest[subpop] = ((DoubleVectorIndividual)state.population.subpops[subpop].individuals[ind]).genome;
                    globalBestFitness[subpop] = state.population.subpops[subpop].individuals[ind].fitness;
                    }
                ((Particle)state.population.subpops[subpop].individuals[ind]).update(state, subpop, ind, 0);
                }
            // clone global best
            globalBest[subpop] = (double[])(globalBest[subpop].clone());
            globalBestFitness[subpop] = (Fitness)(globalBestFitness[subpop].clone());
            }


        // now move the particles
        for(int subpop = 0 ; subpop < state.population.subpops.length ; subpop++)
            {
            for(int ind = 0 ; ind < state.population.subpops[subpop].individuals.length ; ind++)
                // tweak in place, destructively
                ((Particle)state.population.subpops[subpop].individuals[ind]).tweak(state, globalBest[subpop],
                    velCoeff, personalCoeff, informantCoeff, globalCoeff, 0);
            }
                
        // we return the same population
        return state.population ;
        }
    }

