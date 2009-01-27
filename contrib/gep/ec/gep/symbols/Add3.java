package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The addition of 3 parameters.
 */

public class Add3 extends GEPFunctionSymbol {

	/**
	 * The addition of 3 parameters.
	 */
	public Add3() 
	{
		super("add3", 3);
	}

	/**
	 * Evaluate the addition of 3 parameters.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return (params[0] + params[1] + params[2])
	 */
	public double eval(double params[]) 
	{
		//should check that there are 3 params
		return (params[0] + params[1] + params[2]);
	}
	
	/**
	 * Addition is not a logical function.
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
		return "(" + p[0] + "+" +p[1] + "+" + p[2] + ")";
	}
}
