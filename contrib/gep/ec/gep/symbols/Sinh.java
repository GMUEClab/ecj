package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The hyperbolic sine of a single parameter (radians).
 */

public class Sinh extends GEPFunctionSymbol {

	/**
	 * The hyperbolic sine of a single parameter (radians).
	 */
	public Sinh() 
	{
		super("sinh", 1);
	}

	/**
	 * Evaluate the hyperbolic sine of a single parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return sinh(params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (Math.sinh(params[0]));
	}
	
	/**
	 * sinh is not a logical function.
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
		return "sinh(" + p[0] + ")";
	}
}
