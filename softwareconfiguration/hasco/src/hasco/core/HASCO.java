package hasco.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hasco.events.HASCOSearchInitializedEvent;
import hasco.events.HASCOSolutionEvent;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.Parameter;
import hasco.model.ParameterRefinementConfiguration;
import hasco.optimizingfactory.SoftwareConfigurationAlgorithm;
import hasco.reduction.HASCOReduction;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.graphvisualizer.gui.VisualizationWindow;
import jaicore.planning.EvaluatedSearchGraphBasedPlan;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionReducer;
import jaicore.planning.graphgenerators.task.tfd.TFDTooltipGenerator;
import jaicore.planning.model.CostSensitiveHTNPlanningProblem;
import jaicore.planning.model.CostSensitivePlanningToSearchProblemTransformer;
import jaicore.planning.model.ceoc.CEOCAction;
import jaicore.planning.model.ceoc.CEOCOperation;
import jaicore.planning.model.core.Plan;
import jaicore.planning.model.task.ceocipstn.CEOCIPSTNPlanningProblem;
import jaicore.planning.model.task.ceocipstn.OCIPMethod;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.algorithms.standard.bestfirst.events.EvaluatedSearchSolutionCandidateFoundEvent;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.probleminputs.GraphSearchProblemInput;
import jaicore.search.model.travesaltree.NodeTooltipGenerator;

/**
 * Hierarchically create an object of type T
 *
 * @author fmohr, wever
 *
 * @param <ISearch>
 * @param <N>
 * @param <A>
 * @param <V>
 */
public class HASCO<ISearch, N, A, V extends Comparable<V>> extends SoftwareConfigurationAlgorithm<RefinementConfiguredSoftwareConfigurationProblem<V>, HASCORunReport<V>, HASCOSolutionCandidate<V>, V> {

	private Logger logger = LoggerFactory.getLogger(HASCO.class);
	private String loggerName;

	/* problem and algorithm setup */
	private final RefinementConfiguredSoftwareConfigurationProblem<V> configurationProblem;
	private final IHASCOPlanningGraphGeneratorDeriver<N, A> planningGraphGeneratorDeriver;
	private final AlgorithmProblemTransformer<GraphSearchProblemInput<N, A, V>, ISearch> searchProblemTransformer;
	private final IGraphSearchFactory<ISearch, ?, N, A, V, ?, ?> searchFactory;

	/* Parameters for the search algorithm configuration */
	/** Factory for producing planners solving the HTN planning problem. */
	// private final IHASCOSearchSpaceUtilFactory<N, A, V> searchSpaceUtilFactory;

	/** Object evaluator for assessing the quality of plans. */

	/* working constants of the algorithms - these are effectively final but are not set at object creation time */
	private CostSensitiveHTNPlanningProblem<CEOCOperation, OCIPMethod, CEOCAction, CEOCIPSTNPlanningProblem<CEOCOperation, OCIPMethod, CEOCAction>, V> planningProblem;
	private GraphSearchProblemInput<N, A, V> searchProblem;
	private IGraphSearch<ISearch, ?, N, A, V, ?, ?> search;
	private final List<HASCOSolutionCandidate<V>> listOfAllRecognizedSolutions = new ArrayList<>();

	/* runtime variables of algorithm */
	private boolean searchCreatedAndInitialized = false;
	private final TimeRecordingEvaluationWrapper<V> timeGrabbingEvaluationWrapper;

	public HASCO(final RefinementConfiguredSoftwareConfigurationProblem<V> configurationProblem, final IHASCOPlanningGraphGeneratorDeriver<N, A> planningGraphGeneratorDeriver,
			final IGraphSearchFactory<ISearch, ?, N, A, V, ?, ?> searchFactory, final AlgorithmProblemTransformer<GraphSearchProblemInput<N, A, V>, ISearch> searchProblemTransformer) {
		super(ConfigFactory.create(HASCOConfig.class));
		if (configurationProblem == null) {
			throw new IllegalArgumentException("Cannot work with configuration problem NULL");
		}
		this.configurationProblem = configurationProblem;
		this.planningGraphGeneratorDeriver = planningGraphGeneratorDeriver;
		this.searchFactory = searchFactory;
		this.searchProblemTransformer = searchProblemTransformer;
		this.timeGrabbingEvaluationWrapper = new TimeRecordingEvaluationWrapper<>(configurationProblem.getCompositionEvaluator());
		this.setInput(new RefinementConfiguredSoftwareConfigurationProblem<>(new SoftwareConfigurationProblem<V>(configurationProblem.getComponents(), configurationProblem.getRequiredInterface(), this.timeGrabbingEvaluationWrapper),
				configurationProblem.getParamRefinementConfig()));
	}

