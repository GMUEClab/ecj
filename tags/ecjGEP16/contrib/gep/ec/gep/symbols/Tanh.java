package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

/**
 * The hyperbolic tangent of a single parameter (radians).
 */

public class Tanh extends GEPFunctionSymbol {

	/**
	 * The hyperbolic tangent of a single parameter (radians).
	 */
	public Tanh() 
	{
		super("tanh", 1);
	}

	/**
	 * Evaluate the hyperbolic tangent of a single parameter.
	 * 
	 * @param params double array with the required parameter(s)
	 * @return tanh(params[0])
	 */
	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (Math.tanh(params[0]));
	}
	
	/**
	 * Hyperbolic Tangent is not a logical function.
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
		return "tanh(" + p[0] + ")";
	}
}
