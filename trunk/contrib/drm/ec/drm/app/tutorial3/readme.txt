# Copyright 2007 by Alberto Cuesta Ca�ada and Instituto
# Tecnol�gico de Inform�tica

This example illustrates how to implement a Island model using EvolutionAgents, 
which sends identical islands to as many nodes as it finds to solve it cooperatively
exchanging individuals.

Compress into a jar at least the following directories:
ec
ec.app.tutorial1
ec.breed
ec.drm
ec.drm.app.tutorial3
ec.drm.peerselect
ec.eval
ec.exchange
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

where you decide the content for 
-g (any name you want your run to have),
-p (a free port on your machine)
-v (the verbosity level, 3 is fine)
-r (the path to the jar file, and the path to the drm launcher, separated by \! if you are in linux and ! if you are in windows)
-a (means the following argument will be interpretated by the drm launcher or ECJ)
-params (the path to the parameters file)

You will probably have to kill and restart the receptor nodes between executions, while I found why they hang :P

