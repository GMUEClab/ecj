package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * The logistic function (1.0/(1.0+Math.pow(Math.E, -params[0]))).
 */

public class Logistic extends GEPFunctionSymbol {

	/**
	 * The logistic function (1.0/(1.0+Math.pow(Math.E, -params[0]))).
	 */
	public Logistic() 
	{
		super("logi", 1);
	}

	/**
	 * Evaluate the logistic function.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return (1.0/(1.0+Math.pow(Math.E, -params[0])))
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (1.0/(1.0+Math.pow(Math.E, -params[0])));
	}
	
	/**
	 * Logistic is not a logical function.
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
		return "(1/(1+pow(e,-"+p[0]+")))";
	}
}
