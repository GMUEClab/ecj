#
# Makefile created by Jeff Bassett, with some
# tweaks by Sean Luke
#
# To compile everything but GUI:	make 
# To delete class files:		make clean
# To build the documentation:		make docs
# To auto-indent using Emacs:		make indent
# To build everything + GUI:		make gui 
#	(requires JFreeChart: www.jfree.org/jfreechart/)
#	(requires iText: www.lowagie.com/iText/)
# [also, used here at GMU, you can ignore it...]
# Prepare for distribution:		make dist
#

JAVAC = javac ${JAVACFLAGS}
#JAVAC = jikes ${JIKESFLAGS}

JAVACFLAGS = -target 1.3 -source 1.3 ${FLAGS}
JIKESFLAGS = -target 1.3 +Pno-shadow ${FLAGS}
FLAGS = -g -nowarn

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
ec/de/*.java \
ec/es/*.java \
ec/eval/*.java \
ec/exchange/*.java \
ec/gp/*.java \
ec/gp/breed/*.java \
ec/gp/build/*.java \
ec/gp/koza/*.java \
ec/multiobjective/*.java \
ec/pso/*.java \
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
ec/multiobjective/spea2/*.java

all: base

base:
	@ echo This builds the code except for gui
	@ echo For other Makefile options, type:  make help
	@ echo
	${JAVAC} ${DIRS}

gui:
	@ echo This builds the base code and the gui code
	@ echo -- requires JFreeChart: www.jfree.org/jfreechart/
	@ echo -- requires iText: www.lowagie.com/iText/
	@ echo
	${JAVAC} ${DIRS} ec/display/chart/*.java ec/app/gui/*.java ec/display/*.java ec/display/portrayal/*.java

clean:
	find . -name "*.class" -exec rm -f {} \;
	find . -name "*.stat" -exec rm -f {} \;
	find . -name ".DS_Store" -exec rm -rf {} \;
	find . -name "*.java*~" -exec rm -rf {} \;
	rm -rf docs/classdocs/*


dist: clean gui indent doc 
	find . -name "*.stat" -exec rm rf {} \; -print
	echo --------------------------
	echo Expect some errors here...
	echo --------------------------
	find . -name "CVS" -exec rm -rf {} \; -print | cat

indent: 
	@ echo This uses emacs to indent all of the code.  To indent with
	@ echo "ECJ's default indent style, create a .emacs file in your home"
	@ echo "directory, with the line:    (setq c-default-style \"whitesmith\")"
	@ echo and run make indent.  To indent with BSD/Allman style, use 
	@ echo "the line:    (setq c-default-style \"bsd\")"
	@ echo
	touch ${HOME}/.emacs
	find . -name "*.java" -print -exec emacs --batch --load ~/.emacs --eval='(progn (find-file "{}") (mark-whole-buffer) (setq indent-tabs-mode nil) (untabify (point-min) (point-max)) (indent-region (point-min) (point-max) nil) (save-buffer))' \;


# Print a help message
help: 
	@ echo ECJ Makefile options
	@ echo 
	@ echo "make          Builds the ECJ code using the default compiler"
	@ echo "make all	(Same thing)"
	@ echo "make docs     Builds the class documentation, found in docs/classsdocs"
	@ echo "make doc	(Same thing)"
	@ echo "make clean    Cleans out all classfiles, checkpoints, and various gunk"
	@ echo "make dist     Does a make clean, make docs, and make, then deletes CVS dirs"
	@ echo "make help     Brings up this message!"
	@ echo "make indent   Uses emacs to re-indent ECJ java files as you'd prefer"
	@ echo "make gui      Compiles the GUI and charting (requires JFreeChart and iText,"
	@ echo "                see www.jfree.org/jfreechart/ and www.lowagie.com/iText/"

