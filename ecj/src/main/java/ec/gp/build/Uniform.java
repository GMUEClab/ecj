/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp.build;
import ec.gp.*;
import java.util.*;
import java.math.*;
import ec.util.*;
import ec.*;
import java.io.*;

/* 
 * Uniform.java
 * 
 * Created Fri Jan 26 14:02:08 EST 2001
 * By: Sean Luke
 */

/**
   Uniform implements the algorithm described in 

   <p>Bohm, Walter and Andreas Geyer-Schulz. 1996. "Exact Uniform Initialization for Genetic Programming".  In <i>Foundations of Genetic Algorithms IV,</i> Richard Belew and Michael Vose, eds.  Morgan Kaufmann.  379-407. (ISBN 1-55860-460-X) 

   <p> The user-provided requested tree size is either provided directly to the Uniform algorithm, or if the size is NOSIZEGIVEN, then Uniform will pick one at random from the GPNodeBuilder probability distribution system (using either max-depth and min-depth, or using num-sizes).  

   <p>Further, if the user sets the <tt>true-dist</tt> parameter, the Uniform will ignore the user's specified probability distribution and instead pick from a distribution between the minimum size and the maximum size the user specified, where the sizes are distributed according to the <i>actual</i> number of trees that can be created with that size.  Since many more trees of size 10 than size 3 can be created, for example, size 10 will be picked that much more often.

   <p>Uniform also prints out the actual number of trees that exist for a given size, return type, and function set.  As if this were useful to you.  :-)

   <p> The algorithm, which is quite complex, is described in pseudocode below.  Basically what the algorithm does is this:

   <ol>
   <li> For each function set and return type, determine the number of trees of each size which exist for that function set and tree type.  Also determine all the permutations of tree sizes among children of a given node.  All this can be done with dynamic programming.  Do this just once offline, after the function sets are loaded. 
   <li> Using these tables, construct distributions of choices of tree size, child tree size permutations, etc.
   <li> When you need to create a tree, pick a size, then use the distriutions to recursively create the tree (top-down).
   </ol>

   <p> <b>Dealing with Zero Distributions</b>
   <p> Some domains have NO tree of a certain size.  For example, 
   Artificial Ant's function set can make NO trees of size 2.
   What happens when we're asked to make a tree of (invalid) size 2 in
   Artificial Ant then?  Uniform presently handles it as follows:
   <ol><li> If the system specifically requests a given size that's invalid, Uniform will 
   look for the next larger size which is valid.  If it can't find any,
   it will then look for the next smaller size which is valid.
   <li> If a random choice yields a given size that's invalid,
   Uniform will pick again.
   <li> If there is *no* valid size for a given return type, which probably indicates
   an error, Uniform will halt and complain.
   </ol>
        
   <h3>Pseudocode:</h3>

   <pre>

   *    Func NumTreesOfType(type,size)
   *        If NUMTREESOFTYPE[type,size] not defined,       // memoize
   *            N[type] = all nodes compatible with type
   *            NUMTREESOFTYPE[type,size] = Sum(n in N[type], NumTreesRootedByNode(n,size))
   *            return NUMTREESOFTYPE[type,size]
   *
   *    Func NumTreesRootedByNode(node,size)
   *        If NUMTREESROOTEDBYNODE[node,size] not defined,   // memoize
   *            count = 0
   *            left = size - 1
   *            If node.children.length = 0 and left = 0  // a valid terminal
   *                count = 1
   *            Else if node.children.length <= left  // a valid nonterminal
   *                For s is 1 to left inclusive  // yeah, that allows some illegal stuff, it gets set to 0
   *                    count += NumChildPermutations(node,s,left,0)
   *            NUMTREESROOTEDBYNODE[node,size] = count
   *        return NUMTREESROOTEBYNODE[node,size]
   *
   *
   *    Func NumChildPermutations(parent,size,outof,pickchild)
   *    // parent is our parent node
   *    // size is the size of pickchild's tree that we're considering
   *    // pickchild is the child we're considering
   *    // outof is the total number of remaining nodes (including size) yet to fill
   *        If NUMCHILDPERMUTATIONS[parent,size,outof,pickchild] is not defined,        // memoize
   *            count = 0
   *            if pickchild = parent.children.length - 1        and outof==size        // our last child, outof must be size
   *                count = NumTreesOfType(parent.children[pickchild].type,size)
   *            else if pickchild < parent.children.length - 1 and 
   *                                outof-size >= (parent.children.length - pickchild-1)    // maybe we can fill with terminals
   *                cval = NumTreesOfType(parent.children[pickchild].type,size)
   *                tot = 0
   *                For s is 1 to outof-size // some illegal stuff, it gets set to 0
   *                    tot += NumChildPermutations(parent,s,outof-size,pickchild+1)
   *                count = cval * tot
   *            NUMCHILDPERMUTATIONS [parent,size,outof,pickchild] = count            
   *        return NUMCHILDPERMUTATIONS[parent,size,outof,pickchild]
   *
   *
   *    For each type type, size size
   *        ROOT_D[type,size] = probability distribution of nodes of type and size, derived from
   *                            NUMTREESOFTYPE[type,size], our node list, and NUMTREESROOTEDBYNODE[node,size]
   *
   *    For each parent,outof,pickchild
   *        CHILD_D[parent,outof,pickchild] = probability distribution of tree sizes, derived from
   *                            NUMCHILDPERMUTATIONS[parent,size,outof,pickchild]
   *
   *    Func FillNodeWithChildren(parent,pickchild,outof)
   *        If pickchild = parent.children.length - 1               // last child
   *            Fill parent.children[pickchild] with CreateTreeOfType(parent.children[pickchild].type,outof)
   *        Else choose size from CHILD_D[parent,outof,pickchild]
   *            Fill parent.pickchildren[pickchild] with CreateTreeOfType(parent.children[pickchild].type,size)
   *            FillNodeWithChildren(parent,pickchild+1,outof-size)
   *        return
   </pre>

   Func CreateTreeOfType(type,size)
   Choose node from ROOT_D[type,size]
   If size > 1
   FillNodeWithChildren(node,0,size-1)
   return node


   <p><b>Parameters</b><br>
   <table>
   <tr><td valign=top><i>base</i>.<tt>true-dist</tt><br>
   <font size=-1>bool= true or false (default)</font></td>
   <td valign=top>(should we use the true numbers of trees for each size as the distribution for picking trees, as opposed to the user-specified distribution?)</td></tr>
   </table>

   <p><b>Default Base</b><br>
   gp.build.uniform

*/



