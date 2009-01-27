package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The value of the 1st parameter raised to a power specified by a second parameter.
 */

public class Pow extends GEPFunctionSymbol {

	/**
	 * The value of the 1st parameter raised to a power specified by a second parameter.
	 */
	public Pow() 
	{
		super("pow", 2);
	}

	/**
	 * Evaluate 1st parameter raised to the power specified by a 2nd parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return pow(params[0], params[1])
	 */
	public double eval(double params[]) 
	{
		//should check that there are exactly 2 paramaters
		return (Math.pow(params[0], params[1]));
	}
	
	/**
	 * Pow is not a logical function.
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
		return "pow(" + p[0] + "," + p[1] + ")";
	}
}
