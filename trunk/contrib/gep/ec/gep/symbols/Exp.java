package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The value of e raised to a power specified by a single parameter.
 */

public class Exp extends GEPFunctionSymbol {

	/**
	 * The value of e raised to a power specified by a single parameter.
	 */
	public Exp() 
	{
		super("exp", 1);
	}

	/**
	 * Evaluate the e raised to the power specified by a single parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return exp(params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (Math.exp(params[0]));
	}
	
	/**
	 * Exp is not a logical function.
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
		return "exp(" + p[0] + ")";
	}
}
