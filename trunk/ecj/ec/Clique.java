/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec;

/* 
 * Clique.java
 * 
 * Created: Wed Oct 13 15:12:23 1999
 * By: Sean Luke
 */

/**
 * Clique is a class pattern marking classes which 
 * create only a few instances, generally accessible through
 * some global mechanism, and every single
 * one of which gets its own distinct setup(...) call.  Cliques should
 * <b>not</b> be Cloneable, but they are Serializable.
 *
 * <p>All Cliques presently in ECJ rely on a central repository which
 * stores members of that Clique for easy access by various objects.
 * This repository typically includes a hashtable of the Clique members,
 * plus perhaps one or more arrays of the members stored in different
 * fashions.  Originally these central repositories were stored as static
 * members of the Clique class; but as of ECJ 13 they have been moved
 * to be instance variables of certain Initializers.  For example,
 * GPInitializer holds the repositories for the GPFunctionSet, GPType,
 * GPNodeConstraints, and GPTreeConstraints cliques.  Likewise,
 * RuleInitializer holds the repository for the RuleConstraints clique.
 * 
 * <p>This change was made to facilitate making ECJ modular; we had to remove
 * all non-final static members.  If you make your own Clique, its repository
 * (if you have one) doesn't have to be in an Initializer, but it's a 
 * convenient location.
 *
 * @author Sean Luke
 * @version 1.0 
 */

public interface Clique extends Setup
    {
    }
