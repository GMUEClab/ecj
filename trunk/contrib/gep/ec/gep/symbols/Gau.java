package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public class Gau extends GEPFunctionSymbol {

	public Gau() 
	{
		super("gau", 1);
	}

	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (Math.exp(-Math.pow(params[0], 2.0)));
	}
	
	public boolean isLogicalFunction()
	{
		return false;
	}

	public String printMathExpression( String p[] )
	{
		return "exp(-pow(" + p[0] + ",2))";
	}
}
