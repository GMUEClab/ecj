package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The minimum of 2 parameters.
 */

public class Min2 extends GEPFunctionSymbol {

	/**
	 * The minimum of 2 parameters.
	 */
	public Min2() 
	{
		super("min2", 2);
	}

	/**
	 * Evaluate the minimum of 2 parameters.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return min(params[0], params[1])
	 */
	public double eval(double params[]) 
	{
		//should check that there are 2 params
		return (Math.min(params[0], params[1]));
	}
	
	/**
	 * Min2 is not a logical function.
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
		return "min(" + p[0] + "," + p[1] + ")";
	}
}
