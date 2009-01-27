package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The minimum of 3 parameters.
 */

public class Min3 extends GEPFunctionSymbol {

	/**
	 * The minimum of 3 parameters.
	 */
	public Min3() 
	{
		super("min3", 3);
	}

	/**
	 * Evaluate the minimum of 3 parameters.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return min(params[0], params[1], params[2])
	 */
	public double eval(double params[]) 
	{
		//should check that there are 3 params
		return (Math.min(Math.min(params[0], params[1]), params[2]));
	}
	
	/**
	 * Min3 is not a logical function.
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
		return "min3(" + p[0] + "," + p[1] + "," + p[2] + ")";
	}
}
