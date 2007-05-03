package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public class Gau3 extends GEPFunctionSymbol {

	public Gau3() 
	{
		super("gau3", 3);
	}

	public double eval(double params[]) 
	{
		//should check that there are exactly 3 paramaters
		return (Math.exp(-Math.pow(params[0]+params[1]+params[2], 2.0)));
	}
	
	public boolean isLogicalFunction()
	{
		return false;
	}

	public String printMathExpression( String p[] )
	{
		return "exp(-pow(" + p[0] + "+" + p[1]+ "+" + p[2] +  ",2))";
	}
}
