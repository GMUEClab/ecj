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
    private List<PheremoneMatrix> pheremoneMatrix;

    public int getNumAnts() { return numAnts; }
    
    public PheremoneMatrix getPheremoneMatrix(final int i)
    {
        assert(i >= 0);
        assert(i < pheremoneMatrix.size());
        return pheremoneMatrix.get(i).clone(); // Defensive copy
    }
    
    @Override
    public void setup(final EvolutionState state, final Parameter base)
    {
        assert(state != null);
        assert(base != null);
        numAnts = state.parameters.getInt(base.push(P_NUM_ANTS), null, 0);
        constructionRule = (ConstructionRule) state.parameters.getInstanceForParameter(base.push(P_CONSTRUCTION_RULE), null, ConstructionRule.class);
        updateRule = (UpdateRule) state.parameters.getInstanceForParameter(base.push(P_UPDATE_RULE), null, UpdateRule.class);
        // TODO initialize pheremones
        //assert(repOK());
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Population breedPopulation(final EvolutionState state)
    {
        assert(state != null);
        if (!(state.evaluator.p_problem instanceof ConstructiveProblemForm))
            {
            state.output.fatal(String.format("Attempted to use %s with problem %s, but %s can only be used with a %s.", this.getClass().getSimpleName(), state.evaluator.p_problem.getClass().getSimpleName(), this.getClass().getSimpleName(), ConstructiveProblemForm.class.getSimpleName()));
            }
            
        final Population newPop = state.population.emptyClone();
        for (int i = 0; i < state.population.subpops.size(); i++)
            {
            final int numAnts = state.population.subpops.get(i).individuals.size();
            breedSubpopulation(state.population.subpops.get(i), newPop.subpops.get(i), numAnts, pheremoneMatrix.get(i), (ConstructiveProblemForm) state.evaluator.p_problem);
            }
        
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void breedSubpopulation(final Subpopulation oldSubpop, final Subpopulation newSubpop, final int numAnts, final PheremoneMatrix pheremones, final ConstructiveProblemForm problem)
    {
        assert(newSubpop != null);
        assert(numAnts > 0);
        assert(pheremones != null);
            
        updateRule.updatePheremoneMatrix(pheremones, oldSubpop); // Update pheremone matrix
        
        // Execute ants
        for (int i = 0; i < numAnts; i++)
            {
            newSubpop.individuals.add(constructionRule.constructSolution(pheremones, problem));
            }
    }
}
