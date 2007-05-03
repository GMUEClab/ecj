package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public class Max2 extends GEPFunctionSymbol {

	public Max2() 
	{
		super("max2", 2);
	}

	public double eval(double params[]) 
	{
		//should check that there are 2 params
		return (Math.max(params[0], params[1]));
	}
	
	public boolean isLogicalFunction()
	{
		return false;
	}

	public String printMathExpression( String p[] )
	{
		return "max(" + p[0] + "," + p[1] + ")";
	}
}
