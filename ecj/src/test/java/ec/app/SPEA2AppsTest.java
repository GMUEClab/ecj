/*
  Copyright 2017 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.app;

import ec.EvolutionState;
import ec.Evolve;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Custom system tests to supplement the automatic harness given by AppsTest.
 * 
 * @author Eric O. Scott
 */
public class SPEA2AppsTest
{
    
    public SPEA2AppsTest()
    {
    }
    
    @Before
    public void setUp()
    {
    }
    
    /**  Moosuite apps inhereit from nsga2.params by default, but we want to
     * test SPEA2 instead.  This test runs all the moosuite examples after asking 
     * them to inhereit from spea2.params.
     */
    @Test
    public void spea2Test()
    {
        final File moosuiteRoot = new File("src/main/resources/ec/app/moosuite");
        // Get the paths to all the parameter files inside the moosuite app, except those on AppsTest's exclude list.
        final List<Object[]> paramFiles = AppsTest.getParamFiles(moosuiteRoot, AppsTest.exclude);
        for (final Object[] f : paramFiles)
            {
            assert(f.length == 1);
            final String examplePath = (String) f[0];
            try {
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Testing " + examplePath);
                final ParameterDatabase parameters = new ParameterDatabase(new File(examplePath));
                parameters.set(new Parameter(EvolutionState.P_GENERATIONS), "2");
                parameters.set(new Parameter(Evolve.P_SILENT), "true");
                parameters.set(new Parameter("parent.1"), "spea2.params");
                // Can't use Evolve.main() because it calls System.exit()
                final EvolutionState state = Evolve.initialize(parameters, 0);
                state.output.setThrowsErrors(true);
                state.run(EvolutionState.C_STARTED_FRESH);
                // No exception is success.
            }
            catch (IOException e){
                fail(e.toString());
            }
            }
    }
}
