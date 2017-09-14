package ec.app.tutorial3copy;
import ec.*;
import ec.util.*;
import java.io.*;
import ec.vector.*;

public class MyStatistics extends Statistics
    {
    public static final String P_POPFILE = "pop-file";
    public int popLog;

    public static final String P_INFOFILE = "info-file";
    public int infoLog;

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state, base);

        File popFile = state.parameters.getFile(base.push(P_POPFILE), null);

        if (popFile!=null) try
                               {
                               popLog = state.output.addLog(popFile,true);
                               }
            catch(IOException i)
                {
                state.output.fatal("An IOException occurred while trying to create the log " + popFile + ":\n" + i);
                }

        File infoFile = state.parameters.getFile(base.push(P_INFOFILE), null);
        if (infoFile!= null) try
                                 {
                                 infoLog = state.output.addLog(infoFile, true);
                                 }
            catch(IOException i)
                {
                state.output.fatal("An IOException occurred while trying to create the log " + infoFile + ":\n" + i);
                }
        }
        
    public void postEvaluationStatistics(final EvolutionState state)
        {
        super.postEvaluationStatistics(state);
        state.output.println("-------------------------\nGENERTION " + state.generation + "\n------------------------", popLog);
        state.population.printPopulation(state, popLog);

        int best = 0;
        double best_val = ((DoubleVectorIndividual) state.population.subpops.get(0).individuals.get(0)).genome[3];
        for(int y = 1; y< state.population.subpops.get(0).individuals.size(); y++)
            {
            double val = ((DoubleVectorIndividual) state.population.subpops.get(0).individuals.get(y)).genome[3];
            if (val > best_val)
                {
                best = y;
                best_val = val;
                }
            }
        state.population.subpops.get(0).individuals.get(best).printIndividualForHumans(state,infoLog);
        }
    }
