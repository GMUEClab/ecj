package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The value the inverse of a single parameter  (1/x).
 */

public class Ln extends GEPFunctionSymbol {

	/**
	 * The natural logarithm of a single parameter  ln(x).
	 */
	public Ln() 
	{
		super("ln", 1);
	}

	/**
	 * Evaluate the natural logarithm of a single parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return ln(params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (Math.log(params[0]));
	}
	
	/**
	 * Ln is not a logical function.
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
		return "ln("+p[0]+")";
	}
}
