package ec.cgp;

import ec.Fitness;
import ec.simple.SimpleFitness;
/**
 * Extension of SimpleFitness that makes smaller fitnesses better.
 * 
 * @author David Oranchak, doranchak@gmail.com, http://oranchak.com
 *
 */
public class FitnessCGP extends SimpleFitness {

	public boolean betterThan(Fitness _fitness) {
        return ((SimpleFitness)_fitness).fitness() > fitness();
	}

}
