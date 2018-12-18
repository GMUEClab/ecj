/*
  Copyright 2013 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.app.mona; 

import ec.vector.*;
import ec.*;

public class MonaVectorIndividual extends DoubleVectorIndividual
    {      
    public void reset(EvolutionState state, int thread)
        {
        super.reset(state, thread);

        int numVertices = ((Mona)(state.evaluator.p_problem)).numVertices;
        int vertexSkip = numVertices * 2 + 4;  // for four colors
                
        for (int x = 3; x < genome.length; x+=vertexSkip)
            // Alsing originally just set all his colors to 0 alpha.
            // Here I divide the alpha by 10 so they're initially very
            // transparent
            genome[x] /= 10;            
        }
    }
