/*
Copyright 2006 by Sean Luke
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/


package ec.gp.koza;
import ec.gp.*;
import ec.*;
import ec.util.*;

/* 
 * FullBuilder.java
 * 
 * Created: Thu Oct  7 18:03:25 1999
 * By: Sean Luke
 */

/**
   FullBuilder is a GPNodeBuilder which implements the FULL tree building method described in Koza I/II.  
   
   <p>GROW works by choosing a random integer <i>d</i> between minDepth and maxDepth, inclusive.  It then grows a full tree of depth <i>d</i>.
   
   <p>Actually, claiming to implement the Koza I/II approach is a bit of a fib -- Koza's original code is somewhat ad-hoc.  In the Koza approach, <i>d</i> is chosen in a kind of round-robin fashion rather than at random, if RAMPED HALF/HALF is used.  Also, for all three algorithms (RAMPED HALF/HALF, GROW, FULL), the algorithm will not generate a tree consisting of a single terminal, unless forced to.

   <p>This implementation instead follows lil-gp's approach, which is to choose <i>d</i> at random from between minDepth and maxDepth, inclusive, and to allow trees consisting of single terminals.   

   <p>Determining what various algorithms do is a little confusing, mostly because the source code for lil-gp and Koza don't actually quite do what they claim.  The table below lists the depth values actually used (counting nodes, rather than edges, for depth).  It's probably not what you had expected!


   <br>
   <br>
   <div align="center">
   <table border="0" cellspacing="1" cellpadding="2">
   <tr>
   <td bgcolor="#ffffff"><font size="-1" face="simple,geneva,arial,helvetica" color="#ffffff" ></font><br></td>
   <td bgcolor="#3366cc"><font size="-1" face="simple,geneva,arial,helvetica" color="#ffffff" >Koza I Min</font><br></td>
   <td bgcolor="#3366cc"><font size="-1" face="simple,geneva,arial,helvetica" color="#ffffff" >Koza I Max</font><br></td>
   <td bgcolor="#3366cc"><font size="-1" face="simple,geneva,arial,helvetica" color="#ffffff" >Koza II Min</font><br></td>
   <td bgcolor="#3366cc"><font size="-1" face="simple,geneva,arial,helvetica" color="#ffffff" >Koza II Max</font><br></td>
   <td bgcolor="#3366cc"><font size="-1" face="simple,geneva,arial,helvetica" color="#ffffff" >lil-gp Min</font><br></td>
   <td bgcolor="#3366cc"><font size="-1" face="simple,geneva,arial,helvetica" color="#ffffff" >lil-gp Max</font><br></td>
   <td bgcolor="#3366cc"><font size="-1" face="simple,geneva,arial,helvetica" color="#ffffff" >ECJ Min</font><br></td>
   <td bgcolor="#3366cc"><font size="-1" face="simple,geneva,arial,helvetica" color="#ffffff" >ECJ Max</font><br></td>
   </tr><tr>
   <td bgcolor="#3366cc"><font size="-1" face="simple,geneva,arial,helvetica" color="#ffffff">GROW (mut)</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">5</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">5</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">5</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">5</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">&nbsp;</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">&nbsp;</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">5</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">5</font><br></td>
   <tr></tr>
   <td bgcolor="#3366cc"><font size="-1" face="simple,geneva,arial,helvetica" color="#ffffff">GROW (new)</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">7</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">7</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">6? 7?</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">6? 7?</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">3</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">7</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">5</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">5</font><br></td>
   <tr></tr>
   <td bgcolor="#3366cc"><font size="-1" face="simple,geneva,arial,helvetica" color="#ffffff">FULL (new)</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">7</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">7</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">6? 7?</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">6? 7?</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">3</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">7</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">&nbsp;</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">&nbsp;</font><br></td>
   <tr></tr>
   <td bgcolor="#3366cc"><font size="-1" face="simple,geneva,arial,helvetica" color="#ffffff">HALF (new)</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">2</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">6</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">2</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">5? 6?</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">3</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">7</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">2</font><br></td>
   <td bgcolor="#cccccc"><font size="-1" face="simple,geneva,arial,helvetica">6</font><br></td>
   </tr></table>
   </div>
   <br>
   <br>

   The table cell is empty when that parameter is not defined by the system by default.  Koza II has two values each because of a possible typo in the text -- while page 656 gives one maximum, page 671 gives another.  Note the odd fact that in Koza I/II GROW and FULL have <i>effectively</i> one-deeper tree values than HALF does, even though they use the same code parameters!  This is because of a quirk in Koza's code.

   <p> This algorithm ignores <tt>requestedSize</tt>, so no pipelines can ask it to grow a tree of a specific fixed size.  The algorithm also ignores any user-provided size distributions.

   <p><b>Parameters</b><br>
   <table>
   <tr><td valign=top><i>base</i>.<tt>min-depth</tt><br>
   <font size=-1>int &gt;= 1</font></td>
   <td valign=top>(smallest "maximum" depth the builder may use for building a tree. 2 is the default.)</td></tr>

   <tr><td valign=top><i>base</i>.<tt>max-depth</tt><br>
   <font size=-1>int &gt;= <i>base</i>.<tt>min-depth</tt></font></td>
   <td valign=top>(largest "maximum" depth the builder may use for building a tree. 6 is the default.)</td></tr>
   </table>

   <p><b>Default Base</b><br>
   gp.koza.full

   * @author Sean Luke
   * @version 1.0 
   */



