package talkjob;

import drm.agents.Agent;
import drm.agentbase.*;

public class Talker extends Agent {

/** calls super constructor */
public Talker( String job, String name ) {

	super( "Talker", job, name );
}

/**
* Launches another agent, sends it a message and prints reply.
* Does not suicide to allow some manual testing afterwards.
*/
public void run() {

	if( name.endsWith("2") ) return; // this agent is passive
	
	IRequest r = base.launch( 
		"RANDOM",new Talker( job, "2" ), null );
	while( r.getStatus() == IRequest.WAITING )
	{
		try { Thread.currentThread().sleep(100); }
		catch( Exception e ) {}
	}
	if( r.getStatus() != IRequest.DONE ) return;

	Address a = (Address)r.getInfo("address");
	r = fireMessage( a, "test", "How are you?" );
	while( r.getStatus() == IRequest.WAITING )
	{
		try { Thread.currentThread().sleep(100); }
		catch( Exception e ) {}
	}
	if( r.getStatus() == IRequest.DONE )
		System.out.println( "Answer: "+r.getInfo("reply") );
}

/**
* Handles message type "test" answering always with the String object
* "Fine thanks."
*/
public boolean handleMessage( Message m, Object object ) {

	if( super.handleMessage( m, object ) ) return true;
	
	if( m.getType().equals("test") )
	{
		System.out.println("Received: "+object);
		m.setReply("Fine thanks.");
		return true;
	}

	return false;
}


}
