package hasco.core;

import hasco.optimizingfactory.SoftwareConfigurationAlgorithmFactory;
import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import jaicore.search.core.interfaces.IOptimalPathInORGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class HASCOFactory<S extends GraphSearchInput<N, A>, N, A, V extends Comparable<V>> implements SoftwareConfigurationAlgorithmFactory<RefinementConfiguredSoftwareConfigurationProblem<V>, HASCOSolutionCandidate<V>, V> {

	private RefinementConfiguredSoftwareConfigurationProblem<V> problem;
	private IHASCOPlanningGraphGeneratorDeriver<N, A> planningGraphGeneratorDeriver;
	private IOptimalPathInORGraphSearchFactory<S, N, A, V> searchFactory;
	private AlgorithmicProblemReduction<GraphSearchWithPathEvaluationsInput<N, A, V>, EvaluatedSearchGraphPath<N, A, V>, S, EvaluatedSearchGraphPath<N, A, V>> searchProblemTransformer;

	public void setProblemInput(final RefinementConfiguredSoftwareConfigurationProblem<V> problemInput) {
		this.problem = problemInput;
	}

	@Override
	public HASCO<S, N, A, V> getAlgorithm() {
		if (this.problem == null) {
			throw new IllegalStateException("Cannot create HASCO, because no problem has been specified.");
		}
		return this.getAlgorithm(this.problem);
	}

	@Override
	public HASCO<S, N, A, V> getAlgorithm(final RefinementConfiguredSoftwareConfigurationProblem<V> problem) {
		if (this.planningGraphGeneratorDeriver == null) {
			throw new IllegalStateException("Cannot create HASCO, because no planningGraphGeneratorDeriver has been specified.");
		}
		if (this.searchFactory == null) {
			throw new IllegalStateException("Cannot create HASCO, because no search factory has been specified.");
		}
		if (this.searchProblemTransformer == null) {
			throw new IllegalStateException("Cannot create HASCO, because no searchProblemTransformer has been specified.");
		}
		return new HASCO<>(problem, this.planningGraphGeneratorDeriver, this.searchFactory, this.searchProblemTransformer);
	}

	public IHASCOPlanningGraphGeneratorDeriver<N, A> getPlanningGraphGeneratorDeriver() {
		return this.planningGraphGeneratorDeriver;
	}

	public void setPlanningGraphGeneratorDeriver(final IHASCOPlanningGraphGeneratorDeriver<N, A> planningGraphGeneratorDeriver) {
		this.planningGraphGeneratorDeriver = planningGraphGeneratorDeriver;
	}

	public IOptimalPathInORGraphSearchFactory<S, N, A, V> getSearchFactory() {
		return this.searchFactory;
	}

	public void setSearchFactory(final IOptimalPathInORGraphSearchFactory<S, N, A, V> searchFactory) {
		this.searchFactory = searchFactory;
	}

	public AlgorithmicProblemReduction<GraphSearchWithPathEvaluationsInput<N, A, V>, EvaluatedSearchGraphPath<N, A, V>, S, EvaluatedSearchGraphPath<N, A, V>> getSearchProblemTransformer() {
		return this.searchProblemTransformer;
	}

	public void setSearchProblemTransformer(final AlgorithmicProblemReduction<GraphSearchWithPathEvaluationsInput<N, A, V>, EvaluatedSearchGraphPath<N, A, V>, S, EvaluatedSearchGraphPath<N, A, V>> searchProblemTransformer) {
		this.searchProblemTransformer = searchProblemTransformer;
	}

	public RefinementConfiguredSoftwareConfigurationProblem<V> getProblem() {
		return this.problem;
	}
}
