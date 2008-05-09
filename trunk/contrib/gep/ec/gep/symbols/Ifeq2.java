package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public class Ifeq2 extends GEPFunctionSymbol {

	public Ifeq2() 
	{
		super("ifeq2", 2);
	}

	public double eval(double params[]) 
	{
		//should check that there are 2 params
		double p1 = params[0];
		double p2 = params[1];
		return (p1 == p2) ? p1 : p2;
	}
	
	public boolean isLogicalFunction()
	{
		return false;
	}

	public String printMathExpression( String p[] )
	{
		return "ifeq2(" + p[0] + ", " + p[1] + ")";
	}
}
