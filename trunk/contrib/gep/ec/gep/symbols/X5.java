package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public class X5 extends GEPFunctionSymbol {

	public X5() 
	{
		super("X5", 1);
	}

	public double eval(double params[]) 
	{
		//should check that there is exactly 1 paramater
		double tmp = params[0] * params[0];
		return (tmp * tmp * params[0]);
	}
	
	public boolean isLogicalFunction()
	{
		return false;
	}

	public String printMathExpression( String p[] )
	{
		return "(" + p[0] + "^5)";
	}
}
