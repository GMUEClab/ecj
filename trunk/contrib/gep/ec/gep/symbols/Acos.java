package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The arcosine value of a single parameter.
 */

public class Acos extends GEPFunctionSymbol {

	/**
	 * The arcosine value of a single parameter.
	 */
	public Acos() 
	{
		super("acos", 1);
	}

	/**
	 * Evaluate the arcosine of a single parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return arcosine(params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (Math.acos(params[0]));
	}
	
	/**
	 * Arcosine is not a logical function.
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
		return "acos("+p[0]+")";
	}
}
