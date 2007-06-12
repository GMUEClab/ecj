package ec.drm.app.tutorial8;

import drm.agentbase.*;

import ec.*;
import ec.drm.*;

public class MyStatisticsData extends StatisticsData{
	private static final long serialVersionUID = 1L;

	public final Individual[] test_individuals;
	
	public MyStatisticsData(Address s, int g, long e, Individual[] bi, Individual[] br, Individual[] te){
		super(s,g,e,bi,br);
		test_individuals = te;
	}
	
	public String toStringForHumans(){
		String s = super.toStringForHumans();
		
	    for (int i=0; i < test_individuals.length; i++){
    		s += "\tTest individual for subpopulation " + i + "\n";
    		s += "\tFitness: " + 
    			test_individuals[i].fitness.fitnessToStringForHumans() + "\n";
    		s += "\tGenotype: " + 
    			test_individuals[i].genotypeToStringForHumans() + "\n";
    	}
		
		return s;
	}
}