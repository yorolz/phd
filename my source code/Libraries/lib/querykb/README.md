# querykb
A simple tool for counting the number of solutions to a conjunctive query over
a knowledge base.

## Setup

querykb has the following dependencies:

* Java 1.8+
* Maven (relatively recent version)

To create a stand-alone JAR, in the main repo directory run the command `mvn
package`. This should create `querykb-0.0.1-SNAPSHOT-jar-with-dependencies.jar`
in the `target/` directory.

## Using the Java API

The main class to interface with is `KnowedgeBase`. A `KnowledgeBase` is
created by reading facts from a `Reader`. The facts can either be
comma-separated triples of the form `subject,predicate,object` (where each
triple is separated by whitespace) or Datalog-style facts of the form
`predicate(subject,object).`. Constants must start with a lowercase letter or
number, and can contain alphanumeric characters and underscores.

A `Query` can be constructed by hand or by using `Parser.parseQuery()`. A query
should have the form

```
:- p_1(s_1,o_1), ..., p_n(s_n,o_n).
```

where each `s_i` and `o_i` is either a constant or a variable. A variable
starts with an uppercase letter or underscore, and can contain alphanumeric
characters and underscores.

The method `KnowledgeBase.query(query, blockSize, parallelLimit,
solutionLimit)` counts the number of unique solutions to the given query in the
knowledge base. The `blockSize` argument sets the maximum size of a subtask, in
tuples. Some experimenting will probably be necessary to find an optimal value.
The `parallelLimit` argument sets the maximum number of subtasks to do in
parallel. The `solutionLimit` argument sets the maximum number of solutions to
find. The query evaluator will quit after it has found more than this number of
solutions, and return the number of solutions it has found.

See [`com.github.aaronbembenek.querykb.Example`](src/main/java/com/githhub/aaronbembenek/querykb/Example.java)
for a simple example of loading a knowledge base and making a query on it.

## Using the REPL

The JAR `querykb-0.0.1-SNAPSHOT-jar-with-dependencies.jar` is also executable.
To launch a (very primitive) REPL for making queries over a knowledge base, run
the command

```
java -jar querykb-0.0.1-SNAPSHOT-jar-with-dependencies.jar [path-to-knowledge-base]
```

The REPL allows you to adjust the query evaluation settings. For example, to
set the maximum number of subtasks to do in parallel to 24, enter

```
set parallelLimit=24.
```

Queries should have the same form as given above. For example, to count the
number of dogs in the knowledge base, do

```
:- isa(X, dog).
```
