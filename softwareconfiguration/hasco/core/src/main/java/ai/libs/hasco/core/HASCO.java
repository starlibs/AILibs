package ai.libs.hasco.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.aeonbits.owner.ConfigFactory;
import org.api4.java.ai.graphsearch.problem.IOptimalPathInORGraphSearch;
import org.api4.java.ai.graphsearch.problem.IOptimalPathInORGraphSearchFactory;
import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import ai.libs.hasco.core.events.HASCOSolutionEvent;
import ai.libs.hasco.core.reduction.planning2search.IHASCOPlanningReduction;
import ai.libs.hasco.core.reduction.softcomp2planning.HASCOReductionSolutionEvaluator;
import ai.libs.jaicore.basic.algorithm.AlgorithmFinishedEvent;
import ai.libs.jaicore.basic.algorithm.AlgorithmInitializedEvent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.ComponentUtil;
import ai.libs.jaicore.components.model.CompositionProblemUtil;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.components.model.UnparametrizedComponentInstance;
import ai.libs.jaicore.components.optimizingfactory.SoftwareConfigurationAlgorithm;
import ai.libs.jaicore.components.serialization.ComponentSerialization;
import ai.libs.jaicore.logging.ToJSONStringUtil;
import ai.libs.jaicore.planning.core.EvaluatedSearchGraphBasedPlan;
import ai.libs.jaicore.planning.core.interfaces.IEvaluatedGraphSearchBasedPlan;
import ai.libs.jaicore.planning.core.interfaces.IPlan;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.planning.hierarchical.problems.ceocipstn.CEOCIPSTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.htn.CostSensitiveHTNPlanningProblem;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.EvaluatedSearchSolutionCandidateFoundEvent;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.timing.TimeRecordingObjectEvaluator;

/**
 * Hierarchically create an object of type T
 *
 * @author fmohr, wever
 *
 * @param <N>
 *            Type of nodes in the search graph to which the problem is reduced
 * @param <A>
 *            Type of arc labels in the search graph to which the problem is reduced
 * @param <V>
 *            Type of scores of solutions
 */
public class HASCO<N, A, V extends Comparable<V>> extends SoftwareConfigurationAlgorithm<RefinementConfiguredSoftwareConfigurationProblem<V>, HASCOSolutionCandidate<V>, V> {

	private Logger logger = LoggerFactory.getLogger(HASCO.class);
	private String loggerName; // this is a bit redundant in order to more easily configure sub-loggers

	/* problem and algorithm setup */
	private final IHASCOPlanningReduction<N, A> planning2searchReduction;
	private final IOptimalPathInORGraphSearchFactory<IPathSearchWithPathEvaluationsInput<N, A, V>, EvaluatedSearchGraphPath<N, A, V>, N, A, V, ?> searchFactory;

	/* working constants of the algorithms */
	private final CostSensitiveHTNPlanningProblem<CEOCIPSTNPlanningProblem, V> planningProblem;
	private final IPathSearchWithPathEvaluationsInput<N, A, V> searchProblem;
	private final IOptimalPathInORGraphSearch<IPathSearchWithPathEvaluationsInput<N, A, V>, EvaluatedSearchGraphPath<N, A, V>, N, A, V> search;
	private final List<HASCOSolutionCandidate<V>> listOfAllRecognizedSolutions = new ArrayList<>();
	private int numUnparametrizedSolutions = -1;
	private final Set<UnparametrizedComponentInstance> returnedUnparametrizedComponentInstances = new HashSet<>();
	private Map<EvaluatedSearchSolutionCandidateFoundEvent<N, A, V>, HASCOSolutionEvent<V>> hascoSolutionEventCache = new ConcurrentHashMap<>();
	private boolean createComponentInstancesFromNodesInsteadOfPlans = false;
	private AtomicBoolean cancelCompleted = new AtomicBoolean();
	private final ComponentSerialization serializer = new ComponentSerialization();

	/* runtime variables of algorithm */
	private final TimeRecordingObjectEvaluator<IComponentInstance, V> timeGrabbingEvaluationWrapper;

	public HASCO(final RefinementConfiguredSoftwareConfigurationProblem<V> configurationProblem, final IHASCOPlanningReduction<N, A> planningGraphGeneratorDeriver,
			final IOptimalPathInORGraphSearchFactory<IPathSearchWithPathEvaluationsInput<N, A, V>, EvaluatedSearchGraphPath<N, A, V>, N, A, V, ?> searchFactory) {
		this(ConfigFactory.create(HASCOConfig.class), configurationProblem, planningGraphGeneratorDeriver, searchFactory);
	}

