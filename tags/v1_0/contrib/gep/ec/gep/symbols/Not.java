package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public class Not extends GEPFunctionSymbol {

	public Not() 
	{
		super("not", 1);
	}

	public double eval(double params[]) 
	{
		//should check that there is 1 param
		return ((params[0] == 0.0) ? 1.0 : 0.0);
	}
	
	public boolean isLogicalFunction()
	{
		return true;
	}

	public String printMathExpression( String p[] )
	{
		return "(not " + p[0] + ")";
	}
}
