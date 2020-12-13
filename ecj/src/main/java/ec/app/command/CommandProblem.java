package ec.app.command;

import java.lang.ProcessBuilder;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.Problem;
import ec.coevolve.GroupedProblemForm;
import ec.simple.SimpleFitness;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;
import ec.vector.DoubleVectorIndividual;

/**
 * A problem that ferries real-valued genomes out to an external shell command to have their 
 * fitnesses evaluated.
 */
public class CommandProblem extends Problem implements SimpleProblemForm, GroupedProblemForm
    {
    private static final long serialVersionUID = 1;

    public final static String P_COMMAND = "command";
    public final static String DELIMITER = ",";

    private ProcessBuilder processBuilder;

    public void setup(final EvolutionState state, final Parameter base)
        {
        final String command = state.parameters.getString(base.push(P_COMMAND), null);
        if (command == null)
            state.output.fatal(String.format("%s: no value given for parameter '%s', but we need a command to run.", this.getClass().getSimpleName(), base.push(P_COMMAND)));
        this.processBuilder = new ProcessBuilder(command);
        }

	@Override
	public void preprocessPopulation(EvolutionState state, Population pop, boolean[] prepareForFitnessAssessment,
            boolean countVictoriesOnly)
        {
		// Do nothing
	    }

	@Override
	public int postprocessPopulation(EvolutionState state, Population pop, boolean[] assessFitness,
            boolean countVictoriesOnly)
        {
		// Do nothing
		return 0;
	    }

    /**
     * Evaluate a chunk of individuals by sending them all at once to an external command.
     * 
     * @param state The algorithm's state
     * @param individuals Array of DoubleVectorIndividuals to be evaluated
     * @param updateFitness Ignored
     * @param countVictoriesOnly Ignored
     * @param subpops Ignored
     * @param threadnum The ID of the thread this job is run on
     */
	@Override
	public void evaluate(EvolutionState state, Individual[] individuals, boolean[] updateFitness, boolean countVictoriesOnly,
            int[] subpops, int threadnum)
        {
        assert(state != null);
        assert(individuals != null);
        assert(individuals.length > 0);
        
        try
            {
            final String simulationResult = runCommand(individuals);
            final List<Double> fitnesses = parseFitnesses(simulationResult);

            if (fitnesses.size() != individuals.length)
                    throw new IllegalStateException(String.format("Sent %d individuals to external command, but the returned simulation results had %d lines.", CommandProblem.class.getSimpleName(), individuals.length, fitnesses.size()));
                
            for (int i = 0; i < individuals.length; i++)
                {
                final Individual ind = individuals[i];
                ind.fitness = new SimpleFitness();
                ((SimpleFitness)ind.fitness).setFitness(state, fitnesses.get(i), false);
                ind.evaluated = true;
                }
            }
        catch (final Exception e)
            {
            state.output.fatal(String.format("%s: %s", this.getClass().getSimpleName(), e));
            }
	    }

    /** Evaluate a single individual by sending its genome to an external command. */
	@Override
    public void evaluate(EvolutionState state, Individual ind, int subpopulation, int threadnum)
        {
		evaluate(state, new Individual[] { ind }, null, false, null, threadnum);
        }

    /**
     * Run an external program, writing genomes to its stdin and receiving a response String back
     * on its stdout.
     * 
     * We assume that the external program will return exactly the same number of lines of output as
     * it is given.
     * 
     * @param individuals Array of DoubleVectorIndividuals to be sent to the command.
     * @return The results the command writes back.
     * @throws IOException
     * @throws InterruptedException
     */
    private String runCommand(final Individual[] individuals) throws IOException, InterruptedException
        {
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        final Process p = processBuilder.start();

        // Write genomes to the command's stdin
        final Writer commandInput = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
        writeIndividuals(individuals, commandInput);
        commandInput.close(); // Sends EOF
        final int exitCode = p.waitFor();

        if (exitCode != 0)
            throw new IllegalStateException(String.format("External command terminated with exit code %d.", exitCode));

        // Read the output from the command's stdout
        final StringBuilder sb = new StringBuilder();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = "";			
        while ((line = reader.readLine())!= null) {
            sb.append(line).append(System.getProperty("line.separator"));
        }
        return sb.toString();
        }
    
    /** Take a list of DoubleVectorIndividuals and output them to a tab-delimited file.
     * 
     * @param individuals A non-empty population of Individuals.  If any element is null an IAE is thrown.
     * @param outWriter A non-null Writer to output the CSV to.  When this method returns it does *not* close the outWriter.
     * @return Nothing.  Side effects: Writes a tab-delimited CSV to outWriter, one row per individual, one column per gene.
     * @throws IOException 
     */
    public static void writeIndividuals(final Individual[] individuals, final Writer outWriter) throws IOException
        {
        assert(outWriter != null);
        assert(individuals != null);
        assert(individuals.length > 0);
        
        for (final Individual ind : individuals)
            {
            final double[] genome = ((DoubleVectorIndividual) ind).genome;
            assert(genome.length > 0);
            outWriter.write(String.valueOf(genome[0]));
            for (int i = 1; i < genome.length; i++)
                outWriter.write(String.format("%s%f", DELIMITER, genome[i]));
            outWriter.write(String.format("%n"));
            }
        }

    /**
     * Parse the String of results that we get back from the external command by
     * interpreting it as a list of fitness values, one per line.
     */
    public static List<Double> parseFitnesses(final String simResult)
        {
            if (simResult.isEmpty())
                throw new IllegalArgumentException(String.format("%s: response from external fitness command was empty.", CommandProblem.class.getSimpleName()));

            try {
                final String[] lines = simResult.split("\\r?\\n");  // Split on either Windows or UNIX line endings
                final List<Double> fitnesses = new ArrayList<>();
                for (final String f : lines)
                    {
                    final double realFitness = Double.valueOf(f);
                    fitnesses.add(realFitness);
                    }
                return fitnesses;
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format("%s: error (%s) while parsing fitness response \"%s\"", CommandProblem.class.getSimpleName(), e, simResult));
            }
        }
    }