	public HASCO(final HASCOConfig algorithmConfig, final RefinementConfiguredSoftwareConfigurationProblem<V> configurationProblem, final IHASCOPlanningReduction<N, A> planning2searchReduction,
			final IOptimalPathInORGraphSearchFactory<IPathSearchWithPathEvaluationsInput<N, A, V>, EvaluatedSearchGraphPath<N, A, V>, N, A, V, ?> searchFactory) {
		super(algorithmConfig, configurationProblem);
		if (configurationProblem == null) {
			throw new IllegalArgumentException("Cannot work with configuration problem NULL");
		}
		if (configurationProblem.getRequiredInterface() == null || configurationProblem.getRequiredInterface().isEmpty()) {
			throw new IllegalArgumentException("Not required interface defined in the input");
		}
		this.planning2searchReduction = planning2searchReduction;
		this.searchFactory = searchFactory;

		/* check whether there is a component that satisfies the query */
		final int numberOfComponentsThatResolveRequest = CompositionProblemUtil.getComponentsThatResolveProblem(configurationProblem).size();
		if (numberOfComponentsThatResolveRequest == 0) {
			throw new IllegalArgumentException("There is no component that provides the required interface \"" + configurationProblem.getRequiredInterface() + "\"");
		}
		this.logger.info("Identified {} components that can be used to resolve the query.", numberOfComponentsThatResolveRequest);

		/* derive planning problem and search problem */
		this.logger.debug("Deriving search problem");
		this.planningProblem = HASCOUtil.getPlannigProblem(configurationProblem);
		this.searchProblem = HASCOUtil.getSearchProblemWithEvaluation(this.planningProblem, planning2searchReduction);
		this.timeGrabbingEvaluationWrapper = ((HASCOReductionSolutionEvaluator<V>)this.planningProblem.getPlanEvaluator()).getTimedEvaluator();

		/* create search object */
		this.logger.debug("Creating and initializing the search object");
		this.search = this.searchFactory.getAlgorithm(this.searchProblem);
	}

