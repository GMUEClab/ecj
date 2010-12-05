/*
  Copyright 20010 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.gp.ge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ec.gp.*;
import ec.*;
import ec.vector.*;
import ec.util.*;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * GESpecies.java
 *
 * Created: Sun Dec  5 11:33:43 EST 2010
 * By: Eric Kangas, Joseph Zelibor III, Houston Mooers, and Sean Luke
 *
 */

/**
 * <p>GESpecies generates GPIndividuals from GEIndividuals through the application of a grammar parse tree
 * computed by the GrammarParser.
 *
 * <p>GESpecies uses a <b>GrammarParser</b> to do its dirty work.  This parser's job is to take a grammar (in the form of a BufferedReader) 
 * and convert it to a tree of GrammarNodes which define the parse tree of the grammar. The GESpecies then interprets his parse tree
 * according to the values in the GEIndividual to produce the equivalent GPIndividual, which is then evaluated.
 * 
 * <p>To do this, GESpecies relies on a subsidiary GPSpecies which defines the GPIndividual and various GPFunctionSets from which to
 * build the parser.  This is a grand hack -- the GPSpecies does not know it's being used this way, and so we must provide various dummy
 * parameters to keep the GPSpecies happy even though they'll never be used.
 *
 * <p>If you are daring, you can replace the GrammarParser with one of your own to customize the parse structure and grammar.
 * 
 * <p><b>ECJ's Default GE Grammar</b>  GE traditionally can use any grammar, and builds parse trees from that.  For simplicity, and in order to
 * remain as compatable as possible with ECJ's existing GP facilities (and GP tradition), ECJ only uses a single Lisp-like grammar which
 * generates standard ECJ trees.  This doesn't lose much in generality as the grammar is quite genral.
 * 
 * <p>The grammar assumes that expansion points are enclosed in &lt;> and functions are enclosed in ().  For example:
 *
 * <p><tt>
 * # This is a comment
 * &lt;prog> ::= &lt;op><br>
 * &lt;op> ::= (if-food-ahead &lt;op> &lt;op>)<br>
 * &lt;op> ::=  (progn2 &lt;op> &lt;op>)<br>
 * &lt;op> ::= (progn3 &lt;op> &lt;op> &lt;op>)<br>
 * &lt;op> ::= (left) | (right) | (move)<br>
 * </tt>
 * 
 * <p>alternatively the grammar could also be writen in the following format:
 * 
 * <p><tt>
 * &lt;prog> ::= &lt;op><br>
 * &lt;op> ::= (if-food-ahead &lt;op> &lt;op>) | (progn2 &lt;op> &lt;op>) | (progn3 &lt;op> &lt;op> &lt;op>) | (left) | (right) | (move)<br>
 * </tt>
 *
 * <p>Note that you can use several lines to define the same grammar rule: for example, <tt>&lt;op></tt> was defined by several lines when
 * it could have consisted of several elements separated by vertical pipes ( <tt>|</tt> ).  Either way is fine, or a combination of both.
 *
 * <p>GPNodes are included in the grammar by using their name.  This includes ERCs, ADFs, ADMs, and ADFArguments, which should all work just fine.
 * For example, since most ERC GPNodes are simply named "ERC", if you have only one ERC GPNode in your function set, you can just use <tt>(ERC)</tt>
 * in your grammar.
 *
 * <p>Once the gammar file has been created and setup has been run trees can the be created using the genome (chromosome) of a GEIndividual.
 * A genome of an individual is an array of random integers each of which are one byte long.  These numbers are used when a decision point 
 * (a rule having more that one choice) is reached within the grammar.  Once a particular gene (index) in the genome has been used it will 
 * not be used again (this may change) when creating the tree.
 *
 * <p>For example:<br>
 * number of chromosomes used = 0<br>
 * genome = {23, 654, 86}<br>
 * the current rule we are considering is &lt;op>.<br>
 * %lt;op> can map into one of the following: (if-food-ahead &lt;op> &lt;op>) | (progn2 &lt;op> &lt;op>) | (progn3 &lt;op> &lt;op> &lt;op>) 
 * | (left) | (right) | (move)<br>
 * Since the rule &lt;op> has more than one choice that it can map to, we must consult the genome to decide which choice to take.  In this case
 * the number of chromosomes used is 0 so genome[0] is used and number of chromosomes used is incremented.  Since values in the genome can
 * be negitive values they are offset by 128 (max negitive of a byte) giving us a value from 0-255.  A modulus is performed on this resulting 
 * number by the number of choices present for the given rule.  In the above example since we are using genome[0] the resulting operation would 
 * look like: 23+128=151, number of choices for &lt;op> = 6, 151%6=1 so we use choices[1] which is: (progn2 &lt;op> &lt;op>).  If all the genes
 * in a genome are used and the tree is still incompete an invalid tree error is returned.
 *
 * <p>Each node in the tree is a GPNode and trees are constructed depth first. 
 *
 *
 * <p><b>Parameters</b><br>
 * <table>
 * <tr><td valign=top><i>base.</i><tt>file</tt><br>
 * <font size=-1>String</font></td>
 * <td valign=top>(the file is where the rules of the gammar are stored)</td></tr>
 * 
 * <tr><td valign=top><i>base.</i><tt>gp-species</tt><br>
 * <font size=-1>classname, inherits and != ec.gp.GPSpecies</font></td>
 * <td valign=top>(the GPSpecies subservient to the GESpecies)</td></tr>
 *
 * <tr><td valign=top><i>base.</i><tt>parser</tt><br>
 * <font size=-1>classname, inherits and != ec.gp.ge.GrammarParser</font></td>
 * <td valign=top>(the GrammarParser used by the GESpecies)</td></tr>
 *
 * </table>
 *
 * <p><b>Default Base</b><br>
 * ec.gp.ge.GESpecies
 *
 * @author Joseph Zelibor III, Eric Kangas, Houston Mooers, and Sean Luke
 * @version 1.0 
 */
 
