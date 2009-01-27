package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The maximum of 2 parameters.
 */

public class Max2 extends GEPFunctionSymbol {

	/**
	 * The maximum of 2 parameters.
	 */
	public Max2() 
	{
		super("max2", 2);
	}

	/**
	 * Evaluate the maximum of 2 parameters.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return max(params[0], params[1])
	 */
	public double eval(double params[]) 
	{
		//should check that there are 2 params
		return (Math.max(params[0], params[1]));
	}
	
	/**
	 * Max2 is not a logical function.
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
		return "max(" + p[0] + "," + p[1] + ")";
	}
}
