
# White Box Fuzzer - Group 15

## Introduction
Many programs take in user inputs. To maintain code quality and security, the programmer might want to know whether or not a program could possibly fail given certain inputs. There are two main types of fuzzers. A blackbox fuzzer, like the name suggests, treats the source file as a blackbox. Without looking at the contents of the program, the inputs are simply randomly generated. Whitebox fuzzing, on the other hand, looks at the source code, and the outputs produced by the code, then tries to intelligently formulate inputs. 
Smart generational models for generating testing inputs is still an open area of research. The consideration of the trade off between the time spent testing the program versus generating smart inputs for program is highly context driven. <br/>

## A quick heads up
YOU ARE AT THE WORKING CODE REPOSITORY FOR THIS PROJECT! <https://agile.bu.edu/bitbucket/scm/~jbelmont/whiteboxfuzzergroup15_backup.git>

Group project directory at: <https://agile.bu.edu/bitbucket/projects/EC504PROJ/repos/group15/browse>

We apologize again for the inconvenience of redirecting you to this repository for working code, but we talked with Prachi Shukla and this was the best we could come up with at the last minute. 

## Our Team
Jack Belmont:
* Class Year: Junior - Undergraduate
* Email: jbelmont@bu.edu

Varun Lalwani:
* Class Year: Junior - Undergraduate
* Email: varunl@bu.edu

Thuc Nguyen
* Class Year: Junior - Undergraduate
* Email: thuchngu@bu.edu

Paschal Igusti
* Class Year: Junior - Undergraduate
* Email: paschali@bu.edu

Phillip Teng 
* Class Year: Senior - Undergraduate
* Email: phillipt@bu.edu



## Implemenation
We designed and implemented a fusion between the whitebox and blackbox fuzzer and catered to test simple Java programs. Our implementation starts by parsing the code, extracting information that we might find usefull, and then repeatedly fuzzes (alters) the information that we parsed to try and cause an error in the code. Our program takes in a single Java source code file that takes a single command-line 0-3 character ASCII argument. The goal in implementing our fuzzer is to quickly identify an argument that causes the program to crash. Our implementation fulfills the following requirements: 
<br/>

* Is able to find a three character argument that causes a simple Java program (with such a vulnerability) to crash in under 5 minutes.
* A GUI was implemented to replace the command line interface usage.
* A graphical-user interface for all the functionality in our code.
* Can potentially produce an argument that would crash the program consisting of up to 5 ASCII characters in under thirty minutes. However this characteristic was mostly removed to stick with 3 character arguments due to its buggy nature.

Our implementation relies on the JavaParser Library. Our team did not write the JavaParser library. <br />
Information and source code can be found at: <br />
JavaParser Website: https://javaparser.org/  <br />
JavaParser GitHub: https://github.com/javaparser/javaparser

## Design
Our code base extends and builds upon the JavaParser library, which is used to formulate an abstract syntax tree. 
Our program functions in two primary phases. 
The first phase is to parse the source file and extract any useful information.
Information that we deemed to be the most useful is any strings, or numbers in the file, along with any conditional statements in control flow logic. (if, while, case, and for statements)
After we generate this information, we apply some transformations to it to expand upon it for more accurate inputs.
The second phase of execution is to actually compile the source code and try running it with fuzzed inputs. 
We start this process by first testing the direct information that we parsed out of the file. 
After we have exhausted these direct results of the parser, we start fuzzing that information gradually, until we either find an error or we run out of time.
When we fuzz the input we start by fuzzing it by a small amount and the longer the process runs the more drastically it will begin to fuzz the input. 

The code base is split into three different classes. The Fuzzer class manages the execution of the program and calls the InputGenerator class to generate the inputs to test. The InputGenerator class scours the source code by calling on functions from the FileParser class to generate possible inputs that might crash the program. The FileParser class parses the program for string and integer literals in an effort to guess inputs that could affect the program's control flow.

