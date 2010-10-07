/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.multiobjective;

import java.io.*;
import ec.util.DecodeReturn;
import ec.util.Parameter;
import ec.util.Code;
import ec.Fitness;
import ec.EvolutionState;
import java.util.*;
import ec.*;

/* 
 * MultiObjectiveFitness.java
 * 
 * Created: Tue Aug 10 20:27:38 1999
 * By: Sean Luke
 */

/**
 * MultiObjectiveFitness is a subclass of Fitness which implements basic
 * multi-objective mechanisms suitable for being used with a variety of
 * multi-objective selection mechanisms, including ones using pareto-optimality.
 * 
 * <p>
 * The object contains two items: an array of floating point values representing
 * the various multiple fitnesses, and a flag (maximize) indicating whether
 * higher is considered better. By default, isIdealFitness() always returns
 * false; you'll probably want to override that [if appropriate to your
 * problem].
 * 
 * <p>
 * The object also contains maximum and minimum fitness values suggested for the
 * problem, on a per-objective basis. By default the maximum values are all 1.0
 * and the minimum values are all 0.0, but you can change these. Note that
 * maximum does not mean "best" unless maximize is true.
 * 
 * <p>
 * <b>Parameters</b><br>
 * <table>
 * <tr>
 * <td valign=top><i>base</i>.<tt>num-objectives</tt><br>
 * (else)<tt>multi.num-objectives</tt><br>
 * <font size=-1>int &gt;= 1</font></td>
 * <td valign=top>(the number of fitnesses in the objectives array)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i>.<tt>maximize</tt><br>
 * <font size=-1> bool = <tt>true</tt> (default) or <tt>false</tt></font></td>
 * <td valign=top>(are higher values considered "better"?)
 * </table>
 * 
 * <tr>
 * <td valign=top><i>base</i>.<tt>max</tt><br>
 * <font size=-1> float (<tt>1.0</tt> default)</font></td>
 * <td valign=top>(maximum fitness value for all objectives)</table>
 * 
 * <tr>
 * <td valign=top><i>base</i>.<tt>max</tt>.<i>i</i><br>
 * <font size=-1> float (<tt>1.0</tt> default)</font></td>
 * <td valign=top>(maximum fitness value for objective <i>i</i>. Overrides the
 * all-objective maximum fitness.)</table>
 * 
 * <tr>
 * <td valign=top><i>base</i>.<tt>min</tt><br>
 * <font size=-1> float (<tt>0.0</tt> (default)</font></td>
 * <td valign=top>(minimum fitness value for all objectives)</table>
 * 
 * <tr>
 * <td valign=top><i>base</i>.<tt>min</tt>.<i>i</i><br>
 * <font size=-1> float = <tt>0.0</tt> (default)</font></td>
 * <td valign=top>(minimum fitness value for objective <i>i</i>. Overrides the
 * all-objective minimum fitness.)</table>
 * 
 * <p>
 * <b>Default Base</b><br>
 * multi.fitness
 * 
 * @author Sean Luke
 * @version 1.1
 */

