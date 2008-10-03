package ec.cgp.problems;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


import ec.EvolutionState;
import ec.Individual;
import ec.cgp.Evaluator;
import ec.cgp.FitnessCGP;
import ec.cgp.Util;
import ec.cgp.Record;
import ec.cgp.representation.VectorIndividualCGP;
import ec.cgp.representation.VectorSpeciesCGP;
import ec.util.MersenneTwisterFast;
import ec.util.Parameter;

/**
 * An extension of CGPProblem which provides some facilities to represent
 * and evaluate classification problems.
 * 
 * @author David Oranchak, doranchak@gmail.com, http://oranchak.com
 * 
 */
public abstract class ClassificationProblem extends ProblemCGP {

	/** storage for records */
	static List<Record> data;
	/** portion of the records used for training */
	static List<Record> trainingSet;
	/** portion of the records used for testing/verification */
	static List<Record> testSet;

	static String P_FILE = "file";
	static String P_TEST = "test";

	/** proportion of records to reserve for the testing set (example: 0.3 = 30%) */
	static float test;


	/**
	 * Configure this Classification Problem. 
	 */
	public void setup(EvolutionState state, Parameter base) {
		super.setup(state, base);
		Parameter def = defaultBase();
        
        String fileName = state.parameters.getString(base.push(P_FILE),def.push(P_FILE));
        if (null==fileName || "".equals(fileName))
        	state.output.fatal("problem.file is missing.");
        state.output.exitIfErrors();
        
        test = state.parameters.getFloatWithDefault(base.push(P_TEST), def.push(P_TEST), 0.3f);
	
        makeRecords(fileName, state.random[0]);
        
	}
	
	
	/**
	 * Read lines of text from the input file and turn each line into a Record
	 * instance.
	 * 
	 * @param fileName
	 *            The full path of the input file.
	 * @param rand
	 *            RNG to use.
	 */
	void makeRecords(String fileName, MersenneTwisterFast rand) {
		List<String> lines = Util.readFile(new File(fileName));
		data = new ArrayList<Record>();
		Record r;
		for (String s : lines) {
			r = makeRecord(s);
			if (r != null)
				data.add(r);
			else
				System.err
						.println("There was a problem making a record out of this line: ["
								+ s + "].");
		}

		/* generate training and test sets */

		testSet = new ArrayList<Record>();
		float testInstances = test * data.size();

		int x;
		for (int i = 0; i < testInstances; i++) {
			x = rand.nextInt(data.size());
			testSet.add(data.get(x));
			data.remove(x);
		}
		trainingSet = data;

		/* dump the training and test sets */
		System.out.println("========= TRAINING SET:");
		for (Record t : trainingSet) {
			System.out.println(t);
		}
		System.out.println("========= TEST SET:");
		for (Record t : testSet) {
			System.out.println(t);
		}

	}

	/**
	 * Your implementing class must do the grunt work of turning a line from the
	 * text file into a Record instance that is specific to your problem.
	 * 
	 * @param line
	 *            The comma-delimited line from the data file.
	 * @return an instance of the Record.
	 */
	abstract Record makeRecord(String line);

	/**
	 * Evaluate this individual. Fitness is set to the proportion of
	 * unsuccessfully classified training instances. If there are constant
	 * values, the input vector is filled with them starting at the end.
	 * 
	 * The test set is also evaluated to measure performance of the classifier.
	 */
	public void evaluate(EvolutionState state, Individual ind,
			int subpopulation, int threadnum) {
		VectorSpeciesCGP s = (VectorSpeciesCGP) ind.species;
		VectorIndividualCGP ind2 = (VectorIndividualCGP) ind;

		Object[] inputs = new Object[s.numInputs];
		for (int i = 0; i < constants.length; i++)
			inputs[s.numInputs - 1 - i] = constants[i];

		int[] diffsTraining = new int[s.numOutputs];
		int diffsTrainingTotal = 0;
		boolean[] results;

		for (Record r : trainingSet) {
			results = eval(state, threadnum, inputs, r, ind2);
			for (int i = 0; i < results.length; i++)
				if (!results[i]) {
					diffsTraining[i]++;
					diffsTrainingTotal++;
				}
		}

		((FitnessCGP) ind.fitness).setFitness(state, (float) diffsTrainingTotal
				/ trainingSet.size(), diffsTrainingTotal == 0);

		/** compute the total accuracy including the test set */

		int[] diffsTest = new int[s.numOutputs];
		int diffsTestTotal = 0;
		for (Record r : testSet) {
			results = eval(state, threadnum, inputs, r, ind2);
			for (int i = 0; i < results.length; i++)
				if (!results[i]) {
					diffsTest[i]++;
					diffsTestTotal++;
				}
		}
		StringBuffer info = new StringBuffer(".   Test inaccuracy: ");
		for (int i = 0; i < diffsTest.length; i++) {
			info.append("[" + ((float) diffsTest[i] / testSet.size()) + "]");
		}
		info.append(" (total: " + ((float) diffsTestTotal / testSet.size())
				+ ").   Training inaccuracy: ");
		for (int i = 0; i < diffsTest.length; i++) {
			info.append("[" + ((float) diffsTraining[i] / trainingSet.size())
					+ "]");
		}
		info.append(" (total: "
				+ ((float) diffsTrainingTotal / trainingSet.size())
				+ ").   Total inaccuracy: ");
		for (int i = 0; i < diffsTest.length; i++) {
			info.append("["
					+ ((float) (diffsTraining[i] + diffsTest[i]) / (trainingSet
							.size() + testSet.size())) + "]");
		}
		info.append(" (total: "
				+ ((float) (diffsTrainingTotal + diffsTestTotal) / (trainingSet
						.size() + testSet.size())) + ").");

		ind2.expression.append(info);

	}

	/**
	 * Your implementing class must map attributes of the given Record to items
	 * in the input vector using this method.
	 * 
	 * @param inputs
	 *            input array to set
	 * @param r
	 *            record to set values from
	 */
	abstract void setInputs(Object[] inputs, Record r);

	/**
	 * During evaluation, outputs are passed to this method. Your implementing
	 * class must compare the outputs to the target class(es) in the given
	 * Record. Each element of the return vector is set to true if the
	 * corresponding output represents a successfully classified instance.
	 * 
	 * @param outputs The output vector resulting from evaluation of the CGP.
	 * @param r The record from which to compare classification results.
	 * @return A boolean vector indicating sucess of classification.
	 */
	abstract boolean[] compare(Object[] outputs, Record r);

	/** 
	 * Sets the inputs, runs the Cartesian Genetic Program, and returns the results
	 * of classification.
	 * 
	 * @param state The evolution state
	 * @param threadnum The current thread number
	 * @param inputs The input vector
	 * @param rec The current record
	 * @param ind The current individual
	 * @return boolean vector indicating successful/unsuccessful classification(s).
	 */
	boolean[] eval(EvolutionState state, int threadnum, Object[] inputs,
			Record rec, VectorIndividualCGP ind) {
		setInputs(inputs, rec);
		Object[] outputs = Evaluator.evaluate(state, threadnum, inputs, ind);
		return compare(outputs, rec);
	}

}
