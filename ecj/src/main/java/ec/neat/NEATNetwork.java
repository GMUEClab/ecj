/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.neat;

import java.util.*;
import ec.*;
import ec.neat.NEATNode.*;
import ec.util.*;

/**
 * NEATNetwork is the phenotype of NEATIndividual. It share the same copy of
 * nodes and genes (links) with its corresponding NEATIndividual. This class
 * handles all operations that is critical in evaluation of the individuals.
 * 
 * @author Ermo Wei and David Freelan
 *
 */
public class NEATNetwork implements Prototype
    {
    public static final String P_NETWORK = "network";

    /** constant used for the sigmoid function */
    public static final double SIGMOID_SLOPE = 4.924273;

    /** The neat individual we belong to */
    public NEATIndividual individual;

    /** A list of all nodes for this network. */
    public ArrayList<NEATNode> nodes;

    /** A list of input nodes for this network. */
    public ArrayList<NEATNode> inputs;

    /** A list of output nodes for this network. */
    public ArrayList<NEATNode> outputs; 

    public void setup(EvolutionState state, Parameter base)
        {
        // create the arraylist
        nodes = new ArrayList<NEATNode>();
        inputs = new ArrayList<NEATNode>();
        outputs = new ArrayList<NEATNode>();
        }
        
    @Override
    public Parameter defaultBase()
        {
        return NEATDefaults.base().push(P_NETWORK);
        }

    public Object clone()
        {
        NEATNetwork myobj = null;
        try
            {
            myobj = (NEATNetwork) (super.clone());
            myobj.nodes = new ArrayList<NEATNode>();
            for(int i = 0; i < nodes.size(); i++)
                myobj.nodes.add((NEATNode)(nodes.get(i).clone()));
            myobj.inputs = new ArrayList<NEATNode>();
            for(int i = 0; i < inputs.size(); i++)
                myobj.inputs.add((NEATNode)(inputs.get(i).clone()));
            myobj.outputs = new ArrayList<NEATNode>();
            for(int i = 0; i < outputs.size(); i++)
                myobj.outputs.add((NEATNode)(outputs.get(i).clone()));
            } 
        catch (CloneNotSupportedException e)
            {
            throw new InternalError();
            } // never happens
        return myobj;
        }


    @Override
    public boolean equals(Object obj)
        {
        if (obj == null)
            return false;

        if(this==obj)
            return true;

        NEATNetwork ind  = (NEATNetwork) obj;
        //if the nodes or incoming and outgoing are different, they are different networks
        if(ind.nodes.size() != this.nodes.size() || ind.inputs.size() !=  this.inputs.size() || ind.outputs.size() != this.outputs.size())
            return false;
        for(int i = 0; i<ind.nodes.size(); i++)
            {
            if(!ind.nodes.get(i).equals(this.nodes.get(i)))
                return false;
            }

        for(int i = 0; i<ind.inputs.size(); i++)
            {
            if(!ind.inputs.get(i).equals(this.inputs.get(i)))
                return false;
            }

        for(int i = 0; i<ind.outputs.size(); i++)
            {
            if(!ind.outputs.get(i).equals(this.outputs.get(i)))
                return false;
            }

        return true;

        }


    public void flush()
        {
        for (int i = 0; i < nodes.size(); ++i)
            {
            NEATNode node = nodes.get(i);
            node.flush();
            }
        }

    /**
     * Activates the net such that all outputs are active.
     */
    public void activate(EvolutionState state)
        {
        // Keep activating until all the outputs have become active
        // (This only happens on the first activation, because after that they
        // are always active)

        boolean oneTime = false; // Make sure we at least activate once
        int abortCounter = 0;  // Used in case the output is somehow truncated
        // from the network

        // make sure all the output are activated, abortCounter make sure it
        // won't go into infinite loop
        while (outputOff() || !oneTime)
            {
            abortCounter++;
           
            if (abortCounter >= ((NEATSpecies)(individual.species)).maxNetworkDepth)
                {
                state.output.fatal("Inputs disconnected from output!");
                }

            // For each node, compute the sum of its incoming activation
            for (int i = 0; i < nodes.size(); ++i)
                {
                // Ignore SENSOR
                NEATNode node = nodes.get(i);
                if (node.type != NodeType.SENSOR)
                    {
                    node.activeSum = 0.0;
                    
                    node.activeFlag = false; // This will tell us if it has any
                    // active inputs
                    // For each incoming connection, add the activity from the
                    // connection to the activeSum
                    ArrayList<NEATGene> incomingLinks = node.incomingGenes;
                    for (int j = 0; j < incomingLinks.size(); ++j)
                        {
                        NEATGene link = incomingLinks.get(j);
                       
                        // Handle possible time delays
                        if (!link.timeDelay)
                            {
                            double amount = link.weight * link.inNode.getActivation();
                            // NOTE: why only set activeFlag to true in here?
                            // need better explanation

                            if (link.inNode.activeFlag || link.inNode.type == NodeType.SENSOR)
                                node.activeFlag = true;
                            node.activeSum += amount;
                            }
                        else
                            {
                            double amount = link.weight * link.inNode.getTimeDelayActivation();
                            node.activeSum += amount;
                            }
                        
                        }
                    }
                }

            // Now activate all the non-sensor nodes off their incoming
            // activation
            for (int i = 0; i < nodes.size(); ++i)
                {
                // Ignore SENSOR
                NEATNode node = nodes.get(i);
                if (node.type != NodeType.SENSOR)
                    {
                    // Only activate if some active input came in
                    if (node.activeFlag)
                        {
                        // Keep a memory of activations for potential time
                        // delayed connections
                        node.previousLastActivation = node.lastActivation;
                        node.lastActivation = node.activation;

                        // Now run the net activation through an activation
                        // function
                        if (node.functionType == FunctionType.SIGMOID)
                            {
                            node.sigmoid(SIGMOID_SLOPE);
                            }

                        // Increment the activationCount
                        // First activation cannot be from nothing!!
                        node.activationCount++;
                        }
                    }
                }

            oneTime = true;
            }

        // NOTE: there is adaptation code here in original code, however, for
        // default settings, it should not be used
        // since it have traits
        // see bool Network::activate()
        }



    /** Add a new input node. */
    public void addInput(NEATNode node)
        {
        inputs.add(node);
        }

    /** Add a new output node. */
    public void addOutput(NEATNode node)
        {
        outputs.add(node);
        }

    /** Takes an array of sensor values and loads it into SENSOR inputs ONLY. */
    public void loadSensors(double[] vals)
        {
        int counter = 0;
        for (int i = 0; i < inputs.size(); ++i)
            {
            // only load values into SENSORS (not BIASes)
            if (inputs.get(i).type == NodeType.SENSOR)
                {
                inputs.get(i).sensorLoad(vals[counter++]);
                }
            }
        }

    /** Produces an array of activation results, one per output node. */
    public double[] getOutputResults()
        {
        double[] results = new double[outputs.size()];
        for(int i = 0; i < results.length; i++)
            results[i] = outputs.get(i).activation;
        return results;
        }

    /**
     * This checks a POTENTIAL link between start from fromNode to toNode to use
     * count and threshold to jump out in the case of an infinite loop.
     */
    public static boolean[] hasPath(EvolutionState state, NEATNode toNode, NEATNode fromNode, int threshold)
        {
        boolean[] results = new boolean[2];
        int level = 0;
        HashSet<NEATNode> set = new HashSet<NEATNode>(); // for keeping track of the visiting nodes
        hasPath(state, toNode, fromNode, set, level, threshold, results);
        return results;
        }

    /** The helper function to check if there is a path from fromNode to toNode. */
    public static void hasPath(EvolutionState state, NEATNode toNode, NEATNode fromNode, HashSet<NEATNode> set, int level,
        int threshold, boolean[] results)
        {
        if (level > threshold)
            {
            // caught in infinite loop
            results[0] = false;
            results[1] = false;
            return;
            }

        if (toNode.nodeId == fromNode.nodeId)
            {
            results[0] = true;
            results[1] = true;
            }
        else
            {
            // Check back on all links...
            // But skip links that are already recurrent
            // (We want to check back through the forward flow of signals only
            for (int i = 0; i < toNode.incomingGenes.size(); ++i)
                {
                NEATGene link = toNode.incomingGenes.get(i);
                if (!link.isRecurrent)
                    {
                    if (!set.contains(link.inNode))
                        {
                        set.add(link.inNode);
                        hasPath(state, link.inNode, fromNode, set, level + 1, threshold, results);
                        if (results[0] && results[1])
                            {
                            return;
                            }
                        }
                    }
                }
            set.add(toNode);
            results[0] = true;
            results[1] = false;
            }
        }

    /** Check if not all output are active. */
    public boolean outputOff()
        {
        for (int i = 0; i < outputs.size(); ++i)
            {
            if (outputs.get(i).activationCount == 0)
                return true;
            }
        return false;
        }

    /** Find the maximum number of neurons between an output and an input. */
    public int maxDepth()
        {
        int curDepth; // The depth of current node
        int maxDepth = 0; // The max depth

        for (int i = 0; i < nodes.size(); ++i)
            {
            NEATNode node = nodes.get(i);
            node.innerLevel = 0;
            node.isTraversed = false;
            }

        for (int i = 0; i < outputs.size(); ++i)
            {
            curDepth = outputs.get(i).depth(0, this, maxDepth);
            if (curDepth > maxDepth)
                maxDepth = curDepth;
            }
        return maxDepth;
        }



    /**
     * Create the phenotype (network) from the genotype (genome). One main task
     * of method is to link the incomingGenes for each nodes.
     */
    public void buildNetwork(NEATIndividual individual)
        {
        this.individual = individual;
        
        nodes.addAll(individual.nodes);

        ArrayList<NEATNode> inputList = new ArrayList<NEATNode>();
        ArrayList<NEATNode> outputList = new ArrayList<NEATNode>();

        // NOTE: original code clone the node, thus organism and network each
        // have a node instance
        // but we do not clone it here
        for (int i = 0; i < individual.nodes.size(); ++i)
            {
            // we are rebuild the network, we clear all the node incomingGenes
            // as we will rebuild it later
            individual.nodes.get(i).clearIncoming();
            // Check for input or output designation of node
            if (individual.nodes.get(i).geneticNodeLabel == NodePlace.INPUT)
                inputList.add(individual.nodes.get(i));
            else if (individual.nodes.get(i).geneticNodeLabel == NodePlace.BIAS)
                inputList.add(individual.nodes.get(i));
            else if (individual.nodes.get(i).geneticNodeLabel == NodePlace.OUTPUT)
                outputList.add(individual.nodes.get(i));
            }
        inputs.addAll(inputList);
        outputs.addAll(outputList);

        // prepare the incomingGenes for each node
        for (int i = 0; i < individual.genome.length; ++i)
            {
            // only deal with enabled nodes
            NEATGene link = (NEATGene) individual.genome[i];
           
            if (link.enable)
                {
                NEATNode outNode = link.outNode;
                
                outNode.incomingGenes.add(link);
                }
            }
        }


    }
