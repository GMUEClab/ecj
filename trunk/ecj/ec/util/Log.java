/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.util;
import java.io.*;
import java.util.zip.*;

/*
 * Log.java
 * Created: Sun Aug  8 18:53:49 1999
 */

/**
 * Defines a log to which Output outputs.  A log consists of three items:
 * <ol>
 * <li> A PrintWriter which prints out messages.
 * <li> A verbosity -- if a message is printed with a verbosity lower than
 * this, then the message will not be printed.
 * <li> A flag which indicates whether or not to filter out (refuse to print)
 * announcements (usually error messages and warnings).
 * </ol>
 *
 * Logs can be <i>restarted</i> after a computer outage by using the 
 * information stored in the LogRestarter
 * <tt>restarter</tt>.  
 *
 * @author Sean Luke
 * @version 1.0
 */


public class Log implements Serializable
    {
    // basic log features

    /** The log's writer */
    public transient PrintWriter writer;   // the actual writer.

    /** A filename, if the writer writes to a file */
    public File filename;                // the filename to write to, if any

    /** The log's verbosity. */
    public int verbosity;                  // the log verbosity

    /** Should the log post announcements? */
    public boolean postAnnouncements;      // will the log post announcements?

    // stuff for restarting

    /** The log's restarter */
    public LogRestarter restarter;         // resets the log

    /** Should the log repost all announcements on restart */
    public boolean repostAnnouncementsOnRestart;   // repost all announcements?

    /** If the log writes to a file, should it append to the file on restart,
        or should it overwrite the file? */
    public boolean appendOnRestart;        // append to a file or replace it?
    
    public boolean isLoggingToSystemOut;


    // values for specifying logs based on System.out or System.err

    /** Specifies that the log should write to stdout (System.out) */
    public final static int D_STDOUT = 0;
    
    /** Specifies that the log should write to stderr (System.err) */
    public final static int D_STDERR = 1;

    protected void finalize() throws Throwable 
        { 
        // rarely happens though... :-(
        super.finalize();
        if (writer!=null && !isLoggingToSystemOut) writer.close();
        }

    /** Creates a log to a given filename; this file may or may not
        be appended to on restart, depending on _appendOnRestart.  If
        and only if the file is <i>not</i> appended to on restart, then
        announcements are reposted on restart.*/
    
    public Log(File _filename,
               int _verbosity,
               boolean _postAnnouncements,
               boolean _appendOnRestart) throws IOException
        {
        this(_filename, _verbosity, _postAnnouncements, _appendOnRestart, false);
        }
        
    /** Creates a log to a given filename; this file may or may not
        be appended to on restart, depending on _appendOnRestart.  If
        and only if the file is <i>not</i> appended to on restart, then
        announcements are reposted on restart. The file can be compressed
        with gzip, but you may not both gzip AND appendOnRestart.  If gzipped,
        then .gz is automagically appended to the file name.*/
    
    public Log(File _filename,
               int _verbosity,
               boolean _postAnnouncements,
               boolean _appendOnRestart,
               boolean gzip) throws IOException
        {
        verbosity = _verbosity;
        postAnnouncements = _postAnnouncements;
        repostAnnouncementsOnRestart = !_appendOnRestart;
        appendOnRestart = _appendOnRestart;
        isLoggingToSystemOut = false;
                    
        if (gzip)
            {
            filename = new File(_filename.getAbsolutePath() + ".gz");
            if (appendOnRestart) throw new IOException("Cannot gzip and appendOnRestart at the same time: new Log(File,int,boolean,boolean,boolean)");

            writer = new PrintWriter(new OutputStreamWriter(new GZIPOutputStream(
                                                                new BufferedOutputStream(
                                                                    new FileOutputStream(filename)))));
            restarter = new LogRestarter()
                {
                public Log restart(Log l) throws IOException
                    {
                    return reopen(l);
                    }
                public Log reopen(Log l) throws IOException
                    {
                    if (l.writer!=null && !l.isLoggingToSystemOut) l.writer.close();
                    l.writer = new PrintWriter(new OutputStreamWriter(new GZIPOutputStream(
                                                                          new BufferedOutputStream(new FileOutputStream(l.filename)))));
                    return l;
                    }
                };
            }
                            
        else 
            {
            filename = _filename;
            writer = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
            restarter = new LogRestarter()
                {
                public Log restart(Log l) throws IOException
                    {
                    l.writer = new PrintWriter(new BufferedWriter(new FileWriter(l.filename.getPath(),l.appendOnRestart)));
                    return l;
                    }
                public Log reopen(Log l) throws IOException
                    {
                    if (l.writer!=null && !isLoggingToSystemOut) l.writer.close();
                    l.writer = new PrintWriter(new BufferedWriter(new FileWriter(l.filename)));
                    return l;
                    }
                };
            }
        }

    /** Creates a log on stdout (descriptor == Log.D_STDOUT) 
        or stderr (descriptor == Log.D_STDERR). */

    public Log(int descriptor,
               int _verbosity,
               boolean _postAnnouncements)
        {
        filename = null;
        verbosity = _verbosity;
        postAnnouncements = _postAnnouncements;
        repostAnnouncementsOnRestart = true;
        appendOnRestart = true;  // doesn't matter
        isLoggingToSystemOut = true;
        if (descriptor == D_STDOUT)
            {
            writer = new PrintWriter(System.out);
            restarter = new LogRestarter()
                {
                public Log restart(Log l) throws IOException
                    {
                    l.writer = new PrintWriter(System.out);
                    return l;
                    }
                public Log reopen(Log l) throws IOException
                    {
                    return l;  // makes no sense
                    }
                };
            }
        else  // D_STDERR
            {
            writer = new PrintWriter(System.err);
            restarter = new LogRestarter()
                {
                public Log restart(Log l) throws IOException
                    {
                    l.writer = new PrintWriter(System.err);
                    return l;
                    }
                public Log reopen(Log l) throws IOException
                    {
                    return l;  // makes no sense
                    }
                };
            }
        }


    /** Creates a log on a given Writer and custom LogRestarter.  In general,
        You should not use this to write to a file.  Use Log(_filename...
        instead. */

    public Log(Writer _writer,
               LogRestarter _restarter,
               int _verbosity,
               boolean _postAnnouncements,
               boolean _repostAnnouncementsOnRestart)
        {
        filename = null;
        verbosity = _verbosity;
        postAnnouncements = _postAnnouncements;
        repostAnnouncementsOnRestart = _repostAnnouncementsOnRestart;
        appendOnRestart = true;  // doesn't matter
        /*
         * Unfortunately, we can't know if the specified Writer wraps
         * System.out.  Err on the side of caution and assume we're not.
         */
        isLoggingToSystemOut = false;
        writer = new PrintWriter(new BufferedWriter(_writer));
        restarter = _restarter;
        }


    /** Restarts a log after a system restart from checkpoint.  Returns
        the restarted log -- note that it may not be the same log! */
    
    public Log restart() throws IOException
        {
        return restarter.restart(this);
        }

    /** Forces a file-based log to reopen, erasing its previous contents.
        non-file logs ignore this. */

    public Log reopen() throws IOException
        {
        return restarter.reopen(this);
        }

    }

