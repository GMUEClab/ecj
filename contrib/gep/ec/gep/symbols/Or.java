package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public class Or extends GEPFunctionSymbol {

	public Or() 
	{
		super("or", 2);
	}

	public double eval(double params[]) 
	{
		//should check that there are 2 params
		boolean p1 = params[0] == 0.0 ? false : true;
		boolean p2 = params[1] == 0.0 ? false : true;
		return (((p1 || p2) == true) ? 1.0 : 0.0);
	}
	
	public boolean isLogicalFunction()
	{
		return true;
	}

	public String printMathExpression( String p[] )
	{
		return "(" + p[0] + " or " + p[1] + ")";
	}
}
