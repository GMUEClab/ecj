package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The square root value of a single parameter.
 */

public class Sqrt extends GEPFunctionSymbol {

	/**
	 * The square root value of a single parameter.
	 */
	public Sqrt() 
	{
		super("sqrt", 1);
	}

	/**
	 * Evaluate the square root of a single parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return sqrt(params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (Math.sqrt(params[0]));
	}
	
	/**
	 * Square root is not a logical function.
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
		return "sqrt(" + p[0] + ")";
	}
}
