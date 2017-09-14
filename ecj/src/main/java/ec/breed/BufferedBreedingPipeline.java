/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.breed;
import ec.*;
import ec.util.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/* 
 * BufferedBreedingPipeline.java
 * 
 * Created: December 28, 1999
 * By: Sean Luke
 */

/**
 * If empty, a 
 * BufferedBreedingPipeline makes a request of exactly <i>num-inds</i> 
 * individuals from a single child source; it then uses these
 * individuals to fill requests (returning min each time),
 * until the buffer is emptied, at
 * which time it grabs exactly <i>num-inds</i> more individuals, and so on.
 *
 * <p>What is this useful for?  Well, let's say for example that 
 * you want to cross over two individuals, then cross
 * them over again.  You'd like to hook up two CrossoverPipelines
 * in series.  Unfortunately, CrossoverPipeline takes
 * two sources; even if you set them to the same source, it requests
 * <i>one</i> individual from the first source and then <i>one</i>
 * from the second, where what you really want is for it to request
 * <i>two</i> individuals from a single source (the other CrossoverPipeline).
 * 
 * <p>The solution to this is to hook a CrossoverPipeline as the
 * source to a BufferedBreedingPipeline of buffer-size 2 (or some
 * multiple of 2 actually).  Then the BufferedBreedingPipeline is
 * set as both sources to another CrossoverPipeline.
 
 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 1

 <p><b>Number of Sources</b><br>
 1
 
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>num-inds</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(the buffer size)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 breed.buffered

 *
 * @author Sean Luke
 * @version 1.0 
 */

public class BufferedBreedingPipeline extends BreedingPipeline
    {
    public static final String P_BUFSIZE = "num-inds";
    public static final String P_BUFFERED = "buffered";
    public static final int INDS_PRODUCED = 1;
    public static final int NUM_SOURCES = 1;

    public ArrayList<Individual> buffer;
    public int bufSize;

    public Parameter defaultBase()
        {
        return BreedDefaults.base().push(P_BUFFERED);
        }

    public int numSources() { return NUM_SOURCES; }    
    public int typicalIndsProduced() { return INDS_PRODUCED;}

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);

        Parameter def = defaultBase();

        bufSize = state.parameters.getInt(base.push(P_BUFSIZE),
            def.push(P_BUFSIZE),1);
        if (bufSize == 0)
            state.output.fatal("BufferedBreedingPipeline's number of individuals must be >= 1.",base.push(P_BUFSIZE),def.push(P_BUFSIZE));
        
        buffer = new ArrayList<Individual>();

        // declare that likelihood isn't used
        if (likelihood < 1.0)
            state.output.warning("BufferedBreedingPipeline does not respond to the 'likelihood' parameter.",
                base.push(P_LIKELIHOOD), def.push(P_LIKELIHOOD));
        }


    public void prepareToProduce(final EvolutionState state,
        final int subpopulation,
        final int thread)
        {
        super.prepareToProduce(state,subpopulation,thread);
        // reset my number of individuals to 0
        buffer.clear();
        }


    public int produce(final int min,
        final int max,
        final int subpopulation,
        final ArrayList<Individual> inds,
        final EvolutionState state,
        final int thread, HashMap<String, Object> misc)

        {
        for(int q=0; q<min; q++ )
            {
            if (buffer.isEmpty())       // reload
                {
                sources[0].produce(bufSize, bufSize, subpopulation, buffer, state, thread, misc);
                }
            
            inds.add(buffer.remove(buffer.size() - 1));
            }
        return min;
        }
    }
