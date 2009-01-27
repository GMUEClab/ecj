package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The Arctangent value of a single parameter.
 */

public class Atan extends GEPFunctionSymbol {

	/**
	 * The Arctangent value of a single parameter.
	 */
	public Atan() 
	{
		super("atan", 1);
	}

	/**
	 * Evaluate the arctangent of a single parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return arctangent(params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (Math.atan(params[0]));
	}
	
	/**
	 * Arctangent is not a logical function.
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
		return "atan("+p[0]+")";
	}
}
