/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.cgp.recombination;

import ec.vector.*;
import ec.*;
import ec.cgp.representation.*;
import ec.util.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;


/**
*
VectorCrossoverPipeline is a BreedingPipeline which implements a simple default crossover
for VectorIndividuals.  Normally it takes two individuals and returns two crossed-over 
child individuals.  Optionally, it can take two individuals, cross them over, but throw
away the second child (a one-child crossover).  VectorCrossoverPipeline works by calling
defaultCrossover(...) on the first parent individual.

<p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
2 * minimum typical number of individuals produced by each source, unless tossSecondParent
is set, in which case it's simply the minimum typical number.

<p><b>Number of Sources</b><br>
2

<p><b>Parameters</b><br>
<table>
<tr><td valign=top><i>base</i>.<tt>toss</tt><br>
<font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</font>/td>
<td valign=top>(after crossing over with the first new individual, should its second sibling individual be thrown away instead of adding it to the population?)</td></tr>
</table>

<p><b>Default Base</b><br>
vector.xover

* @author Sean Luke
* 
* @author Roman Kalkreuth, roman.kalkreuth@tu-dortmund.de,
*         https://orcid.org/0000-0003-1449-5131,
*         https://ls11-www.cs.tu-dortmund.de/staff/kalkreuth,
*         https://twitter.com/RomanKalkreuth,
*         
* @author Jakub Husa, ihusa@fit.vut.cz
* 		  https://www.vut.cz/en/people/jakub-husa-138342?aid_redir=1
* 
*/
public class VectorCrossoverPipeline extends BreedingPipeline {
	public static final String P_TOSS = "toss";
	public static final String P_CROSSOVER = "xover";
	public static final int NUM_SOURCES = 2;
	public static final String KEY_PARENTS = "parents";

	public static final String P_BLOCKSIZE = "blocksize";
	public static final String V_ONEPOINT = "onepoint";
	public static final String V_BLOCK = "block";
	public static final String V_UNIFORM = "uniform";
	
	public final static int C_ONEPOINT = 0;
	public final static int C_BLOCK = 1;
	public final static int C_UNIFORM = 2;
	
	public int crossoverType;// integer value selected bysed on the text input by user //public int
	public int blockSize;

	/** Should the pipeline discard the second parent after crossing over? */
	public boolean tossSecondParent;

	/** Temporary holding place for parents */
	ArrayList<Individual> parents;

	public VectorCrossoverPipeline() {
		// by Ermo. get rid of asList
		// parents = new ArrayList<Individual>(Arrays.asList(new VectorIndividual[2]));;
		parents = new ArrayList<Individual>();
	}

	public Parameter defaultBase() {
		return VectorDefaults.base().push(P_CROSSOVER);
	}

	/** Returns 2 */
	public int numSources() {
		return NUM_SOURCES;
	}

	public Object clone() {
		VectorCrossoverPipeline c = (VectorCrossoverPipeline) (super.clone());

		// deep-cloned stuff
		c.parents = new ArrayList<Individual>(parents);

		return c;
	}

	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		Parameter def = defaultBase();
		tossSecondParent = state.parameters.getBoolean(base.push(P_TOSS), def.push(P_TOSS), false);

		String ctype = state.parameters.getString(base.push(P_CROSSOVER), null);// crossovever type (the text input by
		// user)

		blockSize = state.parameters.getIntWithDefault(base.push(P_BLOCKSIZE), def.push(P_BLOCKSIZE), 3);

		crossoverType = C_ONEPOINT;
		if (ctype == null)
			state.output.warning("advanced crossover type not specified - assuming onepoint crossover",
					base.push(P_CROSSOVER));
		else if (ctype.equalsIgnoreCase(V_ONEPOINT))
			crossoverType = C_ONEPOINT; // redundant
		else if (ctype.equalsIgnoreCase(V_BLOCK))
			crossoverType = C_BLOCK;
		else if (ctype.equalsIgnoreCase(V_UNIFORM))
			crossoverType = C_UNIFORM;

		else
			state.output.fatal("AdvancedVectorCrossoverPipeline given a bad crossover type: " + ctype,
					base.push(P_CROSSOVER));
	}

	/**
	 * Returns 2 * minimum number of typical individuals produced by any sources,
	 * else 1* minimum number if tossSecondParent is true.
	 */
	public int typicalIndsProduced() {
		return (tossSecondParent ? minChildProduction() : minChildProduction() * 2);
	}

	public int produce(final int min, final int max, final int subpopulation, final ArrayList<Individual> inds,
			final EvolutionState state, final int thread, HashMap<String, Object> misc)

	{
		int start = inds.size();

		// how many individuals should we make?
		int n = typicalIndsProduced();
		if (n < min)
			n = min;
		if (n > max)
			n = max;

		IntBag[] parentparents = null;
		IntBag[] preserveParents = null;

		if (misc != null && misc.containsKey(KEY_PARENTS)) {
			preserveParents = (IntBag[]) misc.get(KEY_PARENTS);
			parentparents = new IntBag[2];
			misc.put(KEY_PARENTS, parentparents);
		}

		// should we use them straight?
		if (!state.random[thread].nextBoolean(likelihood)) {
			// just load from source 0 and clone 'em
			sources[0].produce(n, n, subpopulation, inds, state, thread, misc);
			return n;
		}

		for (int q = start; q < n + start; /* no increment */) // keep on going until we're filled up
		{
			parents.clear();

			// grab two individuals from our sources
			if (sources[0] == sources[1]) // grab from the same source
			{
				sources[0].produce(2, 2, subpopulation, parents, state, thread, misc);
			} else // grab from different sources
			{
				sources[0].produce(1, 1, subpopulation, parents, state, thread, misc);
				sources[1].produce(1, 1, subpopulation, parents, state, thread, misc);
			}

			// at this point, parents[] contains our two selected individuals,
			// AND they're copied so we own them and can make whatever modifications
			// we like on them.

			// so we'll cross them over now. Since this is the default pipeline,
			// we'll just do it by calling defaultCrossover on the first child
			
			AdvancedIntegerVectorIndividual p1 =  (AdvancedIntegerVectorIndividual) parents.get(0);
			AdvancedIntegerVectorIndividual p2 =  (AdvancedIntegerVectorIndividual) parents.get(1);
			
			switch (crossoverType)// DELETE debugging text output
			{
			case C_ONEPOINT:
				p1.onepointCrossover(state, thread, p2);
				break;
			case C_BLOCK:
				p1.blockCrossover(state, thread, p2, blockSize);
				break;
			case C_UNIFORM:		
				p1.discreteCrossover(state, thread, p2);
				break;
			default:// this shoudl never happen and default crossover is therefore obsolete!
				p1.defaultCrossover(state, thread, p2);
				break;
			}

			parents.get(0).evaluated = false;
			parents.get(1).evaluated = false;

			// add 'em to the population
			// by Ermo. this should use add instead of set, because the inds is empty, so
			// will throw index out of bounds
			// okay -- Sean
			inds.add(parents.get(0));
			if (preserveParents != null) {
				parentparents[0].addAll(parentparents[1]);
				preserveParents[q] = parentparents[0];
			}
			q++;
			if (q < n + start && !tossSecondParent) {
				// by Ermo. as as here, see the comments above
				inds.add(parents.get(1));
				if (preserveParents != null) {
					preserveParents[q] = new IntBag(parentparents[0]);
				}
				q++;
			}
		}

		if (preserveParents != null) {
			misc.put(KEY_PARENTS, preserveParents);
		}
		return n;
	}
}
