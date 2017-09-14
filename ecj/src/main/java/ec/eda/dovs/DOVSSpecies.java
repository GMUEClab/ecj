
package ec.eda.dovs;

import ec.*;
import ec.vector.*;
import ec.util.*;
import java.util.*;

/**
 * DOVSSpecies is a IntegerVectorSpecies which implements DOVS algorithm. The
 * two most important method for a Species in DOVS problem is
 * updateMostPromisingArea(...) and mostPromisingAreaSamples(...). We call these
 * two methods in sequence to first determine an area around best individual and
 * sample new individual from that area. However, there are several ways to
 * implements these two methods, thus, we let the subclasses to determine the
 * implementation of these two method, e.g. HyperboxSpecies.
 * 
 * 
 * <p>
 * DOVSSpecies must be used in combination with DOVSBreeder, which will call it
 * at appropriate times to reproduce new individuals for next generations. It
 * must also be used in combination with DOVSInitializer and DOVSEvaluator. The
 * former will be used to generate the initial population, and the later will
 * determine a suitable number of evaluation for each individual.
 *
 *
 * <p>
 * <b>Parameters</b><br>
 * <table>
 * <tr>
 * <td valign=top><tt><i>base</i>.initial-reps</tt><br>
 * <font size=-1>Integer &gt; 1</font></td>
 * <td valign=top>Base value of number of evaluations for each individual.</td>
 * </tr>
 * <tr>
 * <td valign=top><tt><i>base</i>.warmup</tt><br>
 * <font size=-1>Integer &gt; 1</font></td>
 * <td valign=top>Number of trial we want to randomize one dimension of the
 * individual, used for sampling.</td>
 * </tr>
 * <tr>
 * <td valign=top><tt><i>base</i>.constraints-size</tt><br>
 * <font size=-1>Integer</font></td>
 * <td valign=top>Number of constraints for the initial optimization problem.
 * link</td>
 * </tr>
 * <tr>
 * <td valign=top><tt><i>base</i>.constraints-A</tt><br>
 * <font size=-1>String</font></td>
 * <td valign=top>A string of double number separate by whitespace specified the
 * left hand side coefficients of the constraint Ax<=b.</td>
 * </tr>
 * <tr>
 * <td valign=top><tt><i>base</i>.constraints-B</tt><br>
 * <font size=-1>Double</font></td>
 * <td valign=top>A double number specified the right hand side of the constraint Ax&lt;=b.</td>
 * </tr>
 * <tr>
 * <td valign=top><tt><i>base</i>.stochastic</tt><br>
 * <font size=-1>Boolean (default = false)</font></td>
 * <td valign=top>Is it the problem a stochastic problem?</td>
 * </tr>
 * </table>
 * 
 * 
 * 
 * <p>
 * <b>Default Base</b><br>
 * dovs.species
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

public class DOVSSpecies extends IntegerVectorSpecies
    {

    public static final String P_DOVS_SPECIES = "species";
    public static final String P_INITIAL_REPETITION = "initial-reps";
    public static final String P_STOCHASTIC = "stochastic";
    //public static final String P_OCBA = "ocba";
    public static final String P_CONSTRAINTS_SIZE = "constraints-size";
    public static final String P_A = "constraints-A";
    public static final String P_B = "constraints-b";
    public static final String P_WARM_UP = "warmup";

    /**
     * This integer indicate the index of optimal individual in the visited
     * array.
     */
    public int optimalIndex = -1;

    /** warm up period for RMD sampling. */
    public int warmUp;

    /**
     * This list contains all the sample we have visited during current
     * algorithm run.
     */
    public ArrayList<Individual> visited;

    /**
     * Given a individual, return the index of this individual in ArrayList
     * visited
     */
    public HashMap<Individual, Integer> visitedIndexMap;

    /**
     * CornerMaps for the all the visisted individuals. This record the
     * key-value pair for each individuals, where key is the coordinates and
     * value is individual itself.
     */
    public ArrayList<CornerMap> corners;

    /**
     * activeSolutions contains all the samples that is on the boundary of the
     * most promising area.
     */
    public ArrayList<Individual> activeSolutions;

    /**
     * This is the Ek in original paper, where is the collection all the
     * individuals evaluated in generation k.
     */
    public ArrayList<Individual> Ek;

    /* Ocba flag. */
    //public boolean ocba;

    /** Is the problem a stochastic problem. */
    public boolean stochastic;

    /** Base value of number evaluation for each individual. */
    public int initialReps;

    /**
     * This value will be updated at each generation to determine how many
     * evaluation is needed for one individual. It make use of the initialReps.
     */
    public int repetition;

    /** This is for future using. */
    public long numOfTotalSamples = 0;

    /** Constraint coefficients */
    public ArrayList<double[]> A;

    /** Constratin coefficients */
    public double[] b;

    public Parameter defaultBase()
        {
        return DOVSDefaults.base().push(P_DOVS_SPECIES);
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state, base);
                
        activeSolutions = new ArrayList<Individual>();
        Ek = new ArrayList<Individual>();
        visited = new ArrayList<Individual>();
        visitedIndexMap = new HashMap<Individual, Integer>();
        corners = new ArrayList<CornerMap>();
        // initialize corner map
        for (int i = 0; i < genomeSize; ++i)
            corners.add(new CornerMap());

        Parameter def = defaultBase();

        stochastic = state.parameters.getBoolean(base.push(P_STOCHASTIC), def.push(P_STOCHASTIC), true);
        //ocba = state.parameters.getBoolean(base.push(P_OCBA), def.push(P_OCBA), true);

        initialReps = state.parameters.getInt(base.push(P_INITIAL_REPETITION), def.push(P_INITIAL_REPETITION), 1);
        if (initialReps < 1)
            state.output.fatal("Initial number of repetitions must be >= 1", base.push(P_INITIAL_REPETITION), def.push(P_INITIAL_REPETITION));

        warmUp = state.parameters.getInt(base.push(P_WARM_UP), def.push(P_WARM_UP), 1);
        if (warmUp < 1)
            state.output.fatal("Warm-up Period must be >= 1", base.push(P_WARM_UP), def.push(P_WARM_UP));

        // read in the constraint
        int size = state.parameters.getInt(base.push(P_CONSTRAINTS_SIZE), def.push(P_CONSTRAINTS_SIZE), 0);

        A = new ArrayList<double[]>();
        b = new double[size];

        if (size > 0)
            {
            // Set up the constraints for A
            for (int x = 0; x < size; x++)
                {
                Parameter p = base.push(P_A).push("" + x);
                Parameter defp = def.push(P_A).push("" + x);
                                
                double[] d = state.parameters.getDoublesUnconstrained(p, defp, this.genomeSize);
                if (d == null)
                    state.output.fatal("Row " + x + " of DOVSSpecies constraints array A must be a space- or tab-delimited list of exactly " + this.genomeSize + " numbers.",
                        p, defp); 
                A.add(d);
                }
                        
            Parameter p = base.push(P_B);
            Parameter defp = def.push(P_B);
                                
            b = state.parameters.getDoublesUnconstrained(p, defp, size);
            if (b == null)
                state.output.fatal("DOVSSpecies constraints vector b must be a space- or tab-delimited list of exactly " + size + " numbers.",
                    p, defp); 

            }

        repetition = stochastic ? initialReps : 1;

        }

    /**
     * Define a most promising area for search of next genertion of individuals.
     */
    public void updateMostPromisingArea(EvolutionState state)
        {
        throw new UnsupportedOperationException("updateMostPromisingArea method not implementd!");
        }

    /**
     * Sample from the most promising area to get new generation of individual
     * for evaluation.
     */
    public ArrayList<Individual> mostPromisingAreaSamples(EvolutionState state, int size)
        {
        throw new UnsupportedOperationException("mostPromisingAreaSamples method not implementd!");
        }

    /**
     * To find the best sample for each generation, we need to go through each
     * individual in the current population, and also best individual and
     * individuals in actionSolutions. These three type of individuals are
     * exactly the individuals evaluated in DOVSEvaluator.
     */
    public void findBestSample(EvolutionState state, Subpopulation subpop)
        {
        // clear Ek
        Ek.clear();

        ArrayList<Individual> individuals = subpop.individuals;
        for (int i = 0; i < individuals.size(); ++i)
            Ek.add(individuals.get(i));
        for (int i = 0; i < activeSolutions.size(); ++i)
            Ek.add(activeSolutions.get(i));
        Ek.add(visited.get(optimalIndex));
        optimalIndex = findOptimalIndividual(Ek);
        }

    /**
     * Given a list of individuals, it will find the one with highest fitness
     * value and retrieve its index in visited solution list.
     */
    private int findOptimalIndividual(ArrayList<Individual> list)
        {
        double maximum = Integer.MIN_VALUE;
        IntegerVectorIndividual bestInd = null;
        for (int i = 0; i < list.size(); ++i)
            {
            IntegerVectorIndividual ind = (IntegerVectorIndividual) list.get(i);
            if (((DOVSFitness)(ind.fitness)).mean > maximum)
                {
                maximum = ((DOVSFitness)(ind.fitness)).mean;
                bestInd = ind;
                }
            }

        return visitedIndexMap.get(bestInd);
        }

    /**
     * This method will take a candidate list and identify is there is redundant
     * individual in it. If yes, it will get rid of the redundant individuals.
     * After that, it will check if all the samples from this generation have
     * been visited in previous generation. If yes, it will retrieve the samples
     * from previous generations.
     */
    public ArrayList<Individual> uniqueSamples(EvolutionState state, ArrayList<Individual> candidates)
        {
        // first filter out the redundant sample with in the set of candidates
        HashSet<Individual> set = new HashSet<Individual>();
        for (int i = 0; i < candidates.size(); ++i)
            {
            if (!set.contains(candidates.get(i)))
                set.add(candidates.get(i));
            }
        // now all the individual in candidates are unique with in the set
        candidates = new ArrayList<Individual>(set);

        // Sk will be the new population
        ArrayList<Individual> Sk = new ArrayList<Individual>();

        // see if we have these individual in visted array before
        for (int i = 0; i < candidates.size(); ++i)
            {
            IntegerVectorIndividual individual = (IntegerVectorIndividual) candidates.get(i);
            if (visitedIndexMap.containsKey(individual))
                {
                // we have this individual before, retrieve that
                int index = visitedIndexMap.get(individual);
                // get the original individual
                individual = (IntegerVectorIndividual) visited.get(index);
                }
            else
                {
                visited.add(individual);
                visitedIndexMap.put(individual, visited.size() - 1);

                // We add the new individual into the CornerMap
                // NOTE: if the individual already, we still need to do this?
                // original code says yes, but it seems to be wrong
                // so we do this only the new individual is new
                for (int j = 0; j < genomeSize; ++j)
                    {
                    // The individual is the content. The key is its
                    // coordinate position
                    corners.get(j).insert(individual.genome[j], individual);
                    }
                }

            Sk.add(individual);
            }

        return Sk;
        }
    }
