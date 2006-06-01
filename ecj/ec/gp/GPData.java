/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp;
import ec.*;
import ec.util.*;

/* 
 * GPData.java
 * 
 * Created: Mon Oct 25 19:37:18 1999
 * By: Sean Luke
 */

/**
 * GPData is the parent class of data transferred between GPNodes.
 * If performed correctly, there need be only one GPData instance 
 * ever created in the evaluation of many individuals. 

 <p><b>Default Base</b><br>
 gp.data

 *
 * @author Sean Luke
 * @version 1.0 
 */

public abstract class GPData implements Prototype
    {
    public static final String P_GPDATA = "data";

    /** Modifies gpd so that gpd is equivalent to us. You may
        safely assume that gpd is of the same class as we are. */
    public abstract GPData copyTo(final GPData gpd);

    public Parameter defaultBase()
        {
        return GPDefaults.base().push(P_GPDATA);
        }

    public void setup(final EvolutionState state, final Parameter base) 
        { }

    public Object clone()
        {
        try { return super.clone(); }
        catch (CloneNotSupportedException e) 
            { throw new InternalError(); } // never happens
        }


    }
