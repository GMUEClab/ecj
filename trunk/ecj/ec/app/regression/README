This directory holds various versions of Koza's Symbolic Regression problem.

QUARTIC:	x^4 + x^3 + x^2 + x	(Koza I)
	Basic class: 	Regression.java
	Using an ERC:	java ec.Evolve -file erc.params
	Not using an ERC:	java ec.Evolve -file noerc.params
	Grammatical Evolution Variant:
		GE Grammar file:	regression.grammar
		Using an ERC:	java ec.Evolve -file ge.params
	Steady State Variant:
		Using no ERCs:	java ec.Evolve -file steadynoerc.params
	
QUINTIC:	x^5 - 2 * ^3 + x	(Koza II)
	Basic class:	Quintic.java
	Using an ERC:	java ec.Evolve -file quintic.params
	Not using an ERC:	java ec.Evolve -file quinticnoerc.params

SEXTIC:		x^6 - 2 * x^4 + x^2	(Koza II)
	Basic class:	Sextic.java
	Using an ERC:	java ec.Evolve -file sextic.params
	Not using an ERC:	java ec.Evolve -file sexticnoerc.params

BENCHMARKS
	The benchmarks class contains about 50 benchmark examples drawn from the
	literature.  This class is part of the GP Benchmarks discussion which took
	place at GECCO 2011 in Dublin.  See the GP Benchmarks Wiki page at:
		http://groups.csail.mit.edu/EVO-DesignOpt/GPBenchmarks/
	The official definition of these functions is NOT the ECJ definition, but
	rather is the paper "Genetic Programming Needs Better Benchmarks", by 
	James McDermott, David R. White, Sean Luke, Luca MAnzoni, Mauro Castelli,
	Leonardo Vanneschi, Wojciech Jaskowski, Krzysztof Krawiec, Robin Harper,
	Kenneth De Jong, and Una-May O'Reilly, found at GECCO 2012.

	See the benchmark.params file for extensive description of your options.

	THE BENCHMARKS CODE IS IN A STATE OF FLUX.  It may have bugs and will change
	at any time.  But it's useful for showing advanced approaches in ECJ for handling
	training/test data, different numbers of variables, and distributions of
	input variables.

	Basic class:	Benchmarks.java
	Using an ERC:	java ec.Evolve -file benchmark.params
	
	NOTE: other benchmarks will require different function sets.  So for example, if you say:

		java ec.Evolve -file benchmark.params -p eval.problem.type=vladislavleva-3 

	... you will get the following warning:

		WARNING:
		The number of variables for the vladislavleva-3 problem (2) is normally 
		handled by the function set vladislavleva-c2 but you are using korns5.  
		Hope you know what you're doing.  To correct this, try adding the 
		parameter gp.tc.0.fset=vladislavleva-c2


	To fix this, you say:

		java ec.Evolve -file benchmark.params -p eval.problem.type=vladislavleva-3 -p gp.tc.0.fset=vladislavleva-c2



