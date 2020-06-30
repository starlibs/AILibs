package ai.libs.hasco.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.aeonbits.owner.ConfigFactory;
import org.api4.java.ai.graphsearch.problem.IOptimalPathInORGraphSearch;
import org.api4.java.ai.graphsearch.problem.IOptimalPathInORGraphSearchFactory;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import ai.libs.hasco.events.HASCOSolutionEvent;
import ai.libs.hasco.model.Component;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.hasco.model.ComponentUtil;
import ai.libs.hasco.model.Parameter;
import ai.libs.hasco.model.ParameterRefinementConfiguration;
import ai.libs.hasco.model.UnparametrizedComponentInstance;
import ai.libs.hasco.optimizingfactory.SoftwareConfigurationAlgorithm;
import ai.libs.hasco.reduction.HASCOReduction;
import ai.libs.jaicore.basic.algorithm.AlgorithmFinishedEvent;
import ai.libs.jaicore.basic.algorithm.AlgorithmInitializedEvent;
import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.logging.ToJSONStringUtil;
import ai.libs.jaicore.planning.core.EvaluatedSearchGraphBasedPlan;
import ai.libs.jaicore.planning.core.interfaces.IEvaluatedGraphSearchBasedPlan;
import ai.libs.jaicore.planning.core.interfaces.IPlan;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.planning.hierarchical.problems.ceocipstn.CEOCIPSTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.htn.CostSensitiveHTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.htn.CostSensitivePlanningToSearchProblemReduction;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.EvaluatedSearchSolutionCandidateFoundEvent;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

/**
 * Hierarchically create an object of type T
 *
 * @author fmohr, wever
 *
 * @param <S>
 * @param <N>
 * @param <A>
 * @param <V>
 */
public class HASCO<S extends GraphSearchWithPathEvaluationsInput<N, A, V>, N, A, V extends Comparable<V>> extends SoftwareConfigurationAlgorithm<RefinementConfiguredSoftwareConfigurationProblem<V>, HASCOSolutionCandidate<V>, V> {

	private Logger logger = LoggerFactory.getLogger(HASCO.class);
	private String loggerName;

	/* problem and algorithm setup */
	private final IHASCOPlanningReduction<N, A> planningGraphGeneratorDeriver;
	private final AlgorithmicProblemReduction<? super GraphSearchWithPathEvaluationsInput<N, A, V>, ? super EvaluatedSearchGraphPath<N, A, V>, S, EvaluatedSearchGraphPath<N, A, V>> searchProblemTransformer;
	private final IOptimalPathInORGraphSearchFactory<S, EvaluatedSearchGraphPath<N, A, V>, N, A, V, ?> searchFactory;

	/* working constants of the algorithms */
	private final CostSensitiveHTNPlanningProblem<CEOCIPSTNPlanningProblem, V> planningProblem;
	private final S searchProblem;
	private final IOptimalPathInORGraphSearch<S, EvaluatedSearchGraphPath<N, A, V>, N, A, V> search;
	private final List<HASCOSolutionCandidate<V>> listOfAllRecognizedSolutions = new ArrayList<>();
	private int numUnparametrizedSolutions = -1;
	private final Set<UnparametrizedComponentInstance> returnedUnparametrizedComponentInstances = new HashSet<>();
	private Map<EvaluatedSearchSolutionCandidateFoundEvent<N, A, V>, HASCOSolutionEvent<V>> hascoSolutionEventCache = new ConcurrentHashMap<>();
	private boolean createComponentInstancesFromNodesInsteadOfPlans = false;

	/* runtime variables of algorithm */
	private final TimeRecordingEvaluationWrapper<V> timeGrabbingEvaluationWrapper;

	public HASCO(final RefinementConfiguredSoftwareConfigurationProblem<V> configurationProblem, final IHASCOPlanningReduction<N, A> planningGraphGeneratorDeriver,
			final IOptimalPathInORGraphSearchFactory<S, EvaluatedSearchGraphPath<N, A, V>, N, A, V, ?> searchFactory,
			final AlgorithmicProblemReduction<? super GraphSearchWithPathEvaluationsInput<N, A, V>, ? super EvaluatedSearchGraphPath<N, A, V>, S, EvaluatedSearchGraphPath<N, A, V>> searchProblemTransformer) {
		this(ConfigFactory.create(HASCOConfig.class), configurationProblem, planningGraphGeneratorDeriver, searchFactory, searchProblemTransformer);
	}

