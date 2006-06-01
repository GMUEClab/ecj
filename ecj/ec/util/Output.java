/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.util;
import java.io.*;
import java.util.Vector;
import java.util.Enumeration;

/*
 * Output.java
 * Created: Sat Aug  7 15:30:54 1999
 *
 */

/**
 * <p>Outputs and logs system messages, errors, and other various
 * items printed as a result of a run.
 *
 * <p> Output maintains zero or more logs, which contain Writers which
 * write out stuff.  Each log has an associated verbosity; if request
 * is made to write text to a log, and the text's maximal verbosity
 * is lower than the verbosity of the log, the log will not write it.
 * Each Output instance also has an instance-level global verbosity;
 * incoming requests to write text are additionally subject to this
 * verbosity test.  Lastly, the Output class itself has a global
 * verbosity as well.  This last verbostity is useful for shutting
 * down writing to all logs in the entire system in a simple way.
 *
 * <p>When the system fails for some reason and must be started back
 * up from a checkpoint, Output's log files may be overwritten.  Output
 * offers three approaches here.  First, Output can clear the log file
 * and overwrite it.  Second, Output can append to the existing log file;
 * because checkpoints are only done occasionally, this may result in
 * duplicate outputs to a file, so keep this in mind.  Third, Output can
 * keep certain written text, typically <i>announcements</i>, in memory;
 * this text gets written out into the checkpoint file, and so it is sound.
 *
 * <p>There are several kinds of announcements, in different levels
 * of importance.
 *
 * <ol>
 * <li> SYSTEM MESSAGEs.  Useful system-level facts printed out for the
 * benefit of the user.
 * <li> FATAL ERRORs.  These errors cause the system to exit(1) immediately.
 * <li> Simple ERRORs.  These errors set the "errors" flag to true; at 
 * the end of a stream of simple errors, the system in general is expected
 * to exit with a fatal error due to the flag being set.  That's the
 * protocol anyway.  On restart from a checkpoint, if there were any
 * simple errors, the system ends with a fatal error automatically.
 * <li> WARNINGs.  These errors do not cause the system to exit under any
 * circumstances.
 * <li> MESSAGEs.  Useful facts printed out for the benefit of the user.
 * </ol>
 *
 * <p>The default verbosity values for different kinds of announcements are
 * given below:
 *
 <table><tr><td>0</td><td>V_VERBOSE</td><td>(totally verbose)</td>
 </tr><tr><td>1000</td><td>V_NO_MESSAGES</td><td>(don't print messages)</td>
 </tr><tr><td>2000</td><td>V_NO_WARNINGS</td><td>(don't print warnings or messages)</td>
 </tr><tr><td>3000</td><td>V_NO_GENERAL</td><td>(don't print warnings, messages, or other "general info" stuff that might come along (like statistics maybe))</td>
 </tr><tr><td>4000</td><td>V_NO_ERRORS</td><td>(don't even print errors)</td>
 </tr><tr><td>5000</td><td>V_TOTALLY_SILENT</td><td>(be totally silent)</td>
 </tr></table>
 *
 *
 * @author Sean Luke
 * @version 1.0
 */

