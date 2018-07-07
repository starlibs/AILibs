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

import hasco.events.HASCORunStartedEvent;
import hasco.events.HASCORunTerminatedEvent;
import hasco.events.HASCOSolutionEvaluationEvent;
import hasco.knowledgebase.PerformanceKnowledgeBase;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.NumericParameterDomain;
import hasco.model.Parameter;
import hasco.model.ParameterRefinementConfiguration;
import hasco.query.Factory;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.IObjectEvaluator;
import jaicore.graph.observation.IObservableGraphAlgorithm;
import jaicore.logging.LoggerUtil;
import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.LiteralParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;
import jaicore.logic.fol.theories.EvaluablePredicate;
import jaicore.planning.algorithms.IHTNPlanningAlgorithm;
import jaicore.planning.algorithms.IObservableGraphBasedHTNPlanningAlgorithmFactory;
import jaicore.planning.algorithms.IPlanningSolution;
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

/**
 * Hierarchically create an object of type T
 *
 * @author fmohr
 *
 * @param <T>
 */
public class HASCO<T, N, A, V extends Comparable<V>, R extends IPlanningSolution> implements Iterable<Solution<R, T, V>>, IObservableGraphAlgorithm<N, A>, ILoggingCustomizable {

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
	private final Collection<Component> components = new ArrayList<>();
	private final Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramRefinementConfig = new HashMap<>();
	private Factory<? extends T> factory;

	/* query */
	private final String nameOfRequiredInterface;
	private final IObjectEvaluator<T, V> benchmark;

	/* search algorithm configuration */
	private final IObservableGraphBasedHTNPlanningAlgorithmFactory<R, N, A, V> plannerFactory;
	private final IObservableORGraphSearchFactory<N, A, V> searchFactory;
	private final IHASCOSearchSpaceUtilFactory<N, A, V> searchSpaceUtilFactory;
	private final INodeEvaluator<N, V> nodeEvaluator;
	private final RandomCompletionEvaluator<N, V> randomCompletionEvaluator;

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
	private Logger logger = LoggerFactory.getLogger(HASCO.class);
	private String loggerName;
	
	/* list of listeners */
	private final Collection<Object> listeners = new ArrayList<>();
	
	/* performance knowledge base */
	private PerformanceKnowledgeBase performanceKB = new PerformanceKnowledgeBase();

	public HASCO(final IObservableGraphBasedHTNPlanningAlgorithmFactory<R, N, A, V> plannerFactory, final IObservableORGraphSearchFactory<N, A, V> searchFactory,
			final IHASCOSearchSpaceUtilFactory<N, A, V> searchSpaceUtilFactory, final INodeEvaluator<N, V> nodeEvaluator, final Factory<? extends T> factory,
			final String nameOfRequiredInterface,
			final IObjectEvaluator<T, V> benchmark) {
		super();
		this.plannerFactory = plannerFactory;
		this.searchFactory = searchFactory;
		this.randomCompletionEvaluator = new RandomCompletionEvaluator<>(new Random(this.randomSeed), 1, searchSpaceUtilFactory.getPathUnifier(), new ISolutionEvaluator<N, V>() {
			@Override
			public V evaluateSolution(final List<N> solutionPath) throws Exception {
				List<Action> plan = HASCO.this.searchSpaceUtilFactory.getPathToPlanConverter().getPlan(solutionPath);
				ComponentInstance composition = Util.getSolutionCompositionForPlan(HASCO.this.components, HASCO.this.getInitState(), plan);
				T solution = HASCO.this.getObjectFromPlan(plan);
				V scoreOfSolution = benchmark.evaluate(solution);
				if (HASCO.this.scoreOfBestRecognizedSolution == null || HASCO.this.scoreOfBestRecognizedSolution.compareTo(scoreOfSolution) > 0) {
					HASCO.this.bestRecognizedSolution = solution;
					HASCO.this.compositionOfBestRecognizedSolution = composition;
					HASCO.this.scoreOfBestRecognizedSolution = scoreOfSolution;
				}
				HASCO.this.solutionEvaluationEventBus.post(new HASCOSolutionEvaluationEvent<>(composition, solution, scoreOfSolution));
				return scoreOfSolution;
			}

			@Override
			public boolean doesLastActionAffectScoreOfAnySubsequentSolution(final List<N> partialSolutionPath) {
				return true;
			}
		});
		this.nodeEvaluator = new AlternativeNodeEvaluator<>(nodeEvaluator, this.randomCompletionEvaluator);
		this.factory = factory;
		this.searchSpaceUtilFactory = searchSpaceUtilFactory;
		this.nameOfRequiredInterface = nameOfRequiredInterface;
		this.benchmark = benchmark;
	}

