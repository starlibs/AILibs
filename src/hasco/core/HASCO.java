package hasco.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.NumericParameter;
import hasco.model.Parameter;
import hasco.model.ParameterRefinementConfiguration;
import hasco.query.Factory;
import jaicore.basic.IObjectEvaluator;
import jaicore.graph.observation.IObservableGraphAlgorithm;
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
import jaicore.search.structure.core.Node;

/**
 * Hierarchically create an object of type T
 * 
 * @author fmohr
 *
 * @param <T>
 */
public class HASCO<T, N, A, V extends Comparable<V>, R extends IPlanningSolution> implements Iterable<Solution<R,T>>, IObservableGraphAlgorithm<N, A> {

	private final static Logger logger = LoggerFactory.getLogger(HASCO.class);

	/* domain description */
	private final Collection<Component> components = new ArrayList<>();
	private final Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramRefinementConfig;
	private Factory<T> factory;

	/* query */
	private final String nameOfRequiredInterface;
	private final IObjectEvaluator<T,V> benchmark;

	/* search algorithm configuration */
	private final IObservableGraphBasedHTNPlanningAlgorithmFactory<R, N, A, V> plannerFactory;
	private final IObservableORGraphSearchFactory<N, A, V> searchFactory;
	private final IHASCOSearchSpaceUtilFactory<N,A,V> searchSpaceUtilFactory;
	private final INodeEvaluator<N, V> nodeEvaluator;

	/* parameters relevant for functionality */
	private int timeout;
	private int numberOfCPUs = 1;
	private Random random = new Random(0);
	
	/* list of listeners */
	private final Collection<Object> listeners = new ArrayList<>();

	
	public HASCO(IObservableGraphBasedHTNPlanningAlgorithmFactory<R, N, A, V> plannerFactory, IObservableORGraphSearchFactory<N, A, V> searchFactory, IHASCOSearchSpaceUtilFactory<N,A,V> searchSpaceUtilFactory, INodeEvaluator<N,V> nodeEvaluator, Factory<T> factory,
			Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramRefinementConfig, String nameOfRequiredInterface, IObjectEvaluator<T,V> benchmark) {
		super();
		this.plannerFactory = plannerFactory;
		this.searchFactory = searchFactory;
		this.nodeEvaluator = new AlternativeNodeEvaluator<>(nodeEvaluator, new RandomCompletionEvaluator<>(random, 3, searchSpaceUtilFactory.getPathUnifier(), new ISolutionEvaluator<N, V>() {

			@Override
			public V evaluateSolution(List<N> solutionPath) throws Exception {
				return benchmark.evaluate(getObjectFromPlan(searchSpaceUtilFactory.getPathToPlanConverter().getPlan(solutionPath)));
			}

			@Override
			public boolean doesLastActionAffectScoreOfAnySubsequentSolution(List<N> partialSolutionPath) {
				return true;
			}
			
		}));
		this.factory = factory;
		this.paramRefinementConfig = paramRefinementConfig;
		this.searchSpaceUtilFactory = searchSpaceUtilFactory;
		this.nameOfRequiredInterface = nameOfRequiredInterface;
		this.benchmark = benchmark;
	}

	public Random getRandom() {
		return random;
	}

	public class HASCOSolutionIterator implements Iterator<Solution<R,T>> {

		private final CEOCIPSTNPlanningDomain domain;
		private final CEOCIPSTNPlanningProblem problem;
		private final CNFFormula knowledge;
		private final Monom init;
		private final IHTNPlanningAlgorithm<R> planner;
		private boolean isInitialized = false;
		private Iterator<R> planIterator;
		private boolean canceled = false;
		
		private HASCOSolutionIterator() {
			domain = getPlanningDomain();
			knowledge = new CNFFormula();
			init = getInitState();
			problem = getPlanningProblem(domain, knowledge, init);
			planner = plannerFactory.newAlgorithm(problem, searchFactory, nodeEvaluator, numberOfCPUs);
			planIterator = planner.iterator();
		}

		@Override
		public boolean hasNext() {
			if (!isInitialized) {
				logger.info("Starting HASCO run.");
				System.out.println("Init State: " + init);

				System.out.println("Methods:\n------------------------------------------");
				for (Method m : problem.getDomain().getMethods()) {
					System.out.println(m);
				}

				System.out.println("Operations:\n------------------------------------------");
				for (Operation o : problem.getDomain().getOperations()) {
					System.out.println(o);
				}

				/* register listeners if the */
				if (planner instanceof IObservableGraphAlgorithm<?, ?>) {
					synchronized (listeners) {
						listeners.forEach(l -> ((IObservableGraphAlgorithm<?, ?>) planner).registerListener(l));
					}
				}
				isInitialized = true;
			}
			if (canceled)
				throw new IllegalStateException("HASCO has already been canceled. Cannot compute more solutions.");
			return planIterator.hasNext();
		}

