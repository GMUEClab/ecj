/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.util;
import java.io.*;
import ec.*;

/* 
 * Code.java
 * 
 * Created: Sat Oct 23 13:45:20 1999
 * By: Sean Luke
 */

/**
 * Code provides some simple wrapper functions for encoding and decoding
 * basic data types for storage in a pseudo-Java source code strings
 * format.  This differs from just "printing"
 * them to string in that the actual precision of the object is maintained.
 * Code attempts to keep the representations as "Java-like" as possible --
 * the exceptions being primarily floats and doubles, which are encoded as
 * ints and longs.  Encoding of objects and arrays is not supported.  You'll
 * have to handle that yourself.  Strings are supported.
 *
 * <p>Everything is case-SENSITIVE.  Here's the breakdown.
 *

 <p><table>
 <tr><td><b>Type</b></td><td><b>Format</b></td></tr>
 <tr><td>boolean</td><td><tt>true</tt> or <tt>false</tt> (old style, case sensitive) or <tt>T</tt> or <tt>F</tt> (new style, case sensitive)</td></tr>
 <tr><td>byte</td><td><tt>b<i>byte</i>|</tt></td></tr>
 <tr><td>short</td><td><tt>s<i>short</i>|</tt></td></tr>
 <tr><td>int</td><td><tt>i<i>int</i>|</tt></td></tr>
 <tr><td>long</td><td><tt>l<i>long</i>|</tt></td></tr>
 <tr><td>float</td><td><tt>f<i>floatConvertedToIntForStorage</i>|<i>humanReadableFloat</i>|</tt> or (only for reading in) f|<i>humanReadableFloat</i>|</td></tr>
 <tr><td>float</td><td><tt>d<i>doubleConvertedToLongForStorage</i>|<i>humanReadableDouble</i>|</tt> or (only for reading in) d|<i>humanReadableDouble</i>|</td></tr>
 <tr><td>char</td><td>standard Java char, except that the only valid escape sequences are: \0 \t \n \b \' \" \ u <i>unicodeHex</i></td></tr>
 <tr><td>string</td><td>standard Java string with \ u ...\ u Unicode escapes, except that the only other valid escape sequences are:  \0 \t \n \b \' \" </i></td></tr>
 </table>
 *
 *
 * @author Sean Luke
 * @version 1.0 
 */