public class FullBuilder extends GPNodeBuilder
    {
    public static final String P_FULLBUILDER = "full";
    public static final String P_MAXDEPTH = "max-depth";
    public static final String P_MINDEPTH = "min-depth";

    /** The largest maximum tree depth FULL can specify. */
    public int maxDepth;

    /** The smallest maximum tree depth FULL can specify. */
    public int minDepth;

    public Parameter defaultBase()
        {
        return GPKozaDefaults.base().push(P_FULLBUILDER); 
        }


    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);

        Parameter def = defaultBase();

        // load maxdepth and mindepth, check that maxdepth>0, mindepth>0, maxdepth>=mindepth
        maxDepth = state.parameters.getInt(base.push(P_MAXDEPTH),def.push(P_MAXDEPTH),1);
        if (maxDepth<=0)
            state.output.fatal("The Max Depth for FullBuilder must be at least 1.",
                               base.push(P_MAXDEPTH),def.push(P_MAXDEPTH));
        
        minDepth = state.parameters.getInt(base.push(P_MINDEPTH),def.push(P_MINDEPTH),1);
        if (minDepth<=0)
            state.output.fatal("The Max Depth for FullBuilder must be at least 1.",
                               base.push(P_MINDEPTH),def.push(P_MINDEPTH));

        if (maxDepth<minDepth)
            state.output.fatal("Max Depth must be >= Min Depth for FullBuilder",
                               base.push(P_MAXDEPTH),def.push(P_MAXDEPTH));
        }


    
    public GPNode newRootedTree(final EvolutionState state,
                                final GPType type,
                                final int thread,
                                final GPNodeParent parent,
                                final GPFunctionSet set,
                                final int argposition,
                                final int requestedSize)
        {
        return fullNode(state,0,state.random[thread].nextInt(maxDepth-minDepth+1) + minDepth,type,thread,parent,argposition,set);
        }


    /** A private recursive method which builds a FULL-style tree for newRootedTree(...) */
    private GPNode fullNode(final EvolutionState state,
                            final int current,
                            final int max,
                            final GPType type,
                            final int thread,
                            final GPNodeParent parent,
                            final int argposition,
                            final GPFunctionSet set) 
        {
        // Pick a random node from Hashtable for a given type --
        // we assume it's been pre-checked for invalid type situations
        
        if (current+1 >= max)  // we're at max depth, force a terminal
            {
            GPNode[] nn = set.terminals[type.type];
            GPNode n = (GPNode)(nn[state.random[thread].nextInt(nn.length)].clone());
            n.resetNode(state,thread);  // give ERCs a chance to randomize
            n.argposition = (byte)argposition;
            n.parent = parent;
            return n;
            }
        else // we're not at max depth, force a nonterminal if you can
            {
            GPNode[] nn = set.nonterminals[type.type];
            if (nn==null || nn.length ==0)  /* no nonterminals, hope the guy
                                               knows what he's doing! */
                nn = set.terminals[type.type];

            GPNode n = (GPNode)(nn[state.random[thread].nextInt(nn.length)].clone());
            n.resetNode(state,thread);  // give ERCs a chance to randomize
            n.argposition = (byte)argposition;
            n.parent = parent;

            // Populate the node...
            GPType[] childtypes = n.constraints(((GPInitializer)state.initializer)).childtypes;
            for(int x=0;x<childtypes.length;x++)
                n.children[x] = fullNode(state,current+1,max,childtypes[x],thread,n,x,set);

            return n;
            }
        }

    }
