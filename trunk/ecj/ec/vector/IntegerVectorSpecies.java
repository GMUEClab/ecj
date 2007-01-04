/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.vector;
import ec.*;
import ec.util.*;

/* 
 * IntegerVectorSpecies.java
 * 
 * Created: Tue Feb 20 13:26:00 2001
 * By: Sean Luke
 */

/**
 * IntegerVectorSpecies is a subclass of VectorSpecies with special constraints
 * for integral vectors, namely ByteVectorIndividual, ShortVectorIndividual,
 * IntegerVectorIndividual, and LongVectorIndividual.
 
 * <p>IntegerVectorSpecies can specify min/max numeric constraints on gene values
 * in three different ways.
 *
 * <ol>
 * <li> You may provide a default min and max value.
 *      This is done by specifying:
 *      <p><i>base.n</i>.<tt>min-gene</tt>
 *      <br><i>base.n</i>.<tt>max-gene</tt>
 *      <p><i>Note:</i> you <b>must</b> provide these values even if you don't use them,
 *      as they're used as defaults by #2 and #3 below.
 *<p>
 * <li> You may provide min and max values for genes in segments (regions) along
 *      the genome.  If not all genes are specified in this way, the default (#1) min and max
 *      value is used, and you receive a warning.  ECJ will check and use this method before
 *      it checks and uses #3 below.  This is done by specifying:
 *      <p><i>base</i>.<tt>num-segments</tt>
 *      The segments may be defined by either start or end indices of genes. 
 *      This is controlled by specifying the value of:
 *      <p><i>base</i>.<tt>segment-type</tt>
 *      which can assume the value of start or end, with start being the default.
 *      The indices are defined using Java array style, i.e. the first gene has the index of 0, 
 *      and the last gene has the index of genome-size - 1.
 *      <p>Using this method, each segment is specified by<i>j</i>...
 *      <p><i>base</i>.<tt>segment.</tt><i>j</i><tt>.start</tt>
 *      <br><i>base</i>.<tt>segment.</tt><i>j</i><tt>.min-gene</tt>
 *      <br><i>base</i>.<tt>segment.</tt><i>j</i><tt>.max-gene</tt>
 *      if segment-type value was chosen as start or by:
 *      <p><i>base</i>.<tt>segment.</tt><i>j</i><tt>.end</tt>
 *      <br><i>base</i>.<tt>segment.</tt><i>j</i><tt>.min-gene</tt>
 *      <br><i>base</i>.<tt>segment.</tt><i>j</i><tt>.max-gene</tt>
 *      if segment-type value is equal to end.
 *<p>
 * <li> You may provide min and max values for each separate gene.  If not all genes
 *      are specified in this way, the default (#1) min and max value is used, and you
 *      receive a warning.  This is done by specifying (for each gene location <i>i</i>
 *      you wish to specify)
 *      <p><i>base.n</i>.<tt>min-gene</tt>.<i>i</i>
 *      <br><i>base.n</i>.<tt>max-gene</tt>.<i>i</i>
 * </ol>
 * 
 *
 *
 *
 * <p><b>Parameters</b><br>
 * <table>
 * <tr><td valign=top><i>base.n</i>.<tt>min-gene</tt><br>
 * <font size=-1>long (default=0)</font></td>
 * <td valign=top>(the minimum gene value)</td></tr>
 *
 * <tr><td valign=top><i>base.n</i>.<tt>max-gene</tt><br>
 * <font size=-1>long &gt;= <i>base.n</i>.min-gene</font></td>
 * <td valign=top>(the maximum gene value)</td></tr>
 *
 * <tr><td valign=top><i>base.n</i>.<tt>min-gene</tt>.<i>i</i><br>
 * <font size=-1>long (default=<i>base.n</i>.<tt>min-gene</tt>)</font></td>
 * <td valign=top>(the minimum gene value for gene <i>i</i>)</td></tr>
 *
 * <tr><td valign=top><i>base.n</i>.<tt>max-gene</tt>.<i>i</i><br>
 * <font size=-1>long &gt;= <i>base.n</i>.min-gene.<i>i</i> (default=<i>base.n</i>.<tt>max-gene</tt>)</font></td>
 * <td valign=top>(the maximum gene value for gene <i>i</i>)</td></tr>
 *
 * <tr><td valign=top><i>base.</i>.<tt>num-segments</tt><br>
 * <font size=-1>int &gt;= 1 (default=no segments used)</font></td>
 * <td valign=top>(the number of gene segments defined)</td>
 * </tr>
 * 
 * <tr><td valign=top><i>base.</i>.<tt>segment-type</tt><br>
 * <font size=-1>int &gt;= 1 (default=start)</font></td>
 * <td valign=top>(defines the way in which segments are defined: either by providing start indices (segment-type=start) or by providing end indices (segment-type=end)</td>
 * </tr>
 *
 * <tr><td valign=top><i>base.</i>.<tt>segment</tt>.<i>j</i>.<tt>start</tt><br>
 * <font size=-1>0 &lt;= int &lt; genome length</font></td>
 * <td valign=top>(the start index of gene segment <i>j</i> -- the end of a segment is before the start of the next segment)</td>
 * <td valign=top>(used when the value of segment-type parameter is equal to start)</td>
 * </tr>
 *
 * <tr><td valign=top><i>base.</i>.<tt>segment</tt>.<i>j</i>.<tt>end</tt><br>
 * <font size=-1>0 &lt;= int &lt; genome length</font></td>
 * <td valign=top>(the end of gene segment <i>j</i> -- the start of a segment is after the end of the previous segment)</td>
 * <td valign=top>(used when the value of segment-type parameter is equal to end)</td>
 * </tr>
 *
 * <tr><td valign=top><i>base.</i>.<tt>segment</tt>.<i>j</i>.<tt>min-gene</tt><br>
 * <font size=-1>double (default=0.0)</font></td>
 * <td valign=top>(the minimum gene value for segment <i>j</i>)</td>
 * </tr>
 *
 * <tr><td valign=top><i>base.</i>.<tt>segment</tt>.<i>j</i>.<tt>max-gene</tt><br>
 * <font size=-1>double &gt;= <i>base.</i>.<tt>segment</tt>.<i>j</i>.<tt>min-gene</tt></td>
 * <td valign=top>(the maximum gene value for segment <i>j</i>)</td>
 * </tr>
 * 
 * </table>
 * @author Sean Luke, Rafal Kicinger
 * @version 1.0 
 */
