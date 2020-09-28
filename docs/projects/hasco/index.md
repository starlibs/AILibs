---
layout: project
title: HASCO
subtitle: HASCO subtitle
navigation_mode: anchor
version: 0.2.3
navigation:
    - { id: "overview", link: "overview", title: "Overview" }
    - { id: "installation", link: "installation", title: "Installation" }
    - { id: "usage", link: "usage", title: "Usage" }
    - { id: "the-hasco-reduction", link: "the-hasco-reduction", title: "The HASCO Reduction" }
    - { id: "javadoc", link: "javadoc", title: "JavaDoc" }
    - { id: "contribute", link: "contribute", title: "Contribute" }
---
# Overview
HASCO is an algorithm to hierarchically configure component systems in which the components can have parameters.
Components can have required interfaces, which, for any of its instances, must be satisfied to make it work properly.
Components that provide the required interfaces can be used to resolve these needs.
A component instance hence can be thought of a component in which the parameters assume concrete values and each required interface is satisfied with a component instance (note the recursion).
We can hence think of a component instance as a tree of component groundings (one each for each required interface).

A problem is represented by
* a collection of components
* the name of a required interface
* a function that assigns a score to each solution

HASCO resolves such a problem by reducing it to hierarchical task network (HTN) planning, which is eventually resolved using a path search algorithm.

# Installation
You can bind in HASCO via a Maven dependency (using Maven central as repository).
### Maven
```
<dependency>
  <groupId>ai.libs</groupId>
  <artifactId>hasco-core</artifactId>
  <version>{{ page.version }}</version>
</dependency>
```

### Gradle 
```gradle
dependencies {
    implementation 'ai.libs:hasco-core:{{ page.version }}'
}
```

# Usage
## Obtaining a Software Configuration Problem Object
```java
RefinementConfiguredSoftwareConfigurationProblem<Double> problem = new RefinementConfiguredSoftwareConfigurationProblem<>(problemFile, "interfacename", n -> 0.0);
```
Here, we have a benchmark that always assigns 0 to any solution.
Replace it with a different benchmark to do something meaningful.

## Obtaining an HASCO instance
The easiest way to use HASCO is to use the HASCO builder.

### Using a BF search with random completions (as e.g. in ML-Plan)
```java
HASCOViaFD<Double> hasco = HASCOBuilder.get().withProblem(problem).withBestFirst().viaRandomCompletions().withNumSamples(3).getAlgorithm()
```
The problem can even be specified in the `get` method, so you can also write
```java
HASCOViaFD<Double> hasco = HASCOBuilder.get(problem).withBestFirst().viaRandomCompletions().withNumSamples(3).getAlgorithm()
```

### Using a depth first search
```java
HASCOViaFD<Double> hasco = HASCOBuilder.get().withProblem(problem).withDFS().getAlgorithm();
```

## Configuring the algorithm beforehand
You can setup the timeout
```java
builder.withTimeout(new Timeout(10, TimeUnit.SECONDS))
```
and the number of CPUs:
```java
builder.withCPUs(4)
```

## Getting Results
One option is to listen to `HASCOSolutionEvent` objects:
```java
HASCOViaFD<Double> hasco = HASCOBuilder.get().withProblem(someProblem).withDFS().getAlgorithm();
hasco.registerSolutionEventListener(e -> System.out.println("Received solution with score " + e.getScore() + ": " + e.getSolutionCandidate().getComponentInstance()));
hasco.call();
```

If you need more flexibility in terms of a more sophisticated listener, you can create a separate object in which you subscribe to the relevant event
```java
/* register listener */
hasco.registerListener(new Object() {

	@Subscribe
	public void receiveSolution(final HASCOSolutionEvent<Double> e) {
		System.out.println("Received solution with score " + e.getScore() + ": " + e.getSolutionCandidate().getComponentInstance());
	}
});

/* run HASCO */
hasco.call();
```

