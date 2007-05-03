package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public class Mod extends GEPFunctionSymbol {

	public Mod() 
	{
		super("mod", 1);
	}

	public double eval(double params[]) 
	{
		//should check that there are exactly 2 paramaters
		return (Math.IEEEremainder(params[0], params[1]));
	}
	
	public boolean isLogicalFunction()
	{
		return false;
	}

	public String printMathExpression( String p[] )
	{
		return "mod(" + p[0] + "," + p[1] + ")";
	}
}
