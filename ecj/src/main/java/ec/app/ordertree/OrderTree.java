/*
  Copyright 2012 by James McDermott
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.ordertree;
import ec.app.ordertree.func.*;
import ec.util.*;
import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;
import java.io.*;
import javax.imageio.stream.*;

/*
 * OrderTree.java
 *
 */

/**
 * OrderTree implements the OrderTree problem of Hoang et al. See the
 * README.txt.  Note that although this is a tunable problem, tuning
 * is achieved by setting the function and terminal sets. No need for
 * a size parameter.
 *
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>data</tt><br>
 <font size=-1>classname, inherits or == ec.app.ordertree.OrderTreeData</font></td>
 <td valign=top>(the class for the prototypical GPData object for the OrderTree problem)</td></tr>
 </table>

 <tr><td valign=top><i>base</i>.<tt>contribution-type</tt><br>
 <font size=-1>Integer specifying the amount of nonlinearity in fitness contributions</font></td>
 <td valign=top>0: add unit; 1: add node value; 2: add node value squared; 3: add 3^node value</td></tr>
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

public class OrderTree extends GPProblem implements SimpleProblemForm
    {

    double fitness;
    static final String P_CONTRIBUTION_TYPE = "contribution-type";
    final static int CONTRIBUTION_UNIT = 0;
    final static int CONTRIBUTION_VALUE = 1;
    final static int CONTRIBUTION_SQUARE = 2;
    final static int CONTRIBUTION_EXPONENTIAL = 3;
    
    int fitnessContributionType;
    
    public void setup(final EvolutionState state,
        final Parameter base)
        {
        // very important, remember this
        super.setup(state,base);

        fitnessContributionType = state.parameters.getInt(base.push(P_CONTRIBUTION_TYPE),null,1);
        if (fitnessContributionType < CONTRIBUTION_UNIT || fitnessContributionType > CONTRIBUTION_EXPONENTIAL) state.output.fatal("Fitness Contribution Type must be an integer greater than 0 and less th an 4", base.push(P_CONTRIBUTION_TYPE)); 

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
            fitness = 0.0;
            nodeCal(((GPIndividual) ind).trees[0].child, state);

            SimpleFitness f = ((SimpleFitness) ind.fitness);
            f.setFitness(state, fitness, false);
            ind.evaluated = true;
            }
        }

    double fitnessContribution(double value, EvolutionState state)
        {
        switch (fitnessContributionType)
            {
            case CONTRIBUTION_UNIT: return 1.0;
            case CONTRIBUTION_VALUE: return value;
            case CONTRIBUTION_SQUARE: return value * value;
            case CONTRIBUTION_EXPONENTIAL: return Math.pow(3.0, value);
            default: state.output.fatal("Unexpected fitness contribution type.");
                return -1.0;
            }
        }
    
    void nodeCal(GPNode p, EvolutionState state)
        {
        int pval = ((OrderTreeNode) p).value();
        for (int i = 0; i < p.children.length; i++)
            {
            GPNode c = p.children[i];
            int cval = ((OrderTreeNode) c).value();
            if (pval < cval)
                {
                // direct fitness contribution
                fitness += fitnessContribution(cval, state);
                nodeCal(c, state);
                }
            else if (pval == cval)
                {
                // neutral-left-walk
                boolean found = false;
                while (c.children.length > 0 && cval == pval && !found)
                    {
                    c = c.children[0];
                    cval = ((OrderTreeNode) c).value();
                    if (pval < cval)
                        {
                        found = true;
                        }
                    }
                if (found)
                    {
                    fitness += fitnessContribution(cval, state);
                    nodeCal(c, state);
                    }
                }
            }
        return;
        }
    }
