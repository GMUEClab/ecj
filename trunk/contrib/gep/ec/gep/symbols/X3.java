package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public class X3 extends GEPFunctionSymbol {

	public X3() 
	{
		super("X3", 1);
	}

	public double eval(double params[]) 
	{
		//should check that there is exactly 1 paramater
		return (params[0] * params[0] * params[0]);
	}
	
	public boolean isLogicalFunction()
	{
		return false;
	}

	public String printMathExpression( String p[] )
	{
		return "("+p[0] + "^3)";
	}
}
