/*
  Copyright 2014 by Xiaomeng Ye
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.gp.lambda.app.helloworld;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Serializable;

import ec.EvolutionState;
import ec.Individual;
import ec.util.Code;
import ec.util.DecodeReturn;
import ec.util.Parameter;
import ec.vector.VectorDefaults;
import ec.vector.VectorIndividual;
import ec.vector.VectorSpecies;

public class CharVectorIndividual extends VectorIndividual implements Serializable {
	private static final long serialVersionUID = 1;
	public static final String P_CHARVECTORINDIVIDUAL = "char-vect-ind";
	public char[] genome;

	// the following two assumptions might be wrong
	int intOfA = 65;
	int intOfZ = 90;

	@Override
	public Parameter defaultBase() {
		return VectorDefaults.base().push(P_CHARVECTORINDIVIDUAL);

	}

	public Object clone() {
		CharVectorIndividual myobj = (CharVectorIndividual) (super.clone());

		// must clone the genome
		myobj.genome = (char[]) (genome.clone());

		return myobj;
	}

	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base); // actually unnecessary (Individual.setup() is
									// empty)

		Parameter def = defaultBase();

		if (!(species instanceof CharVectorSpecies))
			state.output.fatal(
					"IntegerVectorIndividual requires an IntegerVectorSpecies",
					base, def);
		CharVectorSpecies s = (CharVectorSpecies) species;

		genome = new char[s.genomeSize];
	}

	public void defaultCrossover(EvolutionState state, int thread,
			VectorIndividual ind) {
		CharVectorSpecies s = (CharVectorSpecies) species;
		CharVectorIndividual i = (CharVectorIndividual) ind;
		char tmp;
		int point;

		if (genome.length != i.genome.length)
			state.output
					.fatal("Genome lengths are not the same for fixed-length vector crossover");
		switch (s.crossoverType) {
		case VectorSpecies.C_ONE_POINT:
			point = state.random[thread]
					.nextInt((genome.length / s.chunksize) + 1);
			for (int x = 0; x < point * s.chunksize; x++) {
				tmp = i.genome[x];
				i.genome[x] = genome[x];
				genome[x] = tmp;
			}
			break;
		case VectorSpecies.C_TWO_POINT:
			int point0 = state.random[thread]
					.nextInt((genome.length / s.chunksize) + 1);
			point = state.random[thread]
					.nextInt((genome.length / s.chunksize) + 1);
			if (point0 > point) {
				int p = point0;
				point0 = point;
				point = p;
			}
			for (int x = point0 * s.chunksize; x < point * s.chunksize; x++) {
				tmp = i.genome[x];
				i.genome[x] = genome[x];
				genome[x] = tmp;
			}
			break;
		case VectorSpecies.C_ANY_POINT:
			for (int x = 0; x < genome.length / s.chunksize; x++)
				if (state.random[thread].nextBoolean(s.crossoverProbability))
					for (int y = x * s.chunksize; y < (x + 1) * s.chunksize; y++) {
						tmp = i.genome[y];
						i.genome[y] = genome[y];
						genome[y] = tmp;
					}
			break;
		}
	}

	public void defaultMutate(EvolutionState state, int thread) {
		CharVectorSpecies s = (CharVectorSpecies) species;
		for (int x = 0; x < genome.length; x++)
			if (state.random[thread].nextBoolean(s.getMutationProbability(x))) {
				int old = genome[x];
				for (int retries = 0; retries < s.duplicateRetries(x) + 1; retries++) {
					int which2Mutate = state.random[thread]
							.nextInt(s.genomeSize);
					int what2Mutate2 = state.random[thread].nextInt(1 + intOfZ
							- intOfA)
							+ intOfA;
					genome[which2Mutate] = Character.toChars(what2Mutate2)[0];
					// switch(s.mutationType(x))
					// {
					// case IntegerVectorSpecies.C_RESET_MUTATION:
					// genome[x] =
					// randomValueFromClosedInterval((int)s.minGene(x),
					// (int)s.maxGene(x), state.random[thread]);
					// break;
					// case IntegerVectorSpecies.C_RANDOM_WALK_MUTATION:
					// int min = (int)s.minGene(x);
					// int max = (int)s.maxGene(x);
					// if (!s.mutationIsBounded(x))
					// {
					// // okay, technically these are still bounds, but we can't
					// go beyond this without weird things happening
					// max = Integer.MAX_VALUE;
					// min = Integer.MIN_VALUE;
					// }
					// do
					// {
					// int n = (int)(state.random[thread].nextBoolean() ? 1 :
					// -1);
					// int g = genome[x];
					// if ((n == 1 && g < max) ||
					// (n == -1 && g > min))
					// genome[x] = g + n;
					// else if ((n == -1 && g < max) ||
					// (n == 1 && g > min))
					// genome[x] = g - n;
					// }
					// while
					// (state.random[thread].nextBoolean(s.randomWalkProbability(x)));
					// break;
					// }
					if (genome[x] != old)
						break;
					// else genome[x] = old; // try again
				}
			}
	}

	@Override
	public void reset(EvolutionState state, int thread) {
		for (int x = 0; x < genome.length; x++) {
			int what2generate = state.random[thread].nextInt(1 + intOfZ
					- intOfA)
					+ intOfA;
			genome[x] = Character.toChars(what2generate)[0];
		}
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String genotypeToStringForHumans() {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < genome.length; i++) {
			if (i > 0)
				s.append(" ");
			s.append(genome[i]);
		}
		return s.toString();
	}

	public String genotypeToString() {
		StringBuilder s = new StringBuilder();
		s.append(Code.encode(genome.length));
		for (int i = 0; i < genome.length; i++)
			s.append(Code.encode(genome[i]));
		return s.toString();
	}

	protected void parseGenotype(final EvolutionState state,
			final LineNumberReader reader) throws IOException {
		// read in the next line. The first item is the number of genes
		String s = reader.readLine();
		DecodeReturn d = new DecodeReturn(s);
		Code.decode(d);

		// of course, even if it *is* an integer, we can't tell if it's a gene
		// or a genome count, argh...
		if (d.type != DecodeReturn.T_INTEGER) // uh oh
			state.output
					.fatal("Individual with genome:\n"
							+ s
							+ "\n... does not have an integer at the beginning indicating the genome count.");
		int lll = (int) (d.l);

		genome = new char[lll];

		// read in the genes
		for (int i = 0; i < genome.length; i++) {
			Code.decode(d);
			genome[i] = (char) (d.l);
		}
	}

	public boolean equals(Object ind) {
		if (ind == null)
			return false;
		if (!(this.getClass().equals(ind.getClass())))
			return false; // SimpleRuleIndividuals are special.
		CharVectorIndividual i = (CharVectorIndividual) ind;
		if (genome.length != i.genome.length)
			return false;
		for (int j = 0; j < genome.length; j++)
			if (genome[j] != i.genome[j])
				return false;
		return true;
	}

	public Object getGenome() {
		return genome;
	}

	public void setGenome(Object gen) {
		genome = (char[]) gen;
	}

	public int genomeLength() {
		return genome.length;
	}

	public void writeGenotype(final EvolutionState state,
			final DataOutput dataOutput) throws IOException {
		dataOutput.writeInt(genome.length);
		for (int x = 0; x < genome.length; x++)
			dataOutput.writeChar(genome[x]);
	}

	public void readGenotype(final EvolutionState state,
			final DataInput dataInput) throws IOException {
		int len = dataInput.readInt();
		if (genome == null || genome.length != len)
			genome = new char[len];
		for (int x = 0; x < genome.length; x++)
			genome[x] = dataInput.readChar();
	}

	public double distanceTo(Individual otherInd) {
		if (!(otherInd instanceof CharVectorIndividual))
			return super.distanceTo(otherInd); // will return infinity!

		CharVectorIndividual other = (CharVectorIndividual) otherInd;
		char[] otherGenome = other.genome;
		double sumDistance = 0.0;
		for (int i = 0; i < other.genomeLength(); i++) {
			double dist = this.genome[i] - (double) otherGenome[i];
			sumDistance += Math.abs(dist);
		}
		return sumDistance;
	}

}
