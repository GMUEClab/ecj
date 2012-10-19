# Copyright 2012 by James McDermott
# Licensed under the Academic Free License version 3.0
# See the file "LICENSE" for more information

The Royal Tree problem is taken from "The royal tree problem, a
benchmark for single and multi-population genetic programming", Punch,
Zongker and Goodman, in Angeline and Kinnear (eds) Advances in Genetic
Programming II, 1996, MIT Press.

The objective is to match a "complete" target tree as closely as
possible. There is a single terminal, X (arity 0), and multiple
functions, A (arity 1), B (arity 2), C (arity 3), etc. A complete tree
of depth 2 is just (A X). A complete tree of depth 3 is (B (A X) (A
X)). If the highest-arity function is D (arity 4), then the target
tree has root node D, with 4 children C, each with three children B,
each with two children A, each with one child X. The problem is
tunable by varying the alphabet.

Fitness is calculated by comparing to the target tree, in a
complicated formula as follow (based on the description in "Program
Distribution Estimation with Grammar Models", Yin Shan, 2005):

---
The raw fitness of the tree (or any sub-tree) is the score of its
root. Each node calculates its score by summing the weighted scores of
its direct children. If the child is a perfect tree of the appropriate
level (for instance, a complete level-C tree beneath a D node), then
the score of that sub-tree, times a FullBonus weight, is added to the
score of the root. If the child has the correct root but is not a
perfect tree, then the weight is multiplied by a PartialBonus. If the
child's root is incorrect, then the weight is multiplied by
Penalty. After scoring the root, if the function is itself the root of
a perfect tree, the final sum is multiplied by CompleteBonus. Typical
values used are: FullBonus = 2, PartialBonus = 1, Penalty = 1/3, and
CompleteBonus = 2.

The score base case is a level-A tree, which has a score of 4 (the
A-x connection is worth 1, times the FullBonus, times the
CompleteBonus). The reasoning behind this "stairstep" approach is
to give a big jump in evaluation credit to each proper combination,
so that the problem can be solved by progressively discovering
sub-solutions and combining them. The FullBonus is provided to give
a large credit to those trees that find the correct, complete royal
tree child. The PartialBonus is used to give credit for finding the
proper, direct child for a node, even if that direct child is not
the root of a royal tree. This pressure is not as great as the
FullBonus, but it is an effective incentive, since the score is
determined recursively down the tree, and thus each node receives
some credit when if finds its proper, direct children. If a node
does not have the correct, direct children, it is penalised by
Penalty, making the FullBonus and PartialBonus even more effective.
Finally, if the resulting tree is itself complete, then a very
large credit is given. The reasoning behind the increase in arity
required at each increased level of the royal tree is to introduce
tunable difficulty.
---

The aim is to make the largest possible complete tree. In other words,
(B (A x) (A x)) is always better than (A x). There is no sense of a
"target of depth 1". The best possible fitness is determined by the
number of non-terminals available in the language, not by any "target
depth".

I've tested this code and it produces the correct values for perfect
trees at level A (8), B (32), C (384). Note that these aren't really
"maximum" scores -- a tree like (B (B (A X) (A X)) (B (A X) (A X))) is
not a perfect B-tree, but it contains two perfect B-trees, and it will
have a higher score than just a single perfect B-tree. This code also
reproduces the values given in Punch et al, Fig 15.2.


