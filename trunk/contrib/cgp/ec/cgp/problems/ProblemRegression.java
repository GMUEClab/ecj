package ec.cgp.problems;

import ec.*;
import ec.simple.*;
import ec.util.*;
import ec.vector.*;
import ec.cgp.Evaluator;
import ec.cgp.FitnessCGP;
import ec.cgp.representation.VectorIndividualCGP;
import ec.cgp.representation.VectorSpeciesCGP;
import ec.multiobjective.*;

import java.util.*;

/**
 * 
 * Regression problem.
 * 
 * @author David Oranchak, doranchak@gmail.com, http://oranchak.com
 *
 */
public class ProblemRegression extends ProblemCGP {
	
	/** Which function to use.  Acceptable values: {1, 2, 3}. */
	public int function;
	
	static String P_WHICH = "which";
	
	/** 50 randomly generated test points used for fitness evaluation */
	static float[] testPoints = new float[] {0.14794326f,0.108998775f,0.4068538f,-0.47665644f,0.80171514f,0.095415235f,
		0.026154399f,-0.009217739f,0.029135108f,0.2079128f,0.7251047f,-0.23340857f,0.524932f,-0.7481402f,0.382663f,
		-0.7115128f,-0.027229786f,-0.099066496f,0.7357291f,0.33994365f,0.9047879f,-0.4072944f,-0.34821618f,-0.044303536f,
		-0.90075254f,0.30938172f,0.988202f,0.5843065f,-0.20018125f,0.73057616f,0.2547536f,0.018245697f,-0.44960117f,
		0.10484755f,-0.42382956f,-0.3190421f,0.78481805f,-0.4668939f,0.7419597f,0.9006864f,-0.9791528f,-0.59703183f,
		-0.4592893f,-0.9315028f,-0.073480844f,-0.28456664f,-0.69468606f,-0.119933486f,-0.31513882f,0.63493156f};

	/** Evaluate the CGP and compute fitness */
	public void evaluate(EvolutionState state, Individual ind,
			int subpopulation, int threadnum) {
		if (ind.evaluated)
			return;

		VectorSpeciesCGP s = (VectorSpeciesCGP) ind.species;
		VectorIndividualCGP ind2 = (VectorIndividualCGP) ind;
		
		float diff = 0f;
		
		Float[] inputs = new Float[2];
		float fn = 0f;
		
		for (int i=0; i<testPoints.length; i++) {
			inputs[0] = testPoints[i]; // one of the randomly-generated independent variables.
			inputs[1] = 1.0f; // a hard-coded fixed constant value

			/* run the CGP */
			Object[] outputs = Evaluator.evaluate(state, threadnum, inputs, ind2);
			
			/* compare to the real function value */
			if (function == 1) fn = function1(testPoints[i]);
			else if (function == 2) fn = function2(testPoints[i]);
			else if (function == 3) fn = function3(testPoints[i]);
			
			/* compute error */
			diff += Math.abs((Float)outputs[0] - fn);
		}
		((FitnessCGP)ind.fitness).setFitness(state, diff, diff <= 0.01); // stop if error is less than 1%.
		
		ind.evaluated = true;
	}

	/** Sixth-order polynomial. */
	public static float function1(float x) {
		return x*x*x*x*x*x-2*x*x*x*x+x*x;
	}
	
	/** Fifth-order polynomial. */
	public static float function2(float x) {
		return x*x*x*x*x-2*x*x*x+x;
	}
	
	/** Second-order polynomial. */
	public static float function3(float x) {
		return x*x+2*x+1;
	}
	
	public void setup(EvolutionState state, Parameter base) {
		super.setup(state, base);
		
		Parameter def = defaultBase();
        
        function = state.parameters.getInt(base.push(P_WHICH),def.push(P_WHICH),1);
        if (function == 0)
        	state.output.fatal("problem.which must be present and > 0.");
        state.output.exitIfErrors();
	}

}
