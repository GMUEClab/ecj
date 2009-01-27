package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The Arcsine value of a single parameter.
 */

public class Asin extends GEPFunctionSymbol {

	/**
	 * The Arcsine value of a single parameter.
	 */
	public Asin() 
	{
		super("asin", 1);
	}

	/**
	 * Evaluate the arcsine of a single parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return arcsine(params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (Math.asin(params[0]));
	}
	
	/**
	 * Arcsine is not a logical function.
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
		return "asin("+p[0]+")";
	}
}
