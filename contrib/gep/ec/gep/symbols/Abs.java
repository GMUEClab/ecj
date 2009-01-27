package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The absolute value of a single parameter.
 */

public class Abs extends GEPFunctionSymbol {

	/**
	 * The absolute value of a single parameter.
	 */
	public Abs() 
	{
		super("abs", 1);
	}

	/**
	 * Evaluate the absolute value of a single parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return absoluteValue(params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 parameter
		return (Math.abs(params[0]));
	}
	
	/**
	 * Absolute value is not a logical function.
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
		return "abs("+p[0]+")";
	}

}
