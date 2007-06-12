package ec.drm.app.tutorial5;

import ec.*;
import ec.util.*;


/* This class dumps every individual of each generation to file, used to debug.*/
public class DumpStatistics extends ec.simple.SimpleStatistics{
	private static final long serialVersionUID = 1L;
	
    /** Logs the best individual of the generation. */
    public void postEvaluationStatistics(final EvolutionState state){
        super.postEvaluationStatistics(state);

        state.output.println("\nGeneration: " + state.generation,Output.V_NO_GENERAL,statisticslog);
        for(int x=0;x<state.population.subpops.length;x++)
            for(int y=1;y<state.population.subpops[x].individuals.length;y++)
            	state.population.subpops[x].individuals[y].printIndividualForHumans(state,statisticslog,Output.V_NO_GENERAL);
    }
}
