/* Ealontro - systems that evolve and adapt to their environment
 * Copyright (C) 2006, 2007 Ken Webb
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 */

package org.primordion.ealontro.gp;

import org.primordion.xholon.base.IXholon;
import org.primordion.xholon.base.Xholon;
import ec.gp.GPIndividual;
import ec.gp.koza.KozaFitness;
import ec.simple.SimpleEvolutionState;

/**
 * Xholon version of ECJ SimpleEvolutionState.
 * @author Ken Webb
 * @since 0.3 (Created on May 27, 2006)
 */
public class XhSimpleEvolutionState extends SimpleEvolutionState {

	protected static IXholon xhRoot; // root of Xholon node hierarchy
	
	/**
	 * Set the IXholon root node.
	 * @param root The intended root node.
	 */
	public static void setRoot(IXholon root)
	{
		xhRoot = root;
	}
	
	/*
	 * @see ec.EvolutionState#startFresh()
	 */
    public void startFresh()
    {
    	super.startFresh();
        System.out.println("Population: " + population.subpops[0].individuals.length);
    	updateXhTree();
    }
    
    /*
     * @see ec.EvolutionState#evolve()
     */
    public int evolve()
    {
    	int rc = super.evolve();
    	updateXhTree();
    	return rc;
    }
    
    /*
     * @see ec.EvolutionState#finish(int)
     */
    public void finish(int result)
    {
    	super.finish(result);
    	//updateXhTree();
    }
    
    /**
     * Update the Xholon tree.
     */
    protected void updateXhTree()
    {
    	//System.out.println("updateXhTree");
        // Xholon
		GPIndividual ind;
		// get first System node (ex: first of <EcjAntTrailSystem multiplicity="1024">)
		IXholon xhSys = xhRoot.getFirstChild();
		IXholon xhGp;
		for (int i = 0; i < population.subpops[0].individuals.length; i++) {
			// get GeneticProgram node, which is next sibling of a Structure node
			xhGp = xhSys.getFirstChild().getNextSibling();
			ind = (GPIndividual)population.subpops[0].individuals[i];
			ind.trees[0].setParentChildLinks(xhGp);
			setId(ind.trees[0]);
			xhSys.setRoleName("fitness" + ((KozaFitness)ind.fitness).fitness());
			xhSys = xhSys.getNextSibling();
		}
		// end Xholon
    }
    
    /**
     * Set the ID of this IXholon node.
     * @param node The node whose ID is to be set.
     */
    protected void setId(IXholon node)
    {
    	if (node.getId() == 0) {
    		node.setId(Xholon.getNextId());
    	}
    	IXholon newNode = node.getFirstChild();
    	if (newNode != null) {
    		setId(newNode);
    	}
    	newNode = node.getNextSibling();
    	if (newNode != null) {
    		setId(newNode);
    	}
    }
}
