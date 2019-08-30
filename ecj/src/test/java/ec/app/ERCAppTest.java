package ec.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import ec.EvolutionState;
import ec.Evolve;
import ec.test.SystemTest;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

@Category(SystemTest.class)
public class ERCAppTest {
	private final static Parameter BASE = new Parameter("base");
	private ParameterDatabase params;
	private EvolutionState state;

	public String examplePath = "src/main/resources/ec/app/regression/erc.params";

	@Before
	public void setUp() {
		try {
			params = new ParameterDatabase(new File(examplePath));
			params.set(new Parameter(Evolve.P_SILENT), "true");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		state = new EvolutionState();
	}

	@Test
	public void testERCWithMutateAllNodesPipeline() {
		try {
			Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Testing " + examplePath + " with MutateAllNodesPipeline.");
			params.set(new Parameter("pop.subpop.0.species.pipe.source.0"), "ec.gp.breed.MutateAllNodesPipeline");
			params.set(new Parameter("gp.breed.mutate-all-nodes.source.0"), "ec.select.TournamentSelection");
			params.set(new Parameter("gp.breed.mutate-all-nodes.source.1"), "same");
			params.set(new Parameter("gp.breed.mutate-all-nodes.ns.0"), "ec.gp.koza.KozaNodeSelector");
			params.set(new Parameter("gp.breed.mutate-all-nodes.tree.0"), "0");
			state = Evolve.initialize(params, 0);
			state.output.setThrowsErrors(true);
			state.run(EvolutionState.C_STARTED_FRESH);
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	//@Test
	public void testWithSizefairCrossoverPipeline(){
		try{
			Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Testing " + examplePath + " with SizeFairCrossoverPipeline.");
			params.set(new Parameter("pop.subpop.0.species.pipe.source.0"), "ec.gp.breed.SizeFairCrossoverPipeline");
			params.set(new Parameter("pop.subpop.0.species.pipe.source.0.prob"), "0.9");
			params.set(new Parameter("gp.breed.size-fair.source.0"), "ec.select.TournamentSelection");
			params.set(new Parameter("gp.breed.size-fair.source.1"), "same");
			params.set(new Parameter("gp.breed.size-fair.ns.0"), "ec.gp.koza.KozaNodeSelector");
			params.set(new Parameter("gp.breed.size-fair.ns.1"), "same");
			params.set(new Parameter("gp.breed.size-fair.tries"), "1");
			params.set(new Parameter("gp.breed.size-fair.maxdepth"), "17");
			params.set(new Parameter("gp.breed.size-fair.toss"), "false");
			params.set(new Parameter("gp.breed.size-fair.tree.0"), "0");
			params.set(new Parameter("gp.breed.size-fair.tree.1"), "0");
			state = Evolve.initialize(params, 0);
			state.output.setThrowsErrors(true);
			state.run(EvolutionState.C_STARTED_FRESH);
		}
		catch(Exception e){
			fail(e.toString());
		}
	}

	//@Test
	public void testWithInternalCrossoverPipeline(){
		try {
			Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Testing " + examplePath + " with InternalCrossoverPipeline.");
			params.set(new Parameter("pop.subpop.0.species.pipe.source.0"), "ec.gp.breed.InternalCrossoverPipeline");
			params.set(new Parameter("gp.breed.internal-xover.source.0"), "ec.select.TournamentSelection");
			params.set(new Parameter("gp.breed.internal-xover.source.1"), "same");
			params.set(new Parameter("gp.breed.internal-xover.tries"), "1");
			params.set(new Parameter("gp.breed.internal-xover.maxdepth"), "17");
			params.set(new Parameter("gp.breed.internal-xover.toss"), "false");
			params.set(new Parameter("gp.breed.internal-xover.tree.0"), "0");
			params.set(new Parameter("gp.breed.internal-xover.tree.1"), "0");
			params.set(new Parameter("gp.breed.internal-xover.ns.0"), "ec.gp.koza.KozaNodeSelector");
			params.set(new Parameter("gp.breed.internal-xover.ns.1"), "same");
			//params.set(new Parameter(""), "");
			state = Evolve.initialize(params, 0);
			state.output.setThrowsErrors(true);
			state.run(EvolutionState.C_STARTED_FRESH);
		}catch (Exception e) {
			fail(e.toString());
		}
	}

	@Test
	public void testWithMutateDemotePipeline(){
		try{
			Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Testing " + examplePath + " with MutateDemotePipeline.");
			params.set(new Parameter("pop.subpop.0.species.pipe.source.0"), "ec.gp.breed.MutateDemotePipeline");
			params.set(new Parameter("gp.breed.mutate-demote.source.0"), "ec.select.TournamentSelection");
			params.set(new Parameter("gp.breed.mutate-demote.source.1"), "same");
			params.set(new Parameter("gp.breed.mutate-demote.tries"), "1");
			params.set(new Parameter("gp.breed.mutate-demote.maxdepth"), "17");
			params.set(new Parameter("gp.breed.mutate-demote.tree.0"), "0");
			//params.set(new Parameter(""), "");
			state = Evolve.initialize(params, 0);
			state.output.setThrowsErrors(true);
			state.run(EvolutionState.C_STARTED_FRESH);
		}catch(Exception e){
			fail(e.toString());
		}
	}

	@Test
	public void testWithMutatePromotePipeline(){
		try{
			Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Testing " + examplePath + " with MutatePromotePipeline.");
			params.set(new Parameter("pop.subpop.0.species.pipe.source.0"), "ec.gp.breed.MutatePromotePipeline");
			params.set(new Parameter("gp.breed.mutate-promote.source.0"), "ec.select.TournamentSelection");
			params.set(new Parameter("gp.breed.mutate-promote.source.1"), "same");
			params.set(new Parameter("gp.breed.mutate-promote.tries"), "1");
			params.set(new Parameter("gp.breed.mutate-promote.maxdepth"), "17");
			params.set(new Parameter("gp.breed.mutate-promote.tree.0"), "0");
			//params.set(new Parameter(""), "");
			state = Evolve.initialize(params, 0);
			state.output.setThrowsErrors(true);
			state.run(EvolutionState.C_STARTED_FRESH);
		}catch(Exception e){
			fail(e.toString());
		}
	}

	@Test
	public void testWithMutateOneNodePipeline(){
		try{
			Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Testing " + examplePath + " with MutateOneNodePipeline.");
			params.set(new Parameter("pop.subpop.0.species.pipe.source.0"), "ec.gp.breed.MutateOneNodePipeline");
			params.set(new Parameter("gp.breed.mutate-one-node.source.0"), "ec.select.TournamentSelection");
			params.set(new Parameter("gp.breed.mutate-one-node.source.1"), "same");
			params.set(new Parameter("gp.breed.mutate-one-node.ns.0"), "ec.gp.koza.KozaNodeSelector");
			params.set(new Parameter("gp.breed.mutate-one-node.tree.0"), "0");
			//params.set(new Parameter(""), "");
			state = Evolve.initialize(params, 0);
			state.output.setThrowsErrors(true);
			state.run(EvolutionState.C_STARTED_FRESH);
		}catch(Exception e){
			fail(e.toString());
		}
	}

	@Test
	public void testWithMutateERCPipeline(){
		try{
			Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Testing " + examplePath + " with MutateERCPipeline.");
			params.set(new Parameter("pop.subpop.0.species.pipe.source.0"), "ec.gp.breed.MutateERCPipeline");
			params.set(new Parameter("gp.breed.mutate-erc.source.0"), "ec.select.TournamentSelection");
			params.set(new Parameter("gp.breed.mutate-erc.source.1"), "same");
			params.set(new Parameter("gp.breed.mutate-erc.ns.0"), "ec.gp.koza.KozaNodeSelector");
			params.set(new Parameter("gp.breed.mutate-erc.tree.0"), "0");
			//params.set(new Parameter(""), "");
			state = Evolve.initialize(params, 0);
			state.output.setThrowsErrors(true);
			state.run(EvolutionState.C_STARTED_FRESH);
		}catch(Exception e){
			fail(e.toString());
		}
	}

	@Test
	public void testWithRehangPipeline(){
		try{
			Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Testing " + examplePath + " with RehangPipeline.");
			params.set(new Parameter("pop.subpop.0.species.pipe.source.0"), "ec.gp.breed.RehangPipeline");
			params.set(new Parameter("gp.breed.rehang.source.0"), "ec.select.TournamentSelection");
			params.set(new Parameter("gp.breed.rehang.source.1"), "same");
			params.set(new Parameter("gp.breed.rehang.tries"), "1");
			params.set(new Parameter("gp.breed.rehang.tree.0"), "0");
			//params.set(new Parameter(""), "");
			state = Evolve.initialize(params, 0);
			state.output.setThrowsErrors(true);
			state.run(EvolutionState.C_STARTED_FRESH);
		}catch(Exception e){
			fail(e.toString());
		}
	}

	@Test
	public void testWithMutateSwapPipeline(){
		try{
			Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Testing " + examplePath + " with MutateSwapPipeline.");
			params.set(new Parameter("pop.subpop.0.species.pipe.source.0"), "ec.gp.breed.MutateSwapPipeline");
			params.set(new Parameter("gp.breed.mutate-swap.source.0"), "ec.select.TournamentSelection");
			params.set(new Parameter("gp.breed.mutate-swap.source.1"), "same");
			params.set(new Parameter("gp.breed.mutate-swap.tries"), "1");
			params.set(new Parameter("gp.breed.mutate-swap.tree.0"), "0");
			//params.set(new Parameter(""), "");
			state = Evolve.initialize(params, 0);
			state.output.setThrowsErrors(true);
			state.run(EvolutionState.C_STARTED_FRESH);
		}catch(Exception e){
			fail(e.toString());
		}
	}
}
