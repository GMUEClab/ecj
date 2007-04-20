package jumperjob;

import drm.agents.Agent;
import drm.core.*;

public class Jumper extends Agent {

/**
* jump counter. Its value is serialized so it is
* preserved while traveling to other nodes.
*/
private int jumps = 0;

/** calls super constructor */
public Jumper( String job, String name ) {

	super( "Jumper", job, name );
}

/**
* Jumps to another random node 3 times. The witing periods are
* not necessary, they are included only to slow it down so it can be
* followed by a human. It is supposed to be an illustration...
*/
public void run() {

	ContributionBox cb = getDRM().getNewestContribution();
	
	if( cb == null )
	{
		System.err.println("No nodes to jump to");
		try { Thread.currentThread().sleep(1000); }
		catch( Exception e ) {}
		suicide();
	}
	else if( jumps++ < 3 )
	{
		try { Thread.currentThread().sleep(1000); }
		catch( Exception e ) {}
		System.err.println("Jumping to "+cb.contributor);
		base.dispatchAgent(name,cb.contributor);
	}
	else
	{	
		System.err.println("Got tired of jumping around...");
		try { Thread.currentThread().sleep(1000); }
		catch( Exception e ) {}
		suicide();
	}
}

}
