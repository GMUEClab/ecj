/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.ant;
import ec.app.ant.func.*;
import ec.util.*;
import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import java.io.*;
import java.util.*;
import ec.simple.*;

/* 
 * Ant.java
 * 
 * Created: Mon Nov  1 15:46:19 1999
 * By: Sean Luke
 */

/**
 * Ant implements the Artificial Ant problem.
 *
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>data</tt><br>
 <font size=-1>classname, inherits or == ec.app.ant.AntData</font></td>
 <td valign=top>(the class for the prototypical GPData object for the Ant problem)</td></tr>
 <tr><td valign=top><i>base</i>.<tt>file</tt><br>
 <font size=-1>String</font></td>
 <td valign=top>(filename of the .trl file for the Ant problem)</td></tr>
 <tr><td valign=top><i>base</i>.<tt>turns</tt><br>
 <font size=-1>int &gt;= 1</td>
 <td valign=top>(maximal number of moves the ant may make)</td></tr>
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

public class Ant extends GPProblem implements SimpleProblemForm
    {
    public static final String P_FILE = "file";
    public static final String P_MOVES = "moves";

    // map point descriptions
    public static final int ERROR = 0;
    public static final int FOOD = -1;
    public static final int EMPTY = 1;
    public static final int TRAIL = 2;
    public static final int ATE = 3;

    // orientations
    public static final int O_UP = 0;
    public static final int O_LEFT = 1;
    public static final int O_DOWN = 2;
    public static final int O_RIGHT = 3;

    // We'll deep clone this anyway, even though we don't
    // need it by default!
    public AntData input;

    // maximum number of moves
    public int maxMoves;

    // how much food we have
    public int food;

    // our map
    public int map[][];
    
    // store the positions of food so we can reset our map
    // don't need to be deep-cloned, they're read-only
    public int foodx[];
    public int foody[];

    // map[][]'s bounds
    public int maxx;
    public int maxy;

    // our position
    public int posx;
    public int posy;

    // how many points we've gotten
    public int sum;
    
    // our orientation
    public int orientation;

    // how many moves we've made
    public int moves;

    // print modulo for doing the abcdefg.... thing at print-time
    public int pmod;

    public Object clone()
        {
        Ant myobj = (Ant) (super.clone());
        myobj.input = (AntData)(input.clone());
        myobj.map = new int[map.length][];
        for(int x=0;x<map.length;x++)
            myobj.map[x] = (int[])(map[x].clone());
        return myobj;
        }

    public void setup(final EvolutionState state,
                      final Parameter base)
        {
        // very important, remember this
        super.setup(state,base);

        // not using any default base -- it's not safe

        // set up our input
        input = (AntData) state.parameters.getInstanceForParameterEq(
            base.push(P_DATA),null, AntData.class);
        input.setup(state,base.push(P_DATA));

        // how many maxMoves?
        maxMoves = state.parameters.getInt(base.push(P_MOVES),null,1);
        if (maxMoves==0)
            state.output.error("The number of moves an ant has to make must be >0");
        
        // load our file
        File filename = state.parameters.getFile(base.push(P_FILE),null);
        if (filename==null)
            state.output.fatal("Ant trail file name not provided.");

        food = 0;
        try
            {
            LineNumberReader lnr = 
                new LineNumberReader(new FileReader(filename));
            
            StringTokenizer st = new StringTokenizer(lnr.readLine()); // ugh
            maxx = Integer.parseInt(st.nextToken());
            maxy = Integer.parseInt(st.nextToken());
            map = new int[maxx][maxy];
            int y;
            for(y=0;y<maxy;y++)
                {
                String s = lnr.readLine();
                if (s==null)
                    {
                    state.output.warning("Ant trail file ended prematurely");
                    break;
                    }
                int x;
                for(x=0;x<s.length();x++)
                    {
                    if (s.charAt(x)==' ')
                        map[x][y]=EMPTY;
                    else if (s.charAt(x)=='#')
                        { map[x][y]=FOOD; food++; }
                    else if (s.charAt(x)=='.')
                        map[x][y]=TRAIL;
                    else state.output.error("Bad character '" + s.charAt(x) + "' on line number " + lnr.getLineNumber() + " of the Ant trail file.");
                    }
                // fill out rest of X's
                for(int z=x;z<maxx;z++)
                    map[z][y]=EMPTY;
                }
            // fill out rest of Y's
            for (int z=y;z<maxy;z++)
                for(int x=0;x<maxx;x++)
                    map[x][z]=EMPTY;
            }
        catch (NumberFormatException e)
            {
            state.output.fatal("The Ant trail file does not begin with x and y integer values.");
            }
        catch (IOException e)
            {
            state.output.fatal("The Ant trail file could not be read due to an IOException:\n" + e);
            }
        state.output.exitIfErrors();

        // load foodx and foody reset arrays
        foodx = new int[food];
        foody = new int[food];
        int tmpf = 0;
        for(int x=0;x<map.length;x++)
            for(int y=0;y<map[0].length;y++)
                if (map[x][y]==FOOD) 
                    { foodx[tmpf] = x; foody[tmpf] = y; tmpf++; }
        }

    public void evaluate(final EvolutionState state, 
                         final Individual ind, 
                         final int threadnum)
        {
        if (!ind.evaluated)  // don't bother reevaluating
            {
            sum = 0;            
            posx = 0;
            posy = 0;
            orientation = O_RIGHT;

            for(moves=0;moves<maxMoves && sum<food; )
                ((GPIndividual)ind).trees[0].child.eval(
                    state,threadnum,input,stack,((GPIndividual)ind),this);
                
            // the fitness better be KozaFitness!
            KozaFitness f = ((KozaFitness)ind.fitness);
            f.setStandardizedFitness(state,(float)(food - sum));
            f.hits = sum;
            ind.evaluated = true;

            // clean up array
            for(int y=0;y<food;y++)
                map[foodx[y]][foody[y]] = FOOD;
            }
        }

    public void describe(final Individual ind, 
                         final EvolutionState state, 
                         final int threadnum, final int log,
                         final int verbosity)

        {
        state.output.println("\n\nBest Individual's Map\n=====================",
                             verbosity,log);

        sum = 0;
        pmod = 97; /** ascii a */
        posx = 0;
        posy = 0;
        orientation = O_RIGHT;

        int[][] map2 = new int[map.length][];
        for(int x=0;x<map.length;x++)
            map2[x] = (int[])(map[x].clone());

        map2[posx][posy] = pmod; pmod++;
        for(moves=0; moves<maxMoves && sum<food; )
            ((EvalPrint)(((GPIndividual)ind).trees[0].child)).evalPrint(
                state,threadnum,input,stack,((GPIndividual)ind),this,
                map2);
        // print out the map
        for(int y=0;y<map2.length;y++)
            {
            for(int x=0;x<map2.length;x++)
                {
                switch(map2[x][y])
                    {
                    case FOOD: 
                        state.output.print("#",verbosity,log);
                        break;
                    case EMPTY: 
                        state.output.print(".",verbosity,log);
                        break;
                    case TRAIL: 
                        state.output.print("+",verbosity,log);
                        break;
                    case ATE:
                        state.output.print("?",verbosity,log);
                        break;
                    default:
                        state.output.print(""+((char)map2[x][y]),verbosity,log);
                        break;
                    }
                }
            state.output.println("",verbosity,log);
            }

        }
    }
