/** 
 * Copyright 2007 Alberto Cuesta Cañada, licensed under the Academic Free License.
 * @author Alberto Cuesta Cañada
 * @version 0.1 
 */

package ec.drm;

import java.io.*;

import drm.agentbase.*;

import ec.*;

/** Container for sending statistics data between EvolutionAgents */
public class StatisticsData implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public final Address sender;
	public final int generation;
	public final long elapsed;
	public final Individual[] best_individuals;
	public final Individual[] best_of_run;
	public final boolean finalStats;
	
	public StatisticsData(Address s, int g, long e, Individual[] bi, Individual[] br){
		sender = s;
		generation = g;
		elapsed = e;
		best_individuals = bi;
		best_of_run = br;
		finalStats = true;
	}
	
	public StatisticsData(Address s, int g, Individual[] bi){
		sender = s;
		generation = g;
		elapsed = 0L;
		best_individuals = bi;
		best_of_run = null;
		finalStats = false;
	}
	
	public String toStringForHumans(){
	    String s = "";
	    	
	    s += sender.name + ":\n";
	    s += "\tGeneration: " + generation + "\n";
	    for (int i=0; i < best_individuals.length; i++){
	    		s += "\tBest of Generation for Subpopulation " + i + "\n";
	    		s += "\tFitness: " + 
	    			best_individuals[i].fitness.fitnessToStringForHumans() + "\n";
	    		s += "\tGenotype: " + 
	    			best_individuals[i].genotypeToStringForHumans() + "\n";
	    	}

	    if(finalStats){
	    	s += "\tElapsed Time: " + elapsed + "\n";
	        
	    	for (int i=0; i < best_of_run.length; i++){
	    		s += "\tBest of Run for Subpopulation " + i + "\n";
	    		s += "\tFitness: " + 
	    			best_of_run[i].fitness.fitnessToStringForHumans() + "\n";
	    		s += "\tGenotype: " + 
	    			best_of_run[i].genotypeToStringForHumans() + "\n";    	
	    	}
	    }
	    return s;
	}
}