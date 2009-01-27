package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The minimum of 4 parameters.
 */

public class Min4 extends GEPFunctionSymbol {

	/**
	 * The minimum of 4 parameters.
	 */
	public Min4() 
	{
		super("min4", 4);
	}

	/**
	 * Evaluate the minimum of 4 parameters.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return min(params[0], params[1], params[2], params[4])
	 */
	public double eval(double params[]) 
	{
		//should check that there are 4 params
		return (Math.min(Math.min(params[0], params[1]), Math.min(params[2], params[3])));
	}
	
	/**
	 * Min4 is not a logical function.
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
		return "min4(" + p[0] + "," + p[1] + "," + p[2] + "," + p[3] + ")";
	}
}
