# Copyright 2012 by Luca Manzoni
# Licensed under the Academic Free License version 3.0
# See the file "LICENSE" for more information

A very preliminary version of an implementation of K landscapes.
The code currently lacks both polish and documentation.

The K-landscape benchmark is described in: "Leonardo Vanneschi, Mauro Castelli, Luca Manzoni: The K landscapes: a tunably difficult benchmark for genetic programming. GECCO 2011: 1467-1474"

There is one parameters:
1) eval.problem.k-landscapes.k-value. This is the value of k for tuning the difficulty of the problem

The current implemntation uses 4 terminal symbols and 2 functional symbols with arity 2.