public class Code 
    {
    /** Encodes a boolean. */
    public static String encode(final boolean b) 
    // old style -- no longer used
    // { return b ? Boolean.TRUE.toString() : Boolean.FALSE.toString(); }
        { return b ? "T" : "F"; }

    /** Encodes a byte. */
    public static String encode(final byte b) 
        { return "b" + Byte.toString(b) + "|"; }

    /** Encodes a character. */
    public static String encode(final char c)
        {
        if (c >= 32 && c < 127 && c !='\\' && 
            c!= '\'') // we can safely print it
            return "'" + String.valueOf(c) + "'";
        else
            {
            // print it with an escape character
            if (c=='\b')
                return "'\\b'";
            else if (c=='\n')
                return "'\\n'";
            else if (c=='\t')
                return "'\\t'";
            else if (c=='\'')
                return "'\\''";
            else if (c=='\\')
                return "'\\\\'";
            else if (c=='\0')
                return "'\\\\0";
            else
                {
                String s = Integer.toHexString((int)c);
                // pad with 0's  -- Java's parser freaks out otherwise
                switch (s.length())
                    {
                    case 1:  s = "'\\u000" + s + "'"; break;
                    case 2:  s = "'\\u00" + s + "'"; break;
                    case 3:  s = "'\\u0" + s + "'"; break;
                    case 4:  s = "'\\u" + s + "'"; break;
                    default:
                        throw new RuntimeException("Default case should never occur");
                    }
                return s;
                }
            }
        }

    /** Encodes a short. */
    public static String encode(final short s)
        { return "s" + Short.toString(s) + "|"; }

    /** Encodes an int. */
    public static String encode(final int i)
        { return "i" + Integer.toString(i) + "|";  }
    
    /** Encodes a long. */
    public static String encode(final long l)
        { return "l" + Long.toString(l) + "|"; }

    /** Encodes a float. */
    public static String encode(final float f)
        { return "f" + Integer.toString(Float.floatToIntBits(f))+ "|" + String.valueOf(f) + "|"; }

    /** Encodes a double. */
    public static String encode(final double d)
        { return "d" + Long.toString(Double.doubleToLongBits(d))+ "|" + String.valueOf(d) + "|"; }

    /** Encodes a String. */
    public static String encode(final String s)
        {
        boolean inUnicode = false;
        int l = s.length();
        StringBuilder sb = new StringBuilder(l);
        sb.append("\"");
        for(int x=0;x<l;x++)
            {
            char c = s.charAt(x);
            if ( c >= 32 && c < 127 && c !='\\' && c!= '"') // we allow spaces
                // we can safely print it
                {
                if (inUnicode) { sb.append("\\u"); inUnicode=false; } 
                sb.append(c);
                }
            else
                {
                // print it with an escape character
                if (c=='\b')
                    {
                    if (inUnicode) { sb.append("\\u"); inUnicode=false; } 
                    sb.append("\\b");
                    }
                else if (c=='\n')
                    {
                    if (inUnicode) { sb.append("\\u"); inUnicode=false; } 
                    sb.append("\\n");
                    }
                else if (c=='\t')
                    {
                    if (inUnicode) { sb.append("\\u"); inUnicode=false; } 
                    sb.append("\\t");
                    }
                else if (c=='"')
                    {
                    if (inUnicode) { sb.append("\\u"); inUnicode=false; } 
                    sb.append("\\\"");
                    }
                else if (c=='\\')
                    {
                    if (inUnicode) { sb.append("\\u"); inUnicode=false; } 
                    sb.append("\\\\");
                    }
                else if (c=='\0')
                    {
                    if (inUnicode) { sb.append("\\u"); inUnicode=false; } 
                    sb.append("\\0");
                    }
                else
                    {
                    if (!inUnicode) {sb.append("\\u"); inUnicode=true; }
                    String ss = Integer.toHexString((int)c);
                    // pad with 0's  -- Java's parser freaks out otherwise
                    switch (ss.length())
                        {
                        case 1:  sb.append("000" + ss); break;
                        case 2:  sb.append("00" + ss); break;
                        case 3: sb.append("0" + ss); break;
                        case 4: sb.append(ss); break;
                        default:
                            throw new RuntimeException("Default case should never occur");
                        }
                    }
                }
            }
        if (inUnicode) sb.append("\\u");
        sb.append("\"");
        return sb.toString();
        }









    /** Decodes the next item out of a DecodeReturn and modifies the DecodeReturn to hold the results.  See DecodeReturn for more
        explanations about how to interpret the results. */

    public static void decode(DecodeReturn d)
        {
        String dat = d.data;
        int x = d.pos;
        int len = d.data.length();

        // look for whitespace or ( or )
        for ( ; x<len; x++ ) 
            if (!Character.isWhitespace(dat.charAt(x))) break;

        // am I at the end of my rope?
        if (x==len) { d.type = DecodeReturn.T_ERROR; d.s = "Out of tokens"; return; }

        // what type am I?
        switch(dat.charAt(x))
            {

            case 't': // boolean (true)
                if (x+3 < len && /* enough space */ 
                    dat.charAt(x+1)=='r' && 
                    dat.charAt(x+2)=='u' && 
                    dat.charAt(x+3)=='e')
                    { d.type = DecodeReturn.T_BOOLEAN; d.l = 1; d.pos = x+4; return; }
                else { d.type = DecodeReturn.T_ERROR; d.s = "Expected a (true) boolean"; return; }
                //break;

            case 'T': // boolean (true)
                { d.type = DecodeReturn.T_BOOLEAN; d.l = 1; d.pos = x+1; return; }
            //break;
                    
            case 'F': // boolean (false)
                { d.type = DecodeReturn.T_BOOLEAN; d.l = 0; d.pos = x+1; return; }
            //break;
                    



            case 'f': // float or boolean
                if (x+4 < len && /* enough space */ 
                    dat.charAt(x+1)=='a' && dat.charAt(x+2)=='l' && dat.charAt(x+3)=='s' && dat.charAt(x+4)=='e' )
                    { d.type = DecodeReturn.T_BOOLEAN; d.l = 0; d.pos = x+5; return; }
                else
                    {
                    boolean readHuman = false;
                    String sf = null;
                    int initial = x+1;
                                
                    // look for next '|'
                    for ( ; x < len; x++)
                        if (dat.charAt(x)=='|') break;
                    
                    if (x==initial) readHuman=true;
                                
                    if ( x >= len )
                        { d.type = DecodeReturn.T_ERROR; d.s = "Expected a float"; return; }

                    if (!readHuman)
                        sf = dat.substring(initial,x);
                    x++;
                    
                    // look for next '|'
                    int initial2 = x;  // x is now just past first |
                    for ( ; x < len; x++)
                        if (dat.charAt(x)=='|') break;
                    
                    if ( x >= len )
                        { d.type = DecodeReturn.T_ERROR; d.s = "Expected a float"; return; }
                    if (readHuman) 
                        sf = dat.substring(initial2,x);
                    
                    float f;
                    try 
                        { 
                        if (readHuman) f = Float.parseFloat(sf);
                        else f = Float.intBitsToFloat(Integer.parseInt(sf)); 
                        }
                    catch (NumberFormatException e)
                        { d.type = DecodeReturn.T_ERROR; d.s = "Expected a float"; return; }
                    d.type = DecodeReturn.T_FLOAT;
                    d.d = f;
                    d.pos = x+1;
                    return;
                    }








            case 'd': // double
                
                {
                boolean readHuman = false;
                String sf = null;
                int initial = x+1;
                        
                // look for next '|'
                for ( ; x < len; x++)
                    if (dat.charAt(x)=='|') break;
            
                if (x==initial) readHuman=true;
                        
                if ( x >= len )
                    { d.type = DecodeReturn.T_ERROR; d.s = "Expected a double"; return; }

                if (!readHuman)
                    sf = dat.substring(initial,x);
                x++;
            
                // look for next '|'
                int initial2 = x;  // x is now just past first |
                for ( ; x < len; x++)
                    if (dat.charAt(x)=='|') break;
            
                if ( x >= len )
                    { d.type = DecodeReturn.T_ERROR; d.s = "Expected a double"; return; }
                if (readHuman) 
                    sf = dat.substring(initial2,x);
            
                double f;
                try 
                    { 
                    if (readHuman) f = Double.parseDouble(sf);
                    else f = Double.longBitsToDouble(Long.parseLong(sf)); 
                    }
                catch (NumberFormatException e)
                    { d.type = DecodeReturn.T_ERROR; d.s = "Expected a double"; return; }
                d.type = DecodeReturn.T_DOUBLE;
                d.d = f;
                d.pos = x+1;
                return;
                }
            // break;
                        
                        






            case 'b': // byte
                
                {
                int initial = x+1;
                        
                // look for next '|'
                for ( ; x < len; x++)
                    if (dat.charAt(x)=='|') break;
                        
                if ( x >= len )
                    { d.type = DecodeReturn.T_ERROR; d.s = "Expected a byte"; return; }
                String sf = dat.substring(initial,x);
                                            
                byte f;
                try 
                    { f = Byte.parseByte(sf); }
                catch (NumberFormatException e)
                    { d.type = DecodeReturn.T_ERROR; d.s = "Expected a byte"; return; }
                d.type = DecodeReturn.T_BYTE;
                d.l = f;
                d.pos = x+1;
                return;
                }
            // break;






            case 's': // short
                
                {
                int initial = x+1;
                        
                // look for next '|'
                for ( ; x < len; x++)
                    if (dat.charAt(x)=='|') break;
                        
                if ( x >= len )
                    { d.type = DecodeReturn.T_ERROR; d.s = "Expected a short"; return; }
                String sf = dat.substring(initial,x);
                                            
                short f;
                try 
                    { f = Short.parseShort(sf); }
                catch (NumberFormatException e)
                    { d.type = DecodeReturn.T_ERROR; d.s = "Expected a short"; return; }
                d.type = DecodeReturn.T_SHORT;
                d.l = f;
                d.pos = x+1;
                return;
                }
            // break;




            case 'i': // int
                
                {
                int initial = x+1;
                        
                // look for next '|'
                for ( ; x < len; x++)
                    if (dat.charAt(x)=='|') break;
                        
                if ( x >= len )
                    { d.type = DecodeReturn.T_ERROR; d.s = "Expected an int"; return; }
                String sf = dat.substring(initial,x);
                                            
                int f;
                try 
                    { f = Integer.parseInt(sf); }
                catch (NumberFormatException e)
                    { d.type = DecodeReturn.T_ERROR; d.s = "Expected an int"; return; }
                d.type = DecodeReturn.T_INT;
                d.l = f;
                d.pos = x+1;
                return;
                }
            // break;






            case 'l': // long
                
                {
                int initial = x+1;
                        
                // look for next '|'
                for ( ; x < len; x++)
                    if (dat.charAt(x)=='|') break;
                        
                if ( x >= len )
                    { d.type = DecodeReturn.T_ERROR; d.s = "Expected a long"; return; }
                String sf = dat.substring(initial,x);
                                            
                long f;
                try 
                    { f = Long.parseLong(sf); }
                catch (NumberFormatException e)
                    { d.type = DecodeReturn.T_ERROR; d.s = "Expected a long"; return; }
                d.type = DecodeReturn.T_LONG;
                d.l = f;
                d.pos = x+1;
                return;
                }
            // break;






            case '"':  // string
                {
                StringBuilder sb = new StringBuilder();
                boolean inUnicode = false;
            
                x++;
                for ( ; x < len; x++)
                    {
                    char c = dat.charAt(x);
                    if (c=='"') 
                        {
                        // done with the string
                        if (inUnicode)  // uh oh
                            { d.type = DecodeReturn.T_ERROR; d.s = "Forgot to terminate Unicode with a '\\u' in the string"; return; }
                        d.type = DecodeReturn.T_STRING; 
                        d.s = sb.toString(); 
                        d.pos = x+1;
                        return;
                        }
                    else if (c=='\\')  // escape
                        {
                        x++;
                        if ( x >= len )
                            { d.type = DecodeReturn.T_ERROR; d.s = "Unterminated String"; return; }
                        if (dat.charAt(x)!='u' && inUnicode)
                            { d.type = DecodeReturn.T_ERROR; d.s = "Escape character in Unicode sequence"; return; }

                        switch (dat.charAt(x))
                            {
                            case 'u': inUnicode = !inUnicode; break;
                            case 'b': sb.append('\b'); break;
                            case 'n': sb.append('\n'); break;
                            case '"': sb.append('"'); break;
                            case '\'': sb.append('\''); break;
                            case 't': sb.append('\t'); break;
                            case '\\': sb.append('\\'); break;
                            case '0': sb.append('\0'); break;
                            default: 
                                { d.type = DecodeReturn.T_ERROR; d.s = "Bad escape char in String"; return; }
                            }
                        }
                    else if (inUnicode)
                        {
                        if ( x + 3 >= len )
                            { d.type = DecodeReturn.T_ERROR; d.s = "Unterminated String"; return; }
                        try
                            {
                            sb.append((char)(Integer.decode("0x" + c +
                                        dat.charAt(x+1) +
                                        dat.charAt(x+2) +
                                        dat.charAt(x+3)).intValue()));;
                            x+=3;
                            }
                        catch (NumberFormatException e)
                            { d.type = DecodeReturn.T_ERROR; d.s = "Bad Unicode in String"; return; }
                        }
                    else sb.append(c);
                    }
                d.type = DecodeReturn.T_ERROR; d.s = "Unterminated String"; return;
                }
            //break;














            case '\'': // char
                {
                x++;
                if ( x >= len )
                    { d.type = DecodeReturn.T_ERROR; d.s = "Unterminated char"; return; }
                char c = dat.charAt(x);
                if (c=='\\')
                    {
                    x++;
                    if (x>=len)
                        { d.type = DecodeReturn.T_ERROR; d.s = "Unterminated char"; return; }
                    switch (dat.charAt(x))
                        {
                        case 'u':
                            if ( x + 4 >= len )
                                { d.type = DecodeReturn.T_ERROR; d.s = "Unterminated char"; return; }
                            try
                                {
                                c = (char)(Integer.decode("0x" + 
                                        dat.charAt(x+1) +
                                        dat.charAt(x+2) +
                                        dat.charAt(x+3) +
                                        dat.charAt(x+4)).intValue());
                                }
                            catch (NumberFormatException e)
                                { d.type = DecodeReturn.T_ERROR; d.s = "Bad Unicode in char"; return; }
                            x+=5;
                            break;
                    
                        case 'b': c = '\b'; x++; break;
                        case 'n': c = '\n'; x++; break;
                        case '"': c = '"'; x++; break;
                        case '\'': c = '\''; x++; break;
                        case 't':  c = '\t'; x++; break;
                        case '\\': c = '\\'; x++; break;
                        case '0': c = '\0';  x++; break;
                        default: 
                            { d.type = DecodeReturn.T_ERROR; d.s = "Bad escape char in char"; return; }
                        }
                    if (dat.charAt(x)!='\'')
                        { d.type = DecodeReturn.T_ERROR; d.s = "Bad char"; return; }
                    d.type = DecodeReturn.T_CHAR;
                    d.l = c;
                    d.pos = x+1;
                    return;
                    }
                else
                    {
                    x++;
                    if ( x >= len )
                        { d.type = DecodeReturn.T_ERROR; d.s = "Unterminated char"; return; }
                    if (dat.charAt(x)!='\'')
                        { d.type = DecodeReturn.T_ERROR; d.s = "Bad char"; return; }
                    d.type = DecodeReturn.T_CHAR;
                    d.l = c;
                    d.pos = x + 1;
                    return;
                    }


                }
            //break;






            
            default: 
                d.type = DecodeReturn.T_ERROR; d.s = "Unknown token"; return; 
                // break;
            }
        }

    /** Finds the next nonblank line, then trims the line and checks the preamble.  Returns a DecodeReturn on the line if successful, else posts a fatal error.
        Sets the DecodeReturn's line number.  The DecodeReturn has not yet been decoded.  You'll need to do that with Code.decode(...) */
    public static DecodeReturn checkPreamble(String preamble, final EvolutionState state, 
        final LineNumberReader reader)
        {
        int linenumber = 0;  // throw it away later
        try
            {
            // get non-blank line
            String s = "";
            while(s != null && s.trim().equals(""))
                {
                linenumber = reader.getLineNumber();
                s = reader.readLine();
                }

            // check the preamble
            if (s==null || !(s = s.trim()).startsWith(preamble)) // uh oh
                state.output.fatal("Line " + linenumber + 
                    " has a bad preamble.Expected '" + preamble + "'\n-->" + s);
            DecodeReturn d = new DecodeReturn(s, preamble.length());
            d.lineNumber = linenumber;
            return d;
            }
        catch (IOException e)
            {
            state.output.fatal("On line " + linenumber + " an IO error occurred:\n\n" + e);
            return null;  // never happens
            }
        }

    /** Finds the next nonblank line, skips past an expected preamble, and reads in a string if there is one, and returns it.
        Generates an error otherwise. */
    public static String readStringWithPreamble(String preamble, final EvolutionState state, 
        final LineNumberReader reader)
        {
        DecodeReturn d = checkPreamble(preamble, state, reader);
        Code.decode(d);
        if (d.type!=DecodeReturn.T_STRING)
            state.output.fatal("Line " + d.lineNumber + 
                " has no string after preamble '" + preamble + "'\n-->" + d.data);
        return (String)(d.s);
        }

    /** Finds the next nonblank line, skips past an expected preamble, and reads in a character if there is one, and returns it.
        Generates an error otherwise. */
    public static char readCharacterWithPreamble(String preamble, final EvolutionState state, 
        final LineNumberReader reader)
        {
        DecodeReturn d = checkPreamble(preamble, state, reader);
        Code.decode(d);
        if (d.type!=DecodeReturn.T_CHAR)
            state.output.fatal("Line " + d.lineNumber + 
                " has no character after preamble '" + preamble + "'\n-->" + d.data);
        return (char)(d.l);
        }

    /** Finds the next nonblank line, skips past an expected preamble, and reads in a byte if there is one, and returns it.
        Generates an error otherwise. */
    public static byte readByteWithPreamble(String preamble, final EvolutionState state, 
        final LineNumberReader reader)
        {
        DecodeReturn d = checkPreamble(preamble, state, reader);
        Code.decode(d);
        if (d.type!=DecodeReturn.T_BYTE)
            state.output.fatal("Line " + d.lineNumber + 
                " has no byte after preamble '" + preamble + "'\n-->" + d.data);
        return (byte)(d.l);
        }

    /** Finds the next nonblank line, skips past an expected preamble, and reads in a short if there is one, and returns it.
        Generates an error otherwise. */
    public static short readShortWithPreamble(String preamble, final EvolutionState state, 
        final LineNumberReader reader)
        {
        DecodeReturn d = checkPreamble(preamble, state, reader);
        Code.decode(d);
        if (d.type!=DecodeReturn.T_SHORT)
            state.output.fatal("Line " + d.lineNumber + 
                " has no short after preamble '" + preamble + "'\n-->" + d.data);
        return (short)(d.l);
        }

    /** Finds the next nonblank line, skips past an expected preamble, and reads in a long if there is one, and returns it.
        Generates an error otherwise. */
    public static long readLongWithPreamble(String preamble, final EvolutionState state, 
        final LineNumberReader reader)
        {
        DecodeReturn d = checkPreamble(preamble, state, reader);
        Code.decode(d);
        if (d.type!=DecodeReturn.T_LONG)
            state.output.fatal("Line " + d.lineNumber + 
                " has no long after preamble '" + preamble + "'\n-->" + d.data);
        return (long)(d.l);
        }
        
    /** Finds the next nonblank line, skips past an expected preamble, and reads in an integer if there is one, and returns it.
        Generates an error otherwise. */
    public static int readIntegerWithPreamble(String preamble, final EvolutionState state, 
        final LineNumberReader reader)
        {
        DecodeReturn d = checkPreamble(preamble, state, reader);
        Code.decode(d);
        if (d.type!=DecodeReturn.T_INT)
            state.output.fatal("Line " + d.lineNumber + 
                " has no integer after preamble '" + preamble + "'\n-->" + d.data);
        return (int)(d.l);
        }


    /** Finds the next nonblank line, skips past an expected preamble, and reads in a float if there is one, and returns it. 
        Generates an error otherwise. */
    public static float readFloatWithPreamble(String preamble, final EvolutionState state, 
        final LineNumberReader reader)
        {
        DecodeReturn d = checkPreamble(preamble, state, reader);
        Code.decode(d);
        if (d.type!=DecodeReturn.T_FLOAT)
            state.output.fatal("Line " + d.lineNumber + 
                " has no floating point number after preamble '" + preamble + "'\n-->" + d.data);
        return (float)(d.d);
        }

    /** Finds the next nonblank line, skips past an expected preamble, and reads in a double if there is one, and returns it. 
        Generates an error otherwise. */
    public static double readDoubleWithPreamble(String preamble, final EvolutionState state, 
        final LineNumberReader reader)
        {
        DecodeReturn d = checkPreamble(preamble, state, reader);
        Code.decode(d);
        if (d.type!=DecodeReturn.T_DOUBLE)
            state.output.fatal("Line " + d.lineNumber + 
                " has no double floating point number after preamble '" + preamble + "'. -->" + d.data);
        return d.d;
        }

    /** Finds the next nonblank line, skips past an expected preamble, and reads in a boolean value ("true" or "false") if there is one, and returns it. 
        Generates an error otherwise. */
    public static boolean readBooleanWithPreamble(String preamble, final EvolutionState state, 
        final LineNumberReader reader)
        {
        DecodeReturn d = checkPreamble(preamble, state, reader);
        Code.decode(d);
        if (d.type!=DecodeReturn.T_BOOLEAN)
            state.output.fatal("Line " + d.lineNumber + 
                " has no boolean value ('true' or 'false') after preamble '" + preamble + "'\n-->" + d.data);
        return (d.l != 0);
        }

    }


/*
  (BeanShell testing for decoding)

  s = "      true false s-12| i232342|b22|f234123|3234.1| d234111231|4342.31|"


  s = "\"Hello\" true false s-12| i232342|b22|f234123|3234.1| d234111231|4342.31| ' ' '\\'' '\\n' \"Hello\\u0000\\uWorld\""
  c = new ec.util.Code();
  r = new ec.util.DecodeReturn(s);

  c.decode(r);
  System.out.println(r.type);
  System.out.println(r.l);

  System.out.println(r.d);
  System.out.println(r.s);

*/
