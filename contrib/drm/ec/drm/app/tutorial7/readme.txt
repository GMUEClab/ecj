This example illustrates how to implement a Master/Slave model using DRM for a 
regression problem that requires samples from file. It would be the same for
any DRM kind of experiment.

Check that the logic for loading and parsing the data is inside 
ec.drm.app.tutorial7.MyData.setup() and that MultiValuedRegression is instructed
to take it from the data field of the EvolutionAgent for its use.

Compress into a jar at least the following directories:
ec
ec.app.tutorial1
ec.breed
ec.drm
ec.drm.app.tutorial7
ec.drm.masterslave
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

drmnode.new -g alc -p 10121 -v 3 -r tutorial.jar\!ec.drm.masterslave -a -master master.params -slave slave.params

where
-master is the path to the master parameters file
-slave is the path to the slave parameters

You will probably have to kill and restart the receptor nodes between executions, while I found why they hang :P