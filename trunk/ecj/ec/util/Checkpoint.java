/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.util;
import java.util.zip.*;
import ec.*;
import java.io.*;

/* 
 * Checkpoint.java
 * 
 * Created: Tue Aug 10 22:39:19 1999
 * By: Sean Luke
 */

/**
 * Checkpoints ec.EvolutionState objects out to checkpoint files, or 
 * restores the same from checkpoint files.  Checkpoint take the following
 * form:
 *
 * <p><i>checkpointPrefix</i><tt>.</tt><i>generation</i><tt>.gz</tt>
 *
 * <p>...where <i>checkpointPrefix</i> is the checkpoing prefix given
 * in ec.EvolutionState, and <i>generation</i> is the current generation number
 * also given in ec.EvolutionState.
 * The ".gz" is added because the file is GZIPped to save space.
 *
 * <p>When writing a checkpoint file, if you have specified a checkpoint directory
 * in ec.EvolutionState.checkpointDirectory, then this directory will be used to
 * write the checkpoint files.  Otherwise they will be written in your working
 * directory (where you ran the Java process).
 *
 * @author Sean Luke
 * @version 1.1
 */

public class Checkpoint
    {

    /** Writes the evolution state out to a file. */

    public static void setCheckpoint(EvolutionState state)
        {
        try
            {
            File file = new File("" + state.checkpointPrefix + "." + state.generation + ".gz");
            
            if (state.checkpointDirectory != null)
                {
                file = new File(state.checkpointDirectory, 
                    "" + state.checkpointPrefix + "." + state.generation + ".gz");
                }
            ObjectOutputStream s = 
                new ObjectOutputStream(
                    new GZIPOutputStream (
                        new BufferedOutputStream(
                            new FileOutputStream(file))));
                
            s.writeObject(state);
            s.close();
            state.output.message("Wrote out checkpoint file " + 
                state.checkpointPrefix + "." + 
                state.generation + ".gz");
            }
        catch (IOException e)
            {
            state.output.warning("Unable to create the checkpoint file " + 
                state.checkpointPrefix + "." +
                state.generation + ".gz" + 
                "because of an IOException:\n--EXCEPTION--\n" +
                e + 
                "\n--EXCEPTION-END--\n");
            }
        }


    /** Returns an EvolutionState object read from a checkpoint file
        whose filename is <i>checkpoint</i> 
        *
        @exception java.lang.ClassNotFoundException thrown when the checkpoint file contains a class reference which doesn't exist in your class hierarchy.
    **/
    public static EvolutionState restoreFromCheckpoint(String checkpoint)
        throws IOException, ClassNotFoundException, OptionalDataException
    /* must throw something if error -- NEVER return null */
        { 
        // load from the file
        ObjectInputStream s = 
            new ObjectInputStream(
                new GZIPInputStream (
                    new BufferedInputStream (
                        new FileInputStream (checkpoint))));

        EvolutionState e = (EvolutionState) s.readObject();
        s.close();

        // restart from the checkpoint
    
        e.resetFromCheckpoint();
        return e; 
        }
    }

