/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

/*
  A simple class which contains both an Individual and the Queue it's located in.
  Used by SteadyState and by various assistant functions in the distributed evaluator
  to provide individuals to SteadyState
*/
                
package ec.steadystate;
import ec.*;

public class QueueIndividual implements java.io.Serializable
    { 
    public Individual ind;
    public int subpop; 
    public  QueueIndividual(Individual i, int s)
        {
        ind = i; 
        subpop=s; 
        }
    };

