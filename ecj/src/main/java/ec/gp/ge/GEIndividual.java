/*
  Copyright 2010 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp.ge;

import ec.*;
import ec.gp.*;
import ec.vector.*;
import ec.util.*;
import java.util.*;

/*
 * GEIndividual.java
 *
 * Created: Sat Oct 16 23:21:01 EDT 2010
 * By: Joseph Zelibor, Eric Kangas, and Sean Luke
 */

/**
   GEIndividual is a simple subclass of IntegerVectorIndividual which not only prints out (for humans)
   the Individual as a int vector but also prints out the Individual's tree representation.
*/

public class GEIndividual extends IntegerVectorIndividual
    {
    public static final String GP_PREAMBLE = "Equivalent GP Individual:";
    public static final String ERC_PREAMBLE = "ERCs: ";
    public static final String BAD_TREE = "[BAD]";

    public void printIndividualForHumans(EvolutionState state, int log)
        {
        super.printIndividualForHumans(state, log);

        HashMap ERCmapsForFancyPrint = new HashMap();

        // print out Trees
        state.output.println(GP_PREAMBLE, log);
        GPIndividual ind = (((GESpecies)species).map(state, this, 0, ERCmapsForFancyPrint));
        if (ind == null) state.output.println(BAD_TREE, log);
        else ind.printTrees(state, log);

        // print out ERC mapping
        state.output.print(ERC_PREAMBLE, log);
        Iterator iter = (ERCmapsForFancyPrint.keySet()).iterator();
        while(iter.hasNext())
            {
            Integer key = (Integer)(iter.next());
            GPNode val = (GPNode)(ERCmapsForFancyPrint.get(key));
            state.output.print("    " + (byte)(key.intValue()) + " -> " + val.toStringForHumans(), log);
            }
        state.output.println("", log);
        }
    }
