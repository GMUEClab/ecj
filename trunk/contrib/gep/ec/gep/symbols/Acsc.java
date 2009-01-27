package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The Arcosecant value of a single parameter.
 */

public class Acsc extends GEPFunctionSymbol {

	/**
	 * The Arcosecant value of a single parameter.
	 */
	public Acsc() 
	{
		super("acsc", 1);
	}

	/**
	 * Evaluate the arcosecant of a single parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return arcosecant(params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (Math.asin(1.0/params[0]));
	}
	
	/**
	 * Arcosecant is not a logical function.
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
		return "acsc("+p[0]+")";
	}
}
