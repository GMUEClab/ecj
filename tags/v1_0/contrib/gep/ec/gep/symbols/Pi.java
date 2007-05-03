package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public class Pi extends GEPFunctionSymbol {

	public Pi() 
	{
		super("pi", 1);
	}

	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		// This is oddity of GeneXPro ... 1 arg rather than 0!
		return (Math.PI);
	}
	
	public boolean isLogicalFunction()
	{
		return false;
	}

	public String printMathExpression( String p[] )
	{
		return "pi";
	}
}
