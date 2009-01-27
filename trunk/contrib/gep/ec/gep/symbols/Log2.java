package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The logarithm (to a specified base)   log(x, base).
 */

public class Log2 extends GEPFunctionSymbol {

	/**
	 * The logarithm (to a specified base)   log(x, base).
	 */
	public Log2() 
	{
		super("log2", 2);
	}

	/**
	 * Evaluate the logarithm (to a specified base).
	 * 
	 * @param params double array with the required parameter(s)
	 * @return log2(params[0], params[1])
	 */
	public double eval(double params[]) 
	{
		//should check that there are exactly 2 paramaters
		// param 0 is the value, param 1 is the base
		return (Math.log(params[0])/Math.log(params[1]));
	}
	
	/**
	 * Log2 is not a logical function.
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
		return "ln("+p[0]+")/ln("+p[1]+")";
	}
}
