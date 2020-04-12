/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co.ant;

import ec.EvolutionState;
import ec.Fitness;
import ec.Individual;
import ec.Species;
import ec.Subpopulation;
import ec.co.ConstructiveIndividual;
import ec.util.Parameter;
import java.util.ArrayList;

/**
 * Coordinates the core behavior of Ant Colony Optimization algorithms.
 *
 * There are many kinds of ACO algorithms, but many of them can be defined in terms of a <code>PheromoneTable</code>,
 * a <code>ConstructionRule</code> (which reads pheromones to execute "ants"), and a local and/or global
 * <code>UpdateRule</code> (which alter pheromone values in the environment).
 *
 * <br/><br/>
 *
 * <code>AntSpecies</code> species these components for an algorithm and coordinates their execution.  ECJ's ACO
 * algorithms are controlled by the usual <code>EvolutionState</code> characters, which hand off control to
 * <code>Species</code> at appropriate points to generate solutions or perform pheromone updates.
 *
 * @author Eric O. Scott
 * @see ec.app.tsp
 * @see ec.app.knapsack
 */
public class AntSpecies extends Species
    {
    private static final long serialVersionUID = 1;

    public final static Parameter DEFAULT_BASE = new Parameter("constructive");
    public final static String SPECIES_NAME = "constructive-species";

    public final static String P_CONSTRUCTION_RULE = "construction-rule";
    public final static String P_PHEROMONE_TABLE = "pheromone-table";
    public final static String P_UPDATE_RULE = "update-rule";
    public final static String P_LOCAL_UPDATE_RULE = "local-update-rule";

    private ConstructionRule constructionRule;
    private PheromoneTable pheromones;
    private UpdateRule updateRule;
    private UpdateRule localUpdateRule;

    @Override
    public void setup(final EvolutionState state, final Parameter base)
        {
        setupSuper(state, base); // Calling a custom replacement for super.setup(), because Species.setup() looks for parameters that we don't need for ACO.
        assert(state != null);
        assert(base != null);
        constructionRule = (ConstructionRule) state.parameters.getInstanceForParameter(base.push(P_CONSTRUCTION_RULE), null, ConstructionRule.class);
        constructionRule.setup(state, base.push(P_CONSTRUCTION_RULE));

        pheromones = (PheromoneTable) state.parameters.getInstanceForParameter(base.push(P_PHEROMONE_TABLE), null, PheromoneTable.class);
        pheromones.setup(state, base.push(P_PHEROMONE_TABLE));

        if (state.parameters.exists(base.push(P_UPDATE_RULE), null))
            {
            updateRule = (UpdateRule) state.parameters.getInstanceForParameter(base.push(P_UPDATE_RULE), null, UpdateRule.class);
            updateRule.setup(state, base.push(P_UPDATE_RULE));
            }

        if (state.parameters.exists(base.push(P_LOCAL_UPDATE_RULE), null))
            {
            localUpdateRule = (UpdateRule) state.parameters.getInstanceForParameter(base.push(P_LOCAL_UPDATE_RULE), null, UpdateRule.class);
            localUpdateRule.setup(state, base.push(P_LOCAL_UPDATE_RULE));
            }
        assert(repOK());
        }

    /** A custom setup method for Species that skips the initialization of the
     * breeding pipeline.  We call this in place of super.setup(), since this
     * Species doesn't use a pipeline.
     */
    private void setupSuper(final EvolutionState state, final Parameter base)
        {
        assert(state != null);
        assert(base != null);
        Parameter def = defaultBase();
        // load our individual prototype
        i_prototype = (Individual)(state.parameters.getInstanceForParameter(
                base.push(P_INDIVIDUAL),def.push(P_INDIVIDUAL),
                Individual. class));
        // set the species to me before setting up the individual, so they know who I am
        i_prototype.species = this;
        i_prototype.setup(state,base.push(P_INDIVIDUAL));

        // load our fitness
        f_prototype = (Fitness) state.parameters.getInstanceForParameter(
            base.push(P_FITNESS),def.push(P_FITNESS),
            Fitness.class);
        f_prototype.setup(state,base.push(P_FITNESS));
        }

    /** Apply a global update rule to the pheromone table.
     *
     *  @param subpop The Subpopulation to use as the input of the global update. */
    @Override
    public void updateSubpopulation(final EvolutionState state, final Subpopulation subpop)
        {
        updateRule.updatePheromones(state, pheromones, subpop.individuals);
        assert(repOK());
        }

    /** Apply a local update rule to the pheromone table.
     *
     * @param ind The Individual to use as the input of the local update. */
    @Override
    public void updateIndividual(final EvolutionState state, final Individual ind)
        {
        if (localUpdateRule != null)
            {
            final ArrayList<Individual> inds = new ArrayList<>();
            inds.add(ind);
            localUpdateRule.updatePheromones(state, pheromones, inds);
            }
        }

    @Override
    public ConstructiveIndividual<?> newIndividual(final EvolutionState state, final int thread)
        {
        assert(state != null);
        assert(thread >= 0);

        final ConstructiveIndividual<?> ind = (ConstructiveIndividual<?>)(super.newIndividual(state, thread));
        assert(repOK());
        return constructionRule.constructSolution(state, ind, pheromones, thread);
        }

    @Override
    public Parameter defaultBase()
        {
        return DEFAULT_BASE.push(SPECIES_NAME);
        }

    /** Representation invariant, used for verification.
     *
     * @return true if the class is found to be in an erroneous state.
     */
    public final boolean repOK()
        {
        return DEFAULT_BASE != null
            && SPECIES_NAME != null
            && !SPECIES_NAME.isEmpty()
            && P_UPDATE_RULE != null
            && !P_UPDATE_RULE.isEmpty()
            && P_CONSTRUCTION_RULE != null
            && !P_CONSTRUCTION_RULE.isEmpty()
            && constructionRule != null
            && updateRule != null
            && pheromones != null;
        }
    }
