/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.util;

/* 
 * DecodeReturn.java
 * 
 * Created: Sat Oct 23 15:23:39 1999
 * By: Sean Luke
 */

/**
 * DecodeReturn is used by Code to provide varied information returned
 * when decoding. 
 * You start the decoding process by initializing the DecodeReturn
 * on a string you want to decode items out of.  Then you repeatedly
 * pass the DecodeReturn to Code.decode(...), and each time the
 * DecodeReturn will contain information about the next token, namely,
 * its type, the data of the token (depending on type, this
 * can be in one of three slots, d, l, or s), and the start position
 * for reading the next token.
 *
 * <p>In case of an error, type is set to DecodeReturn.T_ERROR,
 * pos is kept at the token where the error occured, and
 * s is set to an error message.
 *
 * @author Sean Luke
 * @version 1.0 
 */

public class DecodeReturn 
    {
    /** The actual error is stored in the String slot */
    public static final byte T_ERROR = -1;

    public static final byte T_BOOLEAN = 0;
    public static final byte T_BYTE = 1;
    public static final byte T_CHAR = 2;
    /** Same as T_CHAR */
    public static final byte T_CHARACTER = 2;
    public static final byte T_SHORT = 3;
    public static final byte T_INT = 4;
    /** Same as T_INT */
    public static final byte T_INTEGER = 4;
    public static final byte T_LONG = 5;
    public static final byte T_FLOAT = 6;
    public static final byte T_DOUBLE = 7;
    public static final byte T_STRING = 8;

    /** The Line number, if it has been posted. */
    public int lineNumber = 0;
    
    /** The DecodeReturn type */
    public byte type;

    /** The DecodeReturn string that's read from. */
    public String data;

    /** The DecodeReturn new position in the string.  Set this yourself.
        New values get set here automatically. */
    public int pos;

    /** Stores booleans (0=false), bytes, chars, shorts, ints, longs */
    public long l;

    /** Stores floats, doubles */
    public double d;
    
    /** Stores strings, error messages */
    public String s;

    /** Use this to make a new DecodeReturn starting at position 0 */
    public DecodeReturn(String _data) { data = _data; pos = 0; }

    /** Use this to make a new DecodeReturn starting at some position */
    public DecodeReturn(String _data, int _pos) { data = _data; pos = _pos; }

    /** Sets the DecodeReturn to begin scanning at _pos, which should be valid. */
    public DecodeReturn scanAt(int _pos) 
        {
        pos = Math.min(Math.max(_pos,0),data.length());
        return this;
        }

    /** Use this to reuse your DecodeReturn for another string */
    public DecodeReturn reset(final String _data) { data = _data; pos = 0; return this; }
    
    /** Use this to reuse your DecodeReturn for another string */
    public DecodeReturn reset(final String _data, int _pos) { data = _data; pos = _pos; return this; }
    }