###Primary Data Structure: AST
The primary data structure used for our code is an Abstract Syntax Tree, or AST for short.
Use of an AST allows for efficient parsing and information gathering of the source code. 
Since our execution time is limited to 5 minutes, efficiency is of the utmost importance.
Since we need to parse many different things from the code (variable names, numbers, strings, conditional statements, etc) we need a way to do this that is faster than repeatedly parsing the code character by character. 
An AST solves this issue. Much resembling the trees that we have seen in traditional data structures, an AST consists of nodes where there are parents and children.
Each node gets broken up into the things that make up that node, allowing us to easily find certain information. 
On a conceptual level, if we were to look at a file called myFile.java, the root node would contain everything in the file. 
The children would be the classes contained in the file. Each class would then have children for all of the methods that it contains. 
Each method would then have children for each variable declaration or method call within it. This process goes down until we are left with the most basic things in the code such as the individual "words" in the source code.
We can then create classes called visitors that traverse through the AST and stop at nodes that pertain to certain types of statements. 
For example we can write a visitor that stops at every single If-statement, and extracts the conditional expression that alters the control flow at that statement. 
If you would like to understand more about AST's, this wikipedia article sums them up well: https://en.wikipedia.org/wiki/Abstract_syntax_tree 

##OurClasses
### FileParser.java && InputGenerator.java
The FileParser begins by constructing an abstract syntax tree through the use of the JavaParser Library.
We are then able to use visitors that go through the tree to extract fields such as expressions and literals. <br/>
The FileParser class defines many functions to parse out specific information that might be usefull.

One of our hypothesis for strings was that the string that crashes the program will be present in the source file. 
Based on this, we search all the strings and put them into a HashSet to ensure we dont collect duplicate strings. 
We can then test the source code's program by using these strings (and fuzzed versions of these strings) as inputs.

Similarly, we noticed that collecting all of the numerical values in the source code can tell us a lot about the control flow and possible errors. 
To display this point lets consider the following code snippet: 

int i = Integer.parseInt(args[0]);  <br />
int div;  <br />
if (i < 10 && i > 5)  <br />
{
do something
}

How can we generate code to get in here? Well at a simple level without solving the expression (which we started to figure out how to do but did not complete) we can know emediatly that the statement will be entered for any number 6-9.
Therefore we can collect all of the numerical values in the code and get a sorted array list that looks like this:  <br />
<5, 10>  <br />
We can then use this to help us generate some inputs. We can randomly generate integers between each intermediate int and also test them as inputs. This might look like this:  <br />
<2, 5, 7, 10, 200>  <br />
If we then turn these numbers into strings and put them into the code as input we will enter the conditional statement! 

Lets see another example

int i= Integer.paraseInt(args[0]);  <br />
int div;  <br />
div = 10 / (i + 5);

We can intuitively see that the code will crash for input of "-5" via a divide by zero exception.
If we do the same thing that we did in the previous example we end up once again with the following:  <br />
<2, 5, 7, 10, 200> //or slightly different via randomly generating the intermediate values.

But we will notice that none of these will cause the program to crash, since the array does not contain -7.
So what can we do? Well, if you notice, the array contains +7, so we can determine that simply testing all the negative values for the numbers will also be helpfull.
When we do this we will get "-7" as an input and the program will in fact crash. Cool!

Furthermore, the InputGenerator also has a constraint solver to solve string concatonations and .compareTo()'s. 
This allows us to get inside conditional branches like the following:

if (args[o]+"hi".compareTo("90hi") == 0) { do something }


After the input generator does all this, it then fuzzes the information we received and tries over and over again. 

### Fuzzer.java
This class performs the control flow of the fuzzer itself. Here we error check to make sure the user passed a valid file,
call the InputGenerator to parse the file, compile the source code, generate an input, test the input, and loop to try again. 

### DependantArg.java
This class contains some things that we were working on but could not quite finish by the deadline.
Despite not finishing the features that this would have been used to enhance, we decided to leave it in the final project, as it was not buggy and shows good intent for the future.
This class gets information about variables that were dependent on one another, and collects information about them including the type of the variable, the name of the variable, and the statement that instantiated the variable.
This would have had a huge influence on our code if we had more time, since it would have allowed us to trace through the assignments of related arguments to greatly improve our condition solver. 

### Main.java
The main class is primarily the GUI creating, and also creates and calls the Fuzzer class, which then calls the rest of the classes.

## Getting Started

### Prerequisites

The following libraries/dependencies/frameworks would have to be installed to properly run our program:
* Javaparser
* Maven

### Installing
A step by step guide can be found in INSTALL.txt in the same folder you found this file.

Once you have followed the setup procedure correctly you should be able to execute and see the following screen:

Here is an example of the output after running one of our test files:
<p align="center"> 
<img src="https://i.imgur.com/MrQ09j3.png">
</p>

Here is the output.