public class Uniform extends GPNodeBuilder 
    {
    public static final String P_UNIFORM = "uniform";
    public static final String P_TRUEDISTRIBUTION = "true-dist";
    
    public Parameter defaultBase()
        {
        return GPBuildDefaults.base().push(P_UNIFORM);
        }

    // Mapping of integers to function sets
    public GPFunctionSet[] functionsets;
    
    // Mapping of function sets to Integers
    public Hashtable _functionsets;
    
    // Mapping of GPNodes to Integers (thus to ints)
    public Hashtable funcnodes;
    
    // number of nodes
    public int numfuncnodes;
    
    // max arity of any node
    public int maxarity;
    
    // maximum size of nodes computed
    public int maxtreesize;
    
    // true size distributions
    public BigInteger[/*functionset*/][/*type*/][/*size*/] _truesizes;
    public double[/*functionset*/][/*type*/][/*size*/] truesizes;
    
    // do we use the true distributions to pick tree sizes?
    public boolean useTrueDistribution;
    
    // Sun in its infinite wisdom (what idiots) decided to make
    // BigInteger IMMUTABLE.  There is a MutableBigInteger, but it's not
    // public!  And Sun only caches the first 16 positive and 16 negative
    // integer constants, not exactly that useful for us.  As a result, we'll
    // be making a dang lot of BigIntegers here.  Garbage-collection hell.  :-(
    // ...well, it's not all that slow really.
    public BigInteger NUMTREESOFTYPE[/*FunctionSet*/][/*type*/][/*size*/];
    public BigInteger NUMTREESROOTEDBYNODE[/*FunctionSet*/][/*nodenum*/][/*size*/];
    public BigInteger NUMCHILDPERMUTATIONS[/*FunctionSet*/][/*parentnodenum*/][/*size*/][/*outof*/][/*pickchild*/];
    
    
    
    // tables derived from the previous ones through some massaging
    
    // [/*the nodes*/] is an array of <node,probability> pairs for all possible nodes rooting
    // trees of the desired size and compatible with the given return type.  It says that if you
    // were to pick a tree, this would be the probability that this node would be the root of it.
    public UniformGPNodeStorage ROOT_D[/*FunctionSet*/][/*type*/][/*size*/][/*the nodes*/];
    
    // True if ROOT_D all zero for all possible nodes in [/*the nodes*/] above. 
    public boolean ROOT_D_ZERO[/*FunctionSet*/][/*type*/][/*size*/];
    
    public double CHILD_D[/*FunctionSet*/][/*type*/][/*outof*/][/*pickchild*/][/* the nodes*/];
    
    
    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        
        Parameter def = defaultBase();
        
        // use true distributions? false is default
        useTrueDistribution = state.parameters.getBoolean(
            base.push(P_TRUEDISTRIBUTION), def.push(P_TRUEDISTRIBUTION),false);
        
        if (minSize>0)  // we're using maxSize and minSize
            maxtreesize=maxSize;
        else if (sizeDistribution != null)
            maxtreesize = sizeDistribution.length;
        else state.output.fatal("Uniform is used for the GP node builder, but no distribution was specified." +
            "  You must specify either a min/max size, or a full size distribution.",
            base.push(P_MINSIZE), def.push(P_MINSIZE));
        // preprocess offline
        preprocess(state,maxtreesize);
        }
        
    public int pickSize(final EvolutionState state, final int thread, 
        final int functionset, final int type)
        {
        if (useTrueDistribution)
            return RandomChoice.pickFromDistribution(
                truesizes[functionset][type],state.random[thread].nextDouble());
        else return super.pickSize(state,thread);
        }
    
    public void preprocess(final EvolutionState state, final int _maxtreesize)
        {
        state.output.message("Determining Tree Sizes");
        
        maxtreesize = _maxtreesize;
        
        Hashtable functionSetRepository = ((GPInitializer)state.initializer).functionSetRepository;
        
        // Put each function set into the arrays
        functionsets = new GPFunctionSet[functionSetRepository.size()];
        _functionsets = new Hashtable();
        Enumeration e = functionSetRepository.elements();
        int count=0;
        while(e.hasMoreElements())
            {
            GPFunctionSet set = (GPFunctionSet)(e.nextElement());
            _functionsets.put(set,Integer.valueOf(count));
            functionsets[count++] = set;
            }
        
        // For each function set, assign each GPNode to a unique integer
        // so we can keep track of it (ick, this will be inefficient!)
        funcnodes = new Hashtable();
        Hashtable t_nodes = new Hashtable();
        count = 0;
        maxarity=0;
        GPNode n;
        for(int x=0;x<functionsets.length;x++)
            {
            // hash all the nodes so we can remove duplicates
            for(int typ=0;typ<functionsets[x].nodes.length;typ++)
                for(int nod=0;nod<functionsets[x].nodes[typ].length;nod++)
                    t_nodes.put(n=functionsets[x].nodes[typ][nod],n);
            // rehash with Integers, yuck
            e = t_nodes.elements();
            GPNode tmpn;
            while(e.hasMoreElements())
                {
                tmpn = (GPNode)(e.nextElement());
                if (maxarity < tmpn.children.length) 
                    maxarity = tmpn.children.length;
                if (!funcnodes.containsKey(tmpn))  // don't remap the node; it'd make holes
                    funcnodes.put(tmpn,new Integer(count++));
                }
            }
        
        numfuncnodes = funcnodes.size();
        
        GPInitializer initializer = ((GPInitializer)state.initializer);
        int numAtomicTypes = initializer.numAtomicTypes;
        int numSetTypes = initializer.numSetTypes;
        
        // set up the arrays
        NUMTREESOFTYPE = new BigInteger[functionsets.length][numAtomicTypes+numSetTypes][maxtreesize+1];
        NUMTREESROOTEDBYNODE = new BigInteger[functionsets.length][numfuncnodes][maxtreesize+1];
        NUMCHILDPERMUTATIONS = new BigInteger[functionsets.length][numfuncnodes][maxtreesize+1][maxtreesize+1][maxarity];
        ROOT_D = new UniformGPNodeStorage[functionsets.length][numAtomicTypes+numSetTypes][maxtreesize+1][];
        ROOT_D_ZERO = new boolean[functionsets.length][numAtomicTypes+numSetTypes][maxtreesize+1];
        CHILD_D = new double[functionsets.length][numfuncnodes][maxtreesize+1][maxtreesize+1][];

        GPType[] types = ((GPInitializer)(state.initializer)).types;
        // Go through each function set and determine numbers
        // (this will take quite a while!  Thankfully it's offline)
        _truesizes = new BigInteger[functionsets.length][numAtomicTypes+numSetTypes][maxtreesize+1];
        for(int x=0;x<functionsets.length;x++)
            for(int y=0;y<numAtomicTypes+numSetTypes;y++)
                for(int z=1;z<=maxtreesize;z++)
                    state.output.message("FunctionSet: " + functionsets[x].name + ", Type: " + types[y].name + ", Size: " + z + " num: " + 
                        (_truesizes[x][y][z] = numTreesOfType(initializer,x,y,z)));

        state.output.message("Compiling Distributions");

        // convert to doubles and organize distribution
        truesizes = new double[functionsets.length][numAtomicTypes+numSetTypes][maxtreesize+1];
        for(int x=0;x<functionsets.length;x++)
            for(int y=0;y<numAtomicTypes+numSetTypes;y++)
                {
                for(int z=1;z<=maxtreesize;z++)
                    truesizes[x][y][z] = _truesizes[x][y][z].doubleValue();
                // and if this is all zero (a possibility) we should be forgiving (hence the 'true') -- I *think*
                RandomChoice.organizeDistribution(truesizes[x][y],true);
                }
        
        // compute our percentages
        computePercentages();
        }
    
    // hopefully this will get inlined
    public final int intForNode(GPNode node)
        {
        return ((Integer)(funcnodes.get(node))).intValue();
        }
    
    
    public BigInteger numTreesOfType(final GPInitializer initializer, 
        final int functionset, final int type, final int size)
        {
        if (NUMTREESOFTYPE[functionset][type][size]==null)
            {
            GPNode[] nodes = functionsets[functionset].nodes[type];
            BigInteger count = BigInteger.valueOf(0);
            for(int x=0;x<nodes.length;x++)
                count = count.add(numTreesRootedByNode(initializer,functionset,nodes[x],size));
            NUMTREESOFTYPE[functionset][type][size] = count;
            }
        return NUMTREESOFTYPE[functionset][type][size];
        }
    
    public BigInteger numTreesRootedByNode(final GPInitializer initializer,
        final int functionset, final GPNode node, final int size)
        {
        if (NUMTREESROOTEDBYNODE[functionset][intForNode(node)][size]==null)
            {
            BigInteger one = BigInteger.valueOf(1);
            BigInteger count = BigInteger.valueOf(0);
            int outof = size-1;
            if (node.children.length == 0 && outof == 0) // a valid terminal
                count = one;
            else if (node.children.length <= outof)  // a valid nonterminal
                for (int s=1;s<=outof;s++)
                    count = count.add(numChildPermutations(initializer,functionset,node,s,outof,0));
            //System.out.println("Node: " + node + " Size: " + size + " Count: " +count);
            NUMTREESROOTEDBYNODE[functionset][intForNode(node)][size] = count;
            }
        return NUMTREESROOTEDBYNODE[functionset][intForNode(node)][size];
        }
    
    public BigInteger numChildPermutations( final GPInitializer initializer,
        final int functionset, final GPNode parent, final int size,
        final int outof, final int pickchild)
        {
        if (NUMCHILDPERMUTATIONS[functionset][intForNode(parent)][size][outof][pickchild]==null)
            {
            BigInteger count = BigInteger.valueOf(0);
            if (pickchild == parent.children.length - 1 && size==outof)
                count = numTreesOfType(initializer,functionset,parent.constraints(initializer).childtypes[pickchild].type,size);
            else if (pickchild < parent.children.length - 1 && 
                outof-size >= (parent.children.length - pickchild-1))
                {
                BigInteger cval = numTreesOfType(initializer,functionset,parent.constraints(initializer).childtypes[pickchild].type,size);
                BigInteger tot = BigInteger.valueOf(0);
                for (int s=1; s<=outof-size; s++)
                    tot = tot.add(numChildPermutations(initializer,functionset,parent,s,outof-size,pickchild+1));
                count = cval.multiply(tot);
                }
            // System.out.println("Parent: " + parent + " Size: " + size + " OutOf: " + outof + 
            //       " PickChild: " + pickchild + " Count: " +count);
            NUMCHILDPERMUTATIONS[functionset][intForNode(parent)][size][outof][pickchild] = count;
            }
        return NUMCHILDPERMUTATIONS[functionset][intForNode(parent)][size][outof][pickchild];
        }
    
    private final double getProb(final BigInteger i)
        {
        if (i==null) return 0.0f;
        else return i.doubleValue();
        }
        
    public void computePercentages()
        {
        // load ROOT_D
        for(int f = 0;f<NUMTREESOFTYPE.length;f++)
            for(int t=0;t<NUMTREESOFTYPE[f].length;t++)
                for(int s=0;s<NUMTREESOFTYPE[f][t].length;s++)
                    {
                    ROOT_D[f][t][s] = new UniformGPNodeStorage[functionsets[f].nodes[t].length];
                    for(int x=0;x<ROOT_D[f][t][s].length;x++)
                        {
                        ROOT_D[f][t][s][x] = new UniformGPNodeStorage();
                        ROOT_D[f][t][s][x].node = functionsets[f].nodes[t][x];
                        ROOT_D[f][t][s][x].prob = getProb(NUMTREESROOTEDBYNODE[f][intForNode(ROOT_D[f][t][s][x].node)][s]);
                        }
                    // organize the distribution
                    //System.out.println("Organizing " + f + " " + t + " " + s);
                    // check to see if it's all zeros
                    for(int x=0;x<ROOT_D[f][t][s].length;x++)
                        if (ROOT_D[f][t][s][x].prob != 0.0)
                            {
                            // don't need to check for negatives here I believe
                            RandomChoice.organizeDistribution(ROOT_D[f][t][s],ROOT_D[f][t][s][0]);
                            ROOT_D_ZERO[f][t][s] = false;
                            break;
                            }
                        else
                            {
                            ROOT_D_ZERO[f][t][s] = true;
                            }
                    }

        // load CHILD_D
        for(int f = 0;f<NUMCHILDPERMUTATIONS.length;f++)
            for(int p=0;p<NUMCHILDPERMUTATIONS[f].length;p++)
                for(int o=0;o<maxtreesize+1;o++)
                    for(int c=0;c<maxarity;c++)
                        {
                        CHILD_D[f][p][o][c] = new double[o+1];
                        for(int s=0;s<CHILD_D[f][p][o][c].length;s++)
                            CHILD_D[f][p][o][c][s] = getProb(NUMCHILDPERMUTATIONS[f][p][s][o][c]);
                        // organize the distribution
                        //System.out.println("Organizing " + f + " " + p + " " + o + " " + c);
                        // check to see if it's all zeros
                        for(int x=0;x<CHILD_D[f][p][o][c].length;x++)
                            if (CHILD_D[f][p][o][c][x] != 0.0)
                                {
                                // don't need to check for negatives here I believe
                                RandomChoice.organizeDistribution(CHILD_D[f][p][o][c]);
                                break;
                                }
                        }
        }
        
    GPNode createTreeOfType(final EvolutionState state, final int thread, final GPInitializer initializer, 
        final int functionset, final int type, final int size, final MersenneTwisterFast mt)
        
        {
        //System.out.println("" + functionset + " " + type + " " + size);
        int choice = RandomChoice.pickFromDistribution(
            ROOT_D[functionset][type][size],ROOT_D[functionset][type][size][0],
            mt.nextDouble());
        GPNode node = (GPNode)(ROOT_D[functionset][type][size][choice].node.lightClone());
        node.resetNode(state,thread);  // give ERCs a chance to randomize
        //System.out.println("Size: " + size + "Rooted: " + node);
        if (node.children.length == 0 && size !=1) // uh oh
            {
            System.out.println("Size: " + size + " Node: " + node);
            for(int x=0;x<ROOT_D[functionset][type][size].length;x++)
                System.out.println("" + x + (GPNode)(ROOT_D[functionset][type][size][x].node) + " " + ROOT_D[functionset][type][size][x].prob );
            }
        if (size > 1)  // nonterminal
            fillNodeWithChildren(state,thread,initializer,functionset,node,ROOT_D[functionset][type][size][choice].node,0,size-1,mt);
        return node;
        }
       
    void fillNodeWithChildren(final EvolutionState state, final int thread, final GPInitializer initializer,
        final int functionset, final GPNode parent, final GPNode parentc, 
        final int pickchild, final int outof, final MersenneTwisterFast mt)
        
        {
        if (pickchild == parent.children.length - 1)
            {
            parent.children[pickchild] = 
                createTreeOfType(state,thread,initializer,functionset,parent.constraints(initializer).childtypes[pickchild].type,outof, mt);
            }
        else 
            {
            int size = RandomChoice.pickFromDistribution(
                CHILD_D[functionset][intForNode(parentc)][outof][pickchild],
                mt.nextDouble());
            parent.children[pickchild] = 
                createTreeOfType(state,thread,initializer,functionset,parent.constraints(initializer).childtypes[pickchild].type,size,mt);
            fillNodeWithChildren(state,thread,initializer,functionset,parent,parentc,pickchild+1,outof-size,mt);
            }
        parent.children[pickchild].parent = parent;
        parent.children[pickchild].argposition = (byte)pickchild;            
        }
        

    public GPNode newRootedTree(final EvolutionState state,
        final GPType type,
        final int thread,
        final GPNodeParent parent,
        final GPFunctionSet set,
        final int argposition,
        final int requestedSize)
        {
        GPInitializer initializer = ((GPInitializer)state.initializer);
        
        if (requestedSize == NOSIZEGIVEN)  // pick from the distribution
            {
            final int BOUNDARY = 20;  // if we try 20 times and fail, check to see if it's possible to succeed
            int bound=0;
                
            int fset = ((Integer)(_functionsets.get(set))).intValue();
            int siz = pickSize(state,thread,fset,type.type);
            int typ = type.type;
            
            // this code is confusing.  The idea is:
            // if the number of trees of our arbitrarily-picked size is zero, we try BOUNDARY
            // number of times to find a tree which will work, picking new sizes each
            // time.  If we still haven't found anything, we will continue to search
            // for a working tree only if we know for sure that one exists in the distribution.
            
            boolean checked = false;
            while(ROOT_D_ZERO[fset][typ][siz])
                {
                if (++bound == BOUNDARY)
                    {
                    check: 
                    if (!checked) 
                        {
                        checked = true;
                        for(int x=0;x<ROOT_D_ZERO[fset][typ].length;x++)
                            if (!ROOT_D_ZERO[fset][typ][x]) 
                                break check;  // found a non-zero
                        // uh oh, we're all zeroes
                        state.output.fatal("ec.gp.build.Uniform was asked to build a tree with functionset " + set + " rooted with type " + type + ", but cannot because for some reason there are no trees of any valid size (within the specified size range) which exist for this function set and type.");       
                        }   
                    }
                siz = pickSize(state,thread,fset,typ);
                }
                    
            // okay, now we have a valid size.
            GPNode n = createTreeOfType(state,thread,initializer,fset,typ,siz,state.random[thread]);
            n.parent = parent;
            n.argposition = (byte)argposition;
            return n;
            }
        else if (requestedSize<1)
            {
            state.output.fatal("ec.gp.build.Uniform requested to build a tree, but a requested size was given that is < 1.");
            return null;  // never happens
            }
        else 
            {
            int fset = ((Integer)(_functionsets.get(set))).intValue();
            int typ = type.type;
            int siz = requestedSize;
            
            // if the number of trees of the requested size is zero, we first march up until we
            // find a tree size with non-zero numbers of trees.  Failing that, we march down to
            // find one.  If that still fails, we issue an error.  Otherwise we use the size
            // we discovered.
            
            determineSize:
            if (ROOT_D_ZERO[fset][typ][siz])
                {
                // march up
                for(int x=siz+1;x<ROOT_D_ZERO[fset][typ].length;x++)
                    if (ROOT_D_ZERO[fset][typ][siz])
                        { siz=x; break determineSize; }
                // march down
                for(int x=siz-1;x>=0;x--)
                    if (ROOT_D_ZERO[fset][typ][siz])
                        { siz=x; break determineSize; }
                // issue an error
                state.output.fatal("ec.gp.build.Uniform was asked to build a tree with functionset " + set + " rooted with type " + type + ", and of size " + requestedSize + ", but cannot because for some reason there are no trees of any valid size (within the specified size range) which exist for this function set and type.");
                }
                
            GPNode n = createTreeOfType(state,thread,initializer,fset,typ,siz,state.random[thread]);
            n.parent = parent;
            n.argposition = (byte)argposition;
            return n;
            }
        }
        
    }
    
    
class UniformGPNodeStorage implements RandomChoiceChooserD, Serializable
    {
    public GPNode node;
    public double prob;
    public double getProbability(final Object obj)
        { return (((UniformGPNodeStorage)obj).prob); }
    public void setProbability(final Object obj, final double _prob)
        { ((UniformGPNodeStorage)obj).prob = _prob; }
    }

    
    
    
    
    
    

    
    
    
    
    
    
    
    
