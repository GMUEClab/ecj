package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The hyperbolic cosine value of a single parameter (radians).
 */

public class Cosh extends GEPFunctionSymbol {

	/**
	 * The hyperbolic cosine value of a single parameter (radians).
	 */
	public Cosh() 
	{
		super("cosh", 1);
	}

	/**
	 * Evaluate the cosh of a single parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return cosh(params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (Math.cosh(params[0]));
	}
	
	/**
	 * hyperbolic cosine is not a logical function.
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
		return "cosh("+p[0]+")";
	}
}
