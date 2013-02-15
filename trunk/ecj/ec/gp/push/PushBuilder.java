/**

   Push s-expressions can have arbitrary arity, requiring a custom tree
   builder.  This is the one that's standard in Push.  The approach
   is as follows.

   BUILD-TREE(size)
   If size == 1, return a terminal
   Else
   .... Make a parent nonterminal p
   .... while (size > 0)
   .... .... a <- random number from 1 to size
   .... .... size <- size - a
   .... .... c <- BUILD-TREE(a)
   .... .... Add c as a child of p
   shuffle order of children of p
   return p
        
   Obviously arbitrary arities will be incompatible with a variety of genetic
   operators, though they work fine with GP crossover, which is the crucial
   one.  And in theory we could do it with GP mutation using this builder.

*/

package ec.gp.push;
import ec.*;
import ec.gp.*;
import ec.util.*;
import java.util.*;

public class PushBuilder extends GPNodeBuilder
    {
    public static final String P_PUSHBUILDER = "push";
    
    int maxSize = 20;

    public Parameter defaultBase()
        {
        return PushDefaults.base().push(P_PUSHBUILDER); 
        }
        
    
    // shuffles the children of the node, if any, and returns the node
    public GPNode[] shuffle(GPNode[] objs, EvolutionState state, int thread)
        {
        int numObjs = objs.length;
        MersenneTwisterFast random = state.random[thread];
        GPNode obj;
        int rand;
        
        for(int x=numObjs-1; x >= 1 ; x--)
            {
            rand = random.nextInt(x+1);
            obj = objs[x];
            objs[x] = objs[rand];
            objs[rand] = obj;
            }
        return objs;
        }
    
    GPNode[] dummy = new GPNode[0];
    
    public GPNode newRootedTree(final EvolutionState state,
        final GPType type,
        final int thread,
        final GPNodeParent parent,
        final GPFunctionSet set,
        final int argposition,
        int requestedSize)
        {
        int t = type.type;
        GPNode[] terminals = set.terminals[t];
        GPNode[] nonterminals = set.nonterminals[t];


        if (requestedSize == NOSIZEGIVEN)
            requestedSize = state.random[thread].nextInt(maxSize) + 1;

        GPNode n = null;
        if (requestedSize == 1)
            {
            // pick a random terminal
            n = (GPNode)(terminals[state.random[thread].nextInt(terminals.length)].lightClone());
            }
        else
            {
            n = (GPNode)(nonterminals[state.random[thread].nextInt(nonterminals.length)].lightClone());  // it's always going to be the Dummy
            
            // do decomposition
            byte pos = 0;            // THIS WILL HAVE TO BE MODIFIED TO AN INT LATER ON AND THIS WILL AFFECT ARGPOSITIONS!!!
            ArrayList list = new ArrayList();  // dunno if this is too expensive
            
            while(requestedSize >= 1)
                {
                int amount = state.random[thread].nextInt(requestedSize) + 1;
                requestedSize -= amount;
                GPNode f = newRootedTree(state, type, thread, parent, set, pos, amount);
                list.add(f);
                }
            
            // shuffle and reassign argument position
            n.children = (GPNode[])(list.toArray(dummy));
            n.children = shuffle(n.children, state, thread);

            for(int i = 0; i < n.children.length; i++)
                n.children[i].argposition = (byte) i;
            }
            
        n.resetNode(state,thread);  // give ERCs a chance to randomize
        n.argposition = (byte)argposition;
        n.parent = parent;
        
        return n;
        }
    }


