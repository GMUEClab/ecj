package ec.app.singular;



import ec.*;
import ec.eda.dovs.*;
import ec.simple.*;
import ec.util.*;
import ec.vector.*;

public class Singular extends Problem implements SimpleProblemForm
    {
    public static final String P_SINGULAR = "singular";

    public Parameter defaultBase()
        {
        return super.defaultBase().push(P_SINGULAR);
        }

    public void evaluate(final EvolutionState state, final Individual ind, final int subpopulation, final int threadnum)
        {

        if (!(ind instanceof IntegerVectorIndividual))
            // TODO : the output text may need to change
            state.output.fatal("Whoa!  It's not an IntegerVectorIndividual!!!", null);

        int[] genome = ((IntegerVectorIndividual) ind).genome;
        if (genome.length != 4)
            // TODO : the output text may need to change
            state.output.fatal("Whoa! The size of the genome is not right!!!", null);
        
        double sum = 1 + (genome[0] + 10 * genome[1]) * (genome[0] + 10 * genome[1])
            + 5 * (genome[2] - genome[3]) * (genome[2] - genome[3])
            + Math.pow(((double) (genome[1] - 2 * genome[2])), 4.0)
            + 10 * Math.pow(((double) (genome[0] - genome[3])), 4.0);
        
        
        // We return g as the fitness, as opposed in original code, where -g is return.
        // Since we are try to maximize our fitness value, not find a min -g solution
        ((DOVSFitness) ind.fitness).recordObservation(state, -sum);

        ind.evaluated = true;
        }

    }

