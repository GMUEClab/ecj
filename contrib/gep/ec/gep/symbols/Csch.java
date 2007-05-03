package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public class Csch extends GEPFunctionSymbol {

	public Csch() 
	{
		super("csc", 1);
	}

	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (1.0/Math.sinh(params[0]));
	}
	
	public boolean isLogicalFunction()
	{
		return false;
	}

	public String printMathExpression( String p[] )
	{
		return "csh("+p[0]+")";
	}
}
