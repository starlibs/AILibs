---
layout: project
title: jaicore-search
navigation_mode: anchor
navigation:
    - { id: "overview", link: "overview", title: "Overview" }
    - { id: "installation", link: "installation", title: "Installation" }
    - { id: "usage", link: "usage", title: "Usage" }
    - { id: "mcts", link: "monte-carlo-tree-search-mcts", title: "MCTS" }
    - { id: "javadoc", link: "javadoc", title: "JavaDoc" }
    - { id: "contribute", link: "contribute", title: "Contribute" }
navigation_active: overview
---

# jaicore-search
**jaicore-search** is a library for heuristic search.

## Overview
With the algorithms contained in jaicore-search, you can search tree-structured graphs with
* Depth First Search
* Best First Search (including A* and variants such as AWA*)
* MCTS
* Limited Discrepancy Search
* Random Search
* R*

## Installation
You can bind in jaicore-search via a Maven dependency (using Maven central as repository).
### Maven
```
<dependency>
  <groupId>ai.libs</groupId>
  <artifactId>{{ page.title }}</artifactId>
  <version>{{ page.version }}</version>
</dependency>
```

### Gradle 
```gradle
dependencies {
    implementation 'ai.libs:{{ page.title }}:{{ page.version }}'
}
```

## Usage
### At a Glance
For every search algorithm, jaicore-search provides a distinct class that implements the `IAlgorithmInterface` and, hence, can be used in a step-wise manner.
In particular, this means that you can iterate over the search algorithm object, and each iteration gives you an of the search algorithm.
In the simplest case, the algorithm steps once for every solution path, but, depending on the concrete search algorithm, there may also be steps for much more subtle events such as a node added to the model or a type switch of a node.

To apply a search algorithm to a graph, three steps are necessary:
1. create an object representing the search problem
2. create a new search algorithm instance for the problem
3. run the search algorithm

A minimal code example in jaicore-search for the 8-queens problems looks like this:
```java
GraphSearchInput<QueenNode, String> input = new NQueensToGraphSearchReducer().encodeProblem(new NQueensProblem(8));
RandomSearch<QueenNode, String> rs = new RandomSearch<>(input);
SearchGraphPath<QueenNode, String> solution = rs.call();
System.out.println(solution);
```
Note that search problems and algorithms are always generic in (at least) the labels associated with nodes (here `QueenNode`) and labels associated with edges (here `String`).

In the following, these steps are described in some more detail, and we describe alternative problem formulations and algorithms.

### In-Depth
#### Defining a Search Problem
Every search algorithm receives an object of the type `GraphSearchInput` (or one of its sub-types) as input.
Such an input provides a `GraphGenerator` and, depending on the concrete sub-class, potentially other objects such as path evaluators, heuristics, etc.
jaicore-search already comes with a number of `GraphGenerator` classes for standard AI search problems, which you can have a look at to understand how these work.

For example, to create a search problem for the 8-queens problem, you can do the following:
```java
GraphSearchInput<QueenNode, String> input1 = new NQueensToGraphSearchReducer().encodeProblem(new NQueensProblem(8));
```

Some algorithms can already work with this kind of problem, e.g. `RandomSearch`.
Some algorithms, however, need additional input.
For example, BestFirstSearch requires a function that can evaluate arbitrary paths starting from the root (typically called *f*).
To this end, a more specific input object can be created for which we can re-use the above search problem definition object:
```java
GraphSearchWithSubpathEvaluationsInput<QueenNode, String, Double> input2 = new GraphSearchWithSubpathEvaluationsInput<>(input1, p -> 0.0);
```
Here, the evaluator simply assigns a constant 0.0 to each path.
You may also observe that this input problem has an additional generic parameter (`Double`), which is the class for the node evaluations.
Often, these are `Double`, but you can also you more complex evaluations like vectors etc. as long as they implement the `Comparable` interface.

#### Initializing and Configuring the Search Algorithm
Every search algorithm can be instantiated with a problem instance that corresponds to its input type.
```java
RandomSearch<QueenNode, String> rs = new RandomSearch<>(input1);
StandardBestFirst<QueenNode, String, Double> bf = new StandardBestFirst<>(input2);
```
Since every search algorithm implements the `IAlgorithm` interface, a timeout, and conditions on CPU usage, and maximum threads can be defined.
```java
rs.setTimeout(new Timeout(10, TimeUnit.SECONDS));
rs.setNumCPUs(4);
rs.setMaxNumThreads(8);
```

#### Running a Search Algorithm
You can simply invoke the `call` method to obtain any solution.
```java
SearchGraphPath<QueenNode, String> solution = rs.call();
```

For algorithms that evaluate paths, you get a more specific object, an `EvaluatedSearchGraphPath`, which you can ask for the score of the solution:
```java
EvaluatedSearchGraphPath<QueenNode, String, Double> solution = bf.call();
Double score = solution.getScore();
```

Alternatively, you can iterate over *all solutions* using
```java
while (true) {
  SearchGraphPath<QueenNode, String> solution = rs.nextSolutionCandidate();
}
```
Note that this throws a `NoSuchElementException` if no more solutions exist.

