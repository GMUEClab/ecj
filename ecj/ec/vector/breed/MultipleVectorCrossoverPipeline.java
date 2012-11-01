/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.vector.breed;

import ec.vector.*;
import ec.*;
import ec.util.Parameter;


/* 
 * MultipleVectorCrossoverPipeline.java
 * 
 * Created: Thu May 14 2009
 * By: Beenish Jamil
 */


/**
 *
 MultipleVectorCrossoverPipeline is a BreedingPipeline which implements a uniform
 (any point) crossover between multiple vectors. It is intended to be used with
 three or more vectors. It takes n parent individuals and returns n crossed over
 individuals. The number of parents and consequently children is specified by the
 number of sources parameter. 
 <p>The standard vector crossover probability is used for this crossover type. 
 <br> <i> Note</i> : It is necessary to set the crossover-type parameter to 'any' 
 in order to use this pipeline.
 
 
 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 number of parents

 <p><b>Number of Sources</b><br>
 variable (generally 3 or more)

 
 <p><b>Default Base</b><br>
 vector.multixover
*/

// This class is MUCH MUCH longer than it need be.  We could just do it by using 
// ECJ's generic split and join operations, but only rely on that in the default
// case, and instead use faster per-array operations.


public class MultipleVectorCrossoverPipeline extends BreedingPipeline {

    /** default base */
    public static final String P_CROSSOVER = "multixover";
    
    /** Temporary holding place for parents */
    VectorIndividual[] parents;
   
    public Parameter defaultBase() { return VectorDefaults.base().push(P_CROSSOVER); }    
    
    /** Returns the number of parents */
    public int numSources() { return DYNAMIC_SOURCES;}
    
