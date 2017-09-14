package ec.app.highdimension;

import ec.*;
import ec.eda.dovs.*;
import ec.simple.*;
import ec.util.*;
import ec.vector.*;

public class HighDimension extends Problem implements SimpleProblemForm
    {
    public static final String P_HIGHDIMENSION = "high-dimension";

    public Parameter defaultBase()
        {
        return super.defaultBase().push(P_HIGHDIMENSION);
        }

    public void evaluate(final EvolutionState state, final Individual ind, final int subpopulation, final int threadnum)
        {

        if (!(ind instanceof IntegerVectorIndividual))
            // TODO : the output text may need to change
            state.output.fatal("Whoa!  It's not an IntegerVectorIndividual!!!", null);

        int[] genome = ((IntegerVectorIndividual) ind).genome;
        if (genome.length != 5)
            // TODO : the output text may need to change
            state.output.fatal("Whoa! The size of the genome is not right!!!", null);
        
        double gamma = 1e-3;
        long xi= 0;
        double beta = 1e4;
        double g = 0;

        double sum = 0;
        for(int j = 0;j<genome.length;++j)
            {
            sum += (genome[j] - xi) * (genome[j] - xi) * gamma;
            }
        
        g = beta * Math.exp(-sum);
        
        double variance = g * 0.09;
        // TODO: how should we use noise?
        double noise = (variance < 1e-30) ? 0:state.random[0].nextGaussian()*variance;
        
        // We return g as the fitness, as opposed in original code, where -g is returned.
        // Since we are try to maximize our fitness value, not find a min -g solution
        ((DOVSFitness) ind.fitness).recordObservation(state, g);
        ind.evaluated = true;
        }

    }