	@Override
	public IAlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {

		/* check on termination */
		this.logger.trace("Conducting next step in {}.", this.getId());
		this.checkAndConductTermination();
		this.logger.trace("No stop criteria have caused HASCO to stop up to now. Proceeding ...");

		/* act depending on state */
		switch (this.getState()) {
		case CREATED:
			if (this.logger.isInfoEnabled()) {
				String reqInterface = this.getInput().getRequiredInterface();
				String components = this.getInput().getComponents().stream().map(c -> "\n\t\t [" + (c.getProvidedInterfaces().contains(reqInterface) ? "*" : " ") + "]" + c.toString()).collect(Collectors.joining());
				this.logger.info(
						"Starting HASCO run. Parametrization:\n\tCPUs: {}\n\tTimeout: {}s\n\tNode evaluator: {}\nProblem:\n\tRequired Interface: {}\n\tComponents: {}\nEnable DEBUG to get an overview of the considered HTN planning problem.",
						this.getNumCPUs(), this.getTimeout().seconds(), this.search.getInput().getPathEvaluator(), reqInterface, components);
			}
			if (this.logger.isDebugEnabled()) {
				String operations = this.planningProblem.getCorePlanningProblem().getDomain().getOperations().stream()
						.map(o -> "\n\t\t" + o.getName() + "(" + o.getParams() + ")\n\t\t\tPre: " + o.getPrecondition() + "\n\t\t\tAdd List: " + o.getAddLists() + "\n\t\t\tDelete List: " + o.getDeleteLists()).collect(Collectors.joining());
				String methods = this.planningProblem.getCorePlanningProblem().getDomain().getMethods().stream().map(m -> "\n\t\t" + m.getName() + "(" + m.getParameters() + ") for task " + m.getTask() + "\n\t\t\tPre: " + m.getPrecondition()
				+ "\n\t\t\tPre Eval: " + m.getEvaluablePrecondition() + "\n\t\t\tNetwork: " + m.getNetwork().getLineBasedStringRepresentation()).collect(Collectors.joining());
				this.logger.debug("Derived the following HTN planning problem:\n\tOperations:{}\n\tMethods:{}", operations, methods);
			}
			AlgorithmInitializedEvent event = this.activate();

			/* analyze problem */
			this.numUnparametrizedSolutions = ComponentUtil.getNumberOfUnparametrizedCompositions(this.getInput().getComponents(), this.getInput().getRequiredInterface());
			this.logger.info("Search space contains {} unparametrized solutions.", this.numUnparametrizedSolutions);

			/* setup search algorithm */
			this.search.setNumCPUs(this.getNumCPUs());
			this.search.setTimeout(this.getTimeout());
			if (this.loggerName != null && this.loggerName.length() > 0 && this.search instanceof ILoggingCustomizable) {
				this.logger.info("Setting logger name of {} to {}.search", this.search.getId(), this.loggerName);
				((ILoggingCustomizable) this.search).setLoggerName(this.loggerName + ".search");
			} else {
				this.logger.info("Not setting the logger name of the search. Logger name of HASCO is {}. Search loggingCustomizable: {}", this.loggerName, (this.search instanceof ILoggingCustomizable));
			}

			/* register a listener on the search that will forward all events to HASCO's event bus */
			this.search.registerListener(new Object() {

				@Subscribe
				public void receiveSearchEvent(final IAlgorithmEvent event) {
					if (!(event instanceof AlgorithmInitializedEvent || event instanceof AlgorithmFinishedEvent)) {
						HASCO.this.post(event);
					}
				}

				@Subscribe
				public void receiveSolutionCandidateFoundEvent(final EvaluatedSearchSolutionCandidateFoundEvent<N, A, V> solutionEvent) {
					HASCO.this.logger.info("Received solution event {}", solutionEvent);
					EvaluatedSearchGraphPath<N, A, V> searchPath = solutionEvent.getSolutionCandidate();
					IPlan plan = HASCO.this.planning2searchReduction.decodeSolution(searchPath);
					ComponentInstance objectInstance;
					if (HASCO.this.createComponentInstancesFromNodesInsteadOfPlans) {
						objectInstance = HASCOUtil.getSolutionCompositionFromState(HASCO.this.getInput().getComponents(), ((TFDNode) searchPath.getNodes().get(searchPath.getNodes().size() - 1)).getState(), true);
					} else {
						objectInstance = HASCOUtil.getSolutionCompositionForPlan(HASCO.this.getInput().getComponents(), HASCO.this.planningProblem.getCorePlanningProblem().getInit(), plan, true);
					}
					HASCO.this.returnedUnparametrizedComponentInstances.add(new UnparametrizedComponentInstance(objectInstance));
					V score;
					boolean scoreInCache = HASCO.this.timeGrabbingEvaluationWrapper.hasEvaluationForComponentInstance(objectInstance);
					if (!scoreInCache) {
						throw new IllegalStateException("The time recording object evaluator has no information about component instance " + objectInstance);
					}
					score = solutionEvent.getSolutionCandidate().getScore();
					IEvaluatedGraphSearchBasedPlan<N, A, V> evaluatedPlan = new EvaluatedSearchGraphBasedPlan<>(plan, score, searchPath);
					HASCOSolutionCandidate<V> solution = new HASCOSolutionCandidate<>(objectInstance, evaluatedPlan, HASCO.this.timeGrabbingEvaluationWrapper.getEvaluationTimeForComponentInstance(objectInstance));
					if (HASCO.this.logger.isInfoEnabled()) {
						HASCO.this.logger.info("Received new solution {} with score {} from search, communicating this solution to the HASCO listeners. Number of returned unparametrized solutions is now {}/{}.", HASCO.this.serializer.serialize(solution.getComponentInstance()), score,
								HASCO.this.returnedUnparametrizedComponentInstances.size(), HASCO.this.numUnparametrizedSolutions);
					}
					HASCO.this.updateBestSeenSolution(solution);
					HASCO.this.listOfAllRecognizedSolutions.add(solution);
					HASCOSolutionEvent<V> hascoSolutionEvent = new HASCOSolutionEvent<>(HASCO.this, solution);
					HASCO.this.hascoSolutionEventCache.put(solutionEvent, hascoSolutionEvent);
					HASCO.this.post(hascoSolutionEvent);
				}
			});

			/* now initialize the search */
			this.logger.debug("Initializing the search");
			try {
				IAlgorithmEvent searchInitializationEvent = this.search.nextWithException();
				assert searchInitializationEvent instanceof AlgorithmInitializedEvent : "The first event emitted by the search was not the initialization event but " + searchInitializationEvent + "!";
				this.logger.debug("Search has been initialized.");
				this.logger.info("HASCO initialization completed. Starting to search for component instances ...");
				return event;
			} catch (AlgorithmException e) {
				throw new AlgorithmException("HASCO initialization failed.\nOne possible reason is that the graph has no solution.", e);
			}

		case ACTIVE:

			/* step search */
			this.logger.debug("Stepping search algorithm.");
			IAlgorithmEvent searchEvent = this.search.nextWithException();
			this.logger.debug("Search step completed, observed {}.", searchEvent.getClass().getName());
			if (searchEvent instanceof AlgorithmFinishedEvent) {
				this.logger.info("The search algorithm has finished. Terminating HASCO.");
				return this.terminate();
			}

			/* otherwise, if a solution has been found, we announce this finding to our listeners and memorize if it is a new best candidate */
			else if (searchEvent instanceof EvaluatedSearchSolutionCandidateFoundEvent) {
				HASCOSolutionEvent<V> hascoSolutionEvent = this.hascoSolutionEventCache.remove(searchEvent);
				assert (hascoSolutionEvent != null) : "Hasco solution event has not been seen yet or cannot be retrieved from cache. " + this.hascoSolutionEventCache;
				this.logger.info("Returning next solution delivered from search with score {}. Number of returned unparametrized solutions is now {}/{}.", hascoSolutionEvent.getScore(), this.returnedUnparametrizedComponentInstances.size(),
						this.numUnparametrizedSolutions);
				return hascoSolutionEvent;
			} else {
				this.logger.debug("Ignoring irrelevant search event {}", searchEvent);
				return searchEvent;
			}

		default:
			throw new IllegalStateException("HASCO cannot do anything in state " + this.getState());
		}
	}

