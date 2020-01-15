package ai.libs.jaicore.search.experiments;

import org.api4.java.ai.graphsearch.problem.IOptimalPathInORGraphSearch;
import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;

import ai.libs.jaicore.experiments.ExperimentDomain;
import ai.libs.jaicore.experiments.IExperimentBuilder;
import ai.libs.jaicore.experiments.IExperimentSetConfig;

public abstract class SearchExperimentDomain<B extends IExperimentBuilder, I extends IPathSearchWithPathEvaluationsInput<N, A, Double>, N, A>
extends ExperimentDomain<B, I, IOptimalPathInORGraphSearch<? extends I, ? extends IEvaluatedPath<N, A, Double>, N, A, Double>> {
	public SearchExperimentDomain(final IExperimentSetConfig config, final ISearchExperimentDecoder<N, A, I, IEvaluatedPath<N, A, Double>, IOptimalPathInORGraphSearch<? extends I, ? extends IEvaluatedPath<N, A, Double>, N, A, Double>> decoder) {
		super(config, decoder);
	}

	@Override
	public ISearchExperimentDecoder<N, A, I, IEvaluatedPath<N, A, Double>, IOptimalPathInORGraphSearch<? extends I, ? extends IEvaluatedPath<N, A, Double>, N, A, Double>> getDecoder() {
		return (ISearchExperimentDecoder<N, A, I, IEvaluatedPath<N, A, Double>, IOptimalPathInORGraphSearch<? extends I, ? extends IEvaluatedPath<N, A, Double>, N, A, Double>>)super.getDecoder();
	}
}
