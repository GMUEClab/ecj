/*
DRM, distributed resource machine supporting special distributed applications
Copyright (C) 2002 The European Commission DREAM Project IST-1999-12679

This file is part of DRM.

DRM is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

DRM is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with DRM; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

contact: http://www.dr-ea-m.org, http://www.sourceforge.net/projects/dr-ea-m
*/


package drm.agents;

import drm.agentbase.Address;
import drm.agentbase.Message;
import drm.agentbase.Logger;
import drm.core.*;

import java.io.PrintStream;

/**
* This is an agent that participates in a collective.
* The name of the collective is the job name passed to the constructor.
*/
public abstract class ContributorAgent extends Agent
implements Contributor, Observer {
static final long serialVersionUID = 3630371562251930132L;


// ============ protected fields ==================================
// ================================================================


/**
* The collective this object is a membet of.
*/
protected Collective collective = null;

/**
* The root contributor. The presence of this is not crutial, it only provides
* a fixed and reliable address to fall back to.
*/
protected Address root = null;


// ============ public constructors ===============================
// ================================================================


/** Calls super constructor */
public ContributorAgent( String type, String job, String name ) {

	super( type, job, name );
}

// ----------------------------------------------------------------

/** Calls super constructor.
* @param root The fixed reliable address in the collective to fall back to
* if no peers are known. Might be null, in that case connection to
* the collective is possible only if this is a root, ie if other
* contributors connect to this address. Might be a local address, in that case
* this agent will change it to the correct remote address (ie to the address
* of its old node) when arriving at a remote node.
*/
public ContributorAgent( String type, String job, String name, Address root) {

	super( type, job, name );
	this.root = root;
}


// ============ public IAgent implementations =====================
// ================================================================

/**
* Corrects root address if it is a local address and we are arriving from
* a remote base. Connects to the collective.
* <p>Extending classes that override this method must make sure that
* they call <code>super.onArrival</code>.
* The suggested practice is that every implementation must begin with the
* line
* <pre>
* super.onArrival(from,to);
* </pre>
*/
public final void onArrival( Address from, Address to ) {

	super.onArrival(from,to);
	
	if( root != null && root.isLocal() && from != null )
		root = new Address( from.getHost(), from.port, root.name );

	if( collective == null ) collective = new Collective(job,this,this);
	else collective = new Collective(collective,this,this,null);
	collective.addPeerAddress(root);
	new Thread(collective,getJob()+" Collective").start();
}

// ----------------------------------------------------------------

/** Closes connection with the collective.
* <p>Extending classes that override this method must make sure that
* they call <code>super.onDestruction</code>.
* The suggested practice is that every implementation must begin with the
* line
* <pre>
* super.onDestruction();
* </pre>
*/
public final void onDestruction() {

	super.onDestruction();
	collective.close();
}

// ----------------------------------------------------------------

/**
* Handles message type <code>"collectiveUpdate-"+getJob()</code>. In fact
* only forwards it to the collective.
* <p>Extending classes that override this method must make sure that
* they call <code>super.handleMessage</code>.
* The suggested practice is that every implementation must begin with the
* line
* <pre>
* if( super.handleMessage(m,object) ) return true;
* </pre>
* and after that handling of the class specific messages.
*/
public boolean handleMessage( Message m, Object object ) {

	if( super.handleMessage( m, object ) ) return true;

	if( m.getType().equals("collectiveUpdate-"+job) )
	{
		return collective.handleMessage(m,object);
	}
	else
	{
		Logger.debug(getClass().getName()+"#handleMessage",
			m.getType() + " received from " + m.getSender());
		return false;
	}
}


// ============ public collective related implementations =========
// ================================================================


/**
* The default implementation returns null. Extending classes must
* redefine this method to be able to contribute to the collective.
*/
public Object getContribution() { return null; }

// ----------------------------------------------------------------

/**
* The default implementation does nothing.
*/
public void collectiveUpdated( ContributionBox peer ) {}

// ----------------------------------------------------------------

//----------------------------------------------------------------

/** Called when no peers in the collective are known, i.e. when starting the
 * agent or when all known peers went unreachable. It returns root (if any) 
 * and all agents present in peer nodes that seem to belong to the same collective.*/

/*public Address[] getPeerAddresses(){
	Iterator nodes = getDRM().getContributions().iterator();
	Iterator agents;
	List peers = new ArrayList();
	String name;
	ContributionBox cb;
	NodeContribution nc;
	
	if(root != null) peers.add(root);
	
	while(nodes.hasNext()){
		cb = (ContributionBox)nodes.next();
		nc = (NodeContribution)cb.contribution;
		agents = nc.getAgents().iterator();
		while(agents.hasNext()){
			name = (String)agents.next();
			if(name.contains(job))
				peers.add(new Address(cb.contributor.getHost(),cb.contributor.port, name));
		}
	}
	if(peers.size() > 0)
		return (Address[])peers.toArray( new Address[ peers.size() ] );
	return null;
}*/

/** Returns the root address. */
public Address getRootAddress(){
	if( root == null ) return null;
	else return root;
}


// ============ convinience methods ===============================
// ================================================================


public void selfTest( PrintStream output ) {

	if( output == null ) output = System.out;
	
	super.selfTest(output);
	output.println("-- root: " + root );
	output.println("-- collective: " + collective );
}


}

