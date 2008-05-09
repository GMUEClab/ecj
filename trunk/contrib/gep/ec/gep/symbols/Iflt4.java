package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public class Iflt4 extends GEPFunctionSymbol {

	public Iflt4() 
	{
		super("iflt4", 4);
	}

	public double eval(double params[]) 
	{
		//should check that there are 4 params
		double p1 = params[0];
		double p2 = params[1];
		double p3 = params[2];
		double p4 = params[3];
		return (p1 < p2) ? p3 : p4;
	}
	
	public boolean isLogicalFunction()
	{
		return false;
	}

	public String printMathExpression( String p[] )
	{
		return "iflt4(" + p[0] + ", " + p[1] + ", " + p[2] + ", " + p[3] + ")";
	}
}