	public void setNumberOfRandomCompletions(final int randomCompletions) {
		this.randomCompletionEvaluator.setNumberOfRandomCompletions(randomCompletions);
	}

	public int getRandom() {
		return this.randomSeed;
	}

	public class HASCOSolutionIterator implements Iterator<Solution<R, T, V>> {

		private final CEOCIPSTNPlanningDomain domain;
		private final CEOCIPSTNPlanningProblem problem;
		private final CNFFormula knowledge;
		private final Monom init;
		private final IHTNPlanningAlgorithm<R> planner;
		private boolean isInitialized = false;
		private Iterator<R> planIterator;
		private boolean canceled = false;

		private HASCOSolutionIterator() {
			this.domain = HASCO.this.getPlanningDomain();
			this.knowledge = new CNFFormula();
			this.init = HASCO.this.getInitState();
			this.problem = HASCO.this.getPlanningProblem(this.domain, this.knowledge, this.init);
			this.planner = HASCO.this.plannerFactory.newAlgorithm(this.problem, HASCO.this.searchFactory, HASCO.this.nodeEvaluator, HASCO.this.numberOfCPUs);
			if (loggerName != null && loggerName.length() > 0 && this.planner instanceof ILoggingCustomizable) {
				((ILoggingCustomizable)this.planner).setLoggerName(loggerName + ".planner");
			}
			this.planIterator = this.planner.iterator();
		}

		@Override
		public boolean hasNext() {
			if (!this.isInitialized) {
				logger.info("Starting HASCO run.");
				logger.debug("Init State: " + this.init);

				logger.debug("Methods:\n------------------------------------------");
				for (Method m : this.problem.getDomain().getMethods()) {
					logger.debug(m.toString());
				}

				logger.debug("Operations:\n------------------------------------------");
				for (Operation o : this.problem.getDomain().getOperations()) {
					logger.debug(o.toString());
				}

				/* check whether there is a refinement config for each numeric parameter */
				for (Component c : HASCO.this.components) {
					for (Parameter p : c.getParameters()) {
						if (p.isNumeric() && (!HASCO.this.paramRefinementConfig.containsKey(c) || !HASCO.this.paramRefinementConfig.get(c).containsKey(p))) {
							throw new IllegalArgumentException("No refinement config was delivered for numeric parameter " + p.getName() + " of component " + c.getName());
						}
					}
				}

				/* register listeners if the */
				if (this.planner instanceof IObservableGraphAlgorithm<?, ?>) {
					synchronized (HASCO.this.listeners) {
						HASCO.this.listeners.forEach(l -> ((IObservableGraphAlgorithm<?, ?>) this.planner).registerListener(l));
					}
				}
				HASCO.this.solutionEvaluationEventBus.post(new HASCORunStartedEvent<>(HASCO.this.randomSeed, HASCO.this.timeout, HASCO.this.numberOfCPUs, HASCO.this.benchmark));
				this.isInitialized = true;
			}
			if (this.canceled) {
				throw new IllegalStateException("HASCO has already been canceled. Cannot compute more solutions.");
			}

			logger.info("Now asking the planning algorithm iterator {} whether there is a next solution.", this.planIterator.getClass().getName());
			return this.planIterator.hasNext();
		}

		@Override
		public Solution<R, T, V> next() {

			/* derive a map of ground components */
			R plan = this.planIterator.next();
			Map<String, Object> solutionAnnotations = this.planner.getAnnotationsOfSolution(plan);
			@SuppressWarnings("unchecked")
			Solution<R, T, V> solution = new Solution<>(plan, HASCO.this.getObjectFromPlan(plan.getPlan()), (V) solutionAnnotations.get("f"),
					solutionAnnotations.containsKey("fTime") ? (int) solutionAnnotations.get("fTime") : -1);
			return solution;
		}

