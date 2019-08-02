package ai.libs.jaicore.search.core.interfaces;

import org.api4.java.ai.graphsearch.problem.IGraphSearch;
import org.api4.java.ai.graphsearch.problem.IGraphSearchFactory;
import org.api4.java.ai.graphsearch.problem.IGraphSearchInput;

import ai.libs.jaicore.basic.algorithm.AAlgorithmFactory;

public abstract class StandardORGraphSearchFactory<I extends IGraphSearchInput<N, A>, O, N, A, V extends Comparable<V>, A2 extends IGraphSearch<I, O, N, A>> extends AAlgorithmFactory<I, O, A2> implements IGraphSearchFactory<I, O, N, A, A2> {

}
