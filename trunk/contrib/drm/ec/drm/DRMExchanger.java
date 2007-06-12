/** Some code taken from Sean Luke's ECJ and Màrk Jelasity's DRM.
 * Copyright 2006 Alberto Cuesta Cañada, licensed under the Academic Free License.
 * @author Alberto Cuesta Cañada
 * @version 0.1 
 */

package ec.drm;

import java.util.*;

import drm.agentbase.*;
import ec.*;
import ec.drm.peerselect.*;
import ec.util.Parameter;

/** Allows exchanging of Individuals through DRM links */
public class DRMExchanger extends Exchanger{
	
	/** Serialization identificator */
	private static final long serialVersionUID = 1L;
	
	public static final String D_SENDER = "sender";
	
	public static final String D_SUBPOP = "subpop";
	
	public static final String D_MIGRANT = "migrant";
	
    /** The selection method for sending individuals to other islands. */
    public static final String P_SELECT_METHOD = "select";

    /** The selection method for deciding individuals to be replaced by immigrants. */
    public static final String P_SELECT_TO_DIE_METHOD = "select-to-die";

    /** The selection method for deciding peers to send migrators to. */
    public static final String P_SELECT_PEER_METHOD = "select-peer";
    
    /** Quantity of migrators per migration */
    public static final String P_MIGRATION_SIZE = "migration-size";

    /** Number of iterations per migration */
    public static final String P_FREQUENCY = "frequency";

    /** Skew in the migration, useful if you want to use the collective for other purposes also */
    public static final String P_OFFSET = "offset";
    
    /** should we use the collective to distribute individuals? */
    //public static final String P_COLLECTIVE = "use-collective";
    
    /** Should the root be a target for migrations? */
    public static final String P_WORKING_ROOT = "drm.working-root";
    
    /** Place where the migration messages containing the migrators wait for being processed. */
    protected transient ArrayList mailbox;
	
    /** Known peers to this node and their contributions */
    protected transient Address[] peers;
    
    /** the selection method for emigrants */
    // SERIALIZE
    public SelectionMethod immigrantsSelectionMethod;

    /** the selection method for individuals to be replaced by immigrants */
    // SERIALIZE
    public SelectionMethod indsToDieSelectionMethod;

    /** The selection method for deciding peers to send migrators to */
    // SERIALIZE
    public PeerSelectionMethod peerSelectionMethod;
    
    /** Quantity of migrators per migration */
    public int migrationsize;
    
    /** Number of iterations per migration */
    public int frequency;
    
    /** Migration skew */
    public int offset;
    
    /**  */
    //public boolean use_collective;
    
    /** Should the root be a target for migrations? */
    public boolean working_root;
    
    public void reset(final EvolutionState state){
    	//if (!(state instanceof EvolutionAgent))
    	//	state.output.fatal("DRMExchanger requires an EvolutionAgent, reset failed",null,null);
    	
    	mailbox = new ArrayList();
    	
    	reinitializeContacts(state);
    }
    
	public void setup(final EvolutionState state, final Parameter base) {
    	if (!(state instanceof EvolutionAgent))
    		state.output.fatal("DRMExchanger requires an EvolutionAgent, reset failed",null,null);

        migrationsize = state.parameters.getIntWithDefault(base.push(P_MIGRATION_SIZE), null, 1);
        frequency = state.parameters.getIntWithDefault(base.push(P_FREQUENCY), null, 1);
        offset = state.parameters.getIntWithDefault(base.push(P_OFFSET), null, 0);
        //use_collective = state.parameters.getBoolean(base.push(P_COLLECTIVE), null, false);
    	
        Parameter p;
    	// Setup the selection methods
        p = base.push( P_SELECT_METHOD );
        immigrantsSelectionMethod = (SelectionMethod)
            state.parameters.getInstanceForParameter( p, null, ec.SelectionMethod.class );
        immigrantsSelectionMethod.setup( state, base );

        // setup the die selection method
        p = base.push( P_SELECT_TO_DIE_METHOD );
        if( state.parameters.exists( p ) )
            indsToDieSelectionMethod = (SelectionMethod)
                state.parameters.getInstanceForParameter( p, null, ec.SelectionMethod.class );
        else // use RandomSelection
            indsToDieSelectionMethod = new ec.select.RandomSelection();
        indsToDieSelectionMethod.setup( state, base );
        
        // setup the peer selection method
        p = base.push( P_SELECT_PEER_METHOD );
        if( state.parameters.exists( p ) )
            peerSelectionMethod = (PeerSelectionMethod)
                state.parameters.getInstanceForParameter( p, null, PeerSelectionMethod.class );
        else // use RandomSelection
        	peerSelectionMethod = new RandomPeerSelection();
        peerSelectionMethod.setup( state, base );
        
        working_root = state.parameters.getBoolean(new Parameter(P_WORKING_ROOT),null,false);
        
        mailbox = new ArrayList();
	}

