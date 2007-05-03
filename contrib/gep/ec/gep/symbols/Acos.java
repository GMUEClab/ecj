package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public class Acos extends GEPFunctionSymbol {

	public Acos() 
	{
		super("acos", 1);
	}

	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (Math.acos(params[0]));
	}
	
	public boolean isLogicalFunction()
	{
		return false;
	}
	
	public String printMathExpression( String p[] )
	{
		return "acos("+p[0]+")";
	}
}
