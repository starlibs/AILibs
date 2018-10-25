package hasco.reduction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import hasco.core.Util;
import hasco.core.isNotRefinable;
import hasco.core.isRefinementCompletedPredicate;
import hasco.core.isValidParameterRangeRefinementPredicate;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.NumericParameterDomain;
import hasco.model.Parameter;
import hasco.model.ParameterRefinementConfiguration;
import jaicore.basic.IObjectEvaluator;
import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.LiteralParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;
import jaicore.logic.fol.theories.EvaluablePredicate;
import jaicore.planning.graphgenerators.IPlanningGraphGeneratorDeriver;
import jaicore.planning.model.CostSensitiveHTNPlanningProblem;
import jaicore.planning.model.ceoc.CEOCAction;
import jaicore.planning.model.ceoc.CEOCOperation;
import jaicore.planning.model.core.Action;
import jaicore.planning.model.core.Operation;
import jaicore.planning.model.core.Plan;
import jaicore.planning.model.task.IHTNPlanningProblem;
import jaicore.planning.model.task.ceocipstn.CEOCIPSTNPlanningDomain;
import jaicore.planning.model.task.ceocipstn.CEOCIPSTNPlanningProblem;
import jaicore.planning.model.task.ceocipstn.OCIPMethod;
import jaicore.planning.model.task.stn.Method;
import jaicore.planning.model.task.stn.TaskNetwork;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.probleminputs.GraphSearchInput;

/**
 * This is the class that conducts the actual problem reduction of software configuration to HTN Planning
 * 
 * @author fmohr
 *
 */
public class HASCOReduction<V extends Comparable<V>> implements AlgorithmProblemTransformer<RefinementConfiguredSoftwareConfigurationProblem<V>, CostSensitiveHTNPlanningProblem<CEOCOperation, OCIPMethod, CEOCAction, CEOCIPSTNPlanningProblem<CEOCOperation, OCIPMethod, CEOCAction>, V>> {

	// component selection
	private static final String RESOLVE_COMPONENT_IFACE_PREFIX = "1_tResolve";
	private static final String SATISFY_PREFIX = "1_satisfy";

	// component configuration
	private static final String REFINE_PARAMETERS_PREFIX = "2_tRefineParamsOf";
	private static final String REFINE_PARAMETER_PREFIX = "2_tRefineParam";
	private static final String DECLARE_CLOSED_PREFIX = "2_declareClosed";
	// private static final String REDEF_CLOSED_PREFIX = "2_satisfy";
	private static final String REDEF_VALUE_PREFIX = "2_redefValue";

	private RefinementConfiguredSoftwareConfigurationProblem<V> originalProblem;
	
	/* working variables */
	private Collection<Component> components;
	private Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramRefinementConfig;
	private boolean configureParams = true; // this could be determined automatically later
	

	public Monom getInitState() {
		if (originalProblem == null)
			throw new IllegalStateException("Cannot compute init state before transformation has been invoked.");
		Monom init = new Monom();
		this.getExistingInterfaces().forEach(s -> init.add(new Literal("iface('" + s + "')")));
		init.add(new Literal("component('request')"));
		return init;
	}

	public Collection<String> getExistingInterfaces() {
		if (originalProblem == null)
			throw new IllegalStateException("Cannot compute existing interfaces before transformation has been invoked.");
		Collection<String> ifaces = new HashSet<>();
		for (Component c : this.components) {
			ifaces.addAll(c.getProvidedInterfaces());
			ifaces.addAll(c.getRequiredInterfaces().values());
		}
		return ifaces;
	}

