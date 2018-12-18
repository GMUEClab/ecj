/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.neat;

import ec.*;
import ec.util.*;

/**
 * NEATInnovation is a class for recording the innovation information during the
 * evolution of neat. This information is critical to determine if two
 * individuals have same origin. There a basic two types of innovation we want
 * to keep track of, adding a node or adding a gene (link) to the individual.
 * Different innovation require record different information.
 * 
 * @author Ermo Wei and David Freelan
 *
 */

public class NEATInnovation implements Prototype
    {
    public final static String P_INNOVATION = "innovation";

    /** Either NEWNODE (0) or NEWLINK (1). */
    public int innovationType;

    /**
     * Two nodes specify where the link innovation took place : this is the
     * input node.
     */
    public int inNodeId;

    /**
     * Two nodes specify where the link innovation took place : this is the
     * output node.
     */
    public int outNodeId;

    /** The number assigned to the innovation. */
    public int innovationNum1;

    /**
     * If this is a new node innovation,then there are 2 innovations (links)
     * added for the new node.
     */
    public int innovationNum2;

    /** If a link is added, this is its weight. */
    public double newWeight;

    /** If a new node was created, this is its node id. */
    public int newNodeId;

    /**
     * If a new node was created, this is the innovation number of the gene's
     * link it is being stuck inside.
     */
    public int oldInnovationNum;

    /** Is the link innovation a recurrent link. */
    public boolean recurFlag;

    @Override
    public void setup(EvolutionState state, Parameter base)
        {
        innovationType = 0;
        inNodeId = 0;
        outNodeId = 0;
        innovationNum1 = 0;
        innovationNum2 = 0;
        newNodeId = 0;
        oldInnovationNum = 0;
        newWeight = 0;
        recurFlag = false;
        }

    @Override
    public Parameter defaultBase()
        {
        return NEATDefaults.base().push(P_INNOVATION);
        }

    /**
     * When we have a new innovation, we clone an existing NEATInnovation
     * instance, and change its information with this reset
     * method.
     */
    public void reset(int inNode, int outNode, int innovNum1, int innovNum2, int newId, int oldInnov)
        {
        innovationType = 0;
        inNodeId = inNode;
        outNodeId = outNode;
        innovationNum1 = innovNum1;
        innovationNum2 = innovNum2;
        newNodeId = newId;
        oldInnovationNum = oldInnov;

        // unused parameters set to zero
        newWeight = 0;
        recurFlag = false;
        }

    /**
     * When we have a new innovation, we clone an existing NEATInnovation
     * instance, and change its information with this reset
     * method.
     */
    public void reset(int inNode, int outNode, int oldInnov)
        {
        innovationType = 0;
        inNodeId = inNode;
        outNodeId = outNode;
        oldInnovationNum = oldInnov;

        // unused parameters set to zero
        innovationNum1 = 0;
        innovationNum2 = 0;
        newNodeId = 0;
        newWeight = 0;
        recurFlag = false;
        }

    /**
     * When we have a new innovation, we clone an existing NEATInnovation
     * instance, and change its information with this reset
     * method.
     */
    public void reset(int inNode, int outNode, int innovNum, double weight, boolean recur)
        {
        innovationType = 1;
        inNodeId = inNode;
        outNodeId = outNode;
        innovationNum1 = innovNum;
        newWeight = weight;
        recurFlag = recur;

        // unused parameters set to zero
        innovationNum2 = 0;
        oldInnovationNum = 0;
        newNodeId = 0;
        }

    /**
     * When we have a new innovation, we clone an existing NEATInnovation
     * instance, and change its information with this reset
     * method.
     */
    public void reset(int inNode, int outNode, boolean recur)
        {
        innovationType = 1;
        inNodeId = inNode;
        outNodeId = outNode;
        recurFlag = recur;

        // unused parameters set to zero
        innovationNum1 = 0;
        newWeight = 0;
        innovationNum2 = 0;
        oldInnovationNum = 0;
        newNodeId = 0;

        }

    public Object clone()
        {
        NEATInnovation myobj = null;
        try
            {
            myobj = (NEATInnovation) (super.clone());
            myobj.innovationType = innovationType;
            myobj.inNodeId = inNodeId;
            myobj.outNodeId = outNodeId;
            myobj.innovationNum1 = innovationNum1;
            myobj.innovationNum2 = innovationNum2;
            myobj.newWeight = newWeight;
            myobj.newNodeId = newNodeId;
            myobj.oldInnovationNum = oldInnovationNum;
            myobj.recurFlag = recurFlag;
            } catch (CloneNotSupportedException e) // never happens
            {
            throw new InternalError();
            }
        return myobj;
        }

    @Override
    public int hashCode()
        {
        int result = innovationType;
        result = result * 31 + 17 + inNodeId;
        result = result * 31 + 17 + outNodeId;
        result = result * 31 + 17 + oldInnovationNum;
        if (recurFlag)
            result = result + 13;

        return result;
        }

    @Override
    public boolean equals(Object obj)
        {
        NEATInnovation inno = (NEATInnovation) obj;
        if (innovationType != inno.innovationType)
            return false;
        if (inNodeId != inno.inNodeId)
            return false;
        if (outNodeId != inno.outNodeId)
            return false;
        if (oldInnovationNum != inno.oldInnovationNum)
            return false;
        return recurFlag == inno.recurFlag;
        }

    }
