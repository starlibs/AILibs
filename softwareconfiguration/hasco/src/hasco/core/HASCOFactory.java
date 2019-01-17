package hasco.core;

import hasco.optimizingfactory.SoftwareConfigurationAlgorithmFactory;
import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.core.interfaces.IOptimalPathInORGraphSearchFactory;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class HASCOFactory<ISearch extends GraphSearchInput<N, A>, N, A, V extends Comparable<V>> implements SoftwareConfigurationAlgorithmFactory<RefinementConfiguredSoftwareConfigurationProblem<V>, HASCOSolutionCandidate<V>, V> {

	private RefinementConfiguredSoftwareConfigurationProblem<V> problem;
	private IHASCOPlanningGraphGeneratorDeriver<N, A> planningGraphGeneratorDeriver;
	private IOptimalPathInORGraphSearchFactory<ISearch, N, A, V, ?, ?> searchFactory;
	private AlgorithmProblemTransformer<GraphSearchWithPathEvaluationsInput<N, A, V>, ISearch> searchProblemTransformer;
	private boolean visualizationEnabled;

	@Override
	public <P> void setProblemInput(P problemInput, AlgorithmProblemTransformer<P, RefinementConfiguredSoftwareConfigurationProblem<V>> reducer) {
		setProblemInput(reducer.transform(problemInput));
	}

	@Override
	public void setProblemInput(RefinementConfiguredSoftwareConfigurationProblem<V> problemInput) {
		this.problem = problemInput;
	}

	@Override
	public HASCO<ISearch, N, A, V> getAlgorithm() {
		HASCO<ISearch, N, A, V> hasco = new HASCO<>(problem, planningGraphGeneratorDeriver, searchFactory, searchProblemTransformer);
		hasco.setVisualization(visualizationEnabled);
		return hasco;
	}

	public IHASCOPlanningGraphGeneratorDeriver<N, A> getPlanningGraphGeneratorDeriver() {
		return planningGraphGeneratorDeriver;
	}

	public void setPlanningGraphGeneratorDeriver(IHASCOPlanningGraphGeneratorDeriver<N, A> planningGraphGeneratorDeriver) {
		this.planningGraphGeneratorDeriver = planningGraphGeneratorDeriver;
	}

	public IOptimalPathInORGraphSearchFactory<ISearch, N, A, V, ?, ?> getSearchFactory() {
		return searchFactory;
	}

	public void setSearchFactory(IOptimalPathInORGraphSearchFactory<ISearch, N, A, V, ?, ?> searchFactory) {
		this.searchFactory = searchFactory;
	}

	public AlgorithmProblemTransformer<GraphSearchWithPathEvaluationsInput<N, A, V>, ISearch> getSearchProblemTransformer() {
		return searchProblemTransformer;
	}

	public void setSearchProblemTransformer(AlgorithmProblemTransformer<GraphSearchWithPathEvaluationsInput<N, A, V>, ISearch> searchProblemTransformer) {
		this.searchProblemTransformer = searchProblemTransformer;
	}

	public boolean isVisualizationEnabled() {
		return visualizationEnabled;
	}

	public void setVisualizationEnabled(boolean visualizationEnabled) {
		this.visualizationEnabled = visualizationEnabled;
	}
}
