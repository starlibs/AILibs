package hasco.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import hasco.RefinementConfiguredSoftwareConfigurationProblem;
import hasco.events.HASCOSolutionEvent;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.Parameter;
import hasco.model.ParameterRefinementConfiguration;
import hasco.reduction.HASCOReduction;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.basic.algorithm.IAlgorithmListener;
import jaicore.planning.EvaluatedSearchGraphBasedPlan;
import jaicore.planning.graphgenerators.IPlanningGraphGeneratorDeriver;
import jaicore.planning.model.CostSensitiveHTNPlanningProblem;
import jaicore.planning.model.CostSensitivePlanningToSearchProblemTransformer;
import jaicore.planning.model.ceoc.CEOCAction;
import jaicore.planning.model.ceoc.CEOCOperation;
import jaicore.planning.model.core.Plan;
import jaicore.planning.model.task.ceocipstn.CEOCIPSTNPlanningProblem;
import jaicore.planning.model.task.ceocstn.OCMethod;
import jaicore.search.algorithms.standard.bestfirst.events.GraphSearchSolutionCandidateFoundEvent;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.probleminputs.GraphSearchProblemInput;

/**
 * Hierarchically create an object of type T
 *
 * @author fmohr
 *
 * @param <T>
 */
public class HASCO<ISearch, N, A, V extends Comparable<V>> implements IAlgorithm<RefinementConfiguredSoftwareConfigurationProblem<V>, HASCOSolutionCandidate<V>, IAlgorithmListener>, ILoggingCustomizable {

	/* communication (event bus and logging) */
	private final EventBus eventBus = new EventBus(); // An EventBus for notifying listeners about the evaluation of solution nodes.
	private Logger logger = LoggerFactory.getLogger(HASCO.class); // Logger instance for controlled output
	private String loggerName; // Name for the logger to facilitate output level configuration.

	/* problem and algorithm setup */
	private final RefinementConfiguredSoftwareConfigurationProblem<V> configurationProblem;
	private final Collection<Component> components;
	private final IPlanningGraphGeneratorDeriver<CEOCOperation, OCMethod, CEOCAction, CEOCIPSTNPlanningProblem<CEOCOperation, OCMethod, CEOCAction>, N, A> planningGraphGeneratorDeriver;
	private final AlgorithmProblemTransformer<GraphSearchProblemInput<N, A, V>, ISearch> searchProblemTransformer;
	private final HASCOConfig CONFIG = ConfigCache.getOrCreate(HASCOConfig.class);
	private final IGraphSearchFactory<ISearch, ?, N, A, V, ?, ?, ?> searchFactory;

	/* Parameters for the search algorithm configuration */
	/** Factory for producing planners solving the HTN planning problem. */
	// private final IHASCOSearchSpaceUtilFactory<N, A, V> searchSpaceUtilFactory;

	/** Object evaluator for assessing the quality of plans. */

	/* working constants of the algorithms - these are effectively final but are not set at object creation time */
	private CostSensitiveHTNPlanningProblem<CEOCOperation, OCMethod, CEOCAction, CEOCIPSTNPlanningProblem<CEOCOperation, OCMethod, CEOCAction>, V> planningProblem;
	private GraphSearchProblemInput<N, A, V> searchProblem;
	private IGraphSearch<ISearch, ?, N, A, V, ?, ?, ?> search;

	/* runtime variables of algorithm */
	private AlgorithmState state = AlgorithmState.created;
	private HASCOSolutionCandidate<V> bestRecognizedSolution;

	public HASCO(RefinementConfiguredSoftwareConfigurationProblem<V> configurationProblem,
			IPlanningGraphGeneratorDeriver<CEOCOperation, OCMethod, CEOCAction, CEOCIPSTNPlanningProblem<CEOCOperation, OCMethod, CEOCAction>, N, A> planningGraphGeneratorDeriver,
			IGraphSearchFactory<ISearch, ?, N, A, V, ?, ?, ?> searchFactory, AlgorithmProblemTransformer<GraphSearchProblemInput<N, A, V>, ISearch> searchProblemTransformer) {
		super();
		this.configurationProblem = configurationProblem;
		this.planningGraphGeneratorDeriver = planningGraphGeneratorDeriver;
		this.searchFactory = searchFactory;
		this.searchProblemTransformer = searchProblemTransformer;
		this.components = configurationProblem.getCoreProblem().getComponents();
	}

