package ec.cgp.functions;


/**
 *
 * Function set for the regression problems.
 * 
 * @author David Oranchak, doranchak@gmail.com, http://oranchak.com
 *
 */
public class FunctionsRegression implements Functions {
	
	/** Add */
	static int F_ADD = 0;
	/** Subtract */
	static int F_SUB = 1;
	/** Multiply */
	static int F_MUL = 2;
	/** Safe divide */
	static int F_DIV = 3;
	
	/** Interpret the given function and apply it to the given inputs. */
	public Object callFunction(Object[] inputs, int function, int numFunctions) {
		if (function == F_ADD) {
			return (Float)inputs[0] + (Float)inputs[1];
		} else if (function == F_SUB) {
			return (Float)inputs[0] - (Float)inputs[1];
		} else if (function == F_MUL) {
			return (Float)inputs[0] * (Float)inputs[1];
		} else if (function == F_DIV) {
			if ((Float)inputs[1] == 0) return 1f;
			return (Float)inputs[0] / (Float)inputs[1];
		} else throw new IllegalArgumentException("Function #" + function + " is unknown.");
	}
	
	/**
	 * Return a function name, suitable for display in expressions, for the
	 * given function.
	 */
	public String functionName(int fn) {
		if (fn == F_ADD) return "+";
		if (fn == F_SUB) return "-";
		if (fn == F_MUL) return "*";
		if (fn == F_DIV) return "/";
		else return "UNKNOWN FUNCTION";
	}
	
	/** Return the arity of the given function */
	public int arityOf(int fn) {
		return 2;
	}
	
	
	/** Return the name, suitable for display, for the given input. */ 
	public String inputName(int inp, Object val) {
		if (inp == 0) return "x"; // dependent variable
		return ""+val; // a constant value
	}
	
}
