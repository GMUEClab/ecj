package ec.cgp.problems;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.cgp.Record;
import ec.cgp.problems.ProblemBreastCancerW.BreastCancerWRecord;
import ec.simple.SimpleProblemForm;
import ec.util.MersenneTwisterFast;
import ec.util.Parameter;

/**
 * 
 * Breast Cancer (Yugoslavia) classification problem. Data set originates from:
 * http://archive.ics.uci.edu/ml/datasets/Breast+Cancer. Data file used comes
 * from Weka: http://www.cs.waikato.ac.nz/ml/weka/
 * 
 * The task is to classify tumors as "no recurrence events" or "recurrence
 * events" based on nine numeric and nominal measurements.
 * 
 * @author David Oranchak, doranchak@gmail.com, http://oranchak.com
 * 
 */
public class ProblemBreastCancerY extends ClassificationProblem {

	/**
	 * Interpret a line from the data file into a Record instance.
	 */
	Record makeRecord(String line) {

		if (line == null || "".equals(line))
			return null;
		BreastCancerYRecord r = new BreastCancerYRecord();
		String[] split = line.split(",");
		String col;
		for (int i = 0; i < split.length; i++) {
			col = split[i].replaceAll("'", "");
			if (i == 0) { // normalize the age classes
				r.age = "10-19".equals(col) ? 0f : "20-29".equals(col) ? 1 / 8f
						: "30-39".equals(col) ? 2 / 8f
								: "40-49".equals(col) ? 3 / 8f : "50-59"
										.equals(col) ? 4 / 8f : "60-69"
										.equals(col) ? 5 / 8f : "70-79"
										.equals(col) ? 6 / 8f : "80-89"
										.equals(col) ? 7 / 8f : "90-99"
										.equals(col) ? 8 / 8f : 0f;
			} else if (i == 1) { // normalize the menopause classes
				r.menopause = "lt40".equals(col) ? 0f
						: "ge40".equals(col) ? 1 / 2f
								: "premeno".equals(col) ? 2 / 2f : 0f;
			} else if (i == 2) { // normalize the tumor size classes
				r.tumorSize = "0-4".equals(col) ? 0f
						: "5-9".equals(col) ? 1 / 11f
								: "10-14".equals(col) ? 2 / 11f : "15-19"
										.equals(col) ? 3 / 11f : "20-24"
										.equals(col) ? 4 / 11f : "25-29"
										.equals(col) ? 5 / 11f : "30-34"
										.equals(col) ? 6 / 11f : "35-39"
										.equals(col) ? 7 / 11f : "40-44"
										.equals(col) ? 8 / 11f : "45-49"
										.equals(col) ? 9 / 11f : "50-54"
										.equals(col) ? 10 / 11f : "50-54"
										.equals(col) ? 11 / 11f : 0;
			} else if (i == 3) { // normalize inv-nodes
				r.invNodes = "0-2".equals(col) ? 0f
						: "3-5".equals(col) ? 1 / 12f
								: "6-8".equals(col) ? 2 / 12f : "9-11"
										.equals(col) ? 3 / 12f : "12-14"
										.equals(col) ? 4 / 12f : "15-17"
										.equals(col) ? 5 / 12f : "18-20"
										.equals(col) ? 6 / 12f : "21-23"
										.equals(col) ? 7 / 12f : "24-26"
										.equals(col) ? 8 / 12f : "27-29"
										.equals(col) ? 9 / 12f : "30-32"
										.equals(col) ? 10 / 12f : "33-35"
										.equals(col) ? 11 / 12f : "36-39"
										.equals(col) ? 12 / 12f : 0;
			} else if (i == 4) {
				r.nodeCaps = "yes".equals(col) ? 1.0f
						: "no".equals(col) ? -1.0f : 0f;
			} else if (i == 5) { // normalize degMalig
				r.degMalig = "1".equals(col) ? 0 : "2".equals(col) ? 1 / 2f
						: "3".equals(col) ? 2 / 2f : 0;
			} else if (i == 6) {
				r.breast = "left".equals(col) ? 0f : 1f;
			} else if (i == 7) {
				r.breastQuad = "left_up".equals(col) ? 0 : "left_low"
						.equals(col) ? 1 / 4f : "right_up".equals(col) ? 2 / 4f
						: "right_low".equals(col) ? 3 / 4f : "central"
								.equals(col) ? 4 / 4f : 0;
			} else if (i == 8) {
				r.irrad = "yes".equals(col) ? 1f : "no".equals(col) ? -1f : 0f;
			} else if (i == 9) {
				r.recurrence = "recurrence-events".equals(col);
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
		BreastCancerYRecord r = (BreastCancerYRecord) rec;
		inputs[0] = r.age;
		inputs[1] = r.menopause;
		inputs[2] = r.tumorSize;
		inputs[3] = r.invNodes;
		inputs[4] = r.nodeCaps;
		inputs[5] = r.degMalig;
		inputs[6] = r.breast;
		inputs[7] = r.breastQuad;
		inputs[8] = r.irrad;
	}

	/**
	 * Determine the result of classification by comparing the evaluated outputs
	 * from our Cartsesian Genetic Program to the instance class(es) specified
	 * somewhere in the given Record.
	 */
	boolean[] compare(Object[] outputs, Record rec) {
		BreastCancerYRecord r = (BreastCancerYRecord) rec;
		float result = (Float) outputs[0];
		return new boolean[] { (r.recurrence == (result > 0 ? true : false)) };
	}

	/**
	 * Record to represent a classification instance.
	 */
	static public class BreastCancerYRecord implements Record {
		public float age;
		public float menopause;
		public float tumorSize;
		public float invNodes;
		public float nodeCaps;
		public float degMalig;
		public float breast;
		public float breastQuad;
		public float irrad;
		public boolean recurrence; /* class */

		public String toString() {
			return age + "," + menopause + "," + tumorSize + "," + invNodes
					+ "," + nodeCaps + "," + degMalig + "," + breast + ","
					+ breastQuad + "," + irrad + "," + recurrence;
		}
	}

}
