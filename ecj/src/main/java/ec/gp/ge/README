This package defines ECJ's Grammatical Evolution package.

Grammatical Evolution uses an arbitrarily-long integer list representation.
An individual is evaluated by taking the list and interpreting it according
to a grammar to produce (in the case of ECJ) a GP tree-style Individual, which 
is then evaluated.  The fitness of the GP individual becomes the fitness of
the Grammatical Evolution list representation individual

Grammatical Evolution's mapping from list to tree uses a grammar file
specified in GESpecies.  The grammar file defines the grammar for the tree.
Here is a very simple example for Artificial Ant:

<prog> ::= <op>
<op> ::= (if-food-ahead <op> <op>) 
<op> ::=  (progn2 <op> <op>) 
<op> ::= (progn3 <op> <op> <op>)
<op> ::= (left) | (right) | (move)  

We say this is simple because practically the only expanded variable name is
<op>.  This doesn't have to be the case -- a GE grammar can have a rich collection
of expanded variables.  Grammatical Evolution's use of a grammar in fact allows
it to define a fairly complex type system that's not easy to replicate with ECJ's
(or GP in general) standard typing options.

The mapping works as follows.  We start with the first variable (in this case,
<prog>.  It expands solely to <op>.  Now we have a choice -- what should <op>
expand to?  There are six choices from the grammar above.  To make the decision,
we consult the first number in the GE Individual, mod 6.  That result is our
expansion.  We then proceed to expand each of the new elements: for example,
if <op> had expanded to (if-food-ahead <op> <op>), we now have two new <op>s
to expand.  We proceed depth-first, each time consulting the next number in
the GE Individual.  When the GPTree is completed and there are no more
expansions, it's handed to a GPProblem to be tested.

If the Individual doesn't have enough integers to cover all the needs of
the grammar, it is considered to be a "bad" Individual and is given a poor
fitness.

Thus a Grammatical Evolution problem requires several pieces:

1. A grammar file to interpret by.
2. A GEIndividual (basically a ByteVectorIndividual) to hold the numbers.
3. A GESpecies to load the grammar and be available to perform the mapping
   from GEIndividual to GPIndividual.
4. A GEProblem to take a GEIndividual, map it through the GEPSpecies, and
   hand it off to a GPProblem to be tested.
5. A GPProblem to do the testing.

Grammatical Evolution can handle ERCs and ADFs as well.  To handle the multiple
trees required by an ADF, we provide multiple grammar files.  See the Lawnmower
example (ec/app/lawnmower) for a demonstration.

Grammatical Evolution requires quite a lot of manipulation of the GP subsystem
to pull all this off.  Notably:

1. The problem will be a GEProbem.  It in turn holds onto the "true" GPProblem
   in a 'problem' parameter
2. The species will be a GESpecies.  It in turn holds onto the "true" GPSpecies
   in a 'gp-species' parameter.

Generally Grammatical Evolution is surprisingly easy to implement in ECJ.  The 
ge/ge.params file will do most of the heavy lifting for you if you're dealing
with just a single tree (no ADFs say).  But in some cases we'll need to add 
special GP elements to this subsidiary GPSpecies, mostly for additiona trees.
For example, in the Lawnmower problem, to have three trees, we need to define
them manually as shown in ec/app/lawnmower/ge.params

Grammatical Evolution has three common breeding operators for its lists of
integers:

- TRUNCATION: Integers unused during the mapping process are later chopped off
  the end of the Individual.  This is implemented using
  ec/gp/ge/breed/GETruncationPipeline

- GE CROSSOVER: This is just like ListCrossover, except that the crossed over
  ranges must include SOME number of genes which were consumed by each of
  the Individuals when producing trees.  Normally only one-point crossover
  is used, but ECJ provides both one- and two-point crossover.  This is
  implemented using ec/gp/ge/breed/GECrossoverPipeline

- GENE DUPLICATION: A string of integers in the list is copied and reinserted
  "downstream" in the list.  This is implemented elsewhere by the class 
  ec/vector/breed/GeneDuplicationPipeline

To this any number of other list crossover and mutation operators could be used.
NOTE that the pipeline defined in ge.params is pretty arbitrary -- you might want
to build something better.



EXAMPLES

See the ec/app/ant, ec/app/regression, and ec/app/lawnmower directories for
GE versions of these GP problems.



CLASSES


GEIndividual.java

This class is just a simple subclass of ByteVectorIndividual which also prints out
the tree form of the Individual.


GESpecies.java

A version of IntegerVectorSpecies which loads the grammar file or files, holds onto
the GPSpecies, and performs the actual mapping prior to evaluation of GEIndividuals.


GrammarParser.java

The parser which reads the grammar rules from the provided grammar files and converts
them into a parse graph.  This parse graph is then what's actually used by GESpecies
to map GEIndividuals into GPIndividuals.  You can replace this parser with one of your
own.


GrammarNode.java
GrammarFunctionNode.java
GrammarRuleNode.java

These form the actual objects in the parse graph.  GrammarNode is an abstract superclass.
GrammarFunctionNode defines functions with arguments (its children).  GrammarRuleNode
defines rules with rule options (its children).


GEProblem.java

A version of Problem which is inserted in lieu of the GPProblem, and which in turn
holds onto the GPProblem.  The GEProblem first maps the GEIndividual into a GPIndividual
using the GESpecies.  It then hands the GPIndividual to the GPProblem to be evaluated.


[in the ge.breed subpackage] 

GETruncationPipeline.java

A special list operator which only works with GEIndividuals.  Truncates from the 
list all those integers which weren't used during the mapping process.

GECrossoverPipeline.java

A special list operator which only works with GEIndividuals.  Guarantees that crossed
over regions include some consumed genes.


[elsewhere in the ec.vector.breed package]

GeneDuplicationPipeline.java

A special list operator which duplicates genes downstream.  Generally used by
GEIndividuals but it's available in ec.vector.breed because it can be used by any
VectorIndividual if you'd really want to.

