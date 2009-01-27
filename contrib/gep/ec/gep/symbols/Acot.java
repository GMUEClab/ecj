package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The Arcotangent value of a single parameter.
 */

public class Acot extends GEPFunctionSymbol {

	/**
	 * The Arcotangent value of a single parameter.
	 */
	public Acot() 
	{
		super("acot", 1);
	}

	/**
	 * Evaluate the arcotangent of a single parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return arcotangent(params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return ((Math.PI/2.0)-Math.atan(params[0]));
	}
	
	/**
	 * Arcotangent is not a logical function.
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
		return "acot("+p[0]+")";
	}
}
