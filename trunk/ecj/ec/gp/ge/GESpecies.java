/*
  Copyright 20010 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.gp.ge;

import java.io.*;
import java.util.*;

import ec.gp.*;
import ec.*;
import ec.vector.*;
import ec.util.*;

import java.util.regex.*;

/*
 * GESpecies.java
 *
 * Created: Sun Dec  5 11:33:43 EST 2010
 * By: Eric Kangas, Joseph Zelibor III, Houston Mooers, and Sean Luke
 *
 */

/**
 * <p>GESpecies generates GPIndividuals from GEIndividuals through the application of a grammar parse graph
 * computed by the GrammarParser.
 *
 * <p>GESpecies uses a <b>GrammarParser</b> to do its dirty work.  This parser's job is to take a grammar (in the form of a BufferedReader)
 * and convert it to a tree of GrammarNodes which define the parse graph of the grammar. The GESpecies then interprets his parse graph
 * according to the values in the GEIndividual to produce the equivalent GPIndividual, which is then evaluated.
 *
 * <p>To do this, GESpecies relies on a subsidiary GPSpecies which defines the GPIndividual and various GPFunctionSets from which to
 * build the parser.  This is a grand hack -- the GPSpecies does not know it's being used this way, and so we must provide various dummy
 * parameters to keep the GPSpecies happy even though they'll never be used.
 *
 * <p>If you are daring, you can replace the GrammarParser with one of your own to customize the parse structure and grammar.
 *
 * <p><b>ECJ's Default GE Grammar</b>  GE traditionally can use any grammar, and builds parse graphs from that.  For simplicity, and in order to
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
 * A genome of an individual is an array of random integers each of which are one int long.  These numbers are used when a decision point
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
 * be negitive values they are offset by 128 (max negitive of a int) giving us a value from 0-255.  A modulus is performed on this resulting
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
 * <font size=-1>classname, inherits and != ge.GrammarParser</font></td>
 * <td valign=top>(the GrammarParser used by the GESpecies)</td></tr>
 *
 * </table>
 *
 * <p><b>Default Base</b><br>
 * ge.GESpecies
 *
 * @author Joseph Zelibor III, Eric Kangas, Houston Mooers, and Sean Luke
 * @version 1.0
 */

