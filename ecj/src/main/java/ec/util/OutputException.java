/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.util;

/*
 * OutputException.java
 * Created: Sat Aug  7 12:06:49 1999
 */


/**
 *
 * Thrown whenever a problem occurs when attempting to output to a Log.
 *
 * @author Sean Luke
 * @version 1.0
 */

public class OutputException extends RuntimeException
    {
    public OutputException(String s) { super(s); }
    }
