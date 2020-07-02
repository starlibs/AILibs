package ai.libs.hasco.builder;

import java.io.File;
import java.io.IOException;

import org.aeonbits.owner.ConfigCache;
import org.aeonbits.owner.ConfigFactory;
import org.api4.java.ai.graphsearch.problem.IOptimalPathInORGraphSearchFactory;
import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.common.attributedobjects.IObjectEvaluator;
import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.hasco.builder.forwarddecomposition.HASCOViaFDBuilder;
import ai.libs.hasco.core.HASCO;
import ai.libs.hasco.core.HASCOConfig;
import ai.libs.hasco.core.HASCOSolutionCandidate;
import ai.libs.hasco.core.reduction.planning2search.DefaultHASCOPlanningReduction;
import ai.libs.hasco.core.reduction.planning2search.IHASCOPlanningReduction;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.components.optimizingfactory.SoftwareConfigurationAlgorithmFactory;
import ai.libs.jaicore.planning.core.interfaces.IPlan;
import ai.libs.jaicore.planning.hierarchical.problems.ceocipstn.CEOCIPSTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.htn.IHierarchicalPlanningToGraphSearchReduction;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;

public abstract class HASCOBuilder<N, A, V extends Comparable<V>, B extends HASCOBuilder<N, A, V, B>>
implements SoftwareConfigurationAlgorithmFactory<RefinementConfiguredSoftwareConfigurationProblem<V>, HASCOSolutionCandidate<V>, V, HASCO<N, A, V>> {

	public enum Reduction {
		FORWARD
	};

	private final Class<V> scoreClass;
	private RefinementConfiguredSoftwareConfigurationProblem<V> problem;
	private IHASCOPlanningReduction<N, A> planningGraphGeneratorDeriver;
	private IOptimalPathInORGraphSearchFactory<IPathSearchWithPathEvaluationsInput<N, A, V>, EvaluatedSearchGraphPath<N, A, V>, N, A, V, ?> searchFactory;
	private HASCOConfig hascoConfig;

	public static HASCOViaFDBuilder<Double, ?> withForwardDecomposition() {
		return withForwardDecomposition(Double.class);
	}

	public HASCOBuilder(final Class<V> scoreClass) {
		this.scoreClass = scoreClass;
	}

	public static <V extends Comparable<V>> HASCOViaFDBuilder<V, ?> withForwardDecomposition(final Class<V> evaluationType) {
		return new HASCOViaFDBuilder<>(evaluationType);
	}

	public static HASCOViaFDBuilder<Double, ?> get(final Reduction reduction) {
		return get(reduction, Double.class);
	}

	public static <V extends Comparable<V>> HASCOViaFDBuilder<V, ?> get(final Reduction reduction, final Class<V> scoreClass) {
		switch (reduction) {
		case FORWARD:
			return withForwardDecomposition(scoreClass);
		default:
			throw new IllegalArgumentException();
		}
	}

	public static HASCOViaFDBuilder<Double, ?> get() {
		return get(Reduction.FORWARD);
	}

	public static HASCOViaFDBuilder<Double, ?> get(final RefinementConfiguredSoftwareConfigurationProblem<Double> problem) {
		HASCOViaFDBuilder<Double, ?> builder = get(Reduction.FORWARD);
		builder.withProblem(problem);
		return builder;

	}

	public HASCOBuilder(final HASCOBuilder<N, A, V, ?> builder) {
		this(builder.scoreClass);
		this.problem = builder.problem;
		this.planningGraphGeneratorDeriver = builder.planningGraphGeneratorDeriver;
		this.searchFactory = builder.searchFactory;
		this.hascoConfig = builder.hascoConfig;
	}

	@Override
	public HASCO<N, A, V> getAlgorithm() {
		this.requireThatProblemHasBeenDefined();
		return this.getAlgorithm(this.problem);
	}

	@Override
	public HASCO<N, A, V> getAlgorithm(final RefinementConfiguredSoftwareConfigurationProblem<V> problem) {
		if (problem.getRequiredInterface() == null || problem.getRequiredInterface().isEmpty()) {
			throw new IllegalArgumentException("No required interface defined!");
		}
		if (this.planningGraphGeneratorDeriver == null) {
			throw new IllegalStateException("Cannot create HASCO, because no planningGraphGeneratorDeriver has been specified.");
		}
		if (this.searchFactory == null) {
			throw new IllegalStateException("Cannot create HASCO, because no search factory has been specified.");
		}
		if (this.hascoConfig == null) {
			throw new IllegalStateException("Cannot create HASCO, because no hasco configuration been specified.");
		}
		return new HASCO<>(this.hascoConfig, problem, this.planningGraphGeneratorDeriver, this.searchFactory);
	}

	public IHASCOPlanningReduction<N, A> getPlanningGraphGeneratorDeriver() {
		return this.planningGraphGeneratorDeriver;
	}

	public void setProblemInput(final RefinementConfiguredSoftwareConfigurationProblem<V> problemInput) {
		this.problem = problemInput;
	}

	public B withPlanningGraphGeneratorDeriver(
			final IHierarchicalPlanningToGraphSearchReduction<N, A, ? super CEOCIPSTNPlanningProblem, ? extends IPlan, ? extends GraphSearchInput<N, A>, ? super ILabeledPath<N, A>> planningGraphGeneratorDeriver) {
		this.planningGraphGeneratorDeriver = (planningGraphGeneratorDeriver instanceof IHASCOPlanningReduction) ? (IHASCOPlanningReduction<N, A>) planningGraphGeneratorDeriver
				: new DefaultHASCOPlanningReduction<>(planningGraphGeneratorDeriver);
		return this.getSelf();
	}

	public IOptimalPathInORGraphSearchFactory<IPathSearchWithPathEvaluationsInput<N, A, V>, EvaluatedSearchGraphPath<N, A, V>, N, A, V, ?> getSearchFactory() {
		return this.searchFactory;
	}

	public void setSearchFactory(final IOptimalPathInORGraphSearchFactory<IPathSearchWithPathEvaluationsInput<N, A, V>, EvaluatedSearchGraphPath<N, A, V>, N, A, V, ?> searchFactory) {
		this.searchFactory = searchFactory;
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

	public B withProblem(final RefinementConfiguredSoftwareConfigurationProblem<V> problem) {
		this.setProblemInput(problem);
		return this.getSelf();
	}

	public B withProblem(final File componentFile, final String requiredInterface, final IObjectEvaluator<ComponentInstance, V> compositionEvaluator) throws IOException {
		return this.withProblem(new RefinementConfiguredSoftwareConfigurationProblem<>(componentFile, requiredInterface, compositionEvaluator));
	}

	public Class<V> getScoreClass() {
		return this.scoreClass;
	}

	protected void requireThatProblemHasBeenDefined() {
		if (this.problem == null) {
			throw new IllegalStateException("Configuration Problem has not been set!");
		}
	}

	public B getSelf() {
		return (B)this;
	}
}