For iterating over *all events*, use:
```java
for (IAlgorithmEvent e : bf) {
  if (e instanceof EvaluatedSearchSolutionCandidateFoundEvent) {
    EvaluatedSearchGraphPath<QueenNode, String, Double> solution = ((EvaluatedSearchSolutionCandidateFoundEvent<QueenNode, String, Double>)e).getSolutionCandidate();
    double score = solution.getScore();
    System.out.println(score);
  }
}
```
In this example, we only consider events that indicate the encounter of a solution, but there are many more events that you can react to.
A list of supported events per algorithm is given in the documentation of the respective algorithm.

You may note that the library adopts Java Generics in a very exhaustive way, and that class names appear sometimes quite complicated (long).
In AILibs, we are trading off readibility, reliability, reusability, and API comfort.
The strongly generic architecture gives AILibs an enormous potential of reusability of code.
API comfort in itself is a goal that needs to trade-off between short syntax and semantic safety.
With less generics, the code that uses our API would be shorter and perhaps a bit easier to read, but generics have the advantage of giving type guarantees at compile time, and relief you from the need of casting.
Our general approach is to try to hide as much of the generics as possible in internal structures, such that API users benefit from generics as much as possible while suffering as little as possible from the overhead that comes along with it.

### List of Search Algorithms

| Algorithm        | Input           | Output  |
| ------------- |-------------|-----|-----|
| DepthFirstSearch | GraphSearchInput      |   SearchGraphPath |
| RandomSearch |  GraphSearchInput | SearchGraphPath |
| MCTSPathSearch |   GraphSearchWithPathEvaluationsInput | EvaluatedSearchGraphPath |
| UCTPathSearch |   GraphSearchWithPathEvaluationsInput | EvaluatedSearchGraphPath |
| StandardBestFirst |  GraphSearchWithSubpathEvaluationsInput | EvaluatedSearchGraphPath |
| AStar | GraphSearchWithNumberBasedAdditivePathEvaluation | EvaluatedSearchGraphPath |
| RStar | GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic | EvaluatedSearchGraphPath |
| AwaStarSearch | GraphSearchWithSubpathEvaluationsInput | EvaluatedSearchGraphPath |
| BestFirstLimitedDiscrepancySearch |  GraphSearchWithNodeRecommenderInput | EvaluatedSearchGraphPath |

## Monte-Carlo Tree Search (MCTS)
MCTS is a tree search algorithm that was originally designed for reinforcement learning.
In AILibs it can be used for both learning a *policy* in an MDP or for finding a best path in a tree.
The two associated classes are `MCTS` and `MCTSPathSearch`.
The latter class reduces the given graph search poblem to a canonical MDP and then uses ordinary MCTS, so the two are fully compatible.

It is recommended to create MCTS algorithms with a respective factory.
AILibs comes with quite a collection of tree policies, each for which has a factory that allows to create an instance of MCTS with it.
Check out for:
* BRUEFactory (for MCTS with the BRUE tree policy)
* DNGFactory (for MCTS with the Dirichlet Normal Gamma (DNG) tree policy)
* PlackettLuceMCTSFactory (for MCTS with the Plackett-Luce tree policy)
* SPUCTFactory (for MCTS with the single player MCTS tree policy)
* TAGMCTSFactory (for MCTS with the Threshold Ascent for Graphs (TAG) tree policy)
* UCTFactory (for MCTS with the UCB1 tree policy)
* UUCTFactory (for MCTS with the UUCB tree policy)

All factories allow to optionally configure the default policy, which is by default the uniform random tree policy.

Here is a code example to solve the n-queens problem with UCT:
```java
/* create search problem */
GraphSearchInput<QueenNode, String> input1 = new NQueensToGraphSearchReducer().encodeProblem(new NQueensProblem(8));
GraphSearchWithPathEvaluationsInput<QueenNode, String, Double> input2 = new GraphSearchWithPathEvaluationsInput<>(input1, p -> 0.0);

/* set inner factory (for the MCTS algorithm itself) */
UCTFactory<QueenNode, String> innerFactory = new UCTFactory<>();
innerFactory.withMaxIterations(100);

/* configure outer factory (for the path search version of MCTS) */
MCTSPathSearchFactory<QueenNode, String> outerFactory = new MCTSPathSearchFactory<>();
outerFactory.withMCTSFactory(innerFactory);
outerFactory.withProblem(input2);

/* create and run algorithm */
MCTSPathSearch<?, QueenNode, String> mcts = outerFactory.getAlgorithm();
IEvaluatedPath<QueenNode, String, Double> solution = mcts.call();
System.out.println(solution.getHead());
System.out.println(solution.getScore());
```

## JavaDoc
JavaDoc is available [here](https://javadoc.io/doc/ai.libs/jaicore-search/).

## Contribute
jaicore-search is currently developed in the [JAICore/jaicore-search folder of AILibs on github.com](https://github.com/fmohr/AILibs/tree/master/JAICore/jaicore-search).

