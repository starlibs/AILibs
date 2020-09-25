---
layout: project
title: jaicore-components
version: 0.2.4
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
**jaicore-components** is a library to support the specification and treatment of component selection and configuration problems.


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

## Specifying Component Domains
Describe here the JSON format we use.

### Required Interface Definitions

| directive 	| type 	|  semantic 	| default value  	| Remarks |
|:-------------:|----	|:---:		|------			|------			|
| optional	| boolean | whether or not the required interface must be satisfied | false | is orthogonal to min |
| unique	| boolean | whether or not components must be used at most once to satisfy the interface | false | |
| ordered	| boolean | whether or not the order of realizations is relevant | true | |
| min		| int+ | minimum number of realizations if realized at all | 1 | can be positive even if optional |
| max		| int+ | maximum number of realizations if realized at all | max | must never be smaller than min |

### Constraints for Instantiation
It is possible to define constraints on instantiation of components.
To this end, just add an array node named `constraints` to the repository description.
Constraints consist of a *premise* and a *conclusion*, both of which are descriptions of (parameterless) component instances.
If the premise is entailed in a concrete component instance, the constraint becomes relevant.
Now the constraint can be either *positive* or *negative*.
If it is positive, the conclusion must *also* be contained in the instance.
If it is negative, the conclusion must *not* be contained in the instance.
By default, constraints are positive.

A concrete example for a specification of such constraints is as follows:
```json
{
    "constraints":[
        {
            "positive":false,
            "premise":{
                "component":"COMPA",
                "requiredInterfaces":{
                    "rif1":[
                        {
                            "component":"COMPB"
                        }
                    ]
                }
            },
            "conclusion":{
                "component":"COMPA",
                "requiredInterfaces":{
                    "rif2":[
                        {
                            "component":"COMPC"
                        }
                    ]
                }
            }
        },
        {
            "positive":true,
            "premise":{
                "component":"COMPA",
                "requiredInterfaces":{
                    "rif2":[
                        {
                            "component":"COMPD"
                        }
                    ]
                }
            },
            "conclusion":{
                "component":"COMPA",
                "requiredInterfaces":{
                    "rif1":[
                        {
                            "component":"COMPB"
                        }
                    ]
                }
            }
        }
    ]
}
```

## Loading and Serializing Component Descriptions and Repositories
The basis for all serialization and deserialization of component descriptions is the `ComponentSerialization` class.
It contains all relevant methods to load component repositories from files and serialize them again.
In particular, it allows to conveniently serialize component instances to JSON.

### Loading a Component Repository
```java
IComponentRepository repository = new ComponentSerialization().deserializeRepository(fileObjectToYourRepository);
```

One may wonder why the method is not static and why `ComponentSerialization` needs to be instantiated.
The reason is that `ComponentSerialization` is an `ILoggingCustomizable`, and we want to allow to customize the logger used during the parsing procedure.
Hence, the serialization and deserialization routines access an object-speficic logger.
This logger can also be set at instantiation time:

```java
IComponentRepository repository = new ComponentSerialization("myLoggerName").deserializeRepository(fileObject);
```

Since version `0.2.4`, it is possible to use *variables* in the component description files.
This can be very helpful if the definition of components depends on a concrete context.
A typical use case is where the number of required interfaces of a component may depend on the context in which it is used.
For example, in multi-label classification, based on the number of labels, we may have one required interface for each possible label, but this number is only known at runtime when receiving a concrete dataset.
The general syntax as described above is to use in the JSON specification variables of the form `{$varname}` and to add the variables `varname` to a `Map` object in Java, associating it with the value to be replaced with:
```java
Map<String, String> replacement = new HashMap<>();
replacement.put("numlabels", 4);
IComponentRepository repository = new ComponentSerialization().deserializeRepository(fileObject, replacement);
```
This will replace all ocurrences of `{$numlabels}` in the JSON file by `4`.

Note that the JSON tree is only parsed *after* the replacement.
This means that, when using variables, you do not need to have valid JSON in the original json file but you *must* replace the variables in a way such that after replacements the string is valid JSON.

### Loading a Numeric Parameter Refinement Map
For hierarchical configuration approaches, it is natural to not directly fix a parameter value but to refine it step by step.
However, it is necessary to specify in general how this refinement should look like, e.g. whether a parameter is to be refined on a log-scale or not.
Since such configurations are only meaningful for such hierarchical approaches but not for the configuration problem in general, we try to isolate them.

Our separation of component definition and parameter refinement maps is not entirely clean yet.
However, at least on the interface level, we already have this separation realized:
```java
INumericParameterRefinementConfigurationMap refinementDefinitions = new ComponentSerialization().deserializeParamMap(fileObjectToRefinementDefinition);
```
Currently, the file for parameter refinements is still the component repository itself, but this will be changed in the next version.

It is still subject to discussion whether parts of this description will be moved to the parameter description itself.
In this case, most likely the code associated with refinement will me moved from `jaicore-components` to solution specific repositories, here `hasco-core`.

### Serializing Component Instances
Component instances are potentially deeply nested structures, and we often might want to somehow visualize them, maybe in the form of JSON objects.
This can easily be achieved via a serialization:
```java
IComponentInstance ci = ...
JsonNode = new ComponentSerialization().serialize(ci);
```
The produced JSON will contain, for each contained (sub-) component instance the name of the component, the values of each paramter, and the component instances attached to the required interfaces.
The latter will be a *list* for each required interface, because since `0.2.4` required interfaces are generally treated as lists even when they can contain, by definition, only one element due to the restrictions in the definition of the respective component.

# Contribute
jaicore-experiments is currently developed in the [JAICore/{{ page.title }} folder of AILibs on github.com](https://github.com/starlibs/AILibs/tree/master/JAICore/jaicore-experiments).
