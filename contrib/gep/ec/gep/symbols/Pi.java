package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The value pi (3.14159...)
 */

public class Pi extends GEPFunctionSymbol {

	/**
	 * The value pi (3.14159...)
	 */
	public Pi() 
	{
		super("pi", 0);
	}

	/**
	 * Evaluate the number pi (3.14159...).
	 * 
	 * @param params double array with the required parameter(s)
	 * @return (pi)
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater? But it should be 0!
		// This is oddity of GeneXPro ... 1 arg rather than 0!
		return (Math.PI);
	}
	
	/**
	 * pi is not a logical function.
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
		return "pi";
	}
}