public class MultiObjectiveFitness extends Fitness
    {
    public static final String MULTI_FITNESS_POSTAMBLE = "[";
    public static final String FITNESS_POSTAMBLE = "]";

    /** parameter for size of objectives */
    public static final String P_NUMOBJECTIVES = "num-objectives";

    /** parameter for max fitness values */
    public static final String P_MAXFITNESSES = "max";

    /** parameter for min fitness values */
    public static final String P_MINFITNESSES = "min";

    /** Is higher better? */
    public static final String P_MAXIMIZE = "maximize";

    /** Desired maximum fitness values. By default these are 1.0. Shared. */
    public float[] maxfitness;

    /** Desired minimum fitness values. By default these are 0.0. Shared. */
    public float[] minfitness;

    /** The various fitnesses. */
    protected float[] objectives; // values range from 0 (worst) to 1 INCLUSIVE
    protected boolean maximize = true;

	/** Returns auxilliary fitness value names to be printed by the statistics object.
		By default, an empty array is returned, but various algorithms may override this to provide additional columns.
		*/
	public String[] getAuxilliaryFitnessNames() { return new String[] { }; }

	/** Returns auxilliary fitness values to be printed by the statistics object.
		By default, an empty array is returned, but various algorithms may override this to provide additional columns.
		*/
	public double[] getAuxilliaryFitnessValues() { return new double[] { }; }

    public boolean isMaximizing()
        {
        return maximize;
        }

    /**
     * Returns the objectives as an array. Note that this is the *actual array*.
     * Though you could set values in this array, you should NOT do this --
     * rather, set them using setObjectives().
     */
    public float[] getObjectives()
        {
        return objectives;
        }

    public float getObjective(int i)
        {
        return objectives[i];
        }

    public void setObjectives(final EvolutionState state, float[] newObjectives)
        {
        if (newObjectives == null)
            {
            state.output.fatal("Null objective array provided to MultiObjectiveFitness.");
            }
        if (newObjectives.length != objectives.length)
            {
            state.output.fatal("New objective array length does not match current length.");
            }
        for (int i = 0; i < newObjectives.length; i++)
            {
            float _f = newObjectives[i];
            if (_f == Float.POSITIVE_INFINITY || _f == Float.NEGATIVE_INFINITY || Float.isNaN(_f))
                {
                state.output.warning("Bad objective #" + i + ": " + _f + ", setting to worst value for that objective.");
                if (maximize)
                    newObjectives[i] = minfitness[i];
                else
                    newObjectives[i] = maxfitness[i];
                }
            }
        objectives = newObjectives;
        }

    public Parameter defaultBase()
        {
        return MultiObjectiveDefaults.base().push(P_FITNESS);
        }

    public Object clone()
        {
        MultiObjectiveFitness f = (MultiObjectiveFitness) (super.clone());
        f.objectives = (float[]) (objectives.clone()); // cloning an array

        // note that we do NOT clone max and min fitness -- they're shared
        return f;
        }

    /**
     * Returns the Max() of objectives, which adheres to Fitness.java's protocol
     * for this method. Though you should not rely on a selection or statistics
     * method which requires this.
     */
    public float fitness()
        {
        float fit = objectives[0];
        for (int x = 1; x < objectives.length; x++)
            if (fit < objectives[x])
                fit = objectives[x];
        return fit;
        }

    /**
     * Sets up. This must be called at least once in the prototype before
     * instantiating any fitnesses that will actually be used in evolution.
     */

    public void setup(EvolutionState state, Parameter base)
        {
        super.setup(state, base); // unnecessary really

        Parameter def = defaultBase();
        int numFitnesses;

        numFitnesses = state.parameters.getInt(base.push(P_NUMOBJECTIVES), def.push(P_NUMOBJECTIVES), 0);
        if (numFitnesses <= 0)
            state.output.fatal("The number of objectives must be an integer >= 1.", base.push(P_NUMOBJECTIVES), def.push(P_NUMOBJECTIVES));

        maximize = state.parameters.getBoolean(base.push(P_MAXIMIZE), def.push(P_MAXIMIZE), true);

        objectives = new float[numFitnesses];
        maxfitness = new float[numFitnesses];
        minfitness = new float[numFitnesses];

        for (int i = 0; i < numFitnesses; i++)
            {
            // load default globals
            minfitness[i] = state.parameters.getFloatWithDefault(base.push(P_MINFITNESSES), def.push(P_MINFITNESSES), 0.0f);
            maxfitness[i] = state.parameters.getFloatWithDefault(base.push(P_MAXFITNESSES), def.push(P_MAXFITNESSES), 1.0f);

            // load specifics if any
            minfitness[i] = state.parameters.getFloatWithDefault(base.push(P_MINFITNESSES).push("" + i), def.push(P_MINFITNESSES).push("" + i), minfitness[i]);
            maxfitness[i] = state.parameters.getFloatWithDefault(base.push(P_MAXFITNESSES).push("" + i), def.push(P_MAXFITNESSES).push("" + i), maxfitness[i]);
            
            // test for validity
            if (minfitness[i] >= maxfitness[i])
                state.output.error("For objective " + i + "the min fitness must be strictly less than the max fitness.");
            }
        state.output.exitIfErrors();
        }

    /**
     * Returns true if this fitness is the "ideal" fitness. Default always
     * returns false. You may want to override this.
     */
    public boolean isIdealFitness()
        {
        return false;
        }

    /**
     * Returns true if I'm equivalent in fitness (neither better nor worse) to
     * _fitness. The rule I'm using is this: If one of us is better in one or
     * more criteria, and we are equal in the others, then equivalentTo is
     * false. If each of us is better in one or more criteria each, or we are
     * equal in all criteria, then equivalentTo is true.   Multiobjective optimization algorithms may
	 * choose to override this to do something else.
     */

    public boolean equivalentTo(Fitness _fitness)
        {
        MultiObjectiveFitness other = (MultiObjectiveFitness) _fitness;
        boolean abeatsb = false;
        boolean bbeatsa = false;

        if (maximize != other.maximize)
            throw new RuntimeException(
                "Attempt made to compare two multiobjective fitnesses; but one expects higher values to be better and the other expectes lower values to be better.");
        if (objectives.length != other.objectives.length)
            throw new RuntimeException("Attempt made to compare two multiobjective fitnesses; but they have different numbers of objectives.");
        if (maximize)
            {
            for (int x = 0; x < objectives.length; x++)
                {
                if (objectives[x] > other.objectives[x])
                    abeatsb = true;
                if (objectives[x] < other.objectives[x])
                    bbeatsa = true;
                if (abeatsb && bbeatsa)
                    return true;
                }
            }
        else
            // lower is better
            {
            for (int x = 0; x < objectives.length; x++)
                {
                if (objectives[x] < other.objectives[x])
                    abeatsb = true;
                if (objectives[x] > other.objectives[x])
                    bbeatsa = true;
                if (abeatsb && bbeatsa)
                    return true;
                }
            }
        if (abeatsb || bbeatsa)
            return false;
        return true;
        }

    /**
     * Returns true if I'm better than _fitness. The DEFAULT rule I'm using is this: if
     * I am better in one or more criteria, and we are equal in the others, then
     * betterThan is true, else it is false. Multiobjective optimization algorithms may
	 * choose to override this to do something else.
     */

	public boolean betterThan(Fitness _fitness)
		{
		return paretoDominates(_fitness);
		}

    /**
     * Returns true if I'm better than _fitness. The rule I'm using is this: if
     * I am better in one or more criteria, and we are equal in the others, then
     * betterThan is true, else it is false.
     */

    public boolean paretoDominates(Fitness _fitness)
        {
        MultiObjectiveFitness other = (MultiObjectiveFitness) _fitness;
        boolean abeatsb = false;
        if (maximize != other.maximize)
            throw new RuntimeException(
                "Attempt made to compare two multiobjective fitnesses; but one expects higher values to be better and the other expectes lower values to be better.");
        if (objectives.length != other.objectives.length)
            throw new RuntimeException("Attempt made to compare two multiobjective fitnesses; but they have different numbers of objectives.");
        if (maximize)
            {
            for (int x = 0; x < objectives.length; x++)
                {
                if (objectives[x] > other.objectives[x])
                    abeatsb = true;
                if (objectives[x] < other.objectives[x])
                    return false;
                }
            }
        else
            {
            for (int x = 0; x < objectives.length; x++)
                {
                if (objectives[x] < other.objectives[x])
                    abeatsb = true;
                if (objectives[x] > other.objectives[x])
                    return false;
                }
            }
        return abeatsb;
        }

	// Remove an individual from the ArrayList, shifting the topmost
	// individual in his place
	static void yank(int val, ArrayList list)
		{
		int size = list.size();
		list.set(val, list.get(size - 1));
		list.remove(size - 1);
		}

    /**
     * Divides an array of Individuals into the Pareto front and the "nonFront" (everyone else). 
	 * The Pareto front is returned.  You may provide ArrayLists for the front and a nonFront.
	 * If you provide null for the front, an ArrayList will be created for you.  If you provide
	 * null for the nonFront, non-front individuals will not be added to it.  This algorithm is
	 * O(n^2).
     */
    public static ArrayList partitionIntoParetoFront(Individual[] inds, ArrayList front, ArrayList nonFront)
        {
		if (front == null)
			front = new ArrayList();
			
		// put the first guy in the front
		front.add(inds[0]);
		
		// iterate over all the individuals
        for (int i = 1; i < inds.length; i++)
            {
            Individual ind = (Individual) (inds[i]);

			boolean noOneWasBetter = true;
            int frontSize = front.size();
			
			// iterate over the entire front
            for (int j = 0; j < frontSize; j++)
                {
                Individual frontmember = (Individual) (front.get(j));
				
				// if the front member is better than the individual, dump the individual and go to the next one
                if (((MultiObjectiveFitness) (frontmember.fitness)).paretoDominates((MultiObjectiveFitness) (ind.fitness)))
                    {
					if (nonFront != null) nonFront.add(ind);
					noOneWasBetter = false;
					break;  // failed.  He's not in the front
                    } 
				// if the individual was better than the front member, dump the front member.  But look over the
				// other front members (don't break) because others might be dominated by the individual as well.
				else if (((MultiObjectiveFitness) (ind.fitness)).paretoDominates((MultiObjectiveFitness) (frontmember.fitness)))
                    {
                    // a front member is dominated by the new individual.  Replace him
					frontSize--; // member got removed
					j++;  // because there's another guy we now need to consider in his place
					if (nonFront != null) nonFront.add(frontmember);
                    }
                }
			if (noOneWasBetter)
				front.add(ind);
            }
		return front;
        }


    /**
     * Returns the sum of the squared difference between two Fitnesses in Objective space.
     */
    public double sumSquaredObjectiveDistance(MultiObjectiveFitness other)
        {
        double s = 0;
        for (int i = 0; i < objectives.length; i++)
            {
			double a = (objectives[i] - other.objectives[i]);
            s += a * a;
            }
        return s;
        }


    public String fitnessToString()
        {
        String s = FITNESS_PREAMBLE + MULTI_FITNESS_POSTAMBLE;
        for (int x = 0; x < objectives.length; x++)
            {
            if (x > 0)
                s = s + " ";
            s = s + Code.encode(objectives[x]);
            }
        s = s + " ";
        s = s + Code.encode(maximize);
        return s + FITNESS_POSTAMBLE;
        }


    public String fitnessToStringForHumans()
        {
        String s = FITNESS_PREAMBLE + MULTI_FITNESS_POSTAMBLE;
        for (int x = 0; x < objectives.length; x++)
            {
            if (x > 0)
                s = s + " ";
            s = s + objectives[x];
            }
        s = s + " ";
        s = s + (maximize ? "max" : "min");
        return s + FITNESS_POSTAMBLE;
        }

    public void readFitness(final EvolutionState state, final LineNumberReader reader) throws IOException
        {
        DecodeReturn d = Code.checkPreamble(FITNESS_PREAMBLE + MULTI_FITNESS_POSTAMBLE, state, reader);
        for (int x = 0; x < objectives.length; x++)
            {
            Code.decode(d);
            if (d.type != DecodeReturn.T_FLOAT)
                state.output.fatal("Reading Line " + d.lineNumber + ": " + "Bad Fitness (objectives value #" + x + ").");
            objectives[x] = (float) d.d;
            }
        Code.decode(d);
        if (d.type != DecodeReturn.T_BOOLEAN)
            state.output.fatal("Reading Line " + d.lineNumber + ": " + "Information missing about whether higher is better");
        maximize = (boolean) (d.l != 0);
        }

    public void writeFitness(final EvolutionState state, final DataOutput dataOutput) throws IOException
        {
        dataOutput.writeInt(objectives.length);
        for (int x = 0; x < objectives.length; x++)
            dataOutput.writeFloat(objectives[x]);
        dataOutput.writeBoolean(maximize);
        }

    public void readFitness(final EvolutionState state, final DataInput dataInput) throws IOException
        {
        int len = dataInput.readInt();
        if (objectives == null || objectives.length != len)
            objectives = new float[len];
        for (int x = 0; x < objectives.length; x++)
            objectives[x] = dataInput.readFloat();
        maximize = dataInput.readBoolean();
        }
    }
