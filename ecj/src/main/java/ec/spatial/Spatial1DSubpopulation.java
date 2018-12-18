/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.spatial;

import ec.*;
import ec.util.*;

/* 
 * Spatial1DSubpopulation.java
 * 
 * By: Liviu Panait
 */

/**
 *      A Spatial1DSubpopulation is an EC subpopulation that is additionally embedded into
 *      a one-dimmensional space.
 *      In a spatially-embedded EA, the subpopulations of individuals are assumed to be
 *      spatially distributed in some sort of space, be it one-dimmensional, two-
 *      dimmensional, or whatever else.  The space may or may not be toroidal (although
 *      it usually is).  Each location in the space has a set of neighboring locations.
 *      Thus, each individual has an index in the subpopulation, and also a location in
 *      the space.
 *
 *      <p>This public interface provides a method to obtain the indexes of the neighbors
 *      of a location.
 *
 *      <P>This Subpopulation does not include toroidalness in writing out to streams.
 *
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><tt>toroidal</tt><br>
 <font size=-1>true (default) or false</font></td>
 <td valign=top>(Is this space toroidal?)</td></tr>

 *
 *
 * @author Liviu Panait
 * @version 1.0 
 */
public class Spatial1DSubpopulation extends Subpopulation implements Space
    {
    /**
       This parameter stipulates whether the world is toroidal or not.
       If missing, its default value is true.
    */
    public static final String P_TOROIDAL = "toroidal";
    public boolean toroidal;

    /**
       Read additional parameters for the spatially-embedded subpopulation.
    */
    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);

        // by default, the space is toroidal
        toroidal = state.parameters.getBoolean(base.push(P_TOROIDAL),null,true);

        }

    /*
      1D mapping is identity
    */
    /*
      public int locationToIndex( final Object location )
      {
      if( location instanceof Integer )
      return ((Integer)location).intValue();
      return -1;
      }
    */

    /*
      1D mapping is identity
    */
    /*
      public Object indexToLocation( final int index)
      {
      return new Integer(index);
      }
    */

    public void setIndex( int threadnum, int index )
        {
        if( indexes == null )
            indexes = new int[threadnum+1];
        if( threadnum >= indexes.length )
            {
            int currentSize = indexes.length;
            int[] temp = new int[threadnum*2+1];
            System.arraycopy(indexes,0,temp,0,currentSize);
            indexes = temp;
            }
        indexes[threadnum] = index;
        }

    public int getIndex( int threadnum )
        {
        if( indexes == null || threadnum > indexes.length )
            return -1;
        else
            return indexes[threadnum];
        }

    // indexed by threadnum
    int[] indexes;

    /**
       Returns a the index of a random neighbor.
    */
    public int getIndexRandomNeighbor( final EvolutionState state, int threadnum, int distance )
        {
        int index = indexes[threadnum];

        int size = individuals.size();
        if( size == 0 )
            return index;
        if( toroidal )
            {
            int max = (2*distance+1>size) ? size : (2*distance+1);
            int rand = state.random[threadnum].nextInt(max);
            int val= (index+rand-distance);
            if (val >= 0 && val < size) return val;
            val = val % size;
            if (val >= 0) return val;
            else return val + size;
            }
        else
            {
            int min = (index-distance<0) ? 0 : (index-distance);
            int max = (index+distance>=size) ? size : (index+distance);
            int val = min + state.random[threadnum].nextInt(max-min+1);
            return val;
            }
        }
    }
