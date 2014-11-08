package ec.gp.lambda.app.churchNumerals.problems;

import java.io.Serializable;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPProblem;
import ec.gp.lambda.interpreter.Tomi;
import ec.util.Parameter;

public abstract class LambdaProblem extends GPProblem implements Serializable{
	private static final long serialVersionUID = 1;
	protected Tomi tomi;
    public final static String P_TOMI = "tomi";
    public final static String P_LEVEL_WEIGHT = "levelWeight";
    public final static String P_BOUND_FREE_WEIGHT = "boundFreeWeight";
	@Override
	public void setup(EvolutionState state, Parameter base) {
		super.setup(state, base);
		tomi = new Tomi();
		Parameter tomiParams = new Parameter(P_TOMI);
		tomi.setLevelWeight(state.parameters.getInt(tomiParams.push(P_LEVEL_WEIGHT), null));
		tomi.setBoundFreeWeight(state.parameters.getInt(tomiParams.push(P_BOUND_FREE_WEIGHT), null));
	}
}
