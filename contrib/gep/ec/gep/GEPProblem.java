/*
 * Copyright (c) 2006 by National Research Council of Canada.
 *
 * This software is the confidential and proprietary information of
 * the National Research Council of Canada ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into
 * with the National Research Council of Canada.
 *
 * THE NATIONAL RESEARCH COUNCIL OF CANADA MAKES NO REPRESENTATIONS OR
 * WARRANTIES ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.
 * THE NATIONAL RESEARCH COUNCIL OF CANADA SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * 
 */


package ec.gep;
import ec.util.*;
import ec.*;

/* 
 * GEPProblem.java
 * 
 * Created: Mon Nov 6, 2006
 * By: Bob Orchard
 */

/**
 * A GEPProblem is a Problem which is meant to efficiently handle GEP
 * evaluation.  These are created by the user (along with the appropriate
 * parameter file and any data files).
 *
 * @author Bob Orchard
 * @version 1.0 
 */

public abstract class GEPProblem extends Problem 
{
    public final static String P_GEPPROBLEM = "problem";


    /** GPProblem defines a default base so your subclass doesn't
        absolutely have to. */
    public Parameter defaultBase()
    {
        return GEPDefaults.base().push(P_GEPPROBLEM);
    }

    public void setup(final EvolutionState state, final Parameter base)
    {
        // must be something we need to do here? seems not ...
    }

    public Object clone()
    {
        GEPProblem prob = (GEPProblem)(super.clone());

        return prob;
    }

    /** If the data set is defined in a data file (CSV ... comma separated values) 
     * then this method does not have to be supplied.
     * 
     * @param s the terminal symbol for which we want the training values (perhaps we should have
     *          just passed the label for the symbol??).
     * @return
     */
    public double[] getDataValues( String s )
    {
    	return null;
    }

    /** If the testing data set is defined in a data file (CSV ... comma separated values) 
     * then this method does not have to be supplied. If there is no testing data set
     * then it doesn't have to be supplied.
     * 
     * @param s the terminal symbol for which we want the testing values (perhaps we should have
     *          just passed the label for the symbol??).
     * @return
     */
    public double[] getTestingDataValues( String s )
    {
    	return null;
    }

    /**
     * If the raw time series data is not in a file then the user program must provide
     * the data.
     * 
     * @return an array with the time series data.
     */
    public double[] getTimeSeriesDataValues( )
    {
    	return null;
    }

    public void describe(final Individual ind, 
                         final EvolutionState state, int subpopulation,
                         final int threadnum, final int log,
                         final int verbosity)
    {
        // default version does nothing
        return;
    }
}
