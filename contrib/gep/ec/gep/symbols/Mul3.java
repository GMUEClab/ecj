package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The multiplication of 3 parameters.
 */

public class Mul3 extends GEPFunctionSymbol {

	/**
	 * The multiplication of 3 parameters.
	 */
	public Mul3() 
	{
		super("mul3", 3);
	}

	/**
	 * Evaluate the multiplication of 3 parameters.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return (params[0] * params[1] * params[2])
	 */
	public double eval(double params[]) 
	{
		//should check that there are 3 params
		return (params[0] * params[1] * params[2]);
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
		return "("+p[0] + "*" + p[1] + "*" + p[2] + ")";
	}
}
