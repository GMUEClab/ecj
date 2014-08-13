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

    public static final double MAXIMUM_INTEGER_IN_DOUBLE = 9.007199254740992E15;

    public double[] genome;

    public Parameter defaultBase()
        {
        return VectorDefaults.base().push(P_DOUBLEVECTORINDIVIDUAL);
        }

    public Object clone()
        {
        DoubleVectorIndividual myobj = (DoubleVectorIndividual) (super.clone());

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

        int len = Math.min(genome.length, i.genome.length);
        if (len != genome.length || len != i.genome.length)
            state.output.warnOnce("Genome lengths are not the same.  Vector crossover will only be done in overlapping region.");

        switch (s.crossoverType)
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
                for (int x = 0; x < len / s.chunksize; x++)
                    if (state.random[thread].nextBoolean(s.crossoverProbability))
                        for (int y = x * s.chunksize; y < (x + 1) * s.chunksize; y++)
                            {
                            tmp = i.genome[y];
                            i.genome[y] = genome[y];
                            genome[y] = tmp;
                            }
                break;
            case VectorSpecies.C_LINE_RECOMB:
            {
            double alpha = state.random[thread].nextDouble(true, true) * (1 + 2*s.lineDistance) - s.lineDistance;
            double beta = state.random[thread].nextDouble(true, true) * (1 + 2*s.lineDistance) - s.lineDistance;
            double t,u,min,max;
            for (int x = 0; x < len; x++)
                {
                min = s.minGene(x);
                max = s.maxGene(x);
                t = alpha * genome[x] + (1 - alpha) * i.genome[x];
                u = beta * i.genome[x] + (1 - beta) * genome[x];
                if (!(t < min || t > max || u < min || u > max))
                    {
                    genome[x] = t;
                    i.genome[x] = u; 
                    }
                }
            }
            break;
            case VectorSpecies.C_INTERMED_RECOMB:
            {
            double t,u,min,max;
            for (int x = 0; x < len; x++)
                {
                do
                    {
                    double alpha = state.random[thread].nextDouble(true, true) * (1 + 2*s.lineDistance) - s.lineDistance;
                    double beta = state.random[thread].nextDouble(true, true) * (1 + 2*s.lineDistance) - s.lineDistance;
                    min = s.minGene(x);
                    max = s.maxGene(x);
                    t = alpha * genome[x] + (1 - alpha) * i.genome[x];
                    u = beta * i.genome[x] + (1 - beta) * genome[x];
                    } while (t < min || t > max || u < min || u > max);
                genome[x] = t;
                i.genome[x] = u; 
                }
            }
            break;
            case VectorSpecies.C_SIMULATED_BINARY:
            {
            simulatedBinaryCrossover(state.random[thread], i, s.crossoverDistributionIndex);
            }
            break;
            default:
                state.output.fatal("In DoubleVectorIndividual.defaultCrossover, default case occurred when it shouldn't have");
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
            if (x >= pieces.length - 2)
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

        MersenneTwisterFast rng = state.random[thread];
        for(int x = 0; x < genome.length; x++)
            if (rng.nextBoolean(s.mutationProbability(x)))
                {
                double old = genome[x];
                for(int retries = 0; retries < s.duplicateRetries(x) + 1 + 1; retries++)
                    { 
                    switch(s.mutationType(x))
                        {
                        case FloatVectorSpecies.C_GAUSS_MUTATION:
                            gaussianMutation(state, rng, s, x);
                            break;
                        case FloatVectorSpecies.C_POLYNOMIAL_MUTATION:
                            polynomialMutation(state, rng, s, x);
                            break;
                        case FloatVectorSpecies.C_RESET_MUTATION:
                            floatResetMutation(rng, s, x);
                            break;
                        case FloatVectorSpecies.C_INTEGER_RESET_MUTATION:
                            integerResetMutation(rng, s, x);
                            break;
                        case FloatVectorSpecies.C_INTEGER_RANDOM_WALK_MUTATION:
                            integerRandomWalkMutation(rng, s, x);
                            break;
                        default:
                            state.output.fatal("In DoubleVectorIndividual.defaultMutate, default case occurred when it shouldn't have");
                            break;
                        }
                    if (genome[x] != old) break;
                    // else genome[x] = old;  // try again
                    }
                }
        }
        
    void integerRandomWalkMutation(MersenneTwisterFast random, FloatVectorSpecies species, int index)
        {
        double min = species.minGene(index);
        double max = species.maxGene(index);
        if (!species.mutationIsBounded(index))
            {
            // okay, technically these are still bounds, but we can't go beyond this without weird things happening
            max = MAXIMUM_INTEGER_IN_DOUBLE;
            min = -(max);
            }
        do
            {
            int n = (int)(random.nextBoolean() ? 1 : -1);
            double g = Math.floor(genome[index]);
            if ((n == 1 && g < max) ||
                (n == -1 && g > min))
                genome[index] = g + n;
            else if ((n == -1 && g < max) ||
                (n == 1 && g > min))
                genome[index] = g - n;     
            }
        while (random.nextBoolean(species.randomWalkProbability(index)));
        }

    void integerResetMutation(MersenneTwisterFast random, FloatVectorSpecies species, int index)
        {
        long minGene = (long)Math.floor(species.minGene(index));
        long maxGene = (long)Math.floor(species.maxGene(index));
        genome[index] = randomValueFromClosedInterval(minGene, maxGene, random); //minGene + random.nextLong(maxGene - minGene + 1);
        }

    void floatResetMutation(MersenneTwisterFast random, FloatVectorSpecies species, int index)
        {
        double minGene = species.minGene(index);
        double maxGene = species.maxGene(index);
        genome[index] = minGene + random.nextDouble(true, true) * (maxGene - minGene);
        }
    
    void gaussianMutation(EvolutionState state, MersenneTwisterFast random, FloatVectorSpecies species, int index)
        {
        double val;
        double min = species.minGene(index);
        double max = species.maxGene(index);
        double stdev = species.gaussMutationStdev(index);
        int outOfBoundsLeftOverTries = species.outOfBoundsRetries;
        boolean givingUpAllowed = species.outOfBoundsRetries != 0;
        do
            {
            val = random.nextGaussian() * stdev + genome[index];
            outOfBoundsLeftOverTries--;
            if (species.mutationIsBounded(index) && (val > max || val < min))
                {
                if (givingUpAllowed && (outOfBoundsLeftOverTries == 0))
                    {
                    val = min + random.nextDouble() * (max - min);
                    species.outOfRangeRetryLimitReached(state);// it better get inlined
                    break;
                    }
                } 
            else break;
            } 
        while (true);
        genome[index] = val;
        }
    
    void polynomialMutation(EvolutionState state, MersenneTwisterFast random, FloatVectorSpecies species, int index)
        {
        double eta_m = species.mutationDistributionIndex(index);
        boolean alternativePolynomialVersion = species.polynomialIsAlternative(index);
        
        double rnd, delta1, delta2, mut_pow, deltaq;
        double y, yl, yu, val, xy;
        double y1;

        y1 = y = genome[index];  // ind[index];
        yl = species.minGene(index); // min_realvar[index];
        yu = species.maxGene(index); // max_realvar[index];
        delta1 = (y-yl)/(yu-yl);
        delta2 = (yu-y)/(yu-yl);

        int totalTries = species.outOfBoundsRetries;
        int tries = 0;
        for(tries = 0; tries < totalTries || totalTries == 0; tries++)  // keep trying until totalTries is reached if it's not zero.  If it's zero, go on forever.
            {
            rnd = random.nextDouble();
            mut_pow = 1.0/(eta_m+1.0);
            if (rnd <= 0.5)
                {
                xy = 1.0-delta1;
                val = 2.0*rnd + (alternativePolynomialVersion ? (1.0-2.0*rnd)*(Math.pow(xy,(eta_m+1.0))) : 0.0);
                deltaq =  Math.pow(val,mut_pow) - 1.0;
                }
            else
                {
                xy = 1.0-delta2;
                val = 2.0*(1.0-rnd) + (alternativePolynomialVersion ? 2.0*(rnd-0.5)*(Math.pow(xy,(eta_m+1.0))) : 0.0);
                deltaq = 1.0 - (Math.pow(val,mut_pow));
                }
            y1 = y + deltaq*(yu-yl);
            if (!species.mutationIsBounded(index) || (y1 >= yl && y1 <= yu)) break;  // yay, found one
            }
                                                                
        // at this point, if tries is totalTries, we failed
        if (totalTries != 0 && tries == totalTries)
            {
            // just randomize
            y1 = (double)(species.minGene(index) + random.nextDouble(true, true) * (species.maxGene(index) - species.minGene(index)));  //(double)(min_realvar[index] + random.nextDouble() * (max_realvar[index] - min_realvar[index]));
            species.outOfRangeRetryLimitReached(state);// it better get inlined
            }
        genome[index] = y1; // ind[index] = y1;
        }

    
    /** This function is broken out to keep it identical to NSGA-II's mutation.c code. eta_m is the distribution
        index.  */
    public void polynomialMutate(EvolutionState state, MersenneTwisterFast random, double eta_m, boolean alternativePolynomialVersion, boolean mutationIsBounded)
        {
        FloatVectorSpecies s = (FloatVectorSpecies) species;
        double[] ind = genome;
        //double[] min_realvar = s.minGenes;
        //double[] max_realvar = s.maxGenes;
                
        double rnd, delta1, delta2, mut_pow, deltaq;
        double y, yl, yu, val, xy;
        double y1;
        for (int j=0; j < ind.length; j++)
            {
            if (random.nextBoolean(s.mutationProbability[j]))
                {
                y1 = y = ind[j];
                yl = s.minGene(j); //min_realvar[j];
                yu = s.maxGene(j); //max_realvar[j];
                delta1 = (y-yl)/(yu-yl);
                delta2 = (yu-y)/(yu-yl);

                int totalTries = s.outOfBoundsRetries;
                int tries = 0;
                for(tries = 0; tries < totalTries || totalTries == 0; tries++)  // keep trying until totalTries is reached if it's not zero.  If it's zero, go on forever.
                    {
                    rnd = (random.nextDouble());
                    mut_pow = 1.0/(eta_m+1.0);
                    if (rnd <= 0.5)
                        {
                        xy = 1.0-delta1;
                        val = 2.0*rnd + (alternativePolynomialVersion ? (1.0-2.0*rnd)*(Math.pow(xy,(eta_m+1.0))) : 0.0);
                        deltaq =  Math.pow(val,mut_pow) - 1.0;
                        }
                    else
                        {
                        xy = 1.0-delta2;
                        val = 2.0*(1.0-rnd) + (alternativePolynomialVersion ? 2.0*(rnd-0.5)*(Math.pow(xy,(eta_m+1.0))) : 0.0);
                        deltaq = 1.0 - (Math.pow(val,mut_pow));
                        }
                    y1 = y + deltaq*(yu-yl);
                    if (!mutationIsBounded || (y1 >= yl && y1 <= yu)) break;  // yay, found one
                    }
                                        
                // at this point, if tries is totalTries, we failed
                if (totalTries != 0 && tries == totalTries)
                    {
                    // just randomize
                    //y1 = (double)(min_realvar[j] + random.nextDouble(true, true) * (max_realvar[j] - min_realvar[j]));
                    y1 = (double)(s.minGene(j) + random.nextDouble(true, true) * (s.maxGene(j) - s.minGene(j)));
                    s.outOfRangeRetryLimitReached(state);// it better get inlined
                    }
                ind[j] = y1;
                }
            }
        }



    public void simulatedBinaryCrossover(MersenneTwisterFast random, DoubleVectorIndividual other, double eta_c)
        {
        final double EPS = FloatVectorSpecies.SIMULATED_BINARY_CROSSOVER_EPS;
        FloatVectorSpecies s = (FloatVectorSpecies) species;
        double[] parent1 = genome;
        double[] parent2 = other.genome;
        //double[] min_realvar = s.minGenes;
        //double[] max_realvar = s.maxGenes;
                
                
        double y1, y2, yl, yu;
        double c1, c2;
        double alpha, beta, betaq;
        double rand;
                
        for(int i = 0; i < parent1.length; i++)
            {
            if (random.nextBoolean())  // 0.5f
                {
                if (Math.abs(parent1[i] - parent2[i]) > EPS)
                    {
                    if (parent1[i] < parent2[i])
                        {
                        y1 = parent1[i];
                        y2 = parent2[i];
                        }
                    else
                        {
                        y1 = parent2[i];
                        y2 = parent1[i];
                        }
                    yl = s.minGene(i); //min_realvar[i];
                    yu = s.maxGene(i); //max_realvar[i];    
                    rand = random.nextDouble();
                    beta = 1.0 + (2.0*(y1-yl)/(y2-y1));
                    alpha = 2.0 - Math.pow(beta,-(eta_c+1.0));
                    if (rand <= (1.0/alpha))
                        {
                        betaq = Math.pow((rand*alpha),(1.0/(eta_c+1.0)));
                        }
                    else
                        {
                        betaq = Math.pow((1.0/(2.0 - rand*alpha)),(1.0/(eta_c+1.0)));
                        }
                    c1 = 0.5*((y1+y2)-betaq*(y2-y1));
                    beta = 1.0 + (2.0*(yu-y2)/(y2-y1));
                    alpha = 2.0 - Math.pow(beta,-(eta_c+1.0));
                    if (rand <= (1.0/alpha))
                        {
                        betaq = Math.pow((rand*alpha),(1.0/(eta_c+1.0)));
                        }
                    else
                        {
                        betaq = Math.pow((1.0/(2.0 - rand*alpha)),(1.0/(eta_c+1.0)));
                        }
                    c2 = 0.5*((y1+y2)+betaq*(y2-y1));
                    if (c1<yl)
                        c1=yl;
                    if (c2<yl)
                        c2=yl;
                    if (c1>yu)
                        c1=yu;
                    if (c2>yu)
                        c2=yu;
                    if (random.nextBoolean())
                        {
                        parent1[i] = c2;
                        parent2[i] = c1;
                        }
                    else
                        {
                        parent1[i] = c1;
                        parent2[i] = c2;
                        }
                    }
                else
                    {
                    // do nothing
                    }
                }
            else
                {
                // do nothing
                }
            }
        }


    // for longs
    long randomValueFromClosedInterval(long min, long max, MersenneTwisterFast random)
        {
        if (max - min < 0) // we had an overflow
            {
            long l = 0;
            do l = random.nextInt();
            while(l < min || l > max);
            return l;
            }
        else return min + random.nextLong(max - min + 1);
        }



    /**
     * Initializes the individual by randomly choosing doubles uniformly from
     * mingene to maxgene.
     */
    public void reset(EvolutionState state, int thread)
        {
        FloatVectorSpecies s = (FloatVectorSpecies) species;
        MersenneTwisterFast random = state.random[thread];
        for (int x = 0; x < genome.length; x++)
            {
            int type = s.mutationType(x);
            if (type == FloatVectorSpecies.C_INTEGER_RESET_MUTATION || 
                type == FloatVectorSpecies.C_INTEGER_RANDOM_WALK_MUTATION)  // integer type
                {
                long minGene = (long)Math.floor(s.minGene(x));
                long maxGene = (long)Math.floor(s.maxGene(x));
                genome[x] = randomValueFromClosedInterval(minGene, maxGene, random); //minGene + random.nextLong(maxGene - minGene + 1);
                }
            else
                {
                genome[x] = (s.minGene(x) + random.nextDouble(true, true) * (s.maxGene(x) - s.minGene(x)));
                }
            }
        }

    public int hashCode()
        {
        // stolen from GPIndividual. It's a decent algorithm.
        int hash = this.getClass().hashCode();

        hash = (hash << 1 | hash >>> 31);
        for (int x = 0; x < genome.length; x++)
            {
            long l = Double.doubleToLongBits(genome[x]);
            hash = (hash << 1 | hash >>> 31) ^ (int) ((l >>> 16) & 0xFFFFFFF) ^ (int) (l & 0xFFFF);
            }

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
        if (d.type != DecodeReturn.T_INTEGER)  // uh oh
            state.output.fatal("Individual with genome:\n" + s + "\n... does not have an integer at the beginning indicating the genome count.");
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
        if (ind == null) return false;
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

    public int genomeLength()
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
        System.arraycopy(genome, 0, newGenome, 0, 
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

    public double distanceTo(Individual otherInd)
        { 
        if (!(otherInd instanceof DoubleVectorIndividual)) 
            return super.distanceTo(otherInd);  // will return infinity!
                
        DoubleVectorIndividual other = (DoubleVectorIndividual) otherInd;
        double[] otherGenome = other.genome;
        double sumSquaredDistance =0.0;
        for(int i=0; i < other.genomeLength(); i++)
            {
            double dist = this.genome[i] - otherGenome[i];
            sumSquaredDistance += dist*dist;
            }
        return StrictMath.sqrt(sumSquaredDistance);
        }
    }
