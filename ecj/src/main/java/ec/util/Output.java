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
 * verbosity as well.  This last verbosity is useful for shutting
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
 * <li> FATAL ERRORs.  These errors cause the system to exit(1) immediately.
 * <li> Simple ERRORs.  These errors set the "errors" flag to true; at 
 * the end of a stream of simple errors, the system in general is expected
 * to exit with a fatal error due to the flag being set.  That's the
 * protocol anyway.  On restart from a checkpoint, if there were any
 * simple errors, the system ends with a fatal error automatically.
 * <li> WARNINGs.  These errors do not cause the system to exit under any
 * circumstances.
 * <li> MESSAGEs.  Useful facts printed out for the benefit of the user.
 * <li> SYSTEM MESSAGEs.  Useful system-level facts printed out for the
 * benefit of the user.
 * </ol>
 *
 * 
 * <p>Output will also store all announcements in memory by default so as to reproduce
 * them if it's restarted from a checkpoint.  You can change this behavior also by
 *
 * @author Sean Luke
 * @version 1.0
 */

public class Output implements Serializable
    {
    private static final long serialVersionUID = 1;

    public static class OutputExitException extends RuntimeException 
        {
        public OutputExitException(String message)
            {
            super(message);
            }
        }
    
    boolean errors;
    Vector logs = new Vector();
    Vector announcements = new Vector();
    // boolean flush = true;
    boolean store = true;
    String filePrefix = "";
    boolean throwsErrors = false;

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

    public static final int ALL_MESSAGE_LOGS = -1;
    /** When passed to print functions, doesn't do any printing */
    public static final int NO_LOGS = -2;
        
    public synchronized void setFilePrefix(String filePrefix) {
        this.filePrefix = filePrefix;
        }

    public synchronized void setThrowsErrors(boolean val)
        {
        throwsErrors = val;
        }
        
    public synchronized boolean getThrowsErrors() { return throwsErrors; }

    protected void finalize() throws Throwable
        {
        // flush the logs
        close();

        // do super.finalize, just for good style
        super.finalize();
        }

    private static void exitWithError(Output output, String message, boolean throwException)
        {
        // flush logs first
        if (output != null) output.close();
        System.out.flush();
        System.err.flush();

        // exit
        if (throwException)
            throw new OutputExitException(message);
        else
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

    /** Creates a new, verbose, empty Output object. 
        @deprecated Verbosity no longer has an effect.
    */
    public Output(boolean storeAnnouncementsInMemory, int _verbosity)
        {
        this(storeAnnouncementsInMemory);
        }

    /** Creates a new, verbose, empty Output object. */
    public Output(boolean storeAnnouncementsInMemory)
        {
        errors = false;
        store = storeAnnouncementsInMemory;
        }

    /** Sets whether the Output flushes its announcements.
        @deprecated We now always flush 
    */
    public synchronized void setFlush(boolean v)
        {
        // flush = v;
        }
    
    /* Returns the Output's flushing behavior. 
       @deprecated We now always flush 
    */
    public synchronized boolean getFlush()
        {
        // return flush;
        return true;
        }
    
    /** Sets whether the Output stores its announcements.*/
    public synchronized void setStore(boolean v)
        {
        store = v;
        }

    /* Returns the Output's storing behavior. */
    public synchronized boolean getStore()
        {
        return store;
        }
    
    /** Sets the Output object's general verbosity to <i>v</i>. 
        @deprecated Verbosity no longer has an effect.
    */
    public synchronized void setVerbosity(int v)
        {
//        verbosity = v;
        }
    
    /** Returns the Output object's general verbosity
        @deprecated Verbosity no longer has an effect.
    */
    public synchronized int getVerbosity()
        {
        return V_VERBOSE;
        }

    /** Creates a new log of minimal verbosity <i>verbosity</i> and adds it 
        to Output.  This log will write to the file <i>file</i>, and may
        or may not <i>post announcements</i> to the log. If the log must be
        reset upon restarting from a checkpoint, it will append to the file
        or erase the file and start over depending on <i>appendOnRestart</i>.
        If <i>appendOnRestart</i> is false and <i>postAnnouncements</i> is
        true, then this log will repost all the announcements on restarting
        from a checkpoint. Returns the position of the log in Output's 
        collection of logs -- you should use this to access the log always;
        never store the log itself, which may go away upon a system restart.
        The log can be compressed with gzip, but you cannot appendOnRestart
        and compress at the same time.
        @deprecated Verbosity no longer has an effect.
    */

    public synchronized int addLog(File file,
        int _verbosity,
        boolean postAnnouncements,
        boolean appendOnRestart,
        boolean gzip) throws IOException
        {
        if (filePrefix != null && filePrefix.length()>0)
            file = new File(file.getParent(),filePrefix+file.getName());
        logs.addElement(new Log(file,postAnnouncements,appendOnRestart,gzip));
        return logs.size()-1;
        }
        
    /** Creates a new log of minimal verbosity <i>verbosity</i> and adds it 
        to Output.  This log will write to the file <i>file</i>, and may
        or may not <i>post announcements</i> to the log. If the log must be
        reset upon restarting from a checkpoint, it will append to the file
        or erase the file and start over depending on <i>appendOnRestart</i>.
        If <i>appendOnRestart</i> is false and <i>postAnnouncements</i> is
        true, then this log will repost all the announcements on restarting
        from a checkpoint. Returns the position of the log in Output's 
        collection of logs -- you should use this to access the log always;
        never store the log itself, which may go away upon a system restart. 
        @deprecated Verbosity no longer has an effect
    */

    public synchronized int addLog(File file,
        int _verbosity,
        boolean postAnnouncements,
        boolean appendOnRestart) throws IOException
        {
        return addLog(file, postAnnouncements, appendOnRestart, false);
        }


    /** Creates a new log <!-- of minimal verbosity V_NO_GENERAL-1 --> and adds it 
        to Output.  This log will write to the file <i>file</i>, and may
        or may not <i>post announcements</i> to the log. If the log must be
        reset upon restarting from a checkpoint, it will append to the file
        or erase the file and start over depending on <i>appendOnRestart</i>.
        If <i>appendOnRestart</i> is false and <i>postAnnouncements</i> is
        true, then this log will repost all the announcements on restarting
        from a checkpoint. Returns the position of the log in Output's 
        collection of logs -- you should use this to access the log always;
        never store the log itself, which may go away upon a system restart.
        The log can be compressed with gzip, but you cannot appendOnRestart
        and compress at the same time.
    */

    public synchronized int addLog(File file,
        boolean postAnnouncements,
        boolean appendOnRestart,
        boolean gzip) throws IOException
        {
        return addLog(file, V_VERBOSE, postAnnouncements, appendOnRestart, gzip);
        }


    /** Creates a new log <!-- of minimal verbosity V_NO_GENERAL-1 --> and adds it 
        to Output.  This log will write to the file <i>file</i>, and you may
        not post announcements to the log. If the log must be
        reset upon restarting from a checkpoint, it will append to the file
        or erase the file and start over depending on <i>appendOnRestart</i>.
        Returns the position of the log in Output's 
        collection of logs -- you should use this to access the log always;
        never store the log itself, which may go away upon a system restart.
        The log can be compressed with gzip, but you cannot appendOnRestart
        and compress at the same time.*/

    public synchronized int addLog(File file,
        boolean appendOnRestart,
        boolean gzip) throws IOException
        {
        return addLog(file, false, appendOnRestart, gzip);
        }
        
    /** Creates a new log <!-- of minimal verbosity V_NO_GENERAL-1 --> and adds it 
        to Output.  This log will write to the file <i>file</i>, and you may
        not post announcements to the log. If the log must be
        reset upon restarting from a checkpoint, it will append to the file
        or erase the file and start over depending on <i>appendOnRestart</i>.
        Returns the position of the log in Output's 
        collection of logs -- you should use this to access the log always;
        never store the log itself, which may go away upon a system restart. */

    public synchronized int addLog(File file,
        boolean appendOnRestart) throws IOException
        {
        return addLog(file, false, appendOnRestart, false);
        }


    /** Creates a new log of minimal verbosity <i>verbosity</i> and adds it 
        to Output.  This log will write to stdout (descriptor == Log.D_STDOUT) 
        or stderr (descriptor == Log.D_STDERR), and may or may not
        <i>post announcements</i> to the log. Returns the position of the 
        log in Output's 
        collection of logs -- you should use this to access the log always;
        never store the log itself, which may go away upon a system restart. 
        @deprecated Verbosity no longer has an effect
    */
    
    public synchronized int addLog(int descriptor,
        int _verbosity,
        boolean postAnnouncements)
        {
        logs.addElement(new Log(descriptor,postAnnouncements));
        return logs.size()-1;
        }

    /** Creates a new log and adds it 
        to Output.  This log will write to stdout (descriptor == Log.D_STDOUT) 
        or stderr (descriptor == Log.D_STDERR), and may or may not
        <i>post announcements</i> to the log. Returns the position of the 
        log in Output's 
        collection of logs -- you should use this to access the log always;
        never store the log itself, which may go away upon a system restart. 
    */
    
    public synchronized int addLog(int descriptor,
        boolean postAnnouncements)
        {
        return addLog(descriptor, V_VERBOSE, postAnnouncements);
        }

    /** Creates a new log of minimal verbosity <i>verbosity</i> and adds it 
        to Output.  This log may or may not <i>post announcements</i> to
        the log, and if it does, it additionally may or may not <i>repost</i>
        all of its announcements to the log upon a restart.  The log
        writes to <i>writer</i>, which is reset upon system restart by
        <i>restarter</i>. Returns the position of the log in Output's 
        collection of logs -- you should use this to access the log always;
        never store the log itself, which may go away upon a system restart. 
        @deprecated Verbosity no longer has an effect
    */

    public synchronized int addLog(Writer writer,
        LogRestarter restarter,
        int _verbosity,
        boolean postAnnouncements,
        boolean repostAnnouncements)
        {
        logs.addElement(new Log(writer,restarter,postAnnouncements,repostAnnouncements));
        return logs.size()-1;
        }

    /** Creates a new log <!-- of minimal verbosity V_NO_GENERAL-1 --> and adds it 
        to Output.  This log may or may not <i>post announcements</i> to
        the log, and if it does, it additionally may or may not <i>repost</i>
        all of its announcements to the log upon a restart.  The log
        writes to <i>writer</i>, which is reset upon system restart by
        <i>restarter</i>. Returns the position of the log in Output's 
        collection of logs -- you should use this to access the log always;
        never store the log itself, which may go away upon a system restart. 
    */

    public synchronized int addLog(Writer writer,
        LogRestarter restarter,
        boolean postAnnouncements,
        boolean repostAnnouncements)
        {
        logs.addElement(new Log(writer,restarter,postAnnouncements,repostAnnouncements));
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
    public synchronized Log getLog(int x)
        {
        return (Log)logs.elementAt(x);
        }

    /** Removes the given log. */
    public synchronized Log removeLog(int x)
        {
        Log l = getLog(x);
        logs.removeElementAt(x);
        return l;
        }
    
    /** Prints an initial warning to System.err.  This is only to
        be used by ec.Evolve in starting up the system. */
    public static void initialWarning(String s)
        {
        initialWarning(s, null, null);
        }

    /** Prints an initial warning to System.err.  This is only to
        be used by ec.Evolve in starting up the system. */
    public static void initialWarning(String s, Parameter p1)
        {
        initialWarning(s, p1, null);
        }

    /** Prints an initial warning to System.err.  This is only to
        be used by ec.Evolve in starting up the system. */
    public static void initialWarning(String s, Parameter p1, Parameter p2)
        {
        System.err.println("STARTUP WARNING:\n" + s);
        if (p1!=null) 
            {
            System.err.println("PARAMETER: " + p1);
            }

        if (p2!=null && p1!=null)
            {
            System.err.println("     ALSO: " + p2);
            }
        }

    /** Prints an initial error to System.err.  This is only to
        be used by ec.Evolve in starting up the system. */
    public static void initialError(String s)
        {
        String er = "STARTUP ERROR:\n" + s;
        System.err.println(er);

        //System.exit(1);
        exitWithError(null, er, false);
        }

    /** Prints an initial error to System.err.  This is only to
        be used by ec.Evolve in starting up the system. */
    public static void initialError(String s, Parameter p1)
        {
        String er = "STARTUP ERROR:\n" + s;
        System.err.println(er);
        if (p1!=null) 
            {
            er += "PARAMETER: " + p1;
            System.err.println("PARAMETER: " + p1);
            }

        //System.exit(1);
        exitWithError(null, er, false);
        }

    /** Prints an initial error to System.err.  This is only to
        be used by ec.Evolve in starting up the system. */
    public static void initialError(String s, Parameter p1, Parameter p2)
        {
        String er = "STARTUP ERROR:\n" + s;
        System.err.println(er);
        if (p1!=null) 
            {
            er += "PARAMETER: " + p1;
            System.err.println("PARAMETER: " + p1);
            }

        if (p2!=null && p1!=null)
            {
            er += "     ALSO: " + p2;
            System.err.println("     ALSO: " + p2);
            }

        //System.exit(1);
        exitWithError(null, er, false);
        }

    /** Prints an initial message to System.err.  This is only to
        be used by ec.Evolve in starting up the system.  These messages are not logged. */
    public static void initialMessage(String s)
        {
        System.err.println(s);
        System.err.flush();
        }

    /** Posts a system message. */
    public synchronized void systemMessage(String s)
        {
        println(s, V_NO_MESSAGES ,ALL_MESSAGE_LOGS, true);
        }

    StringBuilder error = new StringBuilder();
    // builds up an error message in case the user wants to throw
    // an exception rather than quit
    String a(String str)
        {
        error.append(str);
        error.append("\n");
        return str;
        }
                
    /** Posts a fatal error.  This causes the system to exit. */
    public synchronized void fatal(String s)
        {
        StringBuilder error = new StringBuilder();
        println(a("FATAL ERROR:\n"+s), ALL_MESSAGE_LOGS, true);
        exitWithError(this, error.toString(), throwsErrors);
        }
            
    /** Posts a fatal error.  This causes the system to exit. */
    public synchronized void fatal(String s, Parameter p1)
        {
        println(a("FATAL ERROR:\n"+s), ALL_MESSAGE_LOGS, true);
        if (p1!=null) println(a("PARAMETER: " + p1), ALL_MESSAGE_LOGS, true);
        exitWithError(this, error.toString(), throwsErrors);
        }

    /** Posts a fatal error.  This causes the system to exit. */
    public synchronized void fatal(String s, Parameter p1, Parameter p2)
        {
        println(a("FATAL ERROR:\n"+s), ALL_MESSAGE_LOGS, true);
        if (p1!=null) println(a("PARAMETER: " + p1), ALL_MESSAGE_LOGS, true);
        if (p2!=null && p1!=null) println(a("     ALSO: " + p2), ALL_MESSAGE_LOGS, true);
        else println(a("PARAMETER: " + p2), ALL_MESSAGE_LOGS, true);
        exitWithError(this, error.toString(), throwsErrors);
        }

    /** Posts a simple error. This causes the error flag to be raised as well. */
    public synchronized void error(String s)
        {
        println(a("ERROR:\n"+s), ALL_MESSAGE_LOGS, true);
        errors = true;
        }
            
    /** Posts a simple error. This causes the error flag to be raised as well. */
    public synchronized void error(String s, Parameter p1)
        {
        println(a("ERROR:\n"+s), ALL_MESSAGE_LOGS, true);
        if (p1!=null) println(a("PARAMETER: " + p1), ALL_MESSAGE_LOGS, true);
        errors = true;
        }

    /** Posts a simple error. This causes the error flag to be raised as well. */
    public synchronized void error(String s, Parameter p1, Parameter p2)
        {
        println(a("ERROR:\n"+s), ALL_MESSAGE_LOGS, true);
        if (p1!=null) println(a("PARAMETER: " + p1), ALL_MESSAGE_LOGS, true);
        if (p2!=null && p1!=null) println(a("     ALSO: " + p2), ALL_MESSAGE_LOGS, true);
        else println(a("PARAMETER: " + p2), ALL_MESSAGE_LOGS, true);
        errors = true;
        }

    /** Posts a warning. */
    public synchronized void warning(String s, Parameter p1, Parameter p2)
        {
        println("WARNING:\n"+s, ALL_MESSAGE_LOGS, true);
        if (p1!=null) println("PARAMETER: " + p1, ALL_MESSAGE_LOGS, true);
        if (p2!=null && p1!=null) println("     ALSO: " + p2, ALL_MESSAGE_LOGS, true);
        else println("PARAMETER: " + p2, ALL_MESSAGE_LOGS, true);
        }

    /** Posts a warning. */
    public synchronized void warning(String s, Parameter p1)
        {
        println("WARNING:\n"+s, ALL_MESSAGE_LOGS, true);
        if (p1!=null) println("PARAMETER: " + p1, ALL_MESSAGE_LOGS, true);
        }

    /** Posts a warning. */
    public synchronized void warning(String s)
        {
        println("WARNING:\n"+s, ALL_MESSAGE_LOGS, true);
        }
    
    java.util.HashSet oneTimeWarnings = new java.util.HashSet();
    /** Posts a warning one time only. */
    public synchronized void warnOnce(String s)
        {
        if (!oneTimeWarnings.contains(s))
            {
            oneTimeWarnings.add(s);
            println("ONCE-ONLY WARNING:\n"+s, ALL_MESSAGE_LOGS, true);
            }
        }
        
    public synchronized void warnOnce(String s, Parameter p1)
        {
        if (!oneTimeWarnings.contains(s))
            {
            oneTimeWarnings.add(s);
            println("ONCE-ONLY WARNING:\n"+s, ALL_MESSAGE_LOGS, true);
            if (p1!=null) println("PARAMETER: " + p1, ALL_MESSAGE_LOGS, true);
            }
        }
        
    public synchronized void warnOnce(String s, Parameter p1, Parameter p2)
        {
        if (!oneTimeWarnings.contains(s))
            {
            oneTimeWarnings.add(s);
            println("ONCE-ONLY WARNING:\n"+s, ALL_MESSAGE_LOGS, true);
            if (p1!=null) println("PARAMETER: " + p1, ALL_MESSAGE_LOGS, true);
            if (p2!=null && p1!=null) println("     ALSO: " + p2, ALL_MESSAGE_LOGS, true);
            else println("PARAMETER: " + p2, ALL_MESSAGE_LOGS, true);
            }
        }

    
    /** Posts a message. */
    public synchronized void message(String s)
        {
        println(s, ALL_MESSAGE_LOGS, true);
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
        <i>_announcement</i> indicates that the message is an announcement. 
        If the log is null, nothing is printed.
        @deprecated Verbosity no longer has an effect
    */

    synchronized void println(String s,
        int _verbosity,
        Log log,
        boolean _announcement,
        boolean _reposting) throws OutputException
        {
        if (log==null) return;
        if (log.writer==null) throw new OutputException("Log with a null writer: " + log);
        if (!log.postAnnouncements && _announcement) return;  // don't write it
        // if (log.verbosity >= _verbosity) return;  // don't write it
        // if (verbosity >= _verbosity) return;  // don't write it
        if (log.silent) return;  // don't write it
        // now write it
        log.writer.println(s);
        // if (flush) 
        // always flush
        log.writer.flush();
        //...and stash it in memory maybe
        if (store && _announcement && !_reposting)
            announcements.addElement(new Announcement(s));
        }


    /** Prints a message to a given log, 
        with a certain verbosity.  If log==ALL_MESSAGE_LOGS, posted to all logs which accept announcements. 
        If the log is NO_LOGS, nothing is printed.
        @deprecated Verbosity no longer has an effect
    */
    synchronized void println(String s,
        int _verbosity,
        int log,
        boolean _announcement) throws OutputException
        {
        if (log==NO_LOGS) return;
        if (log==ALL_MESSAGE_LOGS) for (int x = 0; x<logs.size();x++)
                                       {
                                       Log l = (Log) logs.elementAt(x);
                                       if (l==null) throw new OutputException("Unknown log number" + l);
                                       println(s,_verbosity,l,_announcement,false);
                                       }
        else
            {
            Log l = (Log) logs.elementAt(log);
            if (l==null) throw new OutputException("Unknown log number" + log);
            println(s,_verbosity,l,_announcement,false);
            }
        }

    /** Prints a message to a given log.  If log==ALL_MESSAGE_LOGS, posted to all logs which accept announcements. 
        If the log is NO_LOGS, nothing is printed.
    */
    public synchronized void println(String s,
        int log,
        boolean _announcement) throws OutputException
        {
        if (log==NO_LOGS) return;
        println(s, V_VERBOSE, log, _announcement);
        }


    /** Prints a non-announcement message to the given logs, 
        with a certain verbosity. 
        If a log is NO_LOGS, nothing is printed to that log.
        @deprecated Verbosity no longer has an effect
    */
    public synchronized void println(String s,
        int _verbosity,
        int[] _logs) throws OutputException
        {
        for(int x=0;x<_logs.length;x++)
            {
            if (_logs[x]==NO_LOGS) break;
            println(s,V_VERBOSE,(Log)(logs.elementAt(_logs[x])),false,false);
            }
        }


    /** Prints a non-announcement message to the given logs, 
        with a certain verbosity. 
        If the log is NO_LOGS, nothing is printed.
        @deprecated Verbosity no longer has an effect
    */
    public synchronized void println(String s,
        int _verbosity,
        int log) throws OutputException
        {
        if (log==NO_LOGS) return;
        println(s,V_VERBOSE,(Log)(logs.elementAt(log)),false,false);
        }


    /** 
        Prints a non-announcement message to the given logs, with a verbosity of V_NO_GENERAL. 
        If the log is NO_LOGS, nothing is printed.
    */
    public synchronized void println(String s,
        int log) throws OutputException
        {
        if (log==NO_LOGS) return;
        println(s,V_VERBOSE,log);
        }


    /** Prints a non-announcement message to a given log, with a 
        certain verbosity. No '\n' is printed.  
        If the log is null, nothing is printed.
    */
    protected synchronized void print(String s,
        int _verbosity,
        Log log) throws OutputException
        {
        if (log==null) return;
        if (log.writer==null) throw new OutputException("Log with a null writer: " + log);
        //if (log.verbosity >= _verbosity) return;  // don't write it
        //if (verbosity >= _verbosity) return;  // don't write it
        if (log.silent) return;  // don't write it
        // now write it
        log.writer.print(s);
        // do not flush until you get a println
        //if (flush) log.writer.flush();
        }

    /** Prints a non-announcement message to a given log, with a
        certain verbosity. If log==ALL_MESSAGE_LOGS, posted to all logs which accept announcements. 
        No '\n' is printed.  
        If the log is NO_LOGS, nothing is printed.
    */
    public synchronized void print(String s,
        int _verbosity,
        int log) throws OutputException
        {
        if (log==NO_LOGS) return;
        if (log==ALL_MESSAGE_LOGS) for (int x = 0; x<logs.size();x++)
                                       {
                                       Log l = (Log) logs.elementAt(x);
                                       if (l==null) throw new OutputException("Unknown log number" + l);
                                       print(s,V_VERBOSE,l);
                                       }
        else
            {
            Log l = (Log) logs.elementAt(log);
            if (l==null) throw new OutputException("Unknown log number" + log);
            print(s,V_VERBOSE,l);
            }
        }

    /** Prints a non-announcement message to a given log<!--, with a verbosity of V_NO_GENERAL-->.
        If log==ALL_MESSAGE_LOGS, posted to all logs which accept announcements. No '\n' is printed.  
        If the log is NO_LOGS, nothing is printed.
    */
    public synchronized void print(String s,
        int log) throws OutputException
        {
        print(s, V_VERBOSE, log);
        }

    /** Prints a non-announcement message to the given logs, 
        with a certain verbosity. No '\n' is printed.  
        If a log is NO_LOGS, nothing is printed to that log.
        @deprecated Verbosity no longer has any effect 
    */
    public synchronized void print(String s,
        int _verbosity,
        int[] _logs) throws OutputException
        {
        for(int x=0;x<_logs.length;x++)
            {
            if (_logs[x]==NO_LOGS) return;
            print(s,_logs[x]);
            }
        }

    /** Prints a non-announcement message to the given logs, 
        with a certain verbosity. No '\n' is printed.  
        If a log is NO_LOGS, nothing is printed to that log.
    */
    public synchronized void print(String s,
        int[] _logs) throws OutputException
        {
        print(s, V_VERBOSE, _logs);
        }

    /** Exits with a fatal error if the error flag has been raised. */
    public synchronized void exitIfErrors()
        {
        if (errors) 
            {
            println(a("SYSTEM EXITING FROM ERRORS\n"), ALL_MESSAGE_LOGS, true);
            exitWithError(this, error.toString(), throwsErrors);
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
            if (l.repostAnnouncementsOnRestart && store)
                {    
                int as = announcements.size();
                for (int y=0;y<as;y++)
                    {
                    Announcement a = (Announcement)(announcements.elementAt(y));
                    println(a.text,V_VERBOSE,l,true,true);
                    }
                }
            }

        // exit with a fatal error if the errors flag is set. 
        exitIfErrors();
        }
        
    /** Returns a compressing input stream using JZLib (http://www.jcraft.com/jzlib/).  If JZLib is not available on your system, this method will return null. */
    public static InputStream makeCompressingInputStream(InputStream in)
        {
        // to do this, we're going to use reflection.  But here's the equivalent code:
        /*
          return new com.jcraft.jzlib.ZInputStream(in);
        */
        try
            {
            return (InputStream)(Class.forName("com.jcraft.jzlib.ZInputStream").getConstructor(new Class[] { InputStream.class } ).newInstance(new Object[] { in }));
            }
        // just in case of RuntimeExceptions
        catch (Exception e) { return null; }  // failed, probably doesn't have JZLib on the system
        }

    /** Returns a compressing output stream using JZLib (http://www.jcraft.com/jzlib/).  If JZLib is not available on your system, this method will return null. */
    public static OutputStream makeCompressingOutputStream(OutputStream out)
        {
        // to do this, we're going to use reflection.  But here's the equivalent code:
        /*
          com.jcraft.jzlib.ZOutputStream stream = new com.jcraft.jzlib.ZOutputStream(out, com.jcraft.jzlib.JZlib.Z_BEST_SPEED);
          stream.setFlushMode(com.jcraft.jzlib.JZlib.Z_SYNC_FLUSH);
          return stream;
        */
        try
            {
            Class outz = Class.forName("com.jcraft.jzlib.JZlib");
            int Z_BEST_SPEED = outz.getField("Z_BEST_SPEED").getInt(null);
            int Z_SYNC_FLUSH = outz.getField("Z_SYNC_FLUSH").getInt(null);
            
            Class outc = Class.forName("com.jcraft.jzlib.ZOutputStream");
            Object outi = outc.getConstructor(new Class[] { OutputStream.class, Integer.TYPE }).newInstance(new Object[] { out, Integer.valueOf(Z_BEST_SPEED) });
            outc.getMethod("setFlushMode", new Class[] { Integer.TYPE }).invoke(outi, new Object[] { new Integer(Z_SYNC_FLUSH) });
            return (OutputStream) outi;
            }
        // just in case of RuntimeExceptions
        catch (Exception e) { return null; } // failed, probably doesn't have JZLib on the system
        }


    static class Announcement implements Serializable
        {
        /** The announcement's...anouncement.*/
        public String text;

        /** Creates a new announcement with text <i>t</i> and verbosity value <i>v</i> */
        public Announcement (String t)
            {
            text = t;
            }
        }

    }
