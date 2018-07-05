#
# Makefile created by Jeff Bassett, with some
# tweaks by Sean Luke
#
# To compile everything but GUI:	make 
# To delete class files:		make clean
# To build the documentation:		make doc
# To auto-indent using Emacs:		make indent
# To build everything + GUI:		make gui 
# To build everything + Push:		make push
#	(requires JFreeChart: www.jfree.org/jfreechart/)
#	(requires iText: www.lowagie.com/iText/)
# [also, used here at GMU, you can ignore it...]
# Prepare for distribution:		make dist
#

JAVAC = javac ${JAVACFLAGS}

CLASSROOT = target/classes
JAVACFLAGS = -target 1.5 -source 1.5 ${FLAGS} -d ${CLASSROOT}
FLAGS = -g -Xlint:deprecation

VERSION = 26

SRCS = \
ec/*.java \
ec/app/ant/*.java \
ec/app/ant/func/*.java \
ec/app/bbob/*.java \
ec/app/cartpole/*.java \
ec/app/ecsuite/*.java \
ec/app/edge/*.java \
ec/app/edge/func/*.java \
ec/app/gpsemantics/*.java \
ec/app/gpsemantics/func/*.java \
ec/app/hiff/*.java \
ec/app/highdimension/*.java \
ec/app/klandscapes/*.java \
ec/app/klandscapes/func/*.java \
ec/app/lawnmower/*.java \
ec/app/lawnmower/func/*.java \
ec/app/lid/*.java \
ec/app/lid/func/*.java \
ec/app/majority/*.java \
ec/app/majority/func/*.java \
ec/app/mona/*.java \
ec/app/moosuite/*.java \
ec/app/multiplexer/*.java \
ec/app/multiplexer/func/*.java \
ec/app/multiplexerslow/*.java \
ec/app/multiplexerslow/func/*.java \
ec/app/nk/*.java \
ec/app/ordertree/*.java \
ec/app/ordertree/func/*.java \
ec/app/parity/*.java \
ec/app/parity/func/*.java \
ec/app/regression/*.java \
ec/app/regression/func/*.java \
ec/app/royaltree/*.java \
ec/app/royaltree/func/*.java \
ec/app/sat/*.java \
ec/app/singular/*.java \
ec/app/sum/*.java \
ec/app/tutorial1/*.java \
ec/app/tutorial2/*.java \
ec/app/tutorial3/*.java \
ec/app/tutorial4/*.java \
ec/app/coevolve1/*.java \
ec/app/coevolve2/*.java \
ec/app/twobox/*.java \
ec/app/twobox/func/*.java \
ec/app/xor/*.java \
ec/breed/*.java \
ec/coevolve/*.java \
ec/de/*.java \
ec/eda/amalgam/*.java \
ec/eda/dovs/*.java \
ec/eda/cmaes/*.java \
ec/eda/pbil/*.java \
ec/es/*.java \
ec/eval/*.java \
ec/evolve/*.java \
ec/exchange/*.java \
ec/gp/*.java \
ec/gp/breed/*.java \
ec/gp/build/*.java \
ec/gp/koza/*.java \
ec/gp/ge/*.java \
ec/gp/ge/breed/*.java \
ec/multiobjective/*.java \
ec/multiobjective/nsga2/*.java \
ec/multiobjective/nsga3/*.java \
ec/multiobjective/spea2/*.java \
ec/neat/*.java \
ec/pso/*.java \
ec/select/*.java \
ec/simple/*.java \
ec/singlestate/*.java \
ec/spatial/*.java \
ec/steadystate/*.java \
ec/util/*.java \
ec/vector/*.java \
ec/vector/breed/*.java \
ec/parsimony/*.java\
ec/rule/*.java \
ec/rule/breed/*.java \

RSRCROOT=src/main/resources/
SRCROOT=src/main/java/
DIRS=$(addprefix $(SRCROOT)/, $(SRCS))

all: base

base:
	@ echo This builds the code except for gui
	@ echo For other Makefile options, type:  make help
	@ echo
	mkdir -p ${CLASSROOT}
	${JAVAC} ${DIRS}
	tar -C ${RSRCROOT} -c ec | tar -C ${CLASSROOT} -x

gui:
	@ echo This builds the base code and the gui code
	@ echo -- requires JFreeChart: www.jfree.org/jfreechart/
	@ echo -- requires iText: www.lowagie.com/iText/
	@ echo
	mkdir -p ${CLASSROOT}
	${JAVAC} ${DIRS} ${SRCROOT}ec/display/chart/*.java ${SRCROOT}ec/app/gui/*.java ${SRCROOT}ec/display/*.java ${SRCROOT}ec/display/portrayal/*.java
	tar -C ${RSRCROOT} -c ec | tar -C ${CLASSROOT} -x

push:
	@ echo This builds the base code and the Push code
	@ echo -- requires Psh:  https://github.com/jonklein/Psh
	@ echo
	mkdir -p ${CLASSROOT}
	${JAVAC} ${DIRS} ${SRCROOT}ec/gp/push/*.java ${SRCROOT}ec/app/push/*.java
	tar -C ${RSRCROOT} -c ec | tar -C ${CLASSROOT} -x

clean:
	find . -name "*.class" -exec rm -f {} \;
	find . -name "*.stat" -exec rm -f {} \;
	find . -name ".DS_Store" -exec rm -rf {} \;
	find . -name "*.java*~" -exec rm -rf {} \;
	rm -rf ${CLASSROOT}
	rm -rf target/
	rm -rf docs/classdocs/*
	rm -rf ${SRCROOT}ec/app/moobenchmarks


dist: clean gui push doc jar
	@ echo If the version is being updated, change it here:
	@ echo "1. ec.util.Version"
	@ echo "2. manual.tex frontmatter (including copyright year)"
	@ echo "3. Makefile VERSION variable"
	find . -name "*.stat" -exec rm rf {} \; -print
	echo --------------------------
	echo Expect some errors here...
	echo --------------------------
	find . -name ".hg" -exec rm -rf {} \; -print | cat

indent: 
	@ echo This uses emacs to indent all of the code.  To indent with
	@ echo "ECJ's default indent style, create a .emacs file in your home"
	@ echo "directory, with the line:    (setq c-default-style \"whitesmith\")"
	@ echo and run make indent.  To indent with BSD/Allman style, use 
	@ echo "the line:    (setq c-default-style \"bsd\")"
	@ echo
	touch ${HOME}/.emacs
	find ${SRCROOT} -name "*.java" -print -exec emacs --batch --load ~/.emacs --eval='(progn (find-file "{}") (mark-whole-buffer) (setq indent-tabs-mode nil) (untabify (point-min) (point-max)) (indent-region (point-min) (point-max) nil) (save-buffer))' \;

doc:
	javadoc -classpath ${CLASSPATH}:${SRCROOT} -protected -Xdoclint:none -Xmaxwarns 10000 -Xmaxerrs 10000 -d docs/classdocs ec ec.breed ec.coevolve ec.de ec.display ec.display.chart ec.display.portrayal ec.eda ec.es ec.eval ec.evolve ec.exchange ec.gp ec.gp.breed ec.gp.build ec.gp.koza ec.multiobjective ec.multiobjective.spea2 ec.multiobjective.nsga2 ec.parsimony ec.pso ec.rule ec.rule.breed ec.select ec.simple ec.spatial ec.steadystate ec.util ec.vector ec.vector.breed ec.gp.ge ec.gp.push ec.neat ec.dovs

# Build a jar file.  Note this collects ALL files in both the $CLASSROOT and the RSRCROOT.
jar: all gui
	touch /tmp/manifest.add
	rm /tmp/manifest.add
	echo "Main-Class: ec.Evolve" > /tmp/manifest.add
	mkdir -p target/
	jar cvfm target/ecj-${VERSION}.jar /tmp/manifest.add -C ${CLASSROOT} . 
	#jar uvf target/ecj-${VERSION}.jar -C ${RSRCROOT} .



# Print a help message
help: 
	@ echo ECJ Makefile options
	@ echo 
	@ echo "make          Builds the ECJ code using the default compiler"
	@ echo "make all	(Same thing)"
	@ echo "make docs     Builds the class documentation, found in docs/classsdocs"
	@ echo "make doc	(Same thing)"
	@ echo "make clean    Cleans out all classfiles, checkpoints, and various gunk"
	@ echo "make dist     Does a make clean, make docs, and make, then deletes SVN dirs"
	@ echo "make help     Brings up this message!"
	@ echo "make indent   Uses emacs to re-indent ECJ java files as you'd prefer"
	@ echo "make gui      Compiles the GUI and charting (requires JFreeChart and iText,"
	@ echo "                see www.jfree.org/jfreechart/ and www.lowagie.com/iText/"
	@ echo "make push     Compiles ECJ, including the Push code (requires Psh,"
	@ echo "                see https://github.com/jonklein/Psh"

