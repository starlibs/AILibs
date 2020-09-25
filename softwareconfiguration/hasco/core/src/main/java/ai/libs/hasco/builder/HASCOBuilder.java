package ai.libs.hasco.builder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.aeonbits.owner.ConfigCache;
import org.aeonbits.owner.ConfigFactory;
import org.api4.java.ai.graphsearch.problem.IOptimalPathInORGraphSearchFactory;
import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.algorithm.Timeout;
import org.api4.java.common.attributedobjects.IObjectEvaluator;
import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.hasco.builder.forwarddecomposition.HASCOViaFDBuilder;
import ai.libs.hasco.core.HASCO;
import ai.libs.hasco.core.HASCOConfig;
import ai.libs.hasco.core.HASCOSolutionCandidate;
import ai.libs.hasco.core.HASCOUtil;
import ai.libs.hasco.core.reduction.planning2search.DefaultHASCOPlanningReduction;
import ai.libs.hasco.core.reduction.planning2search.IHASCOPlanningReduction;
import ai.libs.jaicore.basic.IOwnerBasedAlgorithmConfig;
import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.api.INumericParameterRefinementConfigurationMap;
import ai.libs.jaicore.components.api.IRequiredInterfaceDefinition;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.components.model.SoftwareConfigurationProblem;
import ai.libs.jaicore.components.optimizingfactory.SoftwareConfigurationAlgorithmFactory;
import ai.libs.jaicore.planning.core.interfaces.IPlan;
import ai.libs.jaicore.planning.hierarchical.problems.ceocipstn.CEOCIPSTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.htn.IHierarchicalPlanningToGraphSearchReduction;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;

/**
 *
 * @author Felix Mohr
 *
 * @param <N> Type of nodes
 * @param <A> Type of arcs
 * @param <V> Type of Node scores
 * @param <B> Type of the builder (for chaining)
 */
public abstract class HASCOBuilder<N, A, V extends Comparable<V>, B extends HASCOBuilder<N, A, V, B>>
implements SoftwareConfigurationAlgorithmFactory<RefinementConfiguredSoftwareConfigurationProblem<V>, HASCOSolutionCandidate<V>, V, HASCO<N, A, V>> {

	public enum Reduction {
		FORWARD
	}

	private final Class<V> scoreClass;

	/* problem configuration */
	private Collection<IComponent> components;
	private String requiredInterface;
	private IObjectEvaluator<IComponentInstance, V> evaluator;
	private INumericParameterRefinementConfigurationMap paramRefinementConfig;
	private RefinementConfiguredSoftwareConfigurationProblem<V> problem;

	private IHASCOPlanningReduction<N, A> planningGraphGeneratorDeriver;
	private IOptimalPathInORGraphSearchFactory<IPathSearchWithPathEvaluationsInput<N, A, V>, EvaluatedSearchGraphPath<N, A, V>, N, A, V, ?> searchFactory;
	private HASCOConfig hascoConfig;

	public static HASCOViaFDBuilder<Double, ?> withForwardDecomposition() {
		return withForwardDecomposition(Double.class);
	}

	public HASCOBuilder(final Class<V> scoreClass) {
		this.scoreClass = scoreClass;
		this.withDefaultAlgorithmConfig();
	}

	public static <V extends Comparable<V>> HASCOViaFDBuilder<V, ?> withForwardDecomposition(final Class<V> evaluationType) {
		return new HASCOViaFDBuilder<>(evaluationType);
	}

	public static HASCOViaFDBuilder<Double, ?> get(final Reduction reduction) {
		return get(reduction, Double.class);
	}

	public static <V extends Comparable<V>> HASCOViaFDBuilder<V, ?> get(final Reduction reduction, final Class<V> scoreClass) {
		if (reduction == Reduction.FORWARD) {
			return withForwardDecomposition(scoreClass);
		}
		throw new IllegalArgumentException("Currently only support for forward decomposition.");
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
		for (IComponent c : problemInput.getComponents()) {
			for (IRequiredInterfaceDefinition ri : c.getRequiredInterfaces()) {
				if (!ri.isOrdered()) {
					throw new IllegalArgumentException("HASCO does currently not support non-ordered required-interfaces of components, but required interface \"" + ri.getId() + "\" of component \"" + c.getName() + "\" is not ordered!");
				}
			}
		}
		this.problem = problemInput;
	}

	@SuppressWarnings("unchecked")
	public B withPlanningGraphGeneratorDeriver(
			final IHierarchicalPlanningToGraphSearchReduction<N, A, ? super CEOCIPSTNPlanningProblem, ? extends IPlan, ? extends GraphSearchInput<N, A>, ? super ILabeledPath<N, A>> planning2searchReduction) {
		this.planningGraphGeneratorDeriver = (planning2searchReduction instanceof IHASCOPlanningReduction) ? (IHASCOPlanningReduction<N, A>) planning2searchReduction : new DefaultHASCOPlanningReduction<>(planning2searchReduction);
		return this.getSelf();
	}

	public IOptimalPathInORGraphSearchFactory<IPathSearchWithPathEvaluationsInput<N, A, V>, EvaluatedSearchGraphPath<N, A, V>, N, A, V, ?> getSearchFactory() {
		return this.searchFactory;
	}

	public B withSearchFactory(final IOptimalPathInORGraphSearchFactory<IPathSearchWithPathEvaluationsInput<N, A, V>, EvaluatedSearchGraphPath<N, A, V>, N, A, V, ?> searchFactory) {
		this.searchFactory = searchFactory;
		return this.getSelf();
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

	public HASCOConfig getHascoConfig() {
		return this.hascoConfig;
	}

	public RefinementConfiguredSoftwareConfigurationProblem<V> getProblem() {
		return this.problem;
	}

	public B withProblem(final RefinementConfiguredSoftwareConfigurationProblem<V> problem) {
		this.setProblemInput(problem);
		this.components = problem.getComponents();
		this.evaluator = problem.getCompositionEvaluator();
		this.requiredInterface = problem.getRequiredInterface();
		this.paramRefinementConfig = problem.getParamRefinementConfig();
		return this.getSelf();
	}

	public B withProblem(final File componentFile, final String requiredInterface, final IObjectEvaluator<IComponentInstance, V> compositionEvaluator) throws IOException {
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

	public Collection<IComponent> getComponents() {
		return this.components;
	}

	public B withComponents(final Collection<? extends IComponent> components) {
		this.components = new ArrayList<>(components);
		this.compileProblemIfPossible();
		return this.getSelf();
	}

	public String getRequiredInterface() {
		return this.requiredInterface;
	}

	public B withRequiredInterface(final String requiredInterface) {
		this.requiredInterface = requiredInterface;
		this.compileProblemIfPossible();
		return this.getSelf();
	}

	public IObjectEvaluator<IComponentInstance, V> getEvaluator() {
		return this.evaluator;
	}

	public B withEvaluator(final IObjectEvaluator<IComponentInstance, V> evaluator) {
		this.evaluator = evaluator;
		this.compileProblemIfPossible();
		return this.getSelf();
	}

	public INumericParameterRefinementConfigurationMap getParamRefinementConfig() {
		return this.paramRefinementConfig;
	}

	public B withParamRefinementConfig(final INumericParameterRefinementConfigurationMap paramRefinementConfig) {
		this.paramRefinementConfig = paramRefinementConfig;
		this.compileProblemIfPossible();
		return this.getSelf();
	}

	private void compileProblemIfPossible() {
		if (this.components != null && this.requiredInterface != null && this.paramRefinementConfig != null && this.evaluator != null) {
			SoftwareConfigurationProblem<V> coreProblem = new SoftwareConfigurationProblem<>(this.components, this.requiredInterface, this.evaluator);
			this.problem = new RefinementConfiguredSoftwareConfigurationProblem<>(coreProblem, this.paramRefinementConfig);
		}
	}

	public IPathSearchInput<N, A> getGraphSearchInput() {
		if (this.components == null) {
			throw new IllegalStateException("Cannot create graph search input; no components defined yet.");
		}
		if (this.requiredInterface == null) {
			throw new IllegalStateException("Cannot create graph search input; no required interface defined yet.");
		}
		if (this.paramRefinementConfig == null) {
			throw new IllegalStateException("Cannot create graph search input; no param refinement config defined yet.");
		}
		if (this.planningGraphGeneratorDeriver == null) {
			throw new IllegalStateException("Cannot create graph search input; no reduction from planning to graph search defined yet.");
		}
		return HASCOUtil.getSearchProblem(this.components, this.requiredInterface, this.paramRefinementConfig, this.planningGraphGeneratorDeriver);
	}

	public B withTimeout(final Timeout to) {
		this.hascoConfig.setProperty(IOwnerBasedAlgorithmConfig.K_TIMEOUT, "" + to.milliseconds());
		return this.getSelf();
	}

	public B withCPUs(final int numCPUs) {
		this.hascoConfig.setProperty(IOwnerBasedAlgorithmConfig.K_CPUS, "" + numCPUs);
		return this.getSelf();
	}

	@SuppressWarnings("unchecked")
	public B getSelf() {
		return (B) this;
	}
}
