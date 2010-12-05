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
 * GrammarParser.java
 *
 * Created: Sun Dec  5 11:33:43 EST 2010
 * By: Eric Kangas, Joseph Zelibor III, Houston Mooers, and Sean Luke
 *
 */

/**
 * <p>GESpecies is used to take a grammar from a file and to create a tree for a GEIndividual based on the rules of the
 * given grammar.  
 * 
 * <p><b>Grammar Files:</b>
 * Within the grammar file non-terminals must be enclosed in &lt;> and terminals must be enclosed in ().
 * <p>For example:
 *
 * <p><tt>
 * &lt;prog> ::= &lt;op><br>
 * &lt;op> ::= (if-food-ahead &lt;op> &lt;op>)<br>
 * &lt;op> ::=  (progn2 &lt;op> &lt;op>)<br>
 * &lt;op> ::= (progn3 &lt;op> &lt;op> &lt;op>)<br>
 * &lt;op> ::= (left) | (right) | (move)<br>
 * </tt>
 *
 * 
 * <p>alternatively the grammar could also be writen in the following format:
 * 
 * <p><tt>
 * &lt;prog> ::= &lt;op><br>
 * &lt;op> ::= (if-food-ahead &lt;op> &lt;op>) | (progn2 &lt;op> &lt;op>) | (progn3 &lt;op> &lt;op> &lt;op>) | (left) | (right) | (move)<br>
 * </tt>
 *
 * <p>Rulenames are non-terminals and as such must be enclosed in &lt;> as shown in the above example grammars.  Rulenames
 * must be separated from their corresponding choices by ::= (also shown in the above examples).  If a rule has more that one choice
 * associated with it then one of three ways can be implmented for writing the grammar file: option 1 (example1): each 
 * different choice is writen on a sepearate line with the same rule name, option 2 (example2): each different 
 * choice is writen on the same line seperated by |, or option 3: a combination of option 1 and option 2 (also shown in example1).</p>
 *
 * <p>New Addition (8/17/10): ERC's.  If you wish to include ERC's in your grammar use the following format: (ERC).  Note that an ERC is a terminal.</p>
 * <p>New Addition (8/17/10): commented lines can now be placed in the grammar files.  Commented lines start with a #.  For example: #this is a comment.
 * Note: Comments must be on a line by themselves</p>
 *
 * <p>The rules object takes this form:<br>
 * A rule name (String).<br>
 * An array of choices (ArrayList of Strings)<br>
 *
 * <p>Using the above grammar a rule would look like this:
 * <p><tt>
 * rulename = &lt;op><br>
 * choices = {(if-food-ahead &lt;op> &lt;op>), (progn2 &lt;op> &lt;op>), (progn3 &lt;op> &lt;op> &lt;op>), (left), (right), (move)<br>
 * </tt>
 *
 * <p>Each rule is then added to a hashmap of rules to be used for deriving the trees of each GEIndividual.
 *
 * <p>Note: setup() must be run before trying to use any of the other methods in this class.
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
 * <td valign=top>(the file is where the rules of the gammar are stored)</td></tr>
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
    public static final String P_FILE = "file";
    public static final String P_GESPECIES = "species";
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