package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The complement value of a single parameter (1-value) Only makes sense if value is 0 or 1..
 */

public class Comp extends GEPFunctionSymbol {

	/**
	 * The complement value of a single parameter (1-value) Only makes sense if value is 0 or 1..
	 */
	public Comp() 
	{
		super("comp", 1);
	}

	/**
	 * Evaluate the complement of a single parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return 1-params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (1-params[0]);
	}
	
	/**
	 * Complement is not a logical function. Should it be???
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
		return "(1-"+p[0]+")";
	}
}
