## ML-Plan
### Installation
You can bind in ML-Plan via a Maven dependency (using Maven central as repository).
### Maven
```
<dependency>
  <groupId>ai.libs</groupId>
  <artifactId>mlplan</artifactId>
  <version>0.1.2</version>
</dependency>
```

### Gradle 
```gradle
dependencies {
    implementation 'ai.libs:mlplan:0.1.2'
}
```

### Usage
The shortest way to obtain an optimized WEKA classifier via ML-Plan for your data object `data` is to run
```java
Classifier optimizedClassifier = AbstractMLPlanBuilder.forWeka().withDataset(data).build().call();
```
An analogous call exists for scikit-learn pipelines.
Here, several default parameters apply that you may usually want to customize.

### Customizing ML-Plan
This is just a quick overview of the most important configurations of ML-Plan.

#### Creating an ML-Plan builder for your learning framework
Depending on the library you want to work with, you then can construct a WEKA or scikit-learn related builder for ML-Plan.
Both builders have the same basic capacities (and only these are needed for the simple example below).
For library-specific aspects, there may be additional methods for the respective builders.


Note that ML-Plan for scikit-learn is also Java-based, i.e. we do not have a Python version of ML-Plan only for being able to cope with scikit-learn. Instead, ML-Plan can be configured to work with scikit-learn as the library to be used.

##### ML-Plan for WEKA
```java
MLPlanWekaBuilder builder = AbstractMLPlanBuilder.forWeka();
```

##### ML-Plan for scikit-learn
```java
MLPlanSKLearnBuilder builder = AbstractMLPlanBuilder.forSKLearn();
```

**Note**: If you want to use ML-Plan for scikit-learn, then ML-Plan assumes Python 3.5 or higher to be active (invoked when calling `python` on the command line), and the following packages must be installed:
`liac-arff`,
`numpy`, 
`json`,
`pickle`,
`os`,
`sys`,
`warnings`,
`scipy`,
`scikit-learn`.
Please make sure that you really have `liac-arff` installed, and **not** the `arff` package.

#### Configuring timeouts
With the `builder` variable being configured as above, you can specify timeouts for ML-Plan as a whole, as well as timeouts for the evaluation of a single solution candidate or nodes in the search.
By default, all these timeouts are set to 60 seconds.
```java
/* set the global timeout of ML-Plan to 1 hour: */
builder.withTimeOut(new TimeOut(3600, TimeUnit.SECONDS));

/* set the timeout of a node in the search graph (evaluation of all random completions of a node): */
builder.withNodeEvaluationTimeOut(new TimeOut(300, TimeUnit.SECONDS));

/* set the timeout of a single solution candidate */
builder.withCandidateEvaluationTimeOut(new TimeOut(300, TimeUnit.SECONDS));
```

#### Running ML-Plan with your data
We currently work with the Instances data format of the WEKA library:
```java
/* Load your training dataset with WEKA's instances */
Instances trainDataset = new Instances(new FileReader("myDataset.arff"));

/* configure the builder to use the given data */
builder.withDataset(trainDataset);

/* build and call ML-Plan */
MLPlan mlplan = builder.build();
Classifier chosenClassifier = mlplan.call();
```

### JavaDoc
JavaDoc is available here: https://javadoc.io/doc/ai.libs/mlplan/
