/*
Copyright 2006 by Sean Luke
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/


package ec.app.ant;

import org.primordion.ealontro.gp.XhKozaStatistics; // Ken Webb
import ec.*;
//import ec.gp.koza.*; // Ken Webb
import ec.util.*;
import ec.simple.*;

/* 
 * AntStatistics.java
 * 
 * Created: Fri Nov  5 16:03:44 1999
 * By: Sean Luke
 * 
 * Edited by: Ken Webb, Xholon project, www.primordion.com
 * May 29, 2006 - extends XhKozaStatistics rather than KozaStatistics
 * April 17, 2007 - no changes required from ECJ 15 to ECJ 16
 */

/**
 * @author Sean Luke
 * @version 1.0 
 */

public class AntStatistics extends XhKozaStatistics
    {
    public void finalStatistics(final EvolutionState state, final int result)
        {
        // print out the other statistics
        super.finalStatistics(state,result);

        // we have only one population, so this is kosher
        ((SimpleProblemForm)(state.evaluator.p_problem.clone())).describe(best_of_run[0], state, 0, statisticslog,Output.V_NO_GENERAL);
        }

    }
