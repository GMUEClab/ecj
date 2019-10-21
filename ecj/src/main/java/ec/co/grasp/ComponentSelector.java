package ec.co.grasp;

import ec.EvolutionState;
import ec.Setup;
import ec.co.Component;
import java.util.List;

public interface ComponentSelector extends Setup {
    public abstract Component choose(final EvolutionState state, final List<Component> components, final int thread);
    }