<p align="center"> 
<img src="https://i.imgur.com/DoHmRk7.png">
</p>

## Built With
* [JavaParser](https://javaparser.org/) - Third Party Parsing Library
* [Maven](https://maven.apache.org/) - Dependency Management


## Authors & Work breakdown

* **Paschal Igusti** - *Backend Engineer*
- Implemented smart Integer and String input generation
- Integrated the solution for Integers and Strings with Jack
- Created Visitor framework for indexing into Abstract Syntax Tree
<br/>

* **Varun Lalwani** - *Backend Engineer*
- Implemented String comparison and concatenation to work on crash cases with any string comparisons
- Created larger testbed of edge cases for strings
- Created windows for GUI output
- Implemented solveString function
<br/>

* **Jack Belmont** - *Backend Engineer*
- Lead the project development and task delegation 
- Refactored code to utilize JavaParser and Abstract Syntax Tree
- Developed basic working implementation
- Developed backbone for InputGenerator for various ArgTypes, Conditionals, Expressions, and Literals
<br/>

* **Phillip Teng** - *Backend Engineer*
- Attempted brute force solution for 3 ASCII characters, replaced with simpler random generator
- Developed design for smart double and integer comparison generation
- Performance optimizations for multithreading and decreasing memory constraints by changing maps to sets.
<br/>

* **Thuc Nguyen** - *Frontend Engineer*
- Integrated Fuzzer class to be displayed on a GUI including buttons and callback functions
- Added Maven Integration to the project details
- Created initial test cases and test file
<br/>

Digital Signatures (all group members agree with the above information as factual and accurate): <br/>
Paschal Igusti, Varun Lalwani, Jack Belmont, Phillip Teng, Thuc Nguyen.

## What we didn't Implement
The following are the features that we failed to implement. The primary reason for this is because we ran out of time. 
* Extending the input arguments to 5 ascii characters
* Allow user input of regular expressions
* Static Call Graph
* Infinite Loop Detection
* Identify range of possible outputs
* Enable rewriting code to alter the control flow. 
* Performance comparison. 

### If we had more time:
* The first thing we would have extended if we had more time is allowing up to 5 character inputs. 
In reality we have almost completed this portion, and we know for certain that we can already find some inputs that are even longer that 5 ascii that might cause errors. 
However, we largely did not include this feature, as it was still kind of buggy and was largely untested.

* Allowing user input of Regular Expressions. If we had implemented this we would have added a dialog box in the GUI that can take a Reg Expression string.
Since reg expressions provide a structure to follow we would have implemented a function to generate a string satisfying the reg expression and would have passed these strings as arguments to the function along with the other tests that we performed. 

* Enable range of possible outputs. One of the cool things that the AST lets you do is alter a node to contain something new.
If we would want to change a conditional statement to be easier to enter, we could simply rewrite the conditional block of a if, while, switch, or for statment to depend directly on the args[0] input, thus making it much easier to execut that branch of code. Just like we created the AST by parsing the source code, we can transform our altered AST back into source code. JavaParser even provides a function for this. 

* Identifying range of possible outputs. We could have stored the outputs as a hashset and then determined the range of outputs. We already have commented out functionality for reading the outputs. All we would have needed to do is store them and perform some light analysis on them.

## Sources
JavaParser Book -> https://leanpub.com/javaparservisited

Statically Scanning Java Code: Finding Security Vulnerabilities -> https://ieeexplore.ieee.org/stamp/stamp.jsp?tp=&arnumber=6156713&tag=1

Model Based White Box Fuzzing For Program Binaries -> https://ieeexplore.ieee.org/document/7582789

JFuzz: A Concolic WhiteBox Fuzzer for Java -> https://ntrs.nasa.gov/archive/nasa/casi.ntrs.nasa.gov/20100024457.pdf

Finding Software Vulnerabilities by Smart Fuzzing -> https://ieeexplore.ieee.org/abstract/document/5770635

Looper: Lightweight Detection of Infinite Loops at Runtime -> https://www.burn.im/pubs/BurnimJalbertStergiouSen-ASE09.pdf

Whitebox Fuzzing with Java Path Finder and introduction to jFuzz: A Concolic Whitebox Fuzzer for Java -> https://www.researchgate.net/publication/286048816_Whitebox_Fuzzing_with_Java_Path_Finder_and_introduction_to_jFuzz_A_Concolic_Whitebox_Fuzzer_for_Java
