package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * Gaussian function (exp(-pow(x+y, 2.0)).
 */

public class Gau2 extends GEPFunctionSymbol {

	/**
	 * Gaussian function (exp(-pow(x+y, 2.0)).
	 */
	public Gau2() 
	{
		super("gau2", 2);
	}

	/**
	 * Evaluate the gaussian with 2 parameters (exp(-pow(params[0]+params[1], 2.0)).
	 * 
	 * @param params double array with the required parameter(s)
	 * @return (exp(-pow(params[0]+params[1], 2.0))
	 */
	public double eval(double params[]) 
	{
		//should check that there are exactly 2 paramaters
		return (Math.exp(-Math.pow(params[0]+params[1], 2.0)));
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
		return "exp(-pow(" + p[0] + "+" + p[1] +  ",2))";
	}
}
