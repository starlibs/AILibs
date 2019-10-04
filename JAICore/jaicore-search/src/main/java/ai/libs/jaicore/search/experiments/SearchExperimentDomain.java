package ai.libs.jaicore.search.experiments;

import org.api4.java.ai.graphsearch.problem.IGraphSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.IOptimalPathInORGraphSearch;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;

import ai.libs.jaicore.experiments.ExperimentDomain;
import ai.libs.jaicore.experiments.IExperimentBuilder;
import ai.libs.jaicore.experiments.IExperimentDecoder;
import ai.libs.jaicore.experiments.IExperimentSetConfig;

public abstract class SearchExperimentDomain<B extends IExperimentBuilder, I extends IGraphSearchWithPathEvaluationsInput<N, A, Double>, N, A>
extends ExperimentDomain<B, I, IOptimalPathInORGraphSearch<? extends I, ? extends IEvaluatedPath<N, A, Double>, N, A, Double>> {
	public SearchExperimentDomain(final IExperimentSetConfig config, final IExperimentDecoder<I, IOptimalPathInORGraphSearch<? extends I, ? extends IEvaluatedPath<N, A, Double>, N, A, Double>> decoder) {
		super(config, decoder);
	}
}
