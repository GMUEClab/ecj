package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The negation of 1 parameter.
 */

public class Neg extends GEPFunctionSymbol {

	/**
	 * The negation of 1 parameter.
	 */
	public Neg() 
	{
		super("neg", 1);
	}

	/**
	 * Evaluate the negation of 1 parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return (-params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (-params[0]);
	}
	
	/**
	 * Negation is not a logical function.
	 * @return false
	 */
	public boolean isLogicalFunction()
	{
		return false;
	}

	/**
	 * The human readable form of the expression
	 */
	public String getMathExpressionAsString( String p[] )
	{
		return "(-" + p[0] + ")";
	}
}
