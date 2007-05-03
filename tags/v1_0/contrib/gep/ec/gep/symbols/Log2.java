package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public class Log2 extends GEPFunctionSymbol {

	public Log2() 
	{
		super("log2", 2);
	}

	public double eval(double params[]) 
	{
		//should check that there are exactly 2 paramaters
		// param 0 is the value, param 1 is the base
		return (Math.log(params[0])/Math.log(params[1]));
	}
	
	public boolean isLogicalFunction()
	{
		return false;
	}

	public String printMathExpression( String p[] )
	{
		return "log2("+p[0]+")";
	}
}
