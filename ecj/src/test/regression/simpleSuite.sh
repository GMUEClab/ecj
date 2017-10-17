# !/bin/bash
#
# Copyright 2017 by Sean Luke
# Licensed under the Academic Free License version 3.0
# See the file "LICENSE" for more information
#
# Simple regression tests for ECJ apps.
#
# This script finds every parameter file in the ec/apps directories and,
# if it isn't on the list of files to exclude, launches a job to collect data on their behavior.
#
# Author: Eric O. Scott

ROOT=../../..
RSRCROOT=${ROOT}/src/main/resources

# Apps to exclude because they use distribution.  These need to be handled differently in their own script.
EXCLUDE_DISTRIBUTED="${RSRCROOT}/ec/app/star/ant.master.params
${RSRCROOT}/ec/app/star/ant.slave.params
${RSRCROOT}/ec/app/star/coevolve1.master.params
${RSRCROOT}/ec/app/star/coevolve1.slave.params
${RSRCROOT}/ec/app/star/coevolve2.master.params
${RSRCROOT}/ec/app/star/coevolve2.slave.params
${RSRCROOT}/ec/app/star/mastermeta.params
${RSRCROOT}/ec/app/star/slavemeta.params"

# Apps to exclude because they are costly to execute.  We'll still test these, but we'll spawn more fine-grained jobs for them in a different script.
EXCLUDE_COSTLY="${RSRCROOT}/ec/app/bbob/bbob.params
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
${RSRCROOT}/ec/app/mona/mona.params
${RSRCROOT}/ec/app/multiplexerslow/11.params
${RSRCROOT}/ec/app/ordertree/ordertree.params
${RSRCROOT}/ec/app/parity/parity.params
${RSRCROOT}/ec/app/push/regression.params
${RSRCROOT}/ec/app/regression/benchmark.params
${RSRCROOT}/ec/app/royaltree/royaltree.params"

# Parent files to exclude because they can't be run directly.
EXCLUDE_NOT_RUNNABLE="${RSRCROOT}/ec/app/moosuite/moosuite.params
${RSRCROOT}/ec/app/moosuite/nsga2.params
${RSRCROOT}/ec/app/moosuite/spea2.params"

EXCLUDE="${EXCLUDE_DISTRIBUTED}
${EXCLUDE_COSTLY}
${EXCLUDE_NOT_RUNNABLE}"

files=`find ${RSRCROOT}/ec/app -name *.params | grep -v -f <(echo "${EXCLUDE[@]}")`
RUNS_PER_APP=100
for f in ${files}; do
    echo ${f}
    sbatch -J ECJ_$(basename ${f}) simpleTestJob.sh ${f} ${RUNS_PER_APP}
done;