		public Map<String, Object> getAnnotationsOfSolution(final Solution<R, T, V> solution) {
			return this.planner.getAnnotationsOfSolution(solution.getPlanningSolution());
		}

		public void cancel() {
			this.canceled = true;
			this.planner.cancel();
			HASCO.this.triggerTerminationEvent();
		}
	}

	private void triggerTerminationEvent() {
		HASCO.this.solutionEvaluationEventBus
				.post(new HASCORunTerminatedEvent<>(this.compositionOfBestRecognizedSolution, this.bestRecognizedSolution, this.scoreOfBestRecognizedSolution));
	}

	public T getObjectFromPlan(final List<Action> plan) {
		Monom state = this.getInitState();
		for (Action a : plan) {
			PlannerUtil.updateState(state, a);
		}
		try {
			return this.getObjectFromState(state);
		}
		catch (Exception e) {
			logger.error("Could not retrieve target object from plan. Details:\n{}", LoggerUtil.getExceptionInfo(e));
			return null;
		}
	}

	public T getObjectFromState(final Monom state) throws Exception {
		T object = this.factory.getComponentInstantiation(Util.getSolutionCompositionFromState(this.components, state));
		assert object != null : "Factory has returned NULL";
		return object;
	}

	private Monom getInitState() {
		Monom init = new Monom();
		this.getExistingInterfaces().forEach(s -> init.add(new Literal("iface('" + s + "')")));
		init.add(new Literal("component('request')"));
		return init;
	}

	private Collection<String> getExistingInterfaces() {
		Collection<String> ifaces = new HashSet<>();
		for (Component c : this.components) {
			ifaces.addAll(c.getProvidedInterfaces());
			ifaces.addAll(c.getRequiredInterfaces().values());
		}
		return ifaces;
	}

