This example illustrates how to use the describe function to test the
evolved best individual at the end of an experiment with some test samples.

Check that the logic for loading and parsing the data is inside 
ec.drm.app.tutorial8.MyProblemData.

TrI.data is the training input data.
TrO.data is the training output data, which we will evolve individuals 
to match with.

TeI.data is the testing input data.
TeO.data is the testing output data, which will give use the effectivity 
of the experiment.

IslandAgent is identical to the one in tutorial3.

MultiValuedRegression is instructed to take samples from the data field
of the EvolutionAgent for its use. The describe() function is identical to
the evaluate() function but it ignores the ind.evaluated flag and uses the
test samples instead of the train samples.

The MyStatisticsData extends StatisticsData to include an extra field, which
contains the best individuals tested with the test samples. The toString()
method is modified to reflect this new field.

MyStatistics extends from DRMStatistics so inside finalStatistics() it can
call MultiValuedRegression.describe() and pack the resulting individuals
into a MyStatisticsData structure, which will be sent to the root island.

Compress into a jar at least the following directories:
ec
ec.breed
ec.drm
ec.drm.app.tutorial8
ec.drm.peerselect
ec.eval
ec.exchange
ec.gp
ec.gp.breed
ec.gp.build
ec.gp.koza
ec.select
ec.simple
ec.steadystate
ec.util
ec.vector
ec.vector.breed

start the receptor nodes with something like

drmnode.new -g alc -p 10121 -v 3 -n 192.168.0.1:10121

where -n is the IP address and port of the emissor node, 
where you will execute something like

drmnode.new -g alc -p 10121 -v 3 -r tutorial.jar\!ec.drm.DRMLauncher -a -params tutorial.params

You will probably have to kill and restart the receptor nodes between executions, while I found why they hang :P