/*
  Copyright 2015 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.eda.amalgam;

import ec.*;
import ec.vector.*;
import ec.util.*;
import ec.simple.SimpleFitness;
import java.io.*;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.interfaces.decomposition.CholeskyDecomposition;
import org.ejml.ops.CommonOps;
import org.ejml.ops.NormOps;
import org.ejml.ops.RandomMatrices;
import org.ejml.simple.SimpleMatrix;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;



/**
 * AMALGAMSpecies is a FloatVectorSpecies which implements a faithful version of the
 * iAMaLGaM IDEA algorithm.  The class has two basic methods.  The newIndividual(...)
 * method generates a new random individual underneath the current AMALGAM
 * distribution.  The updateDistribution(...) method revises the
 * distribution to reflect the fitness results of the population.  In many respects
 * this approach is similar to how it's done in CMA-ES [and in fact you'll find that
 * these comments are similar to the CMA-ES comments].
 * 
 * <p>AMALGAMSpecies must be used in combination with AMALGAMBreeder, which will
 * call it at appropriate times to revise the distribution and to generate a new
 * subpopulation of individuals.  Unlike CMA-ES, AMALGAM does not require its own
 * special initializer (we use SimpleInitializer).
 *
 * <p>AMALAGAMSpecies has nine numeric parameters that you can set; five of them
 * have standard default constant values, and four have values which, if you 
 * don't specify them, are updated every step via excessively complex equations.
 *
 * <p>AMALGAMSpecies also has an "alternative termination" option, by default turned off.  
 * Normally ECJ terminates when the optimal individual is discovered or when the generations
 * or maximum number of evaluations has been exceeded.  AMALGAM will also terminate
 * when the "distribution multiplier" is lower than 10^(-10), or when the distribution
 * variance is less than the fitness variance tolerance.  
 *
 * <p>AMALGAMSpecies relies on the EJML matrix library, available at 
 * <a href="http://ejml.org/">http://ejml.org/</a>

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>tau</tt><br>
 <font size=-1>0 &lt;= Floating-point value &lt;= 1</font></td>
 <td valign=top>(proportion of elite individuals)<br>
 If not provided, defaults to 0.35
 </td></tr>

 <tr><td valign=top><i>base</i>.<tt>variance-tolerance</tt><br>
 <font size=-1>0 &lt;= Floating-point value</font></td>
 <td valign=top>(termination condition: if the variance is less than this amount)<br>
 If not provided, defaults to 0.0
 </td></tr>

 <tr><td valign=top><i>base</i>.<tt>nis-max</tt><br>
 <font size=-1>0 &lt;= Floating-point value</font></td>
 <td valign=top>(number of generations with no improvement beyond which the distribution multiplier begins to decrease)<br>
 If not provided, defaults to 25 + genome size
 </td></tr>

 <tr><td valign=top><i>base</i>.<tt>alpha-ams</tt><br>
 <font size=-1>0 &lt; Floating-point value</font></td>
 <td valign=top>(the proportion of individuals to be shifted in the direction of the anticipated mean shift)<br>
 If not provided, defaults to 0.5 * tau * subpopulation size / (subpopulation size - 1)
 </td></tr>

 <tr><td valign=top><i>base</i>.<tt>delta-ams</tt><br>
 <font size=-1>0 &lt; Floating-point value</font></td>
 <td valign=top>(controls how much selected individuals are shifted)<br>
 If not provided, defaults to 2.0
 </td></tr>

 <tr><td valign=top><i>base</i>.<tt>eta-shift</tt><br>
 <font size=-1>0 &lt;= Floating-point value &lt;= 1</font></td>
 <td valign=top>(learning rate of the anticipated mean shift)<br>
 If not provided, defaults to 1.0 - (e ^ (-1.2 * (floor(tau * subpopulation size))^0.31) / (genome size ^ 0.5))
 </td></tr>

 <tr><td valign=top><i>base</i>.<tt>eta-sigma</tt><br>
 <font size=-1>0 &lt;= Floating-point value &lt;= 1</font></td>
 <td valign=top>(learning rate of the covariance matrix)<br>
 If not provided, defaults to 1.0 - e ^ (-1.1 * (floor(tau * subpopulation size)^1.20) / (genome size ^ 1.6))
 </td></tr>

 <tr><td valign=top><i>base</i>.<tt>eta-dec</tt><br>
 <font size=-1>0 &lt;= Floating-point value &lt;= 1</font></td>
 <td valign=top>(degree to which the distribution multipler is decreased in certain conditions)<br>
 If not provided, defaults to 0.9
 </td></tr>

 <tr><td valign=top><i>base</i>.<tt>theta-sdr</tt><br>
 <font size=-1>0 &lt;= Floating-point value</font></td>
 <td valign=top>(threshold for the standard deviation ratio)<br>
 If not provided, defaults to 1.0
 </td></tr>

 <tr><td valign=top><i>base</i>.<tt>alternative-termination</tt><br>
 <font size=-1>boolean, default false</td>
 <td valign=top>Should we also terminate on AMALGAM's additional termination conditions?.
 </td></tr>

 </table>

 <p><b>Default Base</b><br>
 eda.amalgam.species

 * @author Sam McKay and Sean Luke
 * @version 1.0 
 */


