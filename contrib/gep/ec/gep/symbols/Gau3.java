package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * Gaussian function (exp(-pow(x+y+z, 2.0)).
 */

public class Gau3 extends GEPFunctionSymbol {

	/**
	 * Gaussian function (exp(-pow(x+y+z, 2.0)).
	 */
	public Gau3() 
	{
		super("gau3", 3);
	}

	/**
	 * Evaluate the gaussian with 3 parameters (exp(-pow(params[0]+params[1]+params[2], 2.0)).
	 * 
	 * @param params double array with the required parameter(s)
	 * @return (exp(-pow(params[0]+params[1]+params[2], 2.0))
	 */
	public double eval(double params[]) 
	{
		//should check that there are exactly 3 paramaters
		return (Math.exp(-Math.pow(params[0]+params[1]+params[2], 2.0)));
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
		return "exp(-pow(" + p[0] + "+" + p[1]+ "+" + p[2] +  ",2))";
	}
}
