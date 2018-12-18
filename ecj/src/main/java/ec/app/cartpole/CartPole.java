/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.app.cartpole;

import ec.*;
import ec.neat.*;
import ec.simple.*;

public class CartPole extends Problem implements SimpleProblemForm
    {
    int MAX_STEPS=100000;
    double x,                   /* cart position, meters */
        x_dot,                      /* cart velocity */
        theta,                      /* pole angle, radians */
        theta_dot;          /* pole angular velocity */
    int steps=0,y;


    public double[] getNetOutput(NEATNetwork net, double[][] in,EvolutionState state)
        {
        double[] out;
        int netDepth = net.maxDepth();

        net.loadSensors(in[0]);
        for(int relax = 0; relax < netDepth; relax++)
            {
            net.activate(state);
            }

        out = net.getOutputResults();

        net.flush();


        return out;
        }
    public int runCartPole(NEATNetwork net, EvolutionState state)
        {



        // double in[] = new double[5];  //Input loading array

        double out1;
        double out2;
        double twelve_degrees=0.2094384;
        x = x_dot = theta = theta_dot = 0.0;
        steps = 0;




        double[][] in= new double[1][5];
        while (steps++ < MAX_STEPS)
            {

            /*-- setup the input layer based on the four inputs and bias --*/
            //setup_input(net,x,x_dot,theta,theta_dot);
            in[0][0] = 1.0;  //Bias
            in[0][1] = (x + 2.4) / 4.8;

            in[0][2] = (x_dot + .75) / 1.5;
            in[0][3] = (theta + twelve_degrees) / .41;
            in[0][4] = (theta_dot + 1.0) / 2.0;

            double[] out = getNetOutput(net,in,state);

            /*-- decide which way to push via which output unit is greater --*/
            if(out[0] > out[1])
                y=0;
            else
                y=1;

            /*--- Apply action to the simulated cart-pole ---*/
            cart_pole(y);

            /*--- Check for failure.  If so, return steps ---*/
            if (x < -2.4 || x > 2.4  || theta < -twelve_degrees || theta > twelve_degrees)
                return steps;
            }

        return steps;



        }
    void cart_pole(int action)
        {
        double xacc,thetaacc,force,costheta,sintheta,temp;

        final double GRAVITY=9.8;
        final double MASSCART=1.0;
        final double MASSPOLE=0.1;
        final double TOTAL_MASS=(MASSPOLE + MASSCART);
        final double LENGTH=0.5;   /* actually half the pole's length */
        final double POLEMASS_LENGTH=(MASSPOLE * LENGTH);
        final double FORCE_MAG=10.0;
        final double TAU=0.02;     /* seconds between state updates */
        final double FOURTHIRDS=1.3333333333333;

        force = (action>0)? FORCE_MAG : -FORCE_MAG;
        costheta = Math.cos(theta);
        sintheta = Math.sin(theta);

        temp = (force + POLEMASS_LENGTH * theta_dot * theta_dot * sintheta) / TOTAL_MASS;

        thetaacc = (GRAVITY * sintheta - costheta* temp)
            / (LENGTH * (FOURTHIRDS - MASSPOLE * costheta * costheta
                    / TOTAL_MASS));

        xacc  = temp - POLEMASS_LENGTH * thetaacc* costheta / TOTAL_MASS;

        /*** Update the four state variables, using Euler's method. ***/

        x  += TAU * x_dot;
        x_dot += TAU * xacc;
        theta += TAU * theta_dot;
        theta_dot += TAU * thetaacc;
        }
    public void evaluate(EvolutionState state, Individual ind, int subpopulation, int threadnum)
        {

        if (ind.evaluated) return;

        if (!(ind instanceof NEATIndividual))
            state.output.fatal("Whoa! It's not a NEATIndividual!!!", null);

        NEATIndividual neatInd = (NEATIndividual) ind;

        if (!(neatInd.fitness instanceof SimpleFitness))
            state.output.fatal("Whoa! It's not a SimpleFitness!!!", null);


        NEATNetwork net = neatInd.createNetwork();

        double fitness = runCartPole(net, state);

        ((SimpleFitness)neatInd.fitness).setFitness(state, fitness, fitness >= (double)MAX_STEPS);
        neatInd.evaluated = true;


        }

    }
