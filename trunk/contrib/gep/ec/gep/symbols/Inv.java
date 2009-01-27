package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The value the inverse of a single parameter  (1/x).
 */

public class Inv extends GEPFunctionSymbol {

	/**
	 * The value the inverse of a single parameter  (1/x).
	 */
	public Inv() 
	{
		super("inv", 1);
	}

	/**
	 * Evaluate the inverse of a single parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return 1/params[0]
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (1.0/params[0]);
	}
	
	/**
	 * Inv is not a logical function.
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
		return "(1/"+p[0]+")";
	}
}
