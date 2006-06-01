/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.lawnmower;
import ec.util.*;
import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;

/* 
 * Lawnmower.java
 * 
 * Created: Mon Nov  1 15:46:19 1999
 * By: Sean Luke
 */

/**
 * Lawnmower implements the Koza-II Lawnmower problem.
 *
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>data</tt><br>
 <font size=-1>classname, inherits or == ec.app.lawnmower.LawnmowerData</font></td>
 <td valign=top>(the class for the prototypical GPData object for the Lawnmower problem)</td></tr>
 <tr><td valign=top><i>base</i>.<tt>file</tt><br>
 <font size=-1>String</font></td>
 <td valign=top>(filename of the .trl file for the Lawnmower problem)</td></tr>
 <tr><td valign=top><i>base</i>.<tt>turns</tt><br>
 <font size=-1>int &gt;= 1</td>
 <td valign=top>(maximal number of moves the lawnmower may make)</td></tr>
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

public class Lawnmower extends GPProblem implements SimpleProblemForm
    {
    public static final String P_X = "x";
    public static final String P_Y = "y";

    // map point descriptions
    public static final int UNMOWED = 0;

    // orientations
    public static final int O_UP = 0;
    public static final int O_LEFT = 1;
    public static final int O_DOWN = 2;
    public static final int O_RIGHT = 3;

    // We'll deep clone this
    public LawnmowerData input;

    // our map
    public int map[][];
    
    // map[][]'s bounds
    public int maxx;
    public int maxy;

    // our current position
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
        Lawnmower myobj = (Lawnmower) (super.clone());
        myobj.input = (LawnmowerData)(input.clone());
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

        // I'm not using the default base for any of this stuff;
        // it's not safe I think.

        // set up our input
        input = (LawnmowerData) state.parameters.getInstanceForParameterEq(
            base.push(P_DATA),null, LawnmowerData.class);
        input.setup(state,base.push(P_DATA));

        // load our map coordinates
        maxx = state.parameters.getInt(base.push(P_X),null,1);
        if (maxx==0)
            state.output.error("The width (x dimension) of the lawn must be >0",
                               base.push(P_X));
        maxy = state.parameters.getInt(base.push(P_Y),null,1);
        if (maxy==0)
            state.output.error("The length (y dimension) of the lawn must be >0",
                               base.push(P_Y));
        state.output.exitIfErrors();
            
        // set up the map
        
        map = new int[maxx][maxy];
        for(int x=0;x<maxx;x++)
            for(int y=0;y<maxy;y++)
                map[x][y]=UNMOWED;
        }

    public void evaluate(final EvolutionState state, 
                         final Individual ind, 
                         final int threadnum)
        {               
        if (!ind.evaluated)  // don't bother reevaluating
            {
            sum = 0;
            moves = 0;
            posx = maxx/2+1;
            posy = maxy/2+1;
            orientation = O_UP;

            // evaluate the individual
            ((GPIndividual)ind).trees[0].child.eval(
                state,threadnum,input,stack,((GPIndividual)ind),this);
                
            // clean up the map
            for(int x=0;x<maxx;x++)
                for(int y=0;y<maxy;y++)
                    map[x][y]=UNMOWED;

            // the fitness better be KozaFitness!
            KozaFitness f = ((KozaFitness)ind.fitness);
            f.setStandardizedFitness(state,(float)(maxx*maxy - sum));
            f.hits = sum;
            ind.evaluated = true;
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
        moves = 0;
        posx = maxx/2+1;
        posy = maxy/2+1;
        orientation = O_UP;
            
        // evaluate the individual
        ((GPIndividual)ind).trees[0].child.eval(
            state,threadnum,input,stack,((GPIndividual)ind),this);
            
        // print out the map
        state.output.println(" Y ->",verbosity,log);
        for(int x=0;x<map.length;x++)
            {
            if (x==1) state.output.print("v",verbosity,log);
            else if (x==0) state.output.print("X",verbosity,log);
            else state.output.print(" ",verbosity,log);
            state.output.print("+",verbosity,log);
            for(int y=0;y<map[x].length;y++)
                state.output.print("----+",verbosity,log);
            state.output.println("",verbosity,log);
            if (x==0) state.output.print("|",verbosity,log);
            else state.output.print(" ",verbosity,log);
            state.output.print("|",verbosity,log);
                
            for(int y=0;y<map[x].length;y++)
                {
                if (map[x][y]==UNMOWED)
                    state.output.print("    ",verbosity,log);
                else 
                    {
                    String s = "" + (map[x][y]);
                    while (s.length()<4) s = " " + s;
                    state.output.print(s + "|",verbosity,log);
                    }
                }
            state.output.println("",verbosity,log);
            }
        if (map.length==1) state.output.print("v",verbosity,log);
        else state.output.print(" ",verbosity,log);
        state.output.print("+",verbosity,log);
        for(int y=0;y<map[map.length-1].length;y++)
            state.output.print("----+",verbosity,log);
        state.output.println("",verbosity,log);
            
            
        // clean up the map
        for(int x=0;x<maxx;x++)
            for(int y=0;y<maxy;y++)
                map[x][y]=UNMOWED;
        }
    }
