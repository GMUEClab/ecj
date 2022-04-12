package ec.cgp.functions;

/**
 * 
 * Function set for the boolean function learning.
 * Interprets the inputs data as long for the bitwise boolean operations.
 * 
 * @author Roman Kalkreuth, roman.kalkreuth@tu-dortmund.de,
 *         https://orcid.org/0000-0003-1449-5131,
 *         https://ls11-www.cs.tu-dortmund.de/staff/kalkreuth,
 *         https://twitter.com/RomanKalkreuth
 *         
 * @author David Oranchak, doranchak@gmail.com, http://oranchak.com
 * 
 */
public class FunctionsBooleanLong implements Functions {

	static int F_AND = 0;
	static int F_OR = 1;
	static int F_NOR = 2;
	static int F_NAND = 3;
	static int F_NOT = 4;
	static int F_XOR = 5;
	static int F_XNOR = 6;
	static int F_AND_INV = 7;

	/** Interpret the given function and apply it to the given inputs. */
	public Object callFunction(Object[] inputs, int function, int numFunctions) {
		if (function == F_AND) {
			return ((long) inputs[0] & (long) inputs[1]);
		} else if (function == F_OR) {
			return (long) inputs[0] | (long) inputs[1];
		} else if (function == F_NOR) {
			return ~((long) inputs[0] | (long) inputs[1]);
		} else if (function == F_NAND) {
			return ~((long) inputs[0] & (long) inputs[1]);
		} else if (function == F_NOT) {
			return ~((long) inputs[0]);
		} else if (function == F_XOR) {
			return ((long) inputs[0] ^ (long) inputs[1]);
		} else if (function == F_XNOR) {
			return ~((long) inputs[0] ^ (long) inputs[1]);
		} else if (function == F_AND_INV) {
			return ((~(long) inputs[0]) & 0xFF | (long) inputs[1]);
		} else
			throw new IllegalArgumentException("Function #" + function + " is unknown.");
	}

	/**
	 * Return a function name, suitable for display in expressions, for the given
	 * function.
	 */
	public String functionName(int fn) {
		if (fn == F_AND)
			return "and";
		if (fn == F_OR)
			return "or";
		if (fn == F_NOR)
			return "nor";
		if (fn == F_NAND)
			return "nand";
		if (fn == F_NOT)
			return "not";
		if (fn == F_XOR)
			return "xor";
		if (fn == F_XNOR)
			return "xnor";
		if (fn == F_AND_INV)
			return "and_inv";
		else
			return "UNKNOWN FUNCTION";
	}

	/** Return the name, suitable for display, for the given input. */
	public String inputName(int inp, Object val) {
		return "x" + inp;
	}

	/** Return the arity of the given function */
	public int arityOf(int fn) {
		return fn == F_NOT ? 1 : 2;
	}

}
