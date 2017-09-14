This package implements various Differential Evolution algorithms from
"Differential Evolution: A Practical Approach to Global Optimization"
by Kenneth Price, Rainer Storn, and Jouni Lampinen.  

ECJ's implementation of Differential Evolution requires that the
user specify one of several Breeders, and also that you (optionally but
highly suggested) use a specific Evaluator.

The Breeder options implement various Differential Evolution Algorithms.
Options include:

breed = 	ec.de.DEBreeder
breed = 	ec.de.Best1BinDEBreeder
breed = 	ec.de.Rand1EitherOrDEBreeder

The Evaluator is ec.de.DEEvaluator:

eval =          ec.de.DEEvaluator

The ec/app/ecsuite/de.params file contains an example.


ECJ's DE implementation (for now)
---------------------------------

ECJ implements Differential Evolution by largely by replacing the Breeder.
The various DE breeders work like this: for each individual i in the 
population, construct a new individual using a function called 
createIndividual(..., i, ...).  Then replace the entire population with the
newly constructed individuals only if they are superior to the parents
from which they were derived.  The "only if" part is implemented by DEEvaluator,
and if you don't use it, the children will directly replace their parents.

In DE, all individuals are DoubleVectorIndividuals.

DEBreeder has an additional method called crossover(...) which crosses the
original parent into the child.  Some operators use this method, others
do not.  You can override the method to perform your own custom kind of crossover;
the default does a uniform crossover, while guaranteeing that at least one
gene (chosen at random) from the child will be preserved.

The different breeders differ largely based on how 
createIndividual(..., i, ...) is implemented.  In all versions,
createIndividual(..., i, ...) begins by choosing three random individuals
r0, r1, and r2 which are different from one another and from i.  Let j
be the new individual.

There are four parameters which may affect the operation below:

F:          scaling factor for mutating individuals
CR:         the probability, per-gene, of crossing over
F_NOISE:    Bounds on random noise added to F
PF:         Probability of picking one of two subalgorithms for mutation


The default version, implemented by

	ec.de.DEBreeder

... creates a new individual as follows.

j[g] <-- r0[g] + F * (r1[g] - r2[g])

This is the "classic" DE algorithm


The next version, implemented by

        ec.de.Best1BinDEBreeder

... works like this.  We first determine the best individual in the
population.  Then we say:

j[g] <-- best[g] + (F + random(-F_NOISE / 2, F_NOISE / 2)) * (r1[g] - r2[g])


The final version, implemented by

	ec.de.Rand1EitherOrDEBreeder	`  

... works like this.  First we flip a coin of probability PF.  If it comes
up 'true', then we generate the individual as the classic form:

j[g] <-- r0[g] + F * (r1[g] - r2[g])

... else we generate the individual as:

j[g] <-- r0[g] + 0.5 * (F + 1) * (r1[g] + r2[g] - 2 * r0[g])


Don't like any of these?  It should be fairly straightforward to copy an
existing one and modify it.   

Note that DEBreeder is a subclass of Breeder and does NOT implement
multithreaded breeding: but DEEvaluator, as a subclass of SimpleEvaluator,
DOES implement multithreaded evaluation.