		@Override
		public Solution<R,T> next() {

			/* derive a map of ground components */
			R plan = planIterator.next();
			return new Solution<>(plan, getObjectFromPlan(plan.getPlan()));
		}

		public Map<String,Object> getAnnotationsOfSolution(Solution<R,T> solution) {
			return planner.getAnnotationsOfSolution(solution.getPlanningSolution());
		}
		
		public void cancel() {
			this.canceled = true;
			planner.cancel();
		}
	}
	
	private T getObjectFromPlan(List<Action> plan) {
		Monom state = getInitState();
		for (Action a : plan)
			PlannerUtil.updateState(state, a);
		return getObjectFromState(state);
	}
	
	private ComponentInstance getSolutionCompositionForNode(Node<N,V> path) {
		Monom state = getInitState();
		for (Action a : searchSpaceUtilFactory.getPathToPlanConverter().getPlan(path.externalPath()))
			PlannerUtil.updateState(state, a);
		return getSolutionCompositionFromState(state);
	}
	
	private ComponentInstance getSolutionCompositionFromState(Monom state) {
		return Util.getGroundComponentsFromState(state, components, true).get("solution");
	}
	
	private T getObjectFromState(Monom state) {
		return factory.getComponentInstantiation(getSolutionCompositionFromState(state));
	}

	private Monom getInitState() {
		Monom init = new Monom();
		getExistingInterfaces().forEach(s -> init.add(new Literal("iface('" + s + "')")));
		init.add(new Literal("component('request')"));
		return init;
	}

	private Collection<String> getExistingInterfaces() {
		Collection<String> ifaces = new HashSet<>();
		for (Component c : components) {
			ifaces.addAll(c.getProvidedInterfaces());
			ifaces.addAll(c.getRequiredInterfaces());
		}
		return ifaces;
	}

