/*
  Copyright 2017 by Antonio J. Nebro and Juan J. Durillo
  With Modifications by Ben Brumbac, Eric Scott, and Sean Luke
  Licensed under the MIT License shown here:
  
  Permission is hereby granted, free of charge, to any person obtaining a copy of this 
  software and associated documentation files (the "Software"), to deal in the Software 
  without restriction, including without limitation the rights to use, copy, modify, 
  merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
  permit persons to whom the Software is furnished to do so, subject to the following 
  conditions:

  The above copyright notice and this permission notice shall be included in all copies 
  or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
  PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
  FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
  OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
  DEALINGS IN THE SOFTWARE.
*/
 
package ec.multiobjective.nsga3;

import ec.*;
import ec.util.*;
import ec.simple.*;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import ec.multiobjective.*;

/*
  The reference point object is used in NSGA-3 as a way of finding the last set of children from front L for the new population.
  The reference point is used in the SelectorTools class to return what the best individuals for front L would be. For more
  infromation about how reference points are being used check the read me. 

  The design of this object was taken from the JMetal implementation: 
  "https://github.com/jMetal/jMetal/blob/master/jmetal-algorithm/src/main/java/org/uma/jmetal/algorithm/multiobjective/nsgaiii/util/ReferencePoint.java"
*/

public class ReferencePoint 
    {

    ArrayList<Entry<Double, Individual>> associates;
    int associations;
    ArrayList<Double> position;
        
    public ReferencePoint(int size) 
        {
        position = new ArrayList<Double>(size);
        for(int i =0; i < size; i++)
            position.add(0.0);
        associations = 0 ;
        associates = new ArrayList<Entry<Double, Individual>>();
        }
        
    public ReferencePoint(List<Double> point) 
        {
        position = new ArrayList<Double>(point.size());
        for (Double d : point) 
            {
            position.add(new Double(d));
            }
        associations = 0;
        associates = new ArrayList<Entry<Double, Individual>>();
        }

    public List<Double> pos()  { return this.position; }
    public int  numAssociations(){ return associations; }
    public boolean hasAssociates() { return associates.size() > 0; }
    public void clear(){ associations=0; this.associates.clear();}
    public void addAssociation(){this.associations++;}
    
    public void addAssociate(Individual ind, double distance)
        {
        this.associates.add(new SimpleEntry<Double, Individual>(distance,ind));
        }

    public Individual FindClosestAssociate() 
        {
        double minDistance = Double.MAX_VALUE;
        Individual closetAssociate = null;
        for (Entry<Double, Individual> p : this.associates) 
            {
            if (p.getKey() < minDistance) 
                {
                minDistance = p.getKey();
                closetAssociate = p.getValue();
                }
            }
        return closetAssociate;
        }

    public Individual RandomAssociate() 
        {
        return associates.get(new Random().nextInt(associates.size())).getValue();
        }
  
    public void RemoveAssociate(Individual ind) 
        {
        Iterator<Entry<Double, Individual>> iter = this.associates.iterator();
        while (iter.hasNext()) 
            {
            if (iter.next().getValue().equals(ind)) 
                {
                iter.remove();
                break;
                }
            }
        }
    }
