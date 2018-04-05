/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.spatial;

import ec.*;

/* 
 * Space.java
 * 
 * By: Liviu Panait
 */


/**
 *      In a spatially-embedded EA, the subpopulations of individuals are assumed to be
 *      spatially distributed in some sort of space, be it one-dimmensional, two-
 *      dimmensional, or whatever else.  The space may or may not be toroidal (although
 *      it usually is).  Each location in the space has a set of neighboring locations.
 *      Thus, each individual has an index in the subpopulation, and also a location in
 *      the space.
 *
 *      This public interface provides a method to obtain the indexes of the neighbors
 *      of a location.
 *
 *
 *
 *
 * @author Liviu Panait
 * @version 1.0 
 */

public interface Space
    {
    /*
      The Space should provide a bijectional mapping from locations in space
      to indexes in the subpopulation.  Returns -1 if error occured.
    */
    // public int locationToIndex( final Object location );

    /*
      The Space provide a bijectional mapping from indexes in the subpopulation
      to locations in space.  Returns null if error occured.
    */
    // public Object indexToLocation( final int index);

    /**
       Input: a threadnumber (either for evaluation or for breeding), and an index in a subpopulation
       (the index in the subpopulation is, of course, associated with a location in the space)
       Functionality: stores the index and the threadnumber for further accesses to the getIndexRandomNeighbor
       method.  All such accesses from the specific thread will use the exact same index, until
       this function is called again to change the index.
    */
    public void setIndex( int threadnum, int index );

    /**
       Functionality: retrieve the index for a specific threanum.
       Returns -1 if any error is encountered.
    */
    public int getIndex( int threadnum );

    /**
       Input: the maximum distance for neighbors.
       Functionality: computes the location in space associated with the index, then
       computes the neighbors of that location that are within the specified distance.
       Output: returns one random neighbor within that distance (possibly including self)
    */
    public int getIndexRandomNeighbor( final EvolutionState state, int threadnum, int distance );

    }