	@Override
	public Iterator<AlgorithmEvent> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		return state != AlgorithmState.inactive;
	}

	@Override
	public AlgorithmEvent next() {
		switch (state) {
		case created: {
			this.logger.info("Starting HASCO run.");

			/* check whether there is a refinement config for each numeric parameter */
			Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramRefinementConfig = configurationProblem.getParamRefinementConfig();
			for (Component c : this.components) {
				for (Parameter p : c.getParameters()) {
					if (p.isNumeric() && (!paramRefinementConfig.containsKey(c) || !paramRefinementConfig.get(c).containsKey(p))) {
						throw new IllegalArgumentException("No refinement config was delivered for numeric parameter " + p.getName() + " of component " + c.getName());
					}
				}
			}

			/* derive search problem */
			planningProblem = new HASCOReduction<V>().transform(configurationProblem);
			searchProblem = new CostSensitivePlanningToSearchProblemTransformer<CEOCOperation, OCMethod, CEOCAction, CEOCIPSTNPlanningProblem<CEOCOperation, OCMethod, CEOCAction>, V, N, A>(
					planningGraphGeneratorDeriver).transform(planningProblem);

			/* create search algorithm, set its logger, and initialize it */
			searchFactory.setProblemInput(searchProblem, searchProblemTransformer);
			search = searchFactory.getAlgorithm();
			if (this.loggerName != null && this.loggerName.length() > 0 && search instanceof ILoggingCustomizable) {
				this.logger.info("Setting logger name of {} to {}", search, this.loggerName + ".search");
				((ILoggingCustomizable) this.search).setLoggerName(this.loggerName + ".search");
			}
			boolean searchInitializationObserved = false;
			while (search.hasNext() && !(searchInitializationObserved = (search.next() instanceof AlgorithmInitializedEvent)))
				;
			if (!searchInitializationObserved)
				throw new IllegalStateException("The search underlying HASCO could not be initialized successully.");

			/* communicate that algorithm has been initialized */
			AlgorithmInitializedEvent initEvent = new AlgorithmInitializedEvent();
			this.eventBus.post(initEvent);
			this.state = AlgorithmState.active;
			return initEvent;
		}
		case active: {
			if (search.hasNext()) {
				AlgorithmEvent searchEvent = search.next();

				/* if the underlying search algorithm finished, we also finish */
				if (searchEvent instanceof AlgorithmFinishedEvent) {
					return terminate();
				}

				/* otherwise, if a solution has been found, we announce this finding to our listeners and memorize if it is a new best candidate */
				else if (searchEvent instanceof GraphSearchSolutionCandidateFoundEvent) {
					@SuppressWarnings("unchecked")
					GraphSearchSolutionCandidateFoundEvent<N, A, V> solutionEvent = (GraphSearchSolutionCandidateFoundEvent<N, A, V>) searchEvent;
					EvaluatedSearchGraphPath<N, A, V> searchPath = solutionEvent.getSolutionCandidate();
					Plan<CEOCAction> plan = planningGraphGeneratorDeriver.getPlan(searchPath.getNodes());
					V score = solutionEvent.getSolutionCandidate().getScore();
					EvaluatedSearchGraphBasedPlan<CEOCAction, V, N> evaluatedPlan = new EvaluatedSearchGraphBasedPlan<>(plan, score, searchPath);
					ComponentInstance objectInstance = Util.getSolutionCompositionForPlan(components, planningProblem.getCorePlanningProblem().getInit(), plan);
					HASCOSolutionCandidate<V> solution = new HASCOSolutionCandidate<>(objectInstance, evaluatedPlan);
					if (score.compareTo(bestRecognizedSolution.getScore()) < 0)
						bestRecognizedSolution = solution;
					HASCOSolutionEvent<V> hascoSolutionEvent = new HASCOSolutionEvent<>(solution);
					this.eventBus.post(hascoSolutionEvent);
					return hascoSolutionEvent;
				}
			}
		}
		default:
			throw new IllegalStateException("HASCO cannot do anything in state " + state);
		}

	}

	private AlgorithmFinishedEvent terminate() {
		this.state = AlgorithmState.inactive;
		AlgorithmFinishedEvent finishedEvent = new AlgorithmFinishedEvent();
		this.eventBus.post(finishedEvent);
		return finishedEvent;
	}

	protected void afterSearch() {
	}

	/**
	 * @return The config object defining the properties.
	 */
	public HASCOConfig getConfig() {
		return CONFIG;
	}

	/**
	 * Set the number of CPUs to be used by HASCO.
	 *
	 * @param numberOfCPUs
	 *            The number of cpus to be used.
	 */
	@Override
	public void setNumCPUs(final int numberOfCPUs) {
		this.getConfig().setProperty(HASCOConfig.K_CPUS, numberOfCPUs + "");
	}

	public void registerListenerForSolutionEvaluations(final Object listener) {
		this.eventBus.register(listener);
	}

	public GraphGenerator<N, A> getGraphGenerator() {
		return search.getGraphGenerator();
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger from {} to {}", this.logger.getName(), name);
		this.loggerName = name;
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Activated logger {} with name {}", name, this.logger.getName());
	}

	/**
	 * @return The timeout for gathering solutions.
	 */
	public int getTimeout() {
		return this.getConfig().timeout();
	}

	/**
	 * @param timeout
	 *            Timeout for gathering solutions.
	 */
	@Override
	public void setTimeout(final int timeout, TimeUnit timeUnit) {
		this.getConfig().setProperty(HASCOConfig.K_TIMEOUT, timeout + "");
	}

	/**
	 * @return The seed for the random number generator.
	 */
	public int getRandom() {
		return this.getConfig().randomSeed();
	}

	/**
	 * @param randomSeed
	 *            The random seed to initialize the random number generator.
	 */
	public void setRandom(final int randomSeed) {
		this.getConfig().setProperty(HASCOConfig.K_RANDOM_SEED, randomSeed + "");
	}

	/**
	 * @return Returns the number of CPUs that is to be used by HASCO.
	 */
	public int getNumCPUs() {
		return this.getConfig().cpus();
	}

	@Override
	public HASCOSolutionCandidate<V> call() throws Exception {
		return null;
	}

	@Override
	public RefinementConfiguredSoftwareConfigurationProblem<V> getInput() {
		return configurationProblem;
	}

	@Override
	public void cancel() {
		this.terminate();
	}

	@Override
	public void registerListener(IAlgorithmListener listener) {
		eventBus.register(listener);
	}

	@Override
	public TimeUnit getTimeoutUnit() {
		return null;
	}
}
