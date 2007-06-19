package drmhc;

import drm.agentbase.Address;
import drm.agents.ContributorAgent;
import drm.core.*;

import java.util.List;
import java.util.Properties;
import java.io.FileWriter;

public class HillClimber extends ContributorAgent {

private double currentSolution;

private double currentValue;

private int evals = 0;

private transient Properties conf;

// ----------------------------------------------------------------

/** calls super constructor */
public HillClimber( String job, String name, Address root ) {

	super( "HillClimber", job, name, root );
	currentSolution = Math.random();
	currentValue = Algorithm.eval(currentSolution);
}

// ----------------------------------------------------------------

/**
* Runs the hillclimber. The optimized function is defined in
* {@link Algorithm}. The maximal function evals is given by property
* "hillclimber.maxEvals". See default config file
* <a href="doc-files/config.properties">here</a>.
*/
public void run() {

	// loads the config pars as a system resource from the
	// classpath which is the jar or directory of the job
	// It must be loaded here and not construction time because
	// after construction the agent might be serialized.
	conf = new Properties();
	ClassLoader cl = getClass().getClassLoader();
	try
	{
		conf.load( cl.getResourceAsStream(
			"config.properties" ) );
	}
	catch( Exception e ) {}
	
	double x, y;
	final int maxEvals = Integer.parseInt(
		conf.getProperty("hillclimber.maxEvals","1000"));
	
	for(; shouldLive && evals < maxEvals; ++evals )
	{
		x = Algorithm.mutate(currentSolution);
		y = Algorithm.eval(x);
		if( y >= currentValue )
		{
			currentSolution = x;
			currentValue = y;
		}
	}

	if( !name.endsWith("root") ) suicide();
}

// ----------------------------------------------------------------

/**
* Returns the current best solution.
*/
public Object getContribution() {

	return new double[] { currentSolution, currentValue };
}

// ----------------------------------------------------------------

/**
* Checks if there is a better solution than our current best.
* If there is, we adopts it.
* If this is the roor than it attempts to log the contributions to a
* file name, which is given as property "hillclimber.outFile".
* See default config file
* <a href="doc-files/config.properties">here</a>.
*/
public void collectiveUpdated( ContributionBox peer ) {

	List peers = collective.getContributions();
	
	for( int i=0; i<peers.size(); ++i )
	{
		double[] contrib = (double[])
			((ContributionBox)peers.get(i)).contribution;
		if( contrib[1] > currentValue )
		synchronized(this)
		{
			currentSolution = contrib[0];
			currentValue = contrib[1];
		}
	}
	
	// --- log stuff if root
	// don't forget: this is an example. Much more clever logging
	// should be used than simply dumping out stuff every time.
	if( name.endsWith("root") )
	try
	{
		FileWriter fw = new FileWriter( conf.getProperty(
		  "hillclimber.outFile", "hillclimber.out" ), true );
		for( int i=0; i<peers.size(); ++i )
		{
			double[] contrib = (double[])
			 ((ContributionBox)peers.get(i)).contribution;
			fw.write(contrib[0]+" "+contrib[1]+"\n");
		}
		fw.close();
	}
	catch( Exception e ) { e.printStackTrace(); }
}



}
