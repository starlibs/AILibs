---
layout: project
logo: mlplan-logo.png
title: ML-Plan
subtitle: ML-Plan subtitle
navigation_mode: anchor
navigation:
    - { id: "overview", link: "overview", title: "Overview" }
    - { id: "installation", link: "installation", title: "Installation" }
    - { id: "usage", link: "usage", title: "Usage" }
    - { id: "javadoc", link: "javadoc", title: "JavaDoc" }
    - { id: "contribute", link: "contribute", title: "Contribute" }
navigation_active: overview
---
# ML-Plan
## Overview
ML-Plan is a free software library for automated machine learning.
It can be used to optimize machine learning pipelines in WEKA or scikit-learn.

When publishing articles in which you mention ML-Plan, please cite the following paper:

Felix Mohr, Marcel Wever, and Eyke Hüllermeier. "ML-Plan: Automated machine learning via hierarchical planning", Machine Learning, 2018.

```
@article{DBLP:journals/ml/MohrWH18,
  author    = {Felix Mohr and Marcel Wever and Eyke H{\"{u}}llermeier},
  title     = {ML-Plan: Automated machine learning via hierarchical planning},
  journal   = {Machine Learning},
  volume    = {107},
  number    = {8-10},
  pages     = {1495--1515},
  year      = {2018},
  url       = {https://doi.org/10.1007/s10994-018-5735-z},
  doi       = {10.1007/s10994-018-5735-z},
  timestamp = {Wed, 01 Aug 2018 13:10:15 +0200}
}
```

## Installation
You can bind in ML-Plan via a Maven dependency (using Maven central as repository).
### Maven
```
<dependency>
  <groupId>ai.libs</groupId>
  <artifactId>mlplan-full</artifactId>
  <version>{{ page.version }}</version>
</dependency>
```

### Gradle 
```gradle
dependencies {
    implementation 'ai.libs:mlplan-full:{{ page.version }}'
}
```

If you only need specific projects, you can also install only `mlplan-weka`, `mlplan-sklearn`, or `mlplan-meka`.
The full distribution comes with an CLI and some GUI plugins.

## Usage
ML-Plan works with the api4.org dataset specification, which has native support for ARFF files and openml:
```java
ILabeledDataset d1 = OpenMLDatasetReader.deserializeDataset(3);
ILabeledDataset d2 = ArffDatasetAdapter.readDataset(new File("path/to/your/arff"));
```

If you have your data loaded in an object called `data`, the shortest way to obtain an optimized classifier via ML-Plan for your data is to run
```java
IClassifier c = new MLPlanWekaBuilder().withDataset(data).build().call()
```
Analogously, to create a classifier based on sklearn, you can call
```java
ScikitLearnWrapper c = MLPlanScikitLearnBuilder.forClassification().withDataset(data).build().call();
```
Here, several default parameters apply that you may usually want to customize.

### Customizing ML-Plan
This is just a quick overview of the most important configurations of ML-Plan.
The general process is to make the configurations in the builder, build ML-Plan, and invoke its `call` method:
```java
AMLPlanBuilder builder = ... // see below how to get your builder

/* configure the builder */
...

MLPlan mlplan = builder.build();
ISupervisedLearner learner = mlplan.call();
```
Depending on the concrete context (WEKA, sklearn, classification/regression, etc.), the involved types are more specific than the above general types.

#### Creating an ML-Plan builder for your learning framework
Depending on the library you want to work with, you then can construct a WEKA or scikit-learn related builder for ML-Plan.
Both builders have the same basic capacities (and only these are needed for the simple example below).
For library-specific aspects, there may be additional methods for the respective builders.


Note that ML-Plan for scikit-learn is also Java-based, i.e. we do not have a Python version of ML-Plan only for being able to cope with scikit-learn. Instead, ML-Plan can be configured to work with scikit-learn as the library to be used.

##### ML-Plan for WEKA
If you are interested in standard classification tasks such as binary or multinomial classification, create an ML-Plan builder as follows.

```java
MLPlanWekaBuilder builder = MLPlanWekaBuilder.forClassification();
```

