# Copyright 2007 by Alberto Cuesta Cañada and Instituto
# Tecnológico de Informática

This example encapsulates a simple ECJ experiment inside an EvolutionAgent.

Compress into a jar at least the following directories:
ec
ec.app.tutorial1
ec.breed
ec.drm
ec.drm.app.tutorial1
ec.eval
ec.exchange
ec.select
ec.simple
ec.steadystate
ec.util
ec.vector
ec.vector.breed

and execute something like

drmnode.new -g alc -p 10121 -v 3 -r tutorial.jar\!ec.drm.DRMLauncher -a -params tutorial.params

where you decide the content for 
-g (any name you want your run to have),
-p (a free port on your machine)
-v (the verbosity level, 3 is fine)
-r (the path to the jar file, and the path to the drm launcher, separated by \! if you are in linux and ! if you are in windows)
-a (means the following argument will be interpretated by the drm launcher or ECJ)
-params (the path to the parameters file)