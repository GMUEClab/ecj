@ECHO OFF

rem Copyright 2005 by Sean Luke and George Mason University
rem Author: Gabriel Balan
rem Licensed under the Academic Free License version 3.0
rem See the file "LICENSE" for more information


rem We need this batch file because we cannot repeatedly append things to a variable
rem [in a loop] without turning on the delayed expansion/evaluation.
rem (i.e. all occurances of CLASSPATH in the batch file are replaced when the
rem script is started).
rem using a separate script for each append forces a fresh copy of CLASSPATH be read everytime

IF NOT %1==""  set classpath=%classpath%;%1

