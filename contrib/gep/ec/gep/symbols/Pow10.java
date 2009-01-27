package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The value of 10 raised to a power specified by a parameter.
 */

public class Pow10 extends GEPFunctionSymbol {

	/**
	 * The value of 10 raised to a power specified by a parameter.
	 */
	public Pow10() 
	{
		super("pow10", 1);
	}

	/**
	 * Evaluate 10 raised to the power specified by a parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return pow(10, params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is exactly 1 paramater
		return (Math.pow(10.0, params[0]));
	}
	
	/**
	 * Pow10 is not a logical function.
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
		return "pow(10," + p[0] + ")";
	}
}
