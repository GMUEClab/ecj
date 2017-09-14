# Copyright 2012 by James McDermott
# Licensed under the Academic Free License version 3.0
# See the file "LICENSE" for more information

The Lid problem is taken from "What Makes a Problem GP-Hard?
Validating a Hypothesis of Structural Causes", Jason M. Daida,
Hsiaolei Li, Ricky Tang, and Adam M. Hilss, in E. Cant\'u-Paz et
al. (Eds.): GECCO 2003, LNCS 2724, pp. 1665–1677, 2003. (c)
Springer-Verlag Berlin Heidelberg 2003.

The objective is to find a tree of a given size and depth. Content is
not important. It is tunable by varying the target size and depth. The
name comes from Daida et al's hypothesis that randomly-generated GP
programs form a metaphorical "bottle" when arranged in a (depth x
size) space: the interior of the bottle is easy to find, but the walls
and exterior (extremely high or low values of size for a given depth)
are more difficult.

