package ec.gp.push;
import ec.*;
import ec.gp.*;
import ec.util.*;
import java.util.*;

/* 
 * PushBuilder.java
 * 
 * Created: Fri Feb 15 23:00:04 EST 2013
 * By: Sean Luke
 */
 
/**
 *
 * PushBuilder implements the Push-style tree building algorithm, which permits nonterminals of arbitrary arity.
 * This algorithm is as follows:

 <p><tt><pre>
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
 </pre></tt>
   
 <p>You must specify a size distribution for PushBuilder.
  
 <p><b>Default Base</b><br>
 gp.push.builder

 * @author Sean Luke
 * @version 1.0 
 */


public class PushBuilder extends GPNodeBuilder
    {
    public static final String P_PUSHBUILDER = "builder";
        
    public Parameter defaultBase()
        {
        return PushDefaults.base().push(P_PUSHBUILDER); 
        }
        
    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);

        Parameter def = defaultBase();

        // we use size distributions -- did the user specify any?
        if (!canPick())
            state.output.fatal("PushBuilder needs a distribution of tree sizes to pick from.  You can do this by either setting a distribution (with " + P_NUMSIZES + ") or with "
                + P_MINSIZE + " and " + P_MAXSIZE + ".", base, def);
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
            requestedSize = pickSize(state,thread);

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


