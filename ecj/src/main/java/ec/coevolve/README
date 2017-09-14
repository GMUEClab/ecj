This package contains classes for doing certain kinds of coevolution:

- 1-population competitive coevolution, where an individual in a population
  receive its fitness through some M tests against other individuals
  chosen from that population.  These tests are competitive: if an
  individual does well in a test, its competitor does poorly.

- 2-population competitive coevolution, where an individual in population A
  receives its fitness through some M tests against other individuals
  chosen from population B (and vice versa).  These tests are competitive:
  if an individual does well in a test, its competitor does poorly.

- N-population cooperative coevolution, where an individual in population A 
  receives its fitness through some M tests.  In each test, an individual
  is joined with N-1 other individuals, one from each of the other
  populations.  These tests are cooperative: if an individual does well in
  a test, its collaborators also do well.

Coevolution is largely defined by the form of evaluation, so this package
contains mostly Evaluators of different kinds.  The coevolution system places
each coevolved "population" in a separate ECJ subpopulation.  Fitness
assessment in coevolution typically consists of three parts:

	1. Preprocess the full population (all subpopulations) in some way
	2. Perform various tests on groups of individuals
	3. Postprocess the full population, which gathers all the test
	   results and assesses fitness on the individuals.

These three elements are embodied in a Problem form called GroupedProblemForm,
which you are required to use.  It shouldn't be surprising to you that
your Problem subclass will need to understand what kind of coevolution it's
being involved in and assess fitness appropriately.  The GroupedProblemForm
class is:
	
	ec.coevolve.GroupedProblemForm

The evaluate(...) method in GroupedProblemForm is a bit unusual.  You are
given an array of individuals to test together -- the particular subpopulations
from which the individuals are drawn depends on the coevolution method.  The
subpopulations in question are also provided to you as an array.  You are also
provided with an array of booleans indicating WHICH individuals are supposed to
have their fitnesses updated each time.  Last, you're given a boolean telling
you whether fitnesses should be (temporarily) updated to reflect victories
won rather than actual scores (as in the case of Single Elimination Tournament).




1 POPULATION COMPETITIVE COEVOLUTION

Here members of a single subpopulation are tested against one another in some
fashion, often multiple times with multiple "testing partners", before their
fitness is assessed.  ECJ's implementation can be found in

	ec.coevolve.CompetitiveEvaluator

This evaluator has a number of ways that individuals can be tested.  First,
there is the issue of competition style:

	- Single Elimination Tournament.  Individuals are put into a
	  single elimination tournament, and "winners" go on to compete
	  further in the bracket.  It's common for single elimination
	  tournament to be defined such that the degree to which an individual
	  rises in the tournament is defined as his fitness.  You'll need
	  to set a temporary fitness of individuals immediately during 
	  evaluate(...), rather than waiting for Step 3 above, since this 
	  "fitness" will determine which individual won a competition in the
	  tournament.  Afterwards in postprocessing you can then set the final
	  fitness of the individual.  Single Elimination Tournament requires
	  that your subpopulation be a power of 2 in size.

	- Round Robin.  Every individual is tested exactly once against every
	  other individual in the subpopulation.

	- K Random Opponents (One Way).  Each individual is tested against
	  exactly K other individuals, chosen at random.

	- K Random Opponents (Two Ways).  Each individual is tested against
	  AT LEAST K other individuals, chosen at random. Here the other
	  individuals are expected to have their fitnesses updated as well.
 	  In some cases an individual may have one or two more tests than
	  the others.	





2 POPULATION COMPETITIVE COEVOLUTION

Here two subpopulations are pit against one another.  Members from one
subpopulation are tested against members of the other subpopulation.  ECJ's
implementation is found in

	ec.coevolve.MultiPopCoevolutionaryEvaluator

Each individual in a subpopulation will be tested against certain individuals
in the other subpopulation.  This class allows you to specify how many of three
kinds of individuals to test against:

	1. The fittest individuals in the other subpopulation from the
	   previous generation
	
	2. Other individuals from the previous generation, selected via
	   a selection method which you specify.

	3. Random individuals from the current generation

To implement 2-population methods, you'll need to set the number of
subpopulations to 2.  You are responsible for computing the fitness of
individuals in competitive form.




N POPULATION PARALLEL/PARALLEL-PREVIOUS COOPERATIVE COEVOLUTION 

Here N populations are tested in collaboration with one another.  Members from
one subpopulation are tested by grouping them with a member each from the other
subpopulations and assessing their joint fitness.  ECJ's implementation is
again

	ec.coevolve.MutltiPopCoevolutionaryEvaluator

See above for more information about how this class works.  To implement
n-population methods, you'll need to set the number of subpopulations to N
as appropriate.  You are responsible for computing the fitness of individuals
in some cooperative form.


