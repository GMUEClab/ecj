package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The logical not of 1 parameter.
 */

public class Not extends GEPFunctionSymbol {

	/**
	 * The logical not of 1 parameter.
	 */
	public Not() 
	{
		super("not", 1);
	}

	/**
	 * Evaluate the logical not of 1 parameter.
	 * 
	 * @param params double array with the required parameter(s) -- 0 is treated as false, other values as true
	 * @return !(params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is 1 param
		return ((params[0] == 0.0) ? 1.0 : 0.0);
	}
	
	/**
	 * Logical not is  a logical function.
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
		return "(not " + p[0] + ")";
	}
}