public class IntegerVectorSpecies extends VectorSpecies
    {
    public final static String P_MINGENE = "min-gene";
    public final static String P_MAXGENE = "max-gene";
    
    public final static String P_NUM_SEGMENTS = "num-segments";
        
    public final static String P_SEGMENT_TYPE = "segment-type";

    public final static String P_SEGMENT_START = "start";
        
    public final static String P_SEGMENT_END = "end";

    public final static String P_SEGMENT = "segment";
        
    public long minGene;
    public long maxGene;
    /** Set to null if not specified */
    public long[] minGenes;
    /** Set to null if not specified */
    public long[] maxGenes;
    
    public final boolean individualGeneMinMaxUsed()
        {
        return (maxGenes!=null);
        }
        
    public final long maxGene(int gene)
        {
        if (maxGenes!=null && gene >= 0 && gene < maxGenes.length)
            return maxGenes[gene];
        else return maxGene;
        }
    
    public final long minGene(int gene)
        {
        if (minGenes!=null && gene >= 0 && gene < minGenes.length)
            return minGenes[gene];
        else return minGene;
        }
    
    public boolean inNumericalTypeRange(long geneVal)
        {
        if (i_prototype instanceof ByteVectorIndividual)
            return (geneVal <= Byte.MAX_VALUE && geneVal >= Byte.MIN_VALUE);
        else if (i_prototype instanceof ShortVectorIndividual)
            return (geneVal <= Short.MAX_VALUE && geneVal >= Short.MIN_VALUE);
        else if (i_prototype instanceof IntegerVectorIndividual)
            return (geneVal <= Integer.MAX_VALUE && geneVal >= Integer.MIN_VALUE);
        else if (i_prototype instanceof LongVectorIndividual)
            return true;  // geneVal is valid for all longs
        else return false;  // dunno what the individual is...
        }
    
    public void setup(final EvolutionState state, final Parameter base)
        {
        // keep in mind that the *species* variable has not been set yet.
        super.setup(state,base);

        Parameter def = defaultBase();

        minGene = state.parameters.getLongWithDefault(base.push(P_MINGENE),def.push(P_MINGENE),0);
        maxGene = state.parameters.getLong(base.push(P_MAXGENE),def.push(P_MAXGENE),minGene);
        if (maxGene < minGene)
            state.output.fatal("IntegerVectorSpecies must have a default min-gene which is <= the default max-gene",
                               base.push(P_MAXGENE),def.push(P_MAXGENE));
        
        // check to see if these longs are within the data type of the particular individual
        if (!inNumericalTypeRange(minGene))
            state.output.fatal("This IntegerVectorSpecies has a prototype of the kind: " 
                               + i_prototype.getClass().getName() +
                               ", but doesn't have a min-gene value within the range of this prototype's genome's data types",
                               base.push(P_MINGENE),def.push(P_MINGENE));
        if (!inNumericalTypeRange(maxGene))
            state.output.fatal("This IntegerVectorSpecies has a prototype of the kind: " 
                               + i_prototype.getClass().getName() +
                               ", but doesn't have a max-gene value within the range of this prototype's genome's data types",
                               base.push(P_MAXGENE),def.push(P_MAXGENE));

        //Set number of segments to 0 by default
        int numSegments = 0;
        // Now check to see if segments of genes (genes having the same min and
        // max values) exist
        if (state.parameters.exists(base.push(P_NUM_SEGMENTS), def.push(P_NUM_SEGMENTS)))
            {
            numSegments = state.parameters.getIntWithDefault(base.push(P_NUM_SEGMENTS), 
                                                             def.push(P_NUM_SEGMENTS), 0);
                        
            if(numSegments == 0)
                state.output.warning(
                    "The number of genome segments has been defined to be equal to 0.\n"
                    + "Hence, no genome segments will be defined.", 
                    base.push(P_NUM_SEGMENTS), 
                    def.push(P_NUM_SEGMENTS));
            else if(numSegments < 0)
                state.output.fatal(
                    "Invalid number of genome segments: " + numSegments
                    + "\nIt must be a nonnegative value.", 
                    base.push(P_NUM_SEGMENTS), 
                    def.push(P_NUM_SEGMENTS));
                                
                                
            //Initialize min and max gene arrays
            minGenes = new long[genomeSize];
            maxGenes = new long[genomeSize];

                        
                        
            //read the type of segment definition using the default start value
            String segmentType = state.parameters.getStringWithDefault(base.push(P_SEGMENT_TYPE), 
                                                                       def.push(P_SEGMENT_TYPE), P_SEGMENT_START);
                        
            if(segmentType.equalsIgnoreCase(P_SEGMENT_START))
                initializeGenomeSegmentsByStartIndices(state, base, def, numSegments);
            else if(segmentType.equalsIgnoreCase(P_SEGMENT_END))
                initializeGenomeSegmentsByEndIndices(state, base, def, numSegments);
            else
                state.output.fatal(
                    "Invalid specification of genome segment type: " + segmentType
                    + "\nThe " + P_SEGMENT_TYPE + " parameter must have the value of " + P_SEGMENT_START + " or " + P_SEGMENT_END, 
                    base.push(P_SEGMENT_TYPE), 
                    def.push(P_SEGMENT_TYPE));


            }
        // Next check to see if the gene-by-gene min/max values exist
        else if (state.parameters.exists(base.push(P_MAXGENE).push("0"),def.push(P_MAXGENE).push("0")))
            {
            minGenes = new long[genomeSize];
            maxGenes = new long[genomeSize];
            boolean warnedMin=false;
            boolean warnedMax=false;
            for(int x=0;x<genomeSize;x++)
                {
                minGenes[x]=minGene;
                maxGenes[x]=maxGene;
                if (!state.parameters.exists(base.push(P_MINGENE).push(""+x),base.push(P_MINGENE).push(""+x)))
                    {
                    if (!warnedMin)
                        {
                        state.output.warning("IntegerVectorSpecies has missing min-gene values for some genes.\n" +
                                             "The first one is gene #"+x+".", base.push(P_MINGENE).push(""+x),base.push(P_MINGENE).push(""+x));
                        warnedMin = true;
                        }
                    }
                else minGenes[x] = state.parameters.getLongWithDefault(
                    base.push(P_MINGENE).push(""+x),base.push(P_MINGENE).push(""+x),minGene);

                if (!state.parameters.exists(base.push(P_MAXGENE).push(""+x),base.push(P_MAXGENE).push(""+x)))
                    {
                    if (!warnedMax)
                        {
                        state.output.warning("IntegerVectorSpecies has missing max-gene values for some genes.\n" +
                                             "The first one is gene #"+x+".", base.push(P_MAXGENE).push(""+x),base.push(P_MAXGENE).push(""+x));
                        warnedMax = true;
                        }
                    }
                else maxGenes[x] = state.parameters.getLongWithDefault(base.push(P_MAXGENE).push(""+x),base.push(P_MAXGENE).push(""+x),maxGene);
                
                if (maxGenes[x] < minGenes[x])
                    state.output.fatal("IntegerVectorSpecies must have a min-gene["+x+"] which is <= the max-gene["+x+"]",
                                       base.push(P_MAXGENE).push(""+x),base.push(P_MAXGENE).push(""+x));
                
                // check to see if these longs are within the data type of the particular individual
                if (!inNumericalTypeRange(minGenes[x]))
                    state.output.error("This IntegerVectorSpecies has a prototype of the kind: " 
                                       + i_prototype.getClass().getName() +
                                       ", but doesn't have a min-gene["+x+"] value within the range of this prototype's genome's data types",
                                       base.push(P_MINGENE).push(""+x),base.push(P_MINGENE).push(""+x));
                if (!inNumericalTypeRange(maxGenes[x]))
                    state.output.fatal("This IntegerVectorSpecies has a prototype of the kind: " 
                                       + i_prototype.getClass().getName() +
                                       ", but doesn't have a max-gene["+x+"] value within the range of this prototype's genome's data types",
                                       base.push(P_MAXGENE).push(""+x),base.push(P_MAXGENE).push(""+x));
                }
            }
        else  //initialize minGenes and maxGenes based on global mix and max values
            {
            minGenes = new long[genomeSize];
            maxGenes = new long[genomeSize];
            for (int x = 0; x < genomeSize; x++)
                {
                minGenes[x] = minGene;
                maxGenes[x] = maxGene;
                }
            }
                
        /*
        //Debugging
        for(int i = 0; i < minGenes.length; i++)
        System.out.println("Min: " + minGenes[i] + ", Max: " + maxGenes[i]);
        */
        }
    
        
    private void initializeGenomeSegmentsByStartIndices(final EvolutionState state, 
                                                        final Parameter base, 
                                                        final Parameter def,
                                                        int numSegments)
        {
        boolean warnedMin = false;
        boolean warnedMax = false;
        long currentSegmentMinGeneValue = Long.MAX_VALUE;
        long currentSegmentMaxGeneValue = Long.MIN_VALUE;
                
        //loop in reverse order 
        int previousSegmentEnd = genomeSize;
        int currentSegmentEnd = 0;
                
        for (int i = numSegments - 1; i >= 0; i--)
            {
            //check if the segment data exist
            if (state.parameters.exists(base.push(P_SEGMENT).push(""+i).push(P_SEGMENT_START), 
                                        def.push(P_SEGMENT).push(""+i).push(P_SEGMENT_START)))
                {
                //Read the index of the end gene specifying current segment
                currentSegmentEnd = state.parameters.getInt(base.push(P_SEGMENT).push(""+i).push(P_SEGMENT_START), 
                                                            def.push(P_SEGMENT).push(""+i).push(P_SEGMENT_START));
                                
                }
            else
                {
                state.output.fatal("Genome segment " + i + " has not been defined!" +
                                   "\nYou must specify start indices for " + numSegments + " segment(s)", 
                                   base.push(P_SEGMENT).push(""+i).push(P_SEGMENT_START),
                                   base.push(P_SEGMENT).push(""+i).push(P_SEGMENT_START));
                }
                        
            //check if the start index is valid
            if(currentSegmentEnd >= previousSegmentEnd || currentSegmentEnd < 0)
                state.output.fatal(
                    "Invalid start index value for segment " + i + ": " + currentSegmentEnd 
                    +  "\nThe value must be smaller than " + previousSegmentEnd +
                    " and greater than or equal to  " + 0);
                        
            //check if the index of the first segment is equal to 0
            if(i == 0 && currentSegmentEnd != 0)
                state.output.fatal(
                    "Invalid start index value for the first segment " + i + ": " + currentSegmentEnd 
                    +  "\nThe value must be equal to " + 0);
                        
                        
            //get min and max values of genes in this segment
            if (!state.parameters.exists(base.push(P_SEGMENT).push(""+i).push(P_MINGENE), 
                                         base.push(P_SEGMENT).push(""+i).push(P_MINGENE)))
                {
                if (!warnedMin)
                    {
                    state.output.warning(
                        "IntegerVectorSpecies has missing min-gene values for some segments.\n"
                        + "The first segment is #" + i + ".", 
                        base.push(P_SEGMENT).push(""+i), 
                        base.push(P_SEGMENT).push(""+i));
                    warnedMin = true;
                    }
                                
                //the min-gene value has not been defined for this segment so assume the global min value
                currentSegmentMinGeneValue = minGene;
                }
            else  //get the min value for this segment
                {
                currentSegmentMinGeneValue = state.parameters.getLongWithDefault(
                    base.push(P_SEGMENT).push(""+i).push(P_MINGENE), 
                    base.push(P_SEGMENT).push(""+i).push(P_MINGENE), 
                    minGene);
                                
                //check if the value is in range
                if (!inNumericalTypeRange(currentSegmentMinGeneValue))
                    state.output
                        .error(
                            "This IntegerVectorSpecies has a prototype of the kind: "
                            + i_prototype.getClass()
                            .getName()
                            + ", but doesn't have a min-gene "
                            + " value for segment " + i
                            + " within the range of this prototype's genome's data types",
                            base.push(P_SEGMENT).push(""+i).push(P_MINGENE), 
                            base.push(P_SEGMENT).push(""+i).push(P_MINGENE));
                                
                }
                        
            if (!state.parameters.exists(base.push(P_SEGMENT).push(""+i).push(P_MAXGENE), 
                                         base.push(P_SEGMENT).push(""+i).push(P_MAXGENE)))
                {
                if (!warnedMax)
                    {
                    state.output.warning(
                        "IntegerVectorSpecies has missing max-gene values for some segments.\n"
                        + "The first segment is #" + i + ".", 
                        base.push(P_SEGMENT).push(""+i), 
                        base.push(P_SEGMENT).push(""+i));
                    warnedMax = true;
                    }
                                
                //the max-gen value has not been defined for this segment so assume the global max value
                currentSegmentMaxGeneValue = maxGene;
                                
                }
            else   //get the max value for this segment
                {
                currentSegmentMaxGeneValue = state.parameters.getLongWithDefault(
                    base.push(P_SEGMENT).push(""+i).push(P_MAXGENE), 
                    base.push(P_SEGMENT).push(""+i).push(P_MAXGENE), 
                    maxGene);
                                
                //check if the value is in range
                if (!inNumericalTypeRange(currentSegmentMaxGeneValue))
                    state.output
                        .fatal(
                            "This IntegerVectorSpecies has a prototype of the kind: "
                            + i_prototype.getClass()
                            .getName()
                            + ", but doesn't have a max-gene "
                            + " value for segment " + i
                            + " within the range of this prototype's genome's data types",
                            base.push(P_SEGMENT).push(""+i).push(P_MAXGENE), 
                            base.push(P_SEGMENT).push(""+i).push(P_MAXGENE));
                }

            //check is min is smaller than or equal to max
            if (currentSegmentMaxGeneValue < currentSegmentMinGeneValue)
                state.output.fatal(
                    "IntegerVectorSpecies must have a min-gene value for segment "
                    + i + " which is <= the max-gene value", 
                    base.push(P_SEGMENT).push(""+i).push(P_MAXGENE), 
                    base.push(P_SEGMENT).push(""+i).push(P_MAXGENE));

                        
            //and assign min and max values for all genes in this segment
            for(int j = previousSegmentEnd-1; j >= currentSegmentEnd; j--)
                {
                minGenes[j] = currentSegmentMinGeneValue;
                maxGenes[j] = currentSegmentMaxGeneValue;
                }
                        
            previousSegmentEnd = currentSegmentEnd;
                        
            }
                
        }
        
    private void initializeGenomeSegmentsByEndIndices(final EvolutionState state, 
                                                      final Parameter base, 
                                                      final Parameter def,
                                                      int numSegments)
        {
        boolean warnedMin = false;
        boolean warnedMax = false;
        long currentSegmentMinGeneValue = Long.MAX_VALUE;
        long currentSegmentMaxGeneValue = Long.MIN_VALUE;
                
        int previousSegmentEnd = -1;  
        int currentSegmentEnd = 0;
        // iterate over segments and set genes values for each segment
        for (int i = 0; i < numSegments; i++)
            {
            //check if the segment data exist
            if (state.parameters.exists(base.push(P_SEGMENT).push(""+i).push(P_SEGMENT_END), def.push(P_SEGMENT).push(""+i).push(P_SEGMENT_END)))
                {
                //Read the index of the end gene specifying current segment
                currentSegmentEnd = state.parameters.getInt(base.push(P_SEGMENT).push(""+i).push(P_SEGMENT_END), 
                                                            def.push(P_SEGMENT).push(""+i).push(P_SEGMENT_END));
                                
                }
            else
                {
                state.output.fatal("Genome segment " + i + " has not been defined!" +
                                   "\nYou must specify end indices for " + numSegments + " segment(s)", 
                                   base.push(P_SEGMENT).push(""+i).push(P_SEGMENT_END),
                                   base.push(P_SEGMENT).push(""+i).push(P_SEGMENT_END));
                }
                        
            //check if the end index is valid
            if(currentSegmentEnd <= previousSegmentEnd || currentSegmentEnd >= genomeSize)
                state.output.fatal(
                    "Invalid end index value for segment " + i + ": " + currentSegmentEnd 
                    +  "\nThe value must be greater than " + previousSegmentEnd +
                    " and smaller than " + genomeSize);
                        
            //check if the index of the final segment is equal to the genomeSize
            if(i == numSegments - 1 && currentSegmentEnd != (genomeSize-1))
                state.output.fatal(
                    "Invalid end index value for the last segment " + i + ": " + currentSegmentEnd 
                    +  "\nThe value must be equal to the index of the last gene in the genome:  " + (genomeSize-1));
                        
                        
            //get min and max values of genes in this segment
            if (!state.parameters.exists(base.push(P_SEGMENT).push(""+i).push(P_MINGENE), 
                                         base.push(P_SEGMENT).push(""+i).push(P_MINGENE)))
                {
                if (!warnedMin)
                    {
                    state.output.warning(
                        "IntegerVectorSpecies has missing min-gene values for some segments.\n"
                        + "The first segment is #" + i + ".", 
                        base.push(P_SEGMENT).push(""+i), 
                        base.push(P_SEGMENT).push(""+i));
                    warnedMin = true;
                    }
                                
                //the min-gene value has not been defined for this segment so assume the global min value
                currentSegmentMinGeneValue = minGene;
                }
            else  //get the min value for this segment
                {
                currentSegmentMinGeneValue = state.parameters.getLongWithDefault(
                    base.push(P_SEGMENT).push(""+i).push(P_MINGENE), 
                    base.push(P_SEGMENT).push(""+i).push(P_MINGENE), 
                    minGene);
                                
                //check if the value is in range
                if (!inNumericalTypeRange(currentSegmentMinGeneValue))
                    state.output
                        .error(
                            "This IntegerVectorSpecies has a prototype of the kind: "
                            + i_prototype.getClass()
                            .getName()
                            + ", but doesn't have a min-gene "
                            + " value for segment " + i
                            + " within the range of this prototype's genome's data types",
                            base.push(P_SEGMENT).push(""+i).push(P_MINGENE), 
                            base.push(P_SEGMENT).push(""+i).push(P_MINGENE));
                                
                }
                        
            if (!state.parameters.exists(base.push(P_SEGMENT).push(""+i).push(P_MAXGENE), 
                                         base.push(P_SEGMENT).push(""+i).push(P_MAXGENE)))
                {
                if (!warnedMax)
                    {
                    state.output.warning(
                        "IntegerVectorSpecies has missing max-gene values for some segments.\n"
                        + "The first segment is #" + i + ".", 
                        base.push(P_SEGMENT).push(""+i), 
                        base.push(P_SEGMENT).push(""+i));
                    warnedMax = true;
                    }
                                
                //the max-gen value has not been defined for this segment so assume the global max value
                currentSegmentMaxGeneValue = maxGene;
                                
                }
            else   //get the max value for this segment
                {
                currentSegmentMaxGeneValue = state.parameters.getLongWithDefault(
                    base.push(P_SEGMENT).push(""+i).push(P_MAXGENE), 
                    base.push(P_SEGMENT).push(""+i).push(P_MAXGENE), 
                    maxGene);
                                
                //check if the value is in range
                if (!inNumericalTypeRange(currentSegmentMaxGeneValue))
                    state.output
                        .fatal(
                            "This IntegerVectorSpecies has a prototype of the kind: "
                            + i_prototype.getClass()
                            .getName()
                            + ", but doesn't have a max-gene "
                            + " value for segment " + i
                            + " within the range of this prototype's genome's data types",
                            base.push(P_SEGMENT).push(""+i).push(P_MAXGENE), 
                            base.push(P_SEGMENT).push(""+i).push(P_MAXGENE));
                }

            //check is min is smaller than or equal to max
            if (currentSegmentMaxGeneValue < currentSegmentMinGeneValue)
                state.output.fatal(
                    "IntegerVectorSpecies must have a min-gene value for segment "
                    + i + " which is <= the max-gene value", 
                    base.push(P_SEGMENT).push(""+i).push(P_MAXGENE), 
                    base.push(P_SEGMENT).push(""+i).push(P_MAXGENE));

                        
            //and assign min and max values for all genes in this segment
            for(int j = previousSegmentEnd+1; j <= currentSegmentEnd; j++)
                {
                minGenes[j] = currentSegmentMinGeneValue;
                maxGenes[j] = currentSegmentMaxGeneValue;
                }
                        
            previousSegmentEnd = currentSegmentEnd;
                        
            }
        }
    
    }

