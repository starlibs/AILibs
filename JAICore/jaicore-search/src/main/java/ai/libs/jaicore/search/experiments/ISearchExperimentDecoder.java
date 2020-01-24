package ai.libs.jaicore.search.experiments;

import org.api4.java.ai.graphsearch.problem.IOptimalPathInORGraphSearch;
import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;

import ai.libs.jaicore.experiments.IExperimentDecoder;

public interface ISearchExperimentDecoder<N, A, I extends IPathSearchWithPathEvaluationsInput<N, A, Double>, O extends IEvaluatedPath<N, A, Double>, P extends IOptimalPathInORGraphSearch<? extends I, ? extends O, N, A, Double>> extends IExperimentDecoder<I, P> {

}
