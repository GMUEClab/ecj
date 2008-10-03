package ec.cgp.functions;

/**
 * This interface defines how the Cartesian Genetic Program evaluator calls
 * user-defined functions when they are encountered in the genome. You must
 * provide an implementation of this interface when plugging functions into the
 * CGP evaluations.
 * 
 * @author David Oranchak, doranchak@gmail.com, http://oranchak.com
 * 
 */
public interface Functions {

	/**
	 * Executes the given function with the given inputs.
	 * 
	 * @param inputs
	 *            The arguments passed to the function. It is possible that some
	 *            or all of the arguments will be unused by some functions.
	 * @param function
	 *            The function number. Every function number must map to a
	 *            function.
	 * @param numFunctions
	 *            The total number of functions available.
	 * @return the result of the function call
	 */
	public Object callFunction(Object[] inputs, int function, int numFunctions);

	/**
	 * Return a string representation of the given function name, used when
	 * putting together the string representing the entire Cartesian Genetic
	 * Program.
	 * 
	 * @param fn
	 *            The function number
	 * @return The descriptive function name that corresponds to this function
	 *         number.
	 */
	public String functionName(int fn);

	/**
	 * Obtain a string representation of the given input.
	 * 
	 * @param inp
	 *            The input number
	 * @param val
	 *            Optional input value (needed if you want to display a constant
	 *            value for this input)
	 * @return The descriptive input name that corresponds to this input.
	 */
	public String inputName(int inp, Object val);

	/**
	 * Return the arity of the given function.
	 * 
	 * @param fn
	 *            the function number
	 * @return The number of inputs this function expects.
	 */
	public int arityOf(int fn);

}
