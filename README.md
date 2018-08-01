# AILibs
AILibs is a collection of Java libraries related to automated decision making. It currently consists of two building blocks. It is also home of the current version of the AutoML-tool [ML-Plan](https://github.com/fmohr/AILibs/tree/master/softwareconfiguration/mlplan).

* **JAICore** (Java AI Core) is a collection of projects with basic general purpose AI algorithms mainly in the area of logic reasoning, heuristic search, and machine learning
* **softwareconfiguration** is a collection of projects related to automatically configuring software systems. Here we also maintain the code for our AutoML flagship **[ML-Plan](https://github.com/fmohr/AILibs/tree/master/softwareconfiguration/mlplan)**

## Setting up your IDE to work with AILibs
### Eclipse
Navigate to the folder where you cloned this repository and run
```
  ./gradlew eclipse
```
Then open Eclipse, and go to the import menu, e.g., in the package manager. Choose to import *Existing Projects into Workspace*, select the folder where you cloned the repository, and make sure to check the *Search for nested projects* option.
