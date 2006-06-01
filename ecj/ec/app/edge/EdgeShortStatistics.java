/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.edge;
import ec.*;
import ec.gp.koza.*;
import ec.util.*;

/* 
 * EdgeShortStatistics.java
 * 
 * Created: Fri Nov  5 16:03:44 1999
 * By: Sean Luke
 */

/**
 * @author Sean Luke
 * @version 1.0 
 */

public class EdgeShortStatistics extends KozaShortStatistics
    {
    public void _postEvaluationStatistics(final EvolutionState state)
        {
        // compute and print out the other statistics -- we depend on it!
        super._postEvaluationStatistics(state);

        // we have only one population, so this is kosher
        state.output.print(((Edge)(state.evaluator.p_problem.clone())).
                           describeShortGeneralized(best_of_run_a[0], state, 0), 
                           Output.V_NO_GENERAL,statisticslog);
        }

    }