	@Override
	public AlgorithmEvent nextWithException() throws Exception {
		switch (this.getState()) {
		case created: {
			this.logger.info("Starting HASCO run.");

			/* check whether there is a refinement config for each numeric parameter */
			Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramRefinementConfig = this.getInput().getParamRefinementConfig();
			for (Component c : this.getInput().getComponents()) {
				for (Parameter p : c.getParameters()) {
					if (p.isNumeric() && (!paramRefinementConfig.containsKey(c) || !paramRefinementConfig.get(c).containsKey(p))) {
						throw new IllegalArgumentException("No refinement config was delivered for numeric parameter " + p.getName() + " of component " + c.getName());
					}
				}
			}

			/* derive search problem */
			this.logger.debug("Deriving search problem");
			this.planningProblem = new HASCOReduction<V>().transform(this.getInput());
			if (this.logger.isDebugEnabled()) {
				String operations = this.planningProblem.getCorePlanningProblem().getDomain().getOperations().stream()
						.map(o -> "\n\t\t" + o.getName() + "(" + o.getParams() + ")\n\t\t\tPre: " + o.getPrecondition() + "\n\t\t\tAdd List: " + o.getAddLists() + "\n\t\t\tDelete List: " + o.getDeleteLists()).collect(Collectors.joining());
				String methods = this.planningProblem.getCorePlanningProblem().getDomain().getMethods().stream().map(m -> "\n\t\t" + m.getName() + "(" + m.getParameters() + ") for task " + m.getTask() + "\n\t\t\tPre: " + m.getPrecondition()
						+ "\n\t\t\tPre Eval: " + m.getEvaluablePrecondition() + "\n\t\t\tNetwork: " + m.getNetwork().getLineBasedStringRepresentation()).collect(Collectors.joining());
				this.logger.debug("Derived the following HTN planning problem:\n\tOperations:{}\n\tMethods:{}", operations, methods);
			}
			this.searchProblem = new CostSensitivePlanningToSearchProblemTransformer<CEOCOperation, OCIPMethod, CEOCAction, CEOCIPSTNPlanningProblem<CEOCOperation, OCIPMethod, CEOCAction>, V, N, A>(this.planningGraphGeneratorDeriver)
					.transform(this.planningProblem);

			/* communicate that algorithm has been initialized */
			this.logger.debug("Emitting intialization event");
			AlgorithmInitializedEvent initEvent = new AlgorithmInitializedEvent();
			this.post(initEvent);
			this.setState(AlgorithmState.active);
			return initEvent;
		}
		case active: {
			/* if the search itself has not been initialized, do this now */
			if (!this.searchCreatedAndInitialized) {
				/* create search algorithm, set its logger, and initialize visualization*/
				this.logger.debug("Creating the search object");
				this.searchFactory.setProblemInput(this.searchProblem, this.searchProblemTransformer);
				this.search = this.searchFactory.getAlgorithm();
				this.search.setNumCPUs(this.getConfig().cpus());
				this.search.setTimeout(this.getConfig().timeout() * 1000, TimeUnit.MILLISECONDS);
				if (this.loggerName != null && this.loggerName.length() > 0 && this.search instanceof ILoggingCustomizable) {
					this.logger.info("Setting logger name of {} to {}", this.search, this.loggerName + ".search");
					((ILoggingCustomizable) this.search).setLoggerName(this.loggerName + ".search");
				}
				if (this.getConfig().visualizationEnabled()) {
					this.logger.info("Launching graph visualization");
					VisualizationWindow<?, ?> window = new VisualizationWindow<>(this.search);
					if ((this.planningGraphGeneratorDeriver instanceof DefaultHASCOPlanningGraphGeneratorDeriver
							&& ((DefaultHASCOPlanningGraphGeneratorDeriver) this.planningGraphGeneratorDeriver).getWrappedDeriver() instanceof ForwardDecompositionReducer) && this.search instanceof BestFirst) {
						window.setTooltipGenerator(new NodeTooltipGenerator<>(new TFDTooltipGenerator()));
					}
				}

				/* now initialize the search */
				this.logger.debug("Initializing the search");
				boolean searchInitializationObserved = false;
				while (this.search.hasNext() && !(searchInitializationObserved = (this.search.next() instanceof AlgorithmInitializedEvent))) {
					;
				}
				if (!searchInitializationObserved) {
					throw new IllegalStateException("The search underlying HASCO could not be initialized successully.");
				}
				HASCOSearchInitializedEvent event = new HASCOSearchInitializedEvent();
				this.post(event);
				this.searchCreatedAndInitialized = true;
				return event;
			}

			/* otherwise iterate over the search */
			while (this.search.hasNext()) {
				AlgorithmEvent searchEvent = this.search.nextWithException();

				/* if the underlying search algorithm finished, we also finish */
				if (searchEvent instanceof AlgorithmFinishedEvent) {
					return this.terminate();
				}

				/* otherwise, if a solution has been found, we announce this finding to our listeners and memorize if it is a new best candidate */
				else if (searchEvent instanceof EvaluatedSearchSolutionCandidateFoundEvent) {
					this.logger.info("Received new solution from search, communicating this solution to the HASCO listeners.");
					@SuppressWarnings("unchecked")
					EvaluatedSearchSolutionCandidateFoundEvent<N, A, V> solutionEvent = (EvaluatedSearchSolutionCandidateFoundEvent<N, A, V>) searchEvent;
					EvaluatedSearchGraphPath<N, A, V> searchPath = solutionEvent.getSolutionCandidate();
					Plan<CEOCAction> plan = this.planningGraphGeneratorDeriver.getPlan(searchPath.getNodes());
					ComponentInstance objectInstance = Util.getSolutionCompositionForPlan(this.getInput().getComponents(), this.planningProblem.getCorePlanningProblem().getInit(), plan, true);
					V score = this.timeGrabbingEvaluationWrapper.hasEvaluationForComponentInstance(objectInstance) ? solutionEvent.getSolutionCandidate().getScore() : this.timeGrabbingEvaluationWrapper.evaluate(objectInstance);
					EvaluatedSearchGraphBasedPlan<CEOCAction, V, N> evaluatedPlan = new EvaluatedSearchGraphBasedPlan<>(plan, score, searchPath);
					HASCOSolutionCandidate<V> solution = new HASCOSolutionCandidate<>(objectInstance, evaluatedPlan, this.timeGrabbingEvaluationWrapper.getEvaluationTimeForComponentInstance(objectInstance));
					this.updateBestSeenSolution(solution);
					this.listOfAllRecognizedSolutions.add(solution);
					HASCOSolutionEvent<V> hascoSolutionEvent = new HASCOSolutionEvent<>(solution);
					this.post(hascoSolutionEvent);
					return hascoSolutionEvent;
				}
			}
			return this.terminate();
		}
		default:
			throw new IllegalStateException("HASCO cannot do anything in state " + this.getState());
		}

	}

