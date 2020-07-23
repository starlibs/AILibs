---
layout: main
title: Design Principles
---

# Design Principles
This page provides a summary of the (Java) design principles we (try to) adopt in AILibs.

## Logging
The importance of good logging is undebatable for effective algorithm behavior analysis, debugging, and maintanaece.

### Maximal Logging Flexibility by Individual Loggers
In AILibs we seek to maximize the logging flexibility, which means that the library user can customize logging behavior as fine granular and targeted as possible.
There is nothing more annoying in the attempt of debugging than a logger than can only be set globally to INFO or DEBUG level and hence either not logging anything meaningful or spamming every detail of any parts of the software mostly irrelevant in the concrete context of interest.

To achieve this goal, AILibs adopts three main principles for logging:
1. **Non-Static and Customizable Loggers.** In contrast to other libraries, AILibs considers *static loggers an anti-pattern*, because static loggers do not allow to contextualize logging behavior.
For example, there might be two (algorithm) objects of the same class with entirely different setups, and we certainly do not want to mix the log outputs of the two; with static loggers this is unavoidable.
Consequently, loggers should always be *object variables* and not static.
\\
\\
The typical way to define a logger is then as follows:
```java
private Logger logger = LoggerFactory.getLogger(<nameOfClass>.class);
```
This way, the logger is initialized with some default value (typically the class name) but can be configured on an instance-wise basis after the object creation.
\\
\\
To allow the customization of loggers declared as above, AILibs introduces the `ILoggingCustomizable` interface, which should in fact be implemented by all non-entity classes that produce any log output.
The interface comes with two methods, one that allows to set the *name* of the logger and one to get this name.
Note that the interface does not offer, in any way, a direct access to the logger itself.
However a default implementation of these methods is to set the logger by 
```java
public void setLoggerName(String loggerName) {
	this.logger = LoggerFactory.getLogger(loggerName);
}
```


2. **Logging Cascades.**
The `ILoggingCustomizable` interface has an important additional role other than standardizing the way how loggers are set: For an object that implements logged processes, the interface indicates which of the *used sub-components* can be customized as well.
This gives rise to *logging cascades*, i.e., the benefit now comes from the ability of each class to *know* its sub-components and configure *their* logger in turn on an instance-wise basis.
In this way, a call to *setLoggerName* on some object `o` in fact should trigger a whole cascade of *setLoggerName* invocations of all objects that are somehow relevant for the behavior of `o`.
For example, setting the logger name of a search algorithm should also imply a reset
\\
\\
The convention in AILibs for logger cascades is to suffix the child loggers with a dot-notation based role name.
For example, the `BestFirst` search algorithm uses node evaluators to acquire the quality of nodes, but the concrete class of those evaluators is unknown.
Hence, on setting the logger name of a `BestFirst` instance, the algorithm checks whether its node evaluator is logging customizable and in that case sets the logger correspondingly:
```java
this.bfLogger.info("Switching logger from {} to {}", this.bfLogger.getName(), name);
this.loggerName = name;
this.bfLogger = LoggerFactory.getLogger(name);
this.bfLogger.info("Activated logger {} with name {}", name, this.bfLogger.getName());
if (this.nodeEvaluator instanceof ILoggingCustomizable) {
	this.bfLogger.info("Setting logger of node evaluator {} to {}.nodeevaluator", this.nodeEvaluator, name);
	((ILoggingCustomizable) this.nodeEvaluator).setLoggerName(name + ".nodeevaluator");
} else {
	this.bfLogger.info("Node evaluator {} does not implement ILoggingCustomizable, so its logger won't be customized.", this.nodeEvaluator);
}
```
Here, the node evaluator is customized with the name for the search algorithm plus `.nodeevaluator`.
The advantage is that for an instance configured with `mysearchalgorithm`, we can directly control the logging behavior of the node evaluator of that specific instance with an config entry for `mysearchalgorithm.nodeevaluator`.
In this way, the method can be used to configure all logging-relevant sub-objects with an appropriate name of their role in this context.
The above method code also shows a (maybe) good practice in that it logs the information about the logger change of the child or indicates that it cannot be customized with respect to its logging behavior.

3. **No Logger-Reuse (or only with caution).**
\\
Suppose that A is a superclass of B, and A is an `ILoggingCustomizable` (and so is hence B).
\\
\\
Q: Should we allow B to re-use the logger of A?
\\
A: It depends. The general answer is *no*, and this is the reason why the above code suggests to declare loggers as *private*.
\\
\\
The general motivation to forbid such "shared" loggers among super- and sub-classes is that it breaks the possibility to configure the logging behavior of the object code of the parent (A) and the object code of the child (B) separately.
This is often desirable.
For example, the abstract `AAlgorithm` contains a lot of logging code on different log-levels, but these log-information are really a kind of meta information not referring to the actual *context-specific* behavior of the algorithm (and how could that possibly be; this is why it is abstract).
If you are interested in the DEBUG-level logs of your algorithm, you maybe do not want these to be messed up with technical DEBUG-level logs of the `AAlgorithm` class on termination checks or anything of the like.
A shared logger would take away the possibility of separate logs for the two aspects.
Hence, in general, to allow the possibility of separate configurations for those loggers, a separate logger is needed for A an B.
\\
\\
The only exception for this convention we are currently aware of are **tests**.
On one hand, a sub-class structure in tests is typically used to *complement* the tests of the abstract class or to put it into context.
That is, all the essential behavior is already defined in the abstract class, and additional methods in the sub-class do normall not invoke methods of the parent but simply define new (and independent) tests.
On the other hand, the user has typically no need to locally configure the logger behavior.
In fact, a user will hardly every manually create an object of a test class, but this all works automatically.
\\
\\
While, in theory, there might be an interest to have different logging behaviors for different testers or tested classes, there is a convention for logger names that should work in 95% of the cases:
	* loggers in test classes should be `protected` (not static to ease direct re-use in sub-classes) with the static name `LoggerUtil.LOGGER_NAME_TESTER`, which is currently defined as `tester`
	* there is no reason for test classes to be `ILoggingCustomizable` since users will never manually create test objects and, hence, never change the logger manually.
	* testers that are sub-classes of other testers do (and usually *should*) not have their own logger but re-use the one of their parent
	* if a test is used to test the behavior of an algorithm, it should usually only test *one* algorithm instance. If that algorithm is an `ILoggingCustomizable`, it is good practice to set its logger to the constant `LOGGER_NAME_TESTEDALGORITHM`, which currently evaluates to `testedalgorithm`.
	This way, it is always easy to configure the logging behavior of tested algorithms without needing to know a particular context-specific logger name.
	* similarly, for examples, there is a static directive `LoggerUtil.LOGGER_NAME_EXAMPLE`, currently resolving to `example` that allows to configure the logging behavior of examples.

## Testing

## Generics - Use with Caution

## Builders and Factories