If you want to have access to the control flow of HASCO while accessing the found solutions, you can use the step technique:
```java
/* create algorithm */
HASCOViaFD<Double> hasco = HASCOBuilder.get().withProblem(problem).withDFS().getAlgorithm();

/* step over events until algorithm finishes */
while (hasco.hasNext()) {
	IAlgorithmEvent event = hasco.nextWithException();
	if (event instanceof HASCOSolutionEvent) {
		HASCOSolutionCandidate<Double> s = ((HASCOSolutionEvent<Double>) event).getSolutionCandidate();
		System.out.println("Received solution with score " + s.getScore() + ": " + s.getComponentInstance());
	}
}
```

## Using Custom Search Algorithms
If you want to use another search algorithm that is not covered in the builder, you can do that via setting the respective search algorithm factory.
The only condition is that the factoy implements the interface `IOptimalPathInORGraphSearchFactory<IPathSearchWithPathEvaluationsInput<N, A, V>, EvaluatedSearchGraphPath<N, A, V>, N, A, V, ?>`,
where typically `N = TFDNode`, `A = String`, `V = Double`.
An example of such a customized search factory using Monte Carlo Tree Search (MCTS) in the path search variant is as follows:
```java
/* configure MCTS path search factory */
MCTSPathSearchFactory<TFDNode, String> mctsPathSearchFactory = new MCTSPathSearchFactory<>();
UCTFactory<TFDNode, String> uctFactory = new UCTFactory<>();
mctsPathSearchFactory.withMCTSFactory(uctFactory);

/* configure the builder with this factory */
HASCOViaFDBuilder<Double, ?> builder = HASCOBuilder.get(problem);
builder.withSearchFactory(mctsPathSearchFactory);
HASCOViaFD<Double> hasco = builder.getAlgorithm();

/* register listener */
hasco.registerSolutionEventListener(e -> System.out.println("Received solution with score " + e.getScore() + ": " + e.getSolutionCandidate().getComponentInstance()));

/* find all solutions */
for (int i = 0; i < 10; i++) {
	hasco.nextWithException(); // UCT draws the same paths multiple times, so we see solutions several times here
}
```

## Serializing Component Instances to JSON
```java
new ComponentSerialization().serialize(ci)
```
Here, `ci` is your `IComponentInstance` object.


# THE HASCO Reduction
HASCO solves the software configuration problem by conducting a double reduction step.
First, it reduces the original problem to an HTN planning problem.
Second, it reduces the HTN problem to a path search problem.

## The HTN Planning Problem

### Tasks
```
Complex Tasks
----------------------------------------------------------------
tResolve<i>(c1, c2)				# resolve component instance c1 with component instance c2
tRefineParamsOf<c>(c,p1,..,pm)			# refine all the parameters of c
tRefineParam<p>Of<c>(ci, pc)			# refine the parameter container pc belonging to component instance ci

Primitive Tasks
----------------------------------------------------------------
satisfy<i>With<c>(c1, c2, p1,.., pm, r1,..,rn)	# declare in state that component interface <i> of component instance c1 is resolved via c2
redefValue(container, previousValue, newValue)	# changes the value in the parameter value container
declareClosed(container)			# declare in state that the container value will not be changed anymore
```

### Predicates and their Semantics
```
Uninterpreted Predicates
-----------------------------------------------------------------
component(x)				# x is a component INSTANCE (maybe a bit misleading)
parameterContainer(cName, pName, c, p)	# p is the container for the parameter value of parameter pName of component cName in the component instance c
val(p,v)				# v is the value currently hold by the parameter container p
interfaceIdentifier(cName, iName, c, i)	# i is the reference to the interface with name iName of the component cName in the instance c
overwritten(p)				# the parameter container p has been redefined at least once

Evaluable Predicates
-----------------------------------------------------------------
refinementCompleted(cName, c)						# the instance c of component cName has been completely specified
isValidParameterRangeRefinement(cName, c, pName, p, curval, newval)	# the parameter container p of param pName in the instance c of component cName may be refined from curval to newval
notRefinable(cName, c, pName, p, curval)				# it is allowed to further refine the parameter container p of param pName in the instance c of component cName

```


