/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.util;
import java.io.IOException;
import java.io.Serializable;

/* 
 * LogRestarter.java
 * 
 * Created: Wed Aug 11 14:55:03 1999
 * By: Sean Luke
 */

/**
 * A LogRestarter is an abstract superclass of objects which are
 * capable of restarting logs after a computer failure.   
 * LogRestarters subclasses are generally used
 * internally in Logs only; you shouldn't need to deal with them.
 *
 * @author Sean Luke
 * @version 1.0 
 */

public abstract class LogRestarter implements Serializable
    {
    /* recreate the writer for, and properly reopen a log
       upon a system restart from a checkpoint */
    public abstract Log restart(Log l) throws IOException;

    /* close an existing log file and reopen it (non-appending),
       if that' appropriate for this kind of log.  Otherwise,
       don't do anything. */
    public abstract Log reopen(Log l) throws IOException;
    }
