/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp;
import ec.*;
import ec.util.*;

/* 
 * GPNodeBuilder.java
 * 
 * Created: Thu Oct  7 17:31:41 1999
 * By: Sean Luke
 */

/**
 * GPNodeBuilder is a Prototype which defines the superclass for objects
 * which create ("grow") GP trees, whether for population initialization,
 * subtree mutation, or whatnot.  It defines a single abstract method, 
 * newRootedTree(...), which must be implemented to grow the tree.
 *
 * <p>GPNodeBuilder also provides some facilities for user-specification
 * of probabilities of various tree sizes, which the tree builder can use
 * as it sees fit (or totally ignore).  
 * There are two such facilities.  First, the user might
 * specify a minimum and maximum range for tree sizes to be picked from;
 * trees would likely be picked uniformly from this range.  Second, the
 * user might specify an array, <tt>num-sizes</tt> long, of probabilities of 
 * tree sizes, in order to give a precise probability distribution. 

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>min-size</tt><br>
 <font size=-1>int &gt;= 1, or undefined</font></td>
 <td valign=top>(smallest valid size, see discussion above)</td></tr>
   
 <tr><td valign=top><i>base</i>.<tt>max-size</tt><br>
 <font size=-1>int &gt;= <tt>min-size</tt>, or undefined</font></td>
 <td valign=top>(largest valid size, see discussion above)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>num-sizes</tt><br>
 <font size=-1>int &gt;= 1, or underfined</font></td>
 <td valign=top>(number of sizes in the size distribution, see discussion above)
 </td></tr>

 <tr><td valign=top><i>base</i>.<tt>size</tt>.<i>n</i><br>
 <font size=-1>0.0 &lt;= float &lt;= 1.0</font>, or undefined</td>
 <td valign=top>(probability of choosing size <i>n</i>.  See discussion above)
 </td></tr>
 </table>


 * @author Sean Luke
 * @version 1.0 
 */

