/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.multiobjective;
import java.io.*;
import ec.util.DecodeReturn;
import ec.util.Parameter;
import ec.util.Code;
import ec.Fitness;
import ec.EvolutionState;

/* 
 * MultiObjectiveFitness.java
 * 
 * Created: Tue Aug 10 20:27:38 1999
 * By: Sean Luke
 */

/**
 * MultiObjectiveFitness is a subclass of Fitness which implements basic
 * multi-objective mechanisms suitable for being used with a variety of
 * multi-objective selection mechanisms, including ones using pareto-optimality.
 *
 * <p>The object contains two items: an array of floating point values
 * representing the various multiple fitnesses (ranging from 0.0 (worst)
 * to 1.0 inclusive), and a flag (maximize) indicating whether higher is
 * considered better.  By default, isIdealFitness() always returns false;
 * you'll probably want to override that [if appropriate to your problem].

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>num-objectives</tt><br>
 (else)<tt>multi.num-objectives</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(the number of fitnesses in the multifitness array)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>maximize</tt><br>
 <font size=-1> bool = <tt>true</tt> (default) or <tt>false</tt></font></td>
 <td valign=top>(are higher values considered "better"?)</table>

 <p><b>Default Base</b><br>
 multi.fitness

 * @author Sean Luke
 * @version 1.0 
 */

