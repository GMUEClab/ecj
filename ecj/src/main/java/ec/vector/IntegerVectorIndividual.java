/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.vector;

import ec.*;
import ec.util.*;
import java.io.*;

/*
 * IntegerVectorIndividual.java
 * Created: Tue Mar 13 15:03:12 EST 2001
 */

/**
 * IntegerVectorIndividual is a VectorIndividual whose genome is an array of ints.
 * Gene values may range from species.mingene(x) to species.maxgene(x), inclusive.
 * The default mutation method randomizes genes to new values in this range,
 * with <tt>species.mutationProbability</tt>.
 *

 * <P><b>From ec.Individual:</b>  
 * <p>In addition to serialization for checkpointing, Individuals may read and write themselves to streams in three ways.
 *
 * <ul>
 * <li><b>writeIndividual(...,DataOutput)/readIndividual(...,DataInput)</b>&nbsp;&nbsp;&nbsp;This method
 * transmits or receives an individual in binary.  It is the most efficient approach to sending
 * individuals over networks, etc.  These methods write the evaluated flag and the fitness, then
 * call <b>readGenotype/writeGenotype</b>, which you must implement to write those parts of your 
 * Individual special to your functions-- the default versions of readGenotype/writeGenotype throw errors.
 * You don't need to implement them if you don't plan on using read/writeIndividual.
 *
 * <li><b>printIndividual(...,PrintWriter)/readIndividual(...,LineNumberReader)</b>&nbsp;&nbsp;&nbsp;This
 * approach transmits or receives an indivdual in text encoded such that the individual is largely readable
 * by humans but can be read back in 100% by ECJ as well.  To do this, these methods will encode numbers
 * using the <tt>ec.util.Code</tt> class.  These methods are mostly used to write out populations to
 * files for inspection, slight modification, then reading back in later on.  <b>readIndividual</b> reads
 * in the fitness and the evaluation flag, then calls <b>parseGenotype</b> to read in the remaining individual.
 * You are responsible for implementing parseGenotype: the Code class is there to help you.
 * <b>printIndividual</b> writes out the fitness and evaluation flag, then calls <b>genotypeToString</b> 
 * and printlns the resultant string. You are responsible for implementing the genotypeToString method in such
 * a way that parseGenotype can read back in the individual println'd with genotypeToString.  The default form
 * of genotypeToString simply calls <b>toString</b>, which you may override instead if you like.  The default
 * form of <b>parseGenotype</b> throws an error.  You are not required to implement these methods, but without
 * them you will not be able to write individuals to files in a simultaneously computer- and human-readable fashion.
 *
 * <li><b>printIndividualForHumans(...,PrintWriter)</b>&nbsp;&nbsp;&nbsp;This
 * approach prints an individual in a fashion intended for human consumption only.
 * <b>printIndividualForHumans</b> writes out the fitness and evaluation flag, then calls <b>genotypeToStringForHumans</b> 
 * and printlns the resultant string. You are responsible for implementing the genotypeToStringForHumans method.
 * The default form of genotypeToStringForHumans simply calls <b>toString</b>, which you may override instead if you like
 * (though note that genotypeToString's default also calls toString).  You should handle one of these methods properly
 * to ensure individuals can be printed by ECJ.
 * </ul>

 * <p>In general, the various readers and writers do three things: they tell the Fitness to read/write itself,
 * they read/write the evaluated flag, and they read/write the gene array.  If you add instance variables to
 * a VectorIndividual or subclass, you'll need to read/write those variables as well.
 <p><b>Default Base</b><br>
 vector.int-vect-ind

 * @author Sean Luke
 * @version 1.0
 */

