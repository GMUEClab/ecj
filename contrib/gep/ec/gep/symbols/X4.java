package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The quartic of 1 parameter.
 */

public class X4 extends GEPFunctionSymbol {

	/**
	 * The quartic of 1 parameter.
	 */
	public X4() 
	{
		super("X4", 1);
	}

	/**
	 * Evaluate the quartic of 1 parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return (params[0] * params[0] * params[0] * params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is exactly 1 paramater
		double tmp = params[0] * params[0];
		return (tmp * tmp);
	}
	
	/**
	 * X4 is not a logical function.
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
		return "("+p[0] + "^4)";
	}
}
