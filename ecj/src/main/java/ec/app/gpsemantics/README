# Copyright 2012 by James McDermott
# Licensed under the Academic Free License version 3.0
# See the file "LICENSE" for more information

The Order and Majority problems are taken from "Where does the good
stuff go, and why? How contextual semantics influences program
structure in simple genetic programming" Goldberg, D. and O'Reilly,
U.M., in EuroGP 1998.

The Order and Majority problems each use the same tree-based
representation. There is a single binary non-terminal, "J", or
"Join". For a problem of size n (which is tunable), there are 2(n+1)
terminals, X0, X1, .. Xn and N0, N1, .. Nn. Ni has the sense of ~Xi.

The two problems define two different mappings from a tree genotype to
a linear phenotype. In both cases, we begin with an empty-list
phenotype and traverse the tree in a depth-first left-to-right
manner. It doesn't matter whether it's preorder, inorder, or postorder
because we ignore the non-terminals. We "express" the terminals by
appending them to the phenotype as follows.

For Order, for each index i, the first terminal of that index
encountered during traversal is expressed (whether it is Xi or
Ni). Later terminals of the same index are ignored.

For Majority, for each index i, Xi is expressed if there is at least
one Xi in the traversal, and there are at least as many Xi as there
are Ni.

For both problems, the phenotype to fitness mapping is the same: just
count the number of occurences of Xi terminals.
