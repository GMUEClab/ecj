package ec.cgp.functions;


/**
 * 
 * Function set for the even-n-parity problem.
 * 
 * @author David Oranchak, doranchak@gmail.com, http://oranchak.com
 * 
 */
public class FunctionsParity implements Functions {

	/** logical and */
	static int F_AND = 0;
	/** logical or */
	static int F_OR = 1;
	/** logical not */
	static int F_NOT = 2;
	/** logical nor */
	static int F_NOR = 3;
	/** logical nand */
	static int F_NAND = 4;

	/** Interpret the given function and apply it to the given inputs. */
	public Object callFunction(Object[] inputs, int function, int numFunctions) {
		if (function == F_AND) {
			return (Boolean) inputs[0] & (Boolean) inputs[1];
		} else if (function == F_OR) {
			return (Boolean) inputs[0] | (Boolean) inputs[1];
		} else if (function == F_NOT) {
			return !(Boolean) inputs[0];
		} else if (function == F_NOR) {
			return !((Boolean) inputs[0] | (Boolean) inputs[1]);
		} else if (function == F_NAND) {
			return !((Boolean) inputs[0] & (Boolean) inputs[1]);
		} else
			throw new IllegalArgumentException("Function #" + function
					+ " is unknown.");
	}

	/**
	 * Return a function name, suitable for display in expressions, for the
	 * given function.
	 */
	public String functionName(int fn) {
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
		else
			return "UNKNOWN FUNCTION";
	}

	/** Return the name, suitable for display, for the given input. */
	public String inputName(int inp, Object val) {
		if (inp < 8)
			return "x" + inp; 	// dependent variable. TODO: "8" is hardcoded;
								// it needs to be passed in depending on which
								// parity problem we're running.
		
		return "" + val; 		// a constant value
	}

	/** Return the arity of the given function */
	public int arityOf(int fn) {
		return fn == F_NOT ? 1 : 2;
	}


}
