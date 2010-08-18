package ec.gp.ge;

import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;
import ec.coevolve.*;
import ec.util.*;

public class GEProblem extends Problem implements SimpleProblemForm, GroupedProblemForm
    {
    public Problem problem;
        
    public void setup(EvolutionState state, Parameter base)
        {
        problem = (Problem)state.parameters.getInstanceForParameter(base.push("problem"), null, GPProblem.class);
        problem.setup(state, base);
        }
        
    public Object clone()
        {
        GEProblem other = (GEProblem)(super.clone());
        other.problem = (Problem)(problem.clone());
        return other;
        }
        
    public void prepareToEvaluate(final EvolutionState state, final int threadnum)
        {
        problem.prepareToEvaluate(state, threadnum);
        }

    public void finishEvaluating(final EvolutionState state, final int threadnum)
        {
        problem.finishEvaluating(state, threadnum);
        }

    public void initializeContacts( EvolutionState state )
        {
        problem.initializeContacts(state);
        }

    public void reinitializeContacts( EvolutionState state )
        {
        problem.reinitializeContacts(state);
        }
    
    public void closeContacts(EvolutionState state, int result)
        {
        problem.closeContacts(state, result);
        }
        
    public boolean canEvaluate()
        {
        return problem.canEvaluate();
        }

    public void preprocessPopulation(final EvolutionState state, Population pop, final boolean countVictoriesOnly)
        {
        if (!(problem instanceof GroupedProblemForm))
            state.output.fatal("GEProblem's underlying Problem is not a GroupedProblemForm");
        ((GroupedProblemForm)problem).preprocessPopulation(state, pop, countVictoriesOnly);
        }

    public void postprocessPopulation(final EvolutionState state, Population pop, final boolean countVictoriesOnly)
        {
        ((GroupedProblemForm)problem).preprocessPopulation(state, pop, countVictoriesOnly);
        }
        
    /** Default version assumes that every individual is a GEIndividual.
        The underlying problem.evaluate() must be prepared for the possibility that some
        GPIndividuals handed it are in fact null, meaning that they couldn't be extracted
        from the GEIndividual string.  You should assign them bad fitness in some appropriate way.
    */
    public void evaluate(final EvolutionState state,
        final Individual[] ind,  // the individuals to evaluate together
        final boolean[] updateFitness,  // should this individuals' fitness be updated?
        final boolean countVictoriesOnly, // don't bother updating Fitness with socres, just victories
        final int[] subpops,
        final int threadnum)
        {
        // the default version assumes that every subpopulation is a GE Individual
        GPIndividual[] gpi = new GPIndividual[ind.length];
        for(int i = 0; i < gpi.length; i++)
            {
            GEIndividual indiv = (GEIndividual) ind[i];
            GESpecies species = (GESpecies) (ind[i].species);
                
            // warning: gpi[i] may be null
            gpi[i] = species.map(state, indiv, threadnum);
            }
                        
        ((GroupedProblemForm)problem).evaluate(state, gpi, updateFitness, countVictoriesOnly, subpops, threadnum);
                        
        for(int i = 0; i < gpi.length; i++)
            {
            // Now we need to move the evaluated flag from the GPIndividual
            // to the GEIndividual, and also for good measure, let's copy over
            // the GPIndividual's fitness because even though the mapping function
            // set the two Individuals to share the same fitness, it's possible
            // that the evaluation function may have replaced the fitness.
            ind[i].fitness = gpi[i].fitness;
            ind[i].evaluated = gpi[i].evaluated;
            }
        }

    public void evaluate(final EvolutionState state,
        final Individual ind,
        final int subpopulation,
        final int threadnum)
        {
        if (!(problem instanceof SimpleProblemForm))
            state.output.fatal("GEProblem's underlying Problem is not a SimpleProblemForm");

        GEIndividual indiv = (GEIndividual) ind;
        GESpecies species = (GESpecies) (ind.species);
        GPIndividual gpi = species.map(state, indiv, threadnum);
        if (gpi == null)
            {
            KozaFitness fitness = (KozaFitness) (ind.fitness);
            fitness.setStandardizedFitness(state, Float.MAX_VALUE);
            } 
        else
            {
            ((SimpleProblemForm)problem).evaluate(state, gpi, subpopulation, threadnum);
            // Now we need to move the evaluated flag from the GPIndividual
            // to the GEIndividual, and also for good measure, let's copy over
            // the GPIndividual's fitness because even though the mapping function
            // set the two Individuals to share the same fitness, it's possible
            // that the evaluation function may have replaced the fitness.
            ind.fitness = gpi.fitness;
            ind.evaluated = gpi.evaluated;
            }
        }

    public void describe(final EvolutionState state,
        final Individual ind,
        final int subpopulation,
        final int threadnum,
        final int log)
        {
        GEIndividual indiv = (GEIndividual) ind;
        GESpecies species = (GESpecies) (ind.species);
        GPIndividual gpi = species.map(state, indiv, threadnum);
        if (gpi != null)
            {
            problem.describe(state, gpi, subpopulation, threadnum, log);

            // though this is probably not necessary for describe(...),
            // for good measure we're doing the same rigamarole that we
            // did for evaluate(...) above.
            ind.fitness = gpi.fitness;
            ind.evaluated = gpi.evaluated;
            }
        }
    }
