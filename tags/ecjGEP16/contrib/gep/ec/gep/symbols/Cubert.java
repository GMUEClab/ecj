package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

/**
 * The cube root value of a single parameter.
 */

public class Cubert extends GEPFunctionSymbol {

	/**
	 * The cube root value of a single parameter.
	 */
	public Cubert() 
	{
		super("3RT", 1);
	}

	/**
	 * Evaluate the cube root of a single parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return cubeRoot(params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (Math.sqrt(params[0]));
	}
	
	/**
	 * Cube root is not a logical function.
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
		return "(" + p[0] + "^(1.0/3.0))";
	}
}
