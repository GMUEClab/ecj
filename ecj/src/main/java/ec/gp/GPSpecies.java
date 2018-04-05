/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp;
import ec.*;
import ec.util.*;
import java.io.*;

/* 
 * GPSpecies.java
 * 
 * Created: Tue Aug 31 17:00:10 1999
 * By: Sean Luke
 */

/**
 * GPSpecies is a simple individual which is suitable as a species
 * for GP subpopulations.  GPSpecies' individuals must be GPIndividuals,
 * and often their pipelines are GPBreedingPipelines (at any rate,
 * the pipelines will have to return members of GPSpecies!).
 *
 <p><b>Default Base</b><br>
 gp.species

 *
 * @author Sean Luke
 * @version 1.0 
 */

public class GPSpecies extends Species
    {
    public static final String P_GPSPECIES = "species";

    public Parameter defaultBase()
        {
        return GPDefaults.base().push(P_GPSPECIES);
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);

        // check to make sure that our individual prototype is a GPIndividual
        if (!(i_prototype instanceof GPIndividual))
            state.output.fatal("The Individual class for the Species " + getClass().getName() + " is must be a subclass of ec.gp.GPIndividual.", base );
        }    

    public Individual newIndividual(EvolutionState state, int thread) 
        {
        GPIndividual newind = ((GPIndividual)(i_prototype)).lightClone();
        
        // Initialize the trees
        for (int x=0;x<newind.trees.length;x++)
            newind.trees[x].buildTree(state, thread);

        // Set the fitness
        newind.fitness = (Fitness)(f_prototype.clone());
        newind.evaluated = false;

        // Set the species to me
        newind.species = this;
                
        // ...and we're ready!
        return newind;
        }


    // A custom version of newIndividual() which guarantees that the
    // prototype is light-cloned before readIndividual is issued
    public Individual newIndividual(final EvolutionState state,
        final LineNumberReader reader)
        throws IOException
        {
        GPIndividual newind = ((GPIndividual)i_prototype).lightClone();
                
        // Set the fitness -- must be done BEFORE loading!
        newind.fitness = (Fitness)(f_prototype.clone());
        newind.evaluated = false; // for sanity's sake, though it's a useless line

        // load that sucker
        newind.readIndividual(state,reader);

        // Set the species to me
        newind.species = this;

        // and we're ready!
        return newind;  
        }


    // A custom version of newIndividual() which guarantees that the
    // prototype is light-cloned before readIndividual is issued
    public Individual newIndividual(final EvolutionState state,
        final DataInput dataInput)
        throws IOException
        {
        GPIndividual newind = ((GPIndividual)i_prototype).lightClone();
        
        // Set the fitness -- must be done BEFORE loading!
        newind.fitness = (Fitness)(f_prototype.clone());
        newind.evaluated = false; // for sanity's sake, though it's a useless line

        // Set the species to me
        newind.species = this;

        // load that sucker
        newind.readIndividual(state,dataInput);

        // and we're ready!
        return newind;  
        }

    }
