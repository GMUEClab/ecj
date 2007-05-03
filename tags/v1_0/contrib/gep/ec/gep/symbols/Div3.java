package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public class Div3 extends GEPFunctionSymbol {

	public Div3() 
	{
		super("div3", 3);
	}

	public double eval(double params[]) 
	{
		//should check that there are 3 params
		return (params[0] / params[1] / params[2]);
	}
	
	public boolean isLogicalFunction()
	{
		return false;
	}

	public String printMathExpression( String p[] )
	{
		return "("+p[0] + "/" + p[1] + "/" + p[2] + ")";
	}
}
