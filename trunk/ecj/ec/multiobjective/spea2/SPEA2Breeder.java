package ec.multiobjective.spea2;
/*
  Copyright 2006 by Robert Hubley
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

import ec.*;
import ec.util.*;
import ec.simple.*; 

/* 
 * SPEA2Breeder.java
 * 
 * Created: Wed Jun 26 11:20:32 PDT 2002
 * By: Robert Hubley, Institute for Systems Biology
 *     (Based on Breeder.java by Sean Luke)
 */

/**
 * Breeds each subpopulation separately, with no inter-population exchange,
 * and using the SPEA2 approach.  A SPEA2Breeder may have multiple
 * threads; it divvys up a subpopulation into chunks and hands one chunk
 * to each thread to populate.  One array of BreedingPipelines is obtained
 * from a population's Species for each operating breeding thread.
 *
 * Prior to breeding a subpopulation, a SPEA2Breeder will first fill part of the
 * new subpopulation (the archive) with the individuals with an SPEA2 fitness
 * of less than 1.0 from the old subpopulation.  If there are less individuals
 * with this cutoff than can fit in the archive the free slots are filled with
 * the lowest scoring SPEA2fitness individuals.  If there are more individuals
 * with an SPEA2Fitness less than 1 than can fit in the archive then a density
 * metric is used to truncate the archive and remove individuals which are close
 * to others.
 *
 * The archive filling step is performed by a single thread.
 
 * <p>
 * This is actually a re-write of SPEA2Breeder.java by Robert Hubley, to make it more modular so it's easier for me to extend it.
 * Namely:
 * <ol>
 * <li> I isolated the following functionality: "Out of N individuals, give me a front of size M < N."
 * 
 * I need this at the last generation, when one createad a bunch of individuals, evaluated them, 
 * but not give them a chance to enter the archive, as the breeder is not called on the last generation!
 * 
 * <li>Additonally, I made <code>double[][] distances</code> and <code>int[][] sortedIndex</code> static and then reused them to reduce GC!!!
 * For more advanced cases where the the number of individuals is not fixed and/or known in advance I chose to have these arrays extended when needed.  
 * <ul>
 * <li> Note that they need not be shrunk.
 * <li> Note that in the usual case when the number of individuals from the old population is 
 * always the same, the arrays are only allocated once, so there's no efficiency loss here.
 * (except I keep asking `are they big enough?' each time <code>loadElites()</code> is called, but that's not a big deal
 * given that <code>loadElites()</code> is O(n^3)).
 * </ul>
 * 
 * <li>Lastly, I do fewer iterations in the loop that compacts surviving individuals 
 * and copies them in the new population (i.e. previous version was visiting
 * a bunch of nulls at the end of the array).
 * </ol>
 *
 * @author Robert Hubley (based on Breeder.java by Sean Luke), Gabriel Balan, Keith Sullivan
 * @version 1.1
 */

