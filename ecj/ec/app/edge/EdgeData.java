/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.edge;
import ec.gp.*;

/* 
 * EdgeData.java
 * 
 * Created: Wed Nov  3 18:32:13 1999
 * By: Sean Luke
 */

/**
 * @author Sean Luke
 * @version 1.0 
 */

public class EdgeData extends GPData
    {
    // return value
    public int edge;

    public GPData copyTo(final GPData gpd)
        { ((EdgeData)gpd).edge = edge; return gpd; }
    }
