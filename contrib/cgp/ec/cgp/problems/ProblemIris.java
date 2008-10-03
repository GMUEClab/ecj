package ec.cgp.problems;


import ec.EvolutionState;
import ec.Individual;
import ec.cgp.Record;
import ec.util.Parameter;

/**
 * The classic Iris classification problem. Data set originates from
 * http://archive.ics.uci.edu/ml/datasets/Iris. Data file used comes from Weka:
 * http://www.cs.waikato.ac.nz/ml/weka/
 * 
 * The task is to classify the species of Iris flower given a set of four
 * numerical measurements.
 * 
 * @author David Oranchak, doranchak@gmail.com, http://oranchak.com
 * 
 */
public class ProblemIris extends ClassificationProblem {

	/**
	 * Interpret a line from the data file into a Record instance.
	 */
	Record makeRecord(String line) {

		if (line == null || "".equals(line))
			return null;
		IrisRecord r = new IrisRecord();
		String[] split = line.split(",");
		String col;
		for (int i = 0; i < split.length; i++) {
			col = split[i].replaceAll("'", "");
			if (i == 0) {
				r.sepallength = Float.valueOf(col);
			} else if (i == 1) {
				r.sepalwidth = Float.valueOf(col);
			} else if (i == 2) {
				r.petallength = Float.valueOf(col);
			} else if (i == 3) {
				r.petalwidth = Float.valueOf(col);
			} else if (i == 4) {
				if ("Iris-setosa".equals(col))
					r.setosa = true;
				else if ("Iris-versicolor".equals(col))
					r.versicolor = true;
				else if ("Iris-virginica".equals(col))
					r.virginica = true;
			} else {
				System.err.println("Too many columns!!!");
			}
		}
		return r;

	}

	/**
	 * Obtain the inputs from the data record.
	 */
	void setInputs(Object[] inputs, Record rec) {
		IrisRecord r = (IrisRecord) rec;
		inputs[0] = r.sepallength;
		inputs[1] = r.sepalwidth;
		inputs[2] = r.petallength;
		inputs[3] = r.petalwidth;
	}

	/**
	 * Determine the result of classification by comparing the evaluated outputs
	 * from our Cartsesian Genetic Program to the instance class(es) specified
	 * somewhere in the given Record.
	 */
	boolean[] compare(Object[] outputs, Record rec) {
		IrisRecord r = (IrisRecord) rec;
		boolean[] results = new boolean[outputs.length];
		for (int i = 0; i < results.length; i++) {
			float result = (Float) outputs[i];
			boolean compare = i == 0 ? r.virginica : i == 1 ? r.versicolor
					: r.setosa;
			results[i] = compare == (result > 0 ? true : false);
		}
		return results;
	}

	/**
	 * Record to represent a classification instance.
	 */
	static public class IrisRecord implements Record {
		public float sepallength;
		public float sepalwidth;
		public float petallength;
		public float petalwidth;
		public boolean setosa; /* class */
		public boolean versicolor; /* class */
		public boolean virginica; /* class */

		public String toString() {
			return sepallength + "," + sepalwidth + "," + petallength + ","
					+ petalwidth + "," + setosa + "," + versicolor + ","
					+ virginica;
		}
	}

}