public class GESpecies extends IntegerVectorSpecies
    {
    public static final String P_GESPECIES = "species";
    public static final String P_FILE = "file";
    public static final String P_GPSPECIES = "gp-species";
    public static final String P_PARSER = "parser";
	
    /* Return value which denotes that the tree has grown too large. */
    public static final int BIG_TREE_ERROR = -1;
	
	/** The GPSpecies subsidiary to GESpecies. */
    public GPSpecies gpspecies;

	/** All the ERCs created so far. */
    public HashMap ERCBank;

	/** The parsed grammars. */
    public GrammarRuleNode[] grammar;

	/** The prototypical parser used to parse the grammars. */
    public GrammarParser parser_prototype;

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state, base);

        Parameter p = base;
        Parameter def = defaultBase();

        p = base.push(P_GPSPECIES);
        gpspecies = (GPSpecies) (state.parameters.getInstanceForParameterEq(p, def.push(P_GPSPECIES), GPSpecies.class));
        gpspecies.setup(state, p);

        // check to make sure that our individual prototype is a GPIndividual
        if (!(i_prototype instanceof ByteVectorIndividual))
            {
            state.output.fatal("The Individual class for the Species " + getClass().getName() + " is must be a subclass of ec.gp.ge.GEIndividual.", base);
            }

        ERCBank = new HashMap();

        // load the grammars, one per ADF tree
        GPIndividual gpi = (GPIndividual) (gpspecies.i_prototype);
        GPTree[] trees = gpi.trees;
        int numGrammars = trees.length;

        parser_prototype = (GrammarParser) (state.parameters.getInstanceForParameterEq(base.push(P_PARSER), def.push(P_PARSER), GrammarParser.class));

        grammar = new GrammarRuleNode[numGrammars];
        for(int i = 0; i < numGrammars; i++)
            {
            p = base.push(P_FILE);
            def = defaultBase();
                        
            File grammarFile = state.parameters.getFile(p, def.push(P_FILE).push("" + i));
            if(grammarFile == null)
                {
                state.output.fatal("Error retrieving grammar file(s): " + def.toString() + "."+ P_FILE + "." + i + " is undefined.");
                }

            try
                {
                GPFunctionSet gpfs = trees[i].constraints((GPInitializer) state.initializer).functionset;
                GrammarParser grammarparser = (GrammarParser)(parser_prototype.clone());
                grammar[i] = grammarparser.parseRules(state, new BufferedReader(new FileReader(grammarFile)), gpfs);
                }
            catch (FileNotFoundException e)
                {
                state.output.fatal("Error retrieving grammar file(s): " + def.toString() + "."+ P_FILE + "." + i + " does not exist or cannot be opened.");
                }
            }
        }


    /**
     * creates all of an individual's trees
     * @param state Evolution state
     * @param trees array of trees for the individual
     * @param ind the GEIndividual
     * @param threadnum tread number
     * @return number of chromosomes consumed
     */
    public int makeTrees(EvolutionState state, GEIndividual ind, GPTree[] trees, int threadnum)
        {
        int position = 0;

        for (int i = 0; i < trees.length; i++)
            {
            //cannot complete one of the trees with the given chromosome
            if(position < 0)
                return BIG_TREE_ERROR;

            position = makeTree(state, ind, trees[i], position, i, threadnum);
            }

        return position;
        }

    /**
     * makeTree, edits the tree that its given by adding a root (and all subtrees attached)
     * @param state
     * @param ind
     * @param tree
     * @param position 
     * @param treeNum
     * @param threadnum
     * @return the number of chromosomes used, or an BIG_TREE_ERROR sentinel value.
     */
    public int makeTree(EvolutionState state, GEIndividual ind, GPTree tree, int position, int treeNum, int threadnum)
        {
        int[] countNumberOfChromosomesUsed = {  position  };  // hack, use an array to pass an extra value
        byte[] genome = ind.genome;
        GPFunctionSet gpfs = tree.constraints((GPInitializer) state.initializer).functionset;
        GPNode root;

        try // get the tree, or return an error.
            {
            root = makeSubtree(countNumberOfChromosomesUsed, genome, state, gpfs, grammar[treeNum], treeNum, threadnum);
            } 
        catch (BigTreeException e)
            {
            return BIG_TREE_ERROR;
            }

        if(root == null)
            {
            state.output.fatal("Invalid tree: tree #" + treeNum);
            }

        root.parent = tree;
        tree.child = root;
        return countNumberOfChromosomesUsed[0];
        }

    // thrown by makeSubtree when chromosome is not large enough for the generated tree.
    class BigTreeException extends RuntimeException { static final long serialVersionUID = 1L; }

    GPNode makeSubtree(int[] index, byte[] genome, EvolutionState es, GPFunctionSet gpfs, GrammarRuleNode rule, int treeNum, int threadnum)
        {
        //have we exceeded the length of the genome?  No point in going further.
        if (index[0] >= genome.length)
            {
            throw new BigTreeException();
            }

        //expand the rule with the chromosome to get a body element
        int i;

        //key for ERC hashtable look ups is the current index within the genome
        int key = genome[index[0]];

        //non existant rule got passed in
        if (rule == null)
            {
            es.output.fatal("An undefined rule exists within the grammar.");
            }

        //more than one rule to consider, pick one based off the genome, and consume the current gene
        if (rule.getNumChoices() > 1)
            {
            //casting to an int should be ok since the biggest these genes can be is a byte
            i = ((genome[index[0]]) - ((int)(this.minGene(index[0])))) % rule.getNumChoices();
            index[0]++;
            }
        //only 1 rule to consider
        else
            {
            i = 0;
            }               
        GrammarNode choice = rule.getChoice(i);         

        // if body is another rule head
        //look up rule
        if(choice instanceof GrammarRuleNode)
            {
            GrammarRuleNode nextrule = (GrammarRuleNode) choice;
            return makeSubtree(index, genome, es, gpfs, nextrule, treeNum, threadnum);
            }                               
        else //handle functions
            {
            GrammarFunctionNode funcgrammarnode = (GrammarFunctionNode) choice;

            GPNode validNode = funcgrammarnode.getGPNodePrototype();

            int numChildren = validNode.children.length;
            //index 0 is the node itself
            int numChildrenInGrammar = funcgrammarnode.getNumArguments();

            //does the grammar contain the correct amount of children that the GPNode requires
            if (numChildren != numChildrenInGrammar)
                {
                es.output.fatal("GPNode " + validNode.toStringForHumans() + " requires " + numChildren + " children.  "
                    + numChildrenInGrammar + " children found in the grammar.");
                }

            //check to see if it is an ERC node
            if (validNode instanceof ERC)
                {                
                validNode = obtainERC(es, key, genome, threadnum, validNode);
                }
            //non ERC node
            else
                {
                validNode = validNode.lightClone();
                }

            //get the rest.
            for (int j = 0, childNumber = 0; j < funcgrammarnode.getNumArguments(); j++)
                {
                //get and link children to the current GPNode
                validNode.children[childNumber] = makeSubtree(index, genome, es, gpfs, (GrammarRuleNode)funcgrammarnode.getArgument(j), treeNum, threadnum);
                if (validNode.children[childNumber] == null)
                    {
                    return null;
                    }
                childNumber++;
                }
            return validNode;
            }
        }

    /** Loads an ERC from the ERCBank given the value in the genome.  If there is no such ERC, then one is created and randomized, then added to the bank.
		The point of this mechanism is to enable ERCs to appear in multiple places in a GPTree. */
    public GPNode obtainERC(EvolutionState state, int key, byte[] genome, int threadnum, GPNode node)
        {
        ArrayList ERCList = (ArrayList) (ERCBank.get(new Integer(key)));

        if (ERCList == null)
            {
            ERCList = new ArrayList();
            ERCBank.put(new Integer(key), ERCList);
            }

        GPNode dummy = null;

        // search array list for an ERC of the same type we want
        for (int i = 0; i < ERCList.size(); i++)
            {
            dummy = (GPNode) ERCList.get(i);

            // ERC was found inside the arraylist
            if (dummy.nodeEquivalentTo(node))
                {
                return dummy.lightClone();
                }
            }

        // erc was not found in the array list lets make one
        node = node.lightClone();
        node.resetNode(state, threadnum);
        ERCList.add(node);

        return node;
        }

    public Object clone()
        {
        GESpecies other = (GESpecies) (super.clone());
        other.gpspecies = (GPSpecies) (gpspecies.clone());
        return other;
        }

    public Parameter defaultBase()
        {
        return GEDefaults.base().push(P_GESPECIES);
        }

    /** Returns the number of elements consumed from the GEIndividual array to produce
        the tree, else returns -1 if an error occurs, specifically if all elements were
        consumed and the tree had still not been completed. */
    public int consumed(EvolutionState state, GEIndividual ind, int threadnum)
        {
        // create a dummy individual
        GPIndividual newind = ((GPIndividual) (gpspecies.i_prototype)).lightClone();

        // do the mapping and return the number consumed
        return makeTrees(state, ind, newind.trees, threadnum);
        }

    /** Returns a dummy GPIndividual with a single tree which was built by mapping
        over the elements of the given GEIndividual.  Null is returned if an error occurs,
        specifically, if all elements were consumed and the tree had still not been completed. */
    public GPIndividual map(EvolutionState state, GEIndividual ind, int threadnum)
        {
        // create a dummy individual
        GPIndividual newind = ((GPIndividual) (gpspecies.i_prototype)).lightClone();

        // Do NOT initialize its trees

        // Set the fitness to the ByteVectorIndividual's fitness
        newind.fitness = ind.fitness;
        newind.evaluated = false;

        // Set the species to me
        newind.species = gpspecies;

        // do the mapping
        if (makeTrees(state, ind, newind.trees, threadnum) < 0)  // error
            return null;
        else
			return newind;
        }
    }