public class IntegerVectorIndividual extends VectorIndividual
    {
    public static final String P_INTEGERVECTORINDIVIDUAL = "int-vect-ind";
    public int[] genome;
    
    public Parameter defaultBase()
        {
        return VectorDefaults.base().push(P_INTEGERVECTORINDIVIDUAL);
        }

    public Object clone()
        {
        IntegerVectorIndividual myobj = (IntegerVectorIndividual) (super.clone());

        // must clone the genome
        myobj.genome = (int[])(genome.clone());
        
        return myobj;
        } 

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);  // actually unnecessary (Individual.setup() is empty)

        Parameter def = defaultBase();
        
        if (!(species instanceof IntegerVectorSpecies)) 
            state.output.fatal("IntegerVectorIndividual requires an IntegerVectorSpecies", base, def);
        IntegerVectorSpecies s = (IntegerVectorSpecies) species;
        
        genome = new int[s.genomeSize];
        }
                
    public void defaultCrossover(EvolutionState state, int thread, VectorIndividual ind)
        {
        IntegerVectorSpecies s = (IntegerVectorSpecies) species;
        IntegerVectorIndividual i = (IntegerVectorIndividual) ind;
        int tmp;
        int point;

        int len = Math.min(genome.length, i.genome.length);
        if (len != genome.length || len != i.genome.length)
            state.output.warnOnce("Genome lengths are not the same.  Vector crossover will only be done in overlapping region.");

        switch(s.crossoverType)
            {
            case VectorSpecies.C_ONE_POINT:
                //                point = state.random[thread].nextInt((len / s.chunksize)+1);
                // we want to go from 0 ... len-1 
                // so that there is only ONE case of NO-OP crossover, not TWO
                point = state.random[thread].nextInt((len / s.chunksize));
                for(int x=0;x<point*s.chunksize;x++)
                    { 
                    tmp = i.genome[x];
                    i.genome[x] = genome[x]; 
                    genome[x] = tmp; 
                    }
                break;
            case VectorSpecies.C_ONE_POINT_NO_NOP:
                point = state.random[thread].nextInt((len / s.chunksize) - 1) + 1;  // so it goes from 1 .. len-1
                for(int x=0;x<point*s.chunksize;x++)
                    { 
                    tmp = i.genome[x];
                    i.genome[x] = genome[x]; 
                    genome[x] = tmp; 
                    }
                break;
            case VectorSpecies.C_TWO_POINT: 
                {
                //                int point0 = state.random[thread].nextInt((len / s.chunksize)+1);
                //                point = state.random[thread].nextInt((len / s.chunksize)+1);
                // we want to go from 0 to len-1
                // so that the only NO-OP crossover possible is point == point0
                // example; len = 4
                // possibilities: a=0 b=0       NOP                             [0123]
                //                                a=0 b=1       swap 0                  [for 1, 2, 3]
                //                                a=0 b=2       swap 0, 1               [for 2, 3]
                //                                a=0 b=3       swap 0, 1, 2    [for 3]
                //                                a=1 b=1       NOP                             [1230]
                //                                a=1 b=2       swap 1                  [for 2, 3, 0]
                //                                a=1 b=3       swap 1, 2               [for 3, 0]
                //                                a=2 b=2       NOP                             [2301]
                //                                a=2 b=3       swap 2                  [for 3, 0, 1]
                //                                a=3 b=3   NOP                         [3012]
                // All intervals: 0, 01, 012, 0123, 1, 12, 123, 1230, 2, 23, 230, 2301, 3, 30, 301, 3012
                point = state.random[thread].nextInt((len / s.chunksize));
                int point0 = state.random[thread].nextInt((len / s.chunksize));
                if (point0 > point) { int p = point0; point0 = point; point = p; }
                for(int x=point0*s.chunksize;x<point*s.chunksize;x++)
                    {
                    tmp = i.genome[x];
                    i.genome[x] = genome[x];
                    genome[x] = tmp;
                    }
                }
            break;
            case VectorSpecies.C_TWO_POINT_NO_NOP: 
                {
                point = state.random[thread].nextInt((len / s.chunksize));
                int point0 = 0;
                do { point0 = state.random[thread].nextInt((len / s.chunksize)); }
                while (point0 == point);  // NOP
                if (point0 > point) { int p = point0; point0 = point; point = p; }
                for(int x=point0*s.chunksize;x<point*s.chunksize;x++)
                    {
                    tmp = i.genome[x];
                    i.genome[x] = genome[x];
                    genome[x] = tmp;
                    }
                }
            break;
            case VectorSpecies.C_ANY_POINT:
                for(int x=0;x<len/s.chunksize;x++) 
                    if (state.random[thread].nextBoolean(s.crossoverProbability))
                        for(int y=x*s.chunksize;y<(x+1)*s.chunksize;y++)
                            {
                            tmp = i.genome[y];
                            i.genome[y] = genome[y];
                            genome[y] = tmp;
                            }
                break;
            case VectorSpecies.C_LINE_RECOMB:
                {
                double alpha = state.random[thread].nextDouble() * (1 + 2*s.lineDistance) - s.lineDistance;
                double beta = state.random[thread].nextDouble() * (1 + 2*s.lineDistance) - s.lineDistance;
                long t,u;
                long min, max;
                for (int x = 0; x < len; x++)
                    {
                    min = s.minGene(x);
                    max = s.maxGene(x);
                    t = (long) Math.floor(alpha * genome[x] + (1 - alpha) * i.genome[x] + 0.5);
                    u = (long) Math.floor(beta * i.genome[x] + (1 - beta) * genome[x] + 0.5);
                    if (!(t < min || t > max || u < min || u > max))
                        {
                        genome[x] = (int) t;
                        i.genome[x] = (int) u; 
                        }
                    }
                }
            break;
            case VectorSpecies.C_INTERMED_RECOMB:
                {
                long t,u;
                long min, max;
                for (int x = 0; x < len; x++)
                    {
                    do
                        {
                        double alpha = state.random[thread].nextDouble() * (1 + 2*s.lineDistance) - s.lineDistance;
                        double beta = state.random[thread].nextDouble() * (1 + 2*s.lineDistance) - s.lineDistance;
                        min = s.minGene(x);
                        max = s.maxGene(x);
                        t = (long) Math.floor(alpha * genome[x] + (1 - alpha) * i.genome[x] + 0.5);
                        u = (long) Math.floor(beta * i.genome[x] + (1 - beta) * genome[x] + 0.5);
                        } while (t < min || t > max || u < min || u > max);
                    genome[x] = (int) t;
                    i.genome[x] = (int) u; 
                    }
                }
            break;
            }
        }

    /** Splits the genome into n pieces, according to points, which *must* be sorted. 
        pieces.length must be 1 + points.length */
    public void split(int[] points, Object[] pieces)
        {
        int point0, point1;
        point0 = 0; point1 = points[0];
        for(int x=0;x<pieces.length;x++)
            {
            pieces[x] = new int[point1-point0];
            System.arraycopy(genome,point0,pieces[x],0,point1-point0);
            point0 = point1;
            if (x >=pieces.length-2)
                point1 = genome.length;
            else point1 = points[x+1];
            }
        }
    
    /** Joins the n pieces and sets the genome to their concatenation.*/
    public void join(Object[] pieces)
        {
        int sum=0;
        for(int x=0;x<pieces.length;x++)
            sum += ((int[])(pieces[x])).length;
        
        int runningsum = 0;
        int[] newgenome = new int[sum];
        for(int x=0;x<pieces.length;x++)
            {
            System.arraycopy(pieces[x], 0, newgenome, runningsum, ((int[])(pieces[x])).length);
            runningsum += ((int[])(pieces[x])).length;
            }
        // set genome
        genome = newgenome;
        }


    /** Returns a random value from between min and max inclusive.  This method handles
        overflows that complicate this computation.  Does NOT check that
        min is less than or equal to max.  You must check this yourself. */
    public int randomValueFromClosedInterval(int min, int max, MersenneTwisterFast random)
        {
        if (max - min < 0) // we had an overflow
            {
            int l = 0;
            do l = random.nextInt();
            while(l < min || l > max);
            return l;
            }
        else return min + random.nextInt(max - min + 1);
        }


    /** Destructively mutates the individual in some default manner.  The default form
        simply randomizes genes to a uniform distribution from the min and max of the gene values. */
    public void defaultMutate(EvolutionState state, int thread)
        {
        IntegerVectorSpecies s = (IntegerVectorSpecies) species;
        for(int x = 0; x < genome.length; x++)
            if (state.random[thread].nextBoolean(s.mutationProbability(x)))
                {
                int old = genome[x];
                for(int retries = 0; retries < s.duplicateRetries(x) + 1; retries++)
                    {
                    switch(s.mutationType(x))
                        {
                        case IntegerVectorSpecies.C_RESET_MUTATION:
                            genome[x] = randomValueFromClosedInterval((int)s.minGene(x), (int)s.maxGene(x), state.random[thread]);
                            break;
                        case IntegerVectorSpecies.C_RANDOM_WALK_MUTATION:
                            int min = (int)s.minGene(x);
                            int max = (int)s.maxGene(x);
                            if (!s.mutationIsBounded(x))
                                {
                                // okay, technically these are still bounds, but we can't go beyond this without weird things happening
                                max = Integer.MAX_VALUE;
                                min = Integer.MIN_VALUE;
                                }
                            do
                                {
                                int n = (int)(state.random[thread].nextBoolean() ? 1 : -1);
                                int g = genome[x];
                                if ((n == 1 && g < max) ||
                                    (n == -1 && g > min))
                                    genome[x] = g + n;
                                else if ((n == -1 && g < max) ||
                                    (n == 1 && g > min))
                                    genome[x] = g - n;     
                                }
                            while (state.random[thread].nextBoolean(s.randomWalkProbability(x)));
                            break;
                        default:
                            state.output.fatal("In IntegerVectorIndividual.defaultMutate, default case occurred when it shouldn't have");
                            break;
                        }
                    if (genome[x] != old) break;
                    // else genome[x] = old;  // try again
                    }
                }
        }
        
    
    /** Initializes the individual by randomly choosing Integers uniformly from mingene to maxgene. */
    // notice that we bump to longs to avoid overflow errors
    public void reset(EvolutionState state, int thread)
        {
        IntegerVectorSpecies s = (IntegerVectorSpecies) species;
        for(int x=0;x<genome.length;x++)
            genome[x] = randomValueFromClosedInterval((int)s.minGene(x), (int)s.maxGene(x), state.random[thread]);
        }

    public int hashCode()
        {
        // stolen from GPIndividual.  It's a decent algorithm.
        int hash = this.getClass().hashCode();

        hash = ( hash << 1 | hash >>> 31 );
        for(int x=0;x<genome.length;x++)
            hash = ( hash << 1 | hash >>> 31 ) ^ genome[x];

        return hash;
        }

    public String genotypeToStringForHumans()
        {
        StringBuilder s = new StringBuilder();
        for( int i = 0 ; i < genome.length ; i++ )
            { if (i > 0) s.append(" "); s.append(genome[i]); }
        return s.toString();
        }
        
    public String genotypeToString()
        {
        StringBuilder s = new StringBuilder();
        s.append( Code.encode( genome.length ) );
        for( int i = 0 ; i < genome.length ; i++ )
            s.append( Code.encode( genome[i] ) );
        return s.toString();
        }
                
    protected void parseGenotype(final EvolutionState state,
        final LineNumberReader reader) throws IOException
        {
        // read in the next line.  The first item is the number of genes
        String s = reader.readLine();
        DecodeReturn d = new DecodeReturn(s);
        Code.decode( d );
        
        // of course, even if it *is* an integer, we can't tell if it's a gene or a genome count, argh...
        if (d.type != DecodeReturn.T_INTEGER)  // uh oh
            state.output.fatal("Individual with genome:\n" + s + "\n... does not have an integer at the beginning indicating the genome count.");
        int lll = (int)(d.l);

        genome = new int[ lll ];

        // read in the genes
        for( int i = 0 ; i < genome.length ; i++ )
            {
            Code.decode( d );
            genome[i] = (int)(d.l);
            }
        }

    public boolean equals(Object ind)
        {
        if (ind == null) return false;
        if (!(this.getClass().equals(ind.getClass()))) return false; // SimpleRuleIndividuals are special.
        IntegerVectorIndividual i = (IntegerVectorIndividual)ind;
        if( genome.length != i.genome.length )
            return false;
        for( int j = 0 ; j < genome.length ; j++ )
            if( genome[j] != i.genome[j] )
                return false;
        return true;
        }

    public Object getGenome()
        { return genome; }
    public void setGenome(Object gen)
        { genome = (int[]) gen; }
    public int genomeLength()
        { return genome.length; }
        
    public void writeGenotype(final EvolutionState state,
        final DataOutput dataOutput) throws IOException
        {
        dataOutput.writeInt(genome.length);
        for(int x=0;x<genome.length;x++)
            dataOutput.writeInt(genome[x]);
        }

    public void readGenotype(final EvolutionState state,
        final DataInput dataInput) throws IOException
        {
        int len = dataInput.readInt();
        if (genome==null || genome.length != len)
            genome = new int[len];
        for(int x=0;x<genome.length;x++)
            genome[x] = dataInput.readInt();
        }

    /** Clips each gene value to be within its specified [min,max] range. */
    public void clamp() 
        {
        IntegerVectorSpecies _species = (IntegerVectorSpecies)species;
        for (int i = 0; i < genomeLength(); i++)
            {
            int minGene = (int)_species.minGene(i);
            if (genome[i] < minGene)
                genome[i] = minGene;
            else 
                {
                int maxGene = (int)_species.maxGene(i);
                if (genome[i] > maxGene)
                    genome[i] = maxGene;
                }
            }
        }
                
    public void setGenomeLength(int len)
        {
        int[] newGenome = new int[len];
        System.arraycopy(genome, 0, newGenome, 0, 
            genome.length < newGenome.length ? genome.length : newGenome.length);
        genome = newGenome;
        }

    /** Returns true if each gene value is within is specified [min,max] range. */
    public boolean isInRange() 
        {
        IntegerVectorSpecies _species = (IntegerVectorSpecies)species;
        for (int i = 0; i < genomeLength(); i++)
            if (genome[i] < _species.minGene(i) ||
                genome[i] > _species.maxGene(i)) return false;
        return true;
        }

    public double distanceTo(Individual otherInd)
        {               
        if (!(otherInd instanceof IntegerVectorIndividual)) 
            return super.distanceTo(otherInd);  // will return infinity!
                
        IntegerVectorIndividual other = (IntegerVectorIndividual) otherInd;
        int[] otherGenome = other.genome;
        double sumSquaredDistance =0.0;
        for(int i=0; i < other.genomeLength(); i++)
            {
            long dist = this.genome[i] - (long)otherGenome[i];
            sumSquaredDistance += dist*dist;
            }
        return StrictMath.sqrt(sumSquaredDistance);
        }
    }
