package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * Gaussian function (exp(-pow(x, 2.0)).
 */

public class Gau extends GEPFunctionSymbol {

	/**
	 * Gaussian function (exp(-pow(x, 2.0)).
	 */
	public Gau() 
	{
		super("gau", 1);
	}

	/**
	 * Evaluate the gaussian of the single parameter (exp(-pow(params[0], 2.0)).
	 * 
	 * @param params double array with the required parameter(s)
	 * @return (exp(-pow(params[0], 2.0))
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (Math.exp(-Math.pow(params[0], 2.0)));
	}
	
	/**
	 * Gau is not a logical function.
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
		return "exp(-pow(" + p[0] + ",2))";
	}
}