	protected void afterSearch() {
		// hook that is invoked after the search algorithm
	}

	public GraphGenerator<N, A> getGraphGenerator() {
		return this.searchProblem.getGraphGenerator();
	}

	@Override
	public RefinementConfiguredSoftwareConfigurationProblem<V> getInput() {
		return this.configurationProblem;
	}

	public CostSensitiveHTNPlanningProblem<CEOCOperation, OCIPMethod, CEOCAction, CEOCIPSTNPlanningProblem<CEOCOperation, OCIPMethod, CEOCAction>, V> getPlanningProblem() {
		return this.planningProblem;
	}

	public void setVisualization(final boolean visualization) {
		this.getConfig().setProperty(HASCOConfig.K_VISUALIZE, String.valueOf(visualization));
	}

	@Override
	public void cancel() {
		super.cancel();
		if (this.search != null) {
			this.search.cancel();
		}
		this.terminate();
	}

	public IHASCOPlanningGraphGeneratorDeriver<N, A> getPlanningGraphGeneratorDeriver() {
		return this.planningGraphGeneratorDeriver;
	}

	public AlgorithmProblemTransformer<GraphSearchProblemInput<N, A, V>, ISearch> getSearchProblemTransformer() {
		return this.searchProblemTransformer;
	}

	public AlgorithmInitializedEvent init() {
		AlgorithmEvent e = null;
		while (this.hasNext()) {
			e = this.next();
			if (e instanceof AlgorithmInitializedEvent) {
				return (AlgorithmInitializedEvent) e;
			}
		}
		throw new IllegalStateException("Could not complete initialization");
	}

	@Override
	public HASCORunReport<V> getOutput() {
		return new HASCORunReport<>(this.listOfAllRecognizedSolutions);
	}

	@Override
	public HASCOConfig getConfig() {
		return (HASCOConfig) super.getConfig();
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger from {} to {}", this.logger.getName(), name);
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Activated logger {} with name {}", name, this.logger.getName());
		super.setLoggerName(this.loggerName + "._swConfigAlgo");
	}
}
