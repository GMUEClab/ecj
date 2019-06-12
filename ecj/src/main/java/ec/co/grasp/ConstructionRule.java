package ec.co.grasp;

import ec.EvolutionState;
import ec.Setup;
import ec.co.ConstructiveIndividual;

public interface ConstructionRule extends Setup
    {
    public abstract ConstructiveIndividual constructSolution(EvolutionState state, ConstructiveIndividual ind, int thread);
    }
