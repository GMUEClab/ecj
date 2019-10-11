package ec.co.grasp;

import ec.Species;
import ec.util.Parameter;
import ec.EvolutionState;
import ec.Fitness;
import ec.Individual;
import ec.co.ConstructiveIndividual;

public class GraspSpecies extends Species
    {
    public final static Parameter DEFAULT_BASE = new Parameter("constructive");
    public final static String SPECIES_NAME = "constructive-species";

    public final static String P_CONSTRUCTION_RULE = "construction-rule";

    private ConstructionRule constructionRule;

    @Override
    public void setup(final EvolutionState state, final Parameter base)
        {
        setupSuper(state, base); // Calling a custom replacement for super.setup(), because Species.setup() looks for parameters that we don't need for ACO.
        assert(state != null);
        assert(base != null);
        constructionRule = (ConstructionRule) state.parameters.getInstanceForParameter(base.push(P_CONSTRUCTION_RULE), null, ConstructionRule.class);
        constructionRule.setup(state, base.push(P_CONSTRUCTION_RULE));

        assert(repOK());
        }


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

    @Override
    public ConstructiveIndividual newIndividual(final EvolutionState state, final int thread)
        {
        assert(state != null);
        assert(thread >= 0);

        final ConstructiveIndividual ind = (ConstructiveIndividual)(super.newIndividual(state, thread));
        assert(repOK());
        return constructionRule.constructSolution(state, ind, thread);
        }

    @Override
    public Parameter defaultBase() { return DEFAULT_BASE.push(SPECIES_NAME);}

    public final boolean repOK()
        {
        return DEFAULT_BASE != null
            && SPECIES_NAME != null
            && !SPECIES_NAME.isEmpty()
            && P_CONSTRUCTION_RULE != null
            && !P_CONSTRUCTION_RULE.isEmpty()
            && constructionRule != null;
        }
    }
