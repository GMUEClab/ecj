/*
Copyright 2006 by Sean Luke
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/


package ec;
import ec.util.Parameter;

/* 
 * Prototype.java
 * 
 * Created: Sat Oct  2 22:04:44 1999
 * By: Sean Luke
 *
 */

/**
 * Prototype classes typically have one or a few <i>prototype instances</i>
 * created during the course of a run.  These prototype instances each get
 * setup(...) called on them.  From then on, all new instances of a Prototype
 * classes are Cloned from these prototype instances.
 *
 * This EC library uses Prototypes a lot.  Individuals are prototypes.
 * Species are prototypes.  Fitness objects are prototypes.  
 * In the GP section, GPNodes and GPTrees are prototypes.
 *
 * The purpose of a prototype is to make it possible to ask classes,
 * determined at run-time by user parameters, to instantiate
 * themselves very many times without using the Reflection library, which 
 * would be very inefficient.
 *
 * Prototypes must be Cloneable, Serializable (through Setup), and of
 * course, Setup.
 *
 * @author Sean Luke
 * @version 1.0 
 */

public interface Prototype extends Cloneable, Setup
    {
    /** Creates a new individual cloned from a prototype,
        and suitable to begin use in its own evolutionary
        context.

        <p>The question here is whether or not this means to perform a 
        "deep" or "light" ("shallow") clone, or something in-between.  
        You may need to deep-clone parts of your object
        rather than simply copying their references, depending
        on the situation:

        <p>
        <ul>
        <li>If you hold objects which are shared with other instances,
        don't clone them.
        <li>If you hold objects which must be unique, clone them.
        <li>If you hold objects which were given to you as a gesture
        of kindness, and aren't owned by you, you probably shouldn't clone
        them.
        <li> DON'T attempt to clone: Singletons, Cliques, or Groups.
        <li>Arrays are not cloned automatically; you may need to
        clone an array if you're not sharing it with other instances.
        Arrays have the nice feature of being copyable by calling clone()
        on them.
        </ul>

        <p><b>Implementations.</b>

        <ul>
        <li>If no ancestor of yours implements protoClone(),
        and you have no need to either (light cloning is fine with you),
        and you are abstract, then you should not declare protoClone().
        
        <li>If no ancestor of yours implements protoClone(),
        and you have no need to either (light cloning is fine with you),
        and you are <b>not</b> abstract, then you should implement
        it as follows:

        <p>
        <tt><pre>
        public Object protoClone() 
        { 
        return super.clone();
        }
        </pre></tt>
        
        <li>If no ancestor of yours implements protoClone(), but you
        need to deep-clone some things, then you should implement it
        as follows:

        <p>
        <tt><pre>
        public Object protoClone() 
        {
        myobj = (MyObject) (super.clone());

        // put your deep-cloning code here...
        // ...you should use protoClone and not 
        // protoCloneSimple to clone subordinate objects...
        return myobj;
        } 
        </pre></tt>

        <li>If you need to override an ancestors' implementation
        of protoClone, in order to do your own deep cloning as well,
        then you should implement it as follows:

        <p>
        <tt><pre>
        public Object protoClone() 
        {
        MyObject myobj = (MyObject)(super.protoClone());

        // put your deep-cloning code here...
        // ...you should use protoClone and not 
        // protoCloneSimple to clone subordinate objects...
        return myobj;
        } 
        </pre></tt>

        </ul>

        <p>If you know that your superclasses will <i>never</i> change
        their protoClone() implementations, you might try inlining them
        in your overridden protoClone() method.  But this is dangerous
        (though it yields a small net increase).

        <p>In general, you want to keep your deep cloning to an absolute
        minimum, so that you don't have to call protoClone() but
        one time.

        <p>The approach taken here is the fastest that I am aware of
        while still permitting objects to be specified at runtime from
        a parameter file.  It would be faster to use the "new" operator;
        but that would require hard-coding that we can't do.  Although
        using java.lang.Object.clone() entails an extra layer that
        deals with stripping away the "protected" keyword and also 
        wrapping the exception handling (which is a BIG hit, about
        three times as slow as using "new"), it's still MUCH faster
        than using java.lang.Class.newInstance(), and also much faster
        than rolling our own Clone() method.
    */

    public Object clone();



    /** Sets up the object by reading it from the parameters stored
        in <i>state</i>, built off of the parameter base <i>base</i>.
        If an ancestor implements this method, be sure to call
        super.setup(state,base);  before you do anything else. 

        <p>For prototypes, setup(...) is typically called once for
        the prototype instance; cloned instances do not receive
        the setup(...) call.  setup(...) <i>may</i> be called
        more than once; the only guarantee is that it will get
        called at least once on an instance or some "parent"
        object from which it was ultimately cloned. */

    public void setup(final EvolutionState state, final Parameter base);

    /** Returns the default base for this prototype.
        This should generally be implemented by building off of the static base()
        method on the DefaultsForm object for the prototype's package. This should
        be callable during setup(...).  */
    public Parameter defaultBase();
    }

