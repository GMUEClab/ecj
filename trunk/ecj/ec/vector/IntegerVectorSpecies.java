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
 * IntegerVectorSpecies is a subclass of VectorSpecies with special
 * constraints for integral vectors, namely ByteVectorIndividual, ShortVectorIndividual,
 * IntegerVectorIndividual, and LongVectorIndividual.
 *
 * <p>IntegerVectorSpecies can specify numeric constraints on gene values
 * in one of two ways.  First, they can simply specify a default min and max value.  Or
 * they can specify an array of min/max pairs, one pair per gene.  IntegerVectorSpecies
 * will check to see if the second approach is to be used by looking for parameter 
 * <i>base.n</i>.<tt>max-gene</tt>.0 in the
 * array -- if it exists, IntegerVectorSpecies will assume all such parameters
 * exist, and will load up to the genome length.  If a parameter is missing, in this
 * range, a warning will be issued during Individual setup.  If the array is shorter 
 * than the genome, then the default min/max
 * values will be used for the remaining genome values.  This means that even if you 
 * specify the array, you need to still specify the default min/max values just in case.
 *
 * @author Sean Luke
 * @version 1.0 

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base.n</i>.<tt>min-gene</tt><br>
 <font size=-1>long (default=0)</font></td>
 <td valign=top>(the minimum gene value)</td></tr>

 <tr><td valign=top><i>base.n</i>.<tt>max-gene</tt><br>
 <font size=-1>long &gt;= <i>base.n</i>.min-gene</font></td>
 <td valign=top>(the maximum gene value)</td></tr>

 <tr><td valign=top><i>base.n</i>.<tt>min-gene</tt>.<i>i</i><br>
 <font size=-1>long (default=<i>base.n</i>.<tt>min-gene</tt>)</font></td>
 <td valign=top>(the minimum gene value for gene <i>i</i>)</td></tr>

 <tr><td valign=top><i>base.n</i>.<tt>max-gene</tt>.<i>i</i><br>
 <font size=-1>long &gt;= <i>base.n</i>.min-gene.<i>i</i> (default=<i>base.n</i>.<tt>max-gene</tt>)</font></td>
 <td valign=top>(the maximum gene value for gene <i>i</i>)</td></tr>
 </table>

*/
public class IntegerVectorSpecies extends VectorSpecies
    {
    public final static String P_MINGENE = "min-gene";
    public final static String P_MAXGENE = "max-gene";
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
    
    public boolean inRange(long geneVal)
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
        if (!inRange(minGene))
            state.output.fatal("This IntegerVectorSpecies has a prototype of the kind: " 
                               + i_prototype.getClass().getName() +
                               ", but doesn't have a min-gene value within the range of this prototype's genome's data types",
                               base.push(P_MINGENE),def.push(P_MINGENE));
        if (!inRange(maxGene))
            state.output.fatal("This IntegerVectorSpecies has a prototype of the kind: " 
                               + i_prototype.getClass().getName() +
                               ", but doesn't have a max-gene value within the range of this prototype's genome's data types",
                               base.push(P_MAXGENE),def.push(P_MAXGENE));

        // Next check to see if the gene-by-gene min/max values exist
        if (state.parameters.exists(base.push(P_MAXGENE).push("0"),def.push(P_MAXGENE).push("0")))
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
                if (!inRange(minGenes[x]))
                    state.output.error("This IntegerVectorSpecies has a prototype of the kind: " 
                                       + i_prototype.getClass().getName() +
                                       ", but doesn't have a min-gene["+x+"] value within the range of this prototype's genome's data types",
                                       base.push(P_MINGENE).push(""+x),base.push(P_MINGENE).push(""+x));
                if (!inRange(maxGenes[x]))
                    state.output.fatal("This IntegerVectorSpecies has a prototype of the kind: " 
                                       + i_prototype.getClass().getName() +
                                       ", but doesn't have a max-gene["+x+"] value within the range of this prototype's genome's data types",
                                       base.push(P_MAXGENE).push(""+x),base.push(P_MAXGENE).push(""+x));
                }
            }
        }
    }

