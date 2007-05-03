package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public class Comp extends GEPFunctionSymbol {

	public Comp() 
	{
		super("comp", 1);
	}

	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (1-params[0]);
	}
	
	public boolean isLogicalFunction()
	{
		return false;
	}

	public String printMathExpression( String p[] )
	{
		return "(1-"+p[0]+")";
	}
}
