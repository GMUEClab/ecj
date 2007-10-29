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
 * GeneVectorIndividual.java
 * Created: Thu Mar 22 13:13:20 EST 2001
 */

/**
 * GeneVectorIndividual is a VectorIndividual whose genome is an array of VectorGenes.
 * The default mutation method calls the mutate() method on each gene independently
 * with <tt>species.mutationProbability</tt>.  Initialization calls reset(), which
 * should call reset() on each gene.  Do not expect that the genes will actually
 * exist during initialization -- see the default implementation of reset() as an example
 * for how to handle this.
 *

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
 <p><b>Default Base</b><br>
 vector.gene-vect-ind

 * @author Sean Luke
 * @version 1.0
 */

public class GeneVectorIndividual extends VectorIndividual
    {
    public static final String P_GENEVECTORINDIVIDUAL = "gene-vect-ind";
    public VectorGene[] genome;
    
    public Parameter defaultBase()
        {
        return VectorDefaults.base().push(P_GENEVECTORINDIVIDUAL);
        }

    public Object clone()
        {
        GeneVectorIndividual myobj = (GeneVectorIndividual) (super.clone());

        // must clone the genome
        myobj.genome = (VectorGene[])(genome.clone());
        for(int x=0;x<genome.length;x++)
            myobj.genome[x] = (VectorGene)(genome[x].clone());
        
        return myobj;
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);  // actually unnecessary (Individual.setup() is empty)

        // since VectorSpecies set its constraint values BEFORE it called
        // super.setup(...) [which in turn called our setup(...)], we know that
        // stuff like genomeSize has already been set...
        
        Parameter def = defaultBase();
        
        if (!(species instanceof GeneVectorSpecies)) 
            state.output.fatal("GeneVectorIndividual requires a GeneVectorSpecies", base, def);
        GeneVectorSpecies s = (GeneVectorSpecies) species;
        
        // note that genome isn't initialized with any genes yet -- they're all null.
        // reset() needs
        genome = new VectorGene[s.genomeSize];
        reset(state,0);
        }
        
    public void defaultCrossover(EvolutionState state, int thread, VectorIndividual ind)
        {
        GeneVectorSpecies s = (GeneVectorSpecies) species;
        GeneVectorIndividual i = (GeneVectorIndividual) ind;
        VectorGene tmp;
        int point;

        if (genome.length != i.genome.length)
            state.output.fatal("Genome lengths are not the same for fixed-length vector crossover");
        switch(s.crossoverType)
            {
            case VectorSpecies.C_ONE_POINT:
                point = state.random[thread].nextInt((genome.length / s.chunksize)+1);
                for(int x=0;x<point*s.chunksize;x++)
                    { 
                    tmp = i.genome[x];
                    i.genome[x] = genome[x]; 
                    genome[x] = tmp; 
                    }
                break;
            case VectorSpecies.C_TWO_POINT: 
                int point0 = state.random[thread].nextInt((genome.length / s.chunksize)+1);
                point = state.random[thread].nextInt((genome.length / s.chunksize)+1);
                if (point0 > point) { int p = point0; point0 = point; point = p; }
                for(int x=point0*s.chunksize;x<point*s.chunksize;x++)
                    {
                    tmp = i.genome[x];
                    i.genome[x] = genome[x];
                    genome[x] = tmp;
                    }
                break;
            case VectorSpecies.C_ANY_POINT:
                for(int x=0;x<genome.length/s.chunksize;x++) 
                    if (state.random[thread].nextBoolean(s.crossoverProbability))
                        for(int y=x*s.chunksize;y<(x+1)*s.chunksize;y++)
                            {
                            tmp = i.genome[y];
                            i.genome[y] = genome[y];
                            genome[y] = tmp;
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
            pieces[x] = new VectorGene[point1-point0];
            System.arraycopy(genome,point0,pieces[x],0,point1-point0);
            point0 = point1;
            if (x==pieces.length-2)
                point1 = genome.length;
            else point1 = points[x+1];
            }
        }
    
    /** Joins the n pieces and sets the genome to their concatenation.*/
    public void join(Object[] pieces)
        {
        int sum=0;
        for(int x=0;x<pieces.length;x++)
            sum += ((VectorGene[])(pieces[x])).length;
        
        int runningsum = 0;
        VectorGene[] newgenome = new VectorGene[sum];
        for(int x=0;x<pieces.length;x++)
            {
            System.arraycopy(pieces[x], 0, newgenome, runningsum, ((VectorGene[])(pieces[x])).length);
            runningsum += ((VectorGene[])(pieces[x])).length;
            }
        // set genome
        genome = newgenome;
        }

    /** Destructively mutates the individual in some default manner.  The default form
        simply randomizes genes to a uniform distribution from the min and max of the gene values. */
    public void defaultMutate(EvolutionState state, int thread)
        {
        GeneVectorSpecies s = (GeneVectorSpecies) species;
        if (s.mutationProbability>0.0)
            for(int x=0;x<genome.length;x++)
                if (state.random[thread].nextBoolean(s.mutationProbability))
                    genome[x].mutate(state,thread);
        }

    /** Initializes the individual by calling reset(...) on each gene. */
    public void reset(EvolutionState state, int thread)
        {
        GeneVectorSpecies s = (GeneVectorSpecies) species;

        for(int x=0;x<genome.length;x++)
            {
            // first create the gene if it doesn't exist
            if (genome[x]==null) genome[x] = (VectorGene)(s.genePrototype.clone());
            // now reset it
            genome[x].reset(state,thread);
            }

        }

    public int hashCode()
        {
        // stolen from GPIndividual.  It's a decent algorithm.
        int hash = this.getClass().hashCode();

        for(int x=0;x<genome.length;x++)
            hash = ( hash << 1 | hash >>> 31 ) ^ genome[x].hashCode();

        return hash;
        }

    public String genotypeToStringForHumans()
        {
        StringBuffer s = new StringBuffer();
        for( int i = 0 ; i < genome.length ; i++ )
            { s.append(" "); s.append(genome[i].printGeneToStringForHumans()); }
        return s.toString();
        }
        
    public String genotypeToString()
        {
        StringBuffer s = new StringBuffer();
        for( int i = 0 ; i < genome.length ; i++ )
            { s.append(" "); s.append(genome[i].printGeneToString()); }
        return s.toString();
        }

    protected void parseGenotype(final EvolutionState state,
                                 final LineNumberReader reader) throws IOException
        {
        // read in the next line.  The first item is the number of genes
        String s = reader.readLine();
        DecodeReturn d = new DecodeReturn(s);
        Code.decode( d );
        int lll = (int)(d.l);

        genome = new VectorGene[ lll ];

        GeneVectorSpecies _species = (GeneVectorSpecies) species;
        for( int i = 0 ; i < genome.length ; i++ )
            {
            genome[i] = (VectorGene)(_species.genePrototype.clone());
            genome[i].readGene(state,reader);
            }
        }

    public boolean equals(Object ind)
        {
        if (!(this.getClass().equals(ind.getClass()))) return false;
        GeneVectorIndividual i = (GeneVectorIndividual)ind;
        if( genome.length != i.genome.length )
            return false;
        for( int j = 0 ; j < genome.length ; j++ )
            if( !(genome[j].equals(i.genome[j])))
                return false;
        return true;
        }

    public Object getGenome()
        { return genome; }
    public void setGenome(Object gen)
        { genome = (VectorGene[]) gen; }
    public long genomeLength()
        { return genome.length; }

    public void writeGenotype(final EvolutionState state,
                              final DataOutput dataOutput) throws IOException
        {
        dataOutput.writeInt(genome.length);
        for(int x=0;x<genome.length;x++)
            genome[x].writeGene(state,dataOutput);
        }

    public void setGenomeLength(int len)
        {
        GeneVectorSpecies s = (GeneVectorSpecies) species;
        VectorGene[] newGenome = new VectorGene[len];
        System.arraycopy(genome, 0, newGenome, len, 
                         genome.length < newGenome.length ? genome.length : newGenome.length);
        for(int x=genome.length; x< newGenome.length; x++)
            if (genome[x]==null) genome[x] = (VectorGene)(s.genePrototype.clone());  // not reset
        genome = newGenome;
        }

    public void readGenotype(final EvolutionState state,
                             final DataInput dataInput) throws IOException
        {
        int len = dataInput.readInt();
        if (genome==null || genome.length != len)
            genome = new VectorGene[len];
        GeneVectorSpecies _species = (GeneVectorSpecies) species;

        for(int x=0;x<genome.length;x++)
            {
            genome[x] = (VectorGene)(_species.genePrototype.clone());
            genome[x].readGene(state,dataInput);
            }
        }

    }
