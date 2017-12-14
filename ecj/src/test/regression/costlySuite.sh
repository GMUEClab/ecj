# !/bin/bash
#
# Copyright 2017 by Sean Luke
# Licensed under the Academic Free License version 3.0
# See the file "LICENSE" for more information
#
# Launches a regression tests for some especially expensive example apps
# on a SLURM cluster.
#
# Author: Eric O. Scott

ROOT=../../..
RSRCROOT=${ROOT}/src/main/resources
# Excluding ${RSRCROOT}/ec/app/mona/mona.params because it takes too long to run (and hogs memory on the nodes).
files="${RSRCROOT}/ec/app/bbob/bbob.params
${RSRCROOT}/ec/app/bbob/de.params
${RSRCROOT}/ec/app/cartpole/cartpole.params
${RSRCROOT}/ec/app/coevolve2/coevolve2.params
${RSRCROOT}/ec/app/ecsuite/de.params
${RSRCROOT}/ec/app/ecsuite/ecsuite.params
${RSRCROOT}/ec/app/ecsuite/meta.params
${RSRCROOT}/ec/app/ecsuite/pso.params
${RSRCROOT}/ec/app/ecsuite/steady.params
${RSRCROOT}/ec/app/gui/coevolve2.params
${RSRCROOT}/ec/app/lid/lid.params
${RSRCROOT}/ec/app/majority/majorityga.params
${RSRCROOT}/ec/app/majority/majoritygp.params
${RSRCROOT}/ec/app/multiplexerslow/11.params
${RSRCROOT}/ec/app/ordertree/ordertree.params
${RSRCROOT}/ec/app/parity/parity.params
${RSRCROOT}/ec/app/push/regression.params
${RSRCROOT}/ec/app/regression/benchmark.params
${RSRCROOT}/ec/app/royaltree/royaltree.params"

for f in ${files}; do
    echo ${f}
    sbatch -J ECJ_$(basename ${f}) costlyTestJob.sh ${f}
done;