	private CEOCIPSTNPlanningDomain getPlanningDomain() {

		/* create operations */
		Collection<CEOCOperation> operations = new ArrayList<>();
		for (Component c : this.components) {
			for (String i : c.getProvidedInterfaces()) {
				List<VariableParam> params = new ArrayList<>();
				params.add(new VariableParam("c1"));
				params.add(new VariableParam("c2"));
				int j = 0;
				Map<CNFFormula, Monom> addList = new HashMap<>();
				Monom standardKnowledgeAboutNewComponent = new Monom("component(c2) & resolves(c1, '" + i + "', '" + c.getName() + "', c2)");
				for (Parameter p : c.getParameters()) {
					String paramIdentifier = "p" + (++j);
					params.add(new VariableParam(paramIdentifier));

					/* add the information about this parameter container */
					List<LiteralParam> literalParams = new ArrayList<>();
					literalParams.clear();
					literalParams.add(new ConstantParam(c.getName()));
					literalParams.add(new ConstantParam(p.getName()));
					literalParams.add(new VariableParam("c2"));
					literalParams.add(new VariableParam(paramIdentifier));
					standardKnowledgeAboutNewComponent.add(new Literal("parameterContainer", literalParams));

					/* add knowledge about initial value */
					List<LiteralParam> valParams = new ArrayList<>();
					valParams.add(new VariableParam(paramIdentifier));
					if (p.isNumeric()) {
						standardKnowledgeAboutNewComponent.add(new Literal("parameterFocus(c2, '" + p.getName() + "', '" + p.getDefaultValue() + "')"));
						NumericParameterDomain np = (NumericParameterDomain) p.getDefaultDomain();
						valParams.add(new ConstantParam("[" + np.getMin() + "," + np.getMax() + "]"));
					} else {
						valParams.add(new ConstantParam(p.getDefaultValue().toString()));
					}
					standardKnowledgeAboutNewComponent.add(new Literal("val", valParams));
				}
				int k = 0;
				for (String requiredInterfaceID : c.getRequiredInterfaces().keySet()) {
					String reqIntIdentifier = "sc" + (++k);
					params.add(new VariableParam(reqIntIdentifier));

					List<LiteralParam> literalParams = new ArrayList<>();
					literalParams.clear();
					literalParams.add(new ConstantParam(c.getName()));
					literalParams.add(new ConstantParam(requiredInterfaceID));
					literalParams.add(new VariableParam("c2"));
					literalParams.add(new VariableParam(reqIntIdentifier));
					standardKnowledgeAboutNewComponent.add(new Literal("interfaceIdentifier", literalParams));
				}

				addList.put(new CNFFormula(), standardKnowledgeAboutNewComponent);
				CEOCOperation newOp = new CEOCOperation(SATISFY_PREFIX + i + "With" + c.getName(), params, new Monom("component(c1)"), addList, new HashMap<>(), new ArrayList<>());
				operations.add(newOp);
			}
		}

		/* create operations for parameter initialization */
		{
			Map<CNFFormula, Monom> addList = new HashMap<>();
			addList.put(new CNFFormula(), new Monom("val(container,newValue) & overwritten(container)"));
			Map<CNFFormula, Monom> deleteList = new HashMap<>();
			deleteList.put(new CNFFormula(), new Monom("val(container,previousValue)"));
			operations.add(new CEOCOperation(REDEF_VALUE_PREFIX, "container,previousValue,newValue", new Monom("val(container,previousValue)"), addList, deleteList, ""));
			addList = new HashMap<>();
			addList.put(new CNFFormula(), new Monom("closed(container)"));
			deleteList = new HashMap<>();
			operations.add(new CEOCOperation(DECLARE_CLOSED_PREFIX, "container", new Monom(), addList, deleteList, ""));
		}

		/* create methods */
		Collection<OCIPMethod> methods = new ArrayList<>();
		for (Component c : this.components) {

			/* create methods for the refinement of the interfaces offered by this component */
			for (String i : c.getProvidedInterfaces()) {
				List<VariableParam> params = new ArrayList<>();
				VariableParam inputParam = new VariableParam("c1");
				params.add(inputParam);
				params.add(new VariableParam("c2"));
				LinkedHashMap<String, String> requiredInterfaces = c.getRequiredInterfaces();
				List<Literal> network = new ArrayList<>();

				String refinementArguments = "";
				int j = 0;
				if (this.configureParams) {
					for (j = 1; j <= c.getParameters().size(); j++) {
						String paramIdentifier = "p" + j;
						refinementArguments += ", " + paramIdentifier;
					}
				}

				for (int k = 1; k <= requiredInterfaces.entrySet().size(); k++) {
					refinementArguments += ",sc" + k;
				}

				int sc = 0;
				network.add(new Literal(SATISFY_PREFIX + i + "With" + c.getName() + "(c1,c2" + refinementArguments + ")"));
				for (Entry<String, String> requiredInterface : requiredInterfaces.entrySet()) {
					String paramName = "sc" + (++sc);
					params.add(new VariableParam(paramName));
					network.add(new Literal(RESOLVE_COMPONENT_IFACE_PREFIX + requiredInterface.getValue() + "(c2," + paramName + ")"));
				}

				refinementArguments = "";
				if (this.configureParams) {
					for (j = 1; j <= c.getParameters().size(); j++) {
						String paramIdentifier = "p" + j;
						params.add(new VariableParam(paramIdentifier));
						refinementArguments += ", " + paramIdentifier;
					}
				}
				network.add(new Literal(REFINE_PARAMETERS_PREFIX + c.getName() + "(c1,c2" + refinementArguments + ")"));
				List<VariableParam> outputs = new ArrayList<>(params);
				outputs.remove(inputParam);
				methods.add(new OCIPMethod("resolve" + i + "With" + c.getName(), params, new Literal(RESOLVE_COMPONENT_IFACE_PREFIX + i + "(c1,c2)"), new Monom("component(c1)"),
						new TaskNetwork(network), false, outputs, new Monom()));
			}

			/* create methods for choosing/refining parameters */
			List<VariableParam> params = new ArrayList<>();
			params.add(new VariableParam("c1"));
			List<Literal> initNetwork = new ArrayList<>();
			String refinementArguments = "";
			int j = 0;

			/*
			 * go, in an ordering that is consistent with the pre-order on the params imposed by the dependencies, over the set of params
			 */
			if (this.configureParams) {
				for (Parameter p : c.getParameters()) {
					String paramName = "p" + (++j);
					refinementArguments += ", " + paramName;
					params.add(new VariableParam(paramName));
					initNetwork.add(new Literal(REFINE_PARAMETER_PREFIX + p.getName() + "Of" + c.getName() + "(c2, " + paramName + ")"));
					// if (p instanceof NumericParameter) {
					methods.add(new OCIPMethod("ignoreParamRefinementFor" + p.getName() + "Of" + c.getName(), "object, container, curval",
							new Literal(REFINE_PARAMETER_PREFIX + p.getName() + "Of" + c.getName() + "(object,container)"),
							new Monom("parameterContainer('" + c.getName() + "', '" + p.getName() + "', object, container) & val(container,curval)"),
							new TaskNetwork(DECLARE_CLOSED_PREFIX + "(container)"), false, "",
							new Monom("notRefinable('" + c.getName() + "', object, '" + p.getName() + "', container, curval)")));

					methods.add(new OCIPMethod("refineParam" + p.getName() + "Of" + c.getName(), "object, container, curval, newval",
							new Literal(REFINE_PARAMETER_PREFIX + p.getName() + "Of" + c.getName() + "(object,container)"),
							new Monom("parameterContainer('" + c.getName() + "', '" + p.getName() + "', object, container) & val(container,curval)"),
							new TaskNetwork(REDEF_VALUE_PREFIX + "(container,curval,newval)"), false, "",
							new Monom("isValidParameterRangeRefinement('" + c.getName() + "', object, '" + p.getName() + "', container, curval, newval)")));
					// else
					// throw new IllegalArgumentException(
					// "Parameter " + p.getName() + " of type \"" + p.getClass() + "\" in component \"" + c.getName() +
					// "\" is currently not supported.");
				}
				initNetwork.add(new Literal(REFINE_PARAMETERS_PREFIX + c.getName() + "(c1,c2" + refinementArguments + ")"));
				params = new ArrayList<>(params);
				params.add(1, new VariableParam("c2"));
				methods.add(new OCIPMethod("refineParamsOf" + c.getName(), params, new Literal(REFINE_PARAMETERS_PREFIX + c.getName() + "(c1,c2" + refinementArguments + ")"),
						new Monom("component(c1)"), new TaskNetwork(initNetwork), false, new ArrayList<>(), new Monom("!refinementCompleted('" + c.getName() + "', c2)")));
				methods.add(new OCIPMethod("closeRefinementOfParamsOf" + c.getName(), params,
						new Literal(REFINE_PARAMETERS_PREFIX + c.getName() + "(c1,c2" + refinementArguments + ")"), new Monom("component(c1)"), new TaskNetwork(), false,
						new ArrayList<>(), new Monom("refinementCompleted('" + c.getName() + "', c2)")));
			}
		}
		return new CEOCIPSTNPlanningDomain(operations, methods);
	}

