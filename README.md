[![Build Status](https://travis-ci.org/fmohr/AILibs.svg?branch=dev)](https://travis-ci.org/fmohr/AILibs)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=starlibs.ailibs&metric=alert_status)](https://sonarcloud.io/dashboard/index/starlibs.ailibs)
[![Javadocs](https://javadoc.io/badge/ai.libs/jaicore-basic.svg)](https://javadoc.io/doc/ai.libs/jaicore-basic)


# AILibs
AILibs is a collection of Java libraries related to automated decision making. It currently consists of two building blocks. It is also home of the current version of the AutoML-tool [ML-Plan](https://github.com/fmohr/AILibs/tree/master/softwareconfiguration/mlplan).

* **JAICore** (Java AI Core) is a collection of projects with basic general purpose AI algorithms mainly in the area of logic reasoning, heuristic search, and machine learning
* **softwareconfiguration** is a collection of projects related to automatically configuring software systems. Here we also maintain the code for our AutoML flagship **[ML-Plan](https://github.com/fmohr/AILibs/tree/master/softwareconfiguration/mlplan)**

## Using AILibs in your project
You can resolve snapshots of this projects via a maven-dependency.
### Gradle 
First register our departements nexus as a maven repository:
```
repositories {
    mavenCentral()
	  maven { url "https://nexus.cs.upb.de/repository/sfb901-snapshots/" }
}
```
Then, you can either import the bundeled library via:
```
dependencies {
	 compile group: "de.upb.isys", name: "AILibs", version:"0.0.1-SNAPSHOT"
}
```
Or, the different artifacts individually e.g.
```
dependencies {
	 compile group: "de.upb.isys", name: "jaicore-ml", version:"0.0.1-SNAPSHOT"
}
```

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

* [JAICore:jaicore-basic](https://javadoc.io/doc/ai.libs/jaicore-basic/0.1.0)
* [JAICore:jaicore-ea](https://javadoc.io/doc/ai.libs/jaicore-ea/0.1.0)
* [JAICore:jaicore-experiments](https://javadoc.io/doc/ai.libs/jaicore-experiments/0.1.0)
* [JAICore:jaicore-graphvisualizer](https://javadoc.io/doc/ai.libs/jaicore-graphvisualizer/0.1.0)
* [JAICore:jaicore-logic](https://javadoc.io/doc/ai.libs/jaicore-logic/0.1.0)
* [JAICore:jaicore-math](https://javadoc.io/doc/ai.libs/jaicore-math/0.1.0)
* [JAICore:jaicore-ml](https://javadoc.io/doc/ai.libs/jaicore-ml/0.1.0)
* [JAICore:jaicore-planning](https://javadoc.io/doc/ai.libs/jaicore-planning/0.1.0)
* [JAICore:jaicore-processes](https://javadoc.io/doc/ai.libs/jaicore-processes/0.1.0)
* [JAICore:jaicore-search](https://javadoc.io/doc/ai.libs/jaicore-search/0.1.0)

### Software Configuration

* [HASCO](https://javadoc.io/doc/ai.libs/hasco/0.1.0)
* [ML-Plan](https://javadoc.io/doc/ai.libs/mlplan/0.1.0)
