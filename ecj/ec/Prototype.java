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
 * <p>The purpose of a prototype is to make it possible to ask classes,
 * determined at run-time by user parameters, to instantiate
 * themselves very many times without using the Reflection library, which 
 * would be very inefficient.
 *
 * <p>ECJ makes extensive use of Prototypes.  Individuals are prototypes.
 * Species are prototypes.  Fitness objects are prototypes.  Breeding
 * pipelines and selection methods are prototypes.  In the GP section, 
 * GPNodes and GPTrees are prototypes.  In the Rule section, Rulesets and
 * Rules are prototypes.  In the Vector section, VectorGenes are prototypes.
 * And so on.
 *
 * <p>ECJ uses Prototypes almost exclusively instead of calling <tt>new</tt>.
 * This is because <tt>new</t> requires that you know, in your code, the exact
 * class of the object to be created.  Doing so programmatically essentially
 * precludes being able to set up object graphs dynamically from parameter files.
 * 
 * <p>Sadly, clone() is rather slower than calling <tt>new</tt>.  However
 * it <i>is</i> a lot faster than calling java.lang.Class.newInstance(),
 * and somewhat faster than rolling our own "cloner" method.
 *
 * <p>Prototypes must be Cloneable, Serializable (through Setup), and of
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

        <p>Typically this should be a full "deep" clone.
        However, you may share certain elements with other objects
        rather than clone hem, depending on the situation:

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
        <li>If no ancestor of yours implements clone(),
        and you have no need to do clone deeply,
        and you are abstract, then you should not declare clone().
        
        <li>If no ancestor of yours implements clone(),
        and you have no need to do clone deeply,
        and you are <b>not</b> abstract, then you should implement
        it as follows:

        <p>
        <tt><pre>
        * public Object clone() 
        *     {
        *     try
        *         { 
        *         return super.clone();
        *         }
        *     catch ((CloneNotSupportedException e)
        *         { throw new InternalError(); } // never happens
        *     }
        </pre></tt>
        
        <li>If no ancestor of yours implements clone(), but you
        need to deep-clone some things, then you should implement it
        as follows:

        <p>
        <tt><pre>
        * public Object clone() 
        *     {
        *     try
        *         { 
        *         MyObject myobj = (MyObject) (super.clone());
        *
        *         // put your deep-cloning code here...
        *         }
        *     catch ((CloneNotSupportedException e)
        *         { throw new InternalError(); } // never happens
        *     return myobj;
        *     } 
        </pre></tt>

        <li>If an ancestor has implemented clone(), and you also need
        to deep clone some things, then you should implement it as follows:

        <p>
        <tt><pre>
        * public Object clone() 
        *     { 
        *     MyObject myobj = (MyObject) (super.clone());
        *
        *     // put your deep-cloning code here...
        *
        *     return myobj;
        *     } 
        </pre></tt>
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

