---
layout: project
title: jaicore-experiments
version: 0.2.0
navigation_mode: anchor
navigation:
    - { id: "overview", link: "overview", title: "Overview" }
    - { id: "installation", link: "installation", title: "Installation" }
    - { id: "usage", link: "usage", title: "Usage" }
    - { id: "javadoc", link: "javadoc", title: "JavaDoc" }
    - { id: "contribute", link: "contribute", title: "Contribute" }
navigation_active: overview
---

# jaicore-experiments
**jaicore-experiments** is a macro benchmarking library for conducting parameterizable, parallel and persistent performance measures aiming to be reproducible.


## Overview


## Installation
Add the dependency to {{ page.title }} in your favourite build tool, via: 

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

To use this library, the client must provide the following three components:

- An SQL database connection to store the experiment result.
- An experiment configuration that declares parameters, their ranges and results fields.
- A small layer of glue code that executes the experiment and returns the result.

### Setup

#### DB connection config
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
 
#### Experiment config

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

`mem.max' and `cpu.max' are mandatory fields that specify the process in which the experiments are performed and are stated in order to establish reproducible experiments.

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

### Executing Experiments

This part describes how to incorporate the library into the client code.
This includes the preparation of the database,
how to define the evaluator that is called for each experiment,
and finally how to run the experiments.

It assumes that the db configuration file is located at `configs/db.cfg` 
and the experiment set config file is located at `configs/experiments.cfg`.


#### Preparing the database

Load the configuration files:

```java
IExperimentSetConfig expConfig = (IExperimentSetConfig) ConfigFactory
        .create(IExperimentSetConfig.class)
        .loadPropertiesFromFile(new File("configs/experiments.config"));

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

| experiment_id 	| A1 	|  A2 	| A3   	| B1   	| B2                     	| C 	| cpus 	| memory_max 	| time_created         	| host 	| time_started 	| R1   	| R1_time 	| R1_memory 	| R2   	| R2_time 	| R2_memory 	| R3   	| R3_time 	| R3_memory 	| exception 	| time_end 	|
|:-------------:	|----	|:---:	|------	|------	|------------------------	|---	|------	|------------	|----------------------	|------	|--------------	|------	|---------	|-----------	|------	|---------	|-----------	|------	|---------	|-----------	|-----------	|----------	|
| 1             	| 1  	| 100 	| 1.25 	| this 	| value1 with whitespace 	| 1 	| 2    	| 2000       	| Fri Jul 03 2020 ...  	| null 	| null         	| null 	| null    	| null      	| null 	| null    	| null      	| null 	| null    	| null      	| null      	| null     	|
| 2             	| 1  	| 100 	| 2.5  	| this 	| value1 with whitespace 	| 0 	| 2    	| 2000       	| Fri Jul 03 2020 ...  	| null 	| null         	| null 	| null    	| null      	| null 	| null    	| null      	| null 	| null    	| null      	| null      	| null     	|
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

#### Parallelizing experiments

Experiments are assumed to be independent of each other. 
Given the hardware capabilities, it is thus possible, to run experiments in parallel. 

The experiments package is designed to run in parallel without any additional configuration. 
One can simply spawn multiple processes of the code above. 
The experiments will be distributed among the processes and each process conducts a disjoint set of 100 experiments.

Also, it is possible to spawn multiple thread inside of the same java process that conduct experiments from the same set
In the following example 100 jobs are distributed among 10 threads.

```java
ExecutorService executor = Executors.newFixedThreadPool(10);
List<Future> jobs = new ArrayList<>();
// submit 100 jobs:
for (int i = 0; i < 100; i++) {
    Future job = executor.submit(() -> {
        try {
            ExperimentRunner runner = new ExperimentRunner(expConfig, evaluator, handle);
            runner.sequentiallyConductExperiments(1);
        } catch (ExperimentDBInteractionFailedException | InterruptedException e) {
            logger.error("Error trying to run experiments.", e);
            System.exit(1);
        }
    });
    jobs.add(job);
}
// wait 10 minutes until all experiments are finished.
for (Future future : jobs) {
    try {
        future.get(10, TimeUnit.MINUTES);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
        logger.error("Experiments timed out: ", e);
        executor.shutdownNow();
        System.exit(1);
    }
}
executor.shutdown();
```

### Results

The result of each experiment are updated in the database as soon as it is made available by the evaluator above.
This means one can run a large set of experiments and retrieve intermediate results as they are being produced.


## Contribute
jaicore-experiments is currently developed in the [JAICore/jaicore-experiments folder of AILibs on github.com](https://github.com/fmohr/AILibs/tree/master/JAICore/jaicore-experiments).
