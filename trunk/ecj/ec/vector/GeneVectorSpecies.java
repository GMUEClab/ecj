/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.vector;
import ec.*;
import ec.util.*;

/* 
 * GeneVectorSpecies.java
 * 
 * Created: Tue Feb 20 13:26:00 2001
 * By: Sean Luke
 */

/**
 * GeneVectorSpecies is a subclass of VectorSpecies with special
 * constraints for GeneVectorIndividuals.
 *
 * <p>At present there is exactly one item stored in GeneVectorSpecies:
 * the prototypical VectorGene that populates the genome array stored in a
 * GeneVectorIndividual.
 *
 * @author Sean Luke
 * @version 1.0 
 
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><tt>gene</tt><br>
 <font size=-1>classname, inherits and != ec.VectorGene</font></td>
 <td valign=top>(the prototypical gene for this kind of individual)</td></tr>
 </table>

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>gene</tt></td>
 <td>The prototypical gene for this kind of individual</td></tr>
 </table>

*/
 
public class GeneVectorSpecies extends VectorSpecies
    {
    public static final String P_GENE = "gene";
    public VectorGene genePrototype;

    public void setup(final EvolutionState state, final Parameter base)
        {
        Parameter def = defaultBase();

        genePrototype = (VectorGene)(state.parameters.getInstanceForParameterEq(
                                         base.push(P_GENE),def.push(P_GENE),VectorGene.class));
        genePrototype.setup(state,base.push(P_GENE));

        // make sure that super.setup is done AFTER we've loaded our gene prototype.
        super.setup(state,base);
        }
        
    }

