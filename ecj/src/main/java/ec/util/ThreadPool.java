/*
  Copyright 2013 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.util;

import java.util.*;
import java.io.*;

/**
 * ThreadPool.java
 * 
 * A simple, lightweight thread pool, for those who cannot or will not use Java's baroque
 * java.util.concurrent package.
 *
 * <P>A ThreadPool manages a set of "Workers", each of which manages a java.lang.Thread
 * ready to be used to run a task for you.  The threads are setDaemon(true), so they'll
 * automatically die when the program is terminated.   At any point
 * in time, a Worker is either *pooled*, meaning it is in the ThreadPool and available to be
 * used, or it is *outstanding*, meaning that it is presently working on a Runnable that has
 * been assigned it. 
 *
 * <p>You obtain a Worker from a ThreadPool by calling start(...), passing in a Runnable
 * which is the code you wish the Worker to perform.  When this Runnable is completed,
 * that is, when its run() method exits, the Worker will automatically rejoin the ThreadPool
 * and become available for use in future.
 *
 * <p>A Worker manages a Thread underneath in which the Runnable is run. The Worker has a
 * method called interrupt() which you can call if you wish to have interrupt() called on
 * the underlying Thread; otherwise you shouldn't really play around with the underlying
 * thread even if you can obtain it (via Thread.currentThread() for example).  
 *
 * <p>If there are no Workers presently available in the pool when you request one, a new Worker, and
 * an associated underlying thread, will be created on the fly.  When this Worker is done,
 * it will enter the Pool with the others.  Thus the total number of Workers will never shrink,
 * though it may stay the same size.  If you want to trim the number of Workers presently in
 * the Pool, you can call killPooled(), though it's not a common need.
 *
 * <p>You might wish to control the total number of workers at any particular time.  You
 * can do this by using a version of start() which takes a maximum number of workers.  This
 * version will block as long as the number of current working threads is greater than
 * or equal to the desired maximum, then start() afterwards. 
 *
 * <p>You can wait for an outstanding Worker to finish its task by calling join(...).  You can wait for
 * all outstanding Workers to finish their tasks by calling joinAll(...).  This is useful
 * for spawning a number of Workers, then waiting for them to all finish.  And you can
 * wait for all outstanding Workers to finish their tasks, followed by killing them and all
 * their underlying threads (perhaps to clean up in preparation for quitting your program)
 * by calling killAll(...).
 *
 * <p>ThreadPool is java.io.Serializable: but if it is serialized out, it won't serialize
 * out its worker threads, so when it is deserialized back in, the threads will be
 * gone.
 */


