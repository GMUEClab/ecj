package ec.drm.app.tutorial5;

import java.io.*;

import drm.agentbase.Address;
import ec.Individual;

public class EvaluatorData implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public int id;
	public Address sender;
	public int subpop;
	public Individual[] individuals;
	public boolean evaluated;
	
	public EvaluatorData(int i, Address s, int sp, Individual[] inds, boolean eval){
		id = i;
		sender = s;
		subpop = sp;
		individuals = inds;
		evaluated = eval;
	}
}