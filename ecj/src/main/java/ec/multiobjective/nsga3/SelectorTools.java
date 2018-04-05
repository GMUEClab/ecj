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
import ec.multiobjective.*;
import ec.multiobjective.nsga3.NSGA3MultiObjectiveFitness;

/*
  SelectorTools is a class designed to get a list of the individuals selected from the last front called
  front L for the NSGA-3 algorithm. This front is to large to be taken as a whole and added to the new 
  population so individuals are selected based on their proximity to a reference point. These reference 
  points are placed, often, evenly across the hyperplane defined by the normalization of the fitness for 
  each objective. It has been shown that this will better approximate the pareto front compared to the 
  sparsity index used in NSGA-2.

  This is a class that from jmetal that builds a hyperPlane and relate individuals to reference points.
  I have refactored this code to work with ECJ. Most of these functions are simple math algorithms that
  do things like normalize the fitness or select the largest points for objective.

  The jMetal git implementation:
  https://github.com/jMetal/jMetal/blob/master/jmetal-algorithm/src/main/java/org/uma/jmetal/algorithm/multiobjective/nsgaiii/util/EnvironmentalSelection.java
*/

import java.util.*;

public class SelectorTools
    {
    List<ArrayList<Individual>> fronts;
    List<ReferencePoint> referencePoints;
    int numberOfObjectives; 
    
    // This needs to be calculated probably more accurately based on the number of dimentions
    final static int NUMBER_OF_DIVISIONS = 6;
        
    public SelectorTools(List<ArrayList<Individual>> fronts, int numberOfObjectives) 
        {
        this.fronts = fronts;
        this.referencePoints = generateReferencePoints(numberOfObjectives, NUMBER_OF_DIVISIONS);
        this.numberOfObjectives = numberOfObjectives;
        }
        
    private List<ReferencePoint> generateReferencePoints(int numberOfObjectives, int NUMBER_OF_DIVISIONS) 
        {
        ArrayList<Double> location = new ArrayList<Double>(numberOfObjectives);
        for(int i = 0; i < numberOfObjectives; i++)
            location.add(0.0);
        List<ReferencePoint> referencePoints = new ArrayList<ReferencePoint>();
        generateRecursive(referencePoints, location, numberOfObjectives, NUMBER_OF_DIVISIONS, NUMBER_OF_DIVISIONS, 0);
        return referencePoints;
        }

    /*
      This generates each reference point and sets in in the referencePoints list 
    */
    private void generateRecursive(List<ReferencePoint> referencePoints, List<Double> location, int numberOfObjectives, int left, int total, int element) 
        {
        if (element == (numberOfObjectives - 1)) 
            {
            location.set(element, (double) left / total) ;
            referencePoints.add(new ReferencePoint(location)) ;
            } else {
            for (int i = 0 ; i <= left; i +=1) 
                {
                location.set(element, (double)i/total);
                generateRecursive(referencePoints, location, numberOfObjectives, left-i, total, element+1);
                }
            }
        }
        
    /*
      This function is just scaling the objective function back based on the largest value for
      that objective function. This is part of the normalizing step which is part of step C in
      The paper as well as step 3 in algorithm 2.
                
      Ideal_min is the amount that is being scaled back, and is used as I think the origin of the
      hyper-plane
    */
    public List<Double> translateObjectives() 
        {
        List<Double> ideal_point;
        ideal_point = new ArrayList<Double>(numberOfObjectives);
        Individual temp;
           
        for (int f=0; f<numberOfObjectives; f+=1) 
            {
            double minf = Double.MAX_VALUE;
            for (int i=0; i<fronts.get(0).size(); i+=1) // min values must appear in the first front
                {
                temp = fronts.get(0).get(i);
                minf = Math.min(minf, ((NSGA3MultiObjectiveFitness) temp.fitness).getObjective(f));
                }
            ideal_point.add(minf);

            for (List<Individual> list : fronts) 
                {
                for (Individual ind : list)
                    {
                    if (f==0) // in the first objective we create the vector of conv_objs
                        ((NSGA3MultiObjectiveFitness) ind.fitness).initNorm(numberOfObjectives);
                                        
                    ((NSGA3MultiObjectiveFitness) ind.fitness).setNormValue(f, ((NSGA3MultiObjectiveFitness) ind.fitness).getObjective(f)-minf);
                                        
                    }
                }
            }
           
        return ideal_point;
        }

        
    // ----------------------------------------------------------------------
    // ASF: Achivement Scalarization Function
    // jMetal implement here a effcient version of it, which only receives 
    // the index of the objective which uses 1.0; the rest will use 0.00001.
    // ----------------------------------------------------------------------
    private double ASF(Individual ind, int index) 
        {
        double max_ratio = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < numberOfObjectives; i++) 
            {
            double weight = (index == i) ? 1.0 : 0.000001;
            max_ratio = Math.max(max_ratio, ((NSGA3MultiObjectiveFitness) ind.fitness).getObjective(i)/weight);
            }
        return max_ratio;
        }
        
    /*
      We use this to find the largest fitness for each objective. This is used in normalization for
      the same reasons we found the minimum for each fitness. We will use both the min and max of 
      each fitness to construct the hyper plane
    */
    private List<Individual> findExtremePoints() 
        {
        List<Individual> extremePoints = new ArrayList<Individual>();
        Individual min_indv = null;
        for (int f=0; f < numberOfObjectives; f+=1)
            {
            double min_ASF = Double.MAX_VALUE;      
            for (Individual ind : fronts.get(0)) 
                { 
                // only consider the individuals in the first front because the are dominate and have the highest fitness
                double asf = ASF(ind, f);
                if ( asf < min_ASF ) 
                    {
                    min_ASF = asf;
                    min_indv = ind;
                    }
                }
                        
            extremePoints.add(min_indv);
            }
        return extremePoints;
        }
        
    /*
      Simple row reduction function that is used in the when finding the dementions of
      the hyper-plane.
    */
    public List<Double> guassianElimination(List<List<Double>> A, List<Double> b) 
        {
        List<Double> x = new ArrayList<Double>();

        int N = A.size();
        for (int i=0; i<N; i+=1)
            {
            A.get(i).add(b.get(i));
            }

        for (int base=0; base<N-1; base+=1)
            {
            for (int target=base+1; target<N; target+=1)
                {
                double ratio = A.get(target).get(base)/A.get(base).get(base);
                for (int term=0; term<A.get(base).size(); term+=1)
                    {
                    A.get(target).set(term, A.get(target).get(term) - A.get(base).get(term)*ratio);
                    }
                }
            }

        for (int i = 0; i < N; i++)
            x.add(0.0);
            
        for (int i=N-1; i>=0; i-=1)
            {
            for (int known=i+1; known<N; known+=1)
                {
                A.get(i).set(N, A.get(i).get(N) - A.get(i).get(known)*x.get(known));
                }
            x.set(i, A.get(i).get(N)/A.get(i).get(i));
            }
        return x;
        }
        
    /*
      Does what it says, constructs a hyper-plane. This is a plane that has bounds at
      one for each objective. the hyper-plane is used to find the perpendicular distance
      between reference points and individuals.
    */
    public List<Double> constructHyperplane(List<Individual> extreme_points) 
        {
        // Check whether there are duplicate extreme points.
        // This might happen but the original paper does not mention how to deal with it.
        boolean duplicate = false;
        for (int i=0; !duplicate && i< extreme_points.size(); i+=1)
            {
            for (int j=i+1; !duplicate && j<extreme_points.size(); j+=1)
                {
                duplicate = extreme_points.get(i).equals(extreme_points.get(j));
                }
            }

        List<Double> intercepts = new ArrayList<Double>();
                
        if (duplicate) // cannot construct the unique hyperplane (this is a casual method to deal with the condition)
            {
            for (int f=0; f<numberOfObjectives; f+=1)
                {
                // extreme_points[f] stands for the individual with the largest value of objective f
                intercepts.add(((NSGA3MultiObjectiveFitness) extreme_points.get(f).fitness).getObjective(f));
                }
            }
        else
            {
            // Find the equation of the hyperplane
            List<Double> b = new ArrayList<Double>(); //(pop[0].objs().size(), 1.0);
            for (int i =0; i < numberOfObjectives;i++)
                b.add(1.0);
                        
            List<List<Double>> A=new ArrayList<List<Double>>();
            for (Individual ind : extreme_points)
                {
                List<Double> aux = new ArrayList<Double>();
                for (int i = 0; i < numberOfObjectives; i++)
                    aux.add(((NSGA3MultiObjectiveFitness) ind.fitness).getObjective(i));
                A.add(aux);
                }
            List<Double> x = guassianElimination(A, b);
                
            // Find intercepts
            for (int f=0; f<numberOfObjectives; f+=1)
                {
                intercepts.add(1.0/x.get(f));
                                
                }
            }
        return intercepts;
        }
        
    /*
      This is the goal of Algorithm 2 in the paper.
      we normalize all of the objectives so all individuals fit between the hyper-plane
      and the origin. this is so we can corrilate them with reference nodes.
    */
    public void normalizeObjectives() 
        {
        //TODO: figure out something here should change source
        List<Double>     ideal_point      = translateObjectives();
        List<Individual> extreme_points   = findExtremePoints();
        List<Double>     intercepts       = constructHyperplane(extreme_points);
                
        for (int t=0; t<fronts.size(); t+=1)
            {
            for (Individual ind : fronts.get(t)) 
                {
                ArrayList<Double> conv_obj = ((NSGA3MultiObjectiveFitness) ind.fitness).getNormFit();
                for (int f = 0; f < numberOfObjectives; f++) 
                    {
                    if (Math.abs(intercepts.get(f)-ideal_point.get(f))> 10e-10)
                        {
                        conv_obj.set(f,conv_obj.get(f) / (intercepts.get(f)-ideal_point.get(f)));
                        }
                    else
                        {
                        conv_obj.set(f,conv_obj.get(f) / (10e-10));
                        }
                    }
                ((NSGA3MultiObjectiveFitness) ind.fitness).setNormFit(conv_obj);
                }
            }
        }
        
    /*
      This function is used to find the distance between a individual and
      the line from a reference node to the origin. individuals are added to
      the reference point that they are closest too.
    */
    public double perpendicularDistance(List<Double> direction, List<Double> point) 
        {
        double numerator = 0, denominator = 0;
        for (int i=0; i<direction.size(); i+=1)
            {
            numerator += direction.get(i)*point.get(i);
            denominator += Math.pow(direction.get(i),2.0);
            }
        double k = numerator/denominator;
                
        double d = 0;
        for (int i=0; i<direction.size(); i+=1)
            {
            d += Math.pow(k*direction.get(i) - point.get(i),2.0);
            }
        return Math.sqrt(d);
        }
        
    /*
      This is basically algorithm 3 in the paper.
                
      Here we are associating all of the individuals in the population to a reference point.
      individuals that are in front L are stored in the reference point object which are then
      selected to fill the remain popluation during breeding.
    */
    public void associate() 
        {
        for (int t = 0; t < fronts.size(); t++) 
            {
            for (Individual ind : fronts.get(t)) 
                {
                int min_rp = -1;
                double min_dist = Double.MAX_VALUE;
                for (int r = 0; r < this.referencePoints.size(); r++) 
                    {
                    double d = perpendicularDistance(this.referencePoints.get(r).pos(), ((NSGA3MultiObjectiveFitness) ind.fitness).getNormFit());
                    if (d < min_dist) 
                        {
                        min_dist=d;
                        min_rp = r; 
                        }
                    }
                // if its not in front L we only want to consider it part of the reference point
                // so we know which points have a higher concentration of individuals
                if (t+1 != fronts.size()) 
                    {
                    this.referencePoints.get(min_rp).addAssociation();
                    } else {
                    this.referencePoints.get(min_rp).addAssociate(ind, min_dist);
                    }
                }
            }
                
        }
        
    /*
      This is the first half of algorithm 4.
                
      This selects the reference point with the smallest number of individuals
      associated with it. This is similar to findind individuals with high
      sparsity from NSGA-2.
    */
    private ReferencePoint findNicheReferencePoint()
        {
        // find the minimal cluster size
        int min_size = Integer.MAX_VALUE;
                
        // find the reference points with the minimal cluster size Jmin
        List<ReferencePoint> minReferencePoints = new ArrayList<ReferencePoint>();
                
        for (int r=0; r<this.referencePoints.size(); r+=1)
            {
            if (this.referencePoints.get(r).numAssociations() < min_size) 
                {
                minReferencePoints = new ArrayList<ReferencePoint>();
                min_size = referencePoints.get(r).numAssociations();
                }
            if (this.referencePoints.get(r).numAssociations() == min_size)
                minReferencePoints.add(referencePoints.get(r));
            }
                
        // return a random reference point
        if (minReferencePoints.size() > 0)
            return minReferencePoints.get(new Random().nextInt(minReferencePoints.size()));
        return minReferencePoints.get(0);
        }
        
    /*
      This is the second half of algorithm 4.
                
      here we are selecting individuals. To do this we first found the
      reference point in findNicheReferencePoint. we then check if there
      are any points already associated to this reference in a higher front.
      if so we dont care about what point as much, so we just return a random
      individual. if there are no individuals associated in higher fronts
      then we select what ever point is closest.
    */
    public Individual SelectClusterMember(ReferencePoint rp)
        {
        Individual chosen = null;
        if (rp.hasAssociates())
            {
            if (rp.numAssociations() == 0) // currently has no member
                {
                chosen =  rp.FindClosestAssociate();
                }
            else
                {
                chosen =  rp.RandomAssociate();
                }
            }
        return chosen;
        }
        
    /*
      This is steps 12 - 17 in algorithm 1.
                
      Here is where we put it all together. we normalize, then associate
      then start selecting individuals based on the nich referecne point.
                
      we then return a list of individuals which is appended to the population
      to complete select.
    */
    public List<Individual> selectFrontLIndividuals(int numToSelect) 
        {
                
        // ---------- Algorithm 2 ----------
        normalizeObjectives();
                
        // ---------- Algorithm 3 ----------
        associate();
                
        // ---------- Algorithm 4 ----------
        ArrayList<Individual> frontL = new ArrayList<Individual>();
        while (frontL.size() < numToSelect)
            {
            ReferencePoint min_rp = findNicheReferencePoint();

            Individual chosen = SelectClusterMember(min_rp);
            if (chosen == null) // no potential member in Fl, disregard this reference point
                {
                this.referencePoints.remove(min_rp); 
                }
            else
                {
                min_rp.addAssociation();
                min_rp.RemoveAssociate(chosen);
                frontL.add(chosen);
                }
            }
                
        return frontL;
        }
    }
