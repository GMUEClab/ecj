/*
 Copyright 2017 by Sean Luke
 Licensed under the Academic Free License version 3.0
 See the file "LICENSE" for more information
 */
package ec.co.ant;

import ec.Breeder;
import ec.EvolutionState;
import ec.Population;
import ec.Subpopulation;
import ec.co.ConstructiveProblemForm;
import ec.util.Parameter;
import ec.vector.IntegerVectorIndividual;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Eric O. Scott
 */
public class AntBreeder extends Breeder
{
    public final static String P_UPDATE_RULE = "updateRule";
    public final static String P_CONSTRUCTION_RULE = "constructionRule";
    public final static String P_NUM_ANTS = "numAnts";

    private int numAnts;
    private ConstructionRule constructionRule;
    private UpdateRule updateRule;
    private List<PheromoneMatrix> pheromoneMatrix;

    public int getNumAnts() { return numAnts; }
    
    public PheromoneMatrix getPheremoneMatrix(final int i)
    {
        assert(i >= 0);
        assert(i < pheromoneMatrix.size());
        return pheromoneMatrix.get(i).clone(); // Defensive copy
    }
    
    @Override
    public void setup(final EvolutionState state, final Parameter base)
    {
        assert(state != null);
        assert(base != null);
        numAnts = state.parameters.getInt(base.push(P_NUM_ANTS), null, 0);
        constructionRule = (ConstructionRule) state.parameters.getInstanceForParameter(base.push(P_CONSTRUCTION_RULE), null, ConstructionRule.class);
        updateRule = (UpdateRule) state.parameters.getInstanceForParameter(base.push(P_UPDATE_RULE), null, UpdateRule.class);
        pheromoneMatrix = initPheremones(((ConstructiveProblemForm)state.evaluator.p_problem));
        assert(repOK());
    }
    
    private static List<PheromoneMatrix> initPheremones(final ConstructiveProblemForm problem)
    {
        assert(problem != null);
        final int dimensions = problem.numComponents();
        final List<PheromoneMatrix> matrices = new ArrayList<PheromoneMatrix>();
        matrices.add(new PheromoneMatrix(dimensions));
        return matrices;
    }

    @Override
    public Population breedPopulation(final EvolutionState state)
    {
        assert(state != null);
        if (!(state.evaluator.p_problem instanceof ConstructiveProblemForm))
            {
            state.output.fatal(String.format("Attempted to use %s with problem %s, but %s can only be used with a %s.", this.getClass().getSimpleName(), state.evaluator.p_problem.getClass().getSimpleName(), this.getClass().getSimpleName(), ConstructiveProblemForm.class.getSimpleName()));
            }
        final ConstructiveProblemForm problem = (ConstructiveProblemForm) state.evaluator.p_problem;
            
        final Population newPop = state.population.emptyClone();
        for (int i = 0; i < state.population.subpops.size(); i++)
            {
            final Subpopulation oldSubpop = state.population.subpops.get(i);
            final int numAnts = oldSubpop.individuals.size();
            updateRule.updatePheremoneMatrix(pheromoneMatrix.get(i), oldSubpop); // Update pheremone matrix
        
            // Execute ants
            for (int j = 0; j < numAnts; j++)
                {
                final IntegerVectorIndividual newInd = constructionRule.constructSolution(state, i, startNode, pheromoneMatrix.get(i), problem);
                newPop.subpops.get(i).individuals.add(newInd);
                }
            }
        
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void breedSubpopulation(final Subpopulation oldSubpop, final Subpopulation newSubpop, final int numAnts, final PheromoneMatrix pheromones, final ConstructiveProblemForm problem)
    {
        assert(newSubpop != null);
        assert(numAnts > 0);
        assert(pheromones != null);
            
        
    }
    
    /** Representation invariant, used for verification.
     * 
     * @return true if the class is found to be in an erroneous state.
     */
    public final boolean repOK()
    {
        return P_UPDATE_RULE != null
                && !P_UPDATE_RULE.isEmpty()
                && P_CONSTRUCTION_RULE != null
                && !P_CONSTRUCTION_RULE.isEmpty()
                && P_NUM_ANTS != null
                && !P_NUM_ANTS.isEmpty()
                && numAnts > 0
                && constructionRule != null
                && updateRule != null
                && pheromoneMatrix != null
                && !containsNulls(pheromoneMatrix);
    }
    
    private static boolean containsNulls(final Collection c) {
        assert(c != null);
        for (final Object o : c)
            if (o == null)
                return true;
        return false;
    }
}
