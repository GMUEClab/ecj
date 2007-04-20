import drm.core.Node;
import drm.util.ConfigProperties;
import drm.agentbase.IRequest;
import drm.agentbase.Address;
import drm.agentbase.IAgent;
import drmhc.HillClimber;
import java.util.*;
import java.net.InetAddress;

public class Launch implements Runnable {


// =========== Private Fields ========================================
// ===================================================================


private Node node = null;

private final String exper = "hillclimb"+System.currentTimeMillis();


// =========== private methods =======================================
// ===================================================================


private Address launch( String name, Address root ) throws Throwable {

	IAgent a = new HillClimber( exper, name, root );
	
	IRequest r = node.launch("RANDOM", a, null );
	while( r.getStatus() == IRequest.WAITING )
	{
		try { Thread.currentThread().sleep(10); }
		catch( Exception e ) {}
	}
	
	if( r.getThrowable() != null ) throw r.getThrowable();
	return (Address)r.getInfo("address");
}


// =========== Public constructors ===================================
// ===================================================================

	
public Launch( Node node ) {

	this.node = node;
}

// =========== Public Runnable implementation ========================
// ===================================================================

public void run() {
try
{
	System.err.println("Launching hillclimber job");
	if( node == null ) return;
	
	// loads the config pars as a system resource from the
	// classpath which is the jar or directory of the job
	Properties conf = new Properties();
	ClassLoader cl = getClass().getClassLoader();
	try
	{
		conf.load( cl.getResourceAsStream(
			"config.properties" ) );
	}
	catch( Exception e )
	{
		System.err.println(
			"Error loading config, using defaults.");
	}

	// --- launching root

	Address root = launch("root",null);
	// this should be a local launch
	
	if( ! root.isLocal() )
	{
		System.err.print("Root launched to remote node.");
		System.err.print(
			" This is not necessarily what you want.");
		System.err.println(" Something is going wrong...");
	}
	
	// --- launching the rest
	
	int i=Integer.parseInt(
		conf.getProperty("launch.contributors","4"));
	while( i>0 )
	{
		try{ launch( ""+i, root ); }
		catch( Exception e ) { ++i; }
		--i;
	}
}
catch( Throwable e )
{
	e.printStackTrace();
}
finally
{
	System.err.println("Launching finished.");
}

}

}

