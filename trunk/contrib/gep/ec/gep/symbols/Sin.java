package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The sine of a single parameter (radians).
 */

public class Sin extends GEPFunctionSymbol {

	/**
	 * The sine of a single parameter (radians).
	 */
	public Sin() 
	{
		super("sin", 1);
	}

	/**
	 * Evaluate the sine of a single parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return sin(params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (Math.sin(params[0]));
	}
	
	/**
	 * sin is not a logical function.
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
		return "sin(" + p[0] + ")";
	}
}
