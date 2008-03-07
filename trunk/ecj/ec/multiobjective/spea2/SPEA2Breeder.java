/*
  Copyright 2006 by Robert Hubley
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.multiobjective.spea2;
import ec.Individual;
import ec.BreedingPipeline;
import ec.Breeder;
import ec.EvolutionState;
import ec.Population;
import ec.util.Parameter;
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
 *
 * @author Robert Hubley (based on Breeder.java by Sean Luke)
 * @version 1.0 
 */

public class SPEA2Breeder extends SimpleBreeder
    {
    /** Debug messages for this object */
    //public static final int V_DEBUG = 5;

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
	return state.population.subpops[subpopulation].individuals.length 
	    - ((SPEA2Subpopulation)state.population.subpops[subpopulation]).archiveSize;

	}

    // overrides the loadElites function to load the archive the way we'd like to do it.
    public void loadElites(EvolutionState state, Population newpop)
        {
        //state.output.println("Loading the SPEA2 archive...",V_DEBUG, Log.D_STDOUT);
        /** A large number used for sorting */
        double MAXDOUBLE = 99999999;

        for(int sub=0;sub<state.population.subpops.length;sub++) 
            {
            /** The new population after we are done picking the elites */
            Individual[] newInds = newpop.subpops[sub].individuals;
            /** The old population from which to pick elites  */
            Individual[] oldInds = state.population.subpops[sub].individuals;
            /** An array of distances between elites */
            double[][] distances = new double[oldInds.length][oldInds.length];
            /** An array of indexes to individuals sorted by distances */
            int[][] sortedIndex = new int[oldInds.length][oldInds.length];

            // A short reference to the current subpopluation being considered
            SPEA2Subpopulation thisSubpop =
                (SPEA2Subpopulation)state.population.subpops[sub];

            // Sort the old guys
            sort(oldInds);

            // Null out non-candidates and count
            int nIndex = 1;
            for(int x=0;x<oldInds.length;x++)
                {
                if ( nIndex > thisSubpop.archiveSize && 
                     ((SPEA2MultiObjectiveFitness)oldInds[x].fitness).SPEA2Fitness >= 1 )
                    {
                    oldInds[x] = null;
                    }else {
                        nIndex++;
                        }
                }
            nIndex--;

            // Check to see if we need to truncate the archive
            if ( nIndex > thisSubpop.archiveSize ) 
                {
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
                            {
                            sortedIndex[i][k] = sortedIndex[i][k-1];
                            k--;
                            }
                        sortedIndex[i][k] = j;
                        }
                    }

                int mf = nIndex;
                //state.output.println("    - Searching for minimal distances",V_DEBUG, Log.D_STDOUT);
                while (mf > thisSubpop.archiveSize)
                    {
                    // search for minimal distances
                    int minpos = 0;
                    for (int i=1; i<nIndex; i++)
                        {
                        for (int j=1; j<mf; j++)
                            {
                            if (distances[i][sortedIndex[i][j]] < 
                                distances[minpos][sortedIndex[minpos][j]])
                                {
                                minpos = i;
                                break;
                                }
                            else if (distances[i][sortedIndex[i][j]] > 
                                     distances[minpos][sortedIndex[minpos][j]])
                                break;
                            }
                        }
                    // kill entries of pos (which is now minpos) from lists
                    for (int i=0; i<nIndex; i++)
                        {
                        // Don't choose these positions again
                        distances[i][minpos] = MAXDOUBLE;
                        distances[minpos][i] = MAXDOUBLE;
                        for (int j=1; j<mf-1; j++)
                            {
                            if (sortedIndex[i][j]==minpos)
                                {
                                sortedIndex[i][j] = sortedIndex[i][j+1];
                                sortedIndex[i][j+1] = minpos;
                                }
                            }
                        }
                    oldInds[minpos] = null;
                    mf--;
                    } // end while ( mf > thisSubpop.archiveSize )
                //state.output.println("  Done the truncation thang...",V_DEBUG, Log.D_STDOUT);
                } // end if ( nIndex > thisSubpop.archiveSize )

            // Compress and place in newpop
            // NOTE: The archive is maintained at the top block of the individuals
            //       vector.  Immediately prior to selection we copy the archive to
            //       the next generation (top block) and then pass along the old
            //       individuals (archive only) as the bottom block of the oldInds
            //       vector.  The SPEA2TournamentSelection depends on the individuals
            //       being between 0-archiveSize in this vector!
            int nullIndex = -1;
            int newIndex = 1;
            for (int i=0; i<oldInds.length; i++)
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
            // NOTE: This is a key place for debugging.  The archive has been built and all the individuals
            //       have *not* yet been mutated/crossed-over.  

            } // next subpopulation
        }




    // NOTE FROM SEAN: 
    // I have not yet deleted this and replaced it with Arrays.sort or with
    // java.ec.util.Quicksort.sort because I'm not sure what direction the
    // fitness sorting is supposed to go.  > or < ?


    /** Private quicksort function */
    private void quickSort(Individual a[], int l, int r)
        {
        int M = 4;
        int i;
        int j;
        Individual v;

        if ((r-l)>M)
            {
            i = (r+l)/2;
            if (((SPEA2MultiObjectiveFitness)a[l].fitness).SPEA2Fitness >
                ((SPEA2MultiObjectiveFitness)a[i].fitness).SPEA2Fitness) 
                {
                swap(a,l,i);
                }
            if (((SPEA2MultiObjectiveFitness)a[l].fitness).SPEA2Fitness >
                ((SPEA2MultiObjectiveFitness)a[r].fitness).SPEA2Fitness) 
                {
                swap(a,l,r);
                }
            if (((SPEA2MultiObjectiveFitness)a[i].fitness).SPEA2Fitness >
                ((SPEA2MultiObjectiveFitness)a[r].fitness).SPEA2Fitness) 
                {
                swap(a,i,r);
                }
            j = r-1;
            swap(a,i,j);
            i = l;
            v = a[j];
            for(;;)
                {
                while(((SPEA2MultiObjectiveFitness)a[++i].fitness).SPEA2Fitness <
                      ((SPEA2MultiObjectiveFitness)v.fitness).SPEA2Fitness);
                while(((SPEA2MultiObjectiveFitness)a[--j].fitness).SPEA2Fitness >
                      ((SPEA2MultiObjectiveFitness)v.fitness).SPEA2Fitness);
                if (j<i) break;
                swap (a,i,j);
                }
            swap(a,i,r-1);
            quickSort(a,l,j);
            quickSort(a,i+1,r);
            }
        }

    /** Private helper function used by quicksort */
    private void swap(Individual a[], int i, int j)
        {
        Individual T;
        T = a[i]; 
        a[i] = a[j];
        a[j] = T;
        }

    /** Private helper function used by quicksort */
    private void InsertionSort(Individual a[], int lo0, int hi0)
        {
        int i;
        int j;
        Individual v;

        for (i=lo0+1;i<=hi0;i++)
            {
            v = a[i];
            j=i;
            while ((j>lo0) && ((SPEA2MultiObjectiveFitness)a[j-1].fitness).SPEA2Fitness >
                   ((SPEA2MultiObjectiveFitness)v.fitness).SPEA2Fitness)
                {
                a[j] = a[j-1];
                j--;
                }
            a[j] = v;
            }
        }

    /** Private helper function which calls quicksort */
    public void sort(Individual a[])
        {
        quickSort(a, 0, a.length - 1);
        InsertionSort(a,0,a.length-1);
        }
    }

