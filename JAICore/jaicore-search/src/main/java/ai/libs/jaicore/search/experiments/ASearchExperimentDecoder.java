package ai.libs.jaicore.search.experiments;

import org.api4.java.ai.graphsearch.problem.IOptimalPathInORGraphSearch;
import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;

import ai.libs.jaicore.experiments.AExperimentDecoder;
import ai.libs.jaicore.experiments.IExperimentSetConfig;

public abstract class ASearchExperimentDecoder<N, A, I extends IPathSearchWithPathEvaluationsInput<N, A, Double>, O extends IEvaluatedPath<N, A, Double>, P extends IOptimalPathInORGraphSearch<? extends I, ? extends O, N, A, Double>> extends AExperimentDecoder<I, P>  implements ISearchExperimentDecoder<N, A, I, O, P> {

	public ASearchExperimentDecoder(final IExperimentSetConfig config) {
		super(config);
	}

}
