package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The hyperbolic cotangent value of a single parameter (radians).
 */

public class Coth extends GEPFunctionSymbol {

	/**
	 * The hyperbolic cotangent value of a single parameter (radians).
	 */
	public Coth() 
	{
		super("coth", 1);
	}

	/**
	 * Evaluate the hyperbolic cotangent of a single parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return coth(params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return ((Math.pow(Math.E, 2.0*params[0])+1)/(Math.pow(Math.E, 2.0*params[0])-1));
	}
	
	/**
	 * Hyperbolic Cotangent is not a logical function.
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
		return "coth("+p[0]+")";
	}
}
