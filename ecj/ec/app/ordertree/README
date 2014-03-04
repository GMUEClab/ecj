# Copyright 2012 by James McDermott
# Licensed under the Academic Free License version 3.0
# See the file "LICENSE" for more information

The Order Tree problem is taken from "ORDERTREE: A New Test Problem
for Genetic Programming", Tuan-Hao Hoang, Nguyen Xuan Hoai, Nguyen Thi
Hien, RI McKay, Daryl Essam.

The terminals are labelled 0..(n-1) and the nonterminals are also
labelled 0..(n-1). (Note that the paper says 0, 2, ... (n-1) -- that
is a typo, as confirmed by the authors in private communication.) The
aim of the problem is essentially to arrange nonterminals and
terminals in increasing order as one descends from the root of the
tree.

As they say, "The fitness of a program tree is calculated in top-down
fashion. A node contributes one unit to the total fitness of the tree
if its content (numeric value of the label) is bigger than its parent
node content, and if its parent node is also fitness-contributing (by
default, the root node is always fitness-contributing)." Also, "if the
value of a node is equal to that of its parent node, the fitness
calculation continues by visiting the left child. If that new node’s
value is less than its own parent, the process terminates as before,
and no fitness contribution results, the whole subtree being treated
as an intron. If the node value is greater, the subtree is evaluated,
and the fitness contribution is passed to the parent. If the value is
equal to its parent’s, its left child is evaluated recursively. In all
cases, the fitness contribution of the right child is zero, so that
the right subtree acts as an intron." The algorithm given in the paper
is confusing and wrong, I think. This is the algorithm I have 
implemented:

Input: program tree t
Output: fitness of t
Global variable: fitness

Parameter: fitness contribution, can be +1 (unit), +value(c) (value),
+value(c)^2 (square), or +3^value(c) (exponential)

def nodeCal(node p):

  for each child c of p:
    if (value(p) < value(c)):
      # direct fitness contribution
      fitness += fitness_contribution(c)
	  nodeCal(c)
    else if (value(p) == value(c)):
      # neutral-left-walk
      found = false
      while c->left != null and value(c) == value(q) and not found:
        c = c->left
        if value(p) < value(c):
          found = true
      if found:
        fitness += fitness_contribution(c)
        nodeCal(c)

nodeCal(root(t))
 
