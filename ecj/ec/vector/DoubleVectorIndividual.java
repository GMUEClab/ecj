package ec.vector;

import ec.*;
import ec.util.*;

import java.io.*;

/*
 * DoubleVectorIndividual.java
 * Created: Thu Mar 22 13:13:20 EST 2001
 */

/**
 * DoubleVectorIndividual is a VectorIndividual whose genome is an array of
 * doubles. Gene values may range from species.mingene(x) to species.maxgene(x),
 * inclusive. The default mutation method randomizes genes to new values in this
 * range, with <tt>species.mutationProbability</tt>. It can also add gaussian
 * noise to the genes, if so directed in the FloatVectorSpecies. If the gaussian
 * noise pushes the gene out of range, a new noise value is generated.
 * 
 * <p>
 * <P><b>From ec.Individual:</b> 
 *
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
 * files for inspection, slight modification, then reading back in later on.  <b>readIndividual</b>reads
 * in the fitness and the evaluation flag, then calls <b>parseGenotype</b> to read in the remaining individual.
 * You are responsible for implementing parseGenotype: the Code class is there to help you.
 * <b>printIndividual</b> writes out the fitness and evaluation flag, then calls <b>genotypeToString<b> 
 * and printlns the resultant string. You are responsible for implementing the genotypeToString method in such
 * a way that parseGenotype can read back in the individual println'd with genotypeToString.  The default form
 * of genotypeToString simply calls <b>toString</b>, which you may override instead if you like.  The default
 * form of <b>parseGenotype</b> throws an error.  You are not required to implement these methods, but without
 * them you will not be able to write individuals to files in a simultaneously computer- and human-readable fashion.
 *
 * <li><b>printIndividualForHumans(...,PrintWriter)</b>&nbsp;&nbsp;&nbsp;This
 * approach prints an individual in a fashion intended for human consumption only.
 * <b>printIndividualForHumans</b> writes out the fitness and evaluation flag, then calls <b>genotypeToStringForHumans<b> 
 * and printlns the resultant string. You are responsible for implementing the genotypeToStringForHumans method.
 * The default form of genotypeToStringForHumans simply calls <b>toString</b>, which you may override instead if you like
 * (though note that genotypeToString's default also calls toString).  You should handle one of these methods properly
 * to ensure individuals can be printed by ECJ.
 * </ul>

 * <p>In general, the various readers and writers do three things: they tell the Fitness to read/write itself,
 * they read/write the evaluated flag, and they read/write the gene array.  If you add instance variables to
 * a VectorIndividual or subclass, you'll need to read/write those variables as well.
 * <b>Default Base</b><br>
 * vector.double-vect-ind
 * 
 * @author Liviu Panait
 * @author Sean Luke and Liviu Panait
 * @version 2.0
 */

