/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.ant;
import ec.*;
import ec.gp.koza.*;
import ec.util.*;
import ec.simple.*;

/* 
 * AntStatistics.java
 * 
 * Created: Fri Nov  5 16:03:44 1999
 * By: Sean Luke
 */

/**
 * @author Sean Luke
 * @version 1.0 
 */

public class AntStatistics extends KozaStatistics
    {
    public void finalStatistics(final EvolutionState state, final int result)
        {
        // print out the other statistics
        super.finalStatistics(state,result);

        // we have only one population, so this is kosher
        ((SimpleProblemForm)(state.evaluator.p_problem.clone())).describe(best_of_run[0], state, 0, statisticslog,Output.V_NO_GENERAL);
        }

    }