	public IGraphGenerator<N, A> getGraphGenerator() {
		return this.searchProblem.getGraphGenerator();
	}

	public CostSensitiveHTNPlanningProblem<CEOCIPSTNPlanningProblem, V> getPlanningProblem() {
		return this.planningProblem;
	}

	@Override
	public void cancel() {
		if (this.isCanceled()) {
			this.logger.debug("Ignoring cancel, because cancel has been triggered in the past already.");
			return;
		}
		this.logger.info("Received cancel, first processing the cancel locally, then forwarding to search.");
		super.cancel();
		if (this.search != null) {
			this.logger.info("Trigger cancel on search. Thread interruption flag is {}", Thread.currentThread().isInterrupted());
			this.search.cancel();
		}
		this.logger.info("Finished, now terminating. Thread interruption flag is {}", Thread.currentThread().isInterrupted());
		this.terminate();
		this.logger.info("Cancel completed. Thread interruption flag is {}", Thread.currentThread().isInterrupted());
		synchronized (this.cancelCompleted) {
			this.cancelCompleted.set(true);
			this.cancelCompleted.notifyAll();
		}
	}

	public IHASCOPlanningReduction<N, A> getPlanningGraphGeneratorDeriver() {
		return this.planning2searchReduction;
	}

	public HASCORunReport<V> getReport() {
		return new HASCORunReport<>(this.listOfAllRecognizedSolutions);
	}

	@Override
	protected void shutdown() {
		if (this.isShutdownInitialized()) {
			this.logger.debug("Shutdown has already been initialized, ignoring new shutdown request.");
			return;
		}
		this.logger.info("Entering HASCO shutdown routine.");
		super.shutdown();
		this.logger.debug("Cancelling search.");
		this.search.cancel();
		this.logger.debug("Shutdown of HASCO completed.");
	}

	@Override
	public HASCOConfig getConfig() {
		return (HASCOConfig) super.getConfig();
	}

	public IOptimalPathInORGraphSearchFactory<IPathSearchWithPathEvaluationsInput<N, A, V>, EvaluatedSearchGraphPath<N, A, V>, N, A, V, ?> getSearchFactory() {
		return this.searchFactory;
	}

	public IOptimalPathInORGraphSearch<IPathSearchWithPathEvaluationsInput<N, A, V>, EvaluatedSearchGraphPath<N, A, V>, N, A, V> getSearch() {
		return this.search;
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger for {} from {} to {}", this.getId(), this.logger.getName(), name);
		this.loggerName = name;
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Activated logger for {} with name {}", this.getId(), name);
		super.setLoggerName(this.loggerName + "._swConfigAlgo");

		/* set logger in planning problem evaluator */
		((HASCOReductionSolutionEvaluator<?>)this.planningProblem.getPlanEvaluator()).setLoggerName(name + ".planevaluator");

		/* set logger in benchmark */
		if (this.getInput().getCompositionEvaluator() instanceof ILoggingCustomizable) {
			this.logger.info("Setting logger of HASCO solution evaluator {} to {}.solutionevaluator.", this.getInput().getCompositionEvaluator().getClass().getName(), name);
			((ILoggingCustomizable) this.getInput().getCompositionEvaluator()).setLoggerName(name + ".solutionevaluator");
		} else {
			this.logger.info("The solution evaluator {} does not implement ILoggingCustomizable, so no customization possible.", this.getInput().getCompositionEvaluator().getClass().getName());
		}
	}

	public void setCreateComponentInstancesFromNodesInsteadOfPlans(final boolean cIsFromNodes) {
		this.createComponentInstancesFromNodesInsteadOfPlans = cIsFromNodes;
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("planningGraphGeneratorDeriver", this.planning2searchReduction);
		fields.put("planningProblem", this.planningProblem);
		fields.put("search", this.search);
		fields.put("searchProblem", this.searchProblem);
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
	}

	public void registerSolutionEventListener(final Consumer<HASCOSolutionEvent<V>> listener) {
		this.registerListener(new Object() {

			@Subscribe
			public void receiveSolutionEvent(final HASCOSolutionEvent<V> e) {
				listener.accept(e);
			}
		});
	}

	public AtomicBoolean getCancelCompleted() {
		return this.cancelCompleted;
	}
}