/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.exchange;
import ec.*;
import ec.util.*;

/* 
 * InterPopulationExchange.java
 * 
 * Created Sat Feb 10 13:44:11 EST 2001
 * By: Liviu Panait
 */

/**
 * InterPopulationExchange is an Exchanger which implements a simple exchanger
 * between subpopulations. IterPopulationExchange uses an arbitrary graph topology
 * for migrating individuals from subpopulations. The assumption is that all
 * subpopulations have the same representation and same task to solve, otherwise
 * the exchange between subpopulations does not make much sense.

 * <p>InterPopulationExchange has a topology which is similar to the one used by
 * IslandExchange.  Every few generations, a subpopulation will send some number
 * of individuals to other subpopulations.  Since all subpopulations evolve at
 * the same generational speed, this is a synchronous procedure (IslandExchange
 * instead is asynchronous by default, though you can change it to synchronous).
 
 * <p>Individuals are sent from a subpopulation prior to breeding.  They are stored
 * in a waiting area until after all subpopulations have bred; thereafter they are
 * added into the new subpopulation.  This means that the subpopulation order doesn't
 * matter.  Also note that it means that some individuals will be created during breeding,
 * and immediately killed to make way for the migrants.  A little wasteful, we know,
 * but it's simpler that way.
 
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><tt><i>base</i>.chatty</tt><br>
 <font size=-1>boolean, default = true</font></td>
 <td valign=top> Should we be verbose or silent about our exchanges?
 </td></tr>
 </table>
 <p><i>Note:</i> For each subpopulation in your population, there <b>must</b> be 
 one exch.subpop... declaration set.
 <table>
 <tr><td valign=top><tt><i>base</i>.subpop.<i>n</i>.select</tt><br>
 <font size=-1>classname, inherits and != ec.SelectionMethod</font></td>
 <td valign=top> The selection method used by subpopulation #n for picking 
 migrants to emigrate to other subpopulations
 </td></tr>
 <tr><td valign=top><tt><i>base</i>.subpop.<i>n</i>.select-to-die</tt><br>
 <font size=-1>classname, inherits and != ec.SelectionMethod (Default is random selection)</font></td>
 <td valign=top> The selection method used by subpopulation #n for picking 
 individuals to be replaced by migrants
 </td></tr>
 <tr><td valign=top><tt><i>base</i>.subpop.<i>n</i>.mod</tt><br>
 <font size=-1>int >= 1</font></td>
 <td valign=top> The number of generations that subpopulation #n waits between 
 sending emigrants
 </td></tr>
 <tr><td valign=top><tt><i>base</i>.subpop.<i>n</i>.start</tt><br>
 <font size=-1>int >= 0</font></td>
 <td valign=top> The generation when subpopulation #n begins sending emigrants
 </td></tr>
 <tr><td valign=top><tt><i>base</i>.subpop.<i>n</i>.size</tt><br>
 <font size=-1>int >= 0</font></td>
 <td valign=top> The number of emigrants sent at one time by generation #n
 </td></tr>
 <tr><td valign=top><tt><i>base</i>.subpop.<i>n</i>.num-dest</tt><br>
 <font size=-1>int >= 0</font></td>
 <td valign=top> The number of destination subpopulations for this subpopulation.
 </td></tr>
 <tr><td valign=top><tt><i>base</i>.subpop.<i>n</i>.dest.<i>m</i></tt><br>
 <font size=-1>int >= 0</font></td>
 <td valign=top> Subpopulation #n's destination #m is this subpopulation.
 </td></tr>
 </table>
 
 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><tt><i>base</i>.subpop.<i>n</i>.select</tt><br>
 <td>selection method for subpopulation #n's migrants</td></tr>
 </table>

 *  
 * @author Liviu Panait & Sean Luke
 * @version 2.0 
 */