public class ThreadPool implements java.io.Serializable
    {
    private static final long serialVersionUID = 1;

    /** A Worker is a special kind of object which represents an underlying
        Worker thread usable in the ThreadPool. */
    public interface Worker { public void interrupt(); }
        
    // The current collection of available threads in the pool
    // (not including the threads presently working on jobs)
    // This object is transient so it's not written out when serialized
    // out, and so when deserialized it becomes null (which we detect).
    // This is important because Thread is not serializable.
    LinkedList workers = new LinkedList();
    Object workersLock = new Object[0];  // arrays are serializable
        
    // The total number of threads which exist, including those
    // in the pool and those outstanding working on jobs
    int totalWorkers = 0;  // resets to 0 on deserialization
        
    private void writeObject(java.io.ObjectOutputStream stream) throws IOException 
        {
        // DO NOTHING
        // This is because we will be rebuilding ALL THREE variables
        // (workers, workersLock, and totalWorkers) during readObject.
        // We can't accomplish this with 'transient' because workersLock has
        // to be actually rebuilt as an object rather than set to null
        // Further, having an empty writeObject method here prevents Java
        // from attempting to serialize these non-transient objects while some
        // thread might be accessing them.
        }

    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
        {
        // REBUILD THE WHOLE INSTANCE
        workers = new LinkedList();
        workersLock = new Object[0];
        totalWorkers = 0;
        }


    /** Start a thread on the given Runnable and returns it. */
    public Worker start(Runnable run) { return start(run, "" + this); }

    /** Start a thread on the given Runnable with a given thread name (for debugging purposes). */
    public Worker start(Runnable run, String name)
        {
        Node node;
        // ensure we have at least one thread
        synchronized(workersLock) 
            {
            // if (workers == null) workers = new LinkedList();  // deserialized
            if (workers.isEmpty())
                {
                node = new Node(name + " (" + totalWorkers + ")");
                node.thread.start();  // build a new thread
                totalWorkers++;
                }
            else  // pull a thread
                {
                node = (Node)(workers.remove());  // removes from end
                }
            }
                        
        // now get the thread running
        synchronized(node) 
            {
            node.setRun(run);
            node.go = true;
            node.notify(); 
            }

        return node;
        }

    /** Start a thread on the given Runnable and returns it. 
        This method blocks and does not start the thread as long
        as doing so would cause the number of outstanding workers to
        exceed the provided maximum number.  This method can be used
        to limit the number of jobs processed by the ThreadPool at
        any one time.  */ 
    public Worker start(Runnable run, int maximumOutstandingWorkers)
        {
        return start(run, maximumOutstandingWorkers, "" + this);
        }

    /** Start a thread on the given Runnable with a given thread name 
        (for debugging purposes) and returns it. 
        This method blocks and does not start the thread as long
        as doing so would cause the number of outstanding workers to
        exceed the provided maximum number.  This method can be used
        to limit the number of jobs processed by the ThreadPool at
        any one time.  */ 
    public Worker start(Runnable run, int maximumOutstandingWorkers, String name)
        {
        synchronized(workersLock)
            {
            // if (workers == null) workers = new LinkedList();  // deserialized
            while (getOutstandingWorkers() >= maximumOutstandingWorkers)  // too many outstanding jobs
                {
                try { workersLock.wait(); }
                catch (InterruptedException e) { Thread.interrupted(); }
                }
            return start(run, name);
            }
        }
        
    /** Returns the total number of workers, both pooled and outstanding (working on something). */
    public int getTotalWorkers()
        {
        synchronized(workersLock) { return totalWorkers; }
        }
                
    /** Returns the total number of pooled workers (those not working on something right now). */
    public int getPooledWorkers()
        {
        synchronized(workersLock) 
            {
            // if (workers == null) workers = new LinkedList();  // deserialized
            return workers.size();
            }
        }
        
    /** Returns the total number of outstanding workers (those working on something right now). */
    public int getOutstandingWorkers()
        {
        synchronized(workersLock) { return getTotalWorkers() - getPooledWorkers(); }
        }
        
    /** Joins the given thread running the given Runnable.  If the thread is not presently running
        this Runnable, then the method immediately returns.  Else it blocks until the Runnable has
        terminated.  Returns true if the worker was working on the provided Runnable, else false. */
    public boolean join(Worker thread, Runnable run)
        {
        return ((Node)thread).joinRunnable(run);
        }
        
    /** If the thread is presently running a Runnable of any kind, blocks until the Runnable has
        finished running.  Else returns immediately.  Returns true if the worker was working
        on some Runnable, else false.  */
    public boolean join(Worker thread)
        {
        return ((Node)thread).joinRunnable();
        }
        
    /** Waits until there are no outstanding workers: all pool workers are in the pool. */
    public void joinAll()
        {
        synchronized(workersLock)
            {
            // if (workers == null) workers = new LinkedList();  // deserialized
            while (totalWorkers > workers.size())  // there are still outstanding workers
                try { workersLock.wait(); }
                catch (InterruptedException e) { Thread.interrupted(); }  // ignore
            }
        }
        
    /** Kills all unused workers in the pool.  This can be used to reduce the pool
        to a manageable size if the number of workers in it has grown too large
        (an unlikely scenario).  You can still use the
        ThreadPool after calling this function; but it will have to build new workers. */
    public void killPooled()
        {
        synchronized(workersLock)
            {
            // if (workers == null) workers = new LinkedList();  // deserialized
            while(!workers.isEmpty())
                {
                Node node = (Node)(workers.remove()); // removes from front
                synchronized(node) { node.die = true; node.notify(); }  // reel it in
                try { node.thread.join(); }
                catch (InterruptedException e) { Thread.interrupted(); } // ignore
                totalWorkers--;
                }
            }
        }
                
    /** Waits until there are no outstanding workers: all pool workers are in the pool. 
        Then kills all the workers.  This is the approprate way to shut down the 
        ThreadPool in preparation for quitting your program.  You can still use the
        ThreadPool after calling this function; but it will have to build new workers. */
    public void killAll()
        {
        synchronized(workersLock)
            {
            joinAll();
            killPooled();
            }
        }
        
    // This is the underlying class for Worker.
    // Not serializable (Thread is not serializable), but it shouldn't
    // be a problem since the worker list is transient.
    class Node implements Runnable, Worker
        {
        private static final long serialVersionUID = 1;

        // have I been asked to die?
        boolean die = false;
        // have I been asked to start working?
        boolean go = false;
                
        // Thread which is running me
        Thread thread;
                
        // My underlying runnable, or null if I'm not doing a job right now
        Runnable toRun = null;
        Object runLock = new Object[0];
                
        Node(String name) 
            {
            thread = new Thread(this); 
            thread.setDaemon(true);
            thread.setName(name);
            }
                
        public void interrupt()
            {
            synchronized(runLock) 
                {
                if (toRun != null)  // don't interrupt a thread that's pooled
                    thread.interrupt();
                }
            }

        // Sets the runnable to a new job and notifies any waiting processes trying to join on the old thread
        void setRun(Runnable r)
            {
            synchronized(runLock) 
                { 
                toRun = r; 
                runLock.notifyAll(); 
                } 
            }
                        
        // joins on the current runnable if any
        boolean joinRunnable()
            {
            synchronized(runLock)
                {
                if (toRun != null)
                    return joinRunnable(toRun);
                else return false;
                }
            }
                
        // joins on a current runnable if it is running
        boolean joinRunnable(Runnable r)
            {
            synchronized(runLock)
                {
                if (toRun == r)
                    {
                    try { runLock.wait(); }
                    catch (InterruptedException e) { }
                    return true;
                    }
                else 
                    {
                    return false;
                    }
                }
            }
                
        public void run()  // assumes run is non-null
            {
            while(true)
                {
                synchronized(this) 
                    {
                    while(!go)
                        {
                        if (die) { die = false; return; }
                        try { wait(); }  // wait for a new job to work on, or a request to die
                        catch (InterruptedException e) { Thread.interrupted(); } // ignore
                        }
                    go = false;
                    }
                                
                try
                    {
                    toRun.run();  // do the job
                    }
                catch (Exception e) { e.printStackTrace(); Thread.interrupted(); } // resets interrupted flag.  Note ANY exception.

                // add myself back in the list
                synchronized(workersLock)
                    {
                    synchronized(runLock)
                        {
                        // if (workers == null) workers = new LinkedList();  // deserialized
                        workers.add(this);  // adds at end
                                                
                        if (totalWorkers == workers.size())  // we're all in the bag, let the pool know if it's joining
                            workersLock.notify();  // let joinAll know someone's back in the pool
                        toRun = null;
                        runLock.notifyAll(); // let joinRunnable know I'm done
                        }
                    }
                }
            }
        }
        
    public static void main(String[] args)
        {
        ThreadPool p = new ThreadPool();
                
        Runnable[] runs = new Runnable[1000];
        Worker[] workers = new Worker[1000];
                
        while(true)
            {
            for(int x = 0; x < 1000; x++)
                {
                workers[x] = p.start( 
                    runs[x] = new Runnable() { public void run() { try { Thread.currentThread().sleep(5000); }  
                            catch(InterruptedException e) { } } } );
                }
            for(int y = 0; y < 1000; y++)
                {
                System.err.println("joining " + y);
                p.join(workers[y], runs[y]);
                System.err.println("joined");
                }
            }
        }
    }
