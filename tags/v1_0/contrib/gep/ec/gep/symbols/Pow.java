package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public class Pow extends GEPFunctionSymbol {

	public Pow() 
	{
		super("pow", 2);
	}

	public double eval(double params[]) 
	{
		//should check that there are exactly 2 paramaters
		return (Math.pow(params[0], params[1]));
	}
	
	public boolean isLogicalFunction()
	{
		return false;
	}

	public String printMathExpression( String p[] )
	{
		return "pow(" + p[0] + "," + p[1] + ")";
	}
}
