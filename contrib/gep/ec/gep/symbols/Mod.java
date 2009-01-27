package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The modulo (remainder) function applied to 2 parameters. Actually we
 * use the IEEEremainder function for this.
 * 
 */

public class Mod extends GEPFunctionSymbol {

	/**
	 * The modulo (remainder) function applied to 2 parameters. Actually we
	 * use the IEEEremainder function for this.
	 * 
	 */
	public Mod() 
	{
		super("mod", 2);
	}

	/**
	 * Evaluate the modulo (remainder) function given 2 parameters (e.g. mod(8, 3) = 2).
	 * We use the IEEEremainder function for this.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return mod(params[0], params[1])
	 */
	public double eval(double params[]) 
	{
		//should check that there are exactly 2 parameters
		return (Math.IEEEremainder(params[0], params[1]));
	}
	
	/**
	 * Mod and is NOT a logical function.
	 * @return true
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
		return "mod(" + p[0] + "," + p[1] + ")";
	}
}
