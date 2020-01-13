package ai.libs.hasco.core;

import java.io.File;

import org.aeonbits.owner.ConfigCache;
import org.aeonbits.owner.ConfigFactory;
import org.api4.java.ai.graphsearch.problem.IOptimalPathInORGraphSearchFactory;
import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.hasco.optimizingfactory.SoftwareConfigurationAlgorithmFactory;
import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.planning.core.interfaces.IPlan;
import ai.libs.jaicore.planning.hierarchical.problems.ceocipstn.CEOCIPSTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.htn.IHierarchicalPlanningToGraphSearchReduction;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class HASCOFactory<S extends GraphSearchWithPathEvaluationsInput<N, A, V>, N, A, V extends Comparable<V>> implements SoftwareConfigurationAlgorithmFactory<RefinementConfiguredSoftwareConfigurationProblem<V>, HASCOSolutionCandidate<V>, V, HASCO<S, N, A, V>> {

	private RefinementConfiguredSoftwareConfigurationProblem<V> problem;
	private IHASCOPlanningReduction<N, A> planningGraphGeneratorDeriver;
	private IOptimalPathInORGraphSearchFactory<S, EvaluatedSearchGraphPath<N, A, V>, N, A, V, ?> searchFactory;
	private AlgorithmicProblemReduction<? super GraphSearchWithPathEvaluationsInput<N, A, V>, ? super EvaluatedSearchGraphPath<N, A, V>, S, EvaluatedSearchGraphPath<N, A, V>> searchProblemTransformer;
	private HASCOConfig hascoConfig;

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
		if (problem.getRequiredInterface() == null || problem.getRequiredInterface().isEmpty()) {
			throw new IllegalArgumentException("No required interface defined!");
		}
		if (this.planningGraphGeneratorDeriver == null) {
			throw new IllegalStateException("Cannot create HASCO, because no planningGraphGeneratorDeriver has been specified.");
		}
		if (this.searchFactory == null) {
			throw new IllegalStateException("Cannot create HASCO, because no search factory has been specified.");
		}
		if (this.searchProblemTransformer == null) {
			throw new IllegalStateException("Cannot create HASCO, because no searchProblemTransformer has been specified.");
		}
		if (this.hascoConfig == null) {
			throw new IllegalStateException("Cannot create HASCO, because no hasco configuration been specified.");
		}
		return new HASCO<>(this.hascoConfig, problem, this.planningGraphGeneratorDeriver, this.searchFactory, this.searchProblemTransformer);
	}

	public IHASCOPlanningReduction<N, A> getPlanningGraphGeneratorDeriver() {
		return this.planningGraphGeneratorDeriver;
	}

	public void setPlanningGraphGeneratorDeriver(final IHierarchicalPlanningToGraphSearchReduction<N, A, ? super CEOCIPSTNPlanningProblem, ? extends IPlan, ? extends GraphSearchInput<N,A>, ? super ILabeledPath<N,A>> planningGraphGeneratorDeriver) {
		this.planningGraphGeneratorDeriver = (planningGraphGeneratorDeriver instanceof IHASCOPlanningReduction) ? (IHASCOPlanningReduction<N, A>)planningGraphGeneratorDeriver : new DefaultHASCOPlanningReduction<>(planningGraphGeneratorDeriver);
	}

	public IOptimalPathInORGraphSearchFactory<S, EvaluatedSearchGraphPath<N, A, V>, N, A, V, ?> getSearchFactory() {
		return this.searchFactory;
	}

	public void setSearchFactory(final IOptimalPathInORGraphSearchFactory<S, EvaluatedSearchGraphPath<N, A, V>, N, A, V, ?> searchFactory) {
		this.searchFactory = searchFactory;
	}

	public AlgorithmicProblemReduction<? super GraphSearchWithPathEvaluationsInput<N, A, V>, ? super EvaluatedSearchGraphPath<N, A, V>, S, EvaluatedSearchGraphPath<N, A, V>> getSearchProblemTransformer() {
		return this.searchProblemTransformer;
	}

	public void setSearchProblemTransformer(final AlgorithmicProblemReduction<? super GraphSearchWithPathEvaluationsInput<N, A, V>, ? super EvaluatedSearchGraphPath<N, A, V>, S, EvaluatedSearchGraphPath<N, A, V>> searchProblemTransformer) {
		this.searchProblemTransformer = searchProblemTransformer;
	}

	public void withDefaultAlgorithmConfig() {
		this.withAlgorithmConfig(ConfigCache.getOrCreate(HASCOConfig.class));
	}

	public void withAlgorithmConfig(final HASCOConfig hascoConfig) {
		this.hascoConfig = hascoConfig;
	}

	public void withAlgorithmConfigFile(final File hascoConfigFile) {
		this.hascoConfig = (HASCOConfig) ConfigFactory.create(HASCOConfig.class).loadPropertiesFromFile(hascoConfigFile);
	}

	public RefinementConfiguredSoftwareConfigurationProblem<V> getProblem() {
		return this.problem;
	}
}