### Operations
For each component `c` and each of its provided interfaces `c.i`, HASCO creates an operation
```
satisfy<i>With<c>(iHandle, cHandle, p1,.., pm, r1,..,rn)
	pre-condition: <empty>
	add-list:
		component(cHandle) & resolves(iHandle, '<i>', '<c>', c2),
		parameterContainer('<c>', '<p.name>', c2, p1),
		..,
		parameterContainer('<c>', '<p.name>', c2, pm),
		val(p1,[<p1.min>, <p1.max>]),
		..,
		val(pm,default_cat),
		interfaceIdentifier('<c>', '<c.rqid1>', c2, r1),
		..
		interfaceIdentifier('<c>', '<c.rqidn>', c2, rn)
	delete-list: <empty>
```
Here,
* single quotation marks indicate the usage of a logical *constant*, whereas parameters without such quotation marks are considered planning variables.
* `c1` and `c2` are *instances* of components. `c1` is an instance of some component that has a required interface `i`, which will be satisfied by the instance `c2` of component `c`.
* `resolves(c1, '<i>', '<c>', c2)` is a predicate that declares that the interface `i` of the component instance `c1` will be resolved using the instance `c2` of component `c`
* `parameterContainer('<c>', '<p.name>', c2, pi)` is a predicate that declares that the planning variable `pi` stores the value for the parameter `<p.name>` of the instance `c2` of component `c`
* `p1` is pretended to be a numeric parameter, and `pn` is pretended to be a categorical parameter
* `val(pi, '<something>')` indicates that the parameter variable `pi` holds the value `<something>`. This is the variable where the planner will assign a concrete value for each parameter.
* `interfaceIdentifier('<c>', '<c.rqidi>', c2, ri)` is a predicate that declares that the planning variable `ri` represents the i-th required interface (of name `<c.rqidi>`) of the *instance* c2 of the component `c`

In essence, the really important effect is the `resolves` predicate, because it says that we satisfy the required interface `<i>` of `c` with this (new) component instance `c2`. All the other effects only add bookkeeping variables for this newly introduced component instances: One for each of its `m` parameters, and one for each of its `n` required interfaces.

Q: Why is there an individual operator for each combination of `<i>` and `<c>` instead of making these parameters?

A: The reason is that, depending on the component, we need different quantities of parameter containers etc., and determining this at plan time is overly complicated.


There are two more operations:
```
redefValue(container, previousValue, newValue)
	pre-condition: val(container,previousValue)
	add-list: val(container, newValue) & overwritten(container)
	delete-list: val(container, previousValue)
 
declareClosed(container)
	pre-condition: <empty>
	add-list: closed(container)
	delete-list: <empty>
```
The `declaredClosed` operator seems to do nothing relevant, because the `closed` predicate is never used. In fact, it only serves to introduce an explicit state change; states are represented by the agenda and the literals.

### Methods

