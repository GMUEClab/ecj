package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The Arcsecant value of a single parameter.
 */

public class Asec extends GEPFunctionSymbol {

	/**
	 * The Arcsecant value of a single parameter.
	 */
	public Asec() 
	{
		super("asec", 1);
	}

	/**
	 * Evaluate the arcsecant of a single parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return arcsecant(params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (Math.acos(1.0/params[0]));
	}
	
	/**
	 * Arcsecant is not a logical function.
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
		return "asec("+p[0]+")";
	}
}
