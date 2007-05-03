package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public class Acot extends GEPFunctionSymbol {

	public Acot() 
	{
		super("acot", 1);
	}

	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return ((Math.PI/2.0)-Math.atan(params[0]));
	}
	
	public boolean isLogicalFunction()
	{
		return false;
	}

	public String printMathExpression( String p[] )
	{
		return "Acot("+p[0]+")";
	}
}
