---
layout: project
title: jaicore-experiments
navigation_mode: anchor
navigation:
    - { id: "overview", link: "overview", title: "Overview" }
    - { id: "installation", link: "installation", title: "Installation" }
    - { id: "usage", link: "usage", title: "Usage" }
    - { id: "javadoc", link: "javadoc", title: "JavaDoc" }
    - { id: "contribute", link: "contribute", title: "Contribute" }
navigation_active: overview
---

# Overview
**jaicore-experiments** is a macro benchmarking library for conducting parameterizable, parallel and persistent performance measures aiming to be reproducible.


# Installation
Add the dependency to {{ page.title }} in your favourite build tool, via: 

## Maven
```
<dependency>
  <groupId>ai.libs</groupId>
  <artifactId>{{ page.title }}</artifactId>
  <version>{{ page.version }}</version>
</dependency>
```

## Gradle 
```gradle
dependencies {
    implementation 'ai.libs:{{ page.title }}:{{ page.version }}'
}
```

# Usage

To use this library, the client must provide the following three components:

- An SQL database connection to store the experiment result.
- An experiment configuration that declares parameters, their ranges and results fields.
- A small layer of glue code that executes the experiment and returns the result.

## Setup

### DB connection config
Define the SQL database connection as properties in a configuration file:

```properties
db.driver = mysql
db.host = <database host>
db.username = <database user name>
db.password = <database password of the user name>
db.database = <database name>
db.ssl = true
db.table = <result table>
``` 

