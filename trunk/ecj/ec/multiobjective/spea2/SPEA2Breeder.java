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

public class SPEA2Breeder extends Breeder
    {

    /** Debug messages for this object */
    public static final int V_DEBUG = 5;

    public void setup(final EvolutionState state, final Parameter base) 
        {
        }

    /** A simple breeder that doesn't attempt to do any cross-
        population breeding.  Basically it applies pipelines,
        one per thread, to various subchunks of a new population. */
    public Population breedPopulation(EvolutionState state) 
        
        {
        int numinds[][] = 
            new int[state.breedthreads][state.population.subpops.length];
        int from[][] = 
            new int[state.breedthreads][state.population.subpops.length];

        Population newpop = (Population) state.population.emptyClone();

        // load top individuals (elites) into the archive (top of newpop)
        // TODO: Consider if this should be optimized since it is an 
        //       expensive task but not designed to work in a multithreaded
        //       fashion.
        loadArchive(state, newpop);

        for(int y=0;y<state.breedthreads;y++)
            {
            for(int x=0;x<state.population.subpops.length;x++)
                {
                // Subpopulation we are considering
                SPEA2Subpopulation thisSubpop =
                    (SPEA2Subpopulation)state.population.subpops[x];

                // the number of individuals we need to breed
                int length = state.population.subpops[x].individuals.length 
                    - thisSubpop.archiveSize;

                // the size of each breeding chunk except the last one
                int firstBreedChunkSizes = length/state.breedthreads;

                // the size of the last breeding chunk
                int lastBreedChunkSize = firstBreedChunkSizes + length - 
                    firstBreedChunkSizes * (state.breedthreads);

                // figure numinds
                if (y < state.breedthreads-1) // not the last one
                    numinds[y][x] = firstBreedChunkSizes;
                else // the last one
                    numinds[y][x] = lastBreedChunkSize;

                // figure from
                from[y][x] = (firstBreedChunkSizes * y);
                }
            }

        if (state.breedthreads==1)
            {
            breedPopChunk(newpop,state,numinds[0],from[0],0);
            }
        else
            {
            Thread[] t = new Thread[state.breedthreads];

            // start up the threads
            for(int y=0;y<state.breedthreads;y++)
                {
                SPEA2BreederThread r = new SPEA2BreederThread();
                r.threadnum = y;
                r.newpop = newpop;
                r.numinds = numinds[y];
                r.from = from[y];
                r.me = this;
                r.state = state;
                t[y] = new Thread(r);
                t[y].start();
                }

            // gather the threads
            for(int y=0;y<state.breedthreads;y++) try
                {
                t[y].join();
                }
            catch(InterruptedException e)
                {
                state.output.fatal("Whoa! The main breeding thread got interrupted!  Dying...");
                }
            }
        return newpop;
        }


    /** A private helper function for breedPopulation which breeds a chunk
        of individuals in a subpopulation for a given thread.
        Although this method is declared
        public (for the benefit of a private helper class in this file),
        you should not call it. */

    public void breedPopChunk(Population newpop, 
                              EvolutionState state,
                              int[] numinds, 
                              int[] from, 
                              int threadnum) 
        {
        for(int subpop=0;subpop<newpop.subpops.length;subpop++)
            {
            BreedingPipeline bp = (BreedingPipeline)newpop.subpops[subpop].
                species.pipe_prototype.clone();

            // check to make sure that the breeding pipeline produces
            // the right kind of individuals.  Don't want a mistake there! :-)
            int x;
            if (!bp.produces(state,newpop,subpop,threadnum))
                state.output.error("The Breeding Pipeline of subpopulation " + subpop + 
                                   " does not produce individuals of the expected species " + 
                                   newpop.subpops[subpop].species.getClass().getName() + 
                                   " or fitness " + newpop.subpops[subpop].species.f_prototype );
            bp.prepareToProduce(state,subpop,threadnum);
            state.output.exitIfErrors();

            // start breedin'!
            x=from[subpop];
            int upperbound = from[subpop]+numinds[subpop];
            while(x<upperbound)
                {
                x += bp.produce(1,upperbound-x,x,subpop,
                                newpop.subpops[subpop].individuals,
                                state,threadnum);
                }
            if (x>upperbound) // uh oh!  Someone blew it!
                {
                state.output.fatal("Whoa!  A breeding pipeline overwrote the space of " +
                                   "another pipeline in subpopulation " + subpop + 
                                   ".  You need to check your breeding pipeline code (in produce() ).");
                }
            }
        }



    /** A private helper function for breedPopulation which loads the
        archive (top end of indivudal array) with the SPEA2 elites. */
    public void loadArchive(EvolutionState state, Population newpop)
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
    private void QuickSort(Individual a[], int l, int r)
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
            QuickSort(a,l,j);
            QuickSort(a,i+1,r);
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
        QuickSort(a, 0, a.length - 1);
        InsertionSort(a,0,a.length-1);
        }


    }


/** A private helper class for implementing multithreaded breeding */
class SPEA2BreederThread implements Runnable
    {
    Population newpop;
    public int[] numinds;
    public int[] from;
    public SPEA2Breeder me;
    public EvolutionState state;
    public int threadnum;
    public void run()
        {
        me.breedPopChunk(newpop,state,numinds,from,threadnum);
        }
    }
