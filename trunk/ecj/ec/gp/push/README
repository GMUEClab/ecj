This directory contains an experimental version of the Push/PushGP
facility running on top of a java-based Push interpreter called
"Psh".  To use it you will need to install the psh.jar file which
comes with ECJ's library jar files (see the ECJ website).  Do not
use the version on the Psh website, as it has the wrong kind of
random number generator.  

The makefile does not by default build this directory, but you can 
do so by saying    make push

The code works by creating a GPIndividual consisting solely of
two kinds of nodes: dummy nonterminal nodes (ec.gp.push.Nonterminal)
and leaf nodes holding Push instructions (ec.gp.push.Terminal).  
The nonterminals
do nothing at all, all that matters are the leaf nodes.  A leaf
node is a GP ERC which contains a single randomly-chosen Push
instruction.  Push instructions can be one of three possible types:

	- Built-in Push instructions understood by the Psh
	  interpreter.  Examples include float.* or integer.+
	  A full list of valid Psh instructions is at the end
	  of this file.

	- Push ERCs.  Psh supports two ERC types: floats and
	  integers.  For ERCs you will supply a minimum and
	  maximum value.

	- Custom instructions of your own devising.  These
	  are subclasses of ec.gp.push.PushInstruction.

You test an individual by creating a subclass of PushProblem
which creates or resets a Push Interpreter, then seeds the
interpreter's stacks with data as necessary, then executes
the GP Individual on the interpreter, then reads the stacks
and compares against expected results.  

A GP Individual is not directly executed on the Push Interpreter.
Instead at present it is written out to a string which is then
parsed into the interpreter.

Note that the psh.jar file provided with ECJ contains modified 
versions of the Interpreter.java and Instructions.java files
which do two things:
	A. Replace java.util.Random with MersenneTwisterFast
	B. Fix errors in usage of random number generators


WEAKNESSES

This is just an experimental approach, though it does work fine.

1. Slow.  Psh isn't super fast.

2. There's no code (yet) for taking push programs and rebuilding
   the GP tree to reflect the program after evaluation.  This is
   a common Push task, so it'd be nice to do it at some point.

3. At present Push ERC instructions must be hard-coded in the
   Terminal.java file.  Right now we have float.erc and integer.erc
   If necessary we could break out ERCs into custom classes though
   I suspect there wouldn't be much return on investment there.

4. You'll have to be careful with the tree modification operators
   you choose because some of them assume that the arity is fixed.
   But GP crossover should work as well as tree mutation if you use
   the provided PushBuilder tree builder.

5. Current Push work is being done on a Clojure-based interpreter.
   We're using Psh here because it was the fastest way to get up
   and running but eventually we may move to the Clojure version.



CLASSES

Nonterminal.java

All ECJ Push trees consist of non-leaf nodes that are all instances
of the class Nonterminal.  Nonterminals do nothing at all -- they
can't even be evaluated.  But Nonterminals do have one unusual
feature: they have arbitrary arity (>= 1).


PushBuilder.java

PushBuilder is a GPNodeBuilder which implements a standard tree-
generation algorithm used in Push.  This algorithm respects trees
of arbitrary arity, so it works properly with Nonterminal.


Terminal.java

All ECJ Push trees consist of leaf nodes that are all instances of
the class Terminal.  Terminal is an ECJ GP ERC which holds as its 
value a *String* which holds a Push instruction.  This is how ECJ
represents trees holding a wide range of different Push 
instructions within them.  


PushProblem.java

PushProblem is a GPProblem with various utility methods to make it
easier to create a Push interpreter or reset it, to convert the GP
tree into a Push program and submit it to the interpreter, to
initialize the Push interpreter's stacks with data, and to read
data off of the stacks after the program has been run.


PushInstruction.java

PushInstruction allows you to define custom Push instructions which
are automatically submitted to the interpreter.  These differ from
the built-in instructions listed below.



BUILT-IN PUSH INSTRUCTIONS IN THE PSH INTERPRETER

integer.+
integer.-
integer./
integer.%
integer.*
integer.pow
integer.log
integer.=
integer.>
integer.<
integer.min
integer.max
integer.abs
integer.neg
integer.ln
integer.fromfloat
integer.fromboolean
integer.rand
float.+
float.-
float./
float.%
float.*
float.pow
float.log
float.=
float.>
float.<
float.min
float.max
float.sin
float.cos
float.tan
float.exp
float.abs
float.neg
float.ln
float.frominteger
float.fromboolean
float.rand
boolean.=
boolean.not
boolean.and
boolean.or
boolean.xor
boolean.frominteger
boolean.fromfloat
boolean.rand
code.quote
code.fromboolean
code.frominteger
code.fromfloat
code.noop
exec.k
exec.s
exec.y
exec.noop
exec.do*times
code.do*times
exec.do*count
code.do*count
exec.do*range
code.do*range
code.=
exec.=
code.if
exec.if
code.rand
exec.rand
true
false
input.index
input.inall
input.inallrev
input.stackdepth
frame.push
frame.pop

