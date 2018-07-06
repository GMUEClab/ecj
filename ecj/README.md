# Getting Started with ECJ

Before you do anything else, please read the file "[LICENSE](LICENSE)",
located in this directory. It contains the software license
you are bound to if you wish to use this software.

Additionally, please see the following important files:

  * [docs/index.html](https://cs.gmu.edu/~eclab/projects/ecj/docs/)         
	ECJ documentation.  Start here.
  * [docs/manual/manual.pdf](https://cs.gmu.edu/~eclab/projects/ecj/docs/manual/manual.pdf)	
	The ECJ Owner's Manual.  A large reference work describing ECJ in 
	detail.  Go through the tutorials before bothering with this one. 
  * [LICENSE](LICENSE)			
	ECJ's license
  * [CHANGES](CHANGES)			
	Change log.  Very important!
  * [pom.xml](pom.xml)			
	The Project Object Model, used by [Maven](http://maven.apache.org) 
	to compile ECJ against its dependencies.
  * [Makefile](Makefile)		
	The legacy Makefile for building on UNIX systems (you may use this 
	as an alternative to Maven, if you prefer).

## Building ECJ with Maven

The easiest way to build ECJ is to run the Maven `package` target:

```
mvn clean package
```

This will build ECJ and run its test suite, after automatically installing 
ECJ's dependencies into your local Maven repository, downloading packages 
from Maven's central repository as needed.

You will now find a compiled jar at `target/ecj-xx.jar` (where `xx` is the 
current version number).  Various runtime dependency packages will also be 
placed in the `target/dependency` directory.

Take ECJ for a test drive by running one if its example apps:

```
java -jar target/ecj-xx.jar -from app/tutorial3/tutorial3.params
```


## Building ECJ with the Makefile

If you prefer, a variant of ECJ's old Makefile is still available as an 
alternative to Maven.  The downside of the Makefile is that you are 
responsible for manually installing ECJ's dependencies and adding them 
to your `CLASSPATH` environment variable.

ECJ has two modules which rely on external libraies
which you can download as JAR files from the ECJ website
(here:  http://cs.gmu.edu/~eclab/projects/ecj/libraries.zip ).
None of the libraries are required, but if you don't install
them you need to tweak ECJ a bit in order to get it to compile:

- The GUI system relies in part on the JFreeChart and iText
  libraries.  If you do not install these, you cannot compile
  the GUI at all.  You can completely delete the GUI code if
  you like (it's entirely contained within ec/display/ ) or
  if you're using Makefile, just do "make" rather than
  "make gui".

- The distributed evaluation and island model code relies on
  compressed socket options for more efficiency, and Java's
  standard libraries are broken, so they require the JZlib
  library.  If you do not install JZlib library, the code
  will still compile but you will need to turn compression
  *off* in order to run distributed evaluation or island
  models.

TO COMPILE: under UNIX, if you have JFreeChart and iText installed to your 
CLASSPATH, just type

```
  make gui
```

Otherwise,

```
  make
```

This will build ECJ into the directory `target/classes`, which you might as 
well set up to be in your CLASSPATH.

You can build a JAR file out of ECJ, if you like, with

```
  make jar
```

Note that the source code is in TWO locations, due to the need to deal with 
Maven.  The Java source code is located in `src/main/java` and the parameter 
files, images, and other data files are in `src/main/resources`.  When you 
run the Makefile, it will compile the java files, then merge them with the 
various resources and put the whole thing into `target/classes`.


## Running ECJ

Take ECJ for a test drive by running one if its example apps.  If you built
the jar file, and you were in the ecj directory, you could say:

```
java -jar target/ecj-xx.jar -from app/tutorial3/tutorial3.params
```

... where xx is the ECJ version number

Or if you built ECJ from the Makefile, you could do this.  First
add the `target/classes/` directory to your `CLASSPATH`.  Then you could
do:

```
java ec.Evolve -file target/classes/ec/app/tutorial3/tutorial3.params
```

## Where to Go Next

To continue familiarizing yourself with ECJ's features, and to learn how to use
it to write your own problems and algorithms, take a look at
  * the [tutorials](docs/tutorials) and
  * the extensive [ECJ Manual](https://cs.gmu.edu/~eclab/projects/ecj/docs/manual/manual.pdf).

If you're really stuck, try the ECJ-INTEREST mailing list.  
(see "Mailing Lists" at http://cs.gmu.edu/~eclab/projects/ecj/ )

ECJ:	http://cs.gmu.edu/~eclab/projects/ecj/

ECLab:	http://cs.gmu.edu/~eclab/

Happy Evolving!

