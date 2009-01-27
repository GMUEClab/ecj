package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The secant of a single parameter (radians).
 */

public class Sec extends GEPFunctionSymbol {

	/**
	 * The secant of a single parameter (radians).
	 */
	public Sec() 
	{
		super("sec", 1);
	}

	/**
	 * Evaluate the secant of a single parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return sec(params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (1.0/Math.cos(params[0]));
	}
	
	/**
	 * secant is not a logical function.
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
		return "sec(" + p[0] + ")";
	}
}
