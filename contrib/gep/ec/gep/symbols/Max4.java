package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The maximum of 4 parameters.
 */

public class Max4 extends GEPFunctionSymbol {

	/**
	 * The maximum of 4 parameters.
	 */
	public Max4() 
	{
		super("max4", 4);
	}

	/**
	 * Evaluate the maximum of 4 parameters.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return max(params[0], params[1], params[2], params[4])
	 */
	public double eval(double params[]) 
	{
		//should check that there are 4 params
		return (Math.max(Math.max(params[0], params[1]), Math.max(params[2], params[3])));
	}
	
	/**
	 * Max4 is not a logical function.
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
		return "max4(" + p[0] + "," + p[1] + "," + p[2] + "," + p[3] + ")";
	}
}
