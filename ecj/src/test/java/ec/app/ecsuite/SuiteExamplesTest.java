/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.app.ecsuite;

import ec.Evolve;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author Eric O. Scott
 */
@RunWith(Parameterized.class)
public class SuiteExamplesTest {
    
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {   
            {"src/main/java/ec/app/ecsuite/ecsuite.params"},
            {"src/main/java/ec/app/ecsuite/cmaes.params"}
           });
    }
    
    @Parameterized.Parameter
    public String examplePath;

    @Test
    public void testExample() {
        Evolve.main(new String[] { "-file", examplePath });
        // No exception is success.
    }
}
