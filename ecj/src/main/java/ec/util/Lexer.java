package ec.util;
import java.util.regex.*;

/*
 * Lexer.java
 *
 * Created: Sun Dec  5 11:33:43 EST 2010
 * By: Sean Luke
 *
 */

/**
 * A simple line-by-line String tokenizer.  You provide Lexer with a String or other
 * CharSequence as input, plus an array of regular expressions.  Each time you call
 * nextToken(...), the Lexer matches the next token against the regular expressions
 * and returns it.  The regular expressions are checked in order, and the first one
 * that matches is the winner.
 * 
 */

public class Lexer
    {
    /** An index which indicates that no further tokens were found.  This could be due to the end of the string or due to a bad
        string.  You'll need to check the index to determine for sure.*/
    public static final int FAILURE = -1;
        
    CharSequence input;
    int position = 0;
    Matcher[] matchers;
    String[] regexps;
    int matchingIndex = FAILURE;
        
    /** Builds a Lexer for the given input with the provided regular expressions.  The regular expressions
        will be checked in order against the input, and the first one which matches will be assumed to be the token.*/
                
    public Lexer(CharSequence input, String[] regexps)
        {
        this.regexps = regexps;
        matchers = new Matcher[regexps.length];
        for(int i = 0 ; i < regexps.length; i++)
            matchers[i] = Pattern.compile(regexps[i]).matcher(input);  // not DOTALL
        this.input = input;
        }
        
    /** Returns the next token as a string.  If *trim* is true, then the string is first trimmed of whitespace. */
    public String nextToken(boolean trim)
        {
        for(int i = 0 ; i < regexps.length; i++)
            {
            if (!matchers[i].region(position, input.length()).lookingAt()) continue;
            position = matchers[i].end();
            matchingIndex = i;
            return ( trim ? matchers[i].group().trim() : matchers[i].group() );
            }
        // we failed
        matchingIndex = -1;
        return null; 
        }

    /** Returns the next token as a string.  The string is first trimmed of whitespace. */
    public String nextToken() { return nextToken(true); }
        

    /** Returns the index of the regular expression which matched the most recent token. */
    public int getMatchingIndex() 
        {
        return matchingIndex;
        }
                
    /** Returns the regular expression which matched the most recent token. */
    public String getMatchingRule()
        {
        if (matchingIndex == -1) return null;
        return regexps[matchingIndex];
        }
                
    /** Returns the position in the String just beyond the most recent token. */
    public int getMatchingPosition() 
        {
        return position;
        }
        
    }