public class AMALGAMSpecies extends FloatVectorSpecies 
    {
    public static final String P_AMALGAM_SPECIES = "species";

    public static final String P_TAU = "tau"; // tau
    public static final String P_ETA_DEC = "eta-dec"; // eta-dec
    public static final String P_THETA_SDR = "theta-sdr"; // theta-sdr
    public static final String P_ETA_SHIFT = "eta-shift"; // eta-shift
    public static final String P_ETA_SIGMA = "eta-sigma"; // eta-sigma
    public static final String P_NIS_MAX = "nis-max"; // nis-max
    public static final String P_VARIANCE_TOLERANCE = "variance-tolerance"; // variance-tolerance
    public static final String P_DELTA_AMS = "delta-ams"; // delta-ams
    public static final String P_ALPHA_AMS = "alpha-ams"; // alpha-ams

        
    public static final String P_ALTERNATIVE_TERMINATION = "alternative-termination";
    public static final int P_PARAMETER_MISSING = -1;

    public boolean useAltTermination;

    // 0 <= tau <= 1
    // defaults to 0.35
    public double tau;

    // 0 <= variance-tolerance      : we terminate if the distribution variance is less than this
    // 0 by default
    public double fitnessVarianceTolerance = 0;

    // 0 <= nis-max
    // 26 + genome size by default
    public int maximumNoImprovementStretch = 0;
    public int noImprovementStretch = 0;

    // 0 < alpha-ams
    // default is   0.5 * tau * subpop.individuals.size() / (subpop.individuals.size()-1)
    public double alphaAMS;
    public double userAlphaAMS;
    
    // 0 < delta-ams
    // defaults to 2.0
    public double deltaAMS;

    // 0 <= eta-shift <= 1
    // defaults to   1.0-Math.exp(-1.2*Math.pow((int)(tau*subpop.individuals.size()),0.31)/Math.pow(genomeSize,0.50))
    public double etaP;
    public double userEtaP;

    // 0 <= eta-sigma <= 1
    // defaults to  1.0-Math.exp(-1.1*Math.pow((int)(tau*subpop.individuals.size()),1.20)/Math.pow(genomeSize,1.60))
    public double etaS;
    public double userEtaS;

    // 0 <= eta-dec <= 1
    // defaults to 0.9
    public double distributionMultiplierDecrease;
    // this is just 1.0/distributionMultiplierDecrease -- it's only here because Amalgam's C code had that
    public double distributionMultiplierIncrease;
    public double distributionMultiplier;

    // 0 <= theta-sdr 
    // defaults to 1.0
    public double stDevRatioThresh;



    /** The mean of the distribution. */
    public DenseMatrix64F mean;
    public DenseMatrix64F prevMean;
    public DenseMatrix64F xAvgImp;
    public DenseMatrix64F meanShift;

    public DenseMatrix64F genCovarMatrix;
    public DenseMatrix64F aggCovarMatrix;
    public DenseMatrix64F covarMatrix;
    public DenseMatrix64F choleskyLower;

    // various preallocated vectors/matrices used for intermediate computations
    public DenseMatrix64F temp;
    public DenseMatrix64F temp2;
    public DenseMatrix64F temp3;
    public DenseMatrix64F tempMatrix;

    public IdentityHashMap<Individual, Integer> constraintViolations;

    // some stuff is different for the first generation
    // this flag is flipped in breeder
    public boolean firstGeneration;

    public Parameter defaultBase() 
        {
        return AMALGAMDefaults.base().push(P_AMALGAM_SPECIES);
        }

    public void setup(final EvolutionState state, final Parameter base) 
        {
        super.setup(state, base);
        Parameter def = defaultBase();

        Parameter subpopBase = base.pop();
        Parameter subpopDefaultBase =  ECDefaults.base().push(Subpopulation.P_SUBPOPULATION);

        useAltTermination = state.parameters.getBoolean(base.push(P_ALTERNATIVE_TERMINATION), def.push(P_ALTERNATIVE_TERMINATION),false);

        if (!state.parameters.exists(base.push(P_TAU), def.push(P_TAU)))
            {
            state.output.message("AMALGAM tau was not provided, defaulting to 0.35");
            tau = 0.35;
            }
        else
            {
            tau = state.parameters.getDouble(base.push(P_TAU), def.push(P_TAU),0.0);
            if (tau < 0 || tau > 1) 
                state.output.fatal("If AMALGAM tau is provided, it must be a valid number in the range [0,1]", base.push(P_TAU), def.push(P_TAU));
            }


        if (!state.parameters.exists(base.push(P_ETA_DEC), def.push(P_ETA_DEC)))
            {
            state.output.message("AMALGAM eta-dec was not provided, defaulting to 0.9");
            distributionMultiplierDecrease = 0.9;
            }
        else
            {
            distributionMultiplierDecrease = state.parameters.getDouble(base.push(P_ETA_DEC), def.push(P_ETA_DEC),0.0);
            if (distributionMultiplierDecrease < 0 || distributionMultiplierDecrease > 1) 
                state.output.fatal("If AMALGAM eta-dec is provided, it must be a valid number in the range [0,1]", base.push(P_ETA_DEC), def.push(P_ETA_DEC));
            }

        distributionMultiplier = 1;
        distributionMultiplierIncrease = 1.0/distributionMultiplierDecrease;

        if (!state.parameters.exists(base.push(P_THETA_SDR), def.push(P_THETA_SDR)))
            {
            state.output.message("AMALGAM theta-sdr was not provided, defaulting to 1.0");
            stDevRatioThresh = 1.0;
            }
        else
            {
            stDevRatioThresh = state.parameters.getDouble(base.push(P_THETA_SDR), def.push(P_THETA_SDR),1);
            if (stDevRatioThresh < 0 ) 
                state.output.fatal("If AMALGAM theta-sdr is provided, it must be >= 0", base.push(P_THETA_SDR), def.push(P_THETA_SDR));
            }


        if (!state.parameters.exists(base.push(P_VARIANCE_TOLERANCE), def.push(P_VARIANCE_TOLERANCE)))
            {
            state.output.message("AMALGAM variance-tolerance was not provided, defaulting to 0.0");
            fitnessVarianceTolerance = 0.0;
            }
        else
            {
            fitnessVarianceTolerance = state.parameters.getDouble(base.push(P_VARIANCE_TOLERANCE), def.push(P_VARIANCE_TOLERANCE),0.0);
            if (fitnessVarianceTolerance < 0 ) 
                state.output.fatal("If AMALGAM variance-tolerance is provided, it must be >= 0", base.push(P_VARIANCE_TOLERANCE), def.push(P_VARIANCE_TOLERANCE));
            }

        noImprovementStretch = 0;
        if (!state.parameters.exists(base.push(P_NIS_MAX), def.push(P_NIS_MAX)))
            {
            maximumNoImprovementStretch = 25 + genomeSize;
            }
        else
            {
            maximumNoImprovementStretch = state.parameters.getInt(base.push(P_NIS_MAX), def.push(P_NIS_MAX),0);
            if (maximumNoImprovementStretch <= 0 ) 
                state.output.fatal("If AMALGAM nis-max is provided, it must be a valid integer > 0", base.push(P_NIS_MAX), def.push(P_NIS_MAX));
            }


        if (!state.parameters.exists(base.push(P_DELTA_AMS), def.push(P_DELTA_AMS)))
            {
            state.output.message("AMALGAM delta-ams was not provided, defaulting to 2.0");
            deltaAMS = 2;
            }
        else
            {
            deltaAMS = state.parameters.getDouble(base.push(P_DELTA_AMS), def.push(P_DELTA_AMS),0.0);
            if (deltaAMS <= 0 ) 
                state.output.fatal("If AMALGAM delta-ams is provided, it must be > 0", base.push(P_DELTA_AMS), def.push(P_DELTA_AMS));
            }

        // the default values for the following parameters require knowledge of the population size
        // we don't know it at this point, so wait until update distribution to set default values up
        // for now, just assign P_PARAMETER MISSING if the param file does not contain a value

        if (!state.parameters.exists(base.push(P_ALPHA_AMS), def.push(P_ALPHA_AMS)))
            {
            userAlphaAMS = P_PARAMETER_MISSING;
            }
        else
            {
            userAlphaAMS = state.parameters.getDouble(base.push(P_ALPHA_AMS), def.push(P_ALPHA_AMS),P_PARAMETER_MISSING);
            if (userAlphaAMS <= 0 ) 
                state.output.fatal("If AMALGAM alpha-ams is provided, it must be > 0", base.push(P_ALPHA_AMS), def.push(P_ALPHA_AMS));
            }

        if (!state.parameters.exists(base.push(P_ETA_SHIFT), def.push(P_ETA_SHIFT)))
            {
            userEtaP = P_PARAMETER_MISSING;
            }
        else
            {
            userEtaP = state.parameters.getDouble(base.push(P_ETA_SHIFT), def.push(P_ETA_SHIFT),P_PARAMETER_MISSING);
            if (userEtaP < 0 || userEtaP > 1) 
                state.output.fatal("If AMALGAM eta-shift is provided, it must be a valid number in the range [0,1]", base.push(P_ETA_SHIFT), def.push(P_ETA_SHIFT));
            }

        if (!state.parameters.exists(base.push(P_ETA_SIGMA), def.push(P_ETA_SIGMA)))
            {
            userEtaS = P_PARAMETER_MISSING;
            }
        else
            {
            userEtaS = state.parameters.getDouble(base.push(P_ETA_SIGMA), def.push(P_ETA_SIGMA),P_PARAMETER_MISSING);
            if (userEtaS < 0 || userEtaS > 1) 
                state.output.fatal("If AMALGAM eta-sigma is provided, it must be a valid number in the range [0,1]", base.push(P_ETA_SIGMA), def.push(P_ETA_SIGMA));
            }

        alphaAMS = userAlphaAMS;
        etaP = userEtaP;
        etaS = userEtaS;

        mean = new DenseMatrix64F(genomeSize,1);
        prevMean = new DenseMatrix64F(genomeSize,1);
        xAvgImp = new DenseMatrix64F(genomeSize,1);
        meanShift = new DenseMatrix64F(genomeSize,1);

        genCovarMatrix = CommonOps.identity(genomeSize);
        aggCovarMatrix = CommonOps.identity(genomeSize);
        covarMatrix = CommonOps.identity(genomeSize);
        choleskyLower = CommonOps.identity(genomeSize);

        temp = new DenseMatrix64F(genomeSize,1);
        temp3 = new DenseMatrix64F(genomeSize,1);
        temp2 = new DenseMatrix64F(1,genomeSize);
        tempMatrix = new DenseMatrix64F(genomeSize,genomeSize);

        firstGeneration = true;
        }


    public Object clone()
        {
        AMALGAMSpecies myobj = (AMALGAMSpecies) (super.clone());

        myobj.fitnessVarianceTolerance = fitnessVarianceTolerance;

        myobj.noImprovementStretch = noImprovementStretch = 0;
        myobj.maximumNoImprovementStretch = maximumNoImprovementStretch = 0;

        myobj.tau = tau;

        myobj.alphaAMS = alphaAMS;
        myobj.deltaAMS = deltaAMS;

        myobj.userEtaP = userEtaP;
        myobj.userEtaS = userEtaS;

        myobj.distributionMultiplier = distributionMultiplier;

        myobj.distributionMultiplierDecrease = distributionMultiplierDecrease;
        myobj.distributionMultiplierIncrease = distributionMultiplierIncrease;

        myobj.stDevRatioThresh = stDevRatioThresh;

        /** The mean of the distribution. */
        myobj.mean.set(mean);
        myobj.prevMean.set(prevMean);
        myobj.xAvgImp.set(xAvgImp);
        myobj.meanShift.set(meanShift);

        myobj.genCovarMatrix.set(genCovarMatrix);
        myobj.aggCovarMatrix.set(aggCovarMatrix);
        myobj.covarMatrix.set(covarMatrix);
        myobj.choleskyLower.set(choleskyLower);

        myobj.constraintViolations = (IdentityHashMap<Individual, Integer>) constraintViolations.clone();

        return myobj;
        }



    public void computeConstraintViolations(final EvolutionState state, final Subpopulation subpop) 
        {
        constraintViolations = new IdentityHashMap<Individual, Integer>();
        for (int i = 0; i < subpop.individuals.size(); i++) 
            {
            int cv = 0;
            DoubleVectorIndividual dvind = (DoubleVectorIndividual)(subpop.individuals.get(i));
            for (int j = 0; j < genomeSize; j++) 
                {
                if (dvind.genome[j] < minGene(j) || dvind.genome[j] > maxGene(j)) 
                    {
                    cv++;
                    }
                }
            constraintViolations.put(subpop.individuals.get(i), cv);
            }
        }

    public int compareIndividuals(Individual a, Individual b) 
        {
        int constraintViolationA = constraintViolations.get(a);
        int constraintViolationB = constraintViolations.get(b);

        /// The original iAmalgam code uses a merge sort, but the sorting comparison
        /// does not ever state that two individuals are equal.  It just says that
        /// if a and b are the same, then a > b.  This is of course in violation of 
        /// Java sorting contracts.  So we have to tweak it slightly.  
        /// The iAmalgam sorting code (line 783 of iAMaLGaM-Full.c) says:
        /// 
        ///             Sorts an array of objectives and constraints
        ///             using constraint domination and returns the
        ///             sort-order (small to large).
        ///
        /// By this we assume that the following should be proper:
        ///
        /// If A violates fewer constraints than B
        ///             A is best
        ///     Else if B violates less than A
        ///             B is best
        ///     Else if A is fitter than B
        ///             A is best
        ///     Else if B is fitter than A
        ///             B is best
        ///     Else 
        ///             A and B are equal

        if (constraintViolationA < constraintViolationB)
            {
            return -1;  // A is better
            }
        else if (constraintViolationB < constraintViolationA)
            {
            return 1;  // B is better
            }
        else return a.compareTo(b);  // compares based on fitness, with 0 as a tie
        }

    public boolean isValid(DoubleVectorIndividual dvind)
        {
        for (int i = 0; i < genomeSize; i++)
            {
            if (dvind.genome[i] < minGene(i) || dvind.genome[i] > maxGene(i)) 
                {
                return false;
                }
            }
        return true;
        }


    public Individual newIndividual(final EvolutionState state, int thread) 
        {
        Individual newind = super.newIndividual(state, thread);
        MersenneTwisterFast random = state.random[thread];

        if (!(newind instanceof DoubleVectorIndividual))  // uh oh
            state.output.fatal("To use AMALGAMSpecies, the species must be initialized with a DoubleVectorIndividual.  But it contains a " + newind);


        DoubleVectorIndividual dvind = (DoubleVectorIndividual)(newind);
        DenseMatrix64F genome = DenseMatrix64F.wrap(genomeSize,1,dvind.genome);

        while (true) 
            {

            if (!firstGeneration) 
                {
                for ( int i = 0; i < genomeSize; i++ )
                    dvind.genome[i] = random.nextGaussian();

                CommonOps.mult(choleskyLower,genome,temp);
                CommonOps.add(temp,mean,genome);

                if (!isValid(dvind)) 
                    {
                    continue;
                    }
                } 
            else 
                {
                for ( int i = 0; i < genomeSize; i++ )
                    dvind.genome[i] = minGene(i) + (maxGene[i] - minGene(i)) * random.nextDouble();
                }

            return newind;
            }

        }

    public void adaptDistributionMultiplier(final EvolutionState state, final Subpopulation subpop) 
        {
        // don't run for the first generation

        boolean improved = false;
        for (int i = 1; i < tau*subpop.individuals.size(); i++) 
            {
            // if ind[i] is better than ind[0] (the individual kept from the previous population)
            // if (subpop.individuals.get(i).compareTo(subpop.individuals.get(0)) < 0) 
                {
                if (compareIndividuals(subpop.individuals.get(i), subpop.individuals.get(0)) < 0) 
                    {
                    improved = true;
                    break;
                    }
                }
            }
                        
        if (improved) 
            {
            noImprovementStretch = 0;
            if (distributionMultiplier < 1) distributionMultiplier = 1;

            xAvgImp = new DenseMatrix64F(genomeSize, 1);
            int count = 0;
            for (int j = 1; j < tau*subpop.individuals.size(); j++) 
                {
                if (compareIndividuals(subpop.individuals.get(j), subpop.individuals.get(0)) < 0) 
                    {
                    DoubleVectorIndividual dvind = (DoubleVectorIndividual)(subpop.individuals.get(j));
                    DenseMatrix64F genome = DenseMatrix64F.wrap(genomeSize,1,dvind.genome);
                    CommonOps.add(xAvgImp,genome,xAvgImp);
                    count++;
                    }

                }
            CommonOps.scale(1.0/count,xAvgImp,xAvgImp);

            CommonOps.subtract(xAvgImp, mean, temp);
            CommonOps.invert(choleskyLower, tempMatrix);
            CommonOps.mult(tempMatrix, temp, temp3);
            double sdr = CommonOps.elementMaxAbs(temp3);

            if (sdr > stDevRatioThresh) 
                {
                distributionMultiplier *= distributionMultiplierIncrease;
                }
            } 
        else 
            {
            if (distributionMultiplier <= 1) noImprovementStretch++;
            if (distributionMultiplier > 1 || noImprovementStretch > maximumNoImprovementStretch) distributionMultiplier *= distributionMultiplierDecrease;
            if (distributionMultiplier < 1 && noImprovementStretch < maximumNoImprovementStretch) distributionMultiplier = 1;
            }
        }

    public void selectForDiversity(final EvolutionState state, final Subpopulation subpop) 
        {
        int numBest; // the number of individuals with fitness equal to the best individual
        DoubleVectorIndividual bestInd = (DoubleVectorIndividual) subpop.individuals.get(0);
        for (numBest = 1; numBest < subpop.individuals.size(); numBest++) 
            {
            DoubleVectorIndividual dvind = (DoubleVectorIndividual)(subpop.individuals.get(numBest));
            if (dvind.compareTo(bestInd) != 0) 
                {
                break; // break when we find an individual that isn't as good as the best
                }
            }

        // rearrange the selections so that the selected individuals out of the equal fitness ones are at the front of the array
        // chooses the individual that is farthest from the ones selected previously

        int numSelectedSoFar = 1; // first one is already selected
        double[] distances = new double[numBest];
        Arrays.fill(distances, Double.POSITIVE_INFINITY); // value guaranteed to be overwriten
        for (; numSelectedSoFar < tau*subpop.individuals.size(); numSelectedSoFar++) 
            {
            double farthest = -1; // always less than the first candidate

            for (int i = numSelectedSoFar; i < numBest; i++) 
                {
                double distance = subpop.individuals.get(numSelectedSoFar-1).distanceTo(subpop.individuals.get(i));

                if (distance < distances[i]) 
                    {
                    distances[i] = distance;
                    }

                if (distances[i] > farthest)
                    {
                    farthest = distances[i];
                    Individual tmp = subpop.individuals.get(i);
                    subpop.individuals.set(i, subpop.individuals.get(numSelectedSoFar));
                    subpop.individuals.set(numSelectedSoFar, tmp);
                    }
                }
            }

        }

    public void computeMean(final EvolutionState state, final Subpopulation subpop) 
        {
        prevMean.set(mean);
        CommonOps.fill(mean,0);
        if (distributionMultiplier >= 1.0) 
            {
            int i;
            for (i = 0; i < tau*subpop.individuals.size(); i++) 
                {
                DoubleVectorIndividual dvind = (DoubleVectorIndividual)(subpop.individuals.get(i));
                DenseMatrix64F genome = DenseMatrix64F.wrap(genomeSize,1,dvind.genome);
                CommonOps.add(mean,genome,mean);
                }
            CommonOps.scale(1.0/i,mean,mean);
            } 
        else 
            {
            // focus on the best solution
            DoubleVectorIndividual dvind = (DoubleVectorIndividual)(subpop.individuals.get(0));
            mean.set(DenseMatrix64F.wrap(genomeSize,1,dvind.genome));
            }
        }

    public void computeCovariance(final EvolutionState state, final Subpopulation subpop) 
        {
        CommonOps.fill(genCovarMatrix, 0);

        int i;
        for (i = 0; i < tau*subpop.individuals.size(); i++) 
            {
            DoubleVectorIndividual dvind = (DoubleVectorIndividual)(subpop.individuals.get(i));
            DenseMatrix64F genome = DenseMatrix64F.wrap(genomeSize,1,dvind.genome);
            CommonOps.subtract(genome,mean,temp);
            CommonOps.transpose(temp,temp2);
            CommonOps.multAdd(temp,temp2,genCovarMatrix);
            }



        CommonOps.scale(1.0/i,genCovarMatrix,genCovarMatrix);

        if (!firstGeneration) 
            {
            CommonOps.scale(etaS, genCovarMatrix, genCovarMatrix);
            CommonOps.scale(1-etaS, aggCovarMatrix, aggCovarMatrix);
            CommonOps.add(aggCovarMatrix, genCovarMatrix, aggCovarMatrix);
            } 
        else 
            {
            aggCovarMatrix.set(genCovarMatrix);
            }    
        CommonOps.scale(distributionMultiplier, aggCovarMatrix, covarMatrix);

        for ( i = 0; i < genomeSize; i++ )
            for (int j = 0; j < i; j++ )
                covarMatrix.set(i,j,covarMatrix.get(j,i));
        }

    public void computeAMS(final EvolutionState state, final Subpopulation subpop) 
        {
        CommonOps.subtract(mean,prevMean,temp);
        if (!firstGeneration) 
            {
            CommonOps.scale(etaP, temp);
            CommonOps.scale(1-etaP, meanShift);
            CommonOps.add(meanShift,temp,meanShift);
            } 
        else 
            {
            meanShift.set(temp);
            }
        }


    public void updateDistribution(final EvolutionState state, final Subpopulation subpop) 
        {
        CommonOps.fill(temp, 0);
        CommonOps.fill(temp3, 0);
        CommonOps.fill(temp2, 0);
        CommonOps.fill(tempMatrix, 0);

        if (userEtaP == P_PARAMETER_MISSING) 
            {
            etaP = 1.0-Math.exp(-1.2*Math.pow((int)(tau*subpop.individuals.size()),0.31)/Math.pow(genomeSize,0.50));
            }

        if (userEtaS == P_PARAMETER_MISSING) 
            {
            etaS = 1.0-Math.exp(-1.1*Math.pow((int)(tau*subpop.individuals.size()),1.20)/Math.pow(genomeSize,1.60));
            }

        if (userAlphaAMS == P_PARAMETER_MISSING) 
            {
            alphaAMS = 0.5 * tau * subpop.individuals.size() / (subpop.individuals.size()-1);
            }


        computeConstraintViolations(state, subpop);

        if (!firstGeneration)
            {
            adaptDistributionMultiplier(state, subpop);
            }

        Collections.sort(subpop.individuals, new Comparator<Individual>() 
                {
                public int compare(Individual a, Individual b) 
                    {
                    return compareIndividuals(a, b);
                    }
            });
        // printStats(state,subpop);

        if (subpop.individuals.get((int)tau*subpop.individuals.size()).fitness.fitness() == subpop.individuals.get(0).fitness.fitness()) 
            {
            selectForDiversity(state,subpop);
            }


        if (checkTerminationConditions(state,subpop)) 
            {
            state.evaluator.setRunComplete("AMALGAMSpecies: Termination condition reached.");
            }

        computeMean(state, subpop);
        computeCovariance(state, subpop);
        computeAMS(state, subpop);


        // System.out.println("distributionMultiplier: " + distributionMultiplier);
        CholeskyDecomposition chol = DecompositionFactory.chol(genomeSize, true);

        tempMatrix.set(covarMatrix);

        if (!chol.decompose(tempMatrix)) 
            {
            chol.getT(choleskyLower);
            // state.output.fatal("Failed to decompose matrix");
            } 
        else
            {
            chol.getT(choleskyLower);
            }
        }

    public boolean checkTerminationConditions(final EvolutionState state,  final Subpopulation subpop) 
        {
        if (!useAltTermination)
            return false;
                
        if (distributionMultiplier < 1e-10) 
            {
            return true;
            }

        double avg = 0, var = 0;
        // terminate if fitness variance multiplier gets too small
        for (int i = 0; i < subpop.individuals.size(); i++) 
            {
            DoubleVectorIndividual dvind = (DoubleVectorIndividual)(subpop.individuals.get(i));
            avg += dvind.fitness.fitness();
            }
        avg /= subpop.individuals.size();

        for (int i = 0; i < subpop.individuals.size(); i++) 
            {
            DoubleVectorIndividual dvind = (DoubleVectorIndividual)(subpop.individuals.get(i));
            var += (dvind.fitness.fitness()-avg)*(dvind.fitness.fitness()-avg);
            }

        var /= subpop.individuals.size();

        if (var <= 0.0)
            var = 0.0;

        if (var < fitnessVarianceTolerance)
            {
            return true;
            }

        return false;
        }

    public void shiftIndividual(final EvolutionState state, DoubleVectorIndividual ind)
        {
        DenseMatrix64F genome = DenseMatrix64F.wrap(genomeSize,1,ind.genome);
        double shiftMult = 1;
        temp.set(genome);
        do 
            {
            genome.set(temp);
            CommonOps.add(shiftMult*deltaAMS*distributionMultiplier, meanShift, genome, genome);
            shiftMult *= 0.5;
            } while (!isValid(ind) && shiftMult > 1e-10);
        }

    // public void printStats(final EvolutionState state, final Subpopulation subpop) {
    //     // System.out.println(distributionMultiplier);
    //     DoubleVectorIndividual first = (DoubleVectorIndividual)(subpop.individuals.get(0));
    //     double avg = 0, var = 0;
    //     double best = first.fitness.fitness();
    //     double worst = first.fitness.fitness();

    //     for (int i = 0; i < subpop.individuals.size(); i++) {
    //         DoubleVectorIndividual dvind = (DoubleVectorIndividual)(subpop.individuals.get(i));
    //         avg += dvind.fitness.fitness();
    //         if (dvind.fitness.fitness() > best) {
    //             best = dvind.fitness.fitness();
    //         }
    //         if (dvind.fitness.fitness() < worst) {
    //             worst = dvind.fitness.fitness();
    //         }
    //     }
    //     avg /= subpop.individuals.size();

    //     for (int i = 0; i < subpop.individuals.size(); i++) {
    //         DoubleVectorIndividual dvind = (DoubleVectorIndividual)(subpop.individuals.get(i));
    //         var += (dvind.fitness.fitness()-avg)*(dvind.fitness.fitness()-avg);
    //     }

    //     var /= subpop.individuals.size();


    //     System.out.printf("# Generation Evaluations  Average-obj. Variance-obj.     Best-obj.    Worst-obj.   Dist. mult. \n");
    //     System.out.printf("  %10d %11d %13e %13e %13e %13e %13e\n", state.generation, 0, avg, var, best, worst, distributionMultiplier);


    //     for (int i = 0; i < subpop.individuals.size(); i++) {
    //         DoubleVectorIndividual dvind = (DoubleVectorIndividual)(subpop.individuals.get(i));
    //         DenseMatrix64F genome = DenseMatrix64F.wrap(genomeSize,1,dvind.genome);
    //         // genome.transpose().print();
    //         CommonOps.transpose(genome, temp2);
    //         // temp2.print(); 
    //     }  
    // }
    }

