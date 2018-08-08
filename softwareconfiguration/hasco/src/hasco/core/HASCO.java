package hasco.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import hasco.events.HASCORunStartedEvent;
import hasco.events.HASCORunTerminatedEvent;
import hasco.events.HASCOSolutionEvaluationEvent;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.Parameter;
import hasco.model.ParameterRefinementConfiguration;
import hasco.query.Factory;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.IObjectEvaluator;
import jaicore.graph.IObservableGraphAlgorithm;
import jaicore.logging.LoggerUtil;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.algorithms.IObservableGraphBasedHTNPlanningAlgorithm;
import jaicore.planning.algorithms.IObservableGraphBasedHTNPlanningAlgorithmFactory;
import jaicore.planning.algorithms.IPlanningSolution;
import jaicore.planning.model.core.Action;
import jaicore.planning.model.core.Operation;
import jaicore.planning.model.core.PlannerUtil;
import jaicore.planning.model.task.ceocipstn.CEOCIPSTNPlanningProblem;
import jaicore.planning.model.task.stn.Method;
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
public class HASCO<T, N, A, V extends Comparable<V>, R extends IPlanningSolution>
		implements Iterable<Solution<R, T, V>>, IObservableGraphAlgorithm<N, A>, ILoggingCustomizable {

	/* domain description */
	private final Collection<Component> components;
	private final Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramRefinementConfig;
	private Factory<? extends T> factory;

	/* query */
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

	/* parameters for state of a single run */
	private T bestRecognizedSolution;
	private ComponentInstance compositionOfBestRecognizedSolution;
	private V scoreOfBestRecognizedSolution;

	/* logging */
	private Logger logger = LoggerFactory.getLogger(HASCO.class);
	private String loggerName;
	
	/* run-specific options */
	private final HASCOProblemReduction reduction;
	private final CEOCIPSTNPlanningProblem problem;
	private IObservableGraphBasedHTNPlanningAlgorithm<R,N,A,V> planner;
	
	
	private ISolutionEvaluator<N, V> solutionEvaluator = new ISolutionEvaluator<N, V>() {
		@Override
		public V evaluateSolution(final List<N> solutionPath) throws Exception {
			List<Action> plan = HASCO.this.searchSpaceUtilFactory.getPathToPlanConverter().getPlan(solutionPath);
			ComponentInstance composition = Util.getSolutionCompositionForPlan(HASCO.this.components,
					reduction.getInitState(), plan);
			T solution = HASCO.this.getObjectFromPlan(plan);
			V scoreOfSolution = HASCO.this.benchmark.evaluate(solution);
			if (HASCO.this.scoreOfBestRecognizedSolution == null
					|| HASCO.this.scoreOfBestRecognizedSolution.compareTo(scoreOfSolution) > 0) {
				HASCO.this.bestRecognizedSolution = solution;
				HASCO.this.compositionOfBestRecognizedSolution = composition;
				HASCO.this.scoreOfBestRecognizedSolution = scoreOfSolution;
			}
			HASCO.this.solutionEvaluationEventBus
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

	public HASCO(final Collection<Component> components, final Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramRefinementConfig, final IObservableGraphBasedHTNPlanningAlgorithmFactory<R, N, A, V> plannerFactory,
			final IObservableORGraphSearchFactory<N, A, V> searchFactory,
			final IHASCOSearchSpaceUtilFactory<N, A, V> searchSpaceUtilFactory, final Factory<? extends T> factory,
			final String nameOfRequiredInterface, final IObjectEvaluator<T, V> benchmark) {
		super();
		
		/* set components and refinement configs */
		this.components = components;
		this.paramRefinementConfig = paramRefinementConfig;
		
		/* define search relevant factories and evaluators */
		this.plannerFactory = plannerFactory;
		this.searchFactory = searchFactory;
		this.randomCompletionEvaluator = new RandomCompletionEvaluator<>(new Random(this.randomSeed), 3,
				searchSpaceUtilFactory.getPathUnifier(), this.solutionEvaluator);
		this.factory = factory;
		this.searchSpaceUtilFactory = searchSpaceUtilFactory;
		this.benchmark = benchmark;
		
		/* set run specific options */
		reduction = new HASCOProblemReduction(components, paramRefinementConfig, nameOfRequiredInterface, true);
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
			logger.debug("The HTN problem created by HASCO is defined as follows:\n\tInit State: {}\n\tOperations:{}\n\tMethods:{}", reduction.getInitState(), opSB.toString(), methodSB.toString());
		}
	}

	public IObservableORGraphSearchFactory<N, A, V> getSearchFactory() {
		return this.searchFactory;
	}

	public ISolutionEvaluator<N, V> getSolutionEvaluator() {
		return this.solutionEvaluator;
	}

	public void setNumberOfRandomCompletions(final int randomCompletions) {
		this.randomCompletionEvaluator.setNumberOfRandomCompletions(randomCompletions);
	}

	public int getRandom() {
		return this.randomSeed;
	}

	public class HASCOSolutionIterator implements Iterator<Solution<R, T, V>> {

		private boolean isInitialized = false;
		private Iterator<R> planIterator;
		private boolean canceled = false;

		private HASCOSolutionIterator() {
			
			/* set node evaluator based on the currently defined preferred node evaluator */
			INodeEvaluator<N, V> nodeEvaluator = preferredNodeEvaluator == null ? randomCompletionEvaluator : new AlternativeNodeEvaluator<>(preferredNodeEvaluator, randomCompletionEvaluator);
			HASCO.this.planner = HASCO.this.plannerFactory.newAlgorithm(HASCO.this.problem, HASCO.this.searchFactory, nodeEvaluator, HASCO.this.numberOfCPUs);
			this.planIterator = planner.iterator();
		}

		@Override
		public boolean hasNext() {
			if (!this.isInitialized) {
				HASCO.this.logger.info("Starting HASCO run.");
				
				/* check whether there is a refinement config for each numeric parameter */
				for (Component c : HASCO.this.components) {
					for (Parameter p : c.getParameters()) {
						if (p.isNumeric() && (!HASCO.this.paramRefinementConfig.containsKey(c)
								|| !HASCO.this.paramRefinementConfig.get(c).containsKey(p))) {
							throw new IllegalArgumentException(
									"No refinement config was delivered for numeric parameter " + p.getName()
											+ " of component " + c.getName());
						}
					}
				}
				
				/* updating logger name of the planner */
				if (HASCO.this.loggerName != null && HASCO.this.loggerName.length() > 0
						&& planner instanceof ILoggingCustomizable) {
					logger.info("Setting logger name of {} to {}", planner, loggerName + ".planner");
					((ILoggingCustomizable) planner).setLoggerName(loggerName + ".planner");
				}

				/* register listeners */
				synchronized (listeners) {
					listeners.forEach(l -> ((IObservableGraphAlgorithm<?, ?>) planner).registerListener(l));
				}
				solutionEvaluationEventBus.post(new HASCORunStartedEvent<>(HASCO.this.randomSeed,
						HASCO.this.timeout, HASCO.this.numberOfCPUs, HASCO.this.benchmark));
				this.isInitialized = true;
			}
			if (this.canceled) {
				throw new IllegalStateException("HASCO has already been canceled. Cannot compute more solutions.");
			}

			HASCO.this.logger.info("Now asking the planning algorithm iterator {} whether there is a next solution.",
					this.planIterator.getClass().getName());
			return this.planIterator.hasNext();
		}

		@Override
		public Solution<R, T, V> next() {

			/* derive a map of ground components */
			R plan = this.planIterator.next();
			Map<String, Object> solutionAnnotations = planner.getAnnotationsOfSolution(plan);
			ComponentInstance objectInstance = Util.getSolutionCompositionForPlan(HASCO.this.components,
					reduction.getInitState(), plan.getPlan());
			@SuppressWarnings("unchecked")
			Solution<R, T, V> solution = new Solution<>(objectInstance, plan,
					HASCO.this.getObjectFromPlan(plan.getPlan()), (V) solutionAnnotations.get("f"),
					solutionAnnotations.containsKey("fTime") ? (int) solutionAnnotations.get("fTime") : -1);
			return solution;
		}

		public Map<String, Object> getAnnotationsOfSolution(final Solution<R, T, V> solution) {
			return planner.getAnnotationsOfSolution(solution.getPlanningSolution());
		}

		public void cancel() {
			this.canceled = true;
			planner.cancel();
			HASCO.this.triggerTerminationEvent();
		}
	}

	private void triggerTerminationEvent() {
		HASCO.this.solutionEvaluationEventBus
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
