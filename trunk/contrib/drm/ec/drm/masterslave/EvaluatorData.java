/** 
 * Copyright 2007 Alberto Cuesta Cañada, licensed under the Academic Free License.
 * @author Alberto Cuesta Cañada
 * @version 0.1 
 */

package ec.drm.masterslave;

import java.io.*;

import drm.agentbase.Address;
import ec.Individual;

/** Container for exchanging of Individuals between the MasterEvaluator and the SlaveAgents */
public class EvaluatorData implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public int id;
	public int generation;
	public Address sender;
	public int subpop;
	public Individual[] individuals;
	public boolean evaluated;
	
	public EvaluatorData(int i, int g, Address s, int sp, Individual[] inds, boolean eval){
		id = i;
		generation = g;
		sender = s;
		subpop = sp;
		individuals = inds;
		evaluated = eval;
	}
}