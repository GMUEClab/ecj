package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The logical xor of 2 parameters.
 */

public class Xor extends GEPFunctionSymbol {

	/**
	 * The logical xor of 2 parameters.
	 */
	public Xor() 
	{
		super("xor", 2);
	}

	/**
	 * Evaluate the logical xor of 2 parameters.
	 * 
	 * @param params double array with the required parameter(s) -- 0 is treated as false, other values as true
	 * @return (params[0] ^ params[1])
	 */
	public double eval(double params[]) 
	{
		//should check that there are 2 params
		boolean p1 = params[0] == 0.0 ? false : true;
		boolean p2 = params[1] == 0.0 ? false : true;
		return (((p1 ^ p2) == true) ? 1.0 : 0.0);
	}
	
	/**
	 * Logical xor is a logical function.
	 * @return true
	 */
	public boolean isLogicalFunction()
	{
		return true;
	}

	/**
	 * The human readable form of the expression
	 */
	public String getMathExpressionAsString( String p[] )
	{
		return "(" + p[0] + " xor " + p[1] + ")";
	}
}
