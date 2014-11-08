*******************************************************************
Author: Ye Xiaomeng

Great thanks to Dr. Denise Byrnes, Dr. Kazuto Tominaga, Dr. Matthew Moynihan, and Dr. Sean Luke.
*******************************************************************

What is the lambda package?

The lambda package is a package under the gp package. The intention is: to evolve lambda calculus functions using Genetic Programming (GP).

Along with this README file is my paper, Evolving Lambda-Calculus Functions Using Genetic Programming. Chapter 1 introduces GP. If you are using GP of ECJ, your knowledge of GP is probably enough to use the lambda package and can skip this chapter. 

You will need to understand what is lambda calculus before you do anything with the lambda package. Chapter 2 introduces lambda calculus. In short words, lambda calculus is a formal logic system that has an expressive power equivalent to Turing machine. It can express everything that's expressible and solve every problem that's solvable, theoretically. A program (or problem solver) in lambda calculus is called a lambda expression. 

*******************************************************************

Why do I create the lambda package?

The training of a pool of lambda programs is similar to the training of a statistical neuron network. However, there are some fundamental differences in their philosophies. The basic rules of a neuron network is pre-set, the training only influences the parameters, not the fundamental rules. On the other side, when we evolve lambda calculus programs, we are evolving the rules as well. We can evolve "if/else" statements, "while/for" loops, and many others. In neuron network, we tweak the relationships between neurons to gradually get closer to the ideal results. In lambda calculus, we are directly changing a program and changing any component in a program normally has a drastic impact.

Lambda programs are programs, and more interestingly, they are programs that take any lambda expression as input. This freedom allows we use a lambda program as an input for another lambda program. Or we can run one program with itself as the input! This intrinsic property is amazing and to evolve programs with such property is more amazing. According to (my understands) of the works of Dr. Douglass Hofstadter, if we ever want to create artificial intelligence, the artificial intelligence must have the ability to retrospect itself. Maybe we can evolve true AI, by evolving lambda calculus.

Just imagine, if I were able to write the GP program of ECJ in lambda calculus, (say Evolver A), then I evolved some other lambda programs and I were not satisfied with the results given by Evolver A. What can I do? I could evolve Evolver A, by inputing the expectation of Evolver A and run Evolver A with these inputs. And I can recursively do this, I could run Evolver A on Evolver A on Evolver A.... 

Given red, blue, gree, an artist can make new colors by mixing existing colors and draw all kinds of stuff. Using lambda calculus as the vehicle of GP, we have the potential to evolve all programming components, all logic, and thus everything. 

*******************************************************************

Technical details

The lambda package follows the idea of Dr. Kazuto Tominaga to encode lambda expressions in tree structure, and use GP to evolve them to solve target problems. The following is a list of technical problems with the section numbers in my paper that explains the points.

	Motivations for using Lambda-Calculus  --- 2.2
	How to encode lambda expressions into trees  --- 3.1
	A lambda tree have the same power as its corresponding lambda expression  --- 3.3
	How to evolve lambda trees  --- 3.4, essentially the same as in a normal GP process
	Pitfalls  --- 4.4.1

*******************************************************************

What is already in lambda package

Lambda calculus is a formal logic system relying on symbols and reductions. Before we can express/solve anything using lambda calculus, we need an interpretation scheme. More specifically, we need to be able to translate any valid instance in the problem domain into lambda expressions, and we need to be able to use these translated lambda expressions to carry out valid operations in the problem domain. (Just like in a human language, we need to define what each symbol means, and how to combine them into sentences to express a meaning.)

For example, if we are solving problems involving summation of natural numbers, we need to have a way to translate every natural number, and the operation summation. Assume that the translation of the number 1 is "1", and the translation of the summation operation is "+", then our translation scheme should be able to calculate the combination of expressions "1" "+" "1" and produce "2", the translation of the number 2. Remember lambda expressions are programs and the combination of two lambda expressions is basically one program executing the other program as input.

Right now, in the lambda package, only one translation/interpretation scheme is coded, namely, the Church numeral scheme, which allows us to operate on natural numbers. Two examples are offered in lambda.app.churchNumerals.problems. More about church numerals can be found in my paper, Section 2.1.5.

In the SuccessorProblem, we try to evolve a function that takes a natural number N as input and outputs N+1 (of course, both inputs and outputs are in the form of lambda expressions, converted to trees as well). To run this example: run Evolve with following parameter:
-file ecj/ec/gp/lambda/app/churchNumerals/problems/SuccessorProblem.params

In the MultiplicationByTwoProblem, we try to evolve a function that takes a natural number N as input and outputs 2N. To run this example: run Evolve with following parameter:
-file ecj/ec/gp/lambda/app/churchNumerals/problems/MultiplicationByTwoProblem.params

There are two fitness functions for problems involving church numerals. They can be found in interpreter folder:
	CNIntepreter converts a church numeral back to its corresponding natural number. If the expected output is a Church numeral, we can use this fitness function to measure the distance between the actual output and the expected output, by measuring the distance between the numbers they represent. This fitness function is NOT GOOD. Read Section 3.5 for more info.
	A better fitness function is offered by Dr. Kazuto Tominage, etc.. In short words, this fitness function measures the distance between the actual output and the expected output by measuring the differences in their tree structures. Therefore it's more of a syntactical distance instead of a semantical one.

*******************************************************************

How to evolve other stuff

To create your own evolution on ChurchNumerals, you will need to create your own Problem class, in which you define the ideal behavior, fitness function, and other parameters. You may also create a ProblemData class, which contains inputs along with the corresponding expected outputs. Most of the regression problems should be easy to solve this way.

To solve problems that is not in the realm of natural numbers, you will need to have a suitable encoding scheme. Church numeral is the only scheme I know and my knowledge of others is pitifully shallow. However, I know that there exists a scheme to represent TRUE/FALSE using lambda calculus and I am confident we can solve problems related to circuits if we can implement that scheme into our program, to translate TRUE/FALSE into lambda expressions and eventually into trees, and also backwards. 

*******************************************************************

Some last words

Lambda calculus has equivalent power to Turing machine. As long as something is logical enough to be solved in Turing machine, we can evolve it using our program as there must exist a corresponding encoding scheme. 

The lambda package is part of GP and thus the evolution is open to all the problems GP has. For example, the researcher may need to recognize the local maximums and figure out a way to avoid them. For example, in an attempt to evolve the function f(x)=x+1, I had to specifically exclude the local maximum function f(x)=x.

For the two problems I have evolved. Occassionally it falls into the pitfall of local maximum as well. If you see it goes on for more than 50 generations, stop it and run again. Both example problems have been successfully solved. 

Also notice that individuals with different syntactic structure may carry the same semantic meaning. Similar to synonym in English, there are more than one ways to express the same function in lambda calculus. Some synonyms can be converted/reduced to one another as they are essentially the same syntactically, while other synonyms carry completely different syntactical structure but function the same semantically. This may sound weird, but I successfully evolved an addition function in lambda calculus that I have never seen in any textbook about lambda calculus.

*******************************************************************

*******************************************************************

Package Structure

The lambda package contains:
knownChurchNumerals: Some known lambda expressions and church numerals.
problems: two problem classes which are solved successfully.
helloworld: a basic experiment of mine. It basically evolves the string "helloworld". I used this to learn ECJ.
interpreter: fitness function related class.
essential classes for lambda expressions and trees.

individualsForTesting: ignore please. It's mostly used to test if my software is working correctly.

*******************************************************************

Contact Me

This package is far from perfect and I still have a lot to learn. If you spot any error, have any issue, or any advice, please let me know.

yexiao1992@gmail.com