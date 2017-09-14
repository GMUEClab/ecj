/*
  Copyright 2013 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.app.majority;
import ec.gp.*;

public class MajorityData extends GPData
    {
    public long data0;
    public long data1;

    public void copyTo(final GPData gpd) 
        {
        MajorityData md = (MajorityData)gpd; 
        md.data0 = data0;
        md.data1 = data1;
        }
    }
