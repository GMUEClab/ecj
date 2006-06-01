/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.ant;
import ec.gp.*;

/* 
 * AntData.java
 * 
 * Created: Wed Nov  3 18:32:13 1999
 * By: Sean Luke
 */

/**
 * Since Ant doesn't actually pass any information, this
 * object is effectively empty.
 *
 * @author Sean Luke
 * @version 1.0 
 */

public class AntData extends GPData
    {
    public GPData copyTo(final GPData gpd) 
        { return gpd; }
    }
