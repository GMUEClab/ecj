/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp.koza;
import ec.*;
import ec.gp.*;
import ec.util.*;

/* 
 * HalfBuilder.java
 * 
 * Created: Thu Oct  7 18:03:49 1999
 * By: Sean Luke
 */

/** HalfBuilder is a GPNodeBuilder which 
    implements the RAMPED HALF-AND-HALF tree building method described in Koza I/II.  

    <p>RAMPED HALF-AND-HALF works by choosing a random integer <i>d</i> between minDepth and maxDepth, inclusive.  It then grows a tree of depth 1 to <i>d</i> inclusive.  (1-pickGrowProbability) of the time (by default, 0.5) it grows a tree using the FULL method, which generates full trees of exactly depth <i>d</i>.  (pickGrowProbability) of the time, it grows a tree using the GROW method, which may generate trees of any size between 1 and <i>d</i> inclusive.

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
    <tr><td valign=top><i>base</i>.<tt>growp</tt><br>
    <font size=-1>0.0 &lt;= float &lt;= 1.0</font></td>
    <td valign=top>(the likelihood of choosing GROW (as opposed to FULL)></tr>

    <tr><td valign=top><i>base</i>.<tt>min-depth</tt><br>
    <font size=-1>int &gt;= 1</font></td>
    <td valign=top>(smallest "maximum" depth the builder may use for building a tree.  2 is the default.)</td></tr>
   
    <tr><td valign=top><i>base</i>.<tt>max-depth</tt><br>
    <font size=-1>int &gt;= <i>base</i>.<tt>min-depth</tt></font></td>
    <td valign=top>(largest "maximum" depth the builder may use for building a tree. 6 is the default.)</td></tr>
    </table>
   
    <p><b>Default Base</b><br>
    gp.koza.half

    * @author Sean Luke
    * @version 1.0 
    */


public class HalfBuilder extends KozaBuilder
    {
    public static final String P_HALFBUILDER = "half";
    public static final String P_PICKGROWPROBABILITY = "growp";

    /** The likelihood of using GROW over FULL. */
    public float pickGrowProbability;
    
    public Parameter defaultBase()
        {
        return GPKozaDefaults.base().push(P_HALFBUILDER); 
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);

        Parameter def = defaultBase();

        pickGrowProbability = state.parameters.getFloat(
            base.push(P_PICKGROWPROBABILITY),
            def.push(P_PICKGROWPROBABILITY),0.0f,1.0f);
        if (pickGrowProbability < 0.0f)
            state.output.fatal("The Pick-Grow Probability for HalfBuilder must be a floating-point value between 0.0 and 1.0 inclusive.", base.push(P_MAXDEPTH),def.push(P_MAXDEPTH));
        }
    
    public GPNode newRootedTree(final EvolutionState state,
                                final GPType type,
                                final int thread,
                                final GPNodeParent parent,
                                final GPFunctionSet set,
                                final int argposition,
                                final int requestedSize)
        {
        if (state.random[thread].nextFloat() < pickGrowProbability)
            return growNode(state,0,state.random[thread].nextInt(maxDepth-minDepth+1) + minDepth,type,thread,parent,argposition,set);
        else
            return fullNode(state,0,state.random[thread].nextInt(maxDepth-minDepth+1) + minDepth,type,thread,parent,argposition,set);
        }

    }