public class DoubleVectorIndividual extends VectorIndividual
    {
    public static final String P_DOUBLEVECTORINDIVIDUAL = "double-vect-ind";

    public double[] genome;

    public Parameter defaultBase()
        {
        return VectorDefaults.base().push(P_DOUBLEVECTORINDIVIDUAL);
        }

    public Object clone()
        {
        DoubleVectorIndividual myobj = (DoubleVectorIndividual) (super
                                                                 .clone());

        // must clone the genome
        myobj.genome = (double[]) (genome.clone());

        return myobj;
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state, base); // actually unnecessary (Individual.setup()
        // is empty)

        // since VectorSpecies set its constraint values BEFORE it called
        // super.setup(...) [which in turn called our setup(...)], we know that
        // stuff like genomeSize has already been set...

        Parameter def = defaultBase();

        if (!(species instanceof FloatVectorSpecies))
            state.output.fatal(
                "DoubleVectorIndividual requires a FloatVectorSpecies",
                base, def);
        FloatVectorSpecies s = (FloatVectorSpecies) species;

        genome = new double[s.genomeSize];
        }

    public void defaultCrossover(EvolutionState state, int thread,
                                 VectorIndividual ind)
        {
        FloatVectorSpecies s = (FloatVectorSpecies) species;
        DoubleVectorIndividual i = (DoubleVectorIndividual) ind;
        double tmp;
        int point;

        if (genome.length != i.genome.length)
            state.output
                .fatal("Genome lengths are not the same for fixed-length vector crossover");
        switch (s.crossoverType)
            {
            case VectorSpecies.C_ONE_POINT:
                point = state.random[thread]
                    .nextInt((genome.length / s.chunksize) + 1);
                for (int x = 0; x < point * s.chunksize; x++)
                    {
                    tmp = i.genome[x];
                    i.genome[x] = genome[x];
                    genome[x] = tmp;
                    }
                break;
            case VectorSpecies.C_TWO_POINT:
                int point0 = state.random[thread]
                    .nextInt((genome.length / s.chunksize) + 1);
                point = state.random[thread]
                    .nextInt((genome.length / s.chunksize) + 1);
                if (point0 > point)
                    {
                    int p = point0;
                    point0 = point;
                    point = p;
                    }
                for (int x = point0 * s.chunksize; x < point * s.chunksize; x++)
                    {
                    tmp = i.genome[x];
                    i.genome[x] = genome[x];
                    genome[x] = tmp;
                    }
                break;
            case VectorSpecies.C_ANY_POINT:
                for (int x = 0; x < genome.length / s.chunksize; x++)
                    if (state.random[thread].nextBoolean(s.crossoverProbability))
                        for (int y = x * s.chunksize; y < (x + 1) * s.chunksize; y++)
                            {
                            tmp = i.genome[y];
                            i.genome[y] = genome[y];
                            genome[y] = tmp;
                            }
                break;
            }
        }

    /**
     * Splits the genome into n pieces, according to points, which *must* be
     * sorted. pieces.length must be 1 + points.length
     */
    public void split(int[] points, Object[] pieces)
        {
        int point0, point1;
        point0 = 0;
        point1 = points[0];
        for (int x = 0; x < pieces.length; x++)
            {
            pieces[x] = new double[point1 - point0];
            System.arraycopy(genome, point0, pieces[x], 0, point1 - point0);
            point0 = point1;
            if (x == pieces.length - 2)
                point1 = genome.length;
            else
                point1 = points[x + 1];
            }
        }

    /** Joins the n pieces and sets the genome to their concatenation. */
    public void join(Object[] pieces)
        {
        int sum = 0;
        for (int x = 0; x < pieces.length; x++)
            sum += ((double[]) (pieces[x])).length;

        int runningsum = 0;
        double[] newgenome = new double[sum];
        for (int x = 0; x < pieces.length; x++)
            {
            System.arraycopy(pieces[x], 0, newgenome, runningsum,
                             ((double[]) (pieces[x])).length);
            runningsum += ((double[]) (pieces[x])).length;
            }
        // set genome
        genome = newgenome;
        }

    /**
     * Destructively mutates the individual in some default manner. The default
     * form simply randomizes genes to a uniform distribution from the min and
     * max of the gene values. It can also add gaussian noise to the genes, if
     * so directed in the FloatVectorSpecies. If the gaussian noise pushes the
     * gene out of range, a new noise value is generated.
     * 
     * @author Sean Luke, Liviu Panait and Gabriel Balan
     */
    public void defaultMutate(EvolutionState state, int thread)
        {
        FloatVectorSpecies s = (FloatVectorSpecies) species;
        if (!(s.mutationProbability > 0.0))
            return;
        MersenneTwisterFast rng = state.random[thread];

        if (s.individualGeneMinMaxUsed())
            {
            if (s.mutationType == FloatVectorSpecies.C_GAUSS_MUTATION)
                {
                for (int x = 0; x < genome.length; x++)
                    if (rng.nextBoolean(s.mutationProbability))
                        {
                        double val;
                        double min = s.minGene(x);
                        double max = s.maxGene(x);
                        double stdev = s.gaussMutationStdev(x);
                        int outOfBoundsLeftOverTries = s.outOfRangeRetries;
                        boolean givingUpAllowed = s.outOfRangeRetries != 0;
                        do
                            {
                            val = rng.nextGaussian() * stdev + genome[x];
                            outOfBoundsLeftOverTries--;
                            if (val > max || val < min)
                                {
                                if (givingUpAllowed && (outOfBoundsLeftOverTries == 0))
                                    {
                                    val = min + rng.nextFloat() * (max - min);
                                    s.outOfRangeRetryLimitReached(state);// it better get inlined
                                    break;
                                    }
                                } else
                                    break;
                            } while (true);
                        genome[x] = val;
                        }
                } else
                    {// C_RESET_MUTATION
                    for (int x = 0; x < genome.length; x++)
                        if (rng.nextBoolean(s.mutationProbability))
                            genome[x] = s.minGene(x) + rng.nextDouble() * (s.maxGene(x) - s.minGene(x));
                    }
            } else
                // quite a bit faster
                {
                double minGene = s.minGene;
                double maxGene = s.maxGene;
                double stdev = s.gaussMutationStdev;
                if (s.mutationType == FloatVectorSpecies.C_GAUSS_MUTATION)
                    {
                    for (int x = 0; x < genome.length; x++)
                        if (rng.nextBoolean(s.mutationProbability))
                            {
                            double val;
                            int outOfBoundsLeftOverTries = s.outOfRangeRetries;
                            boolean givingUpAllowed = s.outOfRangeRetries != 0;
                            do
                                {
                                val = rng.nextGaussian() * stdev + genome[x];
                                outOfBoundsLeftOverTries--;
                                if (val > maxGene || val < minGene)
                                    {
                                    if (givingUpAllowed && (outOfBoundsLeftOverTries == 0))
                                        {
                                        val = minGene + rng.nextFloat() * (maxGene - minGene);
                                        s.outOfRangeRetryLimitReached(state);// it better get inlined
                                        break;
                                        }
                                    } else
                                        break;
                                } while (true);
                            genome[x] = val;
                            }
                    } else
                        {// C_RESET_MUTATION
                        for (int x = 0; x < genome.length; x++)
                            if (rng.nextBoolean(s.mutationProbability))
                                genome[x] = minGene + rng.nextDouble()
                                    * (maxGene - minGene);
                        }
                }
        }

    /**
     * Initializes the individual by randomly choosing doubles uniformly from
     * mingene to maxgene.
     */
    public void reset(EvolutionState state, int thread)
        {
        FloatVectorSpecies s = (FloatVectorSpecies) species;
        if (s.individualGeneMinMaxUsed())
            for (int x = 0; x < genome.length; x++)
                genome[x] = (s.minGene(x) + state.random[thread].nextDouble()
                             * (s.maxGene(x) - s.minGene(x)));
        else
            // quite a bit faster
            for (int x = 0; x < genome.length; x++)
                genome[x] = (s.minGene + state.random[thread].nextDouble()
                             * (s.maxGene - s.minGene));
        }

    public int hashCode()
        {
        // stolen from GPIndividual. It's a decent algorithm.
        int hash = this.getClass().hashCode();

        hash = (hash << 1 | hash >>> 31);
        for (int x = 0; x < genome.length; x++)
            {
            long l = Double.doubleToLongBits(genome[x]);
            hash = (hash << 1 | hash >>> 31) ^ (int) ((l >>> 16) & 0xFFFFFFF)
                ^ (int) (l & 0xFFFF);
            }

        return hash;
        }

    public String genotypeToStringForHumans()
        {
        String s = "";
        for (int i = 0; i < genome.length; i++)
            s = s + " " + genome[i];
        return s;
        }

    public String genotypeToString()
        {
        StringBuffer s = new StringBuffer();
        s.append(Code.encode(genome.length));
        for (int i = 0; i < genome.length; i++)
            s.append(Code.encode(genome[i]));
        return s.toString();
        }

    protected void parseGenotype(final EvolutionState state,
                                 final LineNumberReader reader) throws IOException
        {
        // read in the next line. The first item is the number of genes
        String s = reader.readLine();
        DecodeReturn d = new DecodeReturn(s);
        Code.decode(d);
        int lll = (int) (d.l);

        genome = new double[lll];

        // read in the genes
        for (int i = 0; i < genome.length; i++)
            {
            Code.decode(d);
            genome[i] = d.d;
            }
        }

    public boolean equals(Object ind)
        {
        if (!(this.getClass().equals(ind.getClass())))
            return false; // SimpleRuleIndividuals are special.
        DoubleVectorIndividual i = (DoubleVectorIndividual) ind;
        if (genome.length != i.genome.length)
            return false;
        for (int j = 0; j < genome.length; j++)
            if (genome[j] != i.genome[j])
                return false;
        return true;
        }

    public Object getGenome()
        {
        return genome;
        }

    public void setGenome(Object gen)
        {
        genome = (double[]) gen;
        }

    public long genomeLength()
        {
        return genome.length;
        }

    public void writeGenotype(final EvolutionState state,
                              final DataOutput dataOutput) throws IOException
        {
        dataOutput.writeInt(genome.length);
        for (int x = 0; x < genome.length; x++)
            dataOutput.writeDouble(genome[x]);
        }

    public void readGenotype(final EvolutionState state,
                             final DataInput dataInput) throws IOException
        {
        int len = dataInput.readInt();
        if (genome == null || genome.length != len)
            genome = new double[len];
        for (int x = 0; x < genome.length; x++)
            genome[x] = dataInput.readDouble();
        }

    /** Clips each gene value to be within its specified [min,max] range.  
        NaN is presently considered in range but the behavior of this method
        should be assumed to be unspecified on encountering NaN. */
    public void clamp() 
        {
        FloatVectorSpecies _species = (FloatVectorSpecies)species;
        for (int i = 0; i < genomeLength(); i++)
            {
            double minGene = _species.minGene(i);
            if (genome[i] < minGene)
                genome[i] = minGene;
            else 
                {
                double maxGene = _species.maxGene(i);
                if (genome[i] > maxGene)
                    genome[i] = maxGene;
                }
            }
        }
                
    public void setGenomeLength(int len)
        {
        double[] newGenome = new double[len];
        System.arraycopy(genome, 0, newGenome, len, 
                         genome.length < newGenome.length ? genome.length : newGenome.length);
        genome = newGenome;
        }

    /** Returns true if each gene value is within is specified [min,max] range.
        NaN is presently considered in range but the behavior of this method
        should be assumed to be unspecified on encountering NaN. */
    public boolean isInRange() 
        {
        FloatVectorSpecies _species = (FloatVectorSpecies)species;
        for (int i = 0; i < genomeLength(); i++)
            if (genome[i] < _species.minGene(i) ||
                genome[i] > _species.maxGene(i)) return false;
        return true;
        }
    }
