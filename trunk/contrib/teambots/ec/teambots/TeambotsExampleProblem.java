/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.teambots;

import ec.*;
import ec.vector.BitVectorIndividual;

import EDU.gatech.cc.is.util.Vec2;
import EDU.gatech.cc.is.util.Units;

/**
 *
 * This class demonstrates how to create an evolved control system from an
 * individual such that one can use ECJ to evolve ControlSystems for Teambots.
 * For this demonstration, lets assume that the representation of the individual
 * is a boolean-vector one, and that the individuals have rules of 6 genes length
 * each. Its functionality will be encoded as follows: the first 4 genes in a rule
 * will encode the condition for firing (if there are any obstacles in the 4 quadrants)
 * at a distance less than 1 meter. The last 2 genes will codify the action to be
 * taken by the robot (I have no idea what you can encode with 2 boolean values, but
 * this is just an example....)
 *
 * @author Liviu Panait
 * @version 1.0
 */

public abstract class TeambotsExampleProblem extends TeambotsProblem
    {

    /**
       This function is the conversion of an individual from ECJ to a ControlSystem
       for TeamBots.
    */
    public EvolvedControlSystem getControlSystem( final EvolutionState state, final Individual ind, final int threadnum )
        {

        if( !( ind instanceof BitVectorIndividual ) )
            {
            state.output.fatal( "The TeambotsExampleProblem works only with BitVectorIndividuals." );
            }
        if( ((BitVectorIndividual)(ind)).genome.length % 6 != 0 )
            {
            state.output.fatal( "TeambotsExampleProblem error: the length of the individual's chromosome should be a multiple of 6." );
            }

        // Create a new EvolvedControlSystem that has some particularities. One of them
        // is that it contains the individual from ECJ such that real-time decisions
        // can be made based on the values in the individual's genes
        EvolvedControlSystem temp = new EvolvedControlSystem()
            {

            // implementing this function is the most important thing to do
            // because this is the function called by the simulator at each time step
            public int takeStep()
                {

                // get the time
                long curr_time = abstract_robot.getTime();

                // first, i'll calculate the sensor readings such that i use them in my rules as inputs
                // use -1 as parameter to get all information (i think, somebody please check!!!!)
                Vec2[] objects = abstract_robot.getObstacles( curr_time );

                // checks whether there is anything sensed in the specific quadrant in a specific range
                boolean[] sensed = new boolean[4];
                sensed[0] = false;
                sensed[1] = false;
                sensed[2] = false;
                sensed[3] = false;

                // go through all sensed objects and calculate the "sensed" values
                for( int i = 0 ; i < objects.length ; i++ )
                    {
                    // if the distance to the object is in a specific range (assume 0.5 to 2 meters)
                    // mark that there is an obstacle present in that quadrant
                    if( objects[i].r >= 0.5 && objects[i].r <= 2 )
                        {
                        // make sure the angle is in [0..2*PI]
                        double angle = Units.ClipRad( objects[i].r );

                        //decide which quadrant the object belongs to
                        if( objects[i].r <= Math.PI / 2.0 )
                            sensed[0] = true;
                        else if( objects[i].r <= Math.PI )
                            sensed[1] = true;
                        else if( objects[i].r <= 1.5 * Math.PI )
                            sensed[2] = true;
                        else
                            sensed[3] = true;
                        }
                    }

                // convert the Individual ind to a BitVectorIndividual (for easier access)
                BitVectorIndividual individual = (BitVectorIndividual)ind;

                // create the set of rules that fire (that match the "sense" values)
                boolean[] firingRules = new boolean[ individual.genome.length ];

                // the number of rules that fire
                int numRules = 0;

                // detect the rules that fire and copy them into firingRules
                for( int i = 0 ; i < individual.genome.length % 6 ; i++ )
                    {
                    // check if the rule in the individual matches my sensed observations
                    if( individual.genome[ 6*i ]     == sensed[0] &&
                        individual.genome[ 6*i + 1 ] == sensed[1] &&
                        individual.genome[ 6*i + 2 ] == sensed[2] &&
                        individual.genome[ 6*i + 3 ] == sensed[3] )
                        {
                        // add the rule to the firingRules and increase the number of rules that fire
                        for( int j = 0 ; j < 6 ; j++ )
                            firingRules[ 6*numRules + j ] = individual.genome[ 6*i + j ];
                        numRules++;
                        }
                    }

                // now we have to decide which of the rules that fire will actualy be considered
                // this is the CONFLICT RESOLUTION part of the rule-based system
                // for simplicity, we will take here the first rule that fired, but any other
                // mechanism can be implemented instead

                if( numRules == 0 ) // if no rules fired
                    {
                    // do a default action (drive in circle, or whatever)
                    }
                else
                    {
                    // get the first rule and interpret it
                    // for this example, lets suppose that the 5th bit in the rule stands for
                    // "drive with 50% or 100% of the base speed", and the 6th bit represents
                    // whether we should turn arround with 5 degrees or not
                    if( firingRules[4] ) // for new speed encoding in the rule
                        {
                        // set speed to 50% of the base speed
                        abstract_robot.setSpeed( curr_time, 0.5 );
                        }
                    else
                        {
                        // set speed to 100% of the base speed
                        abstract_robot.setSpeed( curr_time, 1.0 );
                        }
                    if( firingRules[5] ) // for new (relative) heading  encoding in the rule
                        {
                        // get the current heading, and set the new current heading
                        double heading = abstract_robot.getSteerHeading(curr_time);
                        heading = Units.ClipRad( heading + 5.0 * Math.PI / 180.0 );
                        abstract_robot.setSteerHeading( curr_time, heading );
                        }
                    }

                // return success
                return CSSTAT_OK;
                }

            };

        return temp;

        }

    }