[MySql](https://www.mysql.com/) is the database implementation that is supported and used in this project by the maintainers.
 Specifically, version `5.7.28` is used internally.
 
 `db.host`, `db.username` and `db.password` depend on the configuration of your database. 
 Test these properties with the following command. Make sure to replace the keys with the properties defined in the configuration.
 If the mysql console can be seen, then the properties are legal.

 ```
mysql -h <db.host> -u <db.username> -p<db.password> 
>Welcome to the MySQL monitor.  Commands end with \; or \\g.
>Your MySQL connection id is 2188
>Server version: 5.7.28 MySQL Community Server (GPL)
> ...

mysql> exit
```

Make sure that the declared database name is created before running any experiments:

```
mysql> CREATE DATABASE <db.database>;
```

`db.table` is the name of the table that is used to store the experiment results.
 This table will be created by the experiments package itself.
 
### Experiment config

Next, define the experiment configuration properties.
In this configuration, the experiment parameters (`keyfields`) and results (`resultfields`)  are described.
The set of experiments that are inferred from this configuration and later on conducted, is the cartesian product of the parameter values.

```properties
mem.max = 2000
cpu.max = 2

keyfields = A1:int,A2:int,A3:float,B1:varchar(500),B2,C:BOOL

A1 = 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15
A2 = 100, 200, 300
A3 = 1.25, 2.5, 3.725, 5
B1 = this, parameter, has, 5, values
B2 = value1 with whitespace, value2 with whitespace, value3 with whitespace
C = 1, 0

resultfields = R1:int,R2:float,R3:varchar(500)

constraints = A1 > A3 / 100, A3 > A1
```
  
This configuration defines these parameter fields for the experiment set:
 
 - the numerical parameters: `A1`, `A2`, `A3` with their respective values.
 - two textual parameters: `B1`, `B2`
 - the boolean parameter `C`
 
It also defines that each experiment outputs three values, that is two numerical results `R1`, `R2` and a textual one: `R3`.

`mem.max` and `cpu.max` are mandatory fields that specify the process in which the experiments are performed and are stated in order to establish reproducible experiments.

It is important to note that this defines a *set* of experiments. 
A single experiment from this set has key fields set to specific values within the defined domains.
For example, the following key values describe the parameters of one of the elements of the experiment set:

```properties
A1 = 5
A2 = 200
A3 = 2.5
B1 = "has"
B2 = "value3 with whitespace"
C = false
```

#### Constraints
It is also possible to define constraints among the key fields.
The experiment contains only experiments that fulfill *all* constraints.
As an example, no experiment in the set defined by the configuration above has the following key values:


```properties
A1 = 1
A2 = 200
A3 = 2.5
B1 = "has"
B2 = "value3 with whitespace"
C = false
```

##### Programmatic Constraints
For entirely general constraints, you can add a constraint `java:<classname>` where `<classname>` must be the name of a class that implements the generic interface `Predicate<List<String>>`.
This predicate must implement the method `test(List<String> partialExperiment)` in which it will receive a *partial* experiment description (the list of Strings can be thought of as a k-tuple with the key values of the first k keys).
This method should return `true` iff there exists any valid experiment that has the first k entries set as defined.

#### Omitting Meta-Information of Result Fields
By default, the experimenter memorizes, for each result field, the time and the (approximated) memory consumption when it was last updated.
To this end, two additional fields are created in the database for each result field.
This behavior can be disabled in the configuration file using the fields `ignore.time` and `ignore.memory`, respectively.

To ignore update times for `R1` and `R2`, and ignore memory for `R1`, you can write:
```
ignore.time = R1, R2
ignore.memory = R1
```
Note that each specification is interpreted as a *regular expression* over result field names. So you could also write
```
ignore.time = R[12]
ignore.memory = R1
```
To entirely disable the logging of this meta information, you can use
```
ignore.time = .*
ignore.memory = .*
```

#### Programatically Derived Keys
Sometimes, it is cumbersome to define the set of keys inside the configuration file, and you might want to *generate* them.
This is possible by setting the domain of a keyfield to a string of the form `java:<classname>`, where `<classname>` must be the name of a class that defines the `IExperimentKeyGenerator`.
At the time of setting up the table or running experiments, the respective class must be in the class path.
Use the fully qualified name of the class (including package) for `<classname>`.

## Executing Experiments

This part describes how to incorporate the library into the client code.
This includes the preparation of the database,
how to define the evaluator that is called for each experiment,
and finally how to run the experiments.

It assumes that the db configuration file is located at `configs/db.properties` 
and the experiment set config file is located at `configs/experiments.cfg`.


### Preparing the database

Load the configuration files:

```java
IExperimentSetConfig expConfig = (IExperimentSetConfig) ConfigFactory
        .create(IExperimentSetConfig.class)
        .loadPropertiesFromFile(new File("configs/experiments.cnf"));

IDatabaseConfig dbConfig = (IDatabaseConfig) ConfigFactory
        .create(IDatabaseConfig.class)
        .loadPropertiesFromFile(new File("configs/db.properties"));
```

Create the MySQL handle and set it up with the experiment set config.
This also creates an empty table whose named is defined by the database properties.

```java
ExperimenterMySQLHandle handle = new ExperimenterMySQLHandle(dbConfig);
try {
    handle.setup(expConfig);
} catch (ExperimentDBInteractionFailedException e) {
    logger.error("Couldn't setup the sql handle.", e);
    System.exit(1);
}
```

Synchronize the experiments table.
This block will fill the table with a unique row for each experiment. 
The results are set to null.

```java
ExperimentDatabasePreparer preparer = new ExperimentDatabasePreparer(expConfig, handle);
try {
    preparer.synchronizeExperiments();
} catch (ExperimentDBInteractionFailedException | IllegalExperimentSetupException | AlgorithmTimeoutedException | InterruptedException | AlgorithmExecutionCanceledException | ExperimentAlreadyExistsInDatabaseException e) {
    logger.error("Couldn't synchrinze experiment table.", e);
    System.exit(1);
}
```

| experiment_id 	| A1 	|  A2 	| A3   	| B1   	| B2                     	| C 	| cpus 	| memory_max 	| time_created         	| host 	| executor | time_started 	| R1   	| R1_time 	| R1_memory 	| R2   	| R2_time 	| R2_memory 	| R3   	| R3_time 	| R3_memory 	| exception 	| time_end 	|
|:-------------:	|----	|:---:	|------	|------	|------------------------	|---	|------	|------------	|----------------------	|------	| -------- | --------------	|------	|---------	|-----------	|------	|---------	|-----------	|------	|---------	|-----------	|-----------	|----------	|
| 1             	| 1  	| 100 	| 1.25 	| this 	| value1 with whitespace 	| 1 	| 2    	| 2000       	| Fri Jul 03 2020 ...  	| null 	| null     | null         	| null 	| null    	| null      	| null 	| null    	| null      	| null 	| null    	| null      	| null      	| null     	|
| 2             	| 1  	| 100 	| 2.5  	| this 	| value1 with whitespace 	| 0 	| 2    	| 2000       	| Fri Jul 03 2020 ...  	| null 	| null     | null         	| null 	| null    	| null      	| null 	| null    	| null      	| null 	| null    	| null      	| null      	| null     	|
| ...           	|    	|     	|      	|      	|                        	|   	|      	|            	|                      	|      	|              	|      	|         	|           	|      	|         	|           	|      	|         	|           	|           	|          	|

As can be seen te result fields are all still set to null.

Now create an evaluator that glues the client code to the experimenter. 
The evaluator is simply any implementation of the interface `IExperimentSetEvaluator`.
The job of the evaluator is to gather the values, call user code and submit the results: 

```java
IExperimentSetEvaluator evaluator =
    (ExperimentDBEntry experimentEntry, IExperimentIntermediateResultProcessor processor) -> {
        Experiment experiment = experimentEntry.getExperiment();
        Map<String, String> keyFields = experiment.getValuesOfKeyFields();
        // gather experiment key values:
        int a1 = Integer.parseInt(keyFields.get("A1"));
        int a2 = Integer.parseInt(keyFields.get("A2"));
        float a3 = Float.parseFloat(keyFields.get("A3"));

        String b1 = keyFields.get("B1");
        String b2 = keyFields.get("B2");

        boolean c = keyFields.get("C").equals("1");

        // glue to the client code:
        someUserFunction(a1, a2, a3, b1, b2, c);
        int resultR1 = getResultR1();
        float resultR2 = getResultR2();
        String resultR3 = getResultR3();

        // submit the results:
        Map<String, Object> result = new HashMap<>();
        result.put("R1", resultR1);
        result.put("R2", resultR2);
        result.put("R3", resultR3);
        processor.processResults(result);
    };
```

### Running Experiments
Now it is possible to run experiments. To do so simply create an `ExperimentRunner` and run a number of desired experiments: 

```java

try {
    ExperimentRunner runner = new ExperimentRunner(expConfig, evaluator, handle);
    runner.sequentiallyConductExperiments(100);
} catch (ExperimentDBInteractionFailedException | InterruptedException e) {
    logger.error("Error trying to run experiments.", e);
    System.exit(1);
}
```

This will run through the database and conduct 100 experiments one after another by calling the evaluator defined above and pushing the results after each evaluation.
If the evaluation for some experiments fails, and an exception is thrown, the runner will log the error and continue with the next experiment.
The runner will also mark the start time and finish time, in addition to memory consumption after each experiment.

### Using the Experimenter Frontend
The experimenter frontend is not a GUI but a simple handle to conveniently manage the experimenter behavior.
```java
ExperimenterFrontend fe = new ExperimenterFrontend().withEvaluator(<your evaluator>).withExperimentsConfig(expConfig).withDatabaseConfig(dbconfig);

/* you can set up a logger name for the evaluator (only makes sense if the evaluator implement ILoggingCustomizable) */
fe.withLoggerNameForAlgorithm("<logger name>");

/* conduct experiments */
fe.randomlyConductExperiments(1);
```
The frontend currently does not work with REST-based evaluations.

### Parallelizing experiments
Experiments are assumed to be independent of each other.
Given the hardware capabilities, it is thus possible, to run experiments in parallel.
However, since the *memory* assigned to an experiment is usually an important experiment criterion, different experiments should *never* be launched in the same process.
That is, do never run several experiments with the same ExperimentRunner in parallel, because this effectively makes the experiments share the assigned memory and hence distortions the results.
If you do not care about the memory aspect, running experiments in parallel in the same process is of course fine.

### Monitoring Experiment Progress and Detecting/Analyzing Corrupt Experiment Executions
If experiments are run in a compute center, it is often helpful to have some kind of overview of how many experiments have been carried out successfully already.
The `ExperimentUtil` provides some methods to generate SQL queries that help to get insights into what is happening in the experiment execution.
#### General Progress Overview

To get an overview of the current progress in terms of number of experiments that are open, currently running, finished, and failed, use the result of this metod call:
```java
ExperimentUtil.getProgressQuery("<tablename>");
```
Here `<tablename>` refers to the table in which the experiments are maintained. The table will contain a field with the estimated time until the whole experiment set is finished.
This computation assumes that all experiments have roughly the same runtime, and it assumes that the same number of executors is used that are currently executing experiments (number of running experiments).
Alternatively, a second parameter can be specified to set this number of executors to a fixed number:
```java
ExperimentUtil.getProgressQuery("<tablename>", <numberofparallelexecutors>);
```

#### Analyzing Failed Experiments and Corrupt Runs
In particular when running parallel experiments on a compute center, tracing back reasons of exceptions and locating the source of problems can be a tough exercise.
To ease this process, the experimenter allows to associate execution information with each experiment executed by an executor.
This is achieved by a field `executor` in the experiments table, which is by default left blank.
To change this behavior, the executor must be configured respectively at time of initialization:
```java
new ExperimentRunner(expConfig, evaluator, handle, "<executorinformation>");

/* if you use the experimenter frontend, you can specify the executor info there */
fe.withExecutorInfo("<executorinformation>");
```
`<executorinformation>` is an optional parameter, which is empty by default.
It defines an identifier of the experiment executor.
This is useful if experiments are conducted in compute centers, because then the executor information can be set to the identifier of the process (often a job id) or any other information that helps to locate log information associated with the experiment.

It is not uncommon that something goes wrong with experiments without having the table updated.
A consequence is that those experiments still are counted as `running` in the above overview while they have in fact already died away.
Assuming that every process that executes experiments has a unique execution information string, the following queries can be helpful to identify corrupt executions:
```java
ExperimentUtil.getQueryToIdentifyCorruptRuns("<tablename>");
```
This will list all the execution informations for which at least two experiments have been marked as started and *not* finished; this cannot be the case if everything went right.

To get the concrete experiment entries for such corrupt executions (this can be helpful in order to get the experiment numbers and execution times that can be used to traverse log files), you can use the following query:
```java
ExperimentUtil.getQueryToListAllUncompletedRunsOfCorruptJob("<tablename>");
```



## Results

The result of each experiment are updated in the database as soon as it is made available by the evaluator above.
This means one can run a large set of experiments and retrieve intermediate results as they are being produced.


# Contribute
jaicore-experiments is currently developed in the [JAICore/jaicore-experiments folder of AILibs on github.com](https://github.com/starlibs/AILibs/tree/master/JAICore/jaicore-experiments).
