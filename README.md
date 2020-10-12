[![Build Status](https://travis-ci.com/starlibs/AILibs.svg?branch=dev)](https://travis-ci.com/starlibs/AILibs)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=starlibs.ailibs&metric=alert_status)](https://sonarcloud.io/dashboard/index/starlibs.ailibs)
[![SonarCloud Coverage](https://sonarcloud.io/api/project_badges/measure?project=starlibs.ailibs&metric=coverage)](https://sonarcloud.io/component_measures?id=starlibs.ailibs&metric=coverage&view=list)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/ai.libs/jaicore-basic/badge.svg)](https://search.maven.org/#search%7Cgav%7C1%7Cg%3A"ai.libs)
[![Javadocs](https://javadoc.io/badge/ai.libs/jaicore-basic.svg)](https://javadoc.io/doc/ai.libs/jaicore-basic)


# AILibs
AILibs is a modular collection of Java libraries related to automated decision making. It's highlight functionalities are:
* Graph Search ([jaicore-search](https://starlibs.github.io/AILibs/projects/jaicore-search/)):  (AStar, BestFirst, Branch & Bound, DFS, MCTS, and more)
* Logic (`jaicore-logic`): Represent and reason about propositional and simple first order logic formulas
* Planning (`jaicore-planning`): State-space planning (STRIPS, PDDL), and hierarchical planning (HTN, ITN, PTN)
* Reproducible Experiments ([jaicore-experiments](https://starlibs.github.io/AILibs/projects/jaicore-experiments/)): Design and efficiently conduct experiments in a highly parallelized manner.
* Automated Software Configuration ([HASCO](https://starlibs.github.io/AILibs/projects/hasco/)): Hierarchical configuration of software systems.
* Automated Machine Learning ([ML-Plan](https://starlibs.github.io/AILibs/projects/mlplan/)): Automatically find optimal machine learning pipelines in WEKA or sklearn

All algorithms in AILibs are steppable, and their behavior can be analyzed via the algorithm inspector: `jaicore-algorithminspector`. For example, graph search algorithms send events that allow a graph visualization in the algorithm inspector.

[Find out more about AILibs and how to use it or how to contribute.](https://starlibs.github.io/AILibs/)

## Using AILibs in your project
You can resolve each of our projects via a Maven dependency (using Maven central as repository).
For example, to bind in our machine learning library `jaicore-ml`, you can do the following:
### Maven
```
<dependency>
  <groupId>ai.libs</groupId>
  <artifactId>jaicore-ml</artifactId>
  <version>0.2.4</version>
</dependency>
```

### Gradle 
```gradle
dependencies {
    implementation 'ai.libs:jaicore-ml:0.2.4'
}
```
For more details, refer to the [documentation](https://starlibs.github.io/AILibs/) of the respective module.

## Setting up your IDE to work with AILibs
### Eclipse
Navigate to the folder where you cloned this repository and run
```
  ./gradlew eclipse
```
This automatically creates the eclipse project files and configures the dependencies among the projects.
Then open Eclipse and go to the import menu, e.g., in the package manager. Choose to import *Existing Projects into Workspace*, select the folder where you cloned the repository, and make sure to check the *Search for nested projects* option.


## AILibs JavaDoc API

### JAICore

* [JAICore:jaicore-basic](https://javadoc.io/doc/ai.libs/jaicore-basic/)
* [JAICore:jaicore-ea](https://javadoc.io/doc/ai.libs/jaicore-ea/)
* [JAICore:jaicore-experiments](https://javadoc.io/doc/ai.libs/jaicore-experiments/)
* [JAICore:jaicore-graphvisualizer](https://javadoc.io/doc/ai.libs/jaicore-graphvisualizer/)
* [JAICore:jaicore-logic](https://javadoc.io/doc/ai.libs/jaicore-logic/)
* [JAICore:jaicore-math](https://javadoc.io/doc/ai.libs/jaicore-math/)
* [JAICore:jaicore-ml](https://javadoc.io/doc/ai.libs/jaicore-ml/)
* [JAICore:jaicore-planning](https://javadoc.io/doc/ai.libs/jaicore-planning/)
* [JAICore:jaicore-processes](https://javadoc.io/doc/ai.libs/jaicore-processes/)
* [JAICore:jaicore-search](https://javadoc.io/doc/ai.libs/jaicore-search/)

### Software Configuration

* [HASCO](https://javadoc.io/doc/ai.libs/hasco/)
* [ML-Plan](https://javadoc.io/doc/ai.libs/mlplan/)