public class InterPopulationExchange extends Exchanger
    {

    /** The subpopulation delimiter */
    public static final String P_SUBPOP = "subpop";

    /** The parameter for the modulo (how many generations should pass between consecutive sendings of individuals */
    public static final String P_MODULO = "mod";

    /** The number of emigrants to be sent */
    public static final String P_SIZE = "size";

    /** How many generations to pass at the beginning of the evolution before the first
        emigration from the current subpopulation */
    public static final String P_OFFSET = "start";

    /** The number of destinations from current island */
    public static final String P_DEST_FOR_SUBPOP = "num-dest";

    /** The prefix for destinations */
    public static final String P_DEST = "dest";

    /** The selection method for sending individuals to other islands */
    public static final String P_SELECT_METHOD = "select";

    /** The selection method for deciding individuals to be replaced by immigrants */
    public static final String P_SELECT_TO_DIE_METHOD = "select-to-die";
    
    /** Whether or not we're chatty */
    public static final String P_CHATTY = "chatty";

    /** My parameter base -- I need to keep this in order to help the server
        reinitialize contacts */
    // SERIALIZE
    public Parameter base;
    
    class IPEInformation
        {

        // the selection method
        SelectionMethod immigrantsSelectionMethod;

        // the selection method
        SelectionMethod indsToDieSelectionMethod;

        // the number of destination subpopulations
        int numDest;

        // the subpopulations where individuals need to be sent
        int[] destinations;

        // the modulo
        int modulo;

        // the start (offset)
        int offset;

        // the size
        int size;

        }

    IPEInformation[] exchangeInformation;

    //  storage for the incoming immigrants: 2 sizes:
    //    the subpopulation and the index of the emigrant
    // this is virtually the array of mailboxes
    Individual[][] immigrants;

    // the number of immigrants in the storage for each of the subpopulations
    int[] nImmigrants;

    int nrSources;
    
    public boolean chatty;

    // sets up the Island Exchanger
    public void setup( final EvolutionState state, final Parameter _base )
        {
        base = _base;

        Parameter p_numsubpops = new Parameter( ec.Initializer.P_POP ).push( ec.Population.P_SIZE );
        int numsubpops = state.parameters.getInt(p_numsubpops,null,1);
        if ( numsubpops == 0 )
            {
            // later on, Population will complain with this fatally, so don't
            // exit here, just deal with it and assume that you'll soon be shut
            // down
            }

        // how many individuals (maximally) would each of the mailboxes have to hold
        int[] incoming = new int[ numsubpops ];

        // allocate some of the arrays
        exchangeInformation = new IPEInformation[ numsubpops ];
        for( int i = 0 ; i < numsubpops ; i++ )
            exchangeInformation[i] = new IPEInformation();
        nImmigrants = new int[ numsubpops ];

        Parameter p;

        Parameter localBase = base.push( P_SUBPOP );

        chatty = state.parameters.getBoolean(base.push(P_CHATTY), null, true);

        for( int i = 0 ; i < numsubpops ; i++ )
            {

            // update the parameter for the new context
            p = localBase.push( "" + i );

            // read the selection method
            exchangeInformation[i].immigrantsSelectionMethod = (SelectionMethod)
                state.parameters.getInstanceForParameter( p.push( P_SELECT_METHOD ), null, ec.SelectionMethod.class );
            if( exchangeInformation[i].immigrantsSelectionMethod == null )
                state.output.fatal( "Invalid parameter.",  p.push( P_SELECT_METHOD ));
            exchangeInformation[i].immigrantsSelectionMethod.setup( state, p.push(P_SELECT_METHOD) );

            // read the selection method
            if( state.parameters.exists( p.push( P_SELECT_TO_DIE_METHOD ) ) )
                exchangeInformation[i].indsToDieSelectionMethod = (SelectionMethod)
                    state.parameters.getInstanceForParameter( p.push( P_SELECT_TO_DIE_METHOD ), null, ec.SelectionMethod.class );
            else // use RandomSelection
                exchangeInformation[i].indsToDieSelectionMethod = new ec.select.RandomSelection();
            exchangeInformation[i].indsToDieSelectionMethod.setup( state, p.push(P_SELECT_TO_DIE_METHOD) );

            // get the modulo
            exchangeInformation[i].modulo = state.parameters.getInt( p.push( P_MODULO ), null, 1 );
            if( exchangeInformation[i].modulo == 0 )
                state.output.fatal( "Parameter not found, or it has an incorrect value.", p.push( P_MODULO ) );
            
            // get the offset
            exchangeInformation[i].offset = state.parameters.getInt( p.push( P_OFFSET ), null, 0 );
            if( exchangeInformation[i].offset == -1 )
                state.output.fatal( "Parameter not found, or it has an incorrect value.", p.push( P_OFFSET ) );
            
            // get the size
            exchangeInformation[i].size = state.parameters.getInt( p.push( P_SIZE ), null, 1 );
            if( exchangeInformation[i].size == 0 )
                state.output.fatal( "Parameter not found, or it has an incorrect value.", p.push( P_SIZE ) );

            // get the number of destinations
            exchangeInformation[i].numDest = state.parameters.getInt( p.push( P_DEST_FOR_SUBPOP ), null, 0 );
            if( exchangeInformation[i].numDest == -1 )
                state.output.fatal( "Parameter not found, or it has an incorrect value.", p.push( P_DEST_FOR_SUBPOP ) );

            exchangeInformation[i].destinations = new int[ exchangeInformation[i].numDest ];
            // read the destinations
            for( int j = 0 ; j < exchangeInformation[i].numDest ; j++ )
                {
                exchangeInformation[i].destinations[j] =
                    state.parameters.getInt( p.push( P_DEST ).push( "" + j ), null, 0 );
                if( exchangeInformation[i].destinations[j] == -1 ||
                    exchangeInformation[i].destinations[j] >= numsubpops )
                    state.output.fatal( "Parameter not found, or it has an incorrect value.", p.push( P_DEST ).push( "" + j ) );
                // update the maximum number of incoming individuals for the destination island
                incoming[ exchangeInformation[i].destinations[j] ] += exchangeInformation[i].size;
                }

            }
            
        // calculate the maximum number of incoming individuals to be stored in the mailbox
        int max = -1;

        for( int i = 0 ; i < incoming.length ; i++ )
            if( max == - 1 || max < incoming[i] )
                max = incoming[i];

        // set up the mailboxes
        immigrants = new Individual[ numsubpops ][ max ];

        }    


    /**
       Initializes contacts with other processes, if that's what you're doing.
       Called at the beginning of an evolutionary run, before a population is set up.
       It doesn't do anything, as this exchanger works on only 1 computer.
    */
    public void initializeContacts(EvolutionState state)
        {
        }

    /**
       Initializes contacts with other processes, if that's what you're doing.  Called after restarting from a checkpoint.
       It doesn't do anything, as this exchanger works on only 1 computer.
    */
    public void reinitializeContacts(EvolutionState state)
        {
        }



    public Population preBreedingExchangePopulation(EvolutionState state)
        {
        // exchange individuals between subpopulations
        // BUT ONLY if the modulo and offset are appropriate for this
        // generation (state.generation)
        // I am responsible for returning a population.  This could
        // be a new population that I created fresh, or I could modify
        // the existing population and return that.

        // for each of the islands that sends individuals
        for( int i = 0 ; i < exchangeInformation.length ; i++ )
            {

            // else, check whether the emigrants need to be sent
            if( ( state.generation >= exchangeInformation[i].offset ) &&
                ( ( exchangeInformation[i].modulo == 0 ) ||
                  ( ( ( state.generation - exchangeInformation[i].offset ) % exchangeInformation[i].modulo ) == 0 ) ) )
                {

                // send the individuals!!!!

                // for each of the islands where we have to send individuals
                for( int x = 0 ; x < exchangeInformation[i].numDest ; x++ )
                    {

                    if (chatty) state.output.message( "Sending the emigrants from subpopulation " +
                                                      i + " to subpopulation " +
                                                      exchangeInformation[i].destinations[x] );

                    // select "size" individuals and send then to the destination as emigrants
                    exchangeInformation[i].immigrantsSelectionMethod.prepareToProduce( state, i, 0 );
                    for( int y = 0 ; y < exchangeInformation[i].size ; y++ ) // send all necesary individuals
                        {
                        // get the index of the immigrant
                        int index = exchangeInformation[i].immigrantsSelectionMethod.produce( i, state, 0 );
                        // copy the individual to the mailbox of the destination subpopulation
                        immigrants[ exchangeInformation[i].destinations[x] ]
                            [ nImmigrants[ exchangeInformation[i].destinations[x] ] ] =
                            state.population.subpops[ i ].individuals[ index ];
                        // increment the counter with the number of individuals in the mailbox
                        nImmigrants[ exchangeInformation[i].destinations[x] ]++;
                        }
                    exchangeInformation[i].immigrantsSelectionMethod.finishProducing( state, i, 0 ); // end the selection step
                    }
                }
            }

        return state.population;

        }


    public Population postBreedingExchangePopulation(EvolutionState state)
        {
        // receiving individuals from other islands
        // same situation here of course.

        for( int x = 0 ; x < nImmigrants.length ; x++ )
            {

            if( nImmigrants[x] > 0 && chatty )
                {
                state.output.message( "Immigrating " +  nImmigrants[x] +
                                      " individuals from mailbox for subpopulation " + x );
                }
                
            int len = state.population.subpops[x].individuals.length;
            // double check that we won't go into an infinite loop!
            if ( nImmigrants[x] >= state.population.subpops[x].individuals.length )
                state.output.fatal("Number of immigrants ("+nImmigrants[x] +
                                   ") is larger than subpopulation #" + x + "'s size (" +
                                   len +").  This would cause an infinite loop in the selection-to-die procedure.");

            boolean[] selected = new boolean[ len ];
            int[] indeces = new int[ nImmigrants[x] ];
            for( int i = 0 ; i < selected.length ; i++ )
                selected[i] = false;
            exchangeInformation[x].indsToDieSelectionMethod.prepareToProduce( state, x, 0 );
            for( int i = 0 ; i < nImmigrants[x] ; i++ )
                {
                do {
                    indeces[i] = exchangeInformation[x].indsToDieSelectionMethod.produce( x, state, 0 );
                    } while( selected[indeces[i]] );
                selected[indeces[i]] = true;
                }
            exchangeInformation[x].indsToDieSelectionMethod.finishProducing( state, x, 0 );

            for( int y = 0 ; y < nImmigrants[x] ; y++ )
                {

                // read the individual
                state.population.subpops[x].individuals[ indeces[y] ] = immigrants[x][y];

                // reset the evaluated flag (the individuals are not evaluated in the current island */
                state.population.subpops[x].individuals[ indeces[y] ].evaluated = false;

                }

            // reset the number of immigrants in the mailbox for the current subpopulation
            // this doesn't need another synchronization, because the thread is already synchronized
            nImmigrants[x] = 0;
            }

        return state.population;

        }

    /** Called after preBreedingExchangePopulation(...) to evaluate whether or not
        the exchanger wishes the run to shut down (with ec.EvolutionState.R_FAILURE).
        This would happen for two reasons.  First, another process might have found
        an ideal individual and the global run is now over.  Second, some network
        or operating system error may have occurred and the system needs to be shut
        down gracefully.
        This function does not return a String as soon as it wants to exit (another island found
        the perfect individual, or couldn't connect to the server). Instead, it sets a flag, called
        message, to remember next time to exit. This is due to a need for a graceful
        shutdown, where checkpoints are working properly and save all needed information. */
    public String runComplete(EvolutionState state)
        {
        return null;
        }

    /** Closes contacts with other processes, if that's what you're doing.  Called at the end of an evolutionary run. result is either ec.EvolutionState.R_SUCCESS or ec.EvolutionState.R_FAILURE, indicating whether or not an ideal individual was found. */
    public void closeContacts(EvolutionState state, int result)
        {
        }

    }
