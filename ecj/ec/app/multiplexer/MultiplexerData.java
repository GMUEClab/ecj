/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.multiplexer;
import java.util.*;
import ec.gp.*;

/* 
 * MultiplexerData.java
 * 
 * Created: Wed Nov  3 18:32:13 1999
 * By: Sean Luke
 */

/**
 * This is ugly and complicated because it needs to hold a variety
 * of different-length bitstrings, including temporary ones held
 * while computing subtrees.
 *
 * @author Sean Luke
 * @version 1.0 
 */

public class MultiplexerData extends GPData
    {
    /** A stack of available long arrays for popDat11/pushDat11 */
    public Stack tmp;

    /** The number of Dn in Multiplexer-3 */
    public static final byte STATUS_3 = 1;
    /** The number of Dn in Multiplexer-6 */
    public static final byte STATUS_6 = 2;
    /** The number of Dn in Multiplexer-11 */
    public static final byte STATUS_11 = 3;
    /** The length of an atomic data element in Multiplexer-3 (a byte) */
    public static final int MULTI_3_BITLENGTH = 8;
    /** The length of an atomic data element in Multiplexer-6 (a long) */
    public static final int MULTI_6_BITLENGTH = 64;
    /** The length of an atomic data element in Multiplexer-11 (a long) */
    public static final int MULTI_11_BITLENGTH = 64;
    /** The number of atomic elements in Multiplexer-11 comprising one string (32) */
    public static final int MULTI_11_NUM_BITSTRINGS = 32;

    /** An array of 32 longs for Multiplexer-11 data */
    public long[] dat_11;
    /** A long for Multiplexer-6 data */
    public long dat_6;
    /** A byte for Multiplexer-3 data */
    public byte dat_3;
    /** A byte indicating the number of Dn in this problem */
    public byte status;

    /** Pops a dat_11 off of the stack; if the stack is empty, creates a new dat_11 and returns that. */
    public long[] popDat11()
        {
        if (tmp.empty())
            return new long[MULTI_11_NUM_BITSTRINGS];
        else return (long[])(tmp.pop());
        }

    /** Pushes a dat_11 onto the stack */
    public void pushDat11(long[] l)
        {
        tmp.push(l);
        }

    public MultiplexerData() 
        { 
        dat_11 = new long[MULTI_11_NUM_BITSTRINGS]; 
        tmp = new Stack();
        }

    public Object clone()
        {
        MultiplexerData dat = (MultiplexerData)(super.clone());
        dat.dat_11 = new long[MULTI_11_NUM_BITSTRINGS];
        System.arraycopy(dat_11,0,dat.dat_11,0,MULTI_11_NUM_BITSTRINGS);
        dat.tmp = new Stack();
        return dat;
        }

    public GPData copyTo(final GPData gpd)
        { 
        MultiplexerData md = ((MultiplexerData)gpd);
        for(int x=0;x<MULTI_11_NUM_BITSTRINGS;x++)
            md.dat_11[x] = dat_11[x];
        md.dat_6 = dat_6;
        md.status = status;
        return gpd;
        }
    }