public class GESpecies extends IntegerVectorSpecies
    {
    private static final long serialVersionUID = 1;

    public static final String P_GESPECIES = "species";
    public static final String P_FILE = "file";
    public static final String P_GPSPECIES = "gp-species";
    public static final String P_PARSER = "parser";
    public static final String P_PASSES = "passes";
    public static final String P_INITSCHEME = "init-scheme" ;

    /* Return value which denotes that the tree has grown too large. */
    public static final int BIG_TREE_ERROR = -1;

    /** The GPSpecies subsidiary to GESpecies. */
    public GPSpecies gpspecies;

    /**
       All the ERCs created so far, the ERCs are mapped as,
       "key --> list of ERC nodes", where the key = (genome[i] - minGene[i]);
       The ERCBank is "static", beacause we need one identical copy
       for all the individuals; Moreover, this copy may be sent to
       other sub-populations as well.
    */
    public HashMap ERCBank;

    /** The parsed grammars. */
    public GrammarRuleNode[] grammar;
        

    /** The number of passes permitted through the genome if we're wrapping.   Must be >= 1. */
    public int passes;

    public String initScheme = "default" ;

    /** The prototypical parser used to parse the grammars. */
    public GrammarParser parser_prototype;

    /** Parser for each grammar -- khaled */ 
    public GrammarParser[] grammarParser = null ; 

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state, base);

        Parameter p = base;
        Parameter def = defaultBase();

        p = base.push(P_GPSPECIES);
        gpspecies = (GPSpecies) (state.parameters.getInstanceForParameterEq(p,
                def.push(P_GPSPECIES), GPSpecies.class));
        gpspecies.setup(state, p);

        // check to make sure that our individual prototype is a GPIndividual
        if (!(i_prototype instanceof IntegerVectorIndividual))
            state.output.fatal("The Individual class for the Species "
                + getClass().getName()
                + " is must be a subclass of ge.GEIndividual.", base);
        ERCBank = new HashMap();

        // load the grammars, one per ADF tree
        GPIndividual gpi = (GPIndividual) (gpspecies.i_prototype);
        GPTree[] trees = gpi.trees;
        int numGrammars = trees.length; // no. of trees = no. of grammars

        parser_prototype = (GrammarParser) (state.parameters.getInstanceForParameterEq(
                base.push(P_PARSER),
                def.push(P_PARSER),
                GrammarParser.class));
        grammar = new GrammarRuleNode[numGrammars];
        grammarParser = new GrammarParser[numGrammars] ;
        for(int i = 0; i < numGrammars; i++)
            {
            p = base.push(P_FILE);
            def = defaultBase();

            // File grammarFile = state.parameters.getFile(p, def.push(P_FILE).push("" + i));
            InputStream grammarFile = state.parameters.getResource(p,
                def.push(P_FILE).push("" + i));
            if(grammarFile == null)
                state.output.fatal("Error retrieving grammar file(s): "
                    + def.toString() + "."+ P_FILE + "." + i
                    + " is undefined.");
            GPFunctionSet gpfs =
                trees[i].constraints((GPInitializer) state.initializer).functionset;
            // now we need different parser object for each of the grammars,
            // why? see GrammarParser.java for details -- khaled
            grammarParser[i] = (GrammarParser)parser_prototype.clone();
            BufferedReader br = new BufferedReader(new InputStreamReader(grammarFile));
            grammar[i] = grammarParser[i].parseRules(state, br, gpfs);
                        
            // Enumerate the grammar tree -- khaled
            grammarParser[i].enumerateGrammarTree(grammar[i]);
            // Generate the predictive parse table -- khaled
            grammarParser[i].populatePredictiveParseTable(grammar[i]);
                        
            try
                {
                br.close();
                }
            catch (IOException e)
                {
                // do nothing
                }
            }

        // get the initialization scheme -- khaled
        initScheme = state.parameters.getString(base.push(P_INITSCHEME), def.push(P_INITSCHEME));
        if( initScheme != null && initScheme.equals("sensible"))
            state.output.warnOnce("Using a \"hacked\" version of \"sensible initialization\"");
        else
            state.output.warnOnce("Using default GE initialization scheme");

        // setup the "passes" parameters
        final int MAXIMUM_PASSES = 1024;

        passes = state.parameters.getInt(base.push(P_PASSES), def.push(P_PASSES), 1);
        if (passes < 1 || passes > MAXIMUM_PASSES)
            state.output.fatal("Number of allowed passes must be >= 1 and <="
                + MAXIMUM_PASSES + ", likely small, such as <= 16.",
                base.push(P_PASSES), def.push(P_PASSES));
        int oldpasses = passes;
        passes = nextPowerOfTwo(passes);
        if (oldpasses != passes)
            state.output.warning("Number of allowed passes must be a power of 2.  Bumping from "
                + oldpasses + " to " + passes,
                base.push(P_PASSES), def.push(P_PASSES));
        }

    int nextPowerOfTwo(int v)
        {
        // if negative or 0, couldn't bump.
        // See http://graphics.stanford.edu/~seander/bithacks.html#RoundUpPowerOf2
        v--;
        v |= v >> 1;
        v |= v >> 2;
        v |= v >> 4;
        v |= v >> 8;
        v |= v >> 16;
        v++;
        return v;
        }

    /**
     * This is an ugly hack to simulate the "Sensible Initialization",
     * First we create a GPIndividual, then reverse-map it to GEIndividuals,
     * We do not need to call IntegerVectorSpecies.newIndividual() since it is overriden
     * by the GPSpecies.newIndividual();
     *
     * Moreover, as in the case for non-identical representations (i,e, GP-GE island
     * models etc,), the grammar rules, tree constraints, ERC's etc, are supposed to be
     * identical across all islands, so we are using the same "gpspecies" inside this class.
     *
     * However, the identicality of the GPTree particulars like grammar, constraints, ADFs,
     * ERC's may not be universally true.
     */
    public Individual newIndividual(final EvolutionState state, int thread)
        {
        GEIndividual gei = null ;
        if(initScheme != null && initScheme.equals("sensible"))
            {
            GPIndividual gpi = (GPIndividual)gpspecies.newIndividual(state, thread);
            gei = reverseMap(state, gpi, thread);
            }
        else
            {
            gei = (GEIndividual)super.newIndividual(state, thread);
            gei.species = this ;
            }
        return gei ;
        }

    /**
     * creates all of an individual's trees
     * @param state Evolution state
     * @param trees array of trees for the individual
     * @param ind the GEIndividual
     * @param threadnum tread number
     * @return number of chromosomes consumed
     */
    public int makeTrees(EvolutionState state, GEIndividual ind, GPTree[] trees,
        int threadnum, HashMap ercMapsForFancyPrint)
        {
        int[] genome = ind.genome ;
        int position = 0 ;

        // We start with one pass, then repeatedly double the genome length and
        // try again until it's big enough. This is simple but very costly in terms of
        // memory so our maximum pass size is MAXIMUM_PASSES, which should be small enough
        // to allow for even pretty long genomes.
        for(int i = 1; i <= passes; i *= 2)  // note i starts at 1
            {
            position = makeTrees(state, genome, trees, threadnum, ercMapsForFancyPrint);
            if (position < 0 && i < passes)  // gotta try again
                {
                // this is a total hack
                int[] old = genome;
                genome = new int[old.length * 2]; 
                System.arraycopy(old, 0, genome, 0, old.length);
                System.arraycopy(old, 0, genome, old.length, old.length);  // duplicate
                }
            }
        return (Math.min(position, ind.genome.length));
        }

    // called by the above
    public int makeTrees(EvolutionState state, int[] genome, GPTree[] trees,
        int threadnum, HashMap ercMapsForFancyPrint)
        {
        int position = 0;

        for (int i = 0; i < trees.length; i++)
            {
            // cannot complete one of the trees with the given chromosome
            if(position < 0)
                return BIG_TREE_ERROR;
            position = makeTree(state, genome, trees[i], position, i, threadnum, ercMapsForFancyPrint);
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
    public int makeTree(EvolutionState state, int[] genome, GPTree tree,
        int position, int treeNum, int threadnum, HashMap ercMapsForFancyPrint)
        {
        // hack, use an array to pass an extra value
        int[] countNumberOfChromosomesUsed = {  position  };

        GPFunctionSet gpfs = tree.constraints((GPInitializer) state.initializer).functionset;
        GPNode root;

        try // get the tree, or return an error.
            {
            root = makeSubtree(countNumberOfChromosomesUsed, genome, state, gpfs,
                grammar[treeNum], treeNum, threadnum, ercMapsForFancyPrint, tree, (byte)0);
            }
        catch (BigTreeException e)
            {
            return BIG_TREE_ERROR;
            }

        if(root == null)
            state.output.fatal("Invalid tree: tree #" + treeNum);

        root.parent = tree;
        tree.child = root;
        return countNumberOfChromosomesUsed[0];
        }

    // thrown by makeSubtree when chromosome is not large enough for the generated tree.
    static class BigTreeException extends RuntimeException
        {
        static final long serialVersionUID = 1L;
        }

    GPNode makeSubtree(int[] index, int[] genome, EvolutionState es, GPFunctionSet gpfs,
        GrammarRuleNode rule, int treeNum, int threadnum, HashMap ercMapsForFancyPrint,
        GPNodeParent parent, byte argposition)
        {
        // have we exceeded the length of the genome?  No point in going further.
        if (index[0] >= genome.length)
            throw new BigTreeException();

        // expand the rule with the chromosome to get a body element
        int i;

        // non existant rule got passed in
        if (rule == null)
            es.output.fatal("An undefined rule exists within the grammar.");
                
        // more than one rule to consider, pick one based off the genome, and consume the current gene
        // avoid mod operation as much as possible
        if (rule.getNumChoices() > 1)
            i = (genome[index[0]] - ((int)this.minGene(index[0]))) % rule.getNumChoices();
        else
            i = 0;
        index[0]++;
        GrammarNode choice = rule.getChoice(i);

        // if body is another rule head
        // look up rule
        if(choice instanceof GrammarRuleNode)
            {
            GrammarRuleNode nextrule = (GrammarRuleNode) choice;
            return makeSubtree(index, genome, es, gpfs, nextrule,
                treeNum, threadnum, ercMapsForFancyPrint, parent, argposition);
            }
        else // handle functions
            {
            GrammarFunctionNode funcgrammarnode = (GrammarFunctionNode) choice;

            GPNode validNode = funcgrammarnode.getGPNodePrototype();

            int numChildren = validNode.children.length;
            // index 0 is the node itself
            int numChildrenInGrammar = funcgrammarnode.getNumArguments();

            // does the grammar contain the correct amount of children that the GPNode requires
            if (numChildren != numChildrenInGrammar)
                {
                es.output.fatal("GPNode " + validNode.toStringForHumans() + " requires "
                    + numChildren + " children.  "
                    + numChildrenInGrammar
                    + " children found in the grammar.");
                }

            // check to see if it is an ERC node
            if (validNode instanceof ERC)
                {
                // have we exceeded the length of the genome?  No point in going further.
                if (index[0] >= genome.length)
                    throw new BigTreeException();

                // ** do we actually need to maintain two vlaues ? key and originalVal ?
                // ** there is no problem if we use the originalVal for both ERCBank and
                // ** ercMapsForFancyPrint, moreover, this will also make the reverse-mapping case
                // ** easier -- khaled

                // these below two lines are from the original code --
                // key for ERC hashtable look ups is the current index within the genome.  Consume it.
                // int key = ((genome[index[0]]) - ((int)(this.minGene(index[0]))));
                // int originalVal = genome[index[0]];

                // this single line is khaled's mod --
                int genomeVal = genome[index[0]];
                index[0]++;
                validNode = obtainERC(es, genomeVal, threadnum, validNode, ercMapsForFancyPrint);
                }
            // non ERC node
            else
                validNode = validNode.lightClone();

            // get the rest.
            for (int j = 0, childNumber = 0; j < funcgrammarnode.getNumArguments(); j++)
                {
                // get and link children to the current GPNode
                validNode.children[childNumber] = makeSubtree(index, genome, es, gpfs,
                    (GrammarRuleNode)funcgrammarnode.getArgument(j), 
                    treeNum, threadnum, ercMapsForFancyPrint, validNode, (byte)childNumber);
                if(validNode.children[childNumber] == null)
                    return null;
                childNumber++;
                }
            validNode.argposition = argposition ;
            validNode.parent = parent ;
            return validNode;
            }
        }

    /**
       Loads an ERC from the ERCBank given the value in the genome.
       If there is no such ERC, then one is created and randomized,
       then added to the bank. The point of this mechanism is to enable
       ERCs to appear in multiple places in a GPTree.
    */
    public GPNode obtainERC(EvolutionState state, int genomeVal, int threadnum, GPNode node, HashMap ercMapsForFancyPrint)
        {
        ArrayList ERCList = (ArrayList) (ERCBank.get(Integer.valueOf(genomeVal)));

        // No such ERC, create a new ERCList.
        if (ERCList == null)
            {
            ERCList = new ArrayList();
            ERCBank.put(new Integer(genomeVal), ERCList);
            }

        GPNode dummy = null;

        // search array list for an ERC of the same type we want
        for (int i = 0; i < ERCList.size(); i++)
            {
            dummy = (GPNode) ERCList.get(i);

            // ERC was found inside the arraylist
            if (dummy.nodeEquivalentTo(node))
                if (ercMapsForFancyPrint != null) ercMapsForFancyPrint.put(new Integer(genomeVal), dummy);                              
            return dummy.lightClone();
            }

        // erc was not found in the array list lets make one
        node = node.lightClone();
        node.resetNode(state, threadnum);
        ERCList.add(node);
        if (ercMapsForFancyPrint != null) ercMapsForFancyPrint.put(new Integer(genomeVal), node);               
        return node;
        }

    public Object clone()
        {
        GESpecies other = (GESpecies) (super.clone());
        other.gpspecies = (GPSpecies) (gpspecies.clone());
        // ERCBank isn't cloned
        // ** I think we need to clone it -- khaled
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
        return makeTrees(state, ind, newind.trees, threadnum, null);
        }

    /**
       Returns a dummy GPIndividual with a single tree which was built by mapping
       over the elements of the given GEIndividual. Null is returned if an error occurs,
       specifically, if all elements were consumed and the tree had still not been completed.
       If you pass in a non-null HashMap for ercMapsForFancyPrint, then ercMapsForFancyPrint will be loaded
       with key->ERCvalue pairs of ERC mappings used in this map.
    */
    public GPIndividual map(EvolutionState state, GEIndividual ind, int threadnum, HashMap ercMapsForFancyPrint)
        {
        // create a dummy individual
        GPIndividual newind = ((GPIndividual) (gpspecies.i_prototype)).lightClone();

        // Do NOT initialize its trees

        // Set the fitness to the IntegerVectorIndividual's fitness
        newind.fitness = ind.fitness;
        newind.evaluated = false;

        // Set the species to me
        newind.species = gpspecies;

        // do the mapping
        if (makeTrees(state, ind, newind.trees, threadnum, ercMapsForFancyPrint) < 0)  // error
            return null;
        else
            return newind;
        }

    /** Flattens an S-expression */
    public List flattenSexp(EvolutionState state, int threadnum, GPTree tree)
        {
        List nodeList = gatherNodeString(state, threadnum, tree.child, 0);
        return nodeList ;
        }

    /** Used by the above function */
    public List gatherNodeString(EvolutionState state, int threadnum, GPNode node, int index)
        {
        List list = new ArrayList();
        if(node instanceof ERC)
            {
            // Now, get the "key" from the "node", NOTE: the "node" is inside an ArrayList,
            // since the ERCBank is mapped as key --> ArrayList of GPNodes.
            // The "key" is the corresponding int value for the ERC.
            list.add(node.name().trim()); // add "ERC"
            // then add the ERC key (original genome value)
            list.add(getKeyFromNode(state, threadnum, node, index).trim());
            }
        else
            list.add(node.toString().trim());
        if(node.children.length > 0)
            {
            for(int i = 0 ; i < node.children.length ; i++)
                {
                index++ ;
                List sublist =
                    gatherNodeString(state, threadnum, node.children[i], index);
                list.addAll(sublist);
                }
            }
        return list ;
        }

    public String getKeyFromNode(EvolutionState state, int threadnum, GPNode node, int index)
        {
        String str = null ;
        // ERCBank has some contents at least.
        if(ERCBank != null && !ERCBank.isEmpty())
            {
            Iterator iter = ERCBank.entrySet().iterator() ;
            while(iter.hasNext())
                {
                Map.Entry pairs = (Map.Entry)iter.next();
                ArrayList nodeList = (ArrayList)pairs.getValue();
                if(Collections.binarySearch(
                        nodeList, 
                        node, 
                        new Comparator(){
                            public int compare(Object o1, Object o2)
                                {
                                if(o1 instanceof GPNode && o2 instanceof GPNode)
                                    return ((GPNode)o1).toString().
                                        compareTo(((GPNode)o2).toString());
                                return 0;
                                }
                            }) >= 0 )
                    {
                    // a match found, save the key, break loop.
                    str = ((Integer)pairs.getKey()).toString();
                    break ;
                    }
                }
            }

        // If a suitable match is not found in the above loop,
        // Add the node in a new list and add it to the ERCBank
        // with a new random value as a key.
        if(str == null)
            {
            // if the hash-map is not created yet
            if(ERCBank == null) ERCBank = new HashMap();
            // if the index is still in the range of minGene.length, use it.
            // otherwise use the minGene[0] value.
            int minIndex = 0 ; if(index < minGene.length) minIndex = index ;
            // now generate a new key
            Integer key = Integer.valueOf((int)minGene[minIndex] 
                + state.random[threadnum].nextInt(
                    (int)(maxGene[minIndex] - minGene[minIndex] + 1)));
            ArrayList list = new ArrayList();
            list.add(node.lightClone());
            ERCBank.put(key, list);
            str = key.toString();
            }
        return str ;
        }
        
    /**
     * The LL(1) parsing algorithm to parse the lisp tree, the lisp tree is actually
     * fed as a flattened list, the parsing code uses the "exact" (and as-is) procedure 
     * described in the dragon book.
     **/
    public int[] parseSexp(ArrayList flatSexp, GrammarParser gp)
        {
        // We can't use array here, because we don't know how we are going to traverse
        // the grammar tree, so the length is not known beforehand.
        ArrayList intList = new ArrayList();
        Queue input = new LinkedList((ArrayList)flatSexp.clone()) ;
        Stack stack = new Stack();
        stack.push(((GrammarNode)gp.productionRuleList.get(0)).getHead());
        int index = 0 ;
        while(!input.isEmpty())
            {
            String token = (String)input.remove();
            while(true)
                {
                if(stack.peek().equals(token))
                    {
                    // if found a match, pop it from the stack
                    stack.pop();
                    // if the stack top is an ERC, read the next token
                    if(token.equals("ERC"))
                        {
                        token = (String)input.remove();
                        intList.add(Integer.valueOf(token));
                        }
                    break;
                    }
                else
                    {
                    int rIndex = ((Integer)gp.ruleHeadToIndex.get(stack.peek())).intValue();
                    int fIndex = ((Integer)gp.functionHeadToIndex.get(token)).intValue();
                    Integer ruleIndex = new Integer(gp.predictiveParseTable[rIndex][fIndex]);
                    // get the action (rule) to expand
                    GrammarNode action = (GrammarNode)gp.indexToRule.get(ruleIndex);
                    // if the index is still in the range of minGene.length, use it.
                    // otherwise use the minGene[0] value.
                    int minIndex = 0 ; if(index < minGene.length) minIndex = index ;
                    // now add
                    intList.add(new Integer(((Integer)gp.absIndexToRelIndex.get(ruleIndex)).intValue() + (int)minGene[minIndex]));
                    index++;
                    stack.pop();
                    action = action.children.get(0);
                    if(action instanceof GrammarFunctionNode)
                        {
                        // push the rule (action) arguments in reverse way
                        for(int i = ((GrammarFunctionNode)action).getNumArguments() - 1 
                                ; i >= 0 ; i--)
                            stack.push(((GrammarFunctionNode)action).getArgument(i).getHead());
                        // the rule (action) head should be on the top
                        stack.push(action.getHead());
                        }
                    else if(action instanceof GrammarRuleNode) // push as usual
                        stack.push(((GrammarRuleNode)action).getHead());
                    }
                }
            }
        // now convert the list into an array
        int[] genomeVals = new int[intList.size()];
        for(int i = 0 ; i < intList.size() ; i++) { genomeVals[i] = ((Integer)intList.get(i)).intValue() ; }
        return genomeVals ;
        }

    /**
       Reverse of the original map() function, takes a GPIndividual and returns
       a corresponding GEIndividual; The GPIndividual may contain more than one trees,
       and such cases are handled accordingly, see the 3rd bullet below --

       NOTE:
       * This reverse mapping is only valid for S-expression trees ;

       * This procedure supports ERC for the current population (not for population
       /subpopulation from other islands); However, that could be done by merging
       all ERCBanks from all the sub-populations but that is not done yet ;

       * Support for the ADF's are done as follows -- suppose in one GPIndividual,
       there are N trees -- T1, T2, ,,, Tn and each of them follows n different
       grammars G1, G2, ,,, Gn respectively; now if they are reverse-mapped to
       int arrays, there will be n int arrays A1[], A2[], ,,, An[]; and suppose
       the i-th tree Ti is reverse mapped to int array Ai[] and morevoer Ai[] is 
       the longest among all the arrays (Bj[]s); so Bi[] is sufficient to build 
       all ADF trees Tjs.
    */
    public GEIndividual reverseMap(EvolutionState state, GPIndividual ind, int threadnum)
        {
        // create a dummy individual
        GEIndividual newind = (GEIndividual)i_prototype.clone();

        // The longest int will be able to contain all ADF trees.
        int longestIntLength = -1 ;
        int[] longestInt = null ;
        // Now go through all the ADF trees.
        for(int treeIndex = 0 ; treeIndex < ind.trees.length ; treeIndex++)
            {
            // Flatten the Lisp tree
            ArrayList flatSexp = (ArrayList)flattenSexp(state, threadnum,
                ind.trees[treeIndex]);
            // Now convert the flatten list into an array of ints
            // no. of trees == no. of grammars
            int[] genomeVals = parseSexp(flatSexp, grammarParser[treeIndex]);
            // store the longest int array
            if(genomeVals.length >= longestIntLength)
                {
                longestIntLength = genomeVals.length ;
                longestInt = new int[genomeVals.length] ;
                System.arraycopy(genomeVals, 0, longestInt, 0, genomeVals.length);
                }
            genomeVals = null ;
            }
        // assign the longest int to the individual's genome
        newind.genome = longestInt ;

        // update the GPIndividual's fitness information
        newind.fitness = ind.fitness ;
        newind.evaluated = false;

        // Set the species to me ? not sure.
        newind.species = this;

        // return it
        return newind ;
        }
    }
