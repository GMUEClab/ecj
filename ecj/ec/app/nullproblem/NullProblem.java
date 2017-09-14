/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.app.nullproblem;

import ec.EvolutionState;
import ec.Fitness;
import ec.Individual;
import ec.Problem;
import ec.simple.SimpleFitness;
import ec.simple.SimpleProblemForm;
import ec.vector.DoubleVectorIndividual;
import ec.vector.VectorIndividual;

/**
 *
 * @author dfreelan Temporary class intended to be used to measure ECJ overhead
 *         doing non-evaluation tasks (statistics and such)
 */
public class NullProblem extends Problem implements SimpleProblemForm
    {

    @Override
    public void evaluate(EvolutionState state, Individual ind, int subpopulation, int threadnum)
        {
        double fit = 10.0 - (((DoubleVectorIndividual) ind).genome[0] * ((DoubleVectorIndividual) ind).genome[0]);

        ((SimpleFitness) ind.fitness).setFitness(state, fit, false);

        }

    }
