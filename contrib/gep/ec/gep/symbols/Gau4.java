package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * Gaussian function (exp(-pow(w+x+y+z, 2.0)).
 */

public class Gau4 extends GEPFunctionSymbol {

	/**
	 * Gaussian function (exp(-pow(w+x+y+z, 2.0)).
	 */
	public Gau4() 
	{
		super("gau4", 4);
	}

	/**
	 * Evaluate the gaussian with 4 parameters (exp(-pow(params[0]+params[1]+params[2]+params[3], 2.0)).
	 * 
	 * @param params double array with the required parameter(s)
	 * @return (exp(-pow(params[0]+params[1]+params[2]+params[3], 2.0))
	 */
	public double eval(double params[]) 
	{
		//should check that there are exactly 4 paramaters
		return (Math.exp(-Math.pow(params[0]+params[1]+params[2]+params[3], 2.0)));
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
		return "exp(-pow(" + p[0] + "+" + p[1]+ "+" + p[2] + "+" + p[3] +  ",2))";
	}
}