public class SPEA2Breeder extends SimpleBreeder
    {
    public void setup(final EvolutionState state, final Parameter base) 
        {
        super.setup(state, base);
        // make sure SimpleBreeder's elites facility isn't being used
        for(int i=0;i<elite.length;i++)
            if (elite[i] != 0)
                state.output.fatal("Elites may not be used with SPEA2Breeder");
        }
    
    // this version returns the archive size for the subpopulation rather than using
    // SimpleBreeder's elites mechanism -- perhaps we should unify this some day.
    public int computeSubpopulationLength(EvolutionState state, int subpopulation)
        {
        SPEA2Subpopulation subpop = (SPEA2Subpopulation)state.population.subpops[subpopulation];
        return subpop.individuals.length - subpop.archiveSize;
        }


    // overrides the loadElites function to load the archive the way we'd like to do it.
    public void loadElites(EvolutionState state, Population newpop)
        {
        //state.output.println("Loading the SPEA2 archive...",V_DEBUG, Log.D_STDOUT);

        for(int sub=0;sub<state.population.subpops.length;sub++) 
            {
            /** The new population after we are done picking the elites */
            Individual[] newInds = newpop.subpops[sub].individuals;
            SPEA2Subpopulation spop = (SPEA2Subpopulation)state.population.subpops[sub];
            /** The old population from which to pick elites  */
            Individual[] oldInds = spop.individuals;
            loadElites(state, oldInds, newInds, spop.archiveSize);
            }
        }


    /** An array of distances between elites; it's static so we save on memory allocation/garbage collection. */
    static double[][] _distances = null;
    /** An array of indexes to individuals sorted by distances; it's static so we save on memory allocation/garbage collection. */
    static int[][] _sortedIndex = null;
    //it's ok that these are static: they're only used in loadElites,
    //which is called before forking the breeding threads
    //(i.e. NO RACE CONDITION!)
    
    /*
     * It's not enough to sort the array and keep the best archive-size individuals.
     * Zitzler says: copy all undominated into the new archive.
     * -if still room in the archive, fill it with dominated inds in order of their fitness;
     * -if too many points, you prune the archive with an ITERATIVE process in which 
     * you drop the point with the closest neighbor in the [still overpopulated] archive.
     * 
     *  
     * S_i= number of inds ind_i dominates              //strength
     * R_i = sum_{j dom i} S_j                                  //raw fitness
     * D_i = 1/[2+dist_to_kth...]                               //density
     * F=R+D                                                                    //fitness,
     * 
     * D<=1/2 and S,R \in N so D matters only if R is a tie!
     * R_undominated = 0.
     * 
     * So all undominated come before the dominated no matter what!!!
     * It's when there are too many undominated that you need to work hard :(
     */
    public static void loadElites(EvolutionState state, Individual[] oldInds, Individual[] newInds, int archiveSize)
        {

        // Sort the old guys
        //sort(oldInds);
        QuickSort.qsort(oldInds, new SortComparator()
            {
            /** Returns true if a < b, else false */
            public boolean lt(Object a, Object b)
                {
                return ((SPEA2MultiObjectiveFitness)(((Individual)a).fitness)).SPEA2Fitness <
                    ((SPEA2MultiObjectiveFitness)(((Individual)b).fitness)).SPEA2Fitness;
                            
                }
                    
            /** Returns true if a > b, else false */
            public boolean gt(Object a, Object b)
                {
                return ((SPEA2MultiObjectiveFitness)(((Individual)a).fitness)).SPEA2Fitness >
                    ((SPEA2MultiObjectiveFitness)(((Individual)b).fitness)).SPEA2Fitness;
                            
                }
            });

        // Null out non-candidates and count
        int nIndex = 1;
        for(int x=0;x<oldInds.length;x++)
            {
            if ( nIndex > archiveSize && 
                ((SPEA2MultiObjectiveFitness)oldInds[x].fitness).SPEA2Fitness >= 1 )
                {
                oldInds[x] = null;
                }else {
                nIndex++;
                }
            }
        nIndex--;

        // Check to see if we need to truncate the archive
        if ( nIndex > archiveSize ) 
            {
            double[][] distances = _distances;
            int[][] sortedIndex = _sortedIndex;
            //I'll reuse the previously allocated matrices, unless they're too small.
            if(distances==null ||distances.length<nIndex)
                {
                distances = _distances = new double[nIndex][nIndex];
                sortedIndex = _sortedIndex = new int[nIndex][nIndex];
                }
                
            // Set distances
            state.output.println("  Truncating the archive",Output.V_NO_MESSAGES, Log.D_STDOUT);
            //state.output.println("    - Calculating distances",V_DEBUG, Log.D_STDOUT);
            for ( int y=0; y<nIndex; y++ ) 
                {
                for(int z=y+1;z<nIndex;z++)
                    {
                    distances[y][z] =
                        ((SPEA2MultiObjectiveFitness)oldInds[y].fitness).
                        calcDistance( (SPEA2MultiObjectiveFitness)oldInds[z].fitness );
                    distances[z][y] = distances[y][z];
                    } // For each individual yz calculate fitness distance
                distances[y][y] = -1;
                //Sure, you'll ask "why not POSITIVE infinity?"
                //all points have -1 as their first min (so an n-way tie that prunes nobody);
                //might as well make it the last tie!
                //Hubley skips the first tie, so it's correct.
                
                } // For each individual y  calculate fitness distances

            //state.output.println("    - Sorting distances",V_DEBUG, Log.D_STDOUT);
            // create sorted index lists
            for (int i=0; i<nIndex; i++)
                {
                sortedIndex[i][0] = 0;
                for (int j=1; j<nIndex; j++)
                    { // for all columns
                    int k = j;  // insertion position
                    while (k>0 && distances[i][j] < distances[i][sortedIndex[i][k-1]])
                        //TODO this looks like O(N^2), but hopefully insert-sort is better than quick sort for small sizes
                        {
                        sortedIndex[i][k] = sortedIndex[i][k-1];
                        k--;
                        }
                    sortedIndex[i][k] = j;
                    }
                }

                
            int mf = nIndex;
            //state.output.println("    - Searching for minimal distances",V_DEBUG, Log.D_STDOUT);
            while (mf > archiveSize)
                {
                // search for minimal distances
                int minpos = 0;
                for (int i=1; i<nIndex; i++)
                    //we start from 1 cause the current candidate (minpos) starts at 0.
                    {
                    for (int j=1; j<mf; j++)//j is rank
                        //I'm guessing we start form 1 cause the first min is -1 for everybody.
                        {
                        double dist_i_sortedIndex_i_j = distances[i][sortedIndex[i][j]];
                        double dist_min_sortedIndex_min_j = distances[minpos][sortedIndex[minpos][j]];
                        //no reason to read these twice.
                        if (dist_i_sortedIndex_i_j<dist_min_sortedIndex_min_j)
                            {
                            minpos = i;
                            break;
                            }
                        else if (dist_i_sortedIndex_i_j > dist_min_sortedIndex_min_j)
                            break;
                        }
                    }
                // kill entries of pos (which is now minpos) from lists
                
                for (int i=0; i<nIndex; i++)
                    {
                    // Don't choose these positions again
                    distances[i][minpos] = Double.POSITIVE_INFINITY;
                    distances[minpos][i] = Double.POSITIVE_INFINITY;

                    int[] sortedIndicesForI = sortedIndex[i];//this is to cut down on range checks.
                    for (int j=1; j<mf-1; j++)
                        {
                        if (sortedIndicesForI[j]==minpos)
                            {
                            sortedIndicesForI[j] = sortedIndicesForI[j+1];
                            sortedIndicesForI[j+1] = minpos;
                            }
                        }
                    }
                oldInds[minpos] = null;
                mf--;
                } // end while ( mf > thisSubpop.archiveSize )
            //state.output.println("  Done the truncation thang...",V_DEBUG, Log.D_STDOUT);
            } // end if ( nIndex > thisSubpop.archiveSize )

        // Compress and place in newpop
        
        int nullIndex = -1;
        int newIndex = 1;
        //for (int i=0; i<oldInds.length; i++)
        for (int i=0; i<nIndex; i++)//no need to visit oldInds.len-nIndex nulls (I know nIndex>=archiveSize)
            {
            if ( oldInds[i] == null )
                {
                if ( nullIndex == -1 )
                    {
                    nullIndex = i;
                    }
                }else
                {
                newInds[newInds.length-newIndex++] = (Individual)(oldInds[i].clone());
                if ( nullIndex > -1 ) 
                    {
                    oldInds[nullIndex++] = oldInds[i];
                    oldInds[i] = null;
                    }
                }
            }
       
        // Right now the archive is in the beginning of the array; we move it
        // to the end of the array here to be consistent with ECJ's assumptions.
        for (int i=0; i < oldInds.length - archiveSize; i++) 
            {
            oldInds[oldInds.length - i - 1] = oldInds[i];
            oldInds[i] = null;
            }
      
        // NOTE: This is a key place for debugging.  The archive has been built and all the individuals
        //       have *not* yet been mutated/crossed-over.  

        }
    }

