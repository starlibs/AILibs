package ai.libs.jaicore.search.core.interfaces;

import org.api4.java.ai.graphsearch.problem.IPathSearch;
import org.api4.java.ai.graphsearch.problem.IPathSearchFactory;

import ai.libs.jaicore.basic.algorithm.AAlgorithmFactory;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;

public abstract class StandardORGraphSearchFactory<I extends GraphSearchInput<N, A>, O, N, A, V extends Comparable<V>, A2 extends IPathSearch<I, O, N, A>> extends AAlgorithmFactory<I, O, A2> implements IPathSearchFactory<I, O, N, A, A2> {

}
