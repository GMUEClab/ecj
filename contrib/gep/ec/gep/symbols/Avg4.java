package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The average of 4 parameters.
 */

public class Avg4 extends GEPFunctionSymbol {

	/**
	 * The average of 4 parameters.
	 */
	public Avg4() 
	{
		super("avg4", 4);
	}

	/**
	 * Evaluate the average of 4 parameters.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return average(params[0],params[1], params[2], params[3])
	 */
	public double eval(double params[]) 
	{
		//should check that there are 4 params
		return ((params[0] + params[1] + params[2] + params[3])/4.0);
	}
	
	/**
	 * Average is not a logical function.
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
		return "((" + p[0] + "+" + p[1] + "+" + p[2] + "+" + p[3] + ")/4)";
	}
}
