/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec;
import ec.util.*;
import ec.steadystate.*;

/* 
 * BreedingPipeline.java
 * 
 * Created: Tue Aug 17 21:37:10 1999
 * By: Sean Luke
 */

/**
 * A BreedingPipeline is a BreedingSource which provides "fresh" individuals which
 * can be used to fill a new population.  BreedingPipelines might include
 * Crossover pipelines, various Mutation pipelines, etc.  This abstract class
 * provides some default versions of various methods to simplify matters for you.
 * It also contains an array of breeding sources for your convenience.  You don't
 * have to use them of course, but this means you have to customize the
 * default methods below to make sure they get distributed to your special
 * sources.  Note that these sources may contain references to the same
 * object -- they're not necessarily distinct.  This is to provide both
 * some simple DAG features and also to conserve space.
 *
 *
 * <p>A BreedingPipeline implements SteadyStateBSourceForm, meaning that
 * it receives the individualReplaced(...) and sourcesAreProperForm(...) messages.
 * however by default it doesn't do anything with these except distribute them
 * to its sources.  You might override these to do something more interesting.

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>num-sources</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(User-specified number of sources to the pipeline.  
 Some pipelines have hard-coded numbers of sources; others indicate 
 (with the java constant DYNAMIC_SOURCES) that the number of sources is determined by this
 user parameter instead.)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>source.</tt><i>n</i><br>
 <font size=-1>classname, inherits and != BreedingSource, or the value <tt>same</tt><br>
 <td valign=top>(Source <i>n</i> for this BreedingPipeline.
 If the value is set to <tt>same</tt>, then this source is the
 exact same source object as <i>base</i>.<tt>source.</tt><i>n-1</i>, and
 further parameters for this object will be ignored and treated as the same 
 as those for <i>n-1</i>.  <tt>same<tt> is not valid for 
 <i>base</i>.<tt>source.0</tt>)</td></tr>
 </table>

 <p><b>Parameter bases</b><br>
 <table>

 <tr><td valign=top><i>base</i>.<tt>source.</tt><i>n</i><br>
 <td>Source <i>n</i></td></tr>
 </table>

 * @author Sean Luke
 * @version 1.0 
 */

