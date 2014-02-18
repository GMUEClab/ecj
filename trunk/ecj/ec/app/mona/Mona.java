/*
  Copyright 2013 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.app.mona; 

import ec.*; 
import ec.util.*; 
import ec.vector.*;
import ec.simple.*;
import java.io.*;

public class Mona extends Problem implements SimpleProblemForm 
    {
    public static final String P_IN = "in";
    public static final String P_OUT = "out";
    public static final String P_VERTICES = "num-vertices";
    
    public Picture pic = new Picture();
    public File in;
    public File out;
    public int numVertices;
    
    public Object clone()
        {
        Mona m = (Mona)(super.clone());
        m.pic = (Picture)(pic.clone());
        return m;
        }
        
    public void setup(EvolutionState state, Parameter base) 
        {
        super.setup(state, base); 
        in = state.parameters.getFile(base.push(P_IN), null);
        out = state.parameters.getFile(base.push(P_OUT), null);
        numVertices = state.parameters.getInt(base.push(P_VERTICES), null, 3);
        if (numVertices < 3) state.output.fatal("Number of vertices must be >= 3");
        pic.load(in);
        }
        
    public void evaluate(final EvolutionState state, final Individual ind, final int subpopulation, final int threadnum)
        {
        if (ind.evaluated) return;
        
        DoubleVectorIndividual _ind = (DoubleVectorIndividual)ind;
        int vertexSkip = numVertices * 2 + 4;  // for four colors
        
        pic.clear();
        for(int i = 0; i < _ind.genome.length; i+=vertexSkip)
            pic.addPolygon(_ind.genome, i, numVertices);

        double error = pic.error();
        ((SimpleFitness)(_ind.fitness)).setFitness(state, (1.0 - error), error == 0);
        ind.evaluated = true;
        }
        
    public void finishEvaluating(final EvolutionState state, final int threadnum)
        {
        pic.disposeGraphics();  // dutifully
        }

    public void describe(
        final EvolutionState state, 
        final Individual ind, 
        final int threadnum,
        final int subpopulation,
        final int log)  
        {
        ind.evaluated = false;
        evaluate(state, ind, subpopulation,threadnum);
        pic.save(new File(out.getParentFile(), "" + (state.generation) + "-" + out.getName()));  // not sure if "." is acceptable in Windows
        pic.display("Best So Far, Generation " + state.generation);
        // System.out.println("Filled Polygons: " + pic.getLatestFilledPolygonCount() + " of " + pic.getLatestTotalCount());
        ind.evaluated = true;
        }
    }
