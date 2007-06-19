package drmhc;

/**
* This class contains algorithmic components needed by the
* hillclimber agent. Don't forget that this is a toy example only.
* This class is separated mainly to illustrate that a job can
* use many classes, not only the agent class.
*
* <p>It contains an objective function to be maximized
* ({@link #eval(double)}) over the domain [0,1]. It contains
* operators that operate on this real domain.
*/
public class Algorithm implements java.io.Serializable {

public static double eval( double x ) {

	// would be far too fast without waiting
	try { Thread.currentThread().sleep(100); }
	catch( Exception e ) {}

	return x*Math.sin(x*50);
}

public static double mutate( double x ) {

	x += (2*Math.random()-1)/3.0;
	if( x < 0 ) x = 0;
	if( x > 1 ) x = 1;

	return x;
}

}

