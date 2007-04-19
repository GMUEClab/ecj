package ec.drm;

import java.io.*;

import drm.agentbase.*;

import ec.*;

public class ExchangerData implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public final Address sender;
	public final int subpop;
	public final Individual[] individuals;
	
	public ExchangerData(Address s, int sp, Individual[] inds){
		sender = s;
		subpop = sp;
		individuals = inds;
	}
}