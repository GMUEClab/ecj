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
 * GPIndividual.java
 * 
 * Created: Fri Aug 27 17:07:45 1999
 * By: Sean Luke
 */

/**
 * GPIndividual is an Individual used for GP evolution runs.
 * GPIndividuals contain, at the very least, a nonempty array of GPTrees.
 * You can use GPIndividual directly, or subclass it to extend it as
 * you see fit.
 
 * <P>GPIndividuals have two clone methods: clone() and lightClone().  clone() is
 * a deep clone method as usual.  lightClone() is a light clone which does not copy
 * the trees.
 *
 * <p>In addition to serialization for checkpointing, Individuals may read and write themselves to streams in three ways.
 *
 * <ul>
 * <li><b>writeIndividual(...,DataOutput)/readIndividual(...,DataInput)</b>&nbsp;&nbsp;&nbsp;This method
 * transmits or receives an individual in binary.  It is the most efficient approach to sending
 * individuals over networks, etc.  These methods write the evaluated flag and the fitness, then
 * call <b>readGenotype/writeGenotype</b>, which you must implement to write those parts of your 
 * Individual special to your functions-- the default versions of readGenotype/writeGenotype throw errors.
 * You don't need to implement them if you don't plan on using read/writeIndividual.
 *
 * <li><b>printIndividual(...,PrintWriter)/readIndividual(...,LineNumberReader)</b>&nbsp;&nbsp;&nbsp;This
 * approach transmits or receives an indivdual in text encoded such that the individual is largely readable
 * by humans but can be read back in 100% by ECJ as well.  To do this, these methods will encode numbers
 * using the <tt>ec.util.Code</tt> class.  These methods are mostly used to write out populations to
 * files for inspection, slight modification, then reading back in later on.  <b>readIndividual</b>reads
 * in the fitness and the evaluation flag, then calls <b>parseGenotype</b> to read in the remaining individual.
 * You are responsible for implementing parseGenotype: the Code class is there to help you.
 * <b>printIndividual</b> writes out the fitness and evaluation flag, then calls <b>genotypeToString<b> 
 * and printlns the resultant string. You are responsible for implementing the genotypeToString method in such
 * a way that parseGenotype can read back in the individual println'd with genotypeToString.  The default form
 * of genotypeToString simply calls <b>toString</b>, which you may override instead if you like.  The default
 * form of <b>parseGenotype</b> throws an error.  You are not required to implement these methods, but without
 * them you will not be able to write individuals to files in a simultaneously computer- and human-readable fashion.
 *
 * <li><b>printIndividualForHumans(...,PrintWriter)</b>&nbsp;&nbsp;&nbsp;This
 * approach prints an individual in a fashion intended for human consumption only.
 * <b>printIndividualForHumans</b> writes out the fitness and evaluation flag, then calls <b>genotypeToStringForHumans<b> 
 * and printlns the resultant string. You are responsible for implementing the genotypeToStringForHumans method.
 * The default form of genotypeToStringForHumans simply calls <b>toString</b>, which you may override instead if you like
 * (though note that genotypeToString's default also calls toString).  You should handle one of these methods properly
 * to ensure individuals can be printed by ECJ.
 * </ul>
 *
 * <p>In general, the various readers and writers do three things: they tell the Fitness to read/write itself,
 * they read/write the evaluated flag, and they read/write the GPTree array (by having each GPTree read/write
 * itself).  If you add instance variables to GPIndividual, you'll need to read/write those variables as well.


 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>numtrees</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(number of trees in the GPIndividual)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>tree.</tt><i>n</i><br>
 <font size=-1>classname, inherits or = ec.gp.GPTree</font></td>
 <td valign=top>(class of tree <i>n</i> in the individual)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 gp.individual

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>tree.</tt><i>n</i></td>
 <td>tree <i>n</i> in the individual</td></tr>
 </table>

 *
 * @author Sean Luke
 * @version 1.0 
 */