public class MultiObjectiveFitness extends Fitness
    {
    public static final String MULTI_FITNESS_POSTAMBLE = "[";
    public static final String FITNESS_POSTAMBLE = "]";

    /** parameter for size of multifitness */
    public static final String P_NUMFITNESSES = "num-objectives"; 

    /** Is higher better? */
    public static final String P_MAXIMIZE = "maximize";

    /** The various fitnesses. */
    public float[] multifitness; // values range from 0 (worst) to 1 INCLUSIVE 
    public boolean maximize = true;

    public Parameter defaultBase()
        {
        return MultiObjectiveDefaults.base().push(P_FITNESS);
        }

    public Object clone()
        {
        MultiObjectiveFitness f = (MultiObjectiveFitness)(super.clone());
        f.multifitness = (float[])(multifitness.clone());  // cloning an array
        return f;
        }

    /** Returns the Max() of multifitnesses, which adheres to Fitness.java's
        protocol for this method. Though you should not rely on a selection
        or statistics method which requires this.  */
    public float fitness()
        {
        float fit = multifitness[0];
        for(int x=1;x<multifitness.length;x++)
            if (fit < multifitness[x]) fit = multifitness[x];
        return fit;
        }

    /** Sets up.  This must be called at least once in the prototype before instantiating any
        fitnesses that will actually be used in evolution. */

    public void setup(EvolutionState state, Parameter base) 
        {
        super.setup(state,base);  // unnecessary really
        
        Parameter def = defaultBase();
        int numFitnesses;

        numFitnesses = state.parameters.getInt(base.push(P_NUMFITNESSES),def.push(P_NUMFITNESSES),0);
        if (numFitnesses<=0)
            state.output.fatal("The number of objectives must be an integer >0.",
                base.push(P_NUMFITNESSES),def.push(P_NUMFITNESSES));

        maximize = state.parameters.getBoolean(
            base.push(P_MAXIMIZE), def.push(P_MAXIMIZE), true);

        multifitness = new float[numFitnesses];         
        }  

    /** Returns true if this fitness is the "ideal" fitness. Default always returns false.  You may want to override this. */
    public boolean isIdealFitness()
        {
        return false;
        }

    /** Returns true if I'm equivalent in fitness (neither better nor worse) 
        to _fitness. The rule I'm using is this:
        If one of us is better in one or more criteria, and we are equal in
        the others, then equivalentTo is false.  If each of us is better in
        one or more criteria each, or we are equal in all criteria, then 
        equivalentTo is true.
    */

    public boolean equivalentTo(Fitness _fitness)
        {
        MultiObjectiveFitness other = (MultiObjectiveFitness)_fitness;
        boolean abeatsb = false;
        boolean bbeatsa = false;

        if (maximize != other.maximize) throw new RuntimeException("Attempt made to compare two multiobjective fitnesses; but one expects higher values to be better and the other expectes lower values to be better.");
        if (multifitness.length != other.multifitness.length) throw new RuntimeException("Attempt made to compare two multiobjective fitnesses; but they have different numbers of objectives.");
        if (maximize)
            {
            for (int x=0;x<multifitness.length; x++)
                {
                if (multifitness[x] > other.multifitness[x]) abeatsb = true;
                if (multifitness[x] < other.multifitness[x]) bbeatsa = true;
                if (abeatsb && bbeatsa) return true;
                }
            }
        else  // lower is better
            {
            for (int x=0;x<multifitness.length; x++)
                {
                if (multifitness[x] < other.multifitness[x]) abeatsb = true;
                if (multifitness[x] > other.multifitness[x]) bbeatsa = true;
                if (abeatsb && bbeatsa) return true;
                }
            }
        if (abeatsb || bbeatsa) return false;
        return true;
        }

    /** Returns true if I'm better than _fitness. The rule I'm using is this:
        if I am better in one or more criteria, and we are equal in the others,
        then betterThan is true, else it is false. */
    
    public boolean betterThan(Fitness _fitness)
        {
        MultiObjectiveFitness other = (MultiObjectiveFitness)_fitness;
        boolean abeatsb = false;
        if (maximize != other.maximize) throw new RuntimeException("Attempt made to compare two multiobjective fitnesses; but one expects higher values to be better and the other expectes lower values to be better.");
        if (multifitness.length != other.multifitness.length) throw new RuntimeException("Attempt made to compare two multiobjective fitnesses; but they have different numbers of objectives.");
        if (maximize)
            {
            for (int x=0;x<multifitness.length;x++)
                {
                if (multifitness[x] > other.multifitness[x]) abeatsb = true;
                if (multifitness[x] < other.multifitness[x]) return false;
                }
            }
        else
            {
            for (int x=0;x<multifitness.length;x++)
                {
                if (multifitness[x] < other.multifitness[x]) abeatsb = true;
                if (multifitness[x] > other.multifitness[x]) return false;
                }
            }
        return abeatsb;
        }

    
    // <p><tt> Fitness: [</tt><i>fitness values encoded with ec.util.Code, separated by spaces</i><tt>]</tt>
    public String fitnessToString()
        {
        String s = FITNESS_PREAMBLE + MULTI_FITNESS_POSTAMBLE;
        for (int x=0;x<multifitness.length;x++)
            {
            if (x>0) s = s + " ";
            s = s + Code.encode(multifitness[x]);
            }
        s = s + " ";
        s = s + Code.encode(maximize);
        return s + FITNESS_POSTAMBLE;
        }

    // <p><tt> Fitness: [</tt><i>fitness values encoded with ec.util.Code, separated by spaces</i><tt>]</tt>
    public String fitnessToStringForHumans()
        {
        String s = FITNESS_PREAMBLE + MULTI_FITNESS_POSTAMBLE;
        for (int x=0;x<multifitness.length;x++)
            {
            if (x>0) s = s + " ";
            s = s + multifitness[x];
            }
        s = s + " ";
        s = s + (maximize ? "max" : "min");
        return s + FITNESS_POSTAMBLE;
        }

    public void readFitness(final EvolutionState state, 
        final LineNumberReader reader)
        throws IOException
        {
        DecodeReturn d = Code.checkPreamble(FITNESS_PREAMBLE + MULTI_FITNESS_POSTAMBLE, state, reader);
        for(int x=0;x<multifitness.length;x++)
            {
            Code.decode(d);
            if (d.type!=DecodeReturn.T_FLOAT)
                state.output.fatal("Reading Line " + d.lineNumber + ": " +
                    "Bad Fitness (multifitness value #" + x + ").");
            multifitness[x] = (float)d.d;
            }
        Code.decode(d);
        if (d.type!=DecodeReturn.T_BOOLEAN)
            state.output.fatal("Reading Line " + d.lineNumber + ": " +
                "Information missing about whether higher is better");
        maximize = (boolean)(d.l != 0);
        }
    
    public void writeFitness(final EvolutionState state,
        final DataOutput dataOutput) throws IOException
        {
        dataOutput.writeInt(multifitness.length);
        for(int x=0;x<multifitness.length;x++)
            dataOutput.writeFloat(multifitness[x]);
        dataOutput.writeBoolean(maximize);
        }

    public void readFitness(final EvolutionState state,
        final DataInput dataInput) throws IOException
        {
        int len = dataInput.readInt();
        if (multifitness==null || multifitness.length != len)
            multifitness = new float[len];
        for(int x=0;x<multifitness.length;x++)
            multifitness[x] = dataInput.readFloat();
        maximize = dataInput.readBoolean();
        }
    }
