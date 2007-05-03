package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public class Pow10 extends GEPFunctionSymbol {

	public Pow10() 
	{
		super("pow10", 1);
	}

	public double eval(double params[]) 
	{
		//should check that there is exactly 1 paramater
		return (Math.pow(10.0, params[0]));
	}
	
	public boolean isLogicalFunction()
	{
		return false;
	}

	public String printMathExpression( String p[] )
	{
		return "pow(10," + p[0] + ")";
	}
}
