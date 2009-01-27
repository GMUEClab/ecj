package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The Ceiling value of a single parameter (promotes the double value to the closest higher integer value).
 */

public class Ceiling extends GEPFunctionSymbol {

	/**
	 * The Ceiling value of a single parameter (promotes the double value to the closest higher integer value).
	 */
	public Ceiling() 
	{
		super("ceil", 1);
	}

	/**
	 * Evaluate the ceiling of a single parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return ceiling(params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (Math.ceil(params[0]));
	}
	
	/**
	 * Ceiling is not a logical function.
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
		return "ceiling("+p[0]+")";
	}
}
