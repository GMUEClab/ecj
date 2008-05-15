package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

/**
 * The value E (2.71828...)
 */

public class E extends GEPFunctionSymbol {

	/**
	 * The value E (2.71828...)
	 */
	public E() 
	{
		super("e", 0);
	}

	/**
	 * Evaluate the number E (2.71828...).
	 * 
	 * @param params double array with the required parameter(s)
	 * @return (E)
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater ... but it is really a constant ... 0 args!
		// This is oddity of GeneXPro ... 1 arg rather than 0!
		return (Math.E);
	}
	
	/**
	 * E is not a logical function.
	 * @return false
	 */
	public boolean isLogicalFunction()
	{
		return false;
	}

	/**
	 * The human readable form of the expression
	 */
	public String printMathExpression( String p[] )
	{
		return "e";
	}
}
