package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

/**
 * The average of 3 parameters.
 */

public class Avg3 extends GEPFunctionSymbol {

	/**
	 * The average of 3 parameters.
	 */
	public Avg3() 
	{
		super("avg3", 3);
	}

	/**
	 * Evaluate the average of 3 parameters.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return average(params[0],params[1], params[2])
	 */
	public double eval(double params[]) 
	{
		//should check that there are 3 params
		return ((params[0] + params[1] + params[2])/3.0);
	}
	
	/**
	 * Average is not a logical function.
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
		return "((" + p[0] + "+" + p[1] + "+" + p[2] + ")/3)";
	}
}
