package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public class Coth extends GEPFunctionSymbol {

	public Coth() 
	{
		super("coth", 1);
	}

	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return ((Math.pow(Math.E, 2.0*params[0])+1)/(Math.pow(Math.E, 2.0*params[0])-1));
	}
	
	public boolean isLogicalFunction()
	{
		return false;
	}

	public String printMathExpression( String p[] )
	{
		return "coth("+p[0]+")";
	}
}
