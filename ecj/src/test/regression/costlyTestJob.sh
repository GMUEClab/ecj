#!/bin/bash
#SBATCH --nodes=1
#SBATCH --ntasks=1 # How many jobs you want to run on each node
#SBATCH --cpus-per-task=2 # Optional. Number of cores you’ll be given
#SBATCH --mem-per-cpu=1000M
#SBATCH --time=0-01:00:00 # 1hr time limit
#SBATCH --output=appTest-%N-%A_%a.out # %N is the node it ran on, %A is the job ID
#SBATCH --mail-user=escott8@gmu.edu
#SBATCH --mail-type=All
#SBATCH --job-name=ECJCostly
#SBATCH --array=0-99%20

params=$1
ROOT=../../..
CLASSES=${ROOT}/target/classes/
DEPENDENCIES=${ROOT}/target/dependency/*
TESTCLASSES=${ROOT}/target/test-classes/

APP_NAME=$(basename $(dirname ${params})).$(basename ${params})
OUT=results/${APP_NAME}.${SLURM_ARRAY_TASK_ID}.csv
mkdir -p results
[ -e $OUT ] && rm ${OUT}

echo "${params} (job ${SLURM_ARRAY_TASK_ID}"
java -Xmx1000M -cp ${CLASSES}:${DEPENDENCIES}:${TESTCLASSES} ec.Evolve \
    -file ${params} \
    -p silent=true \
    -p stat=ec.test.TestStatistics \
    -p stat.row-prefix=${APP_NAME} \
    -p stat.do-generation=false \
    -p stat.file=null >> ${OUT};