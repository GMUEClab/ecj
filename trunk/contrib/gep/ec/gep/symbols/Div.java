package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public class Div extends GEPFunctionSymbol {

	public Div() 
	{
		super("/", 2);
	}

	public double eval(double params[]) 
	{
		//should check that there are 2 params
		// and need to handle a 0 denominator ... see what they do in GP
		return (params[0] / params[1]);
	}
	
	public boolean isLogicalFunction()
	{
		return false;
	}

	public String printMathExpression( String p[] )
	{
		return "("+p[0] + "/" + p[1] + ")";
	}
}
