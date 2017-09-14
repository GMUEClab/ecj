/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.neat;

import ec.vector.*;
import ec.*;
import ec.util.*;
import java.util.*;

/**
 * NEATSpecies is a GeneVectorSpecies which implements NEAT algorithm. The class
 * has several important methods. The breedNewPopulation(...) will first use the
 * methods in this class to determined the expected offsprings for each of the
 * subspecies, then call the reproduce of each subspecies to reproduce new
 * individuals. After one individual is created, we call speciate(...) in this
 * class to assign it to a subspecies, this could lead to creation of new
 * subspecies.
 * 
 * <p>
 * NEATSpecies must be used in combination with NEATBreeder, which will call it
 * at appropriate times to reproduce new individuals for next generations. It
 * must also be used in combination with NEATInitializer, which will use it to
 * generate the initial population.
 *
 *
 * 
 * 
 * <p>
 * <b>Parameters</b><br>
 * <table>
 * <tr>
 * <td valign=top><tt><i>base</i>.weight-mut-power</tt><br>
 * <font size=-1>Floating-point value (default is 2.5)</font></td>
 * <td valign=top>Mutation power of the link weights</td>
 * </tr>
 * <tr>
 * <td valign=top><tt><i>base</i>.disjoint-coeff</tt><br>
 * <font size=-1>Floating-point value (default is 1.0)</font></td>
 * <td valign=top>Coefficient for disjoint gene in compatibility computation
 * </td>
 * </tr>
 * <tr>
 * <td valign=top><tt><i>base</i>.excess-coeff</tt><br>
 * <font size=-1>Floating-point value (default is 1.0)</font></td>
 * <td valign=top>Coefficient for excess genes in compatibility computation</td>
 * </tr>
 * <tr>
 * <td valign=top><tt><i>base</i>.mutdiff-coeff</tt><br>
 * <font size=-1>Floating-point value (default is 0.4)</font></td>
 * <td valign=top>Coefficient for mutational difference genes in compatibility
 * computation</td>
 * </tr>
 * <tr>
 * <td valign=top><tt><i>base</i>.compat-thresh</tt><br>
 * <font size=-1>Floating-point value (default is 3.0)</font></td>
 * <td valign=top>Compatible threshold to determine if two individual are
 * compatible</td>
 * </tr>
 * <tr>
 * <td valign=top><tt><i>base</i>.age-significance</tt><br>
 * <font size=-1>Floating-point value (default is 1.0)</font></td>
 * <td valign=top>How much does age matter?</td>
 * </tr>
 * <tr>
 * <td valign=top><tt><i>base</i>.survival-thresh</tt><br>
 * <font size=-1>Floating-point value (default is 0.2)</font></td>
 * <td valign=top>Percent of ave fitness for survival</td>
 * </tr>
 * <tr>
 * <td valign=top><tt><i>base</i>.mutate-only-prob</tt><br>
 * <font size=-1>Floating-point value (default is 0.25)</font></td>
 * <td valign=top>Probability of a non-mating reproduction</td>
 * </tr>
 * <tr>
 * <td valign=top><tt><i>base</i>.mutate-link-weight-prob</tt><br>
 * <font size=-1>Floating-point value (default is 0.9)</font></td>
 * <td valign=top>Probability of doing link weight mutate</td>
 * </tr>
 * <tr>
 * <td valign=top><tt><i>base</i>.mutate-toggle-enable-prob</tt><br>
 * <font size=-1>Floating-point value (default is 0.0)</font></td>
 * <td valign=top>Probability of changing the enable status of gene</td>
 * </tr>
 * <tr>
 * <td valign=top><tt><i>base</i>.mutate-gene-reenable-prob</tt><br>
 * <font size=-1>Floating-point value (default is 0.0)</font></td>
 * <td valign=top>Probability of reenable a disabled gene</td>
 * </tr>
 * <tr>
 * <td valign=top><tt><i>base</i>.mutate-add-node-prob</tt><br>
 * <font size=-1>Floating-point value (default is 0.03)</font></td>
 * <td valign=top>Probability of doing add-node mutation</td>
 * </tr>
 * <tr>
 * <td valign=top><tt><i>base</i>.mutate-add-link-prob</tt><br>
 * <font size=-1>Floating-point value (default is 0.05)</font></td>
 * <td valign=top>Probability of doing add-link mutation</td>
 * </tr>
 * <tr>
 * <td valign=top><tt><i>base</i>.interspecies-mate-prob</tt><br>
 * <font size=-1>Floating-point value (default is 0.001)</font></td>
 r <td valign=top>Probability of doing interspecies crossover</td>
 * </tr>
 * <tr>
 * <td valign=top><tt><i>base</i>.mate-multipoint-prob</tt><br>
 * <font size=-1>Floating-point value (default is 0.6)</font></td>
 * <td valign=top>Probability of doing multipoint crossover</td>
 * </tr>
 * <tr>
 * <td valign=top><tt><i>base</i>.mate-multipoint-avg-prob</tt><br>
 * <font size=-1>Floating-point value (default is 0.4)</font></td>
 * <td valign=top>Probability of doing multipoint crossover with averaging two
 * genes</td>
 * </tr>
 * <tr>
 * <td valign=top><tt><i>base</i>.mate-singlepoint-prob</tt><br>
 * <font size=-1>Floating-point value (default is 0.0)</font></td>
 * <td valign=top>Probability of doing single point crossover (not in used in
 * this implementation, always set to 0)</td>
 * </tr>
 * <tr>
 * <td valign=top><tt><i>base</i>.mate-only-prob</tt><br>
 * <font size=-1>Floating-point value (default is 0.2)</font></td>
 * <td valign=top>Probability of mating without mutation</td>
 * </tr>
 * <tr>
 * <td valign=top><tt><i>base</i>.recur-only-prob</tt><br>
 * <font size=-1>Floating-point value (default is 0.2)</font></td>
 * <td valign=top>Probability of forcing selection of ONLY links that are
 * naturally recurrent</td>
 * </tr>
 * <tr>
 * <td valign=top><tt><i>base</i>.dropoff-age</tt><br>
 * <font size=-1>Integer (default is 15)</font></td>
 * <td valign=top>Age where Species starts to be penalized</td>
 * </tr>
 * <tr>
 * <td valign=top><tt><i>base</i>.new-link-tries</tt><br>
 * <font size=-1>Integer (default is 20)</font></td>
 * <td valign=top>Number of tries mutateAddLink will attempt to find an open
 * link</td>
 * </tr>
 * <td valign=top><tt><i>base</i>.new-node-tries</tt><br>
 * <font size=-1>Integer (default is 20)</font></td>
 * <td valign=top>Number of tries mutateAddNode will attempt to build a valid node.
 * </td>
 * </tr>
 * <td valign=top><tt><i>base</i>.add-node-max-genome-length</tt><br>
 * <font size=-1>Integer (default is 15)</font></td>
 * <td valign=top>For genomes this size or larger, mutateAddNode will do a pure random split when adding the node.
 * </td>
 * </tr>
 * <tr>
 * <td valign=top><tt><i>base</i>.babies-stolen</tt><br>
 * <font size=-1>Integer (default is 0)</font></td>
 * <td valign=top>The number of babies to siphen off to the champions</td>
 * </tr>
 * <tr>
 * <td valign=top><i>base</i>.<tt>node</tt><br>
 * <font size=-1>Classname, = ec.neat.NEATNode</font></td>
 * <td valign=top>Class of node in a network</td>
 * </tr>
 * <tr>
 * <td valign=top><i>base</i>.<tt>subspecies</tt><br>
 * <font size=-1>Classname, = ec.neat.NEATSubspecies</font></td>
 * <td valign=top>Class of subspecies in the species</td>
 * </tr>
 * <tr>
 * <td valign=top><i>base</i>.<tt>innovation</tt><br>
 * <font size=-1>Classname, = ec.neat.NEATInnovation</font></td>
 * <td valign=top>Class of innovation in the species</td>
 * </tr>
 * </table>
 * 
 * 
 * 
 * <p>
 * <b>Default Base</b><br>
 * neat.species
 * 
 * <p>
 * <b>Parameter bases</b><br>
 * <table>
 * <tr>
 * <td valign=top><i>base</i>.<tt>species</tt></td>
 * <td>species (the subpopulations' species)</td>
 * </tr>
 *
 *
 * 
 * @author Ermo Wei and David Freelan
 * 
 */
