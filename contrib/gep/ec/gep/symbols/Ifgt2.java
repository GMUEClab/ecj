package ec.gep.symbols;

import ec.gep.GEPFunctionSymbol;

/**
 * If (a >= b) then c else d
 */

public class Ifgt2 extends GEPFunctionSymbol {

	/**
	 * If (x > y) then x else y
	 */
	public Ifgt2() 
	{
		super("ifgt2", 2);
	}

	/**
	 * Evaluate Ifgt2 with the 2 parameters x,y such that
	 * If (x > y) then x else y.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return Ifgt2(params[0], params[1])
	 */
	public double eval(double params[]) 
	{
		//should check that there are 2 params
		double p1 = params[0];
		double p2 = params[1];
		return (p1 > p2) ? p1 : p2;
	}
	
	/**
	 * Ifgt2 is not a logical function.
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
		return "ifgt2(" + p[0] + ", " + p[1] + ")";
	}
}
