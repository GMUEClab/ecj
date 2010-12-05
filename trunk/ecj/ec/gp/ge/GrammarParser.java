package ec.gp.ge;
import java.io.*;
import java.util.*;
import ec.*;
import ec.gp.GPFunctionSet;
import ec.gp.GPNode;
import ec.util.*;

/*
 * GrammarParser.java
 *
 * Created: Sun Dec  5 11:33:43 EST 2010
 * By: Houston Mooers, with modifications by Sean Luke
 *
 */

/**
 * A GrammarParser is the basic class for parsing a GE ruleset into a parse tree of GrammarNodes.
 * This parse tree is then later used to produce a GPIndividual from a GEIndividual in GESpecies.
 * It is assumed that the root will represent the first rule given in the grammar.
 * 
 */



public class GrammarParser implements Prototype
    {
    public static final String P_PARSER = "parser";

	// The parsed rules, hashed by name
    HashMap rules = new HashMap();
	// The resulting parse tree
    GrammarRuleNode root = null;

    /** The default regular expressions for tokens in the parser.  If you'd
        like to change minor features of the regular expressions, override the
        getRegexes() method in a subclass to return a different array.  Note that
        if you INSERT a new regular expression into the middle of these, the values
        of the various token constants ("LPAREN", "RULE", etc.) will be wrong, so you
        will need to override or modify the methods which use them.*/ 
    public static final String[] DEFAULT_REGEXES = new String[]
    {
    "\\p{Blank}*#[^\\n\\r]*",               // COMMENT: matches a #foo up to but not including the newline.  Should appear first.
    "\\p{Blank}*\\(",                       // LPAREN: matches a (
    "\\p{Blank}*\\)",                       // RPAREN: matches a )
    "\\p{Blank}*<[^<>()\\p{Space}]*>",      // RULE: matches a rule of the form <foo>.  No <, >, (, ), |, or spaces may appear in foo
    "\\p{Blank}*[|]",                       // PIPE: matches a |
    "\\p{Blank}*::=",                       // EQUALS: matches a :==
    "\\p{Blank}*::=",                       // NUMERIC_CONSTANT: does nothing right now, so set to be identical to EQUALS.  Reserved for future use.
    "\\p{Blank}*::=",                       // BOOLEAN_CONSTANT: does nothing right now, so set to be identical to EQUALS.  Reserved for future use.
    "\\p{Blank}*::=",                       // STRING_CONSTANT: does nothing right now, so set to be identical to EQUALS.  Reserved for future use.
    "\\p{Blank}*[^<>()|\\p{Space}]+",       // FUNCTION (must appear after RULE and PIPE): matches a rule of the form foo.  No <, >, (, ), |, or spaces may appear in foo, and foo must have at least one character.
    };
	
    protected static final int COMMENT = 0;
    protected static final int LPAREN = 1;
    protected static final int RPAREN = 2;
    protected static final int RULE = 3;
    protected static final int PIPE = 4;
    protected static final int EQUALS = 5;
    // the following three are reserved for future use
    protected static final int NUMERIC_CONSTANT = 6;
    protected static final int BOOLEAN_CONSTANT = 7;
    protected static final int STRING_CONSTANT = 8;
    // and now we continue with our regularly scheduled program
    protected static final int FUNCTION = 9;
        
	/** Returns the regular expressions to use for tokenizing these rules.  By default DEFAULT_REGEXES are returned. */
    public String[] getRegexes() { return DEFAULT_REGEXES; }
        
    public Parameter defaultBase()
        {
        return GEDefaults.base().push(P_PARSER);
        }

    public void setup(EvolutionState state, Parameter base)
        {
        }

    public Object clone()
        {
        try 
            {
            GrammarParser other = (GrammarParser) (super.clone());
            other.rules = (HashMap)(rules.clone());
            // we'll pointer-copy the root
            return other;
            }
        catch (CloneNotSupportedException e) { return null; } // never happens
        }

	// Returns a rule from the hashmap.  If one does not exist, creates a rule with the
	// given head and stores, then returns that.
	GrammarRuleNode getRule(HashMap rules, String head)
        {
        if (rules.containsKey(head))
            return (GrammarRuleNode)(rules.get(head));
        else
            {
            GrammarRuleNode node = new GrammarRuleNode(head);
            rules.put(head, node);
            return node;
            }
        }

	// Parses a rule, one rule per line, from the lexer.  Adds to the existing hashmap if there's already a rule there.
    GrammarRuleNode parseRule(EvolutionState state, Lexer lexer, GPFunctionSet gpfs)
        {
        GrammarRuleNode retResult = null;
                
        String token = lexer.nextToken();
        if(lexer.getMatchingIndex() == COMMENT) return null; //ignore the comment
        if(lexer.getMatchingIndex() == RULE) //rule head, good, as expected...
            {
            lexer.nextToken();
            if(lexer.getMatchingIndex() != EQUALS) 
                {
                state.output.fatal("GE Grammar Error: Expecting equal sign after rule head: " + token);
                }
            retResult = getRule(rules, token);
            parseProductions(state, retResult, lexer, gpfs);
            }
        else
            {
            state.output.fatal("GE Grammar Error - Unexpected token: Expecting rule head.: " + token);
            }
        return retResult;
        // IMPLEMENTED
        // Need to parse the rule using a recursive descent parser
        // If there was an error, then try to call state.output.error(...).
        //
        // Don't merge into any existing rule -- I do that in parseRules below.  Instead, just pull out
        // rules and hang them into your "new rule" as necessary.  
        // Use getRule(rules, "<rulename>") to extract the rule representing the current rule name which you
        // can hang inside there as necessary. 
        //
        // If you have to you can call state.output.fatal(...) which will terminate the program,
        // but piling up some errors might be useful.  I'll handle the exitIfErors() in parseRules below
        //
        // Return null if there was no rule to parse (blank line or all comments) but no errors.
        // Also return null if you called state.output.error(...).
        }

	// Parses each of a rule's production choices.
    void parseProductions(EvolutionState state, GrammarRuleNode retResult, Lexer lexer, GPFunctionSet gpfs) 
        {
        GrammarFunctionNode grammarfuncnode;
        do 
            {
            String token = lexer.nextToken();
            if(lexer.getMatchingIndex() == RULE)
                {
                retResult.addChoice(getRule(rules, token));
                token = lexer.nextToken();
                }
            else
                {
                if(lexer.getMatchingIndex() != LPAREN) //first expect '('
                    {
                    state.output.fatal("GE Grammar Error - Unexpected token for rule: " + retResult.getHead() + "Expecting '('.");
                    }
                token = lexer.nextToken();
                if(lexer.getMatchingIndex() != FUNCTION) //now expecting function
                    {
                    state.output.fatal("GE Grammar Error - Expecting a function name after first '(' for rule: " + retResult.getHead() + " Error: " + token);
                    }
                else
                    {
					if (!(gpfs.nodesByName.containsKey(token)))
						{
						state.output.fatal("GPNode " + token + " is not defined in the function set.");
						}
					grammarfuncnode = new GrammarFunctionNode(gpfs, token); 
					token = lexer.nextToken(); 
                    while(lexer.getMatchingIndex() != RPAREN)
                        {
                        if(lexer.getMatchingIndex() != RULE) //this better be the name of a rule node
                            {
                            state.output.fatal("GE Grammar Error - Expecting a rule name as argument for function definition: " + grammarfuncnode.getHead() + " Error on : " + token);
                            }
                        grammarfuncnode.addArgument(getRule(rules, token));
                        token = lexer.nextToken();
                        }
                    retResult.addChoice(grammarfuncnode);
                    }
                //after right paren, should see either '|' or newline
                token = lexer.nextToken();
                if(lexer.getMatchingIndex() != PIPE && lexer.getMatchingIndex() != Lexer.FAILURE)
                    {
                    state.output.fatal("GE Grammar Error - Expecting either '|' delimiter or newline. Error on : " + token);
                    }
                }
            }
        while(lexer.getMatchingIndex() == PIPE);
        }

	/** Parses the rules from a grammar and returns the resulting GrammarRuleNode root. */
    public GrammarRuleNode parseRules(EvolutionState state, BufferedReader reader, GPFunctionSet gpfs)
        {
        rules = new HashMap();
        try
            {
            String line;
            while ((line = reader.readLine()) != null)
                {
                GrammarRuleNode rule = parseRule(state, new Lexer(line.trim(), DEFAULT_REGEXES), gpfs);
                if (rule != null && root == null) root = rule;
                }
            }
        catch (IOException e) { } // do nothing
        state.output.exitIfErrors();
		return root;
        }

    public String toString()
        {
        String ret = "Grammar[";
        Iterator i = rules.values().iterator();
        while(i.hasNext())
            {
            ret = ret +"\n" + i.next();
            }
        return ret + "\n\t]";
        }

    /**
     * Checks that all grammar rules in ruleshashmap have at least one possible production
     * @return true if grammar rules are properly defined, false otherwise
     */
    public boolean validateRules()
        {
        boolean isok = true;
        Iterator i = rules.values().iterator();
        while(i.hasNext())
            {
            GrammarRuleNode rule = (GrammarRuleNode)(i.next());
            if(rule.getNumChoices() < 1)
                {
                System.out.println("Grammar is bad! - Rule not defined: " + rule);
                isok = false;
                }
            }
        if (isok) { System.out.println("All rules appear properly defined!"); return true; }            
        return false;
        }

	/** A simple testing fcility. */
    public static void main(String args[]) throws  FileNotFoundException
        {
        // make a dummy EvolutionState that just has an output for testing
        EvolutionState state = new EvolutionState();
        state.output = new Output(true);
        state.output.addLog(ec.util.Log.D_STDOUT,false);
        state.output.addLog(ec.util.Log.D_STDERR,true);

        GrammarParser gp = new GrammarParser();
        gp.parseRules(state, new BufferedReader(new FileReader(new File(args[0]))), null);
        gp.validateRules();
        System.err.println(gp);
        }
    }