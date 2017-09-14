/*
  Copyright 2012 by James McDermott
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information

*/


package ec.app.lid;
import ec.util.*;
import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;

/*
 * Lid.java
 *
 */

/**
 * Lid implements Daida's Lid problem. See the README.txt.
 *
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>data</tt><br>
 <font size=-1>classname, inherits or == ec.app.lid.LidData</font></td>
 <td valign=top>(the class for the prototypical GPData object for the Lid problem)</td></tr>
 </table>

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>data</tt></td>
 <td>species (the GPData object)</td></tr>
 </table>
 *
 * @author James McDermott
 * @version 1.0
 */

public class Lid extends GPProblem implements SimpleProblemForm
    {

    static final String P_TARGET_DEPTH = "targetDepth";
    static final String P_TARGET_TERMINALS = "targetTerminals";
    static final String P_WEIGHT_DEPTH = "weightDepth";

    int maxWeight = 100;
    int targetDepth;
    int targetTerminals;
    int actualDepth;
    int actualTerminals;

    int weightDepth;
    int weightTerminals;

    public void setup(final EvolutionState state,
        final Parameter base)
        {
        // very important, remember this
        super.setup(state,base);

        // load our targets
        targetDepth = state.parameters.getInt(base.push(P_TARGET_DEPTH), null, 1);
        if (targetDepth == 0)
            state.output.error("The target depth must be > 0",
                base.push(P_TARGET_DEPTH));
        targetTerminals = state.parameters.getInt(base.push(P_TARGET_TERMINALS), null, 1);
        if (targetTerminals == 0)
            state.output.error("The target terminals must be > 0",
                base.push(P_TARGET_TERMINALS));
        weightDepth = state.parameters.getInt(base.push(P_WEIGHT_DEPTH), null, 0);
        if (weightDepth < 0 || weightDepth > maxWeight)
            state.output.error("The depth-weighting must be in [0, maxWeight]",
                base.push(P_WEIGHT_DEPTH));
        weightTerminals = maxWeight - weightDepth;
        System.out.println("target depth " + targetDepth + " targetTerminals " + targetTerminals);
        state.output.exitIfErrors();
        }

    public void evaluate(final EvolutionState state,
        final Individual ind,
        final int subpopulation,
        final int threadnum)
        {
        if (!ind.evaluated)  // don't bother reevaluating
            {
            // trees[0].child is the root

            // Note ECJ GPNode counts the root as being depth
            // 1. Daida et al count it as depth 0 (p. 1669).
            actualDepth = ((GPIndividual) ind).trees[0].child.depth() - 1;

            actualTerminals = ((GPIndividual) ind).trees[0].child.numNodes(GPNode.NODESEARCH_TERMINALS);

            double scoreDepth = weightDepth * (1.0 - Math.abs(targetDepth - actualDepth) / (double) targetDepth);
            double scoreTerminals = 0.0;
            if (targetDepth == actualDepth)
                {
                scoreTerminals = weightTerminals * (1.0 - Math.abs(targetTerminals - actualTerminals) / (double) targetTerminals);
                }

            double score = scoreTerminals + scoreDepth;

            SimpleFitness f = ((SimpleFitness) ind.fitness);
            f.setFitness(state, score, false);
            ind.evaluated = true;
            }
        }

    public void describe(
        final EvolutionState state,
        final Individual ind,
        final int subpopulation,
        final int threadnum,
        final int log)
        {
        // trees[0].child is the root
        // Note ECJ GPNode counts the root as being depth
        // 1. Daida et al count it as depth 0. We'll print both.
        actualDepth = ((GPIndividual) ind).trees[0].child.depth() - 1;
        actualTerminals = ((GPIndividual) ind).trees[0].child.numNodes(GPNode.NODESEARCH_TERMINALS);
        state.output.println("\n\nBest Individual: in ECJ terms depth = " + (actualDepth + 1) + "; in Lid terms depth = " + actualDepth + "; number of terminals = " + actualTerminals, log);
        }
    }
