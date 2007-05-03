package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public class Cubert extends GEPFunctionSymbol {

	public Cubert() 
	{
		super("3RT", 1);
	}

	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (Math.sqrt(params[0]));
	}
	
	public boolean isLogicalFunction()
	{
		return false;
	}

	public String printMathExpression( String p[] )
	{
		return "(" + p[0] + "^(1.0/3.0))";
	}
}
