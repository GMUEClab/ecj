package hwjob;

import drm.agents.Agent;

public class HelloWorld extends Agent {

/** calls super constructor */
public HelloWorld( String job, String name ) {

	super( "Helloworld", job, name );
}

/** prints "Hello World" and exits after 5s waiting */
public void run() {

	System.out.println("Hello world!");

	try { Thread.currentThread().sleep(5000); }
	catch( Exception e ) {}
	
	suicide();
}

}
