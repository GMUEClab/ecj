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
 * @author Sean Luke, Joseph Zelibor III, and Eric Kangas
 * @version 1.0 
 */
public class GESpecies extends IntegerVectorSpecies
    {

    HashMap[] rules;
    public String[] startSymbols;
    public static final String P_FILE = "file";
    public static final String P_GESPECIES = "species";
    public static final String P_GPSPECIES = "gp-species";
    //return value which denotes that the tree has grown too large.
    public static final int BIG_TREE_ERROR = -1;
    public GPSpecies gpspecies;
    HashMap ERCBank;

    /*
     * inner class for creating rules.
     * a rule consists of a name and a number of choices.
     */
    class Rule
        {

        String name;
        ArrayList choices;  //strings
        int numberOfChoices;

        public String toString()
            {
            return "Rule [choices=" + choices + ", name=" + name
                + ", numberOfChoices=" + numberOfChoices + "]";
            }
        }

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

        mapperSetup(state, p);

        System.out.println("Grammar Mapping complete: begining simulation.");
        }

    /**
     * Creates a HashMap of rules to be referenced when creating an individual's tree.  The
     * readGrammar method is called inorder to fill an array of strings to be parsed into the rules
     * of the grammar.
     *
     * @param state
     * @param base
     */
    void mapperSetup(EvolutionState state, Parameter base)
        {
        //Dummy stuff to get the number of trees a GPIndividual has
        GPIndividual gpi = (GPIndividual) (gpspecies.i_prototype);
        GPTree[] trees = gpi.trees;
        int numGrammars = trees.length;

        //there is a rule set for each grammar file
        rules = new HashMap[numGrammars];
        startSymbols = new String[numGrammars];
        ERCBank = new HashMap();

        //parse each grammar file and create a rule hashmap for each one
        for (int x = 0; x < numGrammars; x++)
            {
            rules[x] = new HashMap();

            String[] grammar;
            Parameter p = base.push(P_FILE);
            Parameter def = defaultBase();

            File grammarFile = state.parameters.getFile(p, def.push(P_FILE).push("" + x));

            //files need to be read in ascending order starting at .0
            if(grammarFile == null)
                {
                state.output.fatal("Error retrieving grammar file(s): " + def.toString() + "."+ P_FILE + "." + x + " is undefined.");
                }

            grammar = readGrammarFile(grammarFile);
            boolean[] started = {false};

            //get rest of nonterminals and put them into Rules
            for (int i = 0; i < grammar.length; i++)
                {
                    lexTheGrammar(state, grammar[i], started, x);
                }
            }
        }

    public void lexTheGrammar (EvolutionState state, String grammarLine, boolean[] started, int grammarIndex)
    {
        //check to see if the line is only whitespace
        if (grammarLine.trim().equals(""))
            {
            return;  //ignore whitespace outside
            }

        //ignore commented line, comments start with a #
        if (grammarLine.charAt(0) == '#')
            {
            return;
            }

        Pattern grammarPattern = Pattern.compile("^" + //beginning of line
                                                "\\s*" + //possible whitespace
                                                "(<.*>)" +  //capturing group 1, rule name
                                                "\\s*" + //possible whitespace
                                                "::=" +
                                                "\\s*(.*)"); //possible whitespace, capture group 2, grab rest.

//               "\\(?" +  //open paren of lisp statement
//                    		"\\s*" + //possible whitespace
//                    		"(\\w*)" + //group 2, lisp statement name
//                    		"\\s*" + //possible whitespace
//                    		"(.*)" + //group 3, list of rules
//                    		"\\)?" + //close paren of lisp statement
//                    		"\\s*" + //possible whitespace
//                    		"$"); //end of line
        Matcher matcher = grammarPattern.matcher(grammarLine);

        boolean patternFound =matcher.matches();

        if(patternFound)
        {
            Rule r = new Rule();
            r.name = matcher.group(1);

            if (started[0] == false) //special case for startSymbol
            {
            startSymbols[grammarIndex] = r.name;
            started[0] = true;
            }

            r.choices = new ArrayList();  //get choices for each rule
            String[] choices = matcher.group(2).split("\\|");

            for (int j = 0; j < choices.length; j++)
                {
                String s = choices[j].trim();
                
                if (s.charAt(0) == '(' && s.endsWith(")"))
                    r.choices.add(s);
                else if(s.charAt(0) == '<' && s.endsWith(">"))
                    r.choices.add(s);
                else
                    state.output.fatal("invalid statement " + s + ".");
                }


            r.numberOfChoices = r.choices.size();

            Rule oldRule;
            if ((oldRule = (Rule) rules[grammarIndex].get(r.name)) != null)
                {
                //for appending rules (ones which already exist)
                oldRule.numberOfChoices += r.numberOfChoices;
                oldRule.choices.addAll(r.choices);
                } else  //new rules
                {
                rules[grammarIndex].put(r.name, r);
                }
        }
    }

    public String[] lexTheRules(EvolutionState state, String possibleFunction)
    {
        //System.err.println(possibleFunction);

        ArrayList tokens = new ArrayList();

//        Pattern pattern = Pattern.compile("\\s*" +
//                                            "\\(" +
//                                            "\\s*" +
//                                            "([^<\\s]*)" +
//                                            "\\s*" +
//                                            "(.*)\\s*\\)");


        Pattern pattern = Pattern.compile("\\s*" +
                                            "\\(" +
                                            "\\s*" +
                                            "(\\S+)" +
                                            "\\s+" +
                                            "(.*)\\s*\\)");
                                            
                                            
        Matcher matcher = pattern.matcher(possibleFunction);
        boolean isValid = matcher.matches();

        //group(2) is everything that is not part of the name section of a statement
        if (isValid)
        {
            tokens.add(matcher.group(1));

            Pattern pattern2 = Pattern.compile("(\\s*<[^>]*>)");
            Matcher matcher2 = pattern2.matcher(matcher.group(2));

            Pattern pattern3 = Pattern.compile("\\s*(<[^>]*>\\s*)*");
            Matcher matcher3 = pattern3.matcher(matcher.group(2));

            if(!matcher3.matches())
                state.output.fatal(possibleFunction + " contains invaild data.");

            while(isValid = matcher2.find())
            {
                tokens.add(matcher2.group());
            }

            String[] result = new String[tokens.size()];

            //no generics makes me sad beyond belief
            //converts our arraylist to a string array :/
            for (int i = 0; i < tokens.size(); i++)
            {
                result[i] = ((String)(tokens.get(i))).trim();
            }

            //System.out.println(Arrays.deepToString(result));

            return result;
        }

        //it may be a terminal
        else
        {
            possibleFunction = possibleFunction.trim();

            if(possibleFunction.charAt(0) == '(' && possibleFunction.endsWith(")"))
            {
                //remove "("
                possibleFunction = possibleFunction.replaceFirst("\\(", "");
                //remove ")"
                possibleFunction = possibleFunction.substring(0, possibleFunction.length()-1);

                String result[] = {possibleFunction.trim()};

                return result;
            }
        }

        //rest of the invalid cases
        return null;
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
        int[] countNumberOfChromosomesUsed =
            {
            position
            };  //hack, use an array to pass an extra value
        byte[] genome = ind.genome;
        GPFunctionSet gpfs = tree.constraints((GPInitializer) state.initializer).functionset;
        Rule r = ((Rule) (rules[treeNum].get(startSymbols[treeNum])));
        GPNode root;

        try //get the tree, or return an error.
            {
            root = makeSubtree(countNumberOfChromosomesUsed, genome, state, gpfs, r, treeNum, threadnum);
            } catch (BigTreeException e)
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

    //thrown by makeSubtree when chromosome is not large enough for the generated tree.
    class BigTreeException extends RuntimeException
        {

        static final long serialVersionUID = -8668044916857977687L;
        }

    /*
     * returns the tree created from the rules and genome
     */
    GPNode makeSubtree(int[] index, byte[] genome, EvolutionState es, GPFunctionSet gpfs, Rule rule, int treeNum, int threadnum)
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
        if (rule.numberOfChoices > 1)
            {
            //casting to an int should be ok since the biggest these genes can be is a byte
            i = ((genome[index[0]]) - ((int)(this.minGene(index[0])))) % rule.numberOfChoices;
            index[0]++;
            }
        //only 1 rule to consider
        else
            {
            i = 0;
            }
        String choice = ((String) (rule.choices.get(i))).trim();

        //choice is a rule, but is not in the set of rules taken from the grammar
        if (choice.matches("<.*>") && !(rules[treeNum].containsKey(choice)))
            {
            es.output.fatal(choice + " is not defined in the grammar.");
            }

        // if body is another rule head
        //look up rule
        Rule r;
        if ((r = (Rule) rules[treeNum].get(choice)) != null)
            {
            return makeSubtree(index, genome, es, gpfs, r, treeNum, threadnum);
            } else if (choice.startsWith("(")) //handle terminals and nonterminals
            {
            //String[] temparray = choice.substring(1).split(" ");

                //System.out.println(choice);

            String[] temparray = (String[])(lexTheRules(es, choice));

            if(temparray == null)
            {
                es.output.fatal(choice + " is an invalid statement.");
            }

            //temparray[0] = temparray[0].replaceAll("\\)", "");  //remove the close brace ')'

//            System.err.println("temparray = "+temparray);
//            System.err.println("temparray[0] = "+temparray[0]);
//            System.err.println("nodesbyname = " + gpfs.nodesByName);

            //does this rule map to an existing node in the function set?
            if (!(gpfs.nodesByName.containsKey(temparray[0])))
                {
                es.output.fatal("GPNode " + temparray[0] + " is not defined in the function set.");
                }

            //Known: node exists in the function set time to get the GPNode from GPFunctionSet.nodesByName
            GPNode validNode = ((GPNode[]) (gpfs.nodesByName.get(temparray[0])))[0];
            
            int numChildren = validNode.children.length;
            //index 0 is the node itself
            int numChildrenInGrammar = temparray.length -1;

            //does the grammar contain the correct amount of children that the GPNode requires
            if (numChildren != numChildrenInGrammar)
                {
                es.output.fatal("GPNode " + validNode.toStringForHumans() + " requires " + numChildren + " children.  "
                    + numChildrenInGrammar + " children found in the grammar.");
                }

            //check to see if it is an ERC node
            if (validNode.name().equals("ERC"))
                {                
                validNode = obtainERC(es, key, genome, threadnum, validNode);
                }
            //non ERC node
            else
                {
                validNode = validNode.lightClone();
                }

            //get the rest.
            for (int j = 1, childNumber = 0; j < temparray.length; j++)
                {
                    if(temparray[j].trim().matches("<.*>")) //non-terminal
                        {
                        Rule r2 = (Rule) rules[treeNum].get(temparray[j]);

                        //get and link children to the current GPNode
                        validNode.children[childNumber] = makeSubtree(index, genome, es, gpfs, r2, treeNum, threadnum);
                        if (validNode.children[childNumber] == null)
                            {
                            return null;
                            }
                        childNumber++;
                        }
                }

            return validNode;
            }
        
        //handling of extra cases
        return null;
        }

    //method for obtaining a ERC indepen
    public GPNode obtainERC(EvolutionState state, int key, byte[] genome, int threadnum, GPNode node)
        {
        ArrayList ERCList = (ArrayList) (ERCBank.get(new Integer(key)));

        if (ERCList == null)
            {
            ERCList = new ArrayList();
            ERCBank.put(new Integer(key), ERCList);
            }

        GPNode dummy = null;

        //search array list for an ERC of the same type we want
        for (int i = 0; i < ERCList.size(); i++)
            {
            dummy = (GPNode) ERCList.get(i);

            //ERC was found inside the arraylist
            if (dummy.nodeEquivalentTo(node))
                {
                return dummy;
                }
            }

        //erc was not found in the array list lets make one
        node = node.lightClone();
        node.resetNode(state, threadnum);
        ERCList.add(node);

        return node;
        }

    /*
     * reads the grammar file from the given path, returns a String[] where each index is a line in the grammar.
     */
    String[] readGrammarFile(File grammarFile)
        {
        String[] grammar;
        List tempGrammar = new ArrayList();  //list of strings
        BufferedReader br = null;
        try
            {
            br = new BufferedReader(new FileReader(grammarFile));
            } catch (FileNotFoundException e)
            {
            e.printStackTrace();
            return null;
            }

        if (br == null)
            {
            return null;  //error case
            }
        try
            {
            String s;
            while ((s = br.readLine()) != null)
                {
                tempGrammar.add(s);
                }
            } catch (IOException e)
            {
            e.printStackTrace();
            }

        grammar = new String[tempGrammar.size()];
        for (int i = 0; i < tempGrammar.size(); i++)
            {
            grammar[i] = (String) tempGrammar.get(i);
            }
        return grammar;
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
            {
            return null;
            } else
            {
            return newind;
            }
        }
    }
