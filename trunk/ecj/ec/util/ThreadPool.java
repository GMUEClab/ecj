package ec.util;
import java.util.*;

/**
   An ultralightweight, simple pool for threads.  All threads are daemon threads, and you should
   not start() them.  Instead, you should use the two provided methods in
   the pool, startThread() and joinAndReturnThread().  You may join() the thread only
   if you don't plan on handing it back into the ThreadPool via joinAndReturnThread()
   (we don't recommend it).
        
   <p>Java 5.0 has a concurrency package with a heavyweight thread pool.  Why not use it?
   Mostly to allow code which is still compatable with 1.4, that's all.
**/

public class ThreadPool
    {
    // This list holds all the unused threads in the pool
    LinkedList list = new LinkedList();
        
        
    // These are our threads.  They have a run() method which handles the outer loop of
    // waiting for a new runnable, running it, then informing outsiders that we're finished
    class PoolThread extends Thread
        {
        static final int STATE_STARTING = 0;    // The thread had been requested to start (but may not have yet)
        static final int STATE_RUNNING = 1;             // The thread is running or about to
        static final int STATE_FINISHED = 2;    // The thread has finished running and hasn't been asked to start again yet
        int state;
                
        Object lock = new Object[0];
        Runnable runnable = null;                               // The thing the thread is going to run
                
        public PoolThread()
            {
            super();
            this.setDaemon(true);                           // we make sure the thread dies when the 
            state = STATE_FINISHED;
            start();
            }
                
        // can't create a Runnable because of Java's weird restrictions on the location of super(...).
        // so I have to override run()
        public void run()
            {
            while(true)
                {
                synchronized(lock)
                    {
                    while (state != STATE_STARTING)                 // wait until we're STATE_STARTING (and we thus have a runnable to run)
                        {
                        try { lock.wait(); }
                        catch (InterruptedException e) { }
                        }
                    state = STATE_RUNNING;                                  // let others know we're about to start running this sucker.
                    lock.notifyAll();                                               // this is probably unnecessary
                    }
                runnable.run();                                                         // run the user's code
                synchronized(lock)
                    {
                    state = STATE_FINISHED;                                 // let others know we've finished
                    runnable = null;                                                // let GC
                    lock.notifyAll();
                    }
                }
            }
                
        // called to start up the ThreadPool on a provided runnable
        void go(Runnable run)
            {
            synchronized(lock) 
                {
                runnable = run;
                state = STATE_STARTING;                                 // let the run() method know that we're ready to start
                lock.notifyAll();
                }
            }
                        
        // called to "join", so to speak, on the ThreadPool and get it ready for the next runnable
        void finish()
            {
            synchronized(lock) 
                {
                while (state != STATE_FINISHED)                 // wait until we're STATE_FINISHED
                    {
                    try { lock.wait(); }
                    catch (InterruptedException e) { }
                    }
                }
            }
        }
                
    /** Starts and provides a thread on the given runnable, with the provided name.
    	If no thread was avaialble in the pool, creates a new one. */ 
    public Thread startThread(String name, Runnable run)
    	{
        if (run == null)
            throw new RuntimeException("Request to start a thread on a null runnable");
        PoolThread p = null;
        synchronized(list)
            {
            if (!list.isEmpty())
                p = (PoolThread)(list.removeLast());    // use the hot thread if you can
            }
        if (p == null)
            p = new PoolThread();   // couldn't get one from list
        if (name == null) name = "";
        p.setName(name);
        p.go(run);
        return p;
    	}
    	
    /** Starts and provides a thread on the given runnable.  If no thread was avaialble in the pool, creates a new one. */ 
    public Thread startThread(Runnable run)
        {
        return startThread(null, run);
        }
        

    /** Waits until the thread has finished the user's runnable, then adds it back to the pool to await further use.
        You cannot submit a thread via this method which was not provided via startThread() -- it'll generate an error. */ 
    public void joinAndReturn(Thread thread)
        {
        if (thread != null && thread instanceof PoolThread)
            {
            PoolThread p = (PoolThread) thread;
            p.finish();
            synchronized(list)
                {
                list.add(p);                                                    // this adds to the end, so the end is the hot thread
                }
            }
        else if (thread == null)
            throw new RuntimeException("Thread is null.");
        else
            throw new RuntimeException("Thread was not produced by ThreadPool: " + thread);
        }
    }
        
        
        