/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.vector.breed;

/* 
 * ListCrossoverPipeline.java
 * 
 * Created: Sat 23 May 2009 11:57:17 AM EDT
 * By: Stephen Donnelly
 */
 
import ec.vector.*;
import ec.*;
import ec.util.*;


/**
   ListCrossoverPipeline is a crossover pipeline for vector individuals whose length
   may be lengthened or shortened.  There are two crossover options available: one-point
   and two-point.  One-point crossover picks a crossover point for each of the vectors
   (the crossover point can be different), and then does one-point crossover using those
   points.  Two-point crossover picks TWO crossover points for each of the vectors (again,
   the points can be different among the vectors), and swaps the middle regions between
   the respective crossover points.

   <p>ListCrossoverPipeline will try tries times to meet certain constraints: first,
   the resulting children must be no smaller than min-child-size.  Second, the amount
   of material removed from a parent must be no less than mix-crossover-percent and no 
   more than max-crossover-percent.
        
   <p>If toss is true, then only one child is generated, else at most two are generated.

   <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
   2 * minimum typical number of individuals produced by each source, unless toss
   is set, in which case it's simply the minimum typical number.

   <p><b>Number of Sources</b><br>
   2

   <p><b>Parameters</b><br>
   <table>
   <tr><td valign=top><i>base</i>.<tt>toss</tt><br>
   <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</font>/td>
   <td valign=top>(after crossing over with the first new individual, should its second sibling individual be thrown away instead of adding it to 
   the population?)</td></tr>

   <tr><td valign=top><i>base</i>.<tt>tries</tt><br>
   <font size=-1>int &gt;= 1</font></td>
   <td valign=top>(number of times to try finding valid crossover points)</td></tr>

   <tr><td valign=top><i>base</i>.<tt>min-child-size</tt><br>
   <font size=-1>int &gt;= 0 (default)</font></td>
   <td valign=top>(the minimum allowed size of a child)</td></tr>

   <tr><td valign=top><i>base</i>.<tt>min-crossover-percent</tt><br>
   <font size=-1>0 (default) &lt;= double &lt;= 1</font></td>
   <td valign=top>(the minimum percentage of an individual that may be removed during crossover)</td></tr>

   <tr><td valign=top><i>base</i>.<tt>max-crossover-percent</tt><br>
   <font size=-1>0 &lt;= double &lt;= 1 (default)</font></td>
   <td valign=top>(the maximum percentage of an individual that may be removed during crossover)</td></tr>

   </table>

   <p><b>Default Base</b><br>
   vector.list-xover


**/