	private CEOCIPSTNPlanningDomain getPlanningDomain() {

		/* create operations */
		Collection<CEOCOperation> operations = new ArrayList<>();
		for (Component c : components) {
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
					if (p instanceof NumericParameter) {
						standardKnowledgeAboutNewComponent.add(new Literal("parameterFocus(c2, '" + p.getName() + "', '" + p.getDefaultValue() + "')"));
						NumericParameter np = (NumericParameter) p;
						valParams.add(new ConstantParam("[" + np.getMin() + "," + np.getMax() + "]"));
					}
					else
						valParams.add(new ConstantParam(p.getDefaultValue().toString()));
					standardKnowledgeAboutNewComponent.add(new Literal("val", valParams));
				}
				addList.put(new CNFFormula(), standardKnowledgeAboutNewComponent);
				operations.add(new CEOCOperation("satisfy" + i + "With" + c.getName(), params, new Monom("component(c1)"), addList, new HashMap<>(), new ArrayList<>()));
			}
		}

		/* create operations for parameter initialization */
		{
			Map<CNFFormula, Monom> addList = new HashMap<>();
			addList.put(new CNFFormula(), new Monom("val(container,newValue) & overwritten(container)"));
			Map<CNFFormula, Monom> deleteList = new HashMap<>();
			deleteList.put(new CNFFormula(), new Monom("val(container,previousValue)"));
			operations.add(new CEOCOperation("redefValue", "container,previousValue,newValue", new Monom("val(container,previousValue)"), addList, deleteList, ""));
		}

		/* create methods */
		Collection<OCIPMethod> methods = new ArrayList<>();
		for (Component c : components) {

			/* create methods for the refinement of the interfaces offered by this component */
			for (String i : c.getProvidedInterfaces()) {
				List<VariableParam> params = new ArrayList<>();
				VariableParam inputParam = new VariableParam("c1");
				params.add(inputParam);
				params.add(new VariableParam("c2"));
				Collection<String> requiredInterfaces = c.getRequiredInterfaces();
				List<Literal> network = new ArrayList<>();

				String refinementArguments = "";
				int j = 0;
				for (j = 1; j <= c.getParameters().size(); j++) {
					String paramIdentifier = "p" + j;
					refinementArguments += ", " + paramIdentifier;
				}

				network.add(new Literal("satisfy" + i + "With" + c.getName() + "(c1,c2" + refinementArguments + ")"));
				for (String requiredInterface : requiredInterfaces) {
					String paramName = "sc" + (++j);
					params.add(new VariableParam(paramName));
					network.add(new Literal("tResolve" + requiredInterface + "(c2," + paramName + ")"));
				}

				refinementArguments = "";
				for (j = 1; j <= c.getParameters().size(); j++) {
					String paramIdentifier = "p" + j;
					params.add(new VariableParam(paramIdentifier));
					refinementArguments += ", " + paramIdentifier;
				}
				network.add(new Literal("tRefineParamsOf" + c.getName() + "(c1,c2" + refinementArguments + ")"));
				List<VariableParam> outputs = new ArrayList<>(params);
				outputs.remove(inputParam);
				methods.add(new OCIPMethod("resolve" + i + "With" + c.getName(), params, new Literal("tResolve" + i + "(c1,c2)"), new Monom("component(c1)"),
						new TaskNetwork(network), false, outputs, new Monom()));
			}

			/* create methods for choosing/refining parameters */
			List<VariableParam> params = new ArrayList<>();
			params.add(new VariableParam("c1"));
			List<Literal> initNetwork = new ArrayList<>();
			String refinementArguments = "";
			int j = 0;
			for (Parameter p : c.getParameters()) {
				String paramName = "p" + (++j);
				refinementArguments += ", " + paramName;
				params.add(new VariableParam(paramName));
				initNetwork.add(new Literal("tRefineParam" + p.getName() + "Of" + c.getName() + "(c2, " + paramName + ")"));
//				if (p instanceof NumericParameter) {
					methods.add(new OCIPMethod("ignoreParamRefinementFor" + p.getName() + "Of" + c.getName(), "object, container, curval",
							new Literal("tRefineParam" + p.getName() + "Of" + c.getName() + "(object,container)"),
							new Monom("parameterContainer('" + c.getName() + "', '" + p.getName() + "', object, container) & val(container,curval)"),
							new TaskNetwork(), false, "",
							new Monom("notRefinable('" + c.getName() + "', object, '" + p.getName() + "', container, curval)")));
					
					methods.add(new OCIPMethod("refineParam" + p.getName() + "Of" + c.getName(), "object, container, curval, newval",
							new Literal("tRefineParam" + p.getName() + "Of" + c.getName() + "(object,container)"),
							new Monom("parameterContainer('" + c.getName() + "', '" + p.getName() + "', object, container) & val(container,curval)"),
							new TaskNetwork("redefValue(container,curval,newval)"), false, "",
							new Monom("isValidParameterRangeRefinement('" + c.getName() + "', object, '" + p.getName() + "', container, curval, newval)")));
//				else
//					throw new IllegalArgumentException(
//							"Parameter " + p.getName() + " of type \"" + p.getClass() + "\" in component \"" + c.getName() + "\" is currently not supported.");
			}
			initNetwork.add(new Literal("tRefineParamsOf" + c.getName() + "(c1,c2" + refinementArguments + ")"));
			params = new ArrayList<VariableParam>(params);
			params.add(1, new VariableParam("c2"));
			methods.add(new OCIPMethod("refineParamsOf" + c.getName(), params, new Literal("tRefineParamsOf" + c.getName() + "(c1,c2" + refinementArguments + ")"),
					new Monom("component(c1)"), new TaskNetwork(initNetwork), false, new ArrayList<>(), new Monom("!refinementCompleted('" + c.getName() + "', c2)")));
			methods.add(new OCIPMethod("closeRefinementOfParamsOf" + c.getName(), params, new Literal("tRefineParamsOf" + c.getName() + "(c1,c2" + refinementArguments + ")"),
					new Monom("component(c1)"), new TaskNetwork(), false, new ArrayList<>(), new Monom("refinementCompleted('" + c.getName() + "', c2)")));
		}
		return new CEOCIPSTNPlanningDomain(operations, methods);
	}

	private CEOCIPSTNPlanningProblem getPlanningProblem(CEOCIPSTNPlanningDomain domain, CNFFormula knowledge, Monom init) {
		Map<String, EvaluablePredicate> evaluablePredicates = new HashMap<>();
		evaluablePredicates.put("isValidParameterRangeRefinement", new isValidParameterRangeRefinementPredicate(components, paramRefinementConfig));
		evaluablePredicates.put("notRefinable", new isNotRefinable(components, paramRefinementConfig));
		evaluablePredicates.put("refinementCompleted", new isRefinementCompletedPredicate(components, paramRefinementConfig));
		return new CEOCIPSTNPlanningProblem(domain, knowledge, init, new TaskNetwork("tResolve" + nameOfRequiredInterface + "('request', 'solution')"), evaluablePredicates,
				new HashMap<>());
	}

	protected void afterSearch() {
	}
	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setRandom(Random random) {
		this.random = random;
	}

	public int getNumberOfCPUs() {
		return numberOfCPUs;
	}

	public void setNumberOfCPUs(int numberOfCPUs) {
		this.numberOfCPUs = numberOfCPUs;
	}

	public Collection<Component> getComponents() {
		return components;
	}

	public void addComponents(Collection<Component> components) {
		this.components.addAll(components);
	}

	public void addComponent(Component component) {
		this.components.add(component);
	}

	public Factory<T> getFactory() {
		return factory;
	}

	public void setFactory(Factory<T> factory) {
		this.factory = factory;
	}

	public IObjectEvaluator<T,V> getBenchmark() {
		return benchmark;
	}

	@Override
	public HASCOSolutionIterator iterator() {
		return new HASCOSolutionIterator();
	}

	@Override
	public void registerListener(Object listener) {
		synchronized (listeners) {
			this.listeners.add(listener);
		}
	}
}
