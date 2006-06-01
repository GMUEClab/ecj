/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec;

/* 
 * Group.java
 * 
 * Created: Tue Aug 10 20:49:45 1999
 * By: Sean Luke
 */

/**
 * Groups are used for populations and subpopulations.  They are slightly
 * different from Prototypes in a few important ways.
 *
 * A Group instance typically is set up with setup(...) and then <i>used</i>
 * (unlike in a Prototype, where the prototype instance is never used, 
 * but only makes clones
 * which are used).  When a new Group instance is needed, it is created by
 * calling emptyClone() on a previous Group instance, which returns a
 * new instance set up exactly like the first Group instance had been set up
 * when setup(...) was called on it.
 *
 * Groups are Serializable and Cloneable, but you should not clone
 * them -- use emptyClone instead.
 *
 *
 * @author Sean Luke
 * @version 1.0 
 */

public interface Group extends Setup, Cloneable
    {
    /** Returns a copy of the object just as it had been 
        immediately after Setup was called on it (or on
        an ancestor object).  You can obtain a fresh instance
        using clone(), and then modify that.
    */
    public Group emptyClone();
    }