public class ListCrossoverPipeline extends BreedingPipeline
    {
    public static final String P_TOSS = "toss";
    public static final String P_LIST_CROSSOVER = "list-xover";
    public static final String P_MIN_CHILD_SIZE = "min-child-size";
    public static final String P_NUM_TRIES = "tries";
    public static final String P_MIN_CROSSOVER_PERCENT = "min-crossover-percent";
    public static final String P_MAX_CROSSOVER_PERCENT = "max-crossover-percent";
    public static final int NUM_SOURCES = 2;
    
    public boolean tossSecondParent;
    public int crossoverType;
    public int minChildSize;
    public int numTries;
    public double minCrossoverPercentage;
    public double maxCrossoverPercentage;
    
    protected VectorIndividual parents[];
    
    public ListCrossoverPipeline() { parents = new VectorIndividual[2]; }
    public Parameter defaultBase() { return VectorDefaults.base().push(P_LIST_CROSSOVER); }

    public int numSources() { return NUM_SOURCES; }

    public Object clone()
        {
        ListCrossoverPipeline c = (ListCrossoverPipeline)(super.clone());
        c.parents = (VectorIndividual[]) parents.clone();
        return c;
        }
   

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        Parameter def = defaultBase();
        
        tossSecondParent = state.parameters.getBoolean(base.push(P_TOSS),
            def.push(P_TOSS),false);
                                           
        minChildSize = state.parameters.getIntWithDefault(base.push(P_MIN_CHILD_SIZE),
            def.push(P_MIN_CHILD_SIZE), 0);
                                                            
        numTries = state.parameters.getIntWithDefault(base.push(P_NUM_TRIES),
            def.push(P_NUM_TRIES), 1);
                                                         
        minCrossoverPercentage = state.parameters.getDoubleWithDefault(base.push(P_MIN_CROSSOVER_PERCENT),
            def.push(P_MIN_CROSSOVER_PERCENT), 0.0);
        maxCrossoverPercentage = state.parameters.getDoubleWithDefault(base.push(P_MAX_CROSSOVER_PERCENT),
            def.push(P_MAX_CROSSOVER_PERCENT), 1.0);
                                                         

        String crossoverTypeString = state.parameters.getStringWithDefault(base.push(VectorSpecies.P_CROSSOVERTYPE),
            def.push(VectorSpecies.P_CROSSOVERTYPE),
            VectorSpecies.V_TWO_POINT);
                                                                             
        // determine the crossover method to use (only 1-point & 2-point currently supported)
        if(crossoverTypeString.equalsIgnoreCase(VectorSpecies.V_ONE_POINT))
            {
            crossoverType = VectorSpecies.C_ONE_POINT;
            }
        else if(crossoverTypeString.equalsIgnoreCase(VectorSpecies.V_TWO_POINT))
            {
            crossoverType = VectorSpecies.C_TWO_POINT;
            }
        else
            {
            state.output.error("ListCrossoverPipeline:\n:" +
                "   Parameter crossover-type is currently set to: " + crossoverTypeString + "\n" +
                "   Currently supported crossover types are \"one\" and \"two\" point.\n");
            }
        
        // sanity check for crossover parameters
        if(minChildSize < 0)
            {
            state.output.error("ListCrossoverPipeline:\n" +
                "   Parameter min-child-size is currently equal to: " + Integer.toString(minChildSize) + "\n" +
                "   min-child-size must be a positive integer\n");
            }
        
        if(numTries < 1)
            {
            state.output.error("ListCrossoverPipeline:\n" +
                "   Parameter tries is currently equal to: " + Integer.toString(numTries) + "\n" +
                "   tries must be greater than or equal to 1\n");
            }
                               
        
        if(minCrossoverPercentage < 0.0 || minCrossoverPercentage > 1.0)
            {
            state.output.error("ListCrossoverPipeline:\n" +
                "   Parameter min-crossover-percent is currently equal to: " + Double.toString(minCrossoverPercentage) + "\n" +
                "   min-crossover-percent must be either a real-value double float between [0.0, 1.0] or left unspecified\n");
            }
        if(maxCrossoverPercentage < 0.0 || maxCrossoverPercentage > 1.0)
            {
            state.output.error("ListCrossoverPipeline:\n" +
                "   Parameter max-crossover-percent is currently equal to: " + Double.toString(maxCrossoverPercentage) + "\n" +
                "   max-crossover-percent must be either a real-value double float between [0.0, 1.0] or left unspecified\n");
            }
        if(minCrossoverPercentage > maxCrossoverPercentage)
            {
            state.output.error("ListCrossoverPipeline:\n" +
                "   Parameter min-crossover-percent must be less than max-crossover-percent\n");
            }
        if(minCrossoverPercentage == maxCrossoverPercentage)
            {
            state.output.warning("ListCrossoverPipeline:\n" +
                "   Parameter min-crossover-percent and max-crossover-percent are currently equal to: " + 
                Double.toString(minCrossoverPercentage) + "\n" +
                "   This effectively prevents any crossover from occurring\n");
            }
        }
    
    
    public int typicalIndsProduced()
        {
        return (tossSecondParent? minChildProduction(): minChildProduction()*2);
        }
        
    
    
    public int produce(final int min, 
        final int max, 
        final int start,
        final int subpopulation,
        final Individual[] inds,
        final EvolutionState state,
        final int thread) 

        {
       
        // how many individuals should we make?
        int n = typicalIndsProduced();
        if (n < min) n = min;
        if (n > max) n = max;
             

        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            return reproduce(n, start, subpopulation, inds, state, thread, true);  // DO produce children from source -- we've not done so already

        for(int q=start;q<n+start; /* no increment */)  // keep on going until we're filled up
            {
            // grab two individuals from our sources
            if (sources[0]==sources[1])  // grab from the same source
                {
                sources[0].produce(2,2,0,subpopulation,parents,state,thread);
                if (!(sources[0] instanceof BreedingPipeline))  // it's a selection method probably
                    { 
                    parents[0] = (VectorIndividual)(parents[0].clone());
                    parents[1] = (VectorIndividual)(parents[1].clone());
                    }
                }
            else // grab from different sources
                {
                sources[0].produce(1,1,0,subpopulation,parents,state,thread);
                sources[1].produce(1,1,1,subpopulation,parents,state,thread);
                if (!(sources[0] instanceof BreedingPipeline))  // it's a selection method probably
                    parents[0] = (VectorIndividual)(parents[0].clone());
                if (!(sources[1] instanceof BreedingPipeline)) // it's a selection method probably
                    parents[1] = (VectorIndividual)(parents[1].clone());
                }
                
            // determines size of parents, in terms of chunks
            int chunk_size = ((VectorSpecies)(parents[0].species)).chunksize;  
            int[] size = new int[2];  // sizes of parents
            size[0] = (int)parents[0].genomeLength();
            size[1] = (int)parents[1].genomeLength();      
            int[] size_in_chunks = new int[2];   // sizes of parents by chunk (if chunk == 1, this is just size[])
            size_in_chunks[0] = size[0]/chunk_size;
            size_in_chunks[1] = size[1]/chunk_size;
            
            // variables used to split & join the children
            int[] min_chunks = new int[2];  // the minimum number of chunks permitted 
            int[] max_chunks = new int[2];  // the maximum number of chunks permitted
            int[][] split = new int[2][2];
            Object[][] pieces = new Object[2][3];
            
            // determine min and max crossover segment lengths, in terms of chunks
            for(int i = 0; i < 2; i++)
                {
                min_chunks[i] = (int)(size_in_chunks[i]*minCrossoverPercentage);
                // round minCrossoverPercentage up to nearest chunk boundary
                if(size[i] % chunk_size != 0 && min_chunks[i] < size_in_chunks[i])
                    {
                    min_chunks[i]++;
                    }
                max_chunks[i] = (int)(size_in_chunks[i]*maxCrossoverPercentage);
                }

            Object validationData = computeValidationData(state, parents, thread);
            
            // attempt 'num-tries' times to produce valid children (which are bigger than min-child-size)
            boolean valid_children = false;
            int attempts = 0;
            while(valid_children == false && attempts < numTries)
                {
                // generate split indices for one-point (tail end used as end of segment)
                if(crossoverType == VectorSpecies.C_ONE_POINT)
                    {
                    for(int i = 0; i < 2; i++)
                        {
                        // select first index at most 'max_chunks' away from tail end of vector
                        split[i][0] = size_in_chunks[i] - max_chunks[i];
                        // shift back towards tail end with random value based on min/max parameters
                        split[i][0] += state.random[thread].nextInt(max_chunks[i] - min_chunks[i]);
                        // convert split from chunk numbers to array indices
                        split[i][0] *= chunk_size;
                        // select tail end chunk boundary as second split index
                        split[i][1] = size_in_chunks[i]*chunk_size;
                        }
                    }
               
                else if(crossoverType == VectorSpecies.C_TWO_POINT)  // Note that NOOPs are permissible
                    {
                    for(int i = 0; i < 2; i++)
                        {
                        while(true)  // we'll do rejection sampling for two point.  It's slower, maybe much slower, but uniform
                            {
                            split[i][0] = state.random[thread].nextInt(size_in_chunks[i] + 1);  // can go clear to end
                            split[i][1] = state.random[thread].nextInt(size_in_chunks[i] + 1);  // likewise
                                
                            if (split[i][0] > split[i][1])  // swap so 0 is before 1
                                {
                                int temp = split[i][0];
                                split[i][0] = split[i][1];
                                split[i][1] = temp;
                                }
                                        
                            int len = split[i][0] - split[i][1];
                            if (len >= min_chunks[i] && len <= max_chunks[i])  // okay
                                {
                                split[i][0] *= chunk_size;
                                split[i][1] *= chunk_size;
                                break;
                                }
                            attempts++;
                            if (attempts > numTries) break;  // uh oh
                            }
                        }
                    }
                else state.output.fatal("Unknown crossover type specified: " + crossoverType);  // shouldn't ever happen
               
                if (attempts >= numTries) break;  // failed in two-point selection
               
                // use the split indices generated above to split the parents into pieces
                parents[0].split(split[0], pieces[0]);
                parents[1].split(split[1], pieces[1]);         
               
                // create copies of the parents, swap the middle segment, and then rejoin the pieces
                // - this is done to test whether or not the resulting children are of a valid size,
                // - because we are using Object references to an undetermined array type, there is no way 
                //   to cast it to the appropriate array type (i.e. short[] or double[]) to figure out the
                //   length of the pieces
                // - instead, we use the join method on copies, and let each vector type figure out its own
                //   length with the genomeLength() method
                VectorIndividual[] children = new VectorIndividual[2];
                children[0] = (VectorIndividual)(parents[0].clone());
                children[1] = (VectorIndividual)(parents[1].clone());
               
                Object swap = pieces[0][1];
                pieces[0][1] = pieces[1][1];
                pieces[1][1] = swap;
                    
                children[0].join(pieces[0]);
                children[1].join(pieces[1]);
                if(children[0].genomeLength() > minChildSize && children[1].genomeLength() > minChildSize && isValidated(split, validationData))
                    {
                    valid_children = true;
                    }
                attempts++;
                }
           
            // if the children produced were valid, updates the parents
            if(valid_children == true)
                {
                parents[0].join(pieces[0]);
                parents[1].join(pieces[1]);
                parents[0].evaluated=false;
                parents[1].evaluated=false;
                }
            // insert parents back into the population
            inds[q] = parents[0];
            q++;
            if(q < n + start && tossSecondParent == false)
                {
                inds[q] = parents[1];
                q++;
                }
            } 
        
        return n;
        }    
    
    /** A hook called by ListCrossoverPipeline to allow subclasses to prepare for additional validation testing. 
        Primarily used by GECrossoverPipeline.  */ 
    public Object computeValidationData(EvolutionState state, VectorIndividual[] parents, int thread)
        {
        return null;
        }

    /** A hook called by ListCrossoverPipeline to allow subclasses to further validate children crossover points. 
        Primarily used by GECrossoverPipeline.  */ 
    public boolean isValidated(int[][] split, Object validationData)
        {
        return true;
        }

    }
    
    
    
    
    
    
    
