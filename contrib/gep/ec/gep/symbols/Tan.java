package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The tangent of a single parameter (radians).
 */

public class Tan extends GEPFunctionSymbol {

	/**
	 * The tangent of a single parameter (radians).
	 */
	public Tan() 
	{
		super("tan", 1);
	}

	/**
	 * Evaluate the tangent of a single parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return tan(params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (Math.tan(params[0]));
	}
	
	/**
	 * Tangent is not a logical function.
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
		return "tan(" + p[0] + ")";
	}
}
