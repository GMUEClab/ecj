package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public class Gau4 extends GEPFunctionSymbol {

	public Gau4() 
	{
		super("gau4", 4);
	}

	public double eval(double params[]) 
	{
		//should check that there are exactly 4 paramaters
		return (Math.exp(-Math.pow(params[0]+params[1]+params[2]+params[3], 2.0)));
	}
	
	public boolean isLogicalFunction()
	{
		return false;
	}

	public String printMathExpression( String p[] )
	{
		return "exp(-pow(" + p[0] + "+" + p[1]+ "+" + p[2] + "+" + p[3] +  ",2))";
	}
}
