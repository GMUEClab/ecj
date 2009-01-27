package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The average of 2 parameters.
 */

public class Avg2 extends GEPFunctionSymbol {

	/**
	 * The average of 2 parameters.
	 */
	public Avg2() 
	{
		super("avg2", 2);
	}

	/**
	 * Evaluate the average of 2 parameters.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return average(params[0],params[1])
	 */
	public double eval(double params[]) 
	{
		//should check that there are 2 params
		return ((params[0]+ params[1])/2.0);
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
		return "((" + p[0] + "+" + p[1] + ")/2)";
	}
}
