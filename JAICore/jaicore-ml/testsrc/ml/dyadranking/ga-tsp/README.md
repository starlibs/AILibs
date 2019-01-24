# GA-TSP dataset for dyad ranking

GA-TSP is a meta-learning dataset which is about genetic algorithms (GA) applied on traveling salesman problem (TSP) instances.


## Description
The characteristics of both, the individual TSP problems and the individual GAs are described in terms of feature vectors. The features in combination with the (relative) performances can be used by methods that are able to predict the performances of
- (a) known GAs on new TSP instances
- (b) new GAs on known TSP instances or
- (c) new GAs on new TSP instances.

### Features

- GA: (Crossover Rate, Mutation Rate, CX, PMX, OX)

- TSP: (#Cities, Performance Landmarker 1, Performance Landmarker 2, Performance Landmarker 3)

More information can be found in
```
@inproceedings{SchaeferHuell2015a,
        author    = {Sch{\"a}fer, Dirk and H{\"u}llermeier, Eyke},
        title     = {Dyad Ranking using a Bilinear {P}lackett-{L}uce Model},
        booktitle = {Proceedings ECML/PKDD--2015, European Conference on Machine Learning and Knowledge Discovery in Databases},
        pages     = {227--242},
        address   = {Porto, Portugal},
        publisher = {Springer},
        year      = {2015}
}
```
