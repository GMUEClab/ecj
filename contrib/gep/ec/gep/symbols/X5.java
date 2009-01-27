package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * 1 parameter raised to the 5th power.
 */

public class X5 extends GEPFunctionSymbol {

	/**
	 * 1 parameter raised to the 5th power.
	 */
	public X5() 
	{
		super("X5", 1);
	}

	/**
	 * Evaluate 1 parameter raised to the 5th power.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return (params[0] * params[0] * params[0] * params[0] * param[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is exactly 1 paramater
		double tmp = params[0] * params[0];
		return (tmp * tmp * params[0]);
	}
	
	/**
	 * X5 is not a logical function.
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
		return "(" + p[0] + "^5)";
	}
}