If you have a regression problem instead you may be get an appropriate ML-Plan builder like this:

```java
MLPlanWekaBuilder builder = MLPlanWekaBuilder.forRegression();
```

##### ML-Plan for scikit-learn
ML-Plan for scikit-learn can also be instantiated for both classification, and regression. The way the builders are obtained are analogous to how we create these for WEKA:

Use

```java
MLPlanScikitLearnBuilder builder = MLPlanScikitLearnBuilder.forClassification();
```

for obtaining an ML-Plan builder to work with scikit-learn to tackle standard classification problems and 

```java
MLPlanScikitLearnBuilder builder = MLPlanScikitLearnBuilder.forRegression();
```

to obtain a builder which pre-configures the builder ready for tackling regression problems with scikit-learn.

**Note**: If you want to use ML-Plan for scikit-learn, then ML-Plan assumes Python 3.5 or higher to be active (invoked when calling `python` on the command line), and the following packages must be installed:
`liac-arff`,
`numpy`, 
`json`,
`pickle`,
`os`,
`sys`,
`warnings`,
`scipy`,
`scikit-learn`,
`tpot`,
`pandas`,
`xgboost`.
Please make sure that you really have `liac-arff` installed, and **not** the `arff` package.

If you want to be sure that ML-Plan uses the correct python installation on your machine, you can create a file `conf/python.properties` in the ML-Plan folder with the following layout:
```
path = <folder where your python3 binary resides>
pythonCmd = <optional: name of your python3 binary>
```


##### Multi-Label ML-Plan for MEKA (ML2-Plan)
```java
MLPlanMEKABuilder builder = AbstractMLPlanBuilder.forMEKA();
```

**Note**: Datasets, i.e. Instances objects, have to be loaded according to MEKA's conventions. More specifically, in order to use Instances for multi-label classification the labels have to appear in the first columns and the class index marks the number existing labels (starting to count from the first column). The dataset preparation can be conveniently achieved as follows.

```java
Instances myDataset = new Instances(new FileReader(new File("my-dataset-file.arff")));
MLUtils.prepareData(myDataset);
```

#### Configuring the number of CPUs
```java
/* set the number of CPUs allowed for search to 4 */
builder.withNumCpus(4);
```

#### Configuring timeouts
With the `builder` variable being configured as above, you can specify timeouts for ML-Plan as a whole, as well as timeouts for the evaluation of a single solution candidate or nodes in the search.
By default, all these timeouts are set to 60 seconds.
```java
/* set the global timeout of ML-Plan to 1 hour: */
builder.withTimeOut(new Timeout(3600, TimeUnit.SECONDS));

/* set the timeout of a node in the search graph (evaluation of all random completions of a node): */
builder.withNodeEvaluationTimeOut(new Timeout(300, TimeUnit.SECONDS));

/* set the timeout of a single solution candidate */
builder.withCandidateEvaluationTimeOut(new Timeout(300, TimeUnit.SECONDS));
```

#### Configuring the portion used for the selection phase
```java
builder.withPortionOfDataReservedForSelection(.3); // use 30% of the data for selection
builder.withPortionOfDataReservedForSelection(.0); // disable selection phase
```

#### Configuring the cross validation technique to evaluate candidates
```java
builder.withMCCVBasedCandidateEvaluationInSearchPhase(3, .8); // use 3 repetitions with 80%/20% splits each
```

#### Working with WEKA objects
If you want to use ML-Plan with the typical types of the WEKA library such as `Instances` and `Classifier`, you can use the `WekaInstances` wrapper class, and obtain the classifier object directly from ML-Plan:
```java
Instances dataset = new Instances(new FileReader("path/to.arff")); // set the class index appropriately
Classifier c = new MLPlanWekaBuilder().withDataset(new WekaInstances(dataset)).build().call().getClassifier();
```

### JavaDoc
JavaDoc is available [here](https://javadoc.io/doc/ai.libs/mlplan/).

### Contribute
ML-Plan is currently developed in the [softwareconfiguration folder of AILibs on github.com](https://github.com/starlibs/AILibs/tree/master/softwareconfiguration/mlplan).
