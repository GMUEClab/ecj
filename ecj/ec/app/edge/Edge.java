/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.edge;
import ec.util.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;

/* 
 * Edge.java
 * 
 * Created: Mon Nov  1 15:46:19 1999
 * By: Sean Luke
 */

/**
 * Edge implements the Symbolic Edge problem.
 *
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>data</tt><br>
 <font size=-1>classname, inherits or == ec.app.edge.EdgeData</font></td>
 <td valign=top>(the class for the prototypical GPData object for the Edge problem)</td></tr>
 </table>

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>data</tt></td>
 <td>species (the GPData object)</td></tr>
 </table>
 *
 * @author Sean Luke
 * @version 1.0 
 */

public class Edge extends GPProblem implements SimpleProblemForm
    {
    public static final String P_GENERALIZE = "generalize";
    public static final String P_ALLPOS = "allpos";
    public static final String P_ALLNEG = "allneg";
    public static final String P_TESTPOS = "testpos";
    public static final String P_TESTNEG = "testneg";
    public static final String P_MAXTEST = "maxtest";

    public static final int MIN_ARRAY_SIZE = 64;

    // reading states (BAD is initial state)
    public static final int BAD = 0;
    public static final int READING0 = 1;
    public static final int READING1 = 2;
    public static final int EPSILON = 3;

    // we'll need to deep clone this one though.
    public EdgeData input;

    // building graph
    public boolean[] start;
    public boolean[] accept;
    public int numNodes;
    public int[] from;
    public int[] to;
    public int[] reading;
    public int numEdges;

    // adjacency lists
    public int[][] reading1;
    public int[] reading1_l;
    public int[][] reading0;
    public int[] reading0_l;
    public int[][] epsilon;
    public int[] epsilon_l;

    // positive test
    public boolean[][] posT;
    // negative test
    public boolean[][] negT;
    // positive all
    public boolean[][] posA;
    // negative all
    public boolean[][] negA;

    // testing
    public boolean[] state1;
    public boolean[] state2;

    // generalize?
    public boolean generalize;

    public Object clone()
        {
        // we don't need to copy any of our arrays, they're null until
        // we actually start using them.

        Edge myobj = (Edge) (super.clone());

        // we also don't need to clone the positive/negative
        // examples, since they don't change through the course
        // of our run (I hope!)  Otherwise we'd need to clone them
        // here.

        // clone our data object
        myobj.input = (EdgeData)(input.clone());
        return myobj;
        }

    public static String fill(int num, char c)
        {
        char[] buf = new char[num];
        for(int x=0;x<num;x++) buf[x]=c;
        return new String(buf);
        }

    public static final int J_LEFT = 0;
    public static final int J_RIGHT = 1;
    public static final int J_CENTER = 2;
    public static String justify(final String s, final int len, final int justification)
        {
        int size = len - s.length();
        if (size<0) size=0;
        switch(justification)
            {
            case J_LEFT:
                return s + fill(size,' ');
            case J_RIGHT:
                return fill(size,' ') + s;
            default: // (J_CENTER)
                return fill(size/2,' ') + s + fill(size-(size/2),' ');
            }
        }

    public String printCurrentNFA()
        {
        int strsize = String.valueOf(numNodes).length();
        String str = "";
        for(int x=0;x<numNodes;x++)
            {
            str += justify(String.valueOf(x),strsize,J_RIGHT) + " " + 
                (start[x] ? "S" : " ") + (accept[x] ? "A" : " ") + 
                " -> ";

            if (reading0_l[x]>0)
                { 
                str += "(0:";
                for(int y=0;y<reading0_l[x];y++)
                    str += ((y>0 ? "," : "") + String.valueOf(reading0[x][y]));
                str += ") ";
                }

            if (reading1_l[x]>0)
                { 
                str += "(1:";
                for(int y=0;y<reading1_l[x];y++)
                    str += ((y>0 ? "," : "") + String.valueOf(reading1[x][y]));
                str += ") ";
                }

            if (epsilon_l[x]>0)
                { 
                str += "(e:";
                for(int y=0;y<epsilon_l[x];y++)
                    str += ((y>0 ? "," : "") + String.valueOf(epsilon[x][y]));
                str += ")";
                }
            str += "\n";
            }
        return str;
        }

    public boolean[][] restrictToSize(int size, boolean[][]cases, EvolutionState state, int thread)
        {
        int csize = cases.length;
        if (csize < size) return cases;

        Hashtable hash = new Hashtable();
        for(int x=0;x<size;x++)
            {
            while(true)
                {
                boolean[] b = cases[state.random[thread].nextInt(csize)];
                if (!hash.contains(b)) { hash.put(b,b); break; }
                }
            }
        
        boolean[][] newcases = new boolean[size][];
        Enumeration e = hash.keys();
        for(int x=0;x<size;x++)
            {
            newcases[x] = (boolean[])(e.nextElement());
            }

        // sort the cases -- amazing, but hashtable doesn't always
        // return the same ordering, I guess that's because it does
        // pointer hashing.  Just want to guarantee replicability!

        // is this correct?
        java.util.Arrays.sort(newcases,
                              new java.util.Comparator()
                                  {
                                  public int compare(Object a, Object b)
                                      {
                                      boolean[] aa = (boolean[])a;
                                      boolean[] bb = (boolean[])b;
                                        
                                      for(int x=0;x<Math.min(aa.length,bb.length);x++)
                                          if (!aa[x] && bb[x]) return -1;
                                          else if (aa[x] && !bb[x]) return 1;
                                      if (aa.length<bb.length) return -1;
                                      if (aa.length>bb.length) return 1;
                                      return 0;
                                      }
                                  });
        return newcases;
        }



    public boolean[][] slurp(final File f)
        throws IOException
        {
        LineNumberReader r = new LineNumberReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(f))));
        String bits;

        Vector v = new Vector();
        while((bits=r.readLine())!=null)
            {
            bits = bits.trim();
            int len = bits.length();
            if (len==0) continue; // empty line
            if (bits.charAt(0)=='#') continue;  // comment
            if (bits.equalsIgnoreCase("e"))
                v.addElement(new boolean[0]);
            else
                {
                boolean[] b = new boolean[len];
                for(int x=0;x<len;x++)
                    b[x] = (bits.charAt(x)=='1');
                v.addElement(b);
                }
            }
        r.close();
        boolean[][] result = new boolean[v.size()][];
        v.copyInto(result);
        return result;
        }


    public void printBits(final EvolutionState state, final boolean[][] bits)
        {
        StringBuffer s;
        for(int x=0;x<bits.length;x++)
            {
            s = new StringBuffer();
            for(int y=0;y<bits[x].length;y++)
                if (bits[x][y]) s.append('1');
                else s.append('0');
            if (s.length()==0) state.output.message("(empty)");
            else state.output.message(s.toString());
            }
        }


    public void setup(final EvolutionState state,
                      final Parameter base)
        {
        // very important, remember this
        super.setup(state,base);

        // do we generalize?
        generalize = state.parameters.getBoolean(base.push(P_GENERALIZE),null,false);

        // load the test examples here

        File ap = null;
        File an = null;
        File tp = null;
        File tn = null;
        int restriction;

        if (generalize)
            {
            ap = state.parameters.getFile(base.push(P_ALLPOS),null);
            an = state.parameters.getFile(base.push(P_ALLNEG),null);
            }

        tp = state.parameters.getFile(base.push(P_TESTPOS),null);
        tn = state.parameters.getFile(base.push(P_TESTNEG),null);

        if (generalize)
            {
            if (ap==null) state.output.error("File doesn't exist", base.push(P_ALLPOS));
            if (an==null) state.output.error("File doesn't exist", base.push(P_ALLNEG));
            }

        if (tp==null) state.output.error("File doesn't exist", base.push(P_TESTPOS));
        if (tn==null) state.output.error("File doesn't exist", base.push(P_TESTNEG));
        state.output.exitIfErrors();

        if (generalize)
            {
            if (!ap.canRead()) state.output.error("File cannot be read", base.push(P_ALLPOS));
            if (!an.canRead()) state.output.error("File cannot be read", base.push(P_ALLNEG));
            }

        if (!tp.canRead()) state.output.error("File cannot be read", base.push(P_TESTPOS));
        if (!tn.canRead()) state.output.error("File cannot be read", base.push(P_TESTNEG));
        state.output.exitIfErrors();

        if (generalize)
            {
            state.output.message("Reading Positive Examples");
            try { posA = slurp(ap); }
            catch(IOException e) { state.output.error(
                                       "IOException reading file (here it is)\n" + e, base.push(P_ALLPOS)); }
            state.output.message("Reading Negative Examples");
            try { negA = slurp(an); }
            catch(IOException e) { state.output.error(
                                       "IOException reading file (here it is)\n" + e, base.push(P_ALLNEG)); }
            }

        state.output.message("Reading Positive Training Examples");
        try { posT = slurp(tp); }
        catch(IOException e) { state.output.error(
                                   "IOException reading file (here it is)\n" + e, base.push(P_TESTPOS)); }
        if ((restriction = state.parameters.getInt(
                 base.push(P_MAXTEST),null,1))>0)
            {
            // Need to restrict
            state.output.message("Restricting to <= " + restriction + " Unique Examples");
            posT = restrictToSize(restriction,posT,state,0);
            }

        state.output.message("");
        printBits(state,posT);
        state.output.message("");

        state.output.message("Reading Negative Training Examples");
        try { negT = slurp(tn); }
        catch(IOException e) { state.output.error(
                                   "IOException reading file (here it is)\n" + e, base.push(P_TESTNEG)); }
        if ((restriction = state.parameters.getInt(
                 base.push(P_MAXTEST),null,1))>0)
            {
            // Need to restrict
            state.output.message("Restricting to <= " + restriction + " Unique Examples");
            negT = restrictToSize(restriction,negT,state,0);
            }

        state.output.message("");
        printBits(state,negT);
        state.output.message("");

        state.output.exitIfErrors();
            

        // set up our input -- don't want to use the default base, it's unsafe
        input = (EdgeData) state.parameters.getInstanceForParameterEq(
            base.push(P_DATA), null, EdgeData.class);
        input.setup(state,base.push(P_DATA));
        }


    public boolean test(final boolean[] sample)
        {
        final boolean STATE_1 = false;
//        final boolean STATE_2 = true;
        boolean st = STATE_1;
        
        // set initial state
        for(int x=0;x<numNodes;x++)
            state1[x]=start[x];

        // run
        for(int x=0;x<sample.length;x++)
            {
            if (st==STATE_1)
                {
                for(int y=0;y<numNodes;y++)
                    state2[y]=false;
                for(int y=0;y<numNodes;y++)  // yes, *start*.length
                    if (state1[y])  // i'm in this state
                        {
                        // advance edges
                        if (sample[x]) // reading a 1
                            for(int z=0;z<reading1_l[y];z++)
                                state2[reading1[y][z]] = true;
                        else  // reading a 0
                            for(int z=0;z<reading0_l[y];z++)
                                state2[reading0[y][z]] = true;
                        }

                
                // advance along epsilon boundary
                boolean moreEpsilons = true;
                while(moreEpsilons)
                    {
                    moreEpsilons = false;
                    for(int y=0;y<numNodes;y++)
                        if (state2[y])
                            for(int z=0;z<epsilon_l[y];z++)
                                {
                                if (!state2[epsilon[y][z]]) moreEpsilons = true;
                                state2[epsilon[y][z]] = true;
                                }
                    }
                }


            else //if (st==STATE_2)
                {
                for(int y=0;y<numNodes;y++)
                    state1[y]=false;
                for(int y=0;y<numNodes;y++)  // yes, *start*.length
                    if (state2[y])  // i'm in this state
                        {
                        // advance edges
                        if (sample[x]) // reading a 1
                            for(int z=0;z<reading1_l[y];z++)
                                state1[reading1[y][z]] = true;
                        else  // reading a 0
                            for(int z=0;z<reading0_l[y];z++)
                                state1[reading0[y][z]] = true;
                        }

                // advance along epsilon boundary
                boolean moreEpsilons = true;
                while(moreEpsilons)
                    {
                    moreEpsilons = false;
                    for(int y=0;y<numNodes;y++)
                        if (state1[y])
                            for(int z=0;z<epsilon_l[y];z++)
                                {
                                if (!state1[epsilon[y][z]]) moreEpsilons = true;
                                state1[epsilon[y][z]] = true;
                                }
                    }
                }

            st = !st;
            }

        // am I in an accepting state?
        if (st==STATE_1)  // just loaded the result into state 1 from state 2
            {
            for(int x=0;x<numNodes;x++)
                if (accept[x] && state1[x]) return true;
            }
        else // (st==STATE_2)
            {
            for(int x=0;x<numNodes;x++)
                if (accept[x] && state2[x]) return true;
            }
        return false;
        }

    




    int totpos;
    int totneg;
    
    /** Tests an individual, returning its successful positives
        in totpos and its successful negatives in totneg. */
    public void fullTest(final EvolutionState state, 
                         final Individual ind, 
                         final int threadnum,
                         boolean[][] pos,
                         boolean[][] neg)
        {
        // reset the graph
        numNodes = 2;
        numEdges = 1; from[0]=0; to[0]=1;
        start[0]=start[1]=accept[0]=accept[1]=false; 
        ((EdgeData)input).edge = 0;
        
        // generate the graph
        ((GPIndividual)ind).trees[0].child.eval(
            state,threadnum,input,stack,((GPIndividual)ind),this);
        
        // produce the adjacency matrix
        if (reading1.length < numNodes ||
            reading1[0].length < numEdges)
            { 
            reading1 = new int[numNodes*2][numEdges*2];
            reading0 = new int[numNodes*2][numEdges*2];
            epsilon = new int[numNodes*2][numEdges*2];
            reading1_l = new int[numNodes*2];
            reading0_l = new int[numNodes*2];
            epsilon_l = new int[numNodes*2];
            }
        
        for(int y=0;y<numNodes;y++)
            {
            reading1_l[y]=0;
            reading0_l[y]=0;
            epsilon_l[y]=0;
            }
        
        for(int y=0;y<numEdges;y++)
            switch(reading[y])
                {
                case READING0:
                    reading0[from[y]][reading0_l[from[y]]++]=to[y];
                    break;
                case READING1:
                    reading1[from[y]][reading1_l[from[y]]++]=to[y];
                    break;
                case EPSILON:
                    epsilon[from[y]][epsilon_l[from[y]]++]=to[y];
                    break;
                }
        
        // create the states
        if (state1.length < numNodes)
            { 
            state1 = new boolean[numNodes*2];
            state2 = new boolean[numNodes*2];
            }
        
        // test the graph on our data
        
        totpos=0;
        totneg=0;
        for(int y=0;y<pos.length;y++)
            if (test(pos[y])) totpos++;
        for(int y=0;y<neg.length;y++)
            if (!test(neg[y])) totneg++;
        }




    public void evaluate(final EvolutionState state, 
                         final Individual ind, 
                         final int threadnum)
        {
        if (start==null)
            {
            start = new boolean[MIN_ARRAY_SIZE];
            accept = new boolean[MIN_ARRAY_SIZE];
            reading = new int[MIN_ARRAY_SIZE];
            from = new int[MIN_ARRAY_SIZE];
            to = new int[MIN_ARRAY_SIZE];
            state1 = new boolean[MIN_ARRAY_SIZE];
            state2 = new boolean[MIN_ARRAY_SIZE];
            reading1 = new int[MIN_ARRAY_SIZE][MIN_ARRAY_SIZE];
            reading0 = new int[MIN_ARRAY_SIZE][MIN_ARRAY_SIZE];
            epsilon = new int[MIN_ARRAY_SIZE][MIN_ARRAY_SIZE];
            reading1_l = new int[MIN_ARRAY_SIZE];
            reading0_l = new int[MIN_ARRAY_SIZE];
            epsilon_l = new int[MIN_ARRAY_SIZE];
            }

        if (!ind.evaluated)  // don't bother reevaluating
            {
            fullTest(state,ind,threadnum,posT,negT);
            // the fitness better be KozaFitness!
            KozaFitness f = ((KozaFitness)ind.fitness);

            // this is an awful fitness metric, but it's the standard
            // one used for these problems.  :-(
                
            f.setStandardizedFitness(state,(float)
                                     (1.0 - ((double)(totpos + totneg)) / 
                                      (posT.length + negT.length)));

            // here are two other more reasonable fitness metrics
            /*
              f.setStandardizedFitness(state,(float)
              (1.0 - Math.min(((double)totpos)/posT.length,
              ((double)totneg)/negT.length)));

              f.setStandardizedFitness(state,(float)
              (1.0 - (((double)totpos)/posT.length +
              ((double)totneg)/negT.length)/2.0));
            */

            f.hits = totpos + totneg;
            ind.evaluated = true;
            }
        }

    public void describe(final Individual ind, 
                         final EvolutionState state, 
                         final int threadnum, final int log,
                         final int verbosity)
        {
        if (start==null)
            {
            start = new boolean[MIN_ARRAY_SIZE];
            accept = new boolean[MIN_ARRAY_SIZE];
            reading = new int[MIN_ARRAY_SIZE];
            from = new int[MIN_ARRAY_SIZE];
            to = new int[MIN_ARRAY_SIZE];
            state1 = new boolean[MIN_ARRAY_SIZE];
            state2 = new boolean[MIN_ARRAY_SIZE];
            reading1 = new int[MIN_ARRAY_SIZE][MIN_ARRAY_SIZE];
            reading0 = new int[MIN_ARRAY_SIZE][MIN_ARRAY_SIZE];
            epsilon = new int[MIN_ARRAY_SIZE][MIN_ARRAY_SIZE];
            reading1_l = new int[MIN_ARRAY_SIZE];
            reading0_l = new int[MIN_ARRAY_SIZE];
            epsilon_l = new int[MIN_ARRAY_SIZE];
            }

        if (generalize)
            fullTest(state,ind,threadnum,posA,negA);
        else
            fullTest(state,ind,threadnum,posT,negT);
        
        if (generalize)
            state.output.println("\n\nBest Individual's Generalization Score...\n" +
                                 "Pos: " + totpos + "/" + posA.length + 
                                 " Neg: " + totneg + "/" + negA.length + 
                                 "\n(pos+neg)/(allpos+allneg):     " + 
                                 (float)
                                 (((double)(totpos+totneg))/(posA.length+negA.length)) +
                                 "\n((pos/allpos)+(neg/allneg))/2: " + 
                                 (float)
                                 (((((double)totpos)/posA.length)+(((double)totneg)/negA.length))/2) +
                                 "\nMin(pos/allpos,neg/allneg):    " +
                                 (float)Math.min((((double)totpos)/posA.length),(((double)totneg)/negA.length)),
                                 verbosity,log);
                
        state.output.println("\nBest Individual's NFA\n=====================\n",
                             verbosity,log);
        
        state.output.println(printCurrentNFA(),verbosity,log);
        }

    public String describeShortGeneralized(final Individual ind, 
                                           final EvolutionState state, 
                                           final int threadnum)
        {
        if (start==null)
            {
            start = new boolean[MIN_ARRAY_SIZE];
            accept = new boolean[MIN_ARRAY_SIZE];
            reading = new int[MIN_ARRAY_SIZE];
            from = new int[MIN_ARRAY_SIZE];
            to = new int[MIN_ARRAY_SIZE];
            state1 = new boolean[MIN_ARRAY_SIZE];
            state2 = new boolean[MIN_ARRAY_SIZE];
            reading1 = new int[MIN_ARRAY_SIZE][MIN_ARRAY_SIZE];
            reading0 = new int[MIN_ARRAY_SIZE][MIN_ARRAY_SIZE];
            epsilon = new int[MIN_ARRAY_SIZE][MIN_ARRAY_SIZE];
            reading1_l = new int[MIN_ARRAY_SIZE];
            reading0_l = new int[MIN_ARRAY_SIZE];
            epsilon_l = new int[MIN_ARRAY_SIZE];
            }

        fullTest(state,ind,threadnum,posA,negA);

        return ": " + 
            ((double)totpos)/posA.length + " " + 
            ((double)totneg)/negA.length + " " +
            (((double)(totpos+totneg))/(posA.length+negA.length)) + " " +
            (((((double)totpos)/posA.length)+(((double)totneg)/negA.length))/2) + " " +
            Math.min((((double)totpos)/posA.length),(((double)totneg)/negA.length)) + " : " ;
        }           

    }
