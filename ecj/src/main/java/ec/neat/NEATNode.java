/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.neat;

import java.io.*;
import java.text.*;
import java.util.*;
import ec.*;
import ec.util.*;

/**
 * NEATNode is the class to represent node in network, it stores status of the
 * node in that network. A Node is either a NEURON or a SENSOR. If it's a
 * sensor, it can be loaded with a value for output. If it's a neuron, it has a
 * list of its incoming input signals. Based on the position of the node in
 * network, we have output, input, bias and hidden nodes. We use INPUT nodes to
 * load inputs, and get output from OUTPUT nodes.
 * 
 * @author Ermo Wei and David Freelan
 */

public class NEATNode implements Prototype
    {

    /**
     * The type of a node. A node could be a sensor node, where the input get
     * loaded in, or a neuron node, where activation is triggered.
     */
    public enum NodeType
        {
        NEURON, SENSOR
        }

    /** The place this node could be. */
    public enum NodePlace
        {
        HIDDEN, INPUT, OUTPUT, BIAS
        }

    /** The activation function is used in for hidden node. */
    public enum FunctionType
        {
        SIGMOID
        }

    public static final String P_NODE = "node";

    /** Keeps track of which activation the node is currently in. */
    public int activationCount;

    /** Holds the previous step's activation for recurrence. */
    public double lastActivation;

    /**
     * Holds the activation BEFORE the previous step's This is necessary for a
     * special recurrent case when the inNode of a recurrent link is one time
     * step ahead of the outNode. The innode then needs to send from TWO time
     * steps ago.
     */
    public double previousLastActivation;

    /**
     * Indicates if the value of current node has been override by method other
     * than network's activation.
     */
    public boolean override;

    /**
     * Contains the activation value that will override this node's activation.
     */
    public double overrideValue;

    /** When it's true, the node cannot be mutated. */
    public boolean frozen;

    /**
     * The activation function, use sigmoid for default, but can use some other
     * choice, like ReLU.
     */
    public FunctionType functionType;

    /** Distinguish the Sensor node or other neuron node. */
    public NodeType type;

    /** Distinguish the input node, hidden or output node. */
    public NodePlace geneticNodeLabel;

    /** The incoming activity before being processed. */
    public double activeSum;

    /** The total activation entering the node. */
    public double activation;

    /** To make sure outputs are active. */
    public boolean activeFlag;

    /**
     * A list of incoming links, it is used to get activation status of the
     * nodes on the other ends.
     */
    public ArrayList<NEATGene> incomingGenes;

    /** Node id for this node. */
    public int nodeId;

    /**
     * The depth of current node in current network, this field is used in
     * counting max depth in a network.
     */
    public int innerLevel;

    /** Indicate if this node has been traversed in max depth counting. */
    public boolean isTraversed;

    public void setup(EvolutionState state, Parameter base)
        {
        activationCount = 0;
        lastActivation = 0;
        previousLastActivation = 0;
        override = false;
        overrideValue = 0;
        frozen = false;
        // TODO : could be extend to use some other activation function
        functionType = FunctionType.SIGMOID;
        type = NodeType.NEURON;
        geneticNodeLabel = NodePlace.HIDDEN;
        activeSum = 0;
        activation = 0;
        activeFlag = false;
        incomingGenes = new ArrayList<NEATGene>();
        nodeId = 0;
        innerLevel = 0;
        isTraversed = false;
        }

    public Parameter defaultBase()
        {
        return NEATDefaults.base().push(P_NODE);
        }

    /** Reset the node to initial status. */
    public void reset(NodeType nodeType, int id, NodePlace placement)
        {
        // NNode::NNode(nodetype ntype,int nodeid, nodeplace placement)
        nodeId = id;
        activeFlag = false;
        activeSum = 0;
        activation = 0;
        lastActivation = 0;
        previousLastActivation = 0;
        type = nodeType; // NEURON or SENSOR type
        activationCount = 0; // Inactive upon creation
        functionType = FunctionType.SIGMOID;
        geneticNodeLabel = placement;
        frozen = false;
        override = false;
        overrideValue = 0;
        innerLevel = 0;
        isTraversed = false;
        }

    /**
     * Return a clone of this node, but with a empty incomingGenes list.
     */
    public Object emptyClone()
        {
        NEATNode myobj = (NEATNode) clone();
        myobj.incomingGenes = new ArrayList<NEATGene>();

        return myobj;
        }

    public Object clone()
        {
        // NNode::NNode(NNode *n,Trait *t)
        NEATNode myobj = null;
        try
            {
            myobj = (NEATNode) (super.clone());

            myobj.nodeId = nodeId;
            myobj.type = type;
            myobj.geneticNodeLabel = geneticNodeLabel;
            myobj.activationCount = 0;
            myobj.lastActivation = 0;
            myobj.previousLastActivation = 0;
            myobj.override = false;
            myobj.overrideValue = 0;
            myobj.frozen = false;
            myobj.functionType = FunctionType.SIGMOID;
            myobj.activeSum = 0;
            myobj.activation = 0;
            myobj.activeFlag = false;
            myobj.isTraversed = false;
            myobj.innerLevel = 0;
            } catch (CloneNotSupportedException e) // never happens
            {
            throw new InternalError();
            }
        return myobj;
        }

    @Override
    public boolean equals(Object obj)
        {
        NEATNode n = (NEATNode) obj;
        if (nodeId != n.nodeId)
            return false;

        for(int i = 0; i< incomingGenes.size(); i++)
            {
            if(!n.incomingGenes.get(i).equals(incomingGenes.get(i)))
                return false;
            }
        return true;
        }

    @Override
    public int hashCode()
        {
        int result = nodeId;
        for(int i = 0; i< incomingGenes.size(); i++)
            {
            // this is probably sufficient
            result = (result * 31 + 17 + incomingGenes.get(i).hashCode());
            }
        return result;
        }

    /**
     * Old flush code, used in C++ version. Put all the field into initial
     * status, this is useful in flushing the whole network.
     */
    public void flushBack()
        {
        if (type != NodeType.SENSOR)
            {
            // SENSOR Node do not need to flush recursively
            if (activationCount > 0)
                {
                activationCount = 0;
                activation = 0;
                lastActivation = 0;
                previousLastActivation = 0;
                }
            for (int i = 0; i < incomingGenes.size(); ++i)
                {
                NEATGene link = incomingGenes.get(i);
                if (link.inNode.activationCount > 0)
                    {
                    // NOTE : in here we have the add_weight field clear code
                    // for hebbian learning,
                    // we ignore it here since we are not using it
                    link.inNode.flushBack();
                    }
                }
            }
        else
            {
            // Flush the SENSOR
            activationCount = 0;
            activation = 0;
            lastActivation = 0;
            previousLastActivation = 0;
            }
        }

    /**
     * Put all the field into initial status, this is useful in flushing the
     * whole network.
     */
    public void flush()
        {
        activationCount = 0;
        activation = 0;
        lastActivation = 0;
        previousLastActivation = 0;

        // FIXME: jneat code seems have a lot of redundant here
        }

    /** Return the activation status of this node. */
    public double getActivation()
        {
        if (activationCount > 0)
            return activation;
        return 0.0;
        }

    /** Return the last step activation if this node is active at last step. */
    public double getTimeDelayActivation()
        {
        if (activationCount > 1)
            return lastActivation;
        return 0.0;
        }

    /** Set activation to the override value and turn off override. */
    public void activateWithOverride()
        {
        activation = overrideValue;
        override = false;
        }

    /** Force an output value on the node. */
    public void overrideOutput(double newOutput)
        {
        overrideValue = newOutput;
        override = true;
        }

    /**
     * Clear in incomgin links of this node, this is useful in create a new
     * network from current genotype.
     */
    public void clearIncoming()
        {
        incomingGenes.clear();
        }

    /** Return the depth of this node in the network. */
    public int depth(int d, NEATNetwork network, int maxDepth)
        {
        if (d > 100)
            {
            // original code use these number in code, need to find a good way
            // to justify these
            return 10;
            }

        // Base case
        if (this.type == NodeType.SENSOR)
            {
            return d;
            }

        d++;

        // recursion
        int curDepth = 0; // The depth of current node
        for (int i = 0; i < incomingGenes.size(); ++i)
            {
            NEATNode node = incomingGenes.get(i).inNode;
            if (!node.isTraversed)
                {
                node.isTraversed = true;
                curDepth = node.depth(d, network, maxDepth);
                node.innerLevel = curDepth - d;
                }
            else
                curDepth = d + node.innerLevel;

            maxDepth = Math.max(curDepth, maxDepth);
            }
        return maxDepth;

        }

    /**
     * Reads a Node printed by printNode(...). The default form simply reads a
     * line into a string, and then calls readNodeFromString() on that line.
     */
    public void readNode(EvolutionState state, LineNumberReader reader) throws IOException
        {
        // NNode::NNode (const char *argline, std::vector<Trait*> &traits)
        readNodeFromString(reader.readLine(), state);
        }

    /**
     * This method is used to read a node in start genome from file.
     */
    public void readNodeFromString(String string, EvolutionState state)
        {
        DecodeReturn dr = new DecodeReturn(string);
        Code.decode(dr);
        nodeId = (int) dr.l;
        Code.decode(dr);
        int nType = (int) dr.l;
        Code.decode(dr);
        int nPlace = (int) dr.l;

        type = NodeType.values()[nType];
        geneticNodeLabel = NodePlace.values()[nPlace];

        override = false;
        activeSum = 0;
        frozen = false;
        }

    /**
     * This method convert the gene in to human readable format. It can be
     * useful in debugging.
     */
    public String toString()
        {
        StringBuffer stringBuffer = new StringBuffer();
        String maskf = " #,##0";
        DecimalFormat fmtf = new DecimalFormat(maskf);



        if (type == NodeType.SENSOR)
            stringBuffer.append("\n (Sensor)");
        if (type == NodeType.NEURON)
            stringBuffer.append("\n (Neuron)");

        stringBuffer.append(fmtf.format(nodeId));

        String mask5 = " #,##0.000";
        fmtf = new DecimalFormat(mask5);

        stringBuffer.append( " activation count " + fmtf.format(activationCount) + " activation="
            + fmtf.format(activation) + ")");

        return stringBuffer.toString();
        }

    /**
     * This method is used to output a gene that is same as the format in start
     * genome file.
     */
    public String printNodeToString()
        {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(Code.encode(nodeId));
        stringBuilder.append(Code.encode(type.ordinal()));
        stringBuilder.append(Code.encode(geneticNodeLabel.ordinal()));

        return stringBuilder.toString();
        }

    /** The Sigmoid function. */
    public void sigmoid(double slope)
        {
        
        // constant is not used for non shifted steepened
        activation = 1.0 / (1.0 + Math.exp(-(slope * activeSum)));
        }

    /** If this node is a sensor node, load this node with the given input */
    public boolean sensorLoad(double val)
        {
        if (type == NodeType.SENSOR)
            {
            // Time delay memory
            previousLastActivation = lastActivation;
            lastActivation = activation;

            activationCount++;
            activation = val;
            return true;
            }

        return false;
        }

    }