```
resolve<i>With<c>(c1; c2, p1,.., pm, r1,..,rn)
	taskName: tResolve<i>(c1, c2)
	pre-condition: component(c1)
	task-network:
		satisfy<i>With<c>(c1, c2, p1,.., pm, r1,..,rn) ->
		tResolve<i1>(c2, r1) ->
		.. ->
		tResolve<ik>(c2,..,rn) ->
		tRefineParamsOf<c>(c1, c2, p1, .., pm)
	outputs: p1,..,pm,r1,..,rn

ignoreParamRefinementFor<p>Of<c>(object, container, curval)
	taskName: tRefineParam<p>Of<c>(object, container)
	pre-condition: parameterContainer('<c>', '<p.name>', object, container) & val(container,curval) & overwritten(container)
	pre-condition (evaluable): notRefinable('<c>', object, '<p.name>', container, curval)
	task-network: declareClosed(container)

refineParam<p>Of<c>(object, container, curval, newval)
	taskName tRefineParam<p>Of<c>(object, container)
	pre-condition: parameterContainer(<c>', '<p.name>', object, container) & val(container,curval)
	pre-condition (evaluable): isValidParameterRangeRefinement('<c>', object, '<p.name>', container, curval, newval)
	task-network: redefValue(container, curval, newval)
	

refineParamsOf<c>(c2, p1,..,pm)
	taskName: tRefineParamsOf<c>(c2,p1,..,pm)
	pre-condition: component(c2)
	pre-condition (evaluable): !refinementCompleted('<c>', c2)
	task-network:
		tRefineParam<p>Of<c>(c2, p1) ->
		..
		tRefineParam<p>Of<c>(c2, pm) ->
		tRefineParamsOf<c>(c2, p1, .., pm)

closeRefinementOfParamsOf<c>(c2, p1,..,pm)
	taskName: tRefineParamsOf<c>(c2, p1,..,pm)
	pre-condition: component(c2)
	pre-condition (evaluable): refinementCompleted('<c>', c2)
	task-network: <empty>
```

### Problem Definition
```
init task-network: tResolve<reqInterface>('request', 'solution')
```

### Alternative Methods for the case of list interfaces
```
resolve<i>(cHandle, iGroupHandle; ir_1,..,ir_<max(I)>, cHandle_1,..,cHandle_<max(I>)
	taskName: tResolveGroup<i>(iGroupHandle)
	pre-condition: <empty>
	task-network:
		defineInterface(cHandle, ir_1) ->
		..
		defineInterface(cHandle, ir_<max(I)>) ->
		tResolveSingle<i>(iGroupHandle, ir_1, cHandle_1) ->
		..
		tResolveSingle<i>(iGroupHandle, ir_<min(I)>, cHandle_<min(I)>) -> 
		tResolveSingleOptional<i>(iGroupHandle, ir_<min(I) + 1>, cHandle_<min(I)> + 1) ->
		..
		tResolveSingleOptional<i>(iGroupHandle, ir_<max(I)>, cHandle_<max(I)>) -> 
	outputs: c2_1,..,c2_<max(I), cHandle_1,..,cHandle_<max(I>>


resolve<i>With<c>(iHandle, iGroup, cHandle; p1,.., pm, iSubGroup1,..,iSubGroupn)
	taskName: tResolveSingle<i>(iGroup, iHandle, cHandle)
	pre-condition: !anyOmitted(iGroup)
	task-network:
		satisfy<i>With<c>(iHandle, cHandle, p1,.., pm, iSubGroup1,..,iSubGroupn) ->
		tResolveGroup<i1>(cHandle, iSubGroup1) ->
		.. ->
		tResolveGroup<ik>(cHandle, iSubGroupn) ->
		tRefineParamsOf<c>(cHandle, p1, .., pm)
	outputs: p1,..,pm,iSubGroup1,..,iSubGroupn

doResolve<i>(c1, c2)
	taskName: tResolveSingleOptional<i>(c1, c2)
	pre-condition: component(c1), !anyOmitted(c1,'<i>')
	task-network: tResolveSingle<i>(c1, c2)
	outputs: <empty>

doNotResolve<i>(c1, c2)
	taskName: tResolveSingleOptional<i>(c1, c2)
	pre-condition: component(c1)
	task-network: omitResolution(c1, '<i>', c2)
	outputs: <empty>

```

The new operator is
```
omitResolution(c1, i, c2)
	pre-condition: <empty>
	add-list: anyOmitted(c1, i) & null(c2)
	delete-list: <empty>
```

The initial problem changes to
```
init task-network: tResolveSingle<reqInterface>('request', 'solution')
```

# JavaDoc
JavaDoc is available [here](https://javadoc.io/doc/ai.libs/hasco-core/).

# Contribute
ML-Plan is currently developed in the [softwareconfiguration folder of AILibs on github.com](https://github.com/starlibs/AILibs/tree/master/softwareconfiguration/mlplan).
