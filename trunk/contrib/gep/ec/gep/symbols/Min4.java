package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public class Min4 extends GEPFunctionSymbol {

	public Min4() 
	{
		super("min4", 4);
	}

	public double eval(double params[]) 
	{
		//should check that there are 4 params
		return (Math.min(Math.min(params[0], params[1]), Math.min(params[2], params[3])));
	}
	
	public boolean isLogicalFunction()
	{
		return false;
	}

	public String printMathExpression( String p[] )
	{
		return "min4(" + p[0] + "," + p[1] + "," + p[2] + "," + p[3] + ")";
	}
}
