package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public class Avg2 extends GEPFunctionSymbol {

	public Avg2() 
	{
		super("avg2", 2);
	}

	public double eval(double params[]) 
	{
		//should check that there are 2 params
		return ((params[0]+ params[1])/2.0);
	}
	
	public boolean isLogicalFunction()
	{
		return false;
	}

	public String printMathExpression( String p[] )
	{
		return "((" + p[0] + "+" + p[1] + ")/2)";
	}
}
