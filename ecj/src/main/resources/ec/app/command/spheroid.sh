#!/usr/bin/env sh
#
# An example of a script that encodes a fitness function.
# 
#
fitnesses=$(python -c "import sys; print('\n'.join([str(sum(float(x)**2 for x in line.split(','))) for line in sys.stdin]))")
echo "$fitnesses"