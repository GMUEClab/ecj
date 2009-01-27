package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The division of 2 parameters.
 */

public class Div extends GEPFunctionSymbol {

	/**
	 * The division of 2 parameters.
	 */
	public Div() 
	{
		super("/", 2);
	}

	/**
	 * Evaluate the division of 2 parameters.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return (params[0] / params[1])
	 */
	public double eval(double params[]) 
	{
		//should check that there are 2 params
		// and need to handle a 0 denominator ... see what they do in GP
		return (params[0] / params[1]);
	}
	
	/**
	 * Division is not a logical function.
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
		return "("+p[0] + "/" + p[1] + ")";
	}
}
