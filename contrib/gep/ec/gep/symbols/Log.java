package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The logarithm (base 10) of a single parameter  log10(x).
 */

public class Log extends GEPFunctionSymbol {

	/**
	 * The logarithm (base 10) of a single parameter  log10(x).
	 */
	public Log() 
	{
		super("log", 1);
	}

	/**
	 * Evaluate the logarithm (base 10) of a single parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return log10(params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (Math.log10(params[0]));
	}
	
	/**
	 * Log is not a logical function.
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
		return "log10("+p[0]+")";
	}
}
