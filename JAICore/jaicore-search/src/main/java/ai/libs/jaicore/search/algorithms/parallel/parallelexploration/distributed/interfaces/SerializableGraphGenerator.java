package ai.libs.jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces;

import java.io.Serializable;

import ai.libs.jaicore.search.core.interfaces.GraphGenerator;

public interface SerializableGraphGenerator<T,A> extends GraphGenerator<T, A>, Serializable {

}
