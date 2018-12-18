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
 * <h2>Impementing an ERC</h2>
 *
 * A basic no-frills ERC needs to have the following things:
 *
 * <ul>
 * <li>The data holding the ERC (perhaps a float or a couple of floats)
 * <li>An implementation of the <b>eval</b> method which returns the appropriate
 *     data when the node is evaluated.
 * <li>Possibly an implementation of the <b>clone</b> method to copy that data
 *     properly.  If your ERC data is just a simple or immutable type
 *     (like an int or a string), you don't need write a clone() method;
 *     the default one works fine.  But if your data is an array or other
 *     mutable object, you'll need to override the clone() method to copy
 *     the array.  
 *
 * <li>An implementation of the <b>resetNode</b> method to randomize the
 *     data once cloned from the prototype.  This essentially "initializes"
 *     your ERC.
 *
 * <li>An implementation of the <b>encode</b> method which presents the
 *     ERC as a String.  If you don't plan on writing individuals out to
 *     files in a fashion that enables them to be read back in again later,
 *     but only care to print out individuals for statistics purposes, 
 *     you can implement this to just
 *     write <tt>"" + <i>value</i></tt>, where <i>value</i> is your data.
 *
 * <li>An implementation of the <b>nodeEquals</b> method to return true if
 *     the other node is also an ERC of the same type, and it has the
 *     same ERC data as yourself.
 *
 *
 * </ul>
 * 
 * A more advanced ERC will need some of the following gizmos:
 * 
 * <ul>
 *
 * <li>If you have ERCs of different class types (for example, a vector ERC
 *      and a floating-point scalar ERC), you will wish to distinguish them
 *      when they're printed to files.  To do this,  override the <b>name</b> 
 *      method to return different strings for each of them (perhaps "vec" versus "").
 *
 * <li>If you want to write your ERCs to files such that they can be read
 *      back in again, you'll need to override the <b>encode</b> method
 *      to write using the <tt>ec.util.Code</tt> class.  Further, you'll need to
 *      override the <b>decode</b> method to read in the individual using the
 *      <tt>ec.util.Code</tt> and <tt>ec.util.DecodeReturn</tt> classes.  The
 *      default version -- which is wrong -- returns <tt>false</tt>.
 *      When you do this, you'll probably also want to override the <b>toStringForHumans()</b>
 *      method to return a simple string form of the ERC: perhaps just a number
 *      or a vector like "<7.24, 9.23>".  This is because by default <b>toStringForHumans()</b>
 *      calls <b>toString()</b>, which in turn calls <b>encode</b>, which you have
 *      just overidden to be more computer-ish.
 *
 * <li>ERCs can be mutated using a custom mutator pipeline, for example the
 *     <b>ec.gp.breed.MutateERCPipeline</b>.  If you expect to mutate your ERCs,
 *     you may wish to override the <b>mutateERC</b> method to do something
 *     more subtle than its default setting (which just randomizes the
 *     ERC again, by calling resetNode).
 * 
 * <li>The default <b>nodeHashCode</b> implementation is poor and slow (it
 *     creates a string using encode() and then hashes the sting).  You might
 *     create a better (and probably simpler) hash code function.
 *
 * <li>If you're going to use facilities such as the Island Model or the distributed
 *     evaluator, you'll need to implement the <b>writeNode</b> and <b>readNode</b>
 *     methods to read/write the node to DataInput/DataOutput.  The default implementations
 *     just throw errors.
 *
 * <li>If you need to set up your ERC class from the parameter file, do so in the <b>setup</b> method.
 *
 * </ul>
 *
 * <p> See the <b>ec.app.regression.func.RegERC</b> class for an example of a simple but "fuly-implemented"
 * ERC.  A slightly more complicated example can be found in <b>ec.app.lawnmower.func.LawnERC</b>.
 *
 * @author Sean Luke
 * @version 1.0 
 */

public abstract class ERC extends GPNode
    {
    /** Returns the lowercase "name" of this ERC function class, some
        simple, short name which distinguishes this class from other ERC
        function classes you're using.  If you have more than one ERC function,
        you need to distinguish them here.  By default the value is "ERC",
        which works fine for a single ERC function in the function set.
        Whatever the name is, it should
        generally only have letters, numbers, or hyphens or underscores in it.
        No whitespace or other characters. */
    public String name() { return "ERC"; }

    /** Usually ERCs don't have children, and this default implementation makes certain of it. 
        But if you want to override this, you're welcome to. */
    public int expectedChildren() { return 0; }

    /** Remember to override this to randomize your ERC after it has been cloned.  The prototype will not ever receive this method call. */
    public abstract void resetNode(final EvolutionState state, int thread);

    /** Implement this to do ERC-to-ERC comparisons. */
    public abstract boolean nodeEquals(final GPNode node);

    /** Implement this to hash ERCs, along with other nodes, in such a way that two
        "equal" ERCs will usually hash to the same value. The default value, which 
        may not be very good, is a combination of the class hash code and the hash
        code of the string returned by encode().  You might make a better hash value. */
    public int nodeHashCode() { return super.nodeHashCode() ^ encode().hashCode(); }

    /** You might want to override this to return a special human-readable version of the erc value; otherwise this defaults to toString();  This should be something that resembles a LISP atom.  If a simple number or other object won't suffice, you might use something that begins with  name() + [ + ... + ] */
    public String toStringForHumans() 
        { return toString(); }

    /** This defaults to simply name() + "[" + encode() + "]".   You probably shouldn't deviate from this. */
    public String toString() 
        { return name() + "[" + encode() + "]"; }

    /** Encodes data from the ERC, using ec.util.Code.  */
    public abstract String encode();

    /** Decodes data into the ERC from dret.  Return true if you sucessfully
        decoded, false if you didn't.  Don't increment dret.pos's value beyond
        exactly what was needed to decode your ERC.  If you fail to decode,
        you should make sure that the position and data in the dret are exactly
        as they were originally. */
    public boolean decode(final DecodeReturn dret)
        {
        return false;
        }

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
        String str2 = name() + "[";
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
