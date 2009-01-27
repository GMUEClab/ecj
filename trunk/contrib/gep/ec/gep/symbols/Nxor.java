package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The logical nxor of 2 parameters.
 */

public class Nxor extends GEPFunctionSymbol {

	/**
	 * The logical nxor of 2 parameters.
	 */
	public Nxor() 
	{
		super("nxor", 2);
	}

	/**
	 * Evaluate the logical nxor of 2 parameters.
	 * 
	 * @param params double array with the required parameter(s) -- 0 is treated as false, other values as true
	 * @return !(params[0] ^ params[1])
	 */
	public double eval(double params[]) 
	{
		//should check that there are 2 params
		boolean p1 = params[0] == 0.0 ? false : true;
		boolean p2 = params[1] == 0.0 ? false : true;
		return (((p1 ^ p2) == true) ? 0.0 : 1.0); // inverse of xor (if and only if function)
	}
	
	/**
	 * Logical nxor is a logical function.
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
		return "(" + p[0] + " nxor " + p[1] + ")";
	}
}
