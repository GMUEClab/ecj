package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public class Avg4 extends GEPFunctionSymbol {

	public Avg4() 
	{
		super("avg4", 4);
	}

	public double eval(double params[]) 
	{
		//should check that there are 4 params
		return ((params[0] + params[1] + params[2] + params[3])/4.0);
	}
	
	public boolean isLogicalFunction()
	{
		return false;
	}

	public String printMathExpression( String p[] )
	{
		return "((" + p[0] + "+" + p[1] + "+" + p[2] + "+" + p[3] + ")/4)";
	}
}