public class NEATSpecies extends GeneVectorSpecies
    {

    public enum MutationType
        {
        GAUSSIAN, COLDGAUSSIAN
        }

    // parameters
    public static final String P_SPECIES = "species";
    public static final String P_NODE = "node";
    public final static String P_NETWORK = "network";
    public static final String P_SUBSPECIES = "subspecies";
    public static final String P_INNOVATION = "innovation";
    public static final String P_WEIGHT_MUT_POWER = "weight-mut-power";
    public static final String P_DISJOINT_COEFF = "disjoint-coeff";
    public static final String P_EXCESS_COEFF = "excess-coeff";
    public static final String P_MUT_DIFF_COEFF = "mutdiff-coeff";
    public static final String P_COMPAT_THRESH = "compat-thresh";
    public static final String P_AGE_SIGNIFICANCE = "age-significance";
    public static final String P_SURVIVIAL_THRESH = "survival-thresh";
    public static final String P_MUTATE_ONLY_PROB = "mutate-only-prob";
    public static final String P_MUTATE_LINK_WEIGHT_PROB = "mutate-link-weight-prob";
    public static final String P_MUTATE_TOGGLE_ENABLE_PROB = "mutate-toggle-enable-prob";
    public static final String P_MUTATE_GENE_REENABLE_PROB = "mutate-gene-reenable-prob";
    public static final String P_MUTATE_ADD_NODE_PROB = "mutate-add-node-prob";
    public static final String P_MUTATE_ADD_LINK_PROB = "mutate-add-link-prob";
    public static final String P_INTERSPECIES_MATE_PROB = "interspecies-mate-prob";
    public static final String P_MATE_MULTIPOINT_PROB = "mate-multipoint-prob";
    public static final String P_MATE_MULTIPOINT_AVG_PROB = "mate-multipoint-avg-prob";
    public static final String P_MATE_SINGLE_POINT_PROB = "mate-singlepoint-prob";
    public static final String P_MATE_ONLY_PROB = "mate-only-prob";
    public static final String P_RECUR_ONLY_PROB = "recur-only-prob";
    public static final String P_DROPOFF_AGE = "dropoff-age";
    public static final String P_NEW_LINK_TRIES = "new-link-tries";
    public static final String P_NEW_NODE_TRIES = "new-node-tries";
    public static final String P_BABIES_STOLEN = "babies-stolen";
    public static final String P_MAX_NETWORK_DEPTH = "max-network-depth";
    public static final String P_ADD_NODE_MAX_GENOME_LENGTH = "add-node-max-genome-length";
        
    /** The prototypical node for individuals in this species. */
    public NEATNode nodePrototype;

    /** The prototypical network. */
    public NEATNetwork networkPrototype;

    /** The prototypical subspecies for individuals in this species. */
    public NEATSubspecies subspeciesPrototype;

    /** The prototypical innovation for individuals in this species. */
    public NEATInnovation innovationPrototype;

    /** Current node id that is available. */
    public int currNodeId;

    /** Current innovation number that is available. */
    private int currInnovNum;

    /** Used for delta coding, stagnation detector. */
    public double highestFitness;

    /** Used for delta coding, If too high, leads to delta coding. */
    public int highestLastChanged;


    /** The Mutation power of the link's weights. */
    public double weightMutationPower;

    /** Coefficient for disjoint gene in compatibility computation. */
    public double disjointCoeff;

    /** Coefficient for excess genes in compatibility computation. */
    public double excessCoeff;

    /**
     * Coefficient for mutational difference genes in compatibility computation.
     */
    public double mutDiffCoeff;

    /** Compatible threshold to determine if two individual are compatible. */
    public double compatThreshold;

    /** How much does age matter? */
    public double ageSignificance;

    /** Percent of ave fitness for survival. */
    public double survivalThreshold;

    /** Probility of a non-mating reproduction. */
    public double mutateOnlyProb;

    /** Probability of doing link weight mutate. */
    public double mutateLinkWeightsProb;

    /** Probability of changing the enable status of gene. */
    public double mutateToggleEnableProb;

    /** Probability of reenable a disabled gene. */
    public double mutateGeneReenableProb;

    /** Probability of doing add-node mutation. */
    public double mutateAddNodeProb;

    /** Probability of doing add-link mutation. */
    public double mutateAddLinkProb;

    /** Probability of doing interspecies crossover. */
    public double interspeciesMateRate;

    /** Probability of doing multipoint crossover. */
    public double mateMultipointProb;

    /** Probability of doing multipoint crossover with averaging two genes. */
    public double mateMultipointAvgProb;

    /**
     * Probability of doing single point crossover (not in used in this
     * implementation, always set to 0).
     */
    public double mateSinglepointProb;

    /** Probability of mating without mutation. */
    public double mateOnlyProb;

    /**
     * Probability of forcing selection of ONLY links that are naturally
     * recurrent.
     */
    public double recurOnlyProb;

    /** Age where Species starts to be penalized. */
    public int dropoffAge;

    /** Number of tries mutateAddLink will attempt to find an open link. */
    public int newLinkTries;

    /** Number of tries mutateAddNode will attempt to build a new node. */
    public int newNodeTries;

    /** The number of babies to siphen off to the champions. */
    public int babiesStolen;

    /** how deep a node can be in the network, measured by number of parents */
    public int maxNetworkDepth;
    
    /** Beyond this genome length, mutateAddNode does a pure random split rather than a bias. */
    public int addNodeMaxGenomeLength;

    public Parameter base;

    /** A list of the all the subspecies. */
    public ArrayList<NEATSubspecies> subspecies;

    /** A Hashmap for easy tracking the innovation within species. */
    public HashMap<NEATInnovation, NEATInnovation> innovations;

    public void setup(EvolutionState state, Parameter base)
        {
        Parameter def = defaultBase();

        nodePrototype = (NEATNode) (state.parameters.getInstanceForParameterEq(base.push(P_NODE), def.push(P_NODE),
                NEATNode.class));
        nodePrototype.setup(state, base.push(P_NODE));

        subspeciesPrototype = (NEATSubspecies) (state.parameters.getInstanceForParameterEq(base.push(P_SUBSPECIES),
                def.push(P_SUBSPECIES), NEATSubspecies.class));
        subspeciesPrototype.setup(state, base.push(P_SUBSPECIES));

        innovationPrototype = (NEATInnovation) (state.parameters.getInstanceForParameterEq(base.push(P_INNOVATION),
                def.push(P_INNOVATION), NEATInnovation.class));
        subspeciesPrototype.setup(state, base.push(P_INNOVATION));

        networkPrototype = (NEATNetwork) (state.parameters.getInstanceForParameterEq(base.push(P_NETWORK),
                def.push(P_NETWORK), NEATNetwork.class));
        networkPrototype.setup(state, base.push(P_NETWORK));

        // make sure that super.setup is done AFTER we've loaded our gene
        // prototype.
        super.setup(state, base);

        subspecies = new ArrayList<NEATSubspecies>();
        innovations = new HashMap<NEATInnovation, NEATInnovation>();
        highestFitness = 0;
        highestLastChanged = 0;



        // Load parameters from the parameter file
        // Load parameters from the parameter file
        weightMutationPower = state.parameters.getDouble(base.push(P_WEIGHT_MUT_POWER), def.push(P_WEIGHT_MUT_POWER), 2.5);
        disjointCoeff = state.parameters.getDouble(base.push(P_DISJOINT_COEFF), def.push(P_DISJOINT_COEFF), 1.0);
        excessCoeff = state.parameters.getDouble(base.push(P_EXCESS_COEFF), def.push(P_EXCESS_COEFF), 1.0);
        mutDiffCoeff = state.parameters.getDouble(base.push(P_MUT_DIFF_COEFF), def.push(P_MUT_DIFF_COEFF), 0.4);
        compatThreshold = state.parameters.getDouble(base.push(P_COMPAT_THRESH), def.push(P_COMPAT_THRESH), 3.0);
        ageSignificance = state.parameters.getDouble(base.push(P_AGE_SIGNIFICANCE), def.push(P_AGE_SIGNIFICANCE), 1.0);
        survivalThreshold = state.parameters.getDouble(base.push(P_SURVIVIAL_THRESH), def.push(P_SURVIVIAL_THRESH));
        mutateOnlyProb = boundProbabilityParameter(state, base, P_MUTATE_ONLY_PROB, "Mutate only probability");
        mutateLinkWeightsProb = boundProbabilityParameter(state, base, P_MUTATE_LINK_WEIGHT_PROB,"Mutate Link Weight probability");
        mutateToggleEnableProb = boundProbabilityParameter(state, base, P_MUTATE_TOGGLE_ENABLE_PROB,"Mutate Toggle Enable probability");
        mutateGeneReenableProb = boundProbabilityParameter(state, base, P_MUTATE_GENE_REENABLE_PROB, "Mutate Gene Reenable");
        mutateAddNodeProb = boundProbabilityParameter(state, base, P_MUTATE_ADD_NODE_PROB,"Mutate Add Node probability");
        mutateAddLinkProb = boundProbabilityParameter(state, base, P_MUTATE_ADD_LINK_PROB,"Mutate Add Link probability");
        interspeciesMateRate = boundProbabilityParameter(state, base, P_INTERSPECIES_MATE_PROB,"Interspecies Mate probability");
        mateMultipointProb = boundProbabilityParameter(state, base, P_MATE_MULTIPOINT_PROB,"Mate Multipoint probability");
        mateMultipointAvgProb = boundProbabilityParameter(state, base, P_MATE_MULTIPOINT_AVG_PROB,"Mate Multipoint Average probability");
        mateSinglepointProb = boundProbabilityParameter(state, base, P_MATE_SINGLE_POINT_PROB,"Single Point probability");
        mateOnlyProb = boundProbabilityParameter(state, base, P_MATE_ONLY_PROB, "Mate Only probability");
        recurOnlyProb = boundProbabilityParameter(state, base, P_RECUR_ONLY_PROB, "Recurrent Only probability");
        dropoffAge = state.parameters.getInt(base.push(P_DROPOFF_AGE), def.push(P_DROPOFF_AGE), 0);
        newLinkTries = state.parameters.getInt(base.push(P_NEW_LINK_TRIES), def.push(P_NEW_LINK_TRIES), 1);
        newNodeTries = state.parameters.getInt(base.push(P_NEW_NODE_TRIES), def.push(P_NEW_NODE_TRIES), 1);
        babiesStolen = state.parameters.getInt(base.push(P_BABIES_STOLEN), def.push(P_BABIES_STOLEN), 0);
        maxNetworkDepth = state.parameters.getInt(base.push(P_MAX_NETWORK_DEPTH), base.push(P_MAX_NETWORK_DEPTH), 30);
        addNodeMaxGenomeLength = state.parameters.getInt(base.push(P_ADD_NODE_MAX_GENOME_LENGTH), base.push(P_ADD_NODE_MAX_GENOME_LENGTH), 15);
        }

    double boundProbabilityParameter(EvolutionState state, Parameter base, String param, String description)
        {
        Parameter def = defaultBase();
        double probability = state.parameters.getDoubleWithMax(base.push(param), def.push(param), 0.0, 1.0);
        if (probability < 0.0)
            state.output.fatal(description + " is a probability, and must be a value between 0.0 and 1.0.");
        return probability;
        }

    public Parameter defaultBase()
        {
        return NEATDefaults.base().push(P_SPECIES);
        }


    private Object innoLock = new Object[0];  // arrays are synchronizable, so make good locks
    public int nextInnovationNumber()
        {
        synchronized(innoLock) { return currInnovNum++; }
        }

    public void setInnovationNumber(int num)
        {
        synchronized(innoLock) {currInnovNum = num;}
        }

    /** Assign the individual into a species, if not found, create a new one */
    public void speciate(EvolutionState state, Individual ind)
        {
       
        NEATIndividual neatInd = (NEATIndividual) ind;
        // For each individual, search for a subspecies it is compatible to
        if (subspecies.size() == 0) // not subspecies available, create the
            // first species
            {
            NEATSubspecies newSubspecies = (NEATSubspecies) subspeciesPrototype.emptyClone();
            newSubspecies.reset();
            subspecies.add(newSubspecies);
            newSubspecies.addNewGenIndividual(neatInd);
            }
        else
            {
            boolean found = false;
            for (int i = 0; i < subspecies.size(); ++i)
                {
                NEATIndividual represent = (NEATIndividual) subspecies.get(i).newGenerationFirst();
                if (represent == null)
                    represent = (NEATIndividual) subspecies.get(i).first();

                // found compatible subspecies, add this individual to it
                if (compatibility(neatInd, represent) < compatThreshold)
                    {
                    
                    subspecies.get(i).addNewGenIndividual(neatInd);
                    found = true; // change flag
                    break; // search is over, quit loop
                    }
                }
            // if we didn't find a match, create a new subspecies
            if (!found)
                {
                NEATSubspecies newSubspecies = (NEATSubspecies) subspeciesPrototype.emptyClone();
                newSubspecies.reset();
                subspecies.add(newSubspecies);
                newSubspecies.addNewGenIndividual(neatInd);
                }
            }
            

        }

    /** Spawn a new individual with given individual as template. */
    public NEATIndividual spawnWithTemplate(EvolutionState state, NEATSpecies species, int thread, NEATIndividual ind)
        {
        // we clone but do not reset the individual, since these individuals are
        // made from template
        NEATIndividual newInd = (NEATIndividual) ind.clone();
        // for first generation of population, we do not use the weight mutation
        // power from the file
        newInd.mutateLinkWeights(state, thread, species, 1.0, 1.0, MutationType.GAUSSIAN);
        newInd.setGeneration(state);
        newInd.createNetwork(); // we create the network after we have the
        // complete genome
        return newInd;
        }

    /**
     * This function gives a measure of compatibility between two Genomes by
     * computing a linear combination of 3 characterizing variables of their
     * compatibilty. The 3 variables represent PERCENT DISJOINT GENES, PERCENT
     * EXCESS GENES, MUTATIONAL DIFFERENCE WITHIN MATCHING GENES. So the formula
     * for compatibility is:
     * disjointCoeff*numDisjoint+excessCoeff*numExcess+mutdiffCoeff*numMatching.
     */
    public double compatibility(NEATIndividual a, NEATIndividual b)
        {


        int numExcess = 0;
        int numMatching = 0;
        int numDisjoint = 0;
        double mutTotalDiff = 0.0;
        // pointer for two genome
        int i = 0, j = 0;
        while (!(i == a.genome.length && j == b.genome.length))
            {
            // if genome a is already finished, move b's pointer
            if (i == a.genome.length)
                {
                j++;
                numExcess++;
                }
            // if genome b is already finished, move a's pointer
            else if (j == b.genome.length)
                {
                i++;
                numExcess++;
                }
            else
                {
                int aInno = ((NEATGene) a.genome[i]).innovationNumber;
                int bInno = ((NEATGene) b.genome[j]).innovationNumber;
                if (aInno == bInno)
                    {
                    numMatching++;
                    double mutDiff = Math
                        .abs(((NEATGene) a.genome[i]).mutationNumber - ((NEATGene) b.genome[j]).mutationNumber);
                    mutTotalDiff += mutDiff;
                    i++;
                    j++;
                    }
                // innovation number do not match, skip this one
                else if (aInno < bInno)
                    {
                    i++;
                    numDisjoint++;
                    }
                else if (bInno < aInno)
                    {
                    j++;
                    numDisjoint++;
                    }
                }
            }

        // Return the compatibility number using compatibility formula
        // Note that mutTotalDiff/numMatching gives the AVERAGE
        // difference between mutationNums for any two matching Genes
        // in the Genome

        // We do not normalize the terms in here due to the following reason

        // If you decide to use the species compatibility coefficients and
        // thresholds from my own .ne settings files (provided with my NEAT
        // release), then do not normalize the terms in the compatibility
        // function, because I did not do this with my .ne files. In other
        // words, even though my papers suggest normalizing (dividing my number
        // of genes), since I didn't do that the coefficients that I used will
        // not work the same for you if you normalize. If you strongly desire to
        // normalize, you will need to find your own appropriate coefficients
        // and threshold.

        // see the comments above on NEAT page
        // https://www.cs.ucf.edu/~kstanley/neat.html

        // Normalizing for genome size
        // return (disjointCoeff*(numDisjoint/maxGenomeSize)+
        // excessCoeff*(numExcess/maxGenomeSize)+
        // mutDiffCoeff*(mutTotalDiff/numMatching));

        double compatibility = disjointCoeff * (((double) numDisjoint) / 1.0);
        compatibility += excessCoeff * (((double) numExcess) / 1.0);
        compatibility += mutDiffCoeff * (mutTotalDiff / ((double) numMatching));
        
        

        return compatibility;
        }

    /** Determine the offsprings for all the subspecies. */
    public void countOffspring(EvolutionState state, int subpop)
        {
        // Go through the organisms and add up their adjusted fitnesses to
        // compute the overall average
        double total = 0.0;
        ArrayList<Individual> inds = state.population.subpops.get(subpop).individuals;
        for (int i = 0; i < inds.size(); ++i)
            {
            total += ((NEATIndividual) inds.get(i)).adjustedFitness;

            }

        double overallAverage = total / inds.size();

        // Now compute expected number of offspring for each individual organism
        for (int i = 0; i < inds.size(); ++i)
            {
            ((NEATIndividual) inds.get(i)).expectedOffspring = ((NEATIndividual) inds.get(i)).adjustedFitness
                / overallAverage;


            }

        // Now add those offsprings up within each Subspecies to get the number
        // of
        // offspring per subspecies
        double skim = 0.0;
        int totalExpected = 0;
        for (int i = 0; i < subspecies.size(); ++i)
            {
            NEATSubspecies subs = subspecies.get(i);
            skim = subs.countOffspring(skim);
            totalExpected += subs.expectedOffspring;
            }

       

        // Need to make up for lost floating point precision in offspring
        // assignment. If we lost precision, give an extra baby to the best
        // subpecies
        if (totalExpected < inds.size())
            {
            // Find the subspecies expecting the most
            int maxExpected = 0;
            int finalExpected = 0;
            NEATSubspecies best = null;
            for (int i = 0; i < subspecies.size(); ++i)
                {
                if (subspecies.get(i).expectedOffspring >= maxExpected)
                    {
                    maxExpected = subspecies.get(i).expectedOffspring;
                    best = subspecies.get(i);
                    }
                finalExpected += subspecies.get(i).expectedOffspring;
                }

            // Give the extra offspring to the best subspecies
            best.expectedOffspring++;
            finalExpected++;

            // If we still aren't at total, there is a problem
            // Note that this can happen if a stagnant subpecies
            // dominates the population and then gets killed off by its age
            // Then the whole population plummets in fitness
            // If the average fitness is allowed to hit 0, then we no longer
            // have an average we can use to assign offspring.
            if (finalExpected < inds.size())
                {
                state.output.warnOnce("Population has died");
                for (int i = 0; i < subspecies.size(); ++i)
                    {
                    subspecies.get(i).expectedOffspring = 0;
                    }
                best.expectedOffspring = inds.size();
                }
            }
        }

    /**
     * Breed a new generation of population, this is done by first figure the
     * expected offsprings for each subspecies, and then calls each subspecies
     * to reproduce.
     */
    public void breedNewPopulation(EvolutionState state, int subpop, int thread)
        {
        // see epoch method in Population
        ArrayList<Individual> inds = state.population.subpops.get(subpop).individuals;

        clearEvaluationFlag(inds);

        // clear the innovation information of last generation
        innovations.clear();

        // we also ignore the code for competitive coevolution stagnation
        // detection

        // Use Species' ages to modify the objective fitness of organisms
        // in other words, make it more fair for younger species
        // so they have a chance to take hold
        // Also penalize stagnant species
        // Then adjust the fitness using the species size to "share" fitness
        // within a species.
        // Then, within each Species, mark for death
        // those below survivalThresh * average
        for (int i = 0; i < subspecies.size(); ++i)
            {
            subspecies.get(i).adjustFitness(state, dropoffAge, ageSignificance);
            subspecies.get(i).sortIndividuals();
            subspecies.get(i).updateSubspeciesMaxFitness();
            subspecies.get(i).markReproducableIndividuals(survivalThreshold);
            }

        // count the offspring for each subspecies
        countOffspring(state, subpop);

        // sort the subspecies use extra list based on the max fitness
        // these need to use original fitness, descending order
        ArrayList<NEATSubspecies> sortedSubspecies = new ArrayList<NEATSubspecies>(subspecies);
        Collections.sort(sortedSubspecies, new Comparator<NEATSubspecies>()
                {
                @Override
                public int compare(NEATSubspecies o1, NEATSubspecies o2)
                    {
                    NEATIndividual ind1 = (NEATIndividual) o1.individuals.get(0);
                    NEATIndividual ind2 = (NEATIndividual) o2.individuals.get(0);

                    if (ind1.fitness.fitness() < ind2.fitness.fitness())
                        return 1;
                    if (ind1.fitness.fitness() > ind2.fitness.fitness())
                        return -1;
                    return 0;
                    }
            });

        // Check for population-level stagnation code
        populationStagnation(state, subpop, sortedSubspecies);

        // Check for stagnation if there is stagnation, perform delta-coding
        // TODO: fix weird constant
        if (highestLastChanged >= dropoffAge + 5)
            {
            deltaCoding(state, subpop, sortedSubspecies);
            }
        // STOLEN BABIES: The system can take expected offspring away from
        // worse species and give them to superior species depending on
        // the system parameter babies_stolen (when babies_stolen > 0)
        else if (babiesStolen > 0)
            {
            stealBabies(state,thread , subpop, sortedSubspecies);
            }

        // Kill off all Individual marked for death. The remainder
        // will be allowed to reproduce.
        // NOTE this result the size change of individuals in each subspecies
        // however, it doesn't effect the individuals for the whole neat
        // population
        for (int i = 0; i < sortedSubspecies.size(); ++i)
            {
            sortedSubspecies.get(i).removePoorFitnessIndividuals();
            }

        // Reproduction
        // Perform reproduction. Reproduction is done on a per-Species
        // basis. (So this could be paralellized potentially.)
        // we do this with sortedSubspecies instead of subspecies
        // this is due to the fact that new subspecies could be created during
        // the reproduction period
        // thus, the sortedSubspecies are guarantee to contain all the old
        // subspecies
        for (int i = 0; i < sortedSubspecies.size(); ++i)
            {
            // first for all current subspecies, clear their new generation
            // individuals
            NEATSubspecies subs = sortedSubspecies.get(i);
            subs.newGenIndividuals.clear();
            }

        for (int i = 0; i < sortedSubspecies.size(); ++i)
            {
            NEATSubspecies subs = sortedSubspecies.get(i);
            subs.reproduce(state, thread, subpop, sortedSubspecies);
            }

        // Remove all empty subspecies and age ones that survive
        // As this happens, create master individuals list for the new
        // generation

        // first age the old subspecies
        for (int i = 0; i < sortedSubspecies.size(); ++i)
            {
            NEATSubspecies subs = sortedSubspecies.get(i);
            subs.age++;
            }
        ArrayList<NEATSubspecies> remainSubspecies = new ArrayList<NEATSubspecies>();
        ArrayList<Individual> newGenIndividuals = new ArrayList<Individual>();
        for (int i = 0; i < subspecies.size(); ++i)
            {
            if (subspecies.get(i).hasNewGeneration())
                {
                // add to the remaining subspecies
                remainSubspecies.add(subspecies.get(i));
                subspecies.get(i).toNewGeneration();
                // add to the new generation population
                newGenIndividuals.addAll(subspecies.get(i).individuals);
                }
            }
        // replace the old stuff
        subspecies = remainSubspecies;

        state.population.subpops.get(subpop).individuals = newGenIndividuals;
        }

    /** Perform a delta coding. */
    public void deltaCoding(EvolutionState state, int subpop, ArrayList<NEATSubspecies> sortedSubspecies)
        {
        highestLastChanged = 0;

        int popSize = state.population.subpops.get(subpop).initialSize;
        int halfPop = popSize / 2;

        NEATSubspecies bestFitnessSubspecies = sortedSubspecies.get(0);
        // the first individual of the first subspecies can have 1/2 pop size
        // offsprings
        ((NEATIndividual) bestFitnessSubspecies.first()).superChampionOffspring = halfPop;
        // the first subspecies can have 1/2 pop size offspring
        bestFitnessSubspecies.expectedOffspring = halfPop;
        bestFitnessSubspecies.ageOfLastImprovement = bestFitnessSubspecies.age;

        if (sortedSubspecies.size() >= 2)
            {
            // the second subspecies can have the other half pop size
            ((NEATIndividual) sortedSubspecies.get(1).first()).superChampionOffspring = popSize - halfPop;
            sortedSubspecies.get(1).expectedOffspring = popSize - halfPop;
            sortedSubspecies.get(1).ageOfLastImprovement = sortedSubspecies.get(1).age;
            // the remainder subspecies has 0 offsprings
            for (int i = 2; i < sortedSubspecies.size(); ++i)
                {
                sortedSubspecies.get(i).expectedOffspring = 0;
                }
            }
        else
            {
            ((NEATIndividual) bestFitnessSubspecies.first()).superChampionOffspring += popSize - halfPop;
            bestFitnessSubspecies.expectedOffspring = popSize - halfPop;
            }
        }

    /** Determine if the whole subpopulation get into stagnation. */
    public void populationStagnation(EvolutionState state, int subpop, ArrayList<NEATSubspecies> sortedSubspecies)
        {
        NEATIndividual bestFitnessIndividual = (NEATIndividual) sortedSubspecies.get(0).individuals.get(0);
        bestFitnessIndividual.popChampion = true;
        if (bestFitnessIndividual.fitness.fitness() > highestFitness)
            {
            highestFitness = bestFitnessIndividual.fitness.fitness();
            highestLastChanged = 0;
            //state.output.message("Population has reached a new RECORD FITNESS " + highestFitness);
            }
        else
            {
            highestLastChanged++;
            //state.output.message(
            //    highestLastChanged + " generations since last population fitness record " + highestFitness);
            }
        }

    /** Steal the babies from champion subspecies. */
    public void stealBabies(EvolutionState state, int thread,int subpop, ArrayList<NEATSubspecies> sortedSubspecies)
        {
        // Take away a constant number of expected offspring from the worst few
        // species
        int babiesAlreadyStolen = 0;

        for (int i = sortedSubspecies.size() - 1; i >= 0 && babiesAlreadyStolen < babiesStolen; i--)
            {
            NEATSubspecies subs = sortedSubspecies.get(i);

            if (subs.age > 5 && subs.expectedOffspring > 2)
                {
                // This subspecies has enough to finish off the stolen pool
                int babiesNeeded = babiesStolen - babiesAlreadyStolen;
                if (subs.expectedOffspring - 1 >= babiesNeeded)
                    {
                    subs.expectedOffspring -= babiesNeeded;
                    babiesAlreadyStolen = babiesStolen;
                    }
                // Not enough here to complete the pool of stolen, then leave
                // one individual
                // for that subspecies
                else
                    {
                    babiesAlreadyStolen += subs.expectedOffspring - 1;
                    subs.expectedOffspring = 1;
                    }
                }
            }

        // Mark the best champions of the top subspecies to be the super
        // champions
        // who will take on the extra offspring for cloning or mutant cloning
        // Determine the exact number that will be given to the top three
        // They get, in order, 1/5 1/5 and 1/10 of the already stolen babies
        int[] quote = new int[3];
        quote[0] = quote[1] = babiesStolen / 5;
        quote[2] = babiesStolen / 10;

        boolean done = false;
        int quoteIndex = 0;
        Iterator<NEATSubspecies> iterator = sortedSubspecies.iterator();

        while (!done && iterator.hasNext())
            {
            NEATSubspecies subs = iterator.next();
            // Don't give to dying species even if they are champions
            if (subs.timeSinceLastImproved() <= dropoffAge)
                {
                if (quoteIndex < quote.length)
                    {
                    if (babiesAlreadyStolen > quote[quoteIndex])
                        {
                        ((NEATIndividual) subs.first()).superChampionOffspring = quote[quoteIndex];
                        subs.expectedOffspring += quote[quoteIndex];
                        babiesAlreadyStolen -= quote[quoteIndex];
                        }
                    quoteIndex++;
                    }
                else if (quoteIndex >= quote.length)
                    {
                    // Randomize a little which species get boosted by a super
                    // champion
                    if (state.random[thread].nextBoolean(.9))
                        {
                        if (babiesAlreadyStolen > 3)
                            {
                            ((NEATIndividual) subs.first()).superChampionOffspring = 3;
                            subs.expectedOffspring += 3;
                            babiesAlreadyStolen -= 3;
                            }
                        else
                            {
                            ((NEATIndividual) subs.first()).superChampionOffspring = babiesAlreadyStolen;
                            subs.expectedOffspring += babiesAlreadyStolen;
                            babiesAlreadyStolen = 0;
                            }
                        }
                    }
                // assiged all the stolen babies
                if (babiesAlreadyStolen == 0)
                    done = true;
                }
            }

        // If any stolen babies aren't taken, give them to species #1's champion
        if (babiesAlreadyStolen > 0)
            {
            state.output.message("Not all stolen babies assigned, giving to the best subspecies");
            NEATSubspecies subs = subspecies.get(0);
            ((NEATIndividual) subs.first()).superChampionOffspring += babiesAlreadyStolen;
            subs.expectedOffspring += babiesAlreadyStolen;
            babiesAlreadyStolen = 0;
            }

        }



    /** Create a new individual with given nodes and genes */
    public Individual newIndividual(EvolutionState state, int thread, ArrayList<NEATNode> nodes,
        ArrayList<Gene> genes)
        {
        NEATIndividual newind = (NEATIndividual) (super.newIndividual(state, thread));
        newind.reset(nodes, genes);
        return newind;
        }

    public boolean hasInnovation(NEATInnovation inno)
        {
        return innovations.containsKey(inno);
        }

    public NEATInnovation getInnovation(NEATInnovation inno)
        {
        return innovations.get(inno);
        }

    public void addInnovation(NEATInnovation inno)
        {
        innovations.put(inno, inno);
        }

    /**
     * Clear the evaluation flag in each individual. This is important if a
     * evaluation individual mutated.
     */
    public void clearEvaluationFlag(ArrayList<Individual> individuals)
        {
        for (int i = 0; i < individuals.size(); ++i)
            {
            individuals.get(i).evaluated = false;
            }
        }

    }
