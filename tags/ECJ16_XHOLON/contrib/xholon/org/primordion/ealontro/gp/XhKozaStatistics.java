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

import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import org.primordion.xholon.base.IXholon;
import org.primordion.xholon.util.Misc;
import ec.EvolutionState;
import ec.gp.GPIndividual;
import ec.gp.koza.KozaFitness;
import ec.gp.koza.KozaStatistics;

/**
 * Generates Koza-style statistics, including an XML file that contains the best solution found.
 * The XML file can be used as CompositeStructureHierarchy.xml in a Xholon application.
 * @author Ken Webb
 * @since 0.3 (Created on May 17, 2006)
 */
public class XhKozaStatistics extends KozaStatistics {

	// name and path of saved CompositeStructureHierarchy XML file
	protected static final String cshFileName = "EcjCompositeStructureHierarchy_Best.xml";
	protected static String cshFilePath = "./";
	
	// stored best, average, worst fitness (ECJ standardized fitness = ECJ raw fitness) of current generation
	protected float xhFitnessWorst;
	protected float xhFitnessAverage;
	protected float xhFitnessBest;
	
	/**
	 * Get the worst fitness (ECJ standardized fitness = ECJ raw fitness) of this generation.
	 * @return Worst fitness.
	 */
	public float getWorstFitnessThisGeneration() {return xhFitnessWorst;}
	
	/**
	 * Get the average fitness (ECJ standardized fitness = ECJ raw fitness) of this generation.
	 * @return Average fitness.
	 */
	public float getAverageFitnessThisGeneration() {return xhFitnessAverage;}
	
	/**
	 * Get the best fitness (ECJ standardized fitness = ECJ raw fitness) of this generation.
	 * @return Best fitness.
	 */
	public float getBestFitnessThisGeneration() {return xhFitnessBest;}
	
	/**
	 * Set the best fitness (ECJ standardized fitness = ECJ raw fitness) of this generation.
	 * @param state The current ECJ state.
	 */
	protected void setBestFitnessThisGeneration(EvolutionState state)
	{
		int subPop = 0;
		KozaFitness bestFit = (KozaFitness)state.population.subpops[subPop].individuals[0].fitness;
		for (int i = 1; i < state.population.subpops[subPop].individuals.length; ++i) {
			KozaFitness fit = (KozaFitness)state.population.subpops[subPop].individuals[i].fitness;
			if (fit.betterThan(bestFit))
				bestFit = fit;
		}
		xhFitnessBest = bestFit.standardizedFitness();
	}
	
	/**
	 * Set the average fitness (ECJ standardized fitness = ECJ raw fitness) of this generation.
	 * @param state The current ECJ state.
	 */
	protected void setAverageFitnessThisGeneration(EvolutionState state)
	{
		float meanAdjusted = 0.0f;
		int subPop = 0;
		for (int i = 0; i < state.population.subpops[subPop].individuals.length; ++i) {
			meanAdjusted += ((KozaFitness)(state.population.subpops[subPop].individuals[i].fitness)).standardizedFitness(); //.adjustedFitness();
		}
		meanAdjusted /= state.population.subpops[subPop].individuals.length;
		xhFitnessAverage = meanAdjusted;
	}
	
	/**
	 * Set the worst fitness (ECJ standardized fitness = ECJ raw fitness) of this generation.
	 * @param state The current ECJ state.
	 */
	protected void setWorstFitnessThisGeneration(EvolutionState state)
	{
		int subPop = 0;
		KozaFitness worstFit = (KozaFitness)state.population.subpops[subPop].individuals[0].fitness;
		for (int i = 1; i < state.population.subpops[subPop].individuals.length; ++i) {
			KozaFitness fit = (KozaFitness)state.population.subpops[subPop].individuals[i].fitness;
			if (worstFit.betterThan(fit))
				worstFit = fit;
		}
		xhFitnessWorst = worstFit.standardizedFitness();
	}
	
	/*
	 * @see ec.Statistics#postEvaluationStatistics(ec.EvolutionState)
	 */
	public void postEvaluationStatistics(final EvolutionState state)
	{
		super.postEvaluationStatistics(state);
		setWorstFitnessThisGeneration(state);
		setAverageFitnessThisGeneration(state);
		setBestFitnessThisGeneration(state);
	}
	
	/**
	 * Set the CompositeStructureHierarchy.xml file path, given the name of the model.
	 * @param modelName Model name (ex: ./config/ealontro/CartCentering/EcjCartCentering_xhn.xml)
	 */
	public static void setCshFilePath(String modelName)
	{
		System.out.println(modelName);
		int ix = modelName.lastIndexOf("/");
		if (ix == -1) {
			ix = modelName.lastIndexOf("\\");
		}
		if (ix != -1) {
			cshFilePath = modelName.substring(0, ix+1);
		}
	}
	
	/*
	 * @see ec.Statistics#finalStatistics(ec.EvolutionState, int)
	 */
	public void finalStatistics(final EvolutionState state, final int result)
	{
		super.finalStatistics(state,result);
		// write XML file
		writeBestAsXhXml(state);
	}
	
	/**
	 * Write the best of run individual as a CompositeStructureHierarchy XML file.
	 * @param state The current ECJ state.
	 */
	protected void writeBestAsXhXml(EvolutionState state)
	{
		//System.out.println("writeBestAsXhXml");
		// open file
		Writer out = Misc.openOutputFile(cshFilePath + cshFileName); //("./config/CompositeStructureHierarchy_Best.xml");
		IXholon sysXh;
		String sysClassName;
		Date dateOut = new Date();
		// write tree
		try {
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			sysXh = ((GPIndividual)best_of_run[0]).trees[0].getParentNode().getParentNode();
			sysClassName = sysXh.getXhcName();
			out.write("<!--\nBest of run for "
					+ sysClassName + "\n"
					+ "raw fitness: " + ((KozaFitness)best_of_run[0].fitness).rawFitness()
					+ " adjusted fitness: " + ((KozaFitness)best_of_run[0].fitness).fitness()
					+ " hits: " + ((KozaFitness)best_of_run[0].fitness).hits + "\n"
					//+ ((GPIndividual)best_of_run[0]).trees[0].child.printNode(state, (PrintWriter)out) + "\n"
					+ "size of best tree: " + ((GPIndividual)best_of_run[0]).trees[0].treeSize()
					+ " starting at " + ((GPIndividual)best_of_run[0]).trees[0].getName() + "\n"
					+ dateOut + " (" + dateOut.getTime() + ")\n"
					+ "-->\n");
			out.write("<Population>\n");
			out.write("\t<" + sysClassName + ">" + " <!-- xholon instance " + sysXh.getId() + " -->\n");
			out.write("\t\t<" + sysXh.getFirstChild().getXhcName() + ">\n"); // <Structure>
			sysXh.getFirstChild().getFirstChild().writeXml(3, out); // Structure subtree
			out.write("\t\t</" + sysXh.getFirstChild().getXhcName() + ">\n"); // </Structure>
			out.write("\t\t<" + sysXh.getFirstChild().getNextSibling().getXhcName() + ">\n"); // <GeneticProgram>
			((GPIndividual)best_of_run[0]).trees[0].writeXml(3, out); // GPTree PfWrapper and subtree
			out.write("\t\t</" + sysXh.getFirstChild().getNextSibling().getXhcName() + ">\n"); // <GeneticProgram>
			//((GPIndividual)best_of_run[0]).trees[0].getParentNode().getPreviousSibling().writeXml(2, out);
			out.write("\t</" + sysClassName + ">\n");
			out.write("</Population>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		// close file
		Misc.closeOutputFile( out );
	}
}