public abstract class GPNodeBuilder implements Prototype
    {
    /** Produces a new rooted tree of GPNodes whose root's return type is
        swap-compatible with <i>type</i>.  When you build a brand-new
        tree out of GPNodes cloned from the
        prototypes stored in the GPNode[] arrays, you must remember
        to call resetNode() on each cloned GPNode.  This gives ERCs a chance
        to randomize themselves and set themselves up. 

        <p>requestedSize is an
        optional argument which differs based on the GPNodeBuilder used.
        Typically it is set to a tree size that the calling method wants
        the GPNodeBuilder to produce; the GPNodeBuilder is not obligated to
        produce a tree of this size, but it should attempt to interpret this
        argument as appropriate for the given algorithm.  To indicate that
        you don't care what size the tree should be, you can pass NOSIZEGIVEN. 
        However if the algorithm <i>requires</i> you to provide a size, it
        will generate a fatal error to let you know. */

    public static final int NOSIZEGIVEN = -1;
    public static final int CHECK_BOUNDARY = 8;
    public static final String P_MINSIZE = "min-size";
    public static final String P_MAXSIZE = "max-size";
    public static final String P_NUMSIZES = "num-sizes";
    public static final String P_SIZE = "size";

    public int minSize;  /** the minium possible size  -- if unused, it's 0 */
    public int maxSize;  /** the maximum possible size  -- if unused, it's 0 */
    public float[] sizeDistribution;  /* sizeDistribution[x] represents
                                         the likelihood of size x appearing 
                                         -- if unused, it's null */
                        
    /** Returns true if some size distribution (either minSize and maxSize,
        or sizeDistribution) is set up by the user in order to pick sizes randomly. */
    public boolean canPick()
        {
        return (minSize!=0 || sizeDistribution !=null);
        }
    
    /** Assuming that either minSize and maxSize, or sizeDistribution, is defined,
        picks a random size from minSize...maxSize inclusive, or randomly
        from sizeDistribution. */ 
    public int pickSize(final EvolutionState state, final int thread)
        {
        if (minSize>0)
            {
            // pick from minSize...maxSize
            return state.random[thread].nextInt(maxSize-minSize+1) + minSize;
            }
        else if (sizeDistribution!=null)
            {
            // pick from distribution
            return RandomChoice.pickFromDistribution(
                sizeDistribution,
                state.random[thread].nextFloat(),
                CHECK_BOUNDARY) + 1 ;
            }
        else throw new InternalError("Neither minSize nor sizeDistribution is defined in GPNodeBuilder");
        }


    public Object clone()
        {
        try
            {
            GPNodeBuilder c = (GPNodeBuilder)(super.clone());

            if (sizeDistribution != null) c.sizeDistribution = 
                                              (float[]) (sizeDistribution.clone());

            return c;
            }
        catch (CloneNotSupportedException e)
            { throw new InternalError(); } // never happens
        }




    public void setup(final EvolutionState state, final Parameter base)
        {
        Parameter def = defaultBase();

        // min and max size

        if (state.parameters.exists(base.push(P_MINSIZE),
                                    def.push(P_MINSIZE)))
            {
            if (!(state.parameters.exists(base.push(P_MAXSIZE),
                                          def.push(P_MAXSIZE))))
                state.output.fatal("This GPNodeBuilder has a " + 
                                   P_MINSIZE + " but not a " + P_MAXSIZE + ".");
           
            minSize = state.parameters.getInt(
                base.push(P_MINSIZE), def.push(P_MINSIZE),1);
            if (minSize==0) 
                state.output.fatal("The GPNodeBuilder must have a min size >= 1.",
                                   base.push(P_MINSIZE), def.push(P_MINSIZE));
            
            maxSize = state.parameters.getInt(
                base.push(P_MAXSIZE), def.push(P_MAXSIZE),1);
            if (maxSize==0) 
                state.output.fatal("The GPNodeBuilder must have a max size >= 1.",
                                   base.push(P_MAXSIZE), def.push(P_MAXSIZE));

            if (minSize > maxSize)
                state.output.fatal(
                    "The GPNodeBuilder must have min size <= max size.",
                    base.push(P_MINSIZE), def.push(P_MINSIZE));
            }
        else if (state.parameters.exists(base.push(P_MAXSIZE),
                                         def.push(P_MAXSIZE)))
            state.output.fatal("This GPNodeBuilder has a " + 
                               P_MAXSIZE + " but not a " + P_MINSIZE + ".",
                               base.push(P_MAXSIZE), def.push(P_MAXSIZE));

        // load sizeDistribution

        else if (state.parameters.exists(base.push(P_NUMSIZES),
                                         def.push(P_NUMSIZES)))
            {
            int siz = state.parameters.getInt(
                base.push(P_NUMSIZES), def.push(P_NUMSIZES),1);
            if (siz==0)
                state.output.fatal("The number of sizes in the GPNodeBuilder's distribution must be >= 1. ");
            sizeDistribution = new float[siz];
            if (state.parameters.exists(base.push(P_SIZE).push("0"),
                                        def.push(P_SIZE).push("0")))
                state.output.warning(
                    "GPNodeBuilder does not use size #0 in the distribution",
                    base.push(P_SIZE).push("0"),
                    def.push(P_SIZE).push("0"));
            
            float sum = 0.0f;
            for(int x=0;x<siz;x++)
                {
                sizeDistribution[x] = state.parameters.getFloat(
                    base.push(P_SIZE).push(""+(x+1)), 
                    def.push(P_SIZE).push(""+(x+1)), 0.0f);
                if (sizeDistribution[x]<0.0)
                    {
                    state.output.warning(
                        "Distribution value #" + x + " negative or not defined, assumed to be 0.0",
                        base.push(P_SIZE).push(""+(x+1)), 
                        def.push(P_SIZE).push(""+(x+1)));
                    sizeDistribution[x] = 0.0f;
                    }
                sum += sizeDistribution[x];
                }
            if (sum>1.0)
                state.output.warning(
                    "Distribution sums to greater than 1.0",
                    base.push(P_SIZE),
                    def.push(P_SIZE));
            if (sum==0.0)
                state.output.fatal(
                    "Distribution is all 0's",
                    base.push(P_SIZE),
                    def.push(P_SIZE));
            
            // normalize and prepare
            RandomChoice.organizeDistribution(sizeDistribution);
            }
        }

    public abstract GPNode newRootedTree(final EvolutionState state,
                                         final GPType type,
                                         final int thread,
                                         final GPNodeParent parent,
                                         final GPFunctionSet set,
                                         final int argposition,
                                         final int requestedSize);
        
    /** Issues a warning that no terminal was found with a return type of the given type, and that an algorithm
        had requested one.  If fail is true, then a fatal is issued rather than a warning.  The warning takes
        the form of a one-time big explanatory message, followed by a one-time-per-type message. */
    protected void warnAboutNoTerminalWithType(GPType type, boolean fail, EvolutionState state)
        {
        // big explanation -- appears only once
        state.output.warnOnce("A GPNodeBuilder has been requested at least once to generate a one-node tree with " +
                              "a return value type-compatable with a certain type; but there is no TERMINAL which is type-compatable " +
                              "in this way.  As a result, the algorithm was forced to use a NON-TERMINAL, making the tree larger than " +
                              "requested, and exposing more child slots to fill, which if not carefully considered, could " +
                              "recursively repeat this problem and eventually fill all memory.");
                
        // shorter explanation -- appears for each node builder and type combo
        if (fail)
            state.output.fatal("" + this.getClass() + " can't find a terminal type-compatable with " + type + 
                               " and cannot replace it with a nonterminal.  You may need to try a different node-builder algorithm.");
        else
            state.output.warnOnce("" + this.getClass() + " can't find a terminal type-compatable with " + type);
        }
                
    /** If the given test is true, issues a warning that no terminal was found with a return type of the given type, and that an algorithm
        had requested one.  If fail is true, then a fatal is issued rather than a warning.  The warning takes
        the form of a one-time big explanatory message, followed by a one-time-per-type message. Returns the value of the test.
        This form makes it easy to insert warnings into if-statements.  */
    protected boolean warnAboutNonterminal(boolean test, GPType type, boolean fail, EvolutionState state)
        {
        if (test) warnAboutNonTerminalWithType(type, fail, state);
        return test;
        }
         
    /** Issues a warning that no nonterminal was found with a return type of the given type, and that an algorithm
        had requested one.  If fail is true, then a fatal is issued rather than a warning.  The warning takes
        the form of a one-time big explanatory message, followed by a one-time-per-type message. */
    protected void warnAboutNonTerminalWithType(GPType type, boolean fail, EvolutionState state)
        {
        // big explanation -- appears only once
        state.output.warnOnce("A GPNodeBuilder has been requested at least once to generate a one-node tree with " +
                              "a return value type-compatable with a certain type; but there is no NON-TERMINAL which is type-compatable " +
                              "in this way.  As a result, the algorithm was forced to use a TERMINAL, making the tree larger than " +
                              "requested, and exposing more child slots to fill, which if not carefully considered, could " +
                              "recursively repeat this problem and eventually fill all memory.");
                
        // shorter explanation -- appears for each node builder and type combo
        if (fail)
            state.output.fatal("" + this.getClass() + " can't find a terminal type-compatable with " + type + 
                               " and cannot replace it with a nonterminal.  You may need to try a different node-builder algorithm.");
        else
            state.output.warnOnce("" + this.getClass() + " can't find a terminal type-compatable with " + type);
        }

    /** Issues a fatal error that no node (nonterminal or terminal) was found with a return type of the given type, and that an algorithm
        had requested one.  */
    protected void errorAboutNoNodeWithType(GPType type, EvolutionState state)
        {
        state.output.fatal("" + this.getClass() + " could find no terminal or nonterminal type-compatable with " + type);
        }
    }