    public Object clone()
        {
        MultipleVectorCrossoverPipeline c = (MultipleVectorCrossoverPipeline)(super.clone());

        // deep-cloned stuff
        c.parents = (VectorIndividual[]) parents.clone();

        return c;
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);        
        parents = new VectorIndividual[sources.length];
        }
        
    /**
     * Returns the minimum number of children that are produced per crossover 
     */
    public int typicalIndsProduced()
        {
        return minChildProduction()*sources.length; // minChild is always 1     
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


        if(inds[0] instanceof BitVectorIndividual)
            n = multipleBitVectorCrossover(min, max, start, subpopulation, // redundant reassignment
                inds, state, thread);
        
        else if(inds[0] instanceof ByteVectorIndividual)
            n = multipleByteVectorCrossover(min, max, start, subpopulation, 
                inds, state, thread);
        
        else if(inds[0] instanceof DoubleVectorIndividual)
            n = multipleDoubleVectorCrossover(min, max, start, subpopulation, 
                inds, state, thread);
        
        else if(inds[0] instanceof FloatVectorIndividual)
            n = multipleFloatVectorCrossover(min, max, start, subpopulation, 
                inds, state, thread);

        else if(inds[0] instanceof IntegerVectorIndividual)
            n = multipleIntegerVectorCrossover(min, max, start, subpopulation,
                inds, state, thread);
        
        else if(inds[0] instanceof GeneVectorIndividual)
            n = multipleGeneVectorCrossover(min, max, start, subpopulation, 
                inds, state, thread);

        else if(inds[0] instanceof LongVectorIndividual)
            n = multipleLongVectorCrossover(min, max, start, subpopulation, 
                inds, state, thread);

        else if(inds[0] instanceof ShortVectorIndividual)
            n = multipleShortVectorCrossover(min, max, start, subpopulation, 
                inds, state, thread);

        else // default crossover -- shouldn't need this unless a new vector type is added
            {
            // check how many sources are provided
            if(sources.length <= 2)
                // this method shouldn't be called for just two parents 
                state.output.error("Only two parents specified!"); 


            // fill up parents: 
            for(int i = 0;i<parents.length; i++) // parents.length == sources.length
                {               
                // produce one parent from each source 
                sources[i].produce(1,1,i,subpopulation,parents,state,thread);
                if (!(sources[i] instanceof BreedingPipeline))  // it's a selection method probably
                    parents[i] = (VectorIndividual)(parents[i].clone());
                }


            //... some required intermediary steps ....

            // assuming all of the species are the same species ... 
            VectorSpecies species = (VectorSpecies)parents[0].species;

            // an array of the split points (width = 1)
            int[] points = new int[(int)parents[0].genomeLength() - 1];
            for(int i = 0; i < points.length; i++){
                points[i] = i+1;    // first split point/index = 1
                }


            // split all the parents into object arrays 
            Object[][] pieces = new Object[parents.length][(int)parents[0].genomeLength()]; 

            // splitting...
            for(int i = 0; i < parents.length; i++){
                if(parents[i].genomeLength() != parents[0].genomeLength())              
                    state.output.fatal("All vectors must be of the same length for crossover!");
                else
                    parents[i].split(points, pieces[i]);
                }


            // crossing them over now
            for(int i = 0; i < pieces[0].length; i++)
                {   
                if(state.random[thread].nextBoolean(species.crossoverProbability))
                    {
                    // shuffle
                    for(int j = pieces.length-1; j > 0; j--) // no need to shuffle first index at the end
                        {
                        // find parent to swap piece with
                        int parent2 = state.random[thread].nextInt(j); // not inclusive; don't want to swap with self
                        // swap
                        Object temp = pieces[j][i];
                        pieces[j][i] = pieces[parent2][i];
                        pieces[parent2][i] = temp;
                        }
                    }
                }

            // join them and add them to the population starting at the start location
            for(int i = 0, q = start; i < parents.length; i++, q++)
                { 
                parents[i].join(pieces[i]);                         
                parents[i].evaluated = false;
                if(q<inds.length) // just in case
                    {               
                    inds[q] = (VectorIndividual)parents[i];
                    }
                }
            }
        return n;
        }
    
    
    /** Crosses over the Bit Vector Individuals using a 
        uniform crossover method.      
        * 
        * There is no need to call this method separately; produce(...) calls it
        * whenever necessary by default. 
        */
    public int multipleBitVectorCrossover(final int min, 
        final int max, 
        final int start,
        final int subpopulation,
        final Individual[] inds,
        final EvolutionState state,
        final int thread)
        {
        if(!(inds[0] instanceof BitVectorIndividual))
            state.output.fatal("Trying to produce bit vector individuals when you can't!");
        
        
        // check how many sources are provided
        if(sources.length <= 2)
            // this method shouldn't be called for just two parents 
            state.output.error("Only two parents specified!"); 

        
        // how many individuals should we make?
        int n = typicalIndsProduced();
        if (n < min) n = min;
        if (n > max) n = max;
            
        
        
        // fill up parents: 
        for(int i = 0;i<parents.length; i++) // parents.length == sources.length
            {                   
            // produce one parent from each source 
            sources[i].produce(1,1,i,subpopulation,parents,state,thread);
            if (!(sources[i] instanceof BreedingPipeline))  // it's a selection method probably
                parents[i] = (BitVectorIndividual)(parents[i].clone());
            }
        
        
        VectorSpecies species = (VectorSpecies)inds[0].species; // doesn't really matter if 
        //this is dblvector or vector as long as we
        // can get the crossover probability

        
        // crossover
        for(int i = 0; i < parents[0].genomeLength(); i++)
            {
            if(state.random[thread].nextBoolean(species.crossoverProbability))
                {               
                for(int j = parents.length-1; j > 0; j--)
                    {                       
                    int swapIndex = state.random[thread].nextInt(j); // not inclusive; don't want to swap with self                     
                    boolean temp = ((BitVectorIndividual) parents[j]).genome[i]; // modifying genomes directly. it's okay since they're clones
                    ((BitVectorIndividual)parents[j]).genome[i] = 
                        ((BitVectorIndividual)parents[swapIndex]).genome[i];
                    ((BitVectorIndividual)parents[swapIndex]).genome[i] = temp;                 
                    }                       
                }
            }
        
        // add to population
        for(int i = 0, q = start; i < parents.length; i++, q++)
            {         
            parents[i].evaluated = false;
            if(q<inds.length) // just in case
                {           
                inds[q] = (BitVectorIndividual)parents[i];
                }
            }       
        return n;       
        }
    
   
    /** Crosses over the Byte Vector Individuals using a 
        uniform crossover method.      
        * 
        * There is no need to call this method separately; produce(...) calls it
        * whenever necessary by default. 
        */
    public int multipleByteVectorCrossover(final int min, 
        final int max, 
        final int start,
        final int subpopulation,
        final Individual[] inds,
        final EvolutionState state,
        final int thread)
        {
        if(!(inds[0] instanceof ByteVectorIndividual))
            state.output.fatal("Trying to produce byte vector individuals when you can't!");
        
        
        // check how many sources are provided
        if(sources.length <= 2)
            // this method shouldn't be called for just two parents 
            state.output.error("Only two parents specified!"); 

        
        // how many individuals should we make?
        int n = typicalIndsProduced();
        if (n < min) n = min;
        if (n > max) n = max;
            
        
        
        // fill up parents: 
        for(int i = 0;i<parents.length; i++) // parents.length == sources.length
            {                   
            // produce one parent from each source 
            sources[i].produce(1,1,i,subpopulation,parents,state,thread);
            if (!(sources[i] instanceof BreedingPipeline))  // it's a selection method probably
                parents[i] = (ByteVectorIndividual)(parents[i].clone());
            }
        
        
        VectorSpecies species = (VectorSpecies)inds[0].species; // doesn't really matter if 
        //this is dblvector or vector as long as we
        // can get the crossover probability

        
        // crossover
        for(int i = 0; i < parents[0].genomeLength(); i++)
            {
            if(state.random[thread].nextBoolean(species.crossoverProbability))
                {               
                for(int j = parents.length-1; j > 0; j--)
                    {                       
                    int swapIndex = state.random[thread].nextInt(j); // not inclusive; don't want to swap with self                     
                    byte temp = ((ByteVectorIndividual) parents[j]).genome[i]; // modifying genomes directly. it's okay since they're clones
                    ((ByteVectorIndividual)parents[j]).genome[i] = ((ByteVectorIndividual)parents[swapIndex]).genome[i];
                    ((ByteVectorIndividual)parents[swapIndex]).genome[i] = temp;                        
                    }                       
                }
            }
        
        // add to population
        for(int i = 0, q = start; i < parents.length; i++, q++)
            {         
            parents[i].evaluated = false;
            if(q<inds.length) // just in case
                {           
                inds[q] = (ByteVectorIndividual)parents[i];
                }
            }       
        return n;       
        }
    
    
    /** Crosses over the Double Vector Individuals using a 
        uniform crossover method.    
        * 
        * There is no need to call this method separately; produce(...) calls it
        * whenever necessary by default.   
        */
    public int multipleDoubleVectorCrossover(final int min, 
        final int max, 
        final int start,
        final int subpopulation,
        final Individual[] inds,
        final EvolutionState state,
        final int thread)
        {
        
        if(!(inds[0] instanceof DoubleVectorIndividual))
            state.output.fatal("Trying to produce double vector individuals when you can't!");
        
        // check how many sources are provided
        if(sources.length <= 2)
            // this method shouldn't be called for just two parents 
            state.output.error("Only two parents specified!"); 

        
        // how many individuals should we make?
        int n = typicalIndsProduced();
        if (n < min) n = min;
        if (n > max) n = max;
            
        

        // fill up parents: 
        for(int i = 0;i<parents.length; i++) // parents.length == sources.length
            {                   
            // produce one parent from each source 
            sources[i].produce(1,1,i,subpopulation,parents,state,thread);
            if (!(sources[i] instanceof BreedingPipeline))  // it's a selection method probably
                parents[i] = (DoubleVectorIndividual)(parents[i].clone());
            }
        
        
        VectorSpecies species = (VectorSpecies)inds[0].species; // doesn't really matter if 
        //this is dblvector or vector as long as we
        // can get the crossover probability
        
        
        // crossover
        for(int i = 0; i < parents[0].genomeLength(); i++)
            {
            if(state.random[thread].nextBoolean(species.crossoverProbability))
                {               
                for(int j = parents.length-1; j > 0; j--)
                    {                       
                    int swapIndex = state.random[thread].nextInt(j); // not inclusive; don't want to swap with self                     
                    double temp = ((DoubleVectorIndividual) parents[j]).genome[i]; // modifying genomes directly. it's okay since they're clones
                    ((DoubleVectorIndividual)parents[j]).genome[i] = ((DoubleVectorIndividual)parents[swapIndex]).genome[i];
                    ((DoubleVectorIndividual)parents[swapIndex]).genome[i] = temp;                      
                    }                       
                }
            }
        
        // add to population
        for(int i = 0, q = start; i < parents.length; i++, q++)
            {         
            parents[i].evaluated = false;
            if(q<inds.length) // just in case
                {           
                inds[q] = (DoubleVectorIndividual)parents[i];
                }
            }       
        return n;       
        }

    
    /** Crosses over the Float Vector Individuals using a 
        uniform crossover method.      
        * 
        * There is no need to call this method separately; produce(...) calls it
        * whenever necessary by default. 
        */
    public int multipleFloatVectorCrossover(final int min, 
        final int max, 
        final int start,
        final int subpopulation,
        final Individual[] inds,
        final EvolutionState state,
        final int thread)
        {
        if(!(inds[0] instanceof FloatVectorIndividual))
            state.output.fatal("Trying to produce float vector individuals when you can't!");
        
        
        // check how many sources are provided
        if(sources.length <= 2)
            // this method shouldn't be called for just two parents 
            state.output.error("Only two parents specified!"); 

        
        // how many individuals should we make?
        int n = typicalIndsProduced();
        if (n < min) n = min;
        if (n > max) n = max;
            
        
        
        // fill up parents: 
        for(int i = 0;i<parents.length; i++) // parents.length == sources.length
            {                   
            // produce one parent from each source 
            sources[i].produce(1,1,i,subpopulation,parents,state,thread);
            if (!(sources[i] instanceof BreedingPipeline))  // it's a selection method probably
                parents[i] = (FloatVectorIndividual)(parents[i].clone());
            }
        
        
        VectorSpecies species = (VectorSpecies)inds[0].species; // doesn't really matter if 
        //this is dblvector or vector as long as we
        // can get the crossover probability

        
        // crossover
        for(int i = 0; i < parents[0].genomeLength(); i++)
            {
            if(state.random[thread].nextBoolean(species.crossoverProbability))
                {               
                for(int j = parents.length-1; j > 0; j--)
                    {                       
                    int swapIndex = state.random[thread].nextInt(j); // not inclusive; don't want to swap with self                     
                    float temp = ((FloatVectorIndividual) parents[j]).genome[i]; // modifying genomes directly. it's okay since they're clones
                    ((FloatVectorIndividual)parents[j]).genome[i] = ((FloatVectorIndividual)parents[swapIndex]).genome[i];
                    ((FloatVectorIndividual)parents[swapIndex]).genome[i] = temp;                       
                    }                       
                }
            }
        
        // add to population
        for(int i = 0, q = start; i < parents.length; i++, q++)
            {         
            parents[i].evaluated = false;
            if(q<inds.length) // just in case
                {           
                inds[q] = (FloatVectorIndividual)parents[i];
                }
            }       
        return n;       
        }
    
       
    /** Crosses over the Gene Vector Individuals using a 
        uniform crossover method.      
        * 
        * There is no need to call this method separately; produce(...) calls it
        * whenever necessary by default. 
        */
    public int multipleGeneVectorCrossover(final int min, 
        final int max, 
        final int start,
        final int subpopulation,
        final Individual[] inds,
        final EvolutionState state,
        final int thread)
        {
        if(!(inds[0] instanceof GeneVectorIndividual))
            state.output.fatal("Trying to produce gene vector individuals when you can't!");
        
        
        // check how many sources are provided
        if(sources.length <= 2)
            // this method shouldn't be called for just two parents 
            state.output.error("Only two parents specified!"); 

        
        // how many individuals should we make?
        int n = typicalIndsProduced();
        if (n < min) n = min;
        if (n > max) n = max;
            
        
        
        // fill up parents: 
        for(int i = 0;i<parents.length; i++) // parents.length == sources.length
            {                   
            // produce one parent from each source 
            sources[i].produce(1,1,i,subpopulation,parents,state,thread);
            if (!(sources[i] instanceof BreedingPipeline))  // it's a selection method probably
                parents[i] = (GeneVectorIndividual)(parents[i].clone());
            }
        
        
        VectorSpecies species = (VectorSpecies)inds[0].species; // doesn't really matter if 
        //this is dblvector or vector as long as we
        // can get the crossover probability

        
        // crossover
        for(int i = 0; i < parents[0].genomeLength(); i++)
            {
            if(state.random[thread].nextBoolean(species.crossoverProbability))
                {               
                for(int j = parents.length-1; j > 0; j--)
                    {                       
                    int swapIndex = state.random[thread].nextInt(j); // not inclusive; don't want to swap with self                     
                    Gene temp = ((GeneVectorIndividual) parents[j]).genome[i]; // modifying genomes directly. it's okay since they're clones
                    ((GeneVectorIndividual)parents[j]).genome[i] = ((GeneVectorIndividual)parents[swapIndex]).genome[i];
                    ((GeneVectorIndividual)parents[swapIndex]).genome[i] = temp;                        
                    }                       
                }
            }
        
        // add to population
        for(int i = 0, q = start; i < parents.length; i++, q++)
            {         
            parents[i].evaluated = false;
            if(q<inds.length) // just in case
                {           
                inds[q] = (GeneVectorIndividual)parents[i];
                }
            }       
        return n;       
        }

       
    /**Crosses over the Integer Vector Individuals using a uniform crossover method.   
     * 
     * There is no need to call this method separately; produce(...) calls it
     * whenever necessary by default.    
     */
    public int multipleIntegerVectorCrossover(final int min, 
        final int max, 
        final int start,
        final int subpopulation,
        final Individual[] inds,
        final EvolutionState state,
        final int thread)
        {
        
        if(!(inds[0] instanceof IntegerVectorIndividual))
            state.output.fatal("Trying to produce integer vector individuals when you can't!");
        

        
        // check how many sources are provided
        if(sources.length <= 2)
            // this method shouldn't be called for just two parents 
            state.output.error("Only two parents specified!"); 

        
        // how many individuals should we make?
        int n = typicalIndsProduced();
        if (n < min) n = min;
        if (n > max) n = max;
            
            
        
        // fill up parents: 
        for(int i = 0;i<parents.length; i++) // parents.length == sources.length
            {                   
            // produce one parent from each source      
            sources[i].produce(1,1,i,subpopulation,parents,state,thread);
            if (!(sources[i] instanceof BreedingPipeline))  // it's a selection method probably
                parents[i] = (IntegerVectorIndividual)(parents[i].clone());

            }
        
        
        VectorSpecies species = (VectorSpecies)inds[0].species; // doesn't really matter if 
        //this is dblvector or vector as long as we
        // can get the crossover probability

        
        // crossover
        for(int i = 0; i < parents[0].genomeLength(); i++)
            {
            if(state.random[thread].nextBoolean(species.crossoverProbability))
                {               
                for(int j = parents.length-1; j > 0; j--)
                    {                       
                    int swapIndex = state.random[thread].nextInt(j); // not inclusive; don't want to swap with self                     
                    int temp = ((IntegerVectorIndividual) parents[j]).genome[i]; // modifying genomes directly. it's okay since they're clones
                    ((IntegerVectorIndividual)parents[j]).genome[i] = ((IntegerVectorIndividual)parents[swapIndex]).genome[i];
                    ((IntegerVectorIndividual)parents[swapIndex]).genome[i] = temp;                     
                    }                       
                }
            }
        
        // add to population
        for(int i = 0, q = start; i < parents.length; i++, q++)
            {         
            parents[i].evaluated = false;
            if(q<inds.length) // just in case
                {           
                inds[q] = (IntegerVectorIndividual)parents[i];
                }
            }       
        return n;       
        }
 
       
    /** Crosses over the Long Vector Individuals using a 
        uniform crossover method.      
        * 
        * There is no need to call this method separately; produce(...) calls it
        * whenever necessary by default. 
        */
    public int multipleLongVectorCrossover(final int min, 
        final int max, 
        final int start,
        final int subpopulation,
        final Individual[] inds,
        final EvolutionState state,
        final int thread)
        {
        if(!(inds[0] instanceof LongVectorIndividual))
            state.output.fatal("Trying to produce long vector individuals when you can't!");
        
        
        // check how many sources are provided
        if(sources.length <= 2)
            // this method shouldn't be called for just two parents 
            state.output.error("Only two parents specified!"); 

        
        // how many individuals should we make?
        int n = typicalIndsProduced();
        if (n < min) n = min;
        if (n > max) n = max;
            
        
        
        // fill up parents: 
        for(int i = 0;i<parents.length; i++) // parents.length == sources.length
            {                   
            // produce one parent from each source 
            sources[i].produce(1,1,i,subpopulation,parents,state,thread);
            if (!(sources[i] instanceof BreedingPipeline))  // it's a selection method probably
                parents[i] = (LongVectorIndividual)(parents[i].clone());
            }
        
        
        VectorSpecies species = (VectorSpecies)inds[0].species; // doesn't really matter if 
        //this is dblvector or vector as long as we
        // can get the crossover probability
        
        // crossover
        for(int i = 0; i < parents[0].genomeLength(); i++)
            {
            if(state.random[thread].nextBoolean(species.crossoverProbability))
                {               
                for(int j = parents.length-1; j > 0; j--)
                    {                       
                    int swapIndex = state.random[thread].nextInt(j); // not inclusive; don't want to swap with self                     
                    long temp = ((LongVectorIndividual) parents[j]).genome[i]; // modifying genomes directly. it's okay since they're clones
                    ((LongVectorIndividual)parents[j]).genome[i] = ((LongVectorIndividual)parents[swapIndex]).genome[i];
                    ((LongVectorIndividual)parents[swapIndex]).genome[i] = temp;                        
                    }                       
                }
            }
        
        // add to population
        for(int i = 0, q = start; i < parents.length; i++, q++)
            {         
            parents[i].evaluated = false;
            if(q<inds.length) // just in case
                {           
                inds[q] = (LongVectorIndividual)parents[i];
                }
            }       
        return n;       
        }
    
      
    /** Crosses over the Short Vector Individuals using a 
        uniform crossover method.      
        * 
        * There is no need to call this method separately; produce(...) calls it
        * whenever necessary by default. 
        */
    public int multipleShortVectorCrossover(final int min, 
        final int max, 
        final int start,
        final int subpopulation,
        final Individual[] inds,
        final EvolutionState state,
        final int thread)
        {
        if(!(inds[0] instanceof ShortVectorIndividual))
            state.output.fatal("Trying to produce short vector individuals when you can't!");
        
        
        // check how many sources are provided
        if(sources.length <= 2)
            // this method shouldn't be called for just two parents 
            state.output.error("Only two parents specified!"); 

        
        // how many individuals should we make?
        int n = typicalIndsProduced();
        if (n < min) n = min;
        if (n > max) n = max;
            
        
        
        // fill up parents: 
        for(int i = 0;i<parents.length; i++) // parents.length == sources.length
            {                   
            // produce one parent from each source 
            sources[i].produce(1,1,i,subpopulation,parents,state,thread);
            if (!(sources[i] instanceof BreedingPipeline))  // it's a selection method probably
                parents[i] = (ShortVectorIndividual)(parents[i].clone());
            }
        
        
        VectorSpecies species = (VectorSpecies)inds[0].species; // doesn't really matter if 
        //this is dblvector or vector as long as we
        // can get the crossover probability

        
        // crossover
        for(int i = 0; i < parents[0].genomeLength(); i++)
            {
            if(state.random[thread].nextBoolean(species.crossoverProbability))
                {               
                for(int j = parents.length-1; j > 0; j--)
                    {                       
                    int swapIndex = state.random[thread].nextInt(j); // not inclusive; don't want to swap with self                     
                    short temp = ((ShortVectorIndividual) parents[j]).genome[i]; // modifying genomes directly. it's okay since they're clones
                    ((ShortVectorIndividual)parents[j]).genome[i] = ((ShortVectorIndividual)parents[swapIndex]).genome[i];
                    ((ShortVectorIndividual)parents[swapIndex]).genome[i] = temp;                       
                    }                       
                }
            }
        
        // add to population
        for(int i = 0, q = start; i < parents.length; i++, q++)
            {         
            parents[i].evaluated = false;
            if(q<inds.length) // just in case
                {           
                inds[q] = (ShortVectorIndividual)parents[i];
                }
            }       
        return n;       
        }
    }
