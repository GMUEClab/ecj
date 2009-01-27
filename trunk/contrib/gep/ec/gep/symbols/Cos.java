package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The cosine value of a single parameter (radians).
 */

public class Cos extends GEPFunctionSymbol {

	/**
	 * The cosine value of a single parameter (radians).
	 */
	public Cos() 
	{
		super("cos", 1);
	}

	/**
	 * Evaluate the cosine of a single parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return cosine(params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (Math.cos(params[0]));
	}
	
	/**
	 * cosine is not a logical function.
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
		return "cos("+p[0]+")";
	}
}