	private CEOCIPSTNPlanningProblem getPlanningProblem(final CEOCIPSTNPlanningDomain domain, final CNFFormula knowledge, final Monom init) {
		Map<String, EvaluablePredicate> evaluablePredicates = new HashMap<>();
		evaluablePredicates.put("isValidParameterRangeRefinement", new isValidParameterRangeRefinementPredicate(this.components, this.paramRefinementConfig, this.performanceKB));
		evaluablePredicates.put("notRefinable", new isNotRefinable(this.components, this.paramRefinementConfig));
		evaluablePredicates.put("refinementCompleted", new isRefinementCompletedPredicate(this.components, this.paramRefinementConfig));
		return new CEOCIPSTNPlanningProblem(domain, knowledge, init, new TaskNetwork(RESOLVE_COMPONENT_IFACE_PREFIX + this.nameOfRequiredInterface + "('request', 'solution')"),
				evaluablePredicates, new HashMap<>());
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

	public void addComponents(final Collection<Component> components) {
		this.components.addAll(components);
	}

	public void addComponent(final Component component) {
		this.components.add(component);
	}
	
	public void addParamRefinementConfigurations(final Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramRefinementConfig) {
		this.paramRefinementConfig.putAll(paramRefinementConfig);
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

	public boolean isConfigureParams() {
		return this.configureParams;
	}

	public void setConfigureParams(final boolean configureParams) {
		this.configureParams = configureParams;
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

	@Override
	public void setLoggerName(String name) {
		logger.info("Switching logger from {} to {}", logger.getName(), name);
		this.loggerName = name;
		logger = LoggerFactory.getLogger(name);
		logger.info("Activated logger {} with name {}", name, logger.getName());
	}

	@Override
	public String getLoggerName() {
		return loggerName;
	}
}