public abstract class BreedingPipeline extends BreedingSource implements SteadyStateBSourceForm
    {
    /** Indicates that a source is the exact same source as the previous source. */
    public static final String V_SAME = "same";

    /** Indicates that the number of sources is variable and determined by the
        user in the parameter file. */

    public static final int DYNAMIC_SOURCES = 0;

    /** Standard parameter for number of sources (only used if numSources
        returns DYNAMIC_SOURCES */

    public static final String P_NUMSOURCES = "num-sources";

    /** Standard parameter for individual-selectors associated with a BreedingPipeline */
    public static final String P_SOURCE = "source";

    /** My parameter base -- I keep it around so I can print some messages that
        are useful with it (not deep cloned) */
        
    public Parameter mybase;

    /** Array of sources feeding the pipeline */
    public BreedingSource[] sources;

    /** Returns the number of sources to this pipeline.  Called during
        BreedingPipeline's setup.  Be sure to return a value > 0, or
        DYNAMIC_SOURCES which indicates that setup should check the parameter
        file for the parameter "num-sources" to make its determination. */

    public abstract int numSources();
            
    /** Returns the minimum among the typicalIndsProduced() for any children --
        a function that's useful internally, not very useful for you to call externally. */
    public int minChildProduction() 
        {
        if (sources.length==0) return 0;
        int min = sources[0].typicalIndsProduced();
        for(int x=1;x<sources.length;x++)
            {
            int cur = sources[x].typicalIndsProduced();
            if (min > cur) min = cur;
            }
        return min;
        }

    /** Returns the maximum among the typicalIndsProduced() for any children --
        a function that's useful internally, not very useful for you to call externally. */
    public int maxChildProduction() 
        {
        if (sources.length==0) return 0;
        int max = sources[0].typicalIndsProduced();
        for(int x=1;x<sources.length;x++)
            {
            int cur = sources[x].typicalIndsProduced();
            if (max < cur) max = cur;
            }
        return max;
        }


    /** Returns the "typical" number of individuals produced -- by default
        this is the minimum typical number of individuals produced by any
        children sources of the pipeline.  If you'd prefer something different,
        override this method. */
    public int typicalIndsProduced()
        {
        return minChildProduction();
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        mybase = base;
        
        Parameter def = defaultBase();

        int numsources = numSources();
        if (numsources <= DYNAMIC_SOURCES)
            {
            // figure it from the file
            numsources = state.parameters.getInt(
                base.push(P_NUMSOURCES), def.push(P_NUMSOURCES),1);
            if (numsources==0)
                state.output.fatal("Breeding pipeline num-sources value must be > 0",
                                   base.push(P_NUMSOURCES),
                                   def.push(P_NUMSOURCES)); 
            }

        sources = new BreedingSource[numsources];

        for(int x=0;x<sources.length;x++)
            {
            Parameter p = base.push(P_SOURCE).push(""+x);
            Parameter d = def.push(P_SOURCE).push(""+x);

            String s = state.parameters.getString(p,d);
            if (s!=null && s.equals(V_SAME))
                {
                if (x==0)  // oops
                    state.output.fatal(
                        "Source #0 cannot be declared with the value \"same\".",
                        p,d);
                
                // else the source is the same source as before
                sources[x] = sources[x-1];
                }
            else 
                {
                sources[x] = (BreedingSource)
                    (state.parameters.getInstanceForParameter(
                        p,d,BreedingSource.class));
                sources[x].setup(state,p);
                }
            }
        state.output.exitIfErrors();
        }


    public Object clone()
        {
        BreedingPipeline c = (BreedingPipeline)(super.clone());
        
        // make a new array
        c.sources = new BreedingSource[sources.length];

        // clone the sources -- we won't go through the hassle of
        // determining if we have a DAG or not -- we'll just clone
        // it out to a tree.  I doubt it's worth it.

        for(int x=0;x<sources.length;x++)
            {
            if (x==0 || sources[x]!=sources[x-1])
                c.sources[x] = (BreedingSource)(sources[x].clone());
            else 
                c.sources[x] = c.sources[x-1];
            }

        return c;
        }



    public boolean produces(final EvolutionState state,
                            final Population newpop,
                            final int subpopulation,
                            int thread)
        {
        for(int x=0;x<sources.length;x++)
            if (x==0 || sources[x]!=sources[x-1])
                if (!sources[x].produces(state,newpop,subpopulation,thread))
                    return false;
        return true;
        }

    public void prepareToProduce(final EvolutionState state,
                                 final int subpopulation,
                                 final int thread)
        {
        for(int x=0;x<sources.length;x++) 
            if (x==0 || sources[x]!=sources[x-1])
                sources[x].prepareToProduce(state,subpopulation,thread);
        }

    public void finishProducing(final EvolutionState state,
                                final int subpopulation,
                                final int thread)
        {
        for(int x=0;x<sources.length;x++) 
            if (x==0 || sources[x]!=sources[x-1])
                sources[x].finishProducing(state,subpopulation,thread);
        }

    public void preparePipeline(Object hook)
        {
        // the default form calls this on all the sources.
        // note that it follows all the source paths even if they're
        // duplicates
        for(int x=0; x<sources.length;x++) 
            sources[x].preparePipeline(hook);
        }
        
    public void individualReplaced(final SteadyStateEvolutionState state,
                                   final int subpopulation,
                                   final int thread,
                                   final int individual)
        {
        for(int x=0; x<sources.length;x++) 
            ((SteadyStateBSourceForm)(sources[x])).individualReplaced(state,subpopulation,thread,individual);
        }

    public void sourcesAreProperForm(final SteadyStateEvolutionState state)
        {
        for(int x=0; x<sources.length;x++) 
            if (! (sources[x] instanceof SteadyStateBSourceForm))
                {
                state.output.error("The following breeding source is not of SteadyStateBSourceForm.", 
                                   mybase.push(P_SOURCE).push(""+x), defaultBase().push(P_SOURCE).push(""+x));
                }
            else 
                ((SteadyStateBSourceForm)(sources[x])).sourcesAreProperForm(state);
        }

    }


