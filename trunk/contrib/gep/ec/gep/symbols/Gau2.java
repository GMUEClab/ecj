package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public class Gau2 extends GEPFunctionSymbol {

	public Gau2() 
	{
		super("gau2", 2);
	}

	public double eval(double params[]) 
	{
		//should check that there are exactly 2 paramaters
		return (Math.exp(-Math.pow(params[0]+params[1], 2.0)));
	}
	
	public boolean isLogicalFunction()
	{
		return false;
	}

	public String printMathExpression( String p[] )
	{
		return "exp(-pow(" + p[0] + "+" + p[1] +  ",2))";
	}
}
