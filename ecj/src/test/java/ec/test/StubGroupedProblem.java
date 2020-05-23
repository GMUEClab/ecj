package ec.test;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.Problem;
import ec.coevolve.GroupedProblemForm;
import ec.simple.SimpleFitness;
import ec.vector.DoubleVectorIndividual;

/** A trivial GroupedProblemForm that assigns some fitness values to the batch of individuals you feed it.*/
public class StubGroupedProblem extends Problem implements GroupedProblemForm
{
    private static final long serialVersionUID = 1L;

    @Override
    public void preprocessPopulation(EvolutionState state, Population pop, boolean[] prepareForFitnessAssessment, boolean countVictoriesOnly)
        {
        throw new UnsupportedOperationException();
        }

    @Override
    public int postprocessPopulation(EvolutionState state, Population pop, boolean[] assessFitness, boolean countVictoriesOnly)
        {
        throw new UnsupportedOperationException();
        }

    /**
     * Set the fitness of the ith individual to the sum of i and its first gene.
     */
    @Override
    public void evaluate(EvolutionState state, Individual[] ind, boolean[] updateFitness, boolean countVictoriesOnly, int[] subpops, int threadnum)
        {
        for (int i = 0; i < ind.length; i++)
            {
            final double first = ((DoubleVectorIndividual) ind[i]).genome[0];
            ((SimpleFitness) ind[i].fitness).setFitness(state, first, false);
            ind[i].evaluated = true;
            }
        }

}