package ec.eda.dovs;

import java.util.*;
import ec.*;
import ec.util.*;
import ec.vector.*;

/**
 * HyperboxSpecies is a DOVSSpecies which contains method for updating promising
 * sample area and also sample from that area.
 *
 * @author Ermo Wei and David Freelan
 */
public class HyperboxSpecies extends DOVSSpecies
    {
    /** boxA and boxB contain the current constraint hyperbox. */
    public ArrayList<double[]> boxA;

    /** boxA and boxB contain the current constraint hyperbox. */
    public ArrayList<Double> boxB;

    public static double UPPER_BOUND = 1e31;
    public static double EPSILON_STABILITY = 1e-20;
    public static double LARGE_NUMBER = 1e32;

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state, base);
        boxA = new ArrayList<double[]>();
        boxB = new ArrayList<Double>();
        }

    /** Constructing a hyperbox, which defines the next search area. */
    public void updateMostPromisingArea(EvolutionState state)
        {
        int dimension = this.genomeSize;
        // Each time we construct a hyperbox, the previous one,
        // defined by boxA, boxB are no longer useful.
        boxA = new ArrayList<double[]>();
        boxB = new ArrayList<Double>();

        activeSolutions.clear();
        // First the original problem formulation constraints
        // copy the contents of A into boxA and b to boxB
        boxA.addAll(A);
        for (int i = 0; i < b.length; ++i)
            {
            boxB.add(b[i]);
            }

        // for each coordinate d, find xup_d and xlow_d that are closest to
        // xstar_d.
        // If one or both of xup_d and xlow_d do not exist. It is still ok
        // because
        // we have original problem constraints to bound the search region.
        for (int i = 0; i < dimension; ++i)
            {
            int key = ((IntegerVectorIndividual) visited.get(optimalIndex)).genome[i];

            // lowerBound() returns the iterator to the smallest element whose
            // key is
            // equal to or BIGGER than the argument "key". Decreasing it will
            // give the largest element
            // with a key smaller than the argument "key", if such an element
            // exists.
            CornerMap.Pair pair = corners.get(i).lowerBound(key);
            if (pair == null)
                state.output.fatal("Error. Cannot find coordnation in coordinate position map.");
            if (pair.key == key)
                {
                if (corners.get(i).hasSmaller(pair))
                    {
                    // So we fetch the previous item and use its key to do a
                    // search for all
                    // solutions with this key, if there is such a key smaller
                    // than
                    // the key of xstar
                    pair = corners.get(i).smaller(pair);
                    activeSolutions.add(pair.value);
                    double[] Atemp = new double[dimension];
                    Arrays.fill(Atemp, 0);
                    Atemp[i] = 1;
                    // The key is the coordinate position.
                    // So it is the rhs of the constraint
                    double btemp = pair.getKey();
                    boxA.add(Atemp);
                    boxB.add(btemp);
                    }
                }
            else
                {
                // This should never happen.
                state.output.fatal("Problem in constructing hyperbox");
                }

            // upper_bound returns the smallest element whose key is bigger than
            // (excluding equal to) "key",
            // if such an element exists
            pair = corners.get(i).upperBound(key);
            if (pair != null)
                {
                activeSolutions.add(pair.value);

                double[] Atemp = new double[dimension];
                Arrays.fill(Atemp, 0);
                Atemp[i] = -1;

                // The key is the coordinate position.
                // So it is the rhs of the constraint
                double btemp = pair.getKey();
                boxA.add(Atemp);
                boxB.add(btemp);
                }
            }
        }

    /** Sample from the hyperbox to get new samples for evaluation. */
    public ArrayList<Individual> mostPromisingAreaSamples(EvolutionState state, int popSize)
        {
        IntegerVectorIndividual bestIndividual = (IntegerVectorIndividual) visited.get(optimalIndex);
        int dimension = bestIndividual.genomeLength();
        int numOfConstraints = boxA.size();

        ArrayList<Individual> newSolutions = new ArrayList<Individual>();
        ArrayList<Individual> candidates = new ArrayList<Individual>();
        // TODO : do we need implement clone function here?
        IntegerVectorIndividual newInd = (IntegerVectorIndividual) bestIndividual.clone();
        ((DOVSFitness) newInd.fitness).reset();
        newSolutions.add(newInd);

        for (int i = 0; i < popSize; ++i)
            {
            // Whenever a new solution is pushed into the vector candidate, a
            // new solution is created and
            // initially it has the same content as the solution just pushed
            // into the vector.
            if (i > 0)
                {
                newInd = (IntegerVectorIndividual) ((IntegerVectorIndividual) newSolutions.get(i - 1)).clone();
                ((DOVSFitness) newInd.fitness).reset();
                newSolutions.add(newInd);
                }
            for (int j = 0; j < warmUp; ++j)
                {
                newInd = (IntegerVectorIndividual) newSolutions.get(i);
                // To warm up: Randomly pick up a dimension to move along
                int directionToMove = state.random[0].nextInt(dimension);
                double[] b1 = new double[numOfConstraints];
                for (int k = 0; k < numOfConstraints; k++)
                    {
                    // For each constraint
                    double sum = 0;
                    for (int l = 0; l < dimension; l++)
                        {
                        // Do a matrix multiplication
                        if (l != directionToMove)
                            {
                            sum += boxA.get(k)[l] * newInd.genome[l];
                            }
                        }
                    b1[k] = boxB.get(k) - sum;
                    }
                // Now check which constraint is tight
                double upper = UPPER_BOUND, lower = UPPER_BOUND;
                for (int k = 0; k < numOfConstraints; ++k)
                    {
                    double temp = 0;
                    // temp is the temporary value of x_i to make the jth
                    // constraint tight
                    if (Math.abs(boxA.get(k)[directionToMove]) > EPSILON_STABILITY)
                        temp = b1[k] / boxA.get(k)[directionToMove];
                    else
                        temp = LARGE_NUMBER;

                    if (temp > newInd.genome[directionToMove] + EPSILON_STABILITY)
                        {
                        // If the value to make the constraint tight is greater
                        // than the value of the current point,
                        // it means that there is space "above" the current
                        // point, and the upper bound could be shrinked, until
                        // the upper bound becomes the current point itself or
                        // cannot be smaller than 1.
                        if (temp - newInd.genome[directionToMove] < upper)
                            upper = temp - newInd.genome[directionToMove];
                        }
                    else if (temp < newInd.genome[directionToMove] - EPSILON_STABILITY)
                        {
                        if (newInd.genome[directionToMove] - temp < lower)
                            lower = newInd.genome[directionToMove] - temp;
                        }
                    else
                        {
                        // The constraint is already tight at current value,
                        // i.e., the point is now on the boundary. !!!!!!!!!!!
                        // If the coefficient is positive, then increasing,
                        // i.e., moving "up" will reenter feasible region
                        // because the
                        // inequalitys are Ax>=b
                        if (boxA.get(k)[directionToMove] > 0)
                            {
                            lower = 0;
                            }
                        else
                            {
                            // Don't need to worry about
                            // boxA[k][directionToMove] = 0, because in that
                            // case temp will be a large number
                            upper = 0;
                            }
                        }
                    }
                int maxXDirectionToMove = (int) Math.floor(upper) + newInd.genome[directionToMove];
                int minXDirectionToMove = newInd.genome[directionToMove] - (int) Math.floor(lower);
                int length = maxXDirectionToMove - minXDirectionToMove;
                int step = state.random[0].nextInt(length + 1);
                newInd.genome[directionToMove] = minXDirectionToMove + step;
                }
            candidates.add(newSolutions.get(i));
            }

        return candidates;
        }
    }
