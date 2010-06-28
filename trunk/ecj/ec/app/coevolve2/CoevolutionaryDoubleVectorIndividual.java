package ec.app.coevolve2;

import ec.*;
import ec.coevolve.*;
import ec.vector.DoubleVectorIndividual;
import ec.simple.SimpleFitness;

/** This class exists solely to print out the DoubleVectorIndividual and its collaborators in 
    a nice way for statistics purposes. */
        
public class CoevolutionaryDoubleVectorIndividual extends DoubleVectorIndividual
    {
    public CoevolutionaryDoubleVectorIndividual[] context;
    boolean dontPrintContext = false;

    public void printIndividualForHumans(EvolutionState state, int log)
        {
        super.printIndividualForHumans(state, log);
        if (!dontPrintContext && context != null)
            {
            for(int i = 0; i < context.length; i++)
                if (context[i] != null)
                    {
                    state.output.println("--Collaborator " + i + ":", log);
                    // this is a hack but it should be fine because printing
                    // individuals for humans is essentially always single-threaded
                    context[i].dontPrintContext = true;
                    context[i].printIndividualForHumans(state, log);
                    context[i].dontPrintContext = false;
                    }
            }
        }
    }