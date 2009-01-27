package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The multiplication of 2 parameters.
 */

public class Mul extends GEPFunctionSymbol {

	/**
	 * The multiplication of 2 parameters.
	 */
	public Mul() 
	{
		super("*", 2);
	}

	/**
	 * Evaluate the multiplication of 2 parameters.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return (params[0] * params[1])
	 */
	public double eval(double params[]) 
	{
		//should check that there are 2 params
		return (params[0] * params[1]);
	}
	
	/**
	 * multiplication is not a logical function.
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
		return "("+p[0] + "*" + p[1] + ")";
	}
}
