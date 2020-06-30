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

#### Operations
For each component `c` and each of its provided interfaces `c.i`, HASCO creates an operation
```
satisfy<i>With<c>(c1, c2, p1,.., pm, s1,..,sn)
	pre-condition: component(c1)
	add-list:
		component(c2) & resolves(c1, '<i>', '<c.name>', c2),
		parameterContainer('<c.name>', '<p.name>', c2, p1),
		..,
		parameterContainer('<c.name>', '<p.name>', c2, pm),
		val(p1,[<p1.min>, <p1.max>]),
		..,
		val(pm,default_cat),
		interfaceIdentifier('<c.name>', '<c.rqid1>', c2, s1),
		..
		interfaceIdentifier('<c.name>', '<c.rqidn>', c2, sn)
	delete-list: <empty>
```
Here,
* single quotation marks indicate the usage of a logical *constant*, whereas parameters without such quotation marks are considered planning variables.
* `c1` and `c2` are *instances* of components. `c1` is an instance of some component that has a required interface `i`, which will be satisfied by the instance `c2` of component `c`.
* `resolves(c1, '<i>', '<c.name>', c2)` is a predicate that declares that the interface `i` of the component instance `c1` will be resolved using the instance `c2` of component `c`
* `parameterContainer('<c.name>', '<p.name>', c2, pi)` is a predicate that declares that the planning variable `pi` stores the value for the parameter `<p.name>` of the instance `c2` of component `c`
* `p1` is pretended to be a numeric parameter, and `pn` is pretended to be a categorical parameter
* `val(pi, '<something>')` indicates that the parameter variable `pi` holds the value `<something>`. This is the variable where the planner will assign a concrete value for each parameter.
* `interfaceIdentifier('<c.name>', '<c.rqidi>', c2, si)` is a predicate that declares that the planning variable `si` represents the i-th required interface (of name `<c.rqidi>`) of the *instance* c2 of the component `c`

In essence, the really important effect is the `resolves` predicate, because it says that we satisfy the required interface `<i>` of `c` with this (new) component instance `c2`. All the other effects only add bookkeeping variables for this newly introduced component instances: One for each of its `m` parameters, and one for each of its `n` required interfaces.


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

#### Methods

```
resolve<i>With<c>(c1, c2, p1,.., pm, c'1,..,c'k)
	taskName: tResolve<i>(c1, c2)
	pre-condition: component(c1)
	task-network:
		satisfy<i>With<c>(c1, c2, p1,.., pm, s1,..,sn) ->
		tResolve<i1>(c2, c'1) ->
		.. ->
		tResolve<ik>(c2,..,c'k) ->
		tRefineParamsOf<c>(c1, c2, p1, .., pn)
	outputs: p1,..,pm,s1,..,sn
```

### JavaDoc
JavaDoc is available [here](https://javadoc.io/doc/ai.libs/hasco-core/).

### Contribute
ML-Plan is currently developed in the [softwareconfiguration folder of AILibs on github.com](https://github.com/fmohr/AILibs/tree/master/softwareconfiguration/mlplan).
