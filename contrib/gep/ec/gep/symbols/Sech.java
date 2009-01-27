package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The hyperbolic secant of a single parameter (radians).
 */

public class Sech extends GEPFunctionSymbol {

	/**
	 * The hyperbolic secant of a single parameter (radians).
	 */
	public Sech() 
	{
		super("sech", 1);
	}

	/**
	 * Evaluate the hyperbolic secant of a single parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return sech(params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (1.0/Math.cosh(params[0]));
	}
	
	/**
	 * sech is not a logical function.
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
		return "sech(" + p[0] + ")";
	}
}
