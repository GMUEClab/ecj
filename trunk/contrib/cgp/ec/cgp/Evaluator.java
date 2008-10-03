package ec.cgp;

import java.util.List;
import java.util.Map;


import ec.EvolutionState;
import ec.cgp.functions.Functions;
import ec.cgp.representation.FloatVectorIndividual;
import ec.cgp.representation.IntegerVectorIndividual;
import ec.cgp.representation.VectorIndividualCGP;
import ec.cgp.representation.VectorSpeciesCGP;

/**
 * Interprets the program encoded by an individual's genome, and evaluates the
 * results of the program with the given inputs.
 * 
 * @author David Oranchak, doranchak@gmail.com, http://oranchak.com
 * 
 */
public class Evaluator {

	static boolean DEBUG = false;

	/** counter to track number of node evaluations performed. */
	static int evals = 0;

	/**
	 * maps node evaluations to their node numbers. used to avoid re-processing
	 * nodes unnecessarily.
	 */
	public static List<Map<Integer, Object>> nodeMap;

	/**
	 * maps String representations of node sub-expressions to their node
	 * numbers. used to avoid re-processing nodes repeatedly.
	 */
	public static List<Map<Integer, String>> expressionMap;

	/** functions are loaded during setup. */
	public static Functions functions;

	/**
	 * Evaluate the genome against the given inputs. If ind.expression is null,
	 * a string representation of the genome is computed and stored there.
	 * 
	 * @param inputs
	 *            inputs used to evaluate genome
	 * @param ind
	 *            the current individual
	 * 
	 * @return array of computed outputs from our Cartesian genetic program
	 */
	public static Object[] evaluate(EvolutionState state, int threadNum,
			Object[] inputs, VectorIndividualCGP ind) {
		nodeMap.get(threadNum).clear();
		expressionMap.get(threadNum).clear();
		
		VectorSpeciesCGP s = (VectorSpeciesCGP) ind.species;
		Object[] outputs = new Object[s.numOutputs];

		/** Are we using a float[] or int[] representation? */
		int[] gi = null;
		float[] gf = null;
		boolean isFloat = false;
		if (ind instanceof IntegerVectorIndividual)
			gi = ((IntegerVectorIndividual) ind).genome;
		else {
			gf = ((FloatVectorIndividual) ind).genome;
			isFloat = true;
		}

		boolean expression = false;
		StringBuffer sb = null;
		if (ind.expression == null) {
			expression = true;
			sb = new StringBuffer();
		}
		/** Evaluate results for each output node. */
		for (int i = 0; i < outputs.length; i++) {
			add(expression, sb, "o" + i + " = ");
			outputs[i] = evalNode(threadNum, expression, inputs, sb, ind
					.getGenome(), isFloat ? s.interpretFloat(gf.length - 1 - i,
					gf) : gi[gi.length - 1 - i], s);
		}

		if (expression)
			ind.expression = sb;
		
		return outputs;
	}

	/**
	 * Computes the result of evaluating the given node.
	 * 
	 * @param threadNum
	 *            The current thread number.
	 * @param expression
	 *            If true, compute the string representation of the
	 *            sub-expression represented by this node.
	 * @param inputs The input values for this evaluation.
	 * 
	 * @param expr	Storage for the String-representation of the entire expression.
	 * @param genome The current genome.
	 * @param nodeNum The node number we are evaluating.
	 * @param s The CGP species
	 * @return The result of evaluation.
	 * 
	 * TODO: factor out all the float-vs-int checks to speed up evaluation.
	 * 
	 */
	private static Object evalNode(int threadNum, boolean expression,
			Object[] inputs, StringBuffer expr, Object genome, int nodeNum,
			VectorSpeciesCGP s) {
		Object val = nodeMap.get(threadNum).get(nodeNum);
		if (val != null) { /* We've already computed this node. */
			if (expression) /* append the already-computed expression string. */
				add(expression, expr, expressionMap.get(threadNum).get(nodeNum)); 
			return val; /* we already computed a result for this nodenumber, so
				just return the value. */
		}
		StringBuffer sb = null;
		if (expression)
			sb = new StringBuffer();
		// evals++;
		if (nodeNum < s.numInputs) { // output may have hooked directly to an
			// input. check that here.
			nodeMap.get(threadNum).put(nodeNum, inputs[nodeNum]);
			if (expression) {
				add(expression, sb, ""
						+ functions.inputName(nodeNum, inputs[nodeNum]));
				expressionMap.get(threadNum).put(nodeNum, sb.toString());
				expr.append(sb);
			}
			return inputs[nodeNum];
		}
		boolean isFloat = genome instanceof float[];
		int pos = s.positionFromNodeNumber(nodeNum);
		int fn = (isFloat ? s.interpretFloat(pos, (float[]) genome)
				: ((int[]) genome)[pos]);
		add(expression, sb, functions.functionName(fn));

		Object[] args = new Object[s.maxArity];
		for (int i = 0; i < functions.arityOf(fn); i++) { // eval each argument of the function
			int argInt = 0;
			float argFloat = 0;
			if (isFloat) {
				argFloat = ((float[]) genome)[pos + i + 1];
			} else {
				argInt = ((int[]) genome)[pos + i + 1];
			}

			int num = isFloat ? s.interpretFloat(pos + i + 1, (float[]) genome)
					: argInt;
			if (num < s.numInputs) { // argument refers to an input (terminal) node.
				args[i] = inputs[num];
				add(expression, sb, " " + functions.inputName(num, inputs[num]));
			} else { // argument refers to a function node.

				add(expression, sb, " (");
				args[i] = evalNode(threadNum, expression, inputs, sb, genome,
						num, s);
				add(expression, sb, ")");
			}
		}
		
		/* The arguments are ready now.  So, run the function. */
		Object result = functions.callFunction(args, fn, s.numFunctions);
		
		nodeMap.get(threadNum).put(nodeNum, result);
		if (expression) {
			expressionMap.get(threadNum).put(nodeNum, sb.toString());
			expr.append(sb);
		}
		return result;
	}

	/**
	 * Appends the given string to the given expression.
	 * @param expression If true, append; otherwise, ignore.
	 * @param sb The target expression.
	 * @param msg The snippet to append to the target.
	 */
	public static void add(boolean expression, StringBuffer sb, String msg) {
		if (expression)
			sb.append(msg);
	}

	public static void debug(String msg) {
		if (DEBUG)
			System.out.println(msg);
	}

}
