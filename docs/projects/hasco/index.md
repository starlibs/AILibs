---
layout: project
logo: ailibs-logo.png
title: ML-Plan
subtitle: ML-Plan subtitle
navigation_mode: anchor
version: 0.1.5
navigation:
    - { id: "overview", link: "overview", title: "Overview" }
    - { id: "installation", link: "installation", title: "Installation" }
    - { id: "usage", link: "usage", title: "Usage" }
    - { id: "javadoc", link: "javadoc", title: "JavaDoc" }
    - { id: "contribute", link: "contribute", title: "Contribute" }
navigation_active: overview
---
# HASCO
## Overview

## Under the Hood
HASCO solves the software configuration problem by conducting a double reduction step.
First, it reduces the original problem to an HTN planning problem.
Second, it reduces the HTN problem to a path search problem.

### The HTN Planning Problem

#### Tasks
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

#### Predicates and their Semantics
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


#### Operations
For each component `c` and each of its provided interfaces `c.i`, HASCO creates an operation
```
satisfy<i>With<c>(c1, c2, p1,.., pm, r1,..,rn)
	pre-condition: component(c1)
	add-list:
		component(c2) & resolves(c1, '<i>', '<c>', c2),
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

#### Methods

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

#### Problem Definition
```
init task-network: tResolve<reqInterface>('request', 'solution')
```

### JavaDoc
JavaDoc is available [here](https://javadoc.io/doc/ai.libs/hasco-core/).

### Contribute
ML-Plan is currently developed in the [softwareconfiguration folder of AILibs on github.com](https://github.com/fmohr/AILibs/tree/master/softwareconfiguration/mlplan).
