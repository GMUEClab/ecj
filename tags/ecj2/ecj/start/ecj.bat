@ECHO OFF

rem Copyright 2005 by Sean Luke and George Mason University
rem Author: Gabriel Balan
rem Licensed under the Academic Free License version 3.0
rem See the file "LICENSE" for more information


rem Save the classpath so we can restore it at the end.
SET OLDCLASSPATH=%CLASSPATH%;


rem Set ECJ_HOME to the 'ecj' directory
set STARTING_POINT=%CD%
cd ..
SET ECJ_HOME=%CD%
cd %STARTING_POINT%


rem Add ECJ_HOME to the original classpath
rem (btw, ignoreme.bat adds its argument to the classpath)
call ignoreme.bat %ECJ_HOME%


rem If you don't keep your jars in the ECJ_HOME, change this:
SET JAR_DIR=%ECJ_HOME%


rem Add all jars in the jar directory to the classpath.
(for /F %%f IN ('dir /b /a-d %JAR_DIR%\*.jar') do call ignoreme.bat %JAR_DIR%\%%f%) 2>nul



java ec.display.Console >nul 2>nul 

rem Restore the classpath.
SET CLASSPATH=%OLDCLASSPATH%;