	public HASCO(final HASCOConfig algorithmConfig, final RefinementConfiguredSoftwareConfigurationProblem<V> configurationProblem, final IHASCOPlanningReduction<N, A> planningGraphGeneratorDeriver,
			final IOptimalPathInORGraphSearchFactory<S, EvaluatedSearchGraphPath<N, A, V>, N, A, V, ?> searchFactory,
			final AlgorithmicProblemReduction<? super GraphSearchWithPathEvaluationsInput<N, A, V>, ? super EvaluatedSearchGraphPath<N, A, V>, S, EvaluatedSearchGraphPath<N, A, V>> searchProblemTransformer) {
		super(algorithmConfig, configurationProblem);
		if (configurationProblem == null) {
			throw new IllegalArgumentException("Cannot work with configuration problem NULL");
		}
		if (configurationProblem.getRequiredInterface() == null || configurationProblem.getRequiredInterface().isEmpty()) {
			throw new IllegalArgumentException("Not required interface defined in the input");
		}
		this.planningGraphGeneratorDeriver = planningGraphGeneratorDeriver;
		this.searchFactory = searchFactory;
		this.searchProblemTransformer = searchProblemTransformer;
		this.timeGrabbingEvaluationWrapper = new TimeRecordingEvaluationWrapper<>(configurationProblem.getCompositionEvaluator());

		/* check whether there is a refinement config for each numeric parameter */
		Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramRefinementConfig = this.getInput().getParamRefinementConfig();
		for (Component c : this.getInput().getComponents()) {
			for (Parameter p : c.getParameters()) {
				if (p.isNumeric() && (!paramRefinementConfig.containsKey(c) || !paramRefinementConfig.get(c).containsKey(p))) {
					throw new IllegalArgumentException("No refinement config was delivered for numeric parameter " + p.getName() + " of component " + c.getName());
				}
			}
		}

		/* check whether there is a component that satisfies the query */
		final String requiredInterface = configurationProblem.getRequiredInterface();
		Collection<Component> rootComponents = configurationProblem.getComponents().stream().filter(c -> c.getProvidedInterfaces().contains(requiredInterface)).collect(Collectors.toList());
		if (rootComponents.isEmpty()) {
			throw new IllegalArgumentException("There is no component that provides the required interface \"" + requiredInterface + "\"");
		}
		this.logger.info("Identified {} components that can be used to resolve the query.", rootComponents.size());

		/* derive planning problem and search problem */
		this.logger.debug("Deriving search problem");
		RefinementConfiguredSoftwareConfigurationProblem<V> refConfigSoftwareConfigurationProblem = new RefinementConfiguredSoftwareConfigurationProblem<>(
				new SoftwareConfigurationProblem<>(this.getInput().getComponents(), this.getInput().getRequiredInterface(), this.timeGrabbingEvaluationWrapper), this.getInput().getParamRefinementConfig());
		HASCOReduction<V> hascoReduction = new HASCOReduction<>(this::getBestSeenSolution);
		this.planningProblem = hascoReduction.encodeProblem(refConfigSoftwareConfigurationProblem);
		this.searchProblem = new CostSensitivePlanningToSearchProblemReduction<N, A, V, CEOCIPSTNPlanningProblem, S, EvaluatedSearchGraphPath<N, A, V>>(this.planningGraphGeneratorDeriver, searchProblemTransformer)
				.encodeProblem(this.planningProblem);

		/* create search object */
		this.logger.debug("Creating and initializing the search object");
		this.search = this.searchFactory.getAlgorithm(this.searchProblem);

		/* now tell some of the used components that they are used here */
		if (planningGraphGeneratorDeriver instanceof IHascoAware) {
			((IHascoAware) planningGraphGeneratorDeriver).setHascoReference(this);
		}
		if (searchProblemTransformer instanceof IHascoAware) {
			((IHascoAware) searchProblemTransformer).setHascoReference(this);
		}
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
			this.logger.info("Starting HASCO run. Enable DEBUG to get an overview of the considered HTN planning problem.");
			if (this.logger.isInfoEnabled()) {
				String reqInterface = this.getInput().getRequiredInterface();
				this.logger.info("HASCO Configuration:\n\tRequired Interface: {}\n\tComponents: {}", reqInterface,
						this.getInput().getComponents().stream().map(c -> "\n\t\t [" + (c.getProvidedInterfaces().contains(reqInterface) ? "*" : " ") + "]" + c.toString()).collect(Collectors.joining()));

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
				public void receiveSolutionCandidateFoundEvent(final EvaluatedSearchSolutionCandidateFoundEvent<N, A, V> solutionEvent) throws InterruptedException, AlgorithmException {

					EvaluatedSearchGraphPath<N, A, V> searchPath = solutionEvent.getSolutionCandidate();
					IPlan plan = HASCO.this.planningGraphGeneratorDeriver.decodeSolution(searchPath);
					ComponentInstance objectInstance;
					if (HASCO.this.createComponentInstancesFromNodesInsteadOfPlans) {
						objectInstance = Util.getSolutionCompositionFromState(HASCO.this.getInput().getComponents(), ((TFDNode) searchPath.getNodes().get(searchPath.getNodes().size() - 1)).getState(), true);
					} else {
						objectInstance = Util.getSolutionCompositionForPlan(HASCO.this.getInput().getComponents(), HASCO.this.planningProblem.getCorePlanningProblem().getInit(), plan, true);
					}
					HASCO.this.returnedUnparametrizedComponentInstances.add(new UnparametrizedComponentInstance(objectInstance));
					V score;
					try {
						boolean scoreInCache = HASCO.this.timeGrabbingEvaluationWrapper.hasEvaluationForComponentInstance(objectInstance);
						if (scoreInCache) {
							score = solutionEvent.getSolutionCandidate().getScore();
						} else {
							score = HASCO.this.timeGrabbingEvaluationWrapper.evaluate(objectInstance);
						}
					} catch (ObjectEvaluationFailedException e) {
						throw new AlgorithmException("Could not evaluate component instance", e);
					}
					HASCO.this.logger.info("Received new solution with score {} from search, communicating this solution to the HASCO listeners. Number of returned unparametrized solutions is now {}/{}.", score,
							HASCO.this.returnedUnparametrizedComponentInstances.size(), HASCO.this.numUnparametrizedSolutions);
					IEvaluatedGraphSearchBasedPlan<N, A, V> evaluatedPlan = new EvaluatedSearchGraphBasedPlan<>(plan, score, searchPath);
					HASCOSolutionCandidate<V> solution = new HASCOSolutionCandidate<>(objectInstance, evaluatedPlan, HASCO.this.timeGrabbingEvaluationWrapper.getEvaluationTimeForComponentInstance(objectInstance));
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
				this.logger.info("HASCO initialization completed.");
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
				this.logger.info("Received new solution with score {} from search, communicating this solution to the HASCO listeners. Number of returned unparametrized solutions is now {}/{}.", hascoSolutionEvent.getScore(),
						this.returnedUnparametrizedComponentInstances.size(), this.numUnparametrizedSolutions);
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
	}

	public IHASCOPlanningReduction<N, A> getPlanningGraphGeneratorDeriver() {
		return this.planningGraphGeneratorDeriver;
	}

	public AlgorithmicProblemReduction<? super GraphSearchWithPathEvaluationsInput<N, A, V>, ? super EvaluatedSearchGraphPath<N, A, V>, S, EvaluatedSearchGraphPath<N, A, V>> getSearchProblemTransformer() {
		return this.searchProblemTransformer;
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

	public IOptimalPathInORGraphSearchFactory<S, EvaluatedSearchGraphPath<N, A, V>, N, A, V, ?> getSearchFactory() {
		return this.searchFactory;
	}

	public IOptimalPathInORGraphSearch<S, EvaluatedSearchGraphPath<N, A, V>, N, A, V> getSearch() {
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
		fields.put("planningGraphGeneratorDeriver", this.planningGraphGeneratorDeriver);
		fields.put("planningProblem", this.planningProblem);
		fields.put("search", this.search);
		fields.put("searchProblem", this.searchProblem);
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
	}
}