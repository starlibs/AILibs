package hasco.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import hasco.core.HASCO.HASCOSolutionIterator;
import hasco.core.HASCOFDWithParameterPruning.TFDSearchSpaceUtilFactory;
import hasco.events.HASCORunStartedEvent;
import hasco.events.HASCORunTerminatedEvent;
import hasco.events.HASCOSolutionEvaluationEvent;
import hasco.knowledgebase.IParameterImportanceEstimator;
import hasco.knowledgebase.FANOVAParameterImportanceEstimator;
import hasco.knowledgebase.PerformanceKnowledgeBase;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.NumericParameterDomain;
import hasco.model.Parameter;
import hasco.model.ParameterRefinementConfiguration;
import hasco.query.Factory;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.IObjectEvaluator;
import jaicore.basic.SQLAdapter;
import jaicore.graph.IObservableGraphAlgorithm;
import jaicore.logging.LoggerUtil;
import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.LiteralParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;
import jaicore.logic.fol.theories.EvaluablePredicate;
import jaicore.planning.algorithms.IHTNPlanningAlgorithm;
import jaicore.planning.algorithms.IObservableGraphBasedHTNPlanningAlgorithm;
import jaicore.planning.algorithms.IObservableGraphBasedHTNPlanningAlgorithmFactory;
import jaicore.planning.algorithms.IPlanningSolution;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionHTNPlannerFactory;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.model.ceoc.CEOCOperation;
import jaicore.planning.model.core.Action;
import jaicore.planning.model.core.Operation;
import jaicore.planning.model.core.PlannerUtil;
import jaicore.planning.model.task.ceocipstn.CEOCIPSTNPlanningDomain;
import jaicore.planning.model.task.ceocipstn.CEOCIPSTNPlanningProblem;
import jaicore.planning.model.task.ceocipstn.OCIPMethod;
import jaicore.planning.model.task.stn.Method;
import jaicore.planning.model.task.stn.TaskNetwork;
import jaicore.search.algorithms.interfaces.IObservableORGraphSearchFactory;
import jaicore.search.algorithms.interfaces.ISolutionEvaluator;
import jaicore.search.algorithms.standard.bestfirst.RandomCompletionEvaluator;
import jaicore.search.algorithms.standard.core.AlternativeNodeEvaluator;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.structure.core.GraphGenerator;

/**
 * Hierarchically create an object of type T
 *
 * @author fmohr
 *
 * @param <T>
 */
public class HASCOWithParameterPruning<T, N, A, V extends Comparable<V>, R extends IPlanningSolution>
		implements Iterable<Solution<R, T, V>>, IObservableGraphAlgorithm<N, A>, ILoggingCustomizable {

	// component selection
	private static final String RESOLVE_COMPONENT_IFACE_PREFIX = "1_tResolve";
	private static final String SATISFY_PREFIX = "1_satisfy";

	// component configuration
	private static final String REFINE_PARAMETERS_PREFIX = "2_tRefineParamsOf";
	private static final String REFINE_PARAMETER_PREFIX = "2_tRefineParam";
	private static final String DECLARE_CLOSED_PREFIX = "2_declareClosed";
	private static final String REDEF_CLOSED_PREFIX = "2_satisfy";
	private static final String REDEF_VALUE_PREFIX = "2_redefValue";

	/* domain description */
	private final Collection<Component> components;
	private final Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramRefinementConfig;
	private Factory<? extends T> factory;

	/* query */
	private final String nameOfRequiredInterface;
	private final IObjectEvaluator<T, V> benchmark;

	/* search algorithm configuration */
	private final IObservableGraphBasedHTNPlanningAlgorithmFactory<R, N, A, V> plannerFactory;
	private final IObservableORGraphSearchFactory<N, A, V> searchFactory;
	private final IHASCOSearchSpaceUtilFactory<N, A, V> searchSpaceUtilFactory;
	private final RandomCompletionEvaluator<N, V> randomCompletionEvaluator;
	private INodeEvaluator<N, V> preferredNodeEvaluator;

	/* event buses for evaluation events */
	private final EventBus solutionEvaluationEventBus = new EventBus();

	/* parameters relevant for functionality */
	private int timeout;
	private int numberOfCPUs = 1;
	private int randomSeed;
	private boolean configureParams = true;

	/* parameters for state of a single run */
	private T bestRecognizedSolution;
	private ComponentInstance compositionOfBestRecognizedSolution;
	private V scoreOfBestRecognizedSolution;

	/* logging */
	private Logger logger = LoggerFactory.getLogger(HASCOWithParameterPruning.class);
	private String loggerName;

	/* run-specific options */
	private final HASCOProblemReductionWithParameterPruning reduction;
	private final CEOCIPSTNPlanningProblem problem;
	private IObservableGraphBasedHTNPlanningAlgorithm<R, N, A, V> planner;

	private ISolutionEvaluator<N, V> solutionEvaluator = new ISolutionEvaluator<N, V>() {
		@Override
		public V evaluateSolution(final List<N> solutionPath) throws Exception {
			List<Action> plan = HASCOWithParameterPruning.this.searchSpaceUtilFactory.getPathToPlanConverter()
					.getPlan(solutionPath);
			ComponentInstance composition = Util.getSolutionCompositionForPlan(
					HASCOWithParameterPruning.this.components, reduction.getInitState(), plan);
			T solution = HASCOWithParameterPruning.this.getObjectFromPlan(plan);
			V scoreOfSolution = HASCOWithParameterPruning.this.benchmark.evaluate(solution);
			if (HASCOWithParameterPruning.this.scoreOfBestRecognizedSolution == null
					|| HASCOWithParameterPruning.this.scoreOfBestRecognizedSolution.compareTo(scoreOfSolution) > 0) {
				HASCOWithParameterPruning.this.bestRecognizedSolution = solution;
				HASCOWithParameterPruning.this.compositionOfBestRecognizedSolution = composition;
				HASCOWithParameterPruning.this.scoreOfBestRecognizedSolution = scoreOfSolution;
			}
			HASCOWithParameterPruning.this.solutionEvaluationEventBus
					.post(new HASCOSolutionEvaluationEvent<>(composition, solution, scoreOfSolution));
			return scoreOfSolution;
		}

		@Override
		public boolean doesLastActionAffectScoreOfAnySubsequentSolution(final List<N> partialSolutionPath) {
			return true;
		}
	};

	/* list of listeners */
	private final Collection<Object> listeners = new ArrayList<>();

	/* benchmark name */
	private final String benchmarkName = "test";

	/* performance knowledge base */
	private final PerformanceKnowledgeBase performanceKB;

	/* parameter importance estimator */
	private final IParameterImportanceEstimator parameterImportanceEstimator;

	/* threshold below which parameters are estimated to be unimportant */
	private final double importanceThreshold;

	/*
	 * minimum number of performance samples gathered before importance estimation
	 * takes place
	 */
	private final int minNumSamplesForImportanceEstimation;

	/*
	 * flag indicating whether to use parameter importance estimation and pruning of
	 * unimportant parameters
	 */
	private final boolean useParameterImportanceEstimation;

	/* SQL adapter for performance samples */
	// private final SQLAdapter adapter = new SQLAdapter("localhost", "jonas",
	// "password", "mlplan_test");

	// public HASCOJ(ForwardDecompositionHTNPlannerFactory
	// forwardDecompositionHTNPlannerFactory,
	// IObservableORGraphSearchFactory<TFDNode, String, Double> searchFactory2,
	// TFDSearchSpaceUtilFactory tfdSearchSpaceUtilFactory, INodeEvaluator<TFDNode,
	// Double> nodeEvaluator2,
	// Factory<? extends T> converter, String nameOfRequiredInterface2,
	// IObjectEvaluator<T, Double> benchmark2,
	// int i, int j, boolean b) {
	// // TODO Auto-generated constructor stub
	// }

	public HASCOWithParameterPruning(final Collection<Component> components,
			final Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramRefinementConfig,
			final IObservableGraphBasedHTNPlanningAlgorithmFactory<R, N, A, V> plannerFactory,
			final IObservableORGraphSearchFactory<N, A, V> searchFactory,
			final IHASCOSearchSpaceUtilFactory<N, A, V> searchSpaceUtilFactory, final Factory<? extends T> factory,
			final String nameOfRequiredInterface, final IObjectEvaluator<T, V> benchmark,
			final double importanceThreshold, final int minNumSamplesForImportanceEstimation,
			final boolean useParameterImportanceEstimation) {
		super();
		this.components = components;
		this.paramRefinementConfig = paramRefinementConfig;
		this.plannerFactory = plannerFactory;
		this.searchFactory = searchFactory;
		this.benchmark = benchmark;
		this.performanceKB = new PerformanceKnowledgeBase();
		this.parameterImportanceEstimator = new FANOVAParameterImportanceEstimator(performanceKB, benchmarkName);
		this.importanceThreshold = importanceThreshold;
		this.minNumSamplesForImportanceEstimation = minNumSamplesForImportanceEstimation;
		this.useParameterImportanceEstimation = useParameterImportanceEstimation;
		System.out.println("Importance Threshold in HASCO: " + this.importanceThreshold);
		// this.performanceKB.initializeDBTables();
		this.randomCompletionEvaluator = new RandomCompletionEvaluator<>(new Random(this.randomSeed), 1,
				searchSpaceUtilFactory.getPathUnifier(), new ISolutionEvaluator<N, V>() {
					@Override
					public V evaluateSolution(final List<N> solutionPath) throws Exception {
						List<Action> plan = HASCOWithParameterPruning.this.searchSpaceUtilFactory
								.getPathToPlanConverter().getPlan(solutionPath);
						ComponentInstance composition = Util.getSolutionCompositionForPlan(
								HASCOWithParameterPruning.this.components,
								HASCOWithParameterPruning.this.reduction.getInitState(), plan);
						T solution = HASCOWithParameterPruning.this.getObjectFromPlan(plan);
						V scoreOfSolution = benchmark.evaluate(solution);
						// TODO is this cast feasible?
						double score = (double) scoreOfSolution;
						String identifier = Util.getComponentNamesOfComposition(composition);
						System.out.println("add performance sample");
						performanceKB.addPerformanceSample(benchmarkName, composition, score, false);
						System.out.println("Using importance estimation: " + useParameterImportanceEstimation);
						if (HASCOWithParameterPruning.this.scoreOfBestRecognizedSolution == null
								|| HASCOWithParameterPruning.this.scoreOfBestRecognizedSolution
										.compareTo(scoreOfSolution) > 0) {
							HASCOWithParameterPruning.this.bestRecognizedSolution = solution;
							HASCOWithParameterPruning.this.compositionOfBestRecognizedSolution = composition;
							HASCOWithParameterPruning.this.scoreOfBestRecognizedSolution = scoreOfSolution;
						}
						HASCOWithParameterPruning.this.solutionEvaluationEventBus
								.post(new HASCOSolutionEvaluationEvent<>(composition, solution, scoreOfSolution));
						return scoreOfSolution;
					}

					@Override
					public boolean doesLastActionAffectScoreOfAnySubsequentSolution(final List<N> partialSolutionPath) {
						return true;
					}
				});
		this.factory = factory;
		this.searchSpaceUtilFactory = searchSpaceUtilFactory;
		this.nameOfRequiredInterface = nameOfRequiredInterface;

		/* set run specific options */
		// reduction = new HASCOProblemReductionWithParameterPruning(components,
		// paramRefinementConfig, nameOfRequiredInterface, true,
		// this.parameterImportanceEstimator,this.importanceThreshold,
		// this.minNumSamplesForImportanceEstimation, this.performanceKB,true)));
		reduction = new HASCOProblemReductionWithParameterPruning(components, paramRefinementConfig,
				nameOfRequiredInterface, true, parameterImportanceEstimator, importanceThreshold, minNumSamplesForImportanceEstimation,
				performanceKB, useParameterImportanceEstimation);

		this.problem = reduction.getPlanningProblem();
		if (logger.isDebugEnabled()) {
			StringBuilder opSB = new StringBuilder();
			for (Operation op : problem.getDomain().getOperations()) {
				opSB.append("\n\t\t");
				opSB.append(op);
			}
			StringBuilder methodSB = new StringBuilder();
			for (Method method : problem.getDomain().getMethods()) {
				methodSB.append("\n\t\t");
				methodSB.append(method);
			}
			logger.debug(
					"The HTN problem created by HASCO is defined as follows:\n\tInit State: {}\n\tOperations:{}\n\tMethods:{}",
					reduction.getInitState(), opSB.toString(), methodSB.toString());
		}
	}

	public void setNumberOfRandomCompletions(final int randomCompletions) {
		this.randomCompletionEvaluator.setNumberOfRandomCompletions(randomCompletions);
	}

	public int getRandom() {
		return this.randomSeed;
	}

	public ISolutionEvaluator<N, V> getSolutionEvaluator() {
		return this.solutionEvaluator;
	}

	public IObservableORGraphSearchFactory<N, A, V> getSearchFactory() {
		return this.searchFactory;
	}

	public class HASCOSolutionIterator implements Iterator<Solution<R, T, V>> {

		private boolean isInitialized = false;
		private Iterator<R> planIterator;
		private boolean canceled = false;

		private HASCOSolutionIterator() {

			/* set node evaluator based on the currently defined preferred node evaluator */
			INodeEvaluator<N, V> nodeEvaluator = preferredNodeEvaluator == null ? randomCompletionEvaluator
					: new AlternativeNodeEvaluator<>(preferredNodeEvaluator, randomCompletionEvaluator);
			HASCOWithParameterPruning.this.planner = HASCOWithParameterPruning.this.plannerFactory.newAlgorithm(
					HASCOWithParameterPruning.this.problem, HASCOWithParameterPruning.this.searchFactory, nodeEvaluator,
					HASCOWithParameterPruning.this.numberOfCPUs);
			this.planIterator = planner.iterator();
		}

		@Override
		public boolean hasNext() {
			if (!this.isInitialized) {
				HASCOWithParameterPruning.this.logger.info("Starting HASCOWithParameterPruning run.");

				/* check whether there is a refinement config for each numeric parameter */
				for (Component c : HASCOWithParameterPruning.this.components) {
					for (Parameter p : c.getParameters()) {
						if (p.isNumeric() && (!HASCOWithParameterPruning.this.paramRefinementConfig.containsKey(c)
								|| !HASCOWithParameterPruning.this.paramRefinementConfig.get(c).containsKey(p))) {
							throw new IllegalArgumentException(
									"No refinement config was delivered for numeric parameter " + p.getName()
											+ " of component " + c.getName());
						}
					}
				}

				/* updating logger name of the planner */
				if (HASCOWithParameterPruning.this.loggerName != null
						&& HASCOWithParameterPruning.this.loggerName.length() > 0
						&& planner instanceof ILoggingCustomizable) {
					logger.info("Setting logger name of {} to {}", planner, loggerName + ".planner");
					((ILoggingCustomizable) planner).setLoggerName(loggerName + ".planner");
				}

				/* register listeners */
				synchronized (listeners) {
					listeners.forEach(l -> ((IObservableGraphAlgorithm<?, ?>) planner).registerListener(l));
				}
				solutionEvaluationEventBus.post(new HASCORunStartedEvent<>(HASCOWithParameterPruning.this.randomSeed,
						HASCOWithParameterPruning.this.timeout, HASCOWithParameterPruning.this.numberOfCPUs,
						HASCOWithParameterPruning.this.benchmark));
				this.isInitialized = true;
			}
			if (this.canceled) {
				throw new IllegalStateException(
						"HASCOWithParameterPruning has already been canceled. Cannot compute more solutions.");
			}

			HASCOWithParameterPruning.this.logger.info(
					"Now asking the planning algorithm iterator {} whether there is a next solution.",
					this.planIterator.getClass().getName());
			return this.planIterator.hasNext();
		}

		@Override
		public Solution<R, T, V> next() {

			/* derive a map of ground components */
			R plan = this.planIterator.next();
			Map<String, Object> solutionAnnotations = planner.getAnnotationsOfSolution(plan);
			ComponentInstance objectInstance = Util.getSolutionCompositionForPlan(
					HASCOWithParameterPruning.this.components, reduction.getInitState(), plan.getPlan());
			@SuppressWarnings("unchecked")
			Solution<R, T, V> solution = new Solution<>(objectInstance, plan,
					HASCOWithParameterPruning.this.getObjectFromPlan(plan.getPlan()), (V) solutionAnnotations.get("f"),
					solutionAnnotations.containsKey("fTime") ? (int) solutionAnnotations.get("fTime") : -1);
			return solution;
		}

		public Map<String, Object> getAnnotationsOfSolution(final Solution<R, T, V> solution) {
			return planner.getAnnotationsOfSolution(solution.getPlanningSolution());
		}

		public void cancel() {
			this.canceled = true;
			planner.cancel();
			HASCOWithParameterPruning.this.triggerTerminationEvent();
		}
	}

	private void triggerTerminationEvent() {
		HASCOWithParameterPruning.this.solutionEvaluationEventBus
				.post(new HASCORunTerminatedEvent<>(this.compositionOfBestRecognizedSolution,
						this.bestRecognizedSolution, this.scoreOfBestRecognizedSolution));
	}

	public T getObjectFromPlan(final List<Action> plan) {
		Monom state = reduction.getInitState();
		for (Action a : plan) {
			PlannerUtil.updateState(state, a);
		}
		try {
			return this.getObjectFromState(state);
		} catch (Exception e) {
			this.logger.error("Could not retrieve target object from plan. Details:\n{}",
					LoggerUtil.getExceptionInfo(e));
			return null;
		}
	}

	public T getObjectFromState(final Monom state) throws Exception {
		T object = this.factory.getComponentInstantiation(Util.getSolutionCompositionFromState(this.components, state));
		assert object != null : "Factory has returned NULL";
		return object;
	}

	protected void afterSearch() {
	}

	public int getTimeout() {
		return this.timeout;
	}

	public void setTimeout(final int timeout) {
		this.timeout = timeout;
	}

	public void setRandom(final int randomSeed) {
		this.randomSeed = randomSeed;
	}

	public int getNumberOfCPUs() {
		return this.numberOfCPUs;
	}

	public void setNumberOfCPUs(final int numberOfCPUs) {
		this.numberOfCPUs = numberOfCPUs;
	}

	public Collection<Component> getComponents() {
		return this.components;
	}

	public Factory<? extends T> getFactory() {
		return this.factory;
	}

	public void setFactory(final Factory<T> factory) {
		this.factory = factory;
	}

	public IObjectEvaluator<T, V> getBenchmark() {
		return this.benchmark;
	}

	@Override
	public HASCOSolutionIterator iterator() {
		return new HASCOSolutionIterator();
	}

	@Override
	public void registerListener(final Object listener) {
		synchronized (this.listeners) {
			this.listeners.add(listener);
		}
	}

	public void registerListenerForSolutionEvaluations(final Object listener) {
		this.solutionEvaluationEventBus.register(listener);
	}

	public GraphGenerator<N, A> getGraphGenerator() {
		if (planner != null)
			return planner.getGraphGenerator();
		else
			return plannerFactory.newAlgorithm(problem, numberOfCPUs).getGraphGenerator();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger from {} to {}", this.logger.getName(), name);
		this.loggerName = name;
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Activated logger {} with name {}", name, this.logger.getName());
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	public INodeEvaluator<N, V> getPreferredNodeEvaluator() {
		return preferredNodeEvaluator;
	}

	public void setPreferredNodeEvaluator(INodeEvaluator<N, V> preferredNodeEvaluator) {
		this.preferredNodeEvaluator = preferredNodeEvaluator;
	}

	public void setNumberOfSamplesOfRandomCompletion(int numSamples) {
		randomCompletionEvaluator.setNumberOfRandomCompletions(numSamples);
	}
}