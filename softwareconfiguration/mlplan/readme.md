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
```java
// Load your training dataset with WEKA's instances
Instances trainDataset = new Instances(new FileReader("myDataset.arff"));
  
// Create the MLPlan builder for the library of your choice (WEKA, scikit-learn, MEKA, etc.)
MLPlanWekaBuilder builder = AbstractMLPlanBuilder.forWeka();

// DEFINE TIMEOUTS
/* set the global timeout of ML-Plan to 1 hour: */
builder.withTimeOut(new TimeOut(3600, TimeUnit.SECONDS));
/* set the timeout of a single node in the search graph (evaluation of all random completions of a node): */
builder.withNodeEvaluationTimeOut(new TimeOut(300, TimeUnit.SECONDS));
/* set the timeout of a single */
builder.withCandidateEvaluationTimeOut(new TimeOut(300, TimeUnit.SECONDS));

// SET YOUR TRAINING DATA
builder.withDataset(trainDataset);

// BUILD AND CALL ML-Plan
MLPlan mlplan = builder.build();
Classifier chosenClassifier = mlplan.call();
```
