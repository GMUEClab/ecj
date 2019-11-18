# The ECJ Evolutionary Computation Toolkit

<!--- [![Build status](https://travis-ci.org/GMUEClab/ecj.svg?branch=master)](https://travis-ci.org/GMUEClab/ecj) -->

ECJ is an evolutionary computation framework written in Java. The system was designed for large, heavyweight experimental needs and provides tools which provide many popular EC algorithms and conventions of EC algorithms, but with a particular emphasis towards genetic programming. ECJ is free open-source with a BSD-style academic license (AFL 3.0).

ECJ is now well over fifteen years old and is a mature, stable framework which has (fortunately) exhibited relatively few serious bugs over the years. Its design has readily accommodated many later additions, including multiobjective optimization algorithms, island models, master/slave evaluation facilities, coevolution, steady-state and evolution strategies methods, parsimony pressure techniques, and various new individual representations (for example, rule-sets). The system is widely used in the genetic programming community and is reasonably popular in the EC community at large, where it has formed the basis of many theses, publications, and commercial products.

## ECJ's Website

This is ECJ's repository, but [ECJ's official website](http://cs.gmu.edu/~eclab/projects/ecj/) is elsewhere.  Before doing anything else, we'd recommend you started there.

## Getting Started

For instructions on how to begin using the ECJ binary distribution and/or build the source package, take a look at the readme in the '[ecj/](ecj/)' subdirectory.

Going forward, you may also want to avail yourself of
 * the extensive [ECJ Manual](https://cs.gmu.edu/~eclab/projects/ecj/manual.pdf), which explains most of ECJ's features and algorims in detail, with instructions on how to use them,
 * the [ECJ tutorials](ecj/docs/tutorials),
 * and the built-in collectin of example applications (source code [here](ecj/src/main/java/ec/app), parameter files [here](ecj/src/main/resources/ec/app)).

## Citing ECJ

The preferred way to cite ECJ is

 > Sean Luke. ECJ Evolutionary Computation Library (1998).  Available for free at http://cs.gmu.edu/~eclab/projects/ecj/

or in BibTex like so:
```
@misc { Luke1998ECJSoftware,
author       = { Sean Luke },
title        = { {ECJ} Evolutionary Computation Library },
year         = { 1998 },
note         = { Available for free at http://cs.gmu.edu/$\sim$eclab/projects/ecj/  }
}
```
