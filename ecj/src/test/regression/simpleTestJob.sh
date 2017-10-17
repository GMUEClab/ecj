#!/bin/bash
#
# Copyright 2017 by Sean Luke
# Licensed under the Academic Free License version 3.0
# See the file "LICENSE" for more information
#
# SLURM cluster scheduler script for a job that executes an ECJ app $RUNS times in
# sequence, and collects data on the algorithm's performance distribution.
#
# Author: Eric O. Scott
#
#SBATCH --nodes=1
#SBATCH --ntasks=1 # How many jobs you want to run on each node
#SBATCH --cpus-per-task=2 # Optional. Number of cores you’ll be given
#SBATCH --mem-per-cpu=1000M
#SBATCH --time=0-01:00:00 # 1hr time limit
#SBATCH --output=appTest-%N-%A.out # %N is the node it ran on, %A is the job ID
#SBATCH --mail-user=escott8@gmu.edu
#SBATCH --mail-type=All
#SBATCH --job-name=ECJ_Suite

params=$1
RUNS=$2
ROOT=../../..
CLASSES=${ROOT}/target/classes/
DEPENDENCIES=${ROOT}/target/dependency/*
TESTCLASSES=${ROOT}/target/test-classes/

APP_NAME=$(basename $(dirname ${params})).$(basename ${params})
OUT=results/${APP_NAME}.csv
mkdir -p results
[ -e $OUT ] && rm ${OUT}

echo "${params}"
for i in $(seq 1 $RUNS); do
    java -Xmx1000M -cp ${CLASSES}:${DEPENDENCIES}:${TESTCLASSES} ec.Evolve \
	-file ${params} \
	-p silent=true \
	-p stat=ec.test.TestStatistics \
	-p stat.row-prefix=${APP_NAME} \
	-p stat.do-generation=false \
	-p stat.file=null >> ${OUT};
done;
