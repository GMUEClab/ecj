/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.util;
import java.io.Serializable;

/* 
 * Announcement.java
 * 
 * Created: Wed Aug 11 15:45:15 1999
 * By: Sean Luke
 */

/**
 * Announcements are messages which are stored by ec.util.Output in
 * memory, in addition to being logged out to files.  The purpose of
 * this is that announcements saved in the checkpointing process.
 * You can turn off the memory-storage of announcements with an argument
 * passed to ec.Evolve when you start the run.
 *
 * <p>Announcements have text and an integer indicating the level of
 * verbosity above which they will no longer print (higher verbosity
 * numbers indicate <i>less</i> verbosity).
 *
 * @author Sean Luke
 * @version 1.0 
 */

public class Announcement implements Serializable
    {
    /** The announcement's...anouncement.*/
    public String text;
    /** The announcement's maximum verbosity value */
    public int verbosity;

    /** Creates a new announcement with text <i>t</i> and verbosity value <i>v</i> */
    public Announcement (String t, int v)
        {
        text = t; verbosity = v;
        }
    }
