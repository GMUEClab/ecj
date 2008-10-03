package ec.cgp.problems;


import ec.cgp.Record;

/**
 * 
 * Breast Cancer (Wisconsin) classification problem. Data set originates from:
 * http://archive.ics.uci.edu/ml/datasets/Breast+Cancer+Wisconsin+(Original).
 * Data file used comes from Weka: http://www.cs.waikato.ac.nz/ml/weka/
 * 
 * The task is to classify tumors as "benign" or "malignant" based on nine
 * numeric measurements.
 * 
 * @author David Oranchak, doranchak@gmail.com, http://oranchak.com
 * 
 */
public class ProblemBreastCancerW extends ClassificationProblem {

	/**
	 * Interpret a line from the data file into a Record instance. Handles
	 * missing values.
	 */
	Record makeRecord(String line) {

		if (line == null || "".equals(line))
			return null;
		BreastCancerWRecord r = new BreastCancerWRecord();
		String[] split = line.split(",");
		String col;
		for (int i = 0; i < split.length; i++) {
			col = split[i].replaceAll("'", "");
			if (i == 0) {
				r.clumpThickness = "?".equals(col) ? 0
						: (Float.valueOf(col) - 1) / 9f;
			} else if (i == 1) {
				r.cellSizeUniformity = "?".equals(col) ? 0 : (Float
						.valueOf(col) - 1) / 9f;
			} else if (i == 2) {
				r.cellShapeUniformity = "?".equals(col) ? 0 : (Float
						.valueOf(col) - 1) / 9f;
			} else if (i == 3) {
				r.marginalAdhesion = "?".equals(col) ? 0
						: (Float.valueOf(col) - 1) / 9f;
			} else if (i == 4) {
				r.singleEpiCellSize = "?".equals(col) ? 0
						: (Float.valueOf(col) - 1) / 9f;
			} else if (i == 5) {
				r.bareNuclei = "?".equals(col) ? 0
						: (Float.valueOf(col) - 1) / 9f;
			} else if (i == 6) {
				r.blandChromatin = "?".equals(col) ? 0
						: (Float.valueOf(col) - 1) / 9f;
			} else if (i == 7) {
				r.normalNucleoli = "?".equals(col) ? 0
						: (Float.valueOf(col) - 1) / 9f;
			} else if (i == 8) {
				r.mitoses = "?".equals(col) ? 0 : (Float.valueOf(col) - 1) / 9f;
			} else if (i == 9) {
				r.malignant = "malignant".equals(col);
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
		BreastCancerWRecord r = (BreastCancerWRecord) rec;
		inputs[0] = r.clumpThickness;
		inputs[1] = r.cellSizeUniformity;
		inputs[2] = r.cellShapeUniformity;
		inputs[3] = r.marginalAdhesion;
		inputs[4] = r.singleEpiCellSize;
		inputs[5] = r.bareNuclei;
		inputs[6] = r.blandChromatin;
		inputs[7] = r.normalNucleoli;
		inputs[8] = r.mitoses;
	}

	/**
	 * Determine the result of classification by comparing the evaluated
	 * outputs from our Cartsesian Genetic Program to the instance class(es)
	 * specified somewhere in the given Record. 
	 */
	boolean[] compare(Object[] outputs, Record rec) {
		BreastCancerWRecord r = (BreastCancerWRecord) rec;
		boolean[] results = new boolean[outputs.length];
		for (int i = 0; i < results.length; i++) {
			float result = (Float) outputs[i];
			boolean compare = r.malignant;
			results[i] = compare == (result > 0 ? true : false);
		}
		return results;
	}

	/**
	 * Record to represent a classification instance.
	 */
	static public class BreastCancerWRecord implements Record {
		public float clumpThickness;
		public float cellSizeUniformity;
		public float cellShapeUniformity;
		public float marginalAdhesion;
		public float singleEpiCellSize;
		public float bareNuclei;
		public float blandChromatin;
		public float normalNucleoli;
		public float mitoses;
		public boolean malignant; /* class */

		public String toString() {
			return clumpThickness + "," + cellSizeUniformity + ","
					+ cellShapeUniformity + "," + marginalAdhesion + ","
					+ singleEpiCellSize + "," + bareNuclei + ","
					+ blandChromatin + "," + normalNucleoli + "," + mitoses
					+ "," + malignant;
		}
	}

}
