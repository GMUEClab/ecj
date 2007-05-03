package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public class X4 extends GEPFunctionSymbol {

	public X4() 
	{
		super("X4", 1);
	}

	public double eval(double params[]) 
	{
		//should check that there is exactly 1 paramater
		double tmp = params[0] * params[0];
		return (tmp * tmp);
	}
	
	public boolean isLogicalFunction()
	{
		return false;
	}

	public String printMathExpression( String p[] )
	{
		return "("+p[0] + "^4)";
	}
}
