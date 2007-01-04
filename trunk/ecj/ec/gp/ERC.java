/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp;
import ec.*;
import ec.util.*;
import java.io.*;

/* 
 * ERC.java
 * 
 * Created: Mon Oct 25 18:22:15 1999
 * By: Sean Luke
 */

/**
 * ERC is an abstract GPNode which implements Ephemeral Random Constants,
 * as described in Koza I.  An ERC is a node which, when first instantiated,
 * gets set to some random constant value which it always returns from
 * then on, even after being crossed over into other individuals.
 * In order to implement an ERC, you need to override several methods below.
 *
 * <p> Remember that if your ERC value isn't a simple or mutable type 
 * (like an int or a string), you'll have to deep-clone it in an overridden
 * clone() method.
 *
 * @author Sean Luke
 * @version 1.0 
 */

public abstract class ERC extends GPNode
    {
    public static final String ERC_PREFIX = "ERC";

    /** Returns the lowercase "name" of this ERC function class, some
        simple, short name which distinguishes this class from other ERC
        function classes you're using.  If you have only one ERC function
        class, you can just return "".  Whatever the name is, it should
        generally only have letters, numbers, or hyphens or underscores in it.
        No whitespace or other characters. */
    public abstract String name();

    /** Checks to make certain that the ERC has no children. */
    public void checkConstraints(final EvolutionState state,
                                 final int tree,
                                 final GPIndividual typicalIndividual,
                                 final Parameter individualBase)
        {
        super.checkConstraints(state,tree,typicalIndividual,individualBase);
        // make sure we don't have any children...
        if (children.length!= 0) state.output.error("Incorrect number of children for the node " + toStringForError() + " (should be 0)");
        }

    /** Remember to override this to randomize your ERC after it has been cloned.  The prototype will not ever receive this method call. */
    public abstract void resetNode(final EvolutionState state, int thread);

    /** Implement this to do ERC-to-ERC comparisons. */
    public abstract boolean nodeEquals(final GPNode node);

    /** Implement this to hash ERCs, along with other nodes, in such a way that two
        "equal" ERCs will usually hash to the same value. */

    public abstract int nodeHashCode();

    /** You might want to override this to return a special human-readable version of the erc value; otherwise this defaults to toString();  This should be something that resembles a LISP atom.  If a simple number or other object won't suffice, you might use something that begins with ERC_PREFIX + name() + [ + ... + ] */

    public String toStringForHumans() 
        { return toString(); }

    /** This defaults to simply ERC_PREFIX + name() + "[" + encode() + "]" */

    public String toString() 
        { return ERC_PREFIX + name() + "[" + encode() + "]"; }

    /** Encodes data from the ERC, using ec.util.Code.  */
    public abstract String encode();

    /** Decodes data into the ERC from dret.  Return true if you sucessfully
        decoded, false if you didn't.  Don't increment dret.pos's value beyond
        exactly what was needed to decode your ERC.  If you fail to decode,
        you should make sure that the position and data in the dret are exactly
        as they were originally. */
    public abstract boolean decode(final DecodeReturn dret);

    /** Mutates the node's "value".  This is called by mutating operators
        which specifically <i>mutate</i> the "value" of ERCs, as opposed to 
        replacing them with whole new ERCs. The default form of this function
        simply calls resetNode(state,thread), but you might want to modify
        this to do a specialized form of mutation, applying gaussian
        noise for example. */

    public void mutateERC(final EvolutionState state, final int thread)
        {
        resetNode(state,thread);
        }

    /** To successfully write to a DataOutput, you must override this to write your specific ERC data out.  The
        default implementation issues a fatal error. */
    public void writeNode(final EvolutionState state, final DataOutput dataOutput) throws IOException
        {
        state.output.fatal("writeNode(EvolutionState,DataInput) not implemented in " + getClass().getName());
        }

    /** To successfully read from a DataOutput, you must override this to read your specific ERC data in.  The
        default implementation issues a fatal error. */
    public void readNode(final EvolutionState state, final DataInput dataInput) throws IOException
        {
        state.output.fatal("readNode(EvolutionState,DataInput) not implemented in " + getClass().getName());
        }

    public GPNode readNode(final DecodeReturn dret) 
        {
        int len = dret.data.length();
        int originalPos = dret.pos;
        
        // get my name
        String str2 = ERC_PREFIX + name() + "[";
        int len2 = str2.length();

        if (dret.pos + len2 >= len)  // uh oh, not enough space
            return null;

        // check it out
        for(int x=0; x < len2 ; x++)
            if (dret.data.charAt(dret.pos + x) != str2.charAt(x))
                return null;

        // looks good!  try to load this sucker.
        dret.pos += len2;
        ERC node = (ERC) lightClone();
        if (!node.decode(dret)) 
            { dret.pos = originalPos; return null; }  // couldn't decode it

        // the next item should be a "]"
        
        if (dret.pos >= len)
            { dret.pos = originalPos; return null; }
        if (dret.data.charAt(dret.pos) != ']') 
            { dret.pos = originalPos; return null; }
        
        // Check to make sure that the ERC's all there is
        if (dret.data.length() > dret.pos+1)
            {
            char c = dret.data.charAt(dret.pos+1);
            if (!Character.isWhitespace(c) &&
                c != ')' && c != '(') // uh oh
                { dret.pos = originalPos; return null; }
            }   

        dret.pos++;

        return node;
        }
    }