	public CEOCIPSTNPlanningDomain getPlanningDomain() {

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

			/*
			 * create methods for the refinement of the interfaces offered by this component
			 */
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
			 * go, in an ordering that is consistent with the pre-order on the params
			 * imposed by the dependencies, over the set of params
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
							new Monom("parameterContainer('" + c.getName() + "', '" + p.getName() + "', object, container) & val(container,curval) & overwritten(container)"),
							new TaskNetwork(DECLARE_CLOSED_PREFIX + "(container)"), false, "", new Monom("notRefinable('" + c.getName() + "', object, '" + p.getName() + "', container, curval)")));

					methods.add(new OCIPMethod("refineParam" + p.getName() + "Of" + c.getName(), "object, container, curval, newval",
							new Literal(REFINE_PARAMETER_PREFIX + p.getName() + "Of" + c.getName() + "(object,container)"),
							new Monom("parameterContainer('" + c.getName() + "', '" + p.getName() + "', object, container) & val(container,curval)"),
							new TaskNetwork(REDEF_VALUE_PREFIX + "(container,curval,newval)"), false, "",
							new Monom("isValidParameterRangeRefinement('" + c.getName() + "', object, '" + p.getName() + "', container, curval, newval)")));
					// else
					// throw new IllegalArgumentException(
					// "Parameter " + p.getName() + " of type \"" + p.getClass() + "\" in component
					// \"" + c.getName() +
					// "\" is currently not supported.");
				}
				initNetwork.add(new Literal(REFINE_PARAMETERS_PREFIX + c.getName() + "(c1,c2" + refinementArguments + ")"));
				params = new ArrayList<>(params);
				params.add(1, new VariableParam("c2"));
				methods.add(new OCIPMethod("refineParamsOf" + c.getName(), params, new Literal(REFINE_PARAMETERS_PREFIX + c.getName() + "(c1,c2" + refinementArguments + ")"),
						new Monom("component(c1)"), new TaskNetwork(initNetwork), false, new ArrayList<>(), new Monom("!refinementCompleted('" + c.getName() + "', c2)")));
				methods.add(new OCIPMethod("closeRefinementOfParamsOf" + c.getName(), params, new Literal(REFINE_PARAMETERS_PREFIX + c.getName() + "(c1,c2" + refinementArguments + ")"),
						new Monom("component(c1)"), new TaskNetwork(), false, new ArrayList<>(), new Monom("refinementCompleted('" + c.getName() + "', c2)")));
			}
		}
		return new CEOCIPSTNPlanningDomain(operations, methods);
	}

	public CEOCIPSTNPlanningProblem<CEOCOperation, OCIPMethod, CEOCAction> getPlanningProblem(final CEOCIPSTNPlanningDomain domain, final CNFFormula knowledge, final Monom init) {
		Map<String, EvaluablePredicate> evaluablePredicates = new HashMap<>();
		evaluablePredicates.put("isValidParameterRangeRefinement", new isValidParameterRangeRefinementPredicate(this.components, this.paramRefinementConfig));
		evaluablePredicates.put("notRefinable", new isNotRefinable(this.components, this.paramRefinementConfig));
		evaluablePredicates.put("refinementCompleted", new isRefinementCompletedPredicate(this.components, this.paramRefinementConfig));
		return new CEOCIPSTNPlanningProblem<>(domain, knowledge, init, new TaskNetwork(RESOLVE_COMPONENT_IFACE_PREFIX + originalProblem.getRequiredInterface() + "('request', 'solution')"), evaluablePredicates,
				new HashMap<>());
	}

	public CEOCIPSTNPlanningProblem<CEOCOperation, OCIPMethod, CEOCAction> getPlanningProblem() {
		return getPlanningProblem(getPlanningDomain(), new CNFFormula(), getInitState());
	}

	/**
	 * This method is a utility for everybody who wants to work on the graph obtained from HASCO's reduction but without using the search logic of HASCO
	 * 
	 * @param plannerFactory
	 * @return
	 */
	public <T, A, ISearch extends GraphSearchInput<T, A>> GraphGenerator<T, A> getGraphGeneratorUsedByHASCOForSpecificPlanner(final IPlanningGraphGeneratorDeriver<? extends Operation, ? extends Method, ? extends Action, IHTNPlanningProblem<?,?,?>, T, A> transformer) {
		return transformer.transform(getPlanningProblem()) ;
	}

	@Override
	public CostSensitiveHTNPlanningProblem<CEOCOperation, OCIPMethod, CEOCAction, CEOCIPSTNPlanningProblem<CEOCOperation, OCIPMethod, CEOCAction>, V> transform(RefinementConfiguredSoftwareConfigurationProblem<V> problem) {
		
		/* set object variables that will be important for several methods in the reduction */
		originalProblem = problem;
		components = originalProblem.getComponents();
		paramRefinementConfig = originalProblem.getParamRefinementConfig();
		
		/* build the cost insensitive planning problem */
		CEOCIPSTNPlanningProblem<CEOCOperation, OCIPMethod, CEOCAction> planningProblem = getPlanningProblem();

		/* derive a plan evaluator from the configuration evaluator */
		IObjectEvaluator<Plan<CEOCAction>, V> planEvaluator = new IObjectEvaluator<Plan<CEOCAction>, V>() {

			@Override
			public V evaluate(Plan<CEOCAction> plan) throws Exception {
				ComponentInstance solution = Util.getSolutionCompositionForPlan(components, getInitState(), plan, true);
				return problem.getCompositionEvaluator().evaluate(solution);
			}
		};
		CostSensitiveHTNPlanningProblem<CEOCOperation, OCIPMethod, CEOCAction, CEOCIPSTNPlanningProblem<CEOCOperation, OCIPMethod, CEOCAction>, V> costSensitiveProblem = new CostSensitiveHTNPlanningProblem<>(planningProblem, planEvaluator);
		return costSensitiveProblem;
	}
}
