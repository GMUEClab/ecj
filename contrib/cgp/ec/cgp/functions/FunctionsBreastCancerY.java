package ec.cgp.functions;


import ec.util.MersenneTwisterFast;

/**
 * Function set for the Breast Cancer (Wisconsin) classification problem.
 * 
 * @author David Oranchak, doranchak@gmail.com, http://oranchak.com
 * 
 */
public class FunctionsBreastCancerY implements Functions {

	/** add */
	static int F_ADD = 0;
	/** subtract */
	static int F_SUB = 1;
	/** multiply */
	static int F_MUL = 2;
	/** safe divide; return 1 if divisor is 0. */
	static int F_DIV = 3;
	/** less than */
	static int F_LT = 4;
	/** less than or equal to */
	static int F_LTE = 5;
	/** greater than */
	static int F_GT = 6;
	/** greater than or equal to */
	static int F_GTE = 7;
	/** equal to */
	static int F_EQ = 8;
	/** logical and */
	static int F_AND = 9;
	/** logical or */
	static int F_OR = 10;
	/** logical not */
	static int F_NOT = 11;
	/** logical nor */
	static int F_NOR = 12;
	/** logical nand */
	static int F_NAND = 13;
	/** negate */
	static int F_NEG = 14; /*
							 * you must ensure all arity-3 functions appear
							 * after this one (convention imposed by our arityOf method.)
							 */
	/** if... then... else */
	static int F_IF = 15;
	/** if less than zero then... else... */
	static int F_IFLEZ = 16;

	/** Interpret the given function and apply it to the given inputs. */
	public Object callFunction(Object[] inputs, int function, int numFunctions) {
		float[] arg = new float[inputs.length];
		for (int i = 0; i < inputs.length; i++)
			if (!(inputs[i] instanceof Float))
				arg[i] = 0f;
			else
				arg[i] = (Float) inputs[i];

		if (function == F_ADD) {
			return arg[0] + arg[1];
		} else if (function == F_SUB) {
			return arg[0] - arg[1];
		} else if (function == F_MUL) {
			return arg[0] * arg[1];
		} else if (function == F_DIV) {
			if (arg[1] == 0)
				return 1f;
			return arg[0] / arg[1];
		} else if (function == F_LT) {
			return b2f(arg[0] < arg[1]);
		} else if (function == F_LTE) {
			return b2f(arg[0] <= arg[1]);
		} else if (function == F_GT) {
			return b2f(arg[0] > arg[1]);
		} else if (function == F_GTE) {
			return b2f(arg[0] >= arg[1]);
		} else if (function == F_EQ) {
			return b2f(arg[0] == arg[1]);
		} else if (function == F_AND) {
			return b2f(f2b(arg[0]) & f2b(arg[1]));
		} else if (function == F_OR) {
			return b2f(f2b(arg[0]) | f2b(arg[1]));
		} else if (function == F_NOT) {
			return b2f(!f2b(arg[0]));
		} else if (function == F_NOR) {
			return b2f(!(f2b(arg[0]) | f2b(arg[1])));
		} else if (function == F_NAND) {
			return b2f(!(f2b(arg[0]) & f2b(arg[1])));
		} else if (function == F_NEG) {
			return -arg[0];
		} else if (function == F_IF) {
			if (f2b(arg[0]))
				return arg[1];
			else
				return arg[2];
		} else if (function == F_IFLEZ) {
			if (arg[0] <= 0)
				return arg[1];
			else
				return arg[2];
		} else
			throw new IllegalArgumentException("Function #" + function
					+ " is unknown.");
	}

	/**
	 * Interpret the given float as a boolean value. Any value > 0 is
	 * interpreted as "true".
	 */
	public static boolean f2b(float inp) {
		return inp > 0 ? true : false;
	}

	/** Convert the given boolean to float. "True" is 1.0; "false" is -1.0. */
	public static float b2f(boolean inp) {
		return inp ? 1f : -1f;
	}

	/**
	 * Return a function name, suitable for display in expressions, for the
	 * given function.
	 */
	public String functionName(int fn) {
		if (fn == F_ADD)
			return "+";
		if (fn == F_SUB)
			return "-";
		if (fn == F_MUL)
			return "*";
		if (fn == F_DIV)
			return "/";
		if (fn == F_LT)
			return "<";
		if (fn == F_LTE)
			return "<=";
		if (fn == F_GT)
			return ">";
		if (fn == F_GTE)
			return ">=";
		if (fn == F_EQ)
			return "=";
		if (fn == F_AND)
			return "and";
		if (fn == F_OR)
			return "or";
		if (fn == F_NOT)
			return "not";
		if (fn == F_NOR)
			return "nor";
		if (fn == F_NAND)
			return "nand";
		if (fn == F_NEG)
			return "neg";
		if (fn == F_IF)
			return "if";
		if (fn == F_IFLEZ)
			return "iflez";
		else
			return "UNKNOWN FUNCTION";
	}

	/** Return the arity of the given function */
	public int arityOf(int fn) {
		if (fn == F_NOT) return 1;
		if (fn == F_NEG) return 1;
		return fn > F_NEG ? 3 : 2;

	}

	/** Return the name, suitable for display, for the given input. */ 
	public String inputName(int inp, Object val) {
		if (inp == 0)
			return "age";
		if (inp == 1)
			return "menopause";
		if (inp == 2)
			return "tumorSize";
		if (inp == 3)
			return "invNodes";
		if (inp == 4)
			return "nodeCaps";
		if (inp == 5)
			return "degMalig";
		if (inp == 6)
			return "breast";
		if (inp == 7)
			return "breastQuad";
		if (inp == 8)
			return "irrad";
		if (inp == 9)
			return "" + val;
		if (inp == 10)
			return "" + val;
		if (inp == 11)
			return "" + val;
		else
			return "UNKNOWN INPUT";
	}

	/** Simple test of the function set. */
	public static void testFunctions() {
		FunctionsBreastCancerY f = new FunctionsBreastCancerY();

		Float[] inputs;
		MersenneTwisterFast rand = new MersenneTwisterFast();

		for (int i = 0; i < 100; i++) {
			inputs = new Float[] { 2f * (.5f - rand.nextFloat()),
					2f * (.5f - rand.nextFloat()) };
			for (int j = 0; j < 15; j++) {
				System.out.println(inputs[0] + " " + f.functionName(j) + " "
						+ inputs[1] + " = " + f.callFunction(inputs, j, 15));

			}
		}
	}

	public static void main(String[] args) {
		testFunctions();
	}
}
