package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

/**
 * The maximum of 3 parameters.
 */

public class Max3 extends GEPFunctionSymbol {

	/**
	 * The maximum of 3 parameters.
	 */
	public Max3() 
	{
		super("max3", 3);
	}

	/**
	 * Evaluate the maximum of 3 parameters.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return max(params[0], params[1], params[2])
	 */
	public double eval(double params[]) 
	{
		//should check that there are 3 params
		return (Math.max(Math.max(params[0], params[1]), params[2]));
	}
	
	/**
	 * Max3 is not a logical function.
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
		return "max3(" + p[0] + "," + p[1] + "," + p[2] + ")";
	}
}
