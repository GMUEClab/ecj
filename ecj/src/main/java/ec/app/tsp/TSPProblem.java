/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.app.tsp;

import ec.EvolutionState;
import ec.Problem;
import ec.co.ConstructiveProblemForm;
import ec.util.Parameter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * https://www.iwr.uni-heidelberg.de/groups/comopt/software/TSPLIB95/tsp95.pdf
 * 
 * @author Eric O. Scott
 */
public class TSPProblem extends Problem implements ConstructiveProblemForm {
    public final static String P_FILE = "file";
    
    // TODO Create field to store weight matrix.
    
    @Override
    public void setup(EvolutionState state, Parameter base)
    {
        assert(state != null);
        assert(base != null);
        final File file = state.parameters.getFile(base.push(P_FILE), null);
        if (file == null)
            state.output.fatal(String.format("%s: Unable to read file path '%s'.", this.getClass().getSimpleName(), base.push(P_FILE)), base.push(P_FILE));
        /*try
            {
            weights = weightsFromFile(file);
            }
        catch (final IOException e)
            {
            state.output.fatal(String.format("%s: Unable to load TSP instance from file '%s'.", this.getClass().getSimpleName(), base.push(P_FILE)), base.push(P_FILE));
            }
        assert(repOK());*/
    }
        
    private static double[][] weightsFromFile(final File file) throws IOException
    {
        assert(file != null);
        final BufferedReader r = new BufferedReader(new FileReader(file));
        skipToData(r);
        String line;
        while ( (line = r.readLine()) != null)
            {
            final String[] cols = line.split(" ");
            // TODO Add data to weight matrix
            }
        throw new UnsupportedOperationException();
    }
    
    /** Seek to the line where the coordinate data begins in a TSP problem definition of TSPLIB format. */
    private static void skipToData(final BufferedReader tspReader) throws IOException
    {
        assert(tspReader != null);
        boolean done = false;
        String line;
        while ( (line = tspReader.readLine()) != null && !done)
            {
            if (line.trim().equals("NODE_COORD_SECTION"))
                return;
            }
        throw new IllegalStateException("No 'NODE_COORD_SECTION' found in TSP file.  Are you sure this file is in TSPLIB format?");
    }

    @Override
    public double desireability(int from, int to)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int numComponents()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
