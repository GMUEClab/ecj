package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The cube of 1 parameter.
 */

public class X3 extends GEPFunctionSymbol {

	/**
	 * The cube of 1 parameter.
	 */
	public X3() 
	{
		super("X3", 1);
	}

	/**
	 * Evaluate the cube of 1 parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return (params[0] * params[0] * params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is exactly 1 paramater
		return (params[0] * params[0] * params[0]);
	}
	
	/**
	 * X3 is not a logical function.
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
		return "("+p[0] + "^3)";
	}
}
