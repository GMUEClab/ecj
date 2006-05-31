/*
Copyright 2006 by Sean Luke and George Mason University
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/


package ec.rule;
import ec.*;
import ec.util.*;
import java.io.*;

/* 
 * RuleSpecies.java
 * 
 * Created: Wed Feb 31 17:42:10 2001
 * By: Liviu Panait
 */

/**
 * RuleSpecies is a simple individual which is suitable as a species
 * for rule sets subpopulations.  RuleSpecies' individuals must be RuleIndividuals,
 * and often their pipelines are RuleBreedingPipelines (at any rate,
 * the pipelines will have to return members of RuleSpecies!).
 *
 <p><b>Default Base</b><br>
 rule.species

 *
 * @author Liviu Panait
 * @version 1.0 
 */

public class RuleSpecies extends Species
    {
    public static final String P_RULESPECIES = "species";

    public Parameter defaultBase()
        {
        return RuleDefaults.base().push(P_RULESPECIES);
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);

        // check to make sure that our individual prototype is a RuleIndividual
        if (!(i_prototype instanceof RuleIndividual))
            state.output.fatal("The Individual class for the Species " + getClass().getName() + " is must be a subclass of ec.rule.RuleIndividual.", base );
        }    

    public Individual newIndividual(EvolutionState state,
									Subpopulation _population,
									Fitness _fitness) 
        {
        RuleIndividual newind = (RuleIndividual)(i_prototype.clone());
        
        newind.reset(state,0);  // unthreaded at this point...

        // Set the fitness
        newind.fitness = _fitness;
        newind.evaluated = false;

        // Set the species to me
        newind.species = this;

        // ...and we're ready!
        return newind;
        }

/*
    public Individual newIndividual(final EvolutionState state,
                                    final Subpopulation _population,
                                    final Fitness _fitness,
                                    final LineNumberReader reader)
        throws IOException
        {
        RuleIndividual newind = (RuleIndividual)(i_prototype.protoClone());
        
        // Set the fitness -- must be done BEFORE loading!
        newind.fitness = _fitness;
        newind.evaluated = false; // for sanity's sake, though it's a useless line

        // Set the species to me
        newind.species = this;

        // load that sucker
        newind.readIndividual(state,reader);

        // and we're ready!
        return newind;  
        }

    public Individual newIndividual(final EvolutionState state,
                                    final Subpopulation _population,
                                    final Fitness _fitness,
                                    final DataInput dataInput)
        throws IOException
        {
        RuleIndividual newind = (RuleIndividual)(i_prototype.protoClone());
        
        // Set the fitness -- must be done BEFORE loading!
        newind.fitness = _fitness;
        newind.evaluated = false; // for sanity's sake, though it's a useless line

        // Set the species to me
        newind.species = this;

        // load that sucker
        newind.readGenotype(state,dataInput);

        // and we're ready!
        return newind;  
        }

*/
    }
