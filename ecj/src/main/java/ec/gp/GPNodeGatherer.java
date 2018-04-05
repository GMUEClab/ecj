/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp;
import java.io.Serializable;

/* 
 * GPNodeGatherer.java
 * 
 * Created: Fri Nov  5 17:01:13 1999
 * By: Sean Luke
 */

/**
 * GPNodeGatherer is a small container object for the GPNode.nodeInPosition(...)
 * method and GPNode.numNodes(...) method. 
 * It may be safely reused without being reinitialized.
 *
 * @author Sean Luke
 * @version 1.0 
 */

public abstract class GPNodeGatherer implements Serializable
    {
    // used internally by GPNode
    GPNode node;

    /** Returns true if thisNode is the kind of node to be considered in the
        gather count for nodeInPosition(...) and GPNode.numNodes(GPNodeGatherer).
        The default form simply returns true.  */
    public boolean test(final GPNode thisNode) { return true; }
    }
