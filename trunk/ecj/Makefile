#
# Makefile created by Jeff Bassett, with some
# tweaks by Sean Luke
#
# We assume that you have jikes on your system.  It's a Java compiler
# written in C++ by IBM that's far *far* faster than javac.  You can
# get it at http://oss.software.ibm.com/developerworks/opensource/jikes/`
# To just use javac, simply change the JAVAC flag below as appropriate.
#
# To compile everything but charting:	make 
# To delete class files:		make clean
# To build the documentation:		make docs
# To build using jikes for sure:	make jikes
# To build using javac for sure:	make javac
# To auto-indent using Emacs:		make indent
# To build everything + charting:	make chart
#	(requires JFreeChart: www.jfree.org/jfreechart/)
#	(requires iText: www.lowagie.com/iText/)
# [also, used here at GMU, you can ignore it...]
# Prepare for distribution:		make dist
#

#JAVAC = javac
JAVAC = jikes ${JIKESFLAGS}

JIKESFLAGS = -target 1.2 +Pno-shadow
FLAGS = -g

DIRS = \
ec/*.java \
ec/app/ant/*.java \
ec/app/ant/func/*.java \
ec/app/ecsuite/*.java \
ec/app/edge/*.java \
ec/app/edge/func/*.java \
ec/app/lawnmower/*.java \
ec/app/lawnmower/func/*.java \
ec/app/multiplexer/*.java \
ec/app/multiplexer/func/*.java \
ec/app/multiplexerslow/*.java \
ec/app/multiplexerslow/func/*.java \
ec/app/parity/*.java \
ec/app/parity/func/*.java \
ec/app/regression/*.java \
ec/app/regression/func/*.java \
ec/app/sum/*.java \
ec/app/tutorial1/*.java \
ec/app/tutorial2/*.java \
ec/app/tutorial3/*.java \
ec/app/tutorial4/*.java \
ec/app/coevolve1/*.java \
ec/app/coevolve2/*.java \
ec/app/twobox/*.java \
ec/app/twobox/func/*.java \
ec/breed/*.java \
ec/coevolve/*.java \
ec/es/*.java \
ec/eval/*.java \
ec/exchange/*.java \
ec/gp/*.java \
ec/gp/breed/*.java \
ec/gp/build/*.java \
ec/gp/koza/*.java \
ec/multiobjective/*.java \
ec/select/*.java \
ec/simple/*.java \
ec/spatial/*.java \
ec/steadystate/*.java \
ec/util/*.java \
ec/vector/*.java \
ec/vector/breed/*.java \
ec/parsimony/*.java\
ec/rule/*.java \
ec/rule/breed/*.java \
ec/multiobjective/spea2/*.java \
ec/display/*.java \
ec/display/portrayal/*.java

all: base
	@ echo This builds the code except for charting 
	@ echo For other Makefile options, type:  make help

base:
	${JAVAC} ${FLAGS} ${DIRS}

chart: base
	${JAVAC} ${FLAGS} ec/display/chart/*.java ec/app/gui/*.java
	@ echo This builds the base code and the charting code

clean:
	find . -name "*.class" -exec rm -f {} \;
	find . -name "*.stat" -exec rm -f {} \;
	find . -name ".DS_Store" -exec rm -rf {} \;
	find . -name "*.java*~" -exec rm -rf {} \;
	rm -rf docs/classdocs/*

doc:
	javadoc -d docs/classdocs ec ec.gp ec.gp.koza ec.simple ec.select ec.multiobjective ec.steadystate ec.util ec.breed ec.gp.breed ec.gp.build ec.es ec.exchange ec.vector ec.vector.breed ec.parsimony ec.coevolve ec.rule ec.rule.breed ec.multiobjective.spea2 ec.eval ec.spatial ec.display ec.display.portrayal

docs: doc

dist: clean chart doc 
	find . -name "*.stat" -exec rm rf {} \; -print
	echo --------------------------
	echo Expect some errors here...
	echo --------------------------
	find . -name "CVS" -exec rm -rf {} \; -print | cat

javac:
	javac ${FLAGS} ${DIRS}

jikes:
	jikes ${JIKESFLAGS} ${FLAGS} ${DIRS}

indent: 
	touch ${HOME}/.emacs
	find . -name "*.java" -print -exec emacs --batch --load ~/.emacs --eval='(progn (find-file "{}") (mark-whole-buffer) (setq indent-tabs-mode nil) (untabify (point-min) (point-max)) (indent-region (point-min) (point-max) nil) (save-buffer))' \;


# Print a help message
help: 
	@ echo ECJ Makefile options
	@ echo 
	@ echo "make          Builds the ECJ code using the default compiler"
	@ echo "make all	(Same thing)"
	@ echo "make javac    Builds the ECJ code using javac for sure"
	@ echo "make jikes    Builds the ECJ code using jikes"
	@ echo "make docs     Builds the class documentation, found in docs/classsdocs"
	@ echo "make doc	(Same thing)"
	@ echo "make clean    Cleans out all classfiles, checkpoints, and various gunk"
	@ echo "make dist     Does a make clean, make docs, and make, then deletes CVS dirs"
	@ echo "make help     Brings up this message!"
	@ echo "make indent   Uses emacs to re-indent ECJ java files as you'd prefer"
	@ echo "make chart    Compiles the charting facility (requires JFreeChart and iText,"
	@ echo "                see www.jfree.org/jfreechart/ and www.lowagie.com/iText/"
	@ echo
	@ echo By default ECJ uses IBM\'s jikes compiler by default.  You can always
	@ echo use javac as the default: just change the JAVAC variable in the Makefile.
	@ echo If you\'d like to try jikes but don\'t have it, you can download it at
	@ echo "	       http://www.research.ibm.com/jikes/"
	@ echo

