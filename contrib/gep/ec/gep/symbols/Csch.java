package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The hyperbolic cosecant value of a single parameter (radians).
 */

public class Csch extends GEPFunctionSymbol {

	/**
	 * The hyperbolic cosecant value of a single parameter (radians).
	 */
	public Csch() 
	{
		super("csch", 1);
	}

	/**
	 * Evaluate the csch of a single parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return csch(params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (1.0/Math.sinh(params[0]));
	}
	
	/**
	 * hyperbolic cosecant is not a logical function.
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
		return "csch("+p[0]+")";
	}
}
