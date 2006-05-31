#! /bin/tcsh

# Copyright 2005 by Sean Luke
# Licensed under the Academic Free License version 3.0
# See the file "LICENSE" for more information

# Set ECJ_HOME to the 'ecj' directory
setenv ECJ_HOME       ${0:h}/..

# Add ECJ_HOME to the original classpath, even if the original is empty
setenv ORIGINAL_CLASSPATH `printenv CLASSPATH`
setenv CLASSPATH .:${ECJ_HOME}:${ORIGINAL_CLASSPATH}

# Tack on jar files in the 'ecj' directory.
# Turn off matching first so foreach doesn't freak on us.
# That will require testing for the existence of ${ECJ_HOME}/\*\.jar, which
# will tell us that there were no jar files to be had.
set nonomatch=true
set jars=(${ECJ_HOME}/*.jar)
if ("$jars" != "${ECJ_HOME}/\*\.jar") then
        foreach i ($jars)
                setenv CLASSPATH ${i}:${CLASSPATH}
        end
endif

# Since we want to do Java 1.4.1, let's make sure it's not aliased to 1.3.1
unalias java
java ec.display.Console >& /dev/null&
