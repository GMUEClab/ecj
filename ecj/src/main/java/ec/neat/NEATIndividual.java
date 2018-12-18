/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.neat;

import java.io.*;
import java.util.*;
import ec.*;
import ec.neat.NEATSpecies.*;
import ec.neat.NEATNode.*;
import ec.util.*;
import ec.vector.*;

/**
 * NEATIndividual is GeneVectorIndividual with NEATNetwork as phenotype. It
 * contains the genome of the individual and also the nodes (NEATNode) for
 * phenotype. It's the combination of Organism class and Genome class in original
 * code. Most of the mutation and crossover happen in this class.
 * 
 * 
 * <p>
 * <b>Parameters</b><br>
 * <table>
 * <tr>
 * <td valign=top><i>base</i>.<tt>network</tt><br>
 * <font size=-1>Classname, = ec.neat.NEATNetwork</font></td>
 * <td valign=top>Class of network in the individual</td>
 * </tr>
 * </table>
 * 
 * <p>
 * <b>Parameter bases</b><br>
 * <table>
 * <tr>
 * <td valign=top><i>base</i>.<tt>network</tt><br>
 * <td>network in the individual</td>
 * </tr>
 * </table>
 * 
 * <p>
 * <b>Default Base</b><br>
 * neat.individual
 * 
 * @author Ermo Wei and David Freelan
 * 
 */
public class NEATIndividual extends GeneVectorIndividual
    {
    /**
     * Fitness after the adjustment.
     */
    public double adjustedFitness;

    /** The individual's subpecies */
    public NEATSubspecies subspecies;

    /** Number of children this individual may have for next generation. */
    public double expectedOffspring;

    /** Tells which generation this individual is from. */
    public int generation;

    /** Marker for destruction of inferior individual. */
    public boolean eliminate;

    /**
     * Marks the subspecies champion, which is the individual who has the
     * highest fitness with the subspecies.
     */
    public boolean champion;

    /**
     * Number of reserved offspring for a population leader. This is used for
     * delta coding.
     */
    public int superChampionOffspring;

    /** Marks the best individual in current generation of the population. */
    public boolean popChampion;

    /** Marks the duplicate child of a champion (for tracking purposes). */
    public boolean popChampionChild;

    /** debug variable, highest fitness of champion */
    public double highFit;

    /**
     * When playing in real-time allows knowing the maturity of an individual
     */
    public int timeAlive;

    /**
     * All the node of this individual. Nodes are arranged so that the first
     * part of the nodes are SENSOR nodes.
     */
    public ArrayList<NEATNode> nodes;

    public void setup(EvolutionState state, Parameter base)
        {
        super.setup(state, base);

        Parameter def = defaultBase();

        eliminate = false;
        expectedOffspring = 0;
        generation = 0;
        subspecies = null;
        champion = false;
        superChampionOffspring = 0;
        nodes = new ArrayList<NEATNode>();
        }

    @Override
    public Parameter defaultBase()
        {
        return NEATDefaults.base().push(P_INDIVIDUAL);
        }

    /** Initializes an individual with minimal structure. */
    public void reset(EvolutionState state, int thread)
        {
        super.reset(state, thread);
        }

    /** Reset the individual with given nodes and genome */
    public void reset(ArrayList<NEATNode> nodeList, ArrayList<Gene> genes)
        {
        // clone the genome
        genome = new Gene[genes.size()];
        genome = genes.toArray(genome);

        // must clone the nodes
        nodes = nodeList;

        // link the genes in new individual with nodes
        for (int i = 0; i < genome.length; ++i)
            {
            NEATGene gene = (NEATGene) genome[i];
            for (int j = 0; j < nodes.size(); ++j)
                {
                if (nodes.get(j).nodeId == gene.inNodeId)
                    gene.inNode = nodes.get(j);
                if (nodes.get(j).nodeId == gene.outNodeId)
                    gene.outNode = nodes.get(j);
                }
            }
        }
        
    public int hashCode()
        {
        int hash = super.hashCode();
        
        // fold in the nodes
        for(int i = 0; i < nodes.size(); i++)
            {
            hash = hash * 31 + 17 + nodes.get(i).hashCode();
            }
                
        return hash;
        }

    @Override
    public boolean equals(Object obj)
        {
        if (!super.equals(obj))
            return false;
        NEATIndividual ind = (NEATIndividual) obj;
        int len1 = nodes.size();
        int len2 = ind.nodes.size();
        if (len1 != len2) return false;
        for(int i = 0 ; i < len1; i++)
            if (!nodes.get(i).equals(ind.nodes.get(i)))
                return false;
        return true;
        }

    /** Set the born generation of this individual. */
    public void setGeneration(EvolutionState state)
        {
        generation = state.generation;
        }

    /** Get the upperbound for the node id, used in Initializer. */
    public int getNodeIdSup()
        {
        return nodes.get(nodes.size() - 1).nodeId + 1;
        }

    /** Get the upperbound for the innovation number, used in Initializer. */
    public int getGeneInnovationNumberSup()
        {
        return ((NEATGene) genome[genome.length - 1]).innovationNumber + 1;
        }

    public Object clone()
        {
        // Genome *Genome::duplicate(int new_id)
        // we ignore the new_id here as it can be assigned later
        // this clones the genes
        NEATIndividual myobj = (NEATIndividual) (super.clone());

        // must clone the nodes
        myobj.nodes = new ArrayList<NEATNode>();
        for (int i = 0; i < nodes.size(); ++i)
            {
            NEATNode newNode = (NEATNode) nodes.get(i).emptyClone();
            myobj.nodes.add(newNode);
            }

        // link the genes in new individual with nodes
        for (int i = 0; i < myobj.genome.length; ++i)
            {
            NEATGene gene = (NEATGene) myobj.genome[i];
            for (int j = 0; j < myobj.nodes.size(); ++j)
                {
                if (myobj.nodes.get(j).nodeId == gene.inNodeId)
                    gene.inNode = myobj.nodes.get(j);
                if (myobj.nodes.get(j).nodeId == gene.outNodeId)
                    gene.outNode = myobj.nodes.get(j);
                }
            }
        return myobj;
        }


    /**
     * This method is used to output a individual that is same as the format in
     * start genome file.
     */
    public String genotypeToString()
        {
        StringBuilder s = new StringBuilder();
        int size = genome.length;
        s.append(Code.encode(size));
        for (int i = 0; i < genome.length; i++)
            {
            s.append("\n");
            s.append(genome[i].printGeneToString());
            }
        s.append("\n" + Code.encode(nodes.size()));
        for (int i = 0; i < nodes.size(); ++i)
            {
            s.append("\n");
            s.append(nodes.get(i).printNodeToString());
            }
        return s.toString();
        }

    /**
     * This method is used to read a gene in start genome from file. It will
     * then calls the corresponding methods to parse genome and nodes
     * respectively.
     */
    protected void parseGenotype(EvolutionState state, LineNumberReader reader) throws IOException
        {
        // first parse the genotype, the genes of NEAT
        super.parseGenotype(state, reader);

        // after read the gene, start to read nodes for network
        parseNodes(state, reader);

        // go through all the gene (represent edge in network), link with
        for (int i = 0; i < genome.length; ++i)
            {
            NEATGene neatGene = (NEATGene) genome[i];
            for (int j = 0; j < nodes.size(); ++j)
                {
                if (nodes.get(j).nodeId == neatGene.inNodeId)
                    neatGene.inNode = nodes.get(j);
                else if (nodes.get(j).nodeId == neatGene.outNodeId)
                    neatGene.outNode = nodes.get(j);
                }
            }
        }

    /**
     * Create the nodes from the reader, and calls readNode method on each node.
     */
    public void parseNodes(EvolutionState state, LineNumberReader reader) throws IOException
        {
        // read in the next line. The first item is the number of genes
        String string = reader.readLine();

        DecodeReturn d = new DecodeReturn(string);
        Code.decode(d);
        if (d.type != DecodeReturn.T_INTEGER)  // uh oh
            state.output.fatal("Individual with nodes:\n" + string
                + "\n... does not have an integer at the beginning indicating the node count.");
        int lll = (int) (d.l);

        NEATSpecies s = (NEATSpecies) species;
        for (int i = 0; i < lll; ++i)
            {
            NEATNode node = (NEATNode) (s.nodePrototype.emptyClone());
            node.readNode(state, reader);
            nodes.add(node);
            }
        }

    /** We append new gene(s) to the current genome */
    public void addGene(NEATGene[] appendGenes)
        {
        Gene[] newGenome = new Gene[genome.length + appendGenes.length];
        System.arraycopy(genome, 0, newGenome, 0, genome.length);
        System.arraycopy(appendGenes, 0, newGenome, genome.length, appendGenes.length);
        setGenome(newGenome);
        }

    /** Mutate the weights of the genes */
    public void mutateLinkWeights(EvolutionState state, int thread ,NEATSpecies species, double power, double rate,
        MutationType mutationType)
        {
        // Go through all the Genes and perturb their link's weights
        // Signifies the last part of the genome
        double endPart = ((double) genome.length) * 0.8;

        // Modified power by gene number
        // The power of mutation will rise farther into the genome
        // on the theory that the older genes are more fit since
        // they have stood the test of time
        double powerMod = 1.0;

        double gaussPoint, coldGaussPoint;
        boolean severe = state.random[thread].nextBoolean();



        // Loop on all genes
        for (int i = 0; i < genome.length; ++i)
            {
            // The following if determines the probabilities of doing cold
            // gaussian
            // mutation, meaning the probability of replacing a link weight with
            // another, entirely random weight. It is meant to bias such
            // mutations
            // to the tail of a genome, because that is where less time-tested
            // genes
            // reside. The gaussPoint and coldGaussPoint represent values above
            // which a random double will signify that kind of mutation.

            NEATGene gene = (NEATGene) genome[i];

            if (severe)
                {
                gaussPoint = 0.3;
                coldGaussPoint = 0.1;
                }
            else if (genome.length >= 10 && i > endPart)
                {
                // Mutate by modification % of connections
                gaussPoint = 0.5;
                // Mutate the rest by replacement % of the time
                coldGaussPoint = 0.3;
                }
            else
                {
                // Half the time don't do any cold mutations
                if (state.random[thread].nextBoolean())
                    {
                    gaussPoint = 1.0 - rate;
                    coldGaussPoint = 1.0 - rate - 0.1;
                    }
                else
                    {
                    gaussPoint = 1.0 - rate;
                    coldGaussPoint = 1.0 - rate;
                    }
                }

            double value =  (state.random[thread].nextBoolean() ? 1 : -1) * state.random[thread].nextDouble() * power * powerMod;

            if (mutationType == MutationType.GAUSSIAN)
                {
                double randomChoice = state.random[thread].nextDouble();
                if (randomChoice > gaussPoint)
                    gene.weight += value;
                else if (randomChoice > coldGaussPoint)
                    gene.weight = value;
                }
            else if (mutationType == MutationType.COLDGAUSSIAN)
                {
                gene.weight = value;
                }

            // Clip the weight at 8.0 (experimental)
            // FIXME : this code only exist in C++ version
            /*
             * if (gene.weight > 8.0) gene.weight = 8.0; else if (gene.weight <
             * -8.0) gene.weight = -8.0;
             */

            // Record the innovation
            gene.mutationNumber = gene.weight;

            }

        }

    /** Try to add a new gene (link) into the current genome. */
    public void mutateAddLink(EvolutionState state,int thread)
        {
        // Make attempts to find an unconnected pair
        int tryCount = 0;
        NEATSpecies neatSpecies = (NEATSpecies) species;
        int newLinkTries = neatSpecies.newLinkTries;
        // Decide whether to make this recurrent
        boolean doRecur = state.random[thread].nextBoolean(neatSpecies.recurOnlyProb);

        NEATNode firstNode = null, secondNode = null;

        // Find the first non-sensor so that the to-node won't look at sensor as
        // possible destinations
        int firstNonSensor = -1;
        for (int i = 0; i < nodes.size(); ++i)
            {
            if (nodes.get(i).type == NodeType.SENSOR)
                {
                firstNonSensor = i;
                break;
                }
            }

        // Here is the recurrent finder loop- it is done separately
        boolean loopRecur = false;
        int firstNodeIndex = -1;
        int secondNodeIndex = -1;
        boolean found = false;
        while (tryCount < newLinkTries)
            {
            if (doRecur)
                {
                // at this point :
                // 50% of prob to decide a loop recurrency (node X to node X)
                // 50% a normal recurrency (node X to node Y)
                loopRecur = state.random[thread].nextBoolean();
                if (loopRecur)
                    {
                    firstNodeIndex = firstNonSensor + state.random[thread].nextInt(nodes.size() - firstNonSensor);
                    secondNodeIndex = firstNodeIndex;
                    }
                else
                    {
                    firstNodeIndex = state.random[thread].nextInt(nodes.size());
                    secondNodeIndex = firstNonSensor + state.random[thread].nextInt(nodes.size() - firstNonSensor);
                    }
                }
            else  // No recurrency case
                {
                firstNodeIndex = state.random[thread].nextInt(nodes.size());
                secondNodeIndex = firstNonSensor + state.random[thread].nextInt(nodes.size() - firstNonSensor);
                }

            // grab the nodes
            firstNode = nodes.get(firstNodeIndex);
            secondNode = nodes.get(secondNodeIndex);

            // Verify is the possible new gene (link) already exist
            boolean bypass = false;
            for (int i = 0; i < genome.length; ++i)
                {
                NEATGene gene = (NEATGene) genome[i];
                if (secondNode.type == NodeType.SENSOR)
                    {
                    bypass = true;
                    break;
                    }
                if (gene.inNodeId == firstNode.nodeId && gene.outNodeId == secondNode.nodeId && gene.isRecurrent
                    && doRecur)
                    {// already have a recurrent link between these nodes in
                    // recurrent case
                    bypass = true;
                    break;
                    }
                if (gene.inNodeId == firstNode.nodeId && gene.outNodeId == secondNode.nodeId && !gene.isRecurrent
                    && !doRecur)
                    {// already have a normal link between these nodes in normal
                    // case
                    bypass = true;
                    break;
                    }
                }

            if (!bypass)
                {
                int threshold = nodes.size() * nodes.size();
                // we want to add a link from firstNode to secondNode,
                // we first check if there is a potential link from secondNodde
                // to firstNode
                boolean[] result = NEATNetwork.hasPath(state, firstNode, secondNode, threshold);

                // the network contains a
                if (!result[0])
                    {
                    state.output.error("network has infinite loop");
                    return;
                    }
                // if we want a recur link but added link will not add recur
                // or if we do not want a recur link but added link will cause a
                // recur,
                // we keep trying
                if ((!result[1] && doRecur) || (result[1] && !doRecur))
                    tryCount++;
                else
                    {
                    found = true;
                    break;
                    }
                }
            else
                {
                // if bypass is true, this gene is not good
                // and skip to next cycle
                tryCount++;
                }
            }

        if (!found)
            return;

        NEATInnovation testInno = (NEATInnovation) neatSpecies.innovationPrototype.clone();
        testInno.reset(firstNode.nodeId, secondNode.nodeId, doRecur);

        NEATGene[] newGenes = new NEATGene[1];
        if (neatSpecies.hasInnovation(testInno))
            {
            // Grab the existing innovation info
            NEATInnovation innovation = neatSpecies.getInnovation(testInno);

            // create the gene
            newGenes[0] = (NEATGene) neatSpecies.genePrototype.clone();
            newGenes[0].reset(innovation.newWeight, firstNode.nodeId, secondNode.nodeId, doRecur,
                innovation.innovationNum1, 0);
            newGenes[0].inNode = firstNode;
            newGenes[0].outNode = secondNode;
            }
        else
            {
            // The innovation is totally novel
            double weight = state.random[thread].nextBoolean() ? 1 : -1;
            weight *= state.random[thread].nextDouble();

            newGenes[0] = (NEATGene) neatSpecies.genePrototype.clone();
            int currInnovNum = neatSpecies.nextInnovationNumber();
            newGenes[0].reset(weight, firstNode.nodeId, secondNode.nodeId, doRecur, currInnovNum, weight);
            newGenes[0].inNode = firstNode;
            newGenes[0].outNode = secondNode;

            // create innovation information
            NEATInnovation newInno = (NEATInnovation) neatSpecies.innovationPrototype.clone();
            newInno.reset(firstNode.nodeId, secondNode.nodeId, currInnovNum, weight, doRecur);
            neatSpecies.addInnovation(newInno);


            }

        // Now add the new Genes to the genome
        addGene(newGenes);
        }
    /** Add a new node into this individual. */
    public void mutateAddNode(EvolutionState state, int thread)
        {
        NEATSpecies neatSpecies = (NEATSpecies) species;
        NEATGene gene = null;
        int newNodeTries = neatSpecies.newNodeTries;
        int tryCount = 0;
        boolean found = false;
        int i = 0;
        // split next link with a bias towards older links

        if (genomeLength() < neatSpecies.addNodeMaxGenomeLength)
            {

            boolean step2 = false;

            // find the first non enable link whose input is not a bias node
            for (i = 0; i < genome.length; ++i)
                {
                gene = (NEATGene) genome[i];
                if (gene.enable && gene.inNode.geneticNodeLabel != NodePlace.BIAS)
                    break;
                }
            // Now randomize which node is chosen at this point
            // We bias the search towards older genes because
            // this encourages splitting to distribute evenly
            for (; i < genome.length; ++i)
                {
                gene = (NEATGene) genome[i];
                if (state.random[thread].nextBoolean(.7) && (gene.inNode.geneticNodeLabel != NodePlace.BIAS))
                    {
                    step2 = true;
                    break;
                    }
                }

            if (step2 && gene.enable)
                found = true;
            }
        else
            {
            while ((tryCount < newNodeTries) && (!found))
                {
                // Pure random split
                int index = state.random[thread].nextInt(genomeLength());
                gene = (NEATGene) genome[index];
                if (gene.enable && gene.inNode.geneticNodeLabel != NodePlace.BIAS)
                    {
                    found = true;
                    }
                tryCount++;
                }
            }

        // If we couldn't find anything so say goodbye
        if (!found)
            return;

        // Disable the old gene (link)
        gene.enable = false;

        // Extract the link
        double oldWeight = gene.weight;

        // Extract the nodes
        NEATNode inNode = gene.inNode;
        NEATNode outNode = gene.outNode;

        // Check to see if this innovation has already been done
        // in another genome
        // Innovations are used to make sure the same innovation in
        // two separate genomes in the same generation receives
        // the same innovation number.

        // We check to see if an innovation already occured that was:
        // -A new node
        // -Stuck between the same nodes as were chosen for this mutation
        // -Splitting the same gene as chosen for this mutation
        // If so, we know this mutation is not a novel innovation
        // in this generation
        // so we make it match the original, identical mutation which occured
        // elsewhere in the population by coincidence

        NEATInnovation testInno = (NEATInnovation) neatSpecies.innovationPrototype.clone();
        testInno.reset(inNode.nodeId, outNode.nodeId, gene.innovationNumber);

        NEATNode newNode = null;
        NEATGene[] newGenes = new NEATGene[2];
        if (neatSpecies.hasInnovation(testInno))
            {
            
            // Grab the existing innovation info
            NEATInnovation innovation = neatSpecies.getInnovation(testInno);
            newNode = (NEATNode) neatSpecies.nodePrototype.emptyClone();
            newNode.reset(NodeType.NEURON, innovation.newNodeId, NodePlace.HIDDEN);

            // create the gene
            newGenes[0] = (NEATGene) neatSpecies.genePrototype.clone();
            newGenes[0].reset(1, inNode.nodeId, newNode.nodeId, gene.isRecurrent, innovation.innovationNum1, 0);
            newGenes[0].inNode = inNode;
            newGenes[0].outNode = newNode;

            newGenes[1] = (NEATGene) neatSpecies.genePrototype.clone();
            newGenes[1].reset(oldWeight, newNode.nodeId, outNode.nodeId, false, innovation.innovationNum2, 0);
            newGenes[1].inNode = newNode;
            newGenes[1].outNode = outNode;
            }
        else
            {
            // The innovation is totally novel
            // create the new Node
            
            newNode = (NEATNode) neatSpecies.nodePrototype.emptyClone();
            newNode.reset(NodeType.NEURON, neatSpecies.currNodeId++, NodePlace.HIDDEN);

            // create the new Gene
            newGenes[0] = (NEATGene) neatSpecies.genePrototype.clone();
            int currInnovNum = neatSpecies.nextInnovationNumber();
            int currInnovNum2 = neatSpecies.nextInnovationNumber();
            newGenes[0].reset(1, inNode.nodeId, newNode.nodeId, gene.isRecurrent,currInnovNum, 0);
            // link the new gene to node
            newGenes[0].inNode = inNode;
            newGenes[0].outNode = newNode;
            

            newGenes[1] = (NEATGene) neatSpecies.genePrototype.clone();
            newGenes[1].reset(oldWeight, newNode.nodeId, outNode.nodeId, false, currInnovNum2, 0);
            newGenes[1].inNode = newNode;
            newGenes[1].outNode = outNode;
            


            // create innovation information
            NEATInnovation newInno = (NEATInnovation) neatSpecies.innovationPrototype.clone();
            newInno.reset(inNode.nodeId, outNode.nodeId, currInnovNum, currInnovNum2,
                newNode.nodeId, gene.innovationNumber);
            neatSpecies.addInnovation(newInno);
            }

        // Now add the new Node and New Genes to the genome
        nodes.add(newNode);
        addGene(newGenes);

        }

    /** Randomly enable or disable a gene. */
    public void mutateToggleEnable(EvolutionState state, int thread,int times)
        {
        for (int i = 0; i < times; ++i)
            {
            // Choose a random gene
            int index = state.random[thread].nextInt(genome.length);
            NEATGene gene = (NEATGene) genome[index];

            if (gene.enable)
                {
                // We need to make sure that another gene connects out of the
                // in-node
                // Because if not a section of network will break off and become
                // isolated
                boolean found = false;
                for (int j = 0; j < genome.length; ++j)
                    {
                    NEATGene anotherGene = (NEATGene) genome[j];
                    if (anotherGene.inNodeId == gene.inNodeId && anotherGene.enable
                        && anotherGene.innovationNumber != gene.innovationNumber)
                        {
                        found = true;
                        break;
                        }
                    }

                // Disable the gene is it's safe to do so
                if (found)
                    gene.enable = false;
                }
            else // Turn on the enable if it's not enable
                gene.enable = true;
            }

        }

    /** Reenable a gene if it's disabled. */
    public void mutateGeneReenable()
        {
        for (int i = 0; i < genome.length; ++i)
            {
            NEATGene gene = (NEATGene) genome[i];
            if (!gene.enable)
                {
                gene.enable = true;
                break;
                }
            }
        }



    /**
     * Mutation function, determine which mutation is going to proceed with
     * certain probabilities parameters.
     */
    public void defaultMutate(EvolutionState state, int thread)
        {
        NEATSpecies neatSpecies = (NEATSpecies) species;
        // do the mutation depending on the probabilities of various mutations
        if (state.random[thread].nextBoolean(neatSpecies.mutateAddNodeProb))
            {
           
            mutateAddNode(state,thread);
            }
        else if (state.random[thread].nextBoolean(neatSpecies.mutateAddLinkProb))
            {
           
            createNetwork(); // Make sure we have the network
            mutateAddLink(state, thread);
            }
        else
            {

            if (state.random[thread].nextBoolean(neatSpecies.mutateLinkWeightsProb))
                {
                
                mutateLinkWeights(state,thread, neatSpecies, neatSpecies.weightMutationPower, 1.0, MutationType.GAUSSIAN);
                }
            if (state.random[thread].nextBoolean(neatSpecies.mutateToggleEnableProb))
                {
                
                mutateToggleEnable(state,thread, 1);
                }
            if (state.random[thread].nextBoolean(neatSpecies.mutateGeneReenableProb))
                {
               
                mutateGeneReenable();
                }
            }
        }

    /**
     * Crossover function. Unlike defaultCrossover, this does not do destructive
     * crossover. It will create a new individual as the crossover of the new
     * parents.
     */
    public NEATIndividual crossover(EvolutionState state, int thread, NEATIndividual secondParent)
        {

        NEATSpecies neatSpecies = (NEATSpecies) species;
        NEATIndividual newInd = null;
        if (state.random[thread].nextBoolean(neatSpecies.mateMultipointProb))
            {
            // mate multipoint
            
            newInd = mateMultipoint(state, thread, secondParent, false);
            }
        else if (state.random[thread].nextBoolean((neatSpecies.mateMultipointAvgProb
                    / (neatSpecies.mateMultipointAvgProb + neatSpecies.mateSinglepointProb))))
            {

            // mate multipoint average
            newInd = mateMultipoint(state, thread, secondParent, true);
            }
        else
            {
            // mate single point
            
            newInd = mateSinglepoint(state, thread, secondParent);
            }
        // mate_baby = true;
        return newInd;
        }

    @Deprecated
    /**
     * Crossover a single point from two parents, it's not used in original
     * code, as pop.subpop.X.species.mate-singlepoint-prob will always be 0.
     */
    public NEATIndividual mateSinglepoint(EvolutionState state, int thread, NEATIndividual secondParent)
        {
        // NOTE : unfinished code, seems the NEAT is not using this mate method
        NEATSpecies neatSpecies = (NEATSpecies) species;
        ArrayList<NEATNode> newNodes = new ArrayList<NEATNode>();
        ArrayList<Gene> newGenes = new ArrayList<Gene>();

        int sizeA = genomeLength(), sizeB = secondParent.genomeLength();
        // make sure genomeA point to the shorter genome
        Gene[] genomeA = sizeA < sizeB ? genome : secondParent.genome;
        Gene[] genomeB = sizeA < sizeB ? secondParent.genome : genome;

        int lengthA = genomeA.length, lengthB = genomeB.length;
        int crossPoint = state.random[thread].nextInt(lengthA);

        NEATGene geneA = null, geneB = null;
        int indexA = 0, indexB = 0;

        boolean skip = false;  // Default to not skip a Gene
        // Note that we skip when we are on the wrong Genome before
        // crossing

        int geneCounter = 0;  // Ready to count to crosspoint

        NEATGene chosenGene = null;

        // Now move through the Genes of each parent until both genomes end
        while (indexA < lengthA || indexB < lengthB)
            {
            // if genomeA is ended, we move pointer of genomeB
            // select genes from genomeB
            if (indexA == lengthA)
                {
                chosenGene = (NEATGene) genomeB[indexB];
                indexB++;
                }
            // if genomeB is ended, we move pointer of genomeA
            // select genes from genomeA
            else if (indexB == lengthB)
                {
                chosenGene = (NEATGene) genomeA[indexA];
                indexA++;
                }
            else
                {
                // extract current innovation number
                int innovA = ((NEATGene) genomeA[indexA]).innovationNumber;
                int innovB = ((NEATGene) genomeB[indexB]).innovationNumber;

                if (innovA == innovB)
                    {
                    // Pick the chosenGene depending on whether we've crossed
                    // yet
                    if (geneCounter < crossPoint)
                        {
                        chosenGene = (NEATGene) genomeA[indexA];
                        }
                    else if (geneCounter > crossPoint)
                        {
                        chosenGene = (NEATGene) genomeB[indexB];
                        }
                    // We are at the cross point here
                    else
                        {
                        geneA = (NEATGene) genomeA[indexA];
                        geneB = (NEATGene) genomeB[indexB];

                        // set up the average gene
                        NEATGene avgGene = (NEATGene) neatSpecies.genePrototype.clone();

                        double weight = (geneA.weight + geneB.weight) / 2.0;

                        // Average them into the avgGene
                        int inNodeId = (state.random[thread].nextBoolean()) ? geneA.inNodeId : geneB.inNodeId;
                        int outNodeId = (state.random[thread].nextBoolean()) ? geneA.outNodeId : geneB.outNodeId;
                        boolean isRecurrent = (state.random[thread].nextBoolean()) ? geneA.isRecurrent : geneB.isRecurrent;

                        int innovationNumber = geneA.innovationNumber;
                        double mutationNumber = (geneA.mutationNumber + geneB.mutationNumber) / 2.0;

                        avgGene.enable = !(!geneA.enable || !geneB.enable);

                        avgGene.reset(weight, inNodeId, outNodeId, isRecurrent, innovationNumber, mutationNumber);

                        chosenGene = avgGene;
                        }

                    indexA++;
                    indexB++;
                    geneCounter++;
                    }
                else if (innovA < innovB)
                    {
                    if (geneCounter < crossPoint)
                        {
                        chosenGene = (NEATGene) genomeA[indexA];
                        indexA++;
                        geneCounter++;
                        }
                    else
                        {
                        chosenGene = (NEATGene) genomeB[indexB];
                        indexB++;
                        }
                    }
                else if (innovA > innovB)
                    {
                    indexB++;
                    skip = true; // Special case: we need to skip to the next
                    // iteration
                    // because this Gene is before the crosspoint on the wrong
                    // Genome
                    }
                }

            if (hasGene(newGenes, chosenGene))
                skip = true;

            // Check to see if the chosenGene conflicts with an already chosen
            // gene
            // i.e. do they represent the same link
            // if the link is not duplicate, add it to the new individual
            if (!skip)
                {
                // Check for the nodes, add them if not in the baby Genome
                // already
                createNodeCopyIfMissing(newNodes, chosenGene.inNode);
                createNodeCopyIfMissing(newNodes, chosenGene.outNode);

                // clone the chosenGene and add to newGenes
                NEATGene newGene = (NEATGene) chosenGene.clone();
                newGenes.add(newGene);
                }

            skip = false;
            }

        return null;
        }

    /** Test if a genome has certain gene. */
    public boolean hasGene(ArrayList<Gene> genome, Gene gene)
        {
        NEATGene neatGene = (NEATGene) gene;
        for (int i = 0; i < genome.size(); ++i)
            {
            // original code seems redundant
            NEATGene g = (NEATGene) genome.get(i);
            if (g.inNodeId == neatGene.inNodeId && g.outNodeId == neatGene.outNodeId && g.isRecurrent == neatGene.isRecurrent)
                {
                return true;
                }

            }
        return false;
        }

    /**
     * Create the node if the nodeList do not have that node.The nodes in the
     * nodeList is guarantee in ascending order according to node's nodeId.
     */
    public void createNodeCopyIfMissing(ArrayList<NEATNode> nodeList, NEATNode node)
        {
        for (int i = 0; i < nodeList.size(); ++i)
            {
            NEATNode n = nodeList.get(i);
            // if we find the node with the same node id, we simply return it
            if (node.nodeId == n.nodeId)
                return;
            // if we find a node with larger node id, we insert the new node
            // into current position
            else if (node.nodeId < n.nodeId)
                {
                NEATNode newNode = (NEATNode) node.emptyClone();
                nodeList.add(i, newNode);
                return;
                }
            }

        // if we didn't find node in nodeList and it has the highest nodeId
        // we add it to the end of list
        NEATNode newNode = (NEATNode) node.emptyClone();
        nodeList.add(newNode);
        }

    /** Doing crossover from two parent at multiple points in the genome. */
    public NEATIndividual mateMultipoint(EvolutionState state, int thread, NEATIndividual secondParent,
        boolean averageFlag)
        {
        NEATSpecies neatSpecies = (NEATSpecies) species;
        ArrayList<NEATNode> newNodes = new ArrayList<NEATNode>();
        ArrayList<Gene> newGenes = new ArrayList<Gene>();

        int indexA = 0;
        int indexB = 0;
        Gene[] genomeA = genome;
        Gene[] genomeB = secondParent.genome;
        int lengthA = genomeA.length;
        int lengthB = genomeB.length;

        // Figure out which genome is better
        // The worse genome should not be allowed to add extra structural
        // baggage
        // If they are the same, use the smaller one's disjoint and excess genes
        // only


        boolean firstFitter = isSuperiorTo(this, secondParent);

        // Make sure all sensors and outputs are included
        for (int i = 0; i < secondParent.nodes.size(); ++i)
            {
            NEATNode node = secondParent.nodes.get(i);
            if (node.geneticNodeLabel == NodePlace.INPUT || node.geneticNodeLabel == NodePlace.BIAS || node.geneticNodeLabel == NodePlace.OUTPUT)
                {
                createNodeCopyIfMissing(newNodes, node);
                }
            }

        NEATGene chosenGene = null;
        while (indexA < lengthA || indexB < lengthB)
            {
            boolean skip = false;

            if (indexA >= lengthA)
                {
                chosenGene = (NEATGene) genomeB[indexB];
                indexB++;
                // Skip excess from the worse genome
                if (firstFitter)
                    skip = true;
                }
            else if (indexB >= lengthB)
                {
                chosenGene = (NEATGene) genomeA[indexA];
                indexA++;
                // Skip excess from worse genome
                if (!firstFitter)
                    skip = true;
                }
            else
                {
                NEATGene geneA = (NEATGene) genomeA[indexA];
                NEATGene geneB = (NEATGene) genomeB[indexB];

                int innovA = geneA.innovationNumber;
                int innovB = geneB.innovationNumber;

                if (innovA == innovB)
                    {
                    if (!averageFlag)
                        {
                        chosenGene = state.random[thread].nextBoolean() ? geneA : geneB;
                        // If one is disabled, the corresponding gene in the
                        // offspring will likely be disabled
                        if (!geneA.enable || !geneB.enable)
                            {
                            if (state.random[thread].nextBoolean( 0.75))
                                chosenGene.enable = false;
                            }
                        }
                    else
                        {
                        // weight averaged here
                        double weight = (geneA.weight + geneB.weight) / 2.0;

                        // Average them into the avgGene
                        int inNodeId = -1, outNodeId = -1;
                        NEATNode inNode = null, outNode = null;
                        if (state.random[thread].nextBoolean())
                            {
                            inNodeId = geneA.inNodeId;
                            // we direct set the inNode here without clone
                            // this is because we will clone this node
                            // eventually
                            inNode = geneA.inNode;
                            }
                        else
                            {
                            inNodeId = geneB.inNodeId;
                            inNode = geneB.inNode;
                            }
                        if (state.random[thread].nextBoolean())
                            {
                            outNodeId = geneA.outNodeId;
                            // we direct set the inNode here without clone
                            // this is because we will clone this node
                            // eventually
                            outNode = geneA.outNode;
                            }
                        else
                            {
                            outNodeId = geneB.outNodeId;
                            outNode = geneB.outNode;
                            }
                        boolean isRecurrent = (state.random[thread].nextBoolean()) ? geneA.isRecurrent : geneB.isRecurrent;

                        int innovationNumber = geneA.innovationNumber;
                        double mutationNumber = (geneA.mutationNumber + geneB.mutationNumber) / 2.0;

                        boolean enable = true;
                        if (!geneA.enable || !geneB.enable)
                            if (state.random[thread].nextBoolean( 0.75))
                                enable = false;

                        chosenGene = (NEATGene) neatSpecies.genePrototype.clone();
                        chosenGene.reset(weight, inNodeId, outNodeId, isRecurrent, innovationNumber, mutationNumber);
                        chosenGene.enable = enable;
                        chosenGene.inNode = inNode;
                        chosenGene.outNode = outNode;
                        }

                    indexA++;
                    indexB++;
                    }
                else if (innovA < innovB)
                    {
                    chosenGene = (NEATGene) genomeA[indexA];
                    indexA++;
                    // Skip disjoint from worse genome
                    if (!firstFitter)
                        skip = true;
                    }
                else if (innovA > innovB)
                    {
                    chosenGene = (NEATGene) genomeB[indexB];
                    indexB++;
                    // Skip disjoint from worse genome
                    if (firstFitter)
                        skip = true;
                    }
                }

            // Check to see if the chosengene conflicts with an already chosen
            // gene
            // i.e. do they represent the same link
            if (!skip && hasGene(newGenes, chosenGene))
                skip = true;

            if (!skip)
                {
                // Now add the chosenGene to the baby individual
                createNodeCopyIfMissing(newNodes, chosenGene.inNode);
                createNodeCopyIfMissing(newNodes, chosenGene.outNode);

                // clone the chosenGene and add to newGenes
                NEATGene newGene = (NEATGene) chosenGene.clone();
                newGenes.add(newGene);
                }

            }

        // NEATIndividual newInd = (NEATIndividual)
        // neatSpecies.i_prototype.clone();
        // newInd.reset(newNodes, newGenes);

        return (NEATIndividual) neatSpecies.newIndividual(state, thread, newNodes, newGenes);
        }

    /**
     * Return true if first individual has better fitness than the second
     * individual. If they have the same fitness, look at their genome length
     * whoever is shorter is superior.
     */
    private boolean isSuperiorTo(NEATIndividual first, NEATIndividual second)
        {
        boolean firstIsBetter = false;

        if (first.fitness.betterThan(second.fitness))
            firstIsBetter = true;
        else if (second.fitness.betterThan(first.fitness))
            firstIsBetter = false;
        else
            {
            // we compare their genome length if their fitness is equal
            // in this case, the individual with more compact representation
            // are considered as more fit
            firstIsBetter = first.genome.length < second.genome.length;
            }

        return firstIsBetter;
        }

    public NEATNetwork createNetwork()
        {
        NEATNetwork net = (NEATNetwork) (((NEATSpecies)species).networkPrototype.clone());
        net.buildNetwork(this);
        return net;
        }

    /**
     * This method convert the individual in to human readable format. It can be
     * useful in debugging.
     */
    public String toString()
        {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("\n GENOME START  ");
        stringBuffer.append("\n  genes are :" + genome.length);
        stringBuffer.append("\n  nodes are :" + nodes.size());
        
        for (int i = 0; i < nodes.size(); ++i)
            {
            NEATNode node = nodes.get(i);
            if (node.geneticNodeLabel == NodePlace.INPUT)
                stringBuffer.append("\n Input ");
            if (node.geneticNodeLabel == NodePlace.OUTPUT)
                stringBuffer.append("\n Output");
            if (node.geneticNodeLabel == NodePlace.HIDDEN)
                stringBuffer.append("\n Hidden");
            if (node.geneticNodeLabel == NodePlace.BIAS)
                stringBuffer.append("\n Bias  ");
            stringBuffer.append(node.toString());
            }

        for (int i = 0; i < genome.length; ++i)
            {
            NEATGene gene = (NEATGene) genome[i];
            stringBuffer.append(gene.toString());
            }

   
        stringBuffer.append("\n");
        stringBuffer.append(" GENOME END");
        return stringBuffer.toString();
        }

    }