public class GPIndividual extends Individual
    {
    public static final String P_NUMTREES = "numtrees";
    public static final String P_TREE = "tree";
    
    public GPTree[] trees;
    
    public Parameter defaultBase()
        {
        return GPDefaults.base().push(P_INDIVIDUAL);
        }

    public boolean equals(Object ind)
        {
        if (!(this.getClass().equals(ind.getClass()))) return false;  // GPIndividuals are special.
        GPIndividual i = (GPIndividual)ind;
        if (trees.length != i.trees.length) return false;
        // this default version works fine for most GPIndividuals.
        for(int x=0;x<trees.length;x++)
            if (!(trees[x].treeEquals(i.trees[x]))) return false;
        return true;
        }
    
    public int hashCode()
        {
        // stolen from GPNode.  It's a decent algorithm.
        int hash = this.getClass().hashCode();
        
        for(int x=0;x<trees.length;x++)
            hash =
                // Rotate hash and XOR
                (hash << 1 | hash >>> 31 ) ^
                trees[x].treeHashCode();
        return hash;
        }

    /** Sets up a prototypical GPIndividual with those features which it
        shares with other GPIndividuals in its species, and nothing more. */

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);  // actually unnecessary (Individual.setup() is empty)

        Parameter def = defaultBase();

        // set my evaluation to false
        evaluated = false;

        // how many trees?
        int t = state.parameters.getInt(base.push(P_NUMTREES),def.push(P_NUMTREES),1);  // at least 1 tree for GP!
        if (t <= 0) 
            state.output.fatal("A GPIndividual must have at least one tree.",
                               base.push(P_NUMTREES),def.push(P_NUMTREES));
        
        // load the trees
        trees = new GPTree[t];

        for (int x=0;x<t;x++)
            {
            Parameter p = base.push(P_TREE).push(""+x);
            trees[x] = (GPTree)(state.parameters.getInstanceForParameterEq(
                                    p,def.push(P_TREE).push(""+x),GPTree.class));
            trees[x].owner = this;
            trees[x].setup(state,p);
            }
        
        // now that our function sets are all associated with trees,
        // give the nodes a chance to determine whether or not this is
        // going to work for them (especially the ADFs).
        GPInitializer initializer = ((GPInitializer)state.initializer);
        for (int x=0;x<t;x++)
            {
            for(int w = 0;w < trees[x].constraints(initializer).functionset.nodes.length;w++)
                {
                GPNode[] gpfi = trees[x].constraints(initializer).functionset.nodes[w];
                for (int y = 0;y<gpfi.length;y++)
                    gpfi[y].checkConstraints(state,x,this,base);
                }
            }
        // because I promised with checkConstraints(...)
        state.output.exitIfErrors();
        }

    /** Overridden for the GPIndividual genotype, writing each tree in turn. */
    public void printIndividualForHumans(final EvolutionState state, final int log, 
                                         final int verbosity)
        {
        state.output.println(EVALUATED_PREAMBLE + (evaluated ? "true" : "false"), 
                             verbosity, log);
        fitness.printFitnessForHumans(state,log,verbosity);
        for(int x=0;x<trees.length;x++)
            {
            state.output.println("Tree " + x + ":",verbosity,log);
            trees[x].printTreeForHumans(state,log,verbosity);
            }
        }

    /** Overridden for the GPIndividual genotype, writing each tree in turn. */
    public void printIndividual(final EvolutionState state, final int log, 
                                final int verbosity)
        {
        state.output.println(EVALUATED_PREAMBLE + Code.encode(evaluated), 
                             verbosity, log);
        fitness.printFitness(state,log,verbosity);
        for(int x=0;x<trees.length;x++)
            {
            state.output.println("Tree " + x + ":",verbosity,log);
            trees[x].printTree(state,log,verbosity);
            }   
        }
            
    /** Overridden for the GPIndividual genotype, writing each tree in turn. */
    public void printIndividual(final EvolutionState state,
                                final PrintWriter writer)
        {
        writer.println(EVALUATED_PREAMBLE + Code.encode(evaluated));
        fitness.printFitness(state,writer);
        for(int x=0;x<trees.length;x++)
            {
            writer.println("Tree " + x + ":");
            trees[x].printTree(state,writer);
            }   
        }
        
    /** Overridden for the GPIndividual genotype. */
    public void writeGenotype(final EvolutionState state,
                              final DataOutput dataOutput) throws IOException
        {
        dataOutput.writeInt(trees.length);
        for(int x=0;x<trees.length;x++)
            trees[x].writeTree(state,dataOutput);
        }

    /** Overridden for the GPIndividual genotype. */
    public void readGenotype(final EvolutionState state,
                             final DataInput dataInput) throws IOException
        {
        int treelength = dataInput.readInt();
        if (trees == null || treelength != trees.length) // wrong size!
            state.output.fatal("Number of trees differ in GPIndividual when reading from readGenotype(EvolutionState, DataInput).");
        for(int x=0;x<trees.length;x++)
            trees[x].readTree(state,dataInput);
        }

    /** Overridden for the GPIndividual genotype. */
    
    public void readIndividual(final EvolutionState state,
                               final LineNumberReader reader) 
        throws IOException
        {
        /*
        // First, was I evaluated?
        int linenumber = reader.getLineNumber();
        String s = reader.readLine();
        if (s==null || s.length() < EVALUATED_PREAMBLE.length()) // uh oh
        state.output.fatal("Reading Line " + linenumber + ": " +
        "Bad 'Evaluated?' line.");
        DecodeReturn d = new DecodeReturn(s, EVALUATED_PREAMBLE.length());
        Code.decode(d);
        if (d.type!=DecodeReturn.T_BOOLEAN)
        state.output.fatal("Reading Line " + linenumber + ": " +
        "Bad 'Evaluated?' line.");
        evaluated = (d.l!=0);
        */
        evaluated = Code.readBooleanWithPreamble(EVALUATED_PREAMBLE, state, reader);

        // Next, what's my fitness?
        fitness.readFitness(state,reader);

        // Next, read my trees
        for(int x=0;x<trees.length;x++)
            {
            reader.readLine();  // throw it away -- it's the tree indicator
            trees[x].readTree(state,reader);
            }

        }

    /** Deep-clones the GPIndividual.  Note that you should not deep-clone the prototypical GPIndividual
        stored in GPSpecies: they contain blank GPTrees with null roots, and this method,
        which calls GPTree.clone(), will produce a NullPointerException as a result. Instead, you probably
        want to use GPSpecies.newIndividual(...) if you're thinking of playing with the prototypical
        GPIndividual. */
        
    public Object clone()
        {
        // a deep clone
                
        GPIndividual myobj = (GPIndividual)(super.clone());

        // copy the tree array
        myobj.trees = new GPTree[trees.length];
        for(int x=0;x<trees.length;x++)
            {
            myobj.trees[x] = (GPTree)(trees[x].clone());  // force a deep clone
            myobj.trees[x].owner = myobj;  // reset owner away from me
            }
        return myobj;
        }

    /** Like clone(), but doesn't force the GPTrees to deep-clone themselves. */
    public GPIndividual lightClone()
        {
        // a light clone
        GPIndividual myobj = (GPIndividual)(super.clone());
        
        // copy the tree array
        myobj.trees = new GPTree[trees.length];
        for(int x=0;x<trees.length;x++)
            {
            myobj.trees[x] = (GPTree)(trees[x].lightClone());  // note light-cloned!
            myobj.trees[x].owner = myobj;  // reset owner away from me
            }
        return myobj;
        }

    /** Returns the "size" of the individual, namely, the number of nodes
        in all of its subtrees.  */
    public long size()
        {
        long size = 0;
        for(int x=0;x<trees.length;x++)
            size += trees[x].child.numNodes(GPNode.NODESEARCH_ALL);
        return size;
        }

    }
