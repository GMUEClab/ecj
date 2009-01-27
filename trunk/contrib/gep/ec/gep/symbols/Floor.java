package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The Floor value of a single parameter (promotes the double value to the closest lower integer value).
 */

public class Floor extends GEPFunctionSymbol {

	/**
	 * The Floor value of a single parameter (promotes the double value to the closest lower integer value).
	 */
	public Floor() 
	{
		super("floor", 1);
	}

	/**
	 * Evaluate the Floor of a single parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return floor(params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (Math.floor(params[0]));
	}
	
	/**
	 * Floor is not a logical function.
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
		return "floor(" + p[0] + ")";
	}
}
