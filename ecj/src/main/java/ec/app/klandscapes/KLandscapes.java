/*
  Copyright 2012 by Luca Manzoni
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.app.klandscapes;

import ec.app.klandscapes.func.KLandscapeTree;
import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;
import ec.util.*;

import java.util.*;

/**
 * KLandscapes implements the K-Landscapes problem of Vanneschi,
 * Castelli and Manzoni. See the README.txt.  
 *
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>data</tt><br>
 <font size=-1>classname, inherits or == ec.app.klandscapes.KLandscapesData</font></td>
 <td valign=top>(the class for the prototypical GPData object for the KLandscapes problem)</td></tr>
 </table>

 <tr><td valign=top><i>base</i>.<tt>k-value</tt><br>
 <font size=-1>Integer specifying the amount of epistasis in fitness contributions</font></td>
 <td valign=top>Values from 0 upwards.</td></tr>
 </table>

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>data</tt></td>
 <td>species (the GPData object)</td></tr>
 </table>
 *
 * @author Luca Manzoni
 * @version 1.0
 */


public class KLandscapes extends GPProblem implements SimpleProblemForm {

    // Score of the nodes. Functionals (positions 0 and 1) and terminals (positions from 2 to 5)
    double nodeScore[];
    // Score fo the edges. Row: functionals. Columns: funcionals + terminals
    double edgeScore[][];
    // Best possible fitness. Must be not negative.
    double bestFitness;
    // The K of the K-Landscapes. It is an index of epistasis. It has
    // integer values from 2 upwards, in the paper's experimental
    // section, but can take on values 0 and 1.
    int k;
    String P_PROBLEMNAME = "k-landscapes";
    String P_KVALUE = "k-value";
    
    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        state.output.exitIfErrors();
        Parameter kval = new Parameter(state.P_EVALUATOR).push(P_PROBLEM).push(P_PROBLEMNAME).push(P_KVALUE);
        k = state.parameters.getInt(kval,null,0);
        // System.out.println("K = " + k);
        
        for(int i = 0 ; i < indices.length; i++)
            indices[i] = -1;
        indices['A' - 'A'] = 0;
        indices['B' - 'A'] = 1;
        indices['X' - 'A'] = 2;
        indices['Y' - 'A'] = 3;
        indices['Z' - 'A'] = 4;
        indices['W' - 'A'] = 5;


        // now do some initialization
        MersenneTwisterFast r = state.random[0];
        nodeScore = new double[6];
        edgeScore = new double[2][6];
        for (int i = 0; i < 6; i++)
            {
            nodeScore[i] = 2*r.nextDouble() -1;
            }
        // We need to assure that the best fitness is positive (to normalize it to 1)
        // A method to do this is to have at least one terminal symbol with a positive score.
        boolean ok = false;
        for (int i = 2; i < 6; i++)
            {
            if (nodeScore[i] > 0)
                ok = true;
            }
        if (!ok)
            nodeScore[2] = r.nextDouble();
        for (int i = 0; i < 2; i++)
            {
            for (int j = 0; j < 6; j++)
                {
                edgeScore[i][j] = r.nextDouble();
                }
            }
        bestFitness = computeBestFitness();


        }

    // doesn't need to be cloned
    int[] indices = new int[256];  // we assume we only have letters, and 0 means "no sucessor"
    int getIndex(char c)
        {
        return indices[c - 'A'];
        }

    public void evaluate(
        final EvolutionState state,
        final Individual ind,
        final int subpopulation,
        final int threadnum)
        {
        if (!ind.evaluated)
            {
            double score = fitness(((GPIndividual) ind).trees[0].child);
            SimpleFitness f = ((SimpleFitness) ind.fitness);
            f.setFitness(state, score, score==1.0);
            ind.evaluated = true;
            }
        
        }

    double fitness(GPNode root)
        {
        // Compute the penality (it increases with the difference in depth between the tree and k.
        double penalty = 1.0/(1+Math.abs(k+1-root.depth()));
        return penalty*fitnessHelper(root)/bestFitness;
        }

    // We recursively search for the subtree with the maximal "score" 
    double fitnessHelper(GPNode node)
        {
        double max = subtreeFitness(node,k);
        for (int i = 0; i < node.children.length; i++)
            {
            GPNode child = node.children[i];
            double tmp = fitnessHelper(child);
            if (tmp > max)
                max = tmp;
            }
        return max;
        }

    double subtreeFitness(GPNode node, int depth)
        {
        int index = getIndex(((KLandscapeTree) node).value());
        double score = nodeScore[index];
        if (depth == 0 || index > 1) //If we have reached the maximum depth (or we have found a terminal)
            return score;
        for (int i = 0; i < node.children.length; i++)
            {
            GPNode child = node.children[i];
            int childindex = getIndex(((KLandscapeTree) child).value());
            //We recursively compute the "score" of the subtree
            score += (1+edgeScore[index][childindex])*subtreeFitness(child,depth-1);
            }
        return score;
        }

    double computeBestFitness()
        {
        // This is a dynamic programming kludge.
        double ttable[][] = new double[k][2];
        double ftable[][] = new double[k+1][2];
        for (int i = 0; i < 2; i++)
            {
            ftable[0][i] = nodeScore[i];
            }
        // Case 1: the optimum hase depth at most k
        for (int i = 0; i < k; i++)
            {
            for (int j = 0; j < 2; j++)
                {
                if (i == 0)
                    {
                    double max = (1+edgeScore[j][2])*nodeScore[2];
                    for (int h = 3; h < 6; h++)
                        {
                        double tmp = (1+edgeScore[j][h])*nodeScore[h];
                        if (tmp > max)
                            max = tmp;
                        }
                    ttable[i][j] = nodeScore[j] + 2*max;
                    }
                else
                    {
                    double max = (1+edgeScore[j][0])*ttable[i-1][0];
                    for (int h = 1; h < 2; h++)
                        {
                        double tmp = (1+edgeScore[j][h])*ttable[i-1][h];
                        if (tmp > max)
                            max = tmp;
                        }
                    ttable[i][j] = nodeScore[j] + 2*max;
                    }
                }
            }
        // Case 2: the optimum has depth k+1
        for (int i = 1; i < k+1; i++)
            {
            for (int j = 0; j < 2; j++)
                {
                double max = (1+edgeScore[j][0])*ftable[i-1][0];
                for (int h = 1; h < 2; h++)
                    {
                    double tmp = (1+edgeScore[j][h])*ftable[i-1][h];
                    if (tmp > max)
                        max = tmp;
                    }
                ftable[i][j] = nodeScore[j] + 2*max;
                }
            }
        double best = nodeScore[2];
        for (int i = 3; i < 6; i++)
            {
            if (nodeScore[i] > best)
                best = nodeScore[i];
            }
        for (int i = 0; i < k; i++)
            {
            for (int j = 0; j < 2; j++)
                {
                if (ttable[i][j] > best)
                    best = ttable[i][j];
                }
            }
        for (int i = 0; i < 2; i++)
            {
            if (0.5*ftable[k][i] > best)
                best = 0.5*ftable[k][i];
            }
        return best;
        }

    public Object clone()
        {
        KLandscapes tmp = (KLandscapes)(super.clone());
        tmp.nodeScore = new double[6];
        tmp.edgeScore = new double[2][6];
        tmp.bestFitness = bestFitness;
        tmp.k = k;
        for (int i = 0; i < 6; i++)
            {
            tmp.nodeScore[i] = nodeScore[i];
            for (int j = 0; j < 2; j++)
                {
                tmp.edgeScore[j][i] = edgeScore[j][i];
                }
            }
        return tmp;
        }

    }
