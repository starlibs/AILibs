## ML-Plan
### Installation
You can bind in ML-Plan via a Maven dependency (using Maven central as repository).
### Maven
```
<dependency>
  <groupId>ai.libs</groupId>
  <artifactId>mlplan</artifactId>
  <version>0.1.1</version>
</dependency>
```

### Gradle 
```gradle
dependencies {
    implementation 'ai.libs:mlplan:0.1.1'
}
```

### Usage
#### Creating an ML-Plan builder for your learning framework
Depending on the library you want to work with, you then can construct a WEKA or scikit-learn related builder for ML-Plan.
Note that ML-Plan for scikit-learn is also Java-based, i.e. we do not have a Python version of ML-Plan only for being able to cope with scikit-learn. Instead, ML-Plan can be configured to work with scikit-learn as the library to be used.

##### ML-Plan for WEKA
```java
MLPlanWekaBuilder builder = AbstractMLPlanBuilder.forWeka();
```

##### ML-Plan for scikit-learn
```java
MLPlanWekaBuilder builder = AbstractMLPlanBuilder.forSKLearn();
```

#### Configuring timeouts
With the `builder` variable being configured as above, you can specify timeouts for ML-Plan as a whole, as well as timeouts for the evaluation of a single solution candidate or nodes in the search.
By default, all these timeouts are set to 60 seconds.
```java
/* set the global timeout of ML-Plan to 1 hour: */
builder.withTimeOut(new TimeOut(3600, TimeUnit.SECONDS));

/* set the timeout of a single node in the search graph (evaluation of all random completions of a node): */
builder.withNodeEvaluationTimeOut(new TimeOut(300, TimeUnit.SECONDS));

/* set the timeout of a single */
builder.withCandidateEvaluationTimeOut(new TimeOut(300, TimeUnit.SECONDS));
```

#### Running ML-Plan with your data
We currently work with the Instances data format of the WEKA library:
```java
/* Load your training dataset with WEKA's instances */
Instances trainDataset = new Instances(new FileReader("myDataset.arff"));

/* configure the builder to use the given data */
builder.withDataset(trainDataset);

/* build an call ML-Plan */
MLPlan mlplan = builder.build();
Classifier chosenClassifier = mlplan.call();
```
