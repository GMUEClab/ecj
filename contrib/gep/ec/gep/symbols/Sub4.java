package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The subtraction of 4 parameters.
 */

public class Sub4 extends GEPFunctionSymbol {

	/**
	 * The subtraction of 4 parameters.
	 */
	public Sub4() 
	{
		super("sub4", 4);
	}

	/**
	 * Evaluate the subtraction of 4 parameters.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return (params[0] - params[1] - params[2] - params[3])
	 */
	public double eval(double params[]) 
	{
		//should check that there are 4 params
		return (params[0] - params[1] - params[2] - params[3]);
	}
	
	/**
	 * subtraction is not a logical function.
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
		return "("+p[0] + "-" + p[1] + "-" + p[2] + "-" + p[3] + ")";
	}
}