public class Output implements Serializable
    {
    boolean errors;
    Vector logs;
    Vector announcements;
    int verbosity;
    boolean flush;
    String filePrefix = "";

    public static final int ALL_LOGS = -1;

    /** Total verbosity */
    public static final int V_VERBOSE = 0;
    /** Don't print messages */
    public static final int V_NO_MESSAGES = 1000;
    /** Don't print warnings or messages */
    public static final int V_NO_WARNINGS = 2000;
    /** The standard verbosity to use if you don't want common reporting (like statistics) */
    public static final int V_NO_GENERAL = 3000;
    /** Don't print warnings, messages, or simple errors  */
    public static final int V_NO_ERRORS = 4000;
    /** No verbosity at all, not even system messages or fatal errors*/
    public static final int V_TOTALLY_SILENT = 5000;
    
    public void setFilePrefix(String filePrefix) {
        this.filePrefix = filePrefix;
    }

    protected void finalize() throws Throwable
        {
        // flush the logs
        close();

        // do super.finalize, just for good style
        super.finalize();
        }

    private void exitWithError()
        {
        // flush logs first
        close();

        // exit
        System.exit(1);
        }

    /** Closes the logs -- ONLY call this if you are preparing to quit */
    public synchronized void close()
        {
        // just in case
        flush();
        
        Enumeration e = logs.elements();
        while(e.hasMoreElements())
            {
            Log log = (Log)e.nextElement();
            if (!log.isLoggingToSystemOut)
                log.writer.close();
            }
        }

    /** Flushes the logs */
    public synchronized void flush()
        {
        Enumeration e = logs.elements();
        while(e.hasMoreElements())
            {
            Log log = (Log)e.nextElement();
            log.writer.flush();
            }
        // just in case...
        System.out.flush();
        System.err.flush();
        }

    /** Creates a new, verbose, empty Output object. */
    public Output(boolean storeAnnouncementsInMemory, int _verbosity)
        {
        errors = false;
        logs = new Vector();
        if (storeAnnouncementsInMemory) 
            announcements = new Vector();
        verbosity = _verbosity;
        }

    /** Sets whether the Output flushes its announcements.*/
    public synchronized boolean setFlush(boolean v)
        {
        boolean oldFlush = flush;
        flush = v;
        return oldFlush;
        }
    
    /** Sets the Output object's general verbosity to <i>v</i>. */
    public synchronized int setVerbosity(int v)
        {
        int oldVerbosity = verbosity;
        verbosity = v;
        return oldVerbosity;
        }
    
    /** Returns the Output object's general verbosity*/
    public synchronized int getVerbosity()
        {
        return verbosity;
        }

    /** Creates a new log of minimal verbosity <i>verbosity</i> and adds it 
        to Output.  This log will write to the file <i>filename</i>, and may
        or may not <i>post announcements</i> to the log. If the log must be
        reset upon restarting from a checkpoint, it will append to the file
        or erase the file and start over depending on <i>appendOnRestart</i>.
        If <i>appendOnRestart</i> is false and <i>postAnnouncements</i> is
        true, then this log will repost all the announcements on restarting
        from a checkpoint. Returns the position of the log in Output's 
        collection of logs -- you should use this to access the log always;
        never store the log itself, which may go away upon a system restart.
        The log can be compressed with gzip, but you cannot appendOnRestart
        and compress at the same time.*/

    public synchronized int addLog(File filename,
                                   int _verbosity,
                                   boolean postAnnouncements,
                                   boolean appendOnRestart,
                                   boolean gzip) throws IOException
        {
        if (filePrefix != null && filePrefix.length()>0)
            filename = new File(filename.getParent(),filePrefix+filename.getName());
        logs.addElement(new Log(filename,_verbosity,postAnnouncements,appendOnRestart,gzip));
        return logs.size()-1;
        }
        
    /** Creates a new log of minimal verbosity <i>verbosity</i> and adds it 
        to Output.  This log will write to the file <i>filename</i>, and may
        or may not <i>post announcements</i> to the log. If the log must be
        reset upon restarting from a checkpoint, it will append to the file
        or erase the file and start over depending on <i>appendOnRestart</i>.
        If <i>appendOnRestart</i> is false and <i>postAnnouncements</i> is
        true, then this log will repost all the announcements on restarting
        from a checkpoint. Returns the position of the log in Output's 
        collection of logs -- you should use this to access the log always;
        never store the log itself, which may go away upon a system restart. */

    public synchronized int addLog(File filename,
                                   int _verbosity,
                                   boolean postAnnouncements,
                                   boolean appendOnRestart) throws IOException
        {
        if (filePrefix != null && filePrefix.length()>0)
            filename = new File(filename.getParent(),filePrefix+filename.getName());
        logs.addElement(new Log(filename,_verbosity,postAnnouncements,appendOnRestart));
        return logs.size()-1;
        }


    /** Creates a new log of minimal verbosity <i>verbosity</i> and adds it 
        to Output.  This log will write to stdout (descriptor == Log.D_STDOUT) 
        or stderr (descriptor == Log.D_STDERR), and may or may not
        <i>post announcements</i> to the log. Returns the position of the 
        log in Output's 
        collection of logs -- you should use this to access the log always;
        never store the log itself, which may go away upon a system restart. */
    
    public synchronized int addLog(int descriptor,
                                   int _verbosity,
                                   boolean postAnnouncements)
        {
        logs.addElement(new Log(descriptor,_verbosity,postAnnouncements));
        return logs.size()-1;
        }

    /** Creates a new log of minimal verbosity <i>verbosity</i> and adds it 
        to Output.  This log may or may not <i>post announcements</i> to
        the log, and if it does, it additionally may or may not <i>repost</i>
        all of its announcements to the log upon a restart.  The log
        writes to <i>writer</i>, which is reset upon system restart by
        <i>restarter</i>. Returns the position of the log in Output's 
        collection of logs -- you should use this to access the log always;
        never store the log itself, which may go away upon a system restart. */

    public synchronized int addLog(Writer writer,
                                   LogRestarter restarter,
                                   int _verbosity,
                                   boolean postAnnouncements,
                                   boolean repostAnnouncements)
        {
        logs.addElement(new Log(writer,restarter,_verbosity,postAnnouncements,repostAnnouncements));
        return logs.size()-1;
        }

    /** Adds the given log to Output.  In general you shouldn't use this
        method unless you really <i>really</i> need something custom. 
        Returns the position of the log in Output's 
        collection of logs -- you should use this to access the log always;
        never store the log itself, which may go away upon a system restart. */

    public synchronized int addLog(Log l)
        {
        logs.addElement(l);
        return logs.size()-1;
        }

    /** Returns the number of logs currently posted. */
    public synchronized int numLogs()
        {
        return logs.size();
        }

    /** Returns the given log. */
    public synchronized Log log(int x)
        {
        return (Log)logs.elementAt(x);
        }

    /** Removes the given log. */
    public synchronized Log removeLog(int x)
        {
        Log l = log(x);
        logs.removeElementAt(x);
        return l;
        }
    
    /** Prints an initial error to System.err.  This is only to
        be used by ec.Evolve in starting up the system. */
    public static void initialError(String s)
        {
        System.err.println("STARTUP ERROR:\n" + s);

        // just in case...
        System.out.flush();
        System.err.flush();
        System.exit(1);
        }

    /** Prints an initial error to System.err.  This is only to
        be used by ec.Evolve in starting up the system. */
    public static void initialError(String s, Parameter p1)
        {
        System.err.println("STARTUP ERROR:\n" + s);
        if (p1!=null) System.err.println("PARAMETER: " + p1);

        // just in case...
        System.out.flush();
        System.err.flush();
        System.exit(1);
        }

    /** Prints an initial error to System.err.  This is only to
        be used by ec.Evolve in starting up the system. */
    public static void initialError(String s, Parameter p1, Parameter p2)
        {
        System.err.println("STARTUP ERROR:\n" + s);
        if (p1!=null) System.err.println("PARAMETER: " + p1);
        if (p2!=null && p1!=null) System.err.println("     ALSO: " + p2);

        // just in case...
        System.out.flush();
        System.err.flush();
        System.exit(1);
        }
    /** Posts a system message. */
    public synchronized void systemMessage(String s)
        {
        println(s, V_TOTALLY_SILENT,ALL_LOGS, true);
        }

    /** Posts a fatal error.  This causes the system to exit. */
    public synchronized void fatal(String s)
        {
        println("FATAL ERROR:\n"+s, V_TOTALLY_SILENT, ALL_LOGS, true);
        exitWithError();
        }
            
    /** Posts a fatal error.  This causes the system to exit. */
    public synchronized void fatal(String s, Parameter p1)
        {
        println("FATAL ERROR:\n"+s, V_TOTALLY_SILENT, ALL_LOGS, true);
        if (p1!=null) println("PARAMETER: " + p1, V_TOTALLY_SILENT, ALL_LOGS, true);
        exitWithError();
        }

    /** Posts a fatal error.  This causes the system to exit. */
    public synchronized void fatal(String s, Parameter p1, Parameter p2)
        {
        println("FATAL ERROR:\n"+s, V_TOTALLY_SILENT, ALL_LOGS, true);
        if (p1!=null) println("PARAMETER: " + p1, V_TOTALLY_SILENT, ALL_LOGS, true);
        if (p2!=null && p1!=null) println("     ALSO: " + p2, V_TOTALLY_SILENT, ALL_LOGS, true);
        else println("PARAMETER: " + p2, V_TOTALLY_SILENT, ALL_LOGS, true);
        exitWithError();
        }

    /** Posts a simple error. This causes the error flag to be raised as well. */
    public synchronized void error(String s)
        {
        println("ERROR:\n"+s, V_NO_ERRORS, ALL_LOGS, true);
        errors = true;
        }
            
    /** Posts a simple error. This causes the error flag to be raised as well. */
    public synchronized void error(String s, Parameter p1)
        {
        println("ERROR:\n"+s, V_TOTALLY_SILENT, ALL_LOGS, true);
        if (p1!=null) println("PARAMETER: " + p1, V_TOTALLY_SILENT, ALL_LOGS, true);
        errors = true;
        }

    /** Posts a simple error. This causes the error flag to be raised as well. */
    public synchronized void error(String s, Parameter p1, Parameter p2)
        {
        println("ERROR:\n"+s, V_TOTALLY_SILENT, ALL_LOGS, true);
        if (p1!=null) println("PARAMETER: " + p1, V_TOTALLY_SILENT, ALL_LOGS, true);
        if (p2!=null && p1!=null) println("     ALSO: " + p2, V_TOTALLY_SILENT, ALL_LOGS, true);
        else println("PARAMETER: " + p2, V_TOTALLY_SILENT, ALL_LOGS, true);
        errors = true;
        }

    /** Posts a warning. */
    public synchronized void warning(String s, Parameter p1, Parameter p2)
        {
        println("WARNING:\n"+s, V_NO_WARNINGS, ALL_LOGS, true);
        if (p1!=null) println("PARAMETER: " + p1, V_TOTALLY_SILENT, ALL_LOGS, true);
        if (p2!=null && p1!=null) println("     ALSO: " + p2, V_TOTALLY_SILENT, ALL_LOGS, true);
        else println("PARAMETER: " + p2, V_TOTALLY_SILENT, ALL_LOGS, true);
        }

    /** Posts a warning. */
    public synchronized void warning(String s, Parameter p1)
        {
        println("WARNING:\n"+s, V_NO_WARNINGS, ALL_LOGS, true);
        if (p1!=null) println("PARAMETER: " + p1, V_TOTALLY_SILENT, ALL_LOGS, true);
        }

    /** Posts a warning. */
    public synchronized void warning(String s)
        {
        println("WARNING:\n"+s, V_NO_WARNINGS, ALL_LOGS, true);
        }
    
    java.util.HashSet oneTimeWarnings = new java.util.HashSet();
    /** Posts a warning one time only. */
    public synchronized void warnOnce(String s)
        {
        if (!oneTimeWarnings.contains(s))
            {
            oneTimeWarnings.add(s);
            println("ONCE-ONLY WARNING:\n"+s, V_NO_WARNINGS, ALL_LOGS, true);
            }
        }
        
    public synchronized void warnOnce(String s, Parameter p1)
        {
        if (!oneTimeWarnings.contains(s))
            {
            oneTimeWarnings.add(s);
            println("ONCE-ONLY WARNING:\n"+s, V_NO_WARNINGS, ALL_LOGS, true);
            if (p1!=null) println("PARAMETER: " + p1, V_TOTALLY_SILENT, ALL_LOGS, true);
            }
        }
        
    public synchronized void warnOnce(String s, Parameter p1, Parameter p2)
        {
        if (!oneTimeWarnings.contains(s))
            {
            oneTimeWarnings.add(s);
            println("ONCE-ONLY WARNING:\n"+s, V_NO_WARNINGS, ALL_LOGS, true);
            if (p1!=null) println("PARAMETER: " + p1, V_TOTALLY_SILENT, ALL_LOGS, true);
            if (p2!=null && p1!=null) println("     ALSO: " + p2, V_TOTALLY_SILENT, ALL_LOGS, true);
            else println("PARAMETER: " + p2, V_TOTALLY_SILENT, ALL_LOGS, true);
            }
        }

    
    /** Posts a message. */
    public synchronized void message(String s)
        {
        println(s, V_NO_MESSAGES, ALL_LOGS, true);
        }
  
    /** Forces a file-based log to reopen, erasing its previous contents.
        non-file logs ignore this. */
    
    public synchronized void reopen(int _log) throws IOException
        {
        Log oldlog = (Log)logs.elementAt(_log);
        logs.setElementAt(oldlog.reopen(),_log);
        }
    
    /** Forces one or more file-based logs to reopen, erasing 
        their previous contents.  non-file logs ignore this. */
    
    public synchronized void reopen(int[] _logs) throws IOException
        {
        for(int x=0;x<_logs.length;x++)
            {
            Log oldlog = (Log)logs.elementAt(_logs[x]);
            logs.setElementAt(oldlog.reopen(),_logs[x]);
            }
        }
    

    /** Prints a message to a given log, with a certain verbosity.  
        <i>_announcement</i> indicates that the message is an announcement. */

    protected synchronized void println(String s,
                                        int _verbosity,
                                        Log log,
                                        boolean _announcement,
                                        boolean _reposting) throws OutputException
        {
        if (log.writer==null) throw new OutputException("Log with a null writer: " + log);
        if (!log.postAnnouncements && _announcement) return;  // don't write it
        if (log.verbosity >= _verbosity) return;  // don't write it
        if (verbosity >= _verbosity) return;  // don't write it
        // now write it
        log.writer.println(s);
        if (flush) log.writer.flush();
        //...and stash it in memory maybe
        if (announcements!=null && _announcement && !_reposting)
            announcements.addElement(new Announcement(s,_verbosity));
        }


    /** Prints a message to a given log, 
        with a certain verbosity.  If log==ALL_LOGS, posted to all logs. */
    public synchronized void println(String s,
                                     int _verbosity,
                                     int log,
                                     boolean _announcement) throws OutputException
        {
        if (log==ALL_LOGS) for (int x = 0; x<logs.size();x++)
            {
            Log l = (Log) logs.elementAt(x);
            if (l==null) throw new OutputException("Unknown log number" + l);
            println(s,_verbosity,l,_announcement,false);
            }
        else
            {
            Log l = (Log) logs.elementAt(log);
            if (l==null) throw new OutputException("Unknown log number" + l);
            println(s,_verbosity,l,_announcement,false);
            }
        }

    /** Prints a non-announcement message to the given logs, 
        with a certain verbosity. */
    public synchronized void println(String s,
                                     int _verbosity,
                                     int[] _logs) throws OutputException
        {
        for(int x=0;x<_logs.length;x++)
            println(s,_verbosity,(Log)(logs.elementAt(_logs[x])),false,false);
        }


    /** Prints a non-announcement message to the given logs, 
        with a certain verbosity. */
    public synchronized void println(String s,
                                     int _verbosity,
                                     int log) throws OutputException
        {
        println(s,_verbosity,(Log)(logs.elementAt(log)),false,false);
        }


    /** Prints a non-announcement message to a given log, with a 
        certain verbosity. No '\n' is printed.  */
    protected synchronized void print(String s,
                                      int _verbosity,
                                      Log log) throws OutputException
        {
        if (log.writer==null) throw new OutputException("Log with a null writer: " + log);
        if (log.verbosity >= _verbosity) return;  // don't write it
        if (verbosity >= _verbosity) return;  // don't write it
        // now write it
        log.writer.print(s);
        if (flush) log.writer.flush();
        }

    /** Prints a non-announcement message to a given log, with a
        certain verbosity. If log==ALL_LOGS, posted to all logs. 
        No '\n' is printed.  */
    public synchronized void print(String s,
                                   int _verbosity,
                                   int log) throws OutputException
        {
        if (log==ALL_LOGS) for (int x = 0; x<logs.size();x++)
            {
            Log l = (Log) logs.elementAt(x);
            if (l==null) throw new OutputException("Unknown log number" + l);
            print(s,_verbosity,l);
            }
        else
            {
            Log l = (Log) logs.elementAt(log);
            if (l==null) throw new OutputException("Unknown log number" + l);
            print(s,_verbosity,l);
            }
        }

    /** Prints a non-announcement message to the given logs, 
        with a certain verbosity. No '\n' is printed.  */
    public synchronized void print(String s,
                                   int _verbosity,
                                   int[] _logs) throws OutputException
        {
        for(int x=0;x<_logs.length;x++)
            print(s,_verbosity,_logs[x]);
        }


    /** Exits with a fatal error if the error flag has been raised. */
    public synchronized void exitIfErrors()
        {
        if (errors) 
            {
            println("SYSTEM EXITING FROM ERRORS\n", V_TOTALLY_SILENT,ALL_LOGS, true);
            exitWithError();
            }
        }

    /** Clears the error flag. */
    public synchronized void clearErrors()
        {
        errors = false;
        }


    /** Clears out announcements.  Note that this will cause these
        announcements to be unavailable for reposting after a restart! */
    public synchronized void clearAnnouncements()
        {
        if (announcements!=null)
            announcements = new Vector();
        }

    public synchronized void restart() throws IOException
        {
        // restart logs, then repost announcements to them
        int ls = logs.size();
        for(int x=0;x<ls;x++)
            {
            Log l = (Log)(logs.elementAt(x));
            logs.setElementAt(l = l.restarter.restart(l),x);
            if (l.repostAnnouncementsOnRestart && announcements!=null)
                {    
                int as = announcements.size();
                for (int y=0;y<as;y++)
                    {
                    Announcement a = (Announcement)(announcements.elementAt(y));
                    println(a.text,a.verbosity,l,true,true);
                    }
                }
            }

        // exit with a fatal error if the errors flag is set. 
        exitIfErrors();
        }
    }
