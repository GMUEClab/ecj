package ec.pso;

import ec.* ;
import ec.util.* ;
import ec.vector.* ;

public class PSOBreeder extends Breeder
    {
    public static final int C_NEIGHBORHOOD_RANDOM = 0;
    public static final int C_NEIGHBORHOOD_TOROIDAL = 1;

    public static final String P_VELOCITY_COEFFICIENT = "velocity-coefficient" ;
    public static final String P_PERSONAL_COEFFICIENT = "personal-coefficient" ;
    public static final String P_INFORMANT_COEFFICIENT = "informant-coefficient" ;
    public static final String P_GLOBAL_COEFFICIENT = "global-coefficient" ;
    public static final String P_NEIGHBORHOOD_SIZE = "neighborhood-size" ;
    public static final String P_NEIGHBORHOOD = "neighborhood-style" ;
    public static final String V_NEIGHBORHOOD_RANDOM = "random";
    public static final String V_NEIGHBORHOOD_TOROIDAL = "toroidal";

    public int neighborhood = C_NEIGHBORHOOD_RANDOM;        // default neighborhood scheme
    public double velCoeff = 0.5 ;          //  coefficient for the velocity
    public double personalCoeff = 0.5 ;             //  coefficient for self
    public double informantCoeff = 0.5 ;            //  coefficient for informants/neighbours
    public double globalCoeff = 0.5 ;               //  coefficient for global best, this is not done in the standard PSO
    public int neighborhoodSize = 3 ;          

    public double[][] globalBest = null ; // one for each subpopulation
    public Fitness[] globalBestFitness = null;

    public void setup(final EvolutionState state, final Parameter base)
        {
        velCoeff = state.parameters.getDouble(base.push(P_VELOCITY_COEFFICIENT),null,0.0);
        if ( velCoeff < 0.0 || velCoeff > 1.0 )
            state.output.fatal( "Parameter not found, or its value is outside of [0.0, 1.0].", base.push(P_VELOCITY_COEFFICIENT), null );

        personalCoeff = state.parameters.getDouble(base.push(P_PERSONAL_COEFFICIENT),null,0.0);
        if ( personalCoeff < 0.0 || personalCoeff > 1.0 )
            state.output.fatal( "Parameter not found, or its value is outside of [0.0, 1.0].", base.push(P_PERSONAL_COEFFICIENT), null );

        informantCoeff = state.parameters.getDouble(base.push(P_INFORMANT_COEFFICIENT),null,0.0);
        if ( informantCoeff < 0.0 || informantCoeff > 1.0 )
            state.output.fatal( "Parameter not found, or its value is outside of [0.0, 1.0].", base.push(P_INFORMANT_COEFFICIENT), null );

        globalCoeff = state.parameters.getDouble(base.push(P_GLOBAL_COEFFICIENT),null,0.0);
        if ( globalCoeff < 0.0 || globalCoeff > 1.0 )
            state.output.fatal( "Parameter not found, or its value is outside of [0.0, 1.0].", base.push(P_GLOBAL_COEFFICIENT), null );

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
        else state.output.fatal( "Neighborhood style must be either 'random' or 'toroidal'.", base.push(P_NEIGHBORHOOD), null );
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

