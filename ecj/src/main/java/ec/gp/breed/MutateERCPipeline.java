/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp.breed;
import ec.*;
import ec.util.*;
import ec.gp.*;

import java.util.ArrayList;
import java.util.HashMap;

/* 
 * MutateERCPipeline.java
 * 
 * Created: Wed Dec 15 21:41:30 1999
 * By: Sean Luke
 */

/**
 * MutateERCPipeline works very similarly to the "Gaussian" algorithm
 * described in Kumar Chellapilla,
 * "A Preliminary Investigation into Evolving Modular Programs without Subtree
 * Crossover", GP98.
 *
 * <p>MutateERCPipeline picks a random node from a random tree in the individual,
 * using its node selector.  It then proceeds to "mutate" every ERC (ephemeral
 * random constant) located in the subtree rooted at that node.  It does this
 * by calling each ERC's <tt>mutateERC()</tt> method.  The default form of <tt>mutateERC()</tt>
 * method is to simply call <tt>resetNode()</tt>, thus randomizing the ERC;
 * you may want to override this default to provide more useful mutations,
 * such as adding gaussian noise.

 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 ...as many as the source produces

 <p><b>Number of Sources</b><br>
 1

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>ns</tt>.0<br>
 <font size=-1>classname, inherits and != GPNodeSelector</font></td>
 <td valign=top>(GPNodeSelector for tree)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>tree.0</tt><br>
 <font size=-1>0 &lt; int &lt; (num trees in individuals), if exists</font></td>
 <td valign=top>(tree chosen for mutation; if parameter doesn't exist, tree is picked at random)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 gp.breed.mutate-erc

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>ns</tt><br>
 <td>The GPNodeSelector selector</td></tr>
 </table>


 * @author Sean Luke
 * @version 1.0 
 */

public class MutateERCPipeline extends GPBreedingPipeline
    {
    private static final long serialVersionUID = 1;

    public static final String P_MUTATEERC = "mutate-erc";
    public static final int NUM_SOURCES = 1;
    
    /** How the pipeline chooses a subtree to mutate */
    public GPNodeSelector nodeselect;

    /** Is our tree fixed?  If not, this is -1 */
    int tree;

    public Parameter defaultBase() { return GPBreedDefaults.base().push(P_MUTATEERC); }

    public int numSources() { return NUM_SOURCES; }

    public Object clone()
        {
        MutateERCPipeline c = (MutateERCPipeline)(super.clone());
        
        // deep-cloned stuff
        c.nodeselect = (GPNodeSelector)(nodeselect.clone());
        return c;
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);

        Parameter p = base.push(P_NODESELECTOR).push(""+0);
        Parameter def = defaultBase();

        nodeselect = (GPNodeSelector)
            (state.parameters.getInstanceForParameter(
                p,def.push(P_NODESELECTOR).push(""+0),
                GPNodeSelector.class));
        nodeselect.setup(state,p);

        tree = TREE_UNFIXED;
        if (state.parameters.exists(base.push(P_TREE).push(""+0),
                def.push(P_TREE).push(""+0)))
            {
            tree = state.parameters.getInt(base.push(P_TREE).push(""+0),
                def.push(P_TREE).push(""+0),0);
            if (tree==-1)
                state.output.fatal("Tree fixed value, if defined, must be >= 0");
            }
        }


    public final void mutateERCs(final GPNode node, 
        final EvolutionState state, final int thread)
        {
        // is node an erc?
        if (node instanceof ERC)
            ((ERC)node).mutateERC(state,thread);

        // mutate children
        for(int x=0;x<node.children.length;x++)
            mutateERCs(node.children[x],state,thread);
        }

    public int produce(final int min,
        final int max,
        final int subpopulation,
        final ArrayList<Individual> inds,
        final EvolutionState state,
        final int thread, HashMap<String, Object> misc)
        {
        int start = inds.size();
                
        // grab n individuals from our source and stick 'em right into inds.
        // we'll modify them from there
        int n = sources[0].produce(min,max,subpopulation,inds, state,thread, misc);

        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            {
            return n;
            }

        // now let's mutate 'em
        for(int q=start; q < n+start; q++)
            {
            GPIndividual i = (GPIndividual)inds.get(q);
            
            if (tree!=TREE_UNFIXED && (tree<0 || tree >= i.trees.length))
                // uh oh
                state.output.fatal("MutateERCPipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 
            
            int t;
            // pick random tree
            if (tree==TREE_UNFIXED)
                if (i.trees.length>1) t = state.random[thread].nextInt(i.trees.length);
                else t = 0;
            else t = tree;
            
            i.evaluated = false;

            // prepare the nodeselector
            nodeselect.reset();

            // Now pick a random node
            
            GPNode p = nodeselect.pickNode(state,subpopulation,thread,i,i.trees[t]);

            // mutate all the ERCs in p1's subtree

            mutateERCs(p,state,thread);
            
            // add the new individual, replacing its previous source
            inds.set(q,i);
            
            }
        return n;
        }
    }