	/** Stores incoming migration data into the mailbox for future use */
	public void storeData(ExchangerData data){
		synchronized( mailbox ){
			mailbox.add(data);
		}
	}
	
    /** Gets peers from the collective. */
    public void initializeContacts(final EvolutionState state){
    	peers = ((EvolutionAgent)state).getPeerAddresses();
        return;
    }

    /** Gets peers from the collective. */
    public void reinitializeContacts(final EvolutionState state){
    	initializeContacts(state);
        return;
    }

    /** Selects individuals from the population and sends them through the network. */
    public Population preBreedingExchangePopulation(final EvolutionState state){
    	if (frequency == 0) return state.population;
    	if(state.generation % frequency != offset) return state.population;
    	
    	EvolutionAgent agent = (EvolutionAgent)state;
    	
    	reinitializeContacts(state);
    	
        Address peer = peerSelectionMethod.select(state, peers);
        if(peer == null){
        	state.output.message("Couldn't find a suitable target for migration.");
        	return state.population;
        }
        // This one prevents sending migrators to itself.
        if(peer.name.equals(agent.getName()))
        	return state.population;
        // This is only to avoid sending migrators to the root agent.
        if(!working_root && 
        		(peer.getHost().equals(agent.getRootAddress().getHost()) 
        		&& peer.port == agent.getRootAddress().port))
        	return state.population;
        	
	    

	    // for each of the subpopulations
	    for( int subpop = 0 ; subpop < state.population.subpops.length ; subpop++ ){

	    	// select "size" individuals and send then to the destination as emigrants
	    	Individual[] inds = new Individual[migrationsize];
	    	immigrantsSelectionMethod.prepareToProduce( state, subpop, 0 );
	    	immigrantsSelectionMethod.produce(migrationsize,migrationsize, 0, subpop, inds, state, 0);
	    	
	    	ExchangerData exData = new ExchangerData(new Address(agent.getName()),subpop,inds);
	    	
	    	/*if(use_collective){
	    		state.output.message( "Contributed " + migrationsize + " emigrants");
	    		data.put("type", EvolutionAgent.MIGRATION_MESSAGE);
	    		agent.setContribution(data);
	    	}else{*/
	    		state.output.message( "Sending the emigrants to island " + peer.toString());
	    		agent.fireMessage(peer,EvolutionAgent.M_MIGRATION,exData); // send everything (should make sure the migrants arrive?)
	    	//}
        }
        return state.population;
    }

    /** Takes individuals from the mailbox and injects them into the population. */
    public Population postBreedingExchangePopulation(final EvolutionState state){
    	if (frequency == 0) return state.population;
    	if(state.generation % frequency != offset) return state.population;
    	
    	ExchangerData exData;
    	int n,s;
    	int[] replace;
    	
    	while( mailbox.size() > 0){
    		synchronized( mailbox ){
    			exData = (ExchangerData)mailbox.get(0);
    			mailbox.remove(0);
    		}
    		n = exData.individuals.length;
        	s = exData.subpop;
        	replace = new int[n];
        	
        	state.output.message( "Immigrating " + n + " individuals from " + exData.sender.name + " for subpopulation " + s );

        	// Select individuals to be replaced from the population
        	indsToDieSelectionMethod.prepareToProduce( state, s, 0 );
            for( int i = 0 ; i < n ; i++ )
            	replace[i] = indsToDieSelectionMethod.produce(s, state, 0 );
            	
            indsToDieSelectionMethod.finishProducing( state, s, 0 );
            // Change expelled individuals with the newcomers
            for( int i = 0 ; i < n ; i++ ){
                state.population.subpops[s].individuals[replace[i]] = exData.individuals[i];
                state.population.subpops[s].individuals[replace[i]].evaluated = false; 
            }
    	}
        return state.population;
    }

    /** Doesn't do anything. Suicide() takes care. */
    public void closeContacts(final EvolutionState state, final int result){
        // don't care
        return;
    }

    /** Always returns null */
    public String runComplete(final EvolutionState state){
        return null;
    }
}