package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The cosecant value of a single parameter (radians).
 */

public class Csc extends GEPFunctionSymbol {

	/**
	 * The cosecant value of a single parameter (radians).
	 */
	public Csc() 
	{
		super("csc", 1);
	}

	/**
	 * Evaluate the cosecant of a single parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return cosecant(params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (1.0/Math.sin(params[0]));
	}
	
	/**
	 * Cosecant is not a logical function.
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
		return "csc("+p[0]+")";
	}
}
