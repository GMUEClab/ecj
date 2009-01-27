package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The division of 4 parameters.
 */

public class Div4 extends GEPFunctionSymbol {

	/**
	 * The division of 4 parameters.
	 */
	public Div4() 
	{
		super("div4", 4);
	}

	/**
	 * Evaluate the division of 4 parameters.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return (params[0] / params[1] / params[2] / params[3])
	 */
	public double eval(double params[]) 
	{
		//should check that there are 4 params
		return (params[0] / params[1] / params[2] / params[3]);
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
		return "("+p[0] + "/" + p[1] + "/" + p[2] + "/" + p[3] + ")";
	}
}
