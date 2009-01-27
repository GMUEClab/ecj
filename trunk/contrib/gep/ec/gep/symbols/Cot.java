package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The cotangent of a single parameter (radians).
 */

public class Cot extends GEPFunctionSymbol {

	/**
	 * The cotangent of a single parameter (radians).
	 */
	public Cot() 
	{
		super("cot", 1);
	}

	/**
	 * Evaluate the cotangent of a single parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return cotangent(params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (1.0/Math.tan(params[0]));
	}
	
	/**
	 * cotangent is not a logical function.
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
		return "cot("+p[0]+")";
	}
}
