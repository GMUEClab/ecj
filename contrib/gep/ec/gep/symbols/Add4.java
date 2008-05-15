package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

/**
 * The addition of 4 parameters.
 */

public class Add4 extends GEPFunctionSymbol {

	/**
	 * The addition of 4 parameters.
	 */
	public Add4() 
	{
		super("add4", 4);
	}

	/**
	 * Evaluate the addition of 4 parameters.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return (params[0] + params[1] + params[2] + params[3])
	 */
	public double eval(double params[]) 
	{
		//should check that there are 4 params
		return (params[0] + params[1] + params[2] + params[3]);
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
	public String printMathExpression( String p[] )
	{
		return "(" + p[0] + "+" + p[1] + "+" + p[2] +  "+" + p[3] + ")";
	}
}
