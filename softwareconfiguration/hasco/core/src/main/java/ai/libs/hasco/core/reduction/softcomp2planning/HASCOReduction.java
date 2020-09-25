package ai.libs.hasco.core.reduction.softcomp2planning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.api4.java.datastructure.graph.implicit.IGraphGenerator;

import ai.libs.hasco.core.HASCOUtil;
import ai.libs.hasco.core.predicate.IsNotRefinablePredicate;
import ai.libs.hasco.core.predicate.IsRefinementCompletedPredicate;
import ai.libs.hasco.core.predicate.IsValidParameterRangeRefinementPredicate;
import ai.libs.hasco.core.reduction.planning2search.IHASCOPlanningReduction;
import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.INumericParameterRefinementConfigurationMap;
import ai.libs.jaicore.components.api.IParameter;
import ai.libs.jaicore.components.api.IRequiredInterfaceDefinition;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.NumericParameterDomain;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.logic.fol.structure.CNFFormula;
import ai.libs.jaicore.logic.fol.structure.ConstantParam;
import ai.libs.jaicore.logic.fol.structure.Literal;
import ai.libs.jaicore.logic.fol.structure.LiteralParam;
import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.logic.fol.structure.VariableParam;
import ai.libs.jaicore.logic.fol.theories.EvaluablePredicate;
import ai.libs.jaicore.planning.classical.problems.ceoc.CEOCOperation;
import ai.libs.jaicore.planning.core.interfaces.IEvaluatedPlan;
import ai.libs.jaicore.planning.core.interfaces.IPlan;
import ai.libs.jaicore.planning.hierarchical.problems.ceocipstn.CEOCIPSTNPlanningDomain;
import ai.libs.jaicore.planning.hierarchical.problems.ceocipstn.CEOCIPSTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.ceocipstn.OCIPMethod;
import ai.libs.jaicore.planning.hierarchical.problems.htn.CostSensitiveHTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.stn.TaskNetwork;

/**
 * This is the class that conducts the actual problem reduction of software configuration to HTN Planning
 *
 * @author fmohr
 *
 */
public class HASCOReduction<V extends Comparable<V>>
implements AlgorithmicProblemReduction<RefinementConfiguredSoftwareConfigurationProblem<V>, ComponentInstance, CostSensitiveHTNPlanningProblem<CEOCIPSTNPlanningProblem, V>, IEvaluatedPlan<V>> {

	private static final boolean CONFIGURE_PARAMS = true; // this could be determined automatically later
	private static final Map<CNFFormula, Monom> EMPTY_PRECONDITION = new HashMap<>();

	// component selection
	private static final String RESOLVE_COMPONENT_IFACE_PREFIX = "1_tResolve";
	private static final String RESOLVE_IFACE_GROUP_PREFIX = "1_tResolveGroup";
	private static final String RESOLVE_COMPONENT_IFACE_OPTIONAL_PREFIX = "1_tResolveOpt";
	private static final String RESOLVE_SINGLE = "1_tResolveSingle";
	private static final String RESOLVE_SINGLE_OPTIONAL = "1_tResolveSingleOptional";
	private static final String SATISFY_PREFIX = "1_satisfy";

	// component configuration
	private static final String REFINE_PARAMETERS_PREFIX = "2_tRefineParamsOf";
	private static final String REFINE_PARAMETER_PREFIX = "2_tRefineParam";
	private static final String DECLARE_CLOSED_PREFIX = "2_declareClosed";
	private static final String REDEF_VALUE_PREFIX = "2_redefValue";
	private static final String OMIT_RESOLUTION_PREFIX = "1_omitResolution";

	private static final String COMPONENT_OF_C2 = "component(c2)";

	private RefinementConfiguredSoftwareConfigurationProblem<V> originalProblem;

	/* working variables */
	private Collection<IComponent> components;
	private INumericParameterRefinementConfigurationMap paramRefinementConfig;

	public static Monom getInitState() {
		return new Monom("component('request')");
	}

	public static List<CEOCOperation> getOperations(final Collection<? extends IComponent> components, final INumericParameterRefinementConfigurationMap paramRefinementConfig) {
		List<CEOCOperation> operations = new ArrayList<>();
		for (IComponent c : components) {
			String cName = c.getName();
			for (String i : c.getProvidedInterfaces()) {
				List<VariableParam> opParams = new ArrayList<>();
				opParams.add(new VariableParam("iGroupHandle")); // handle for the required interface to be resolved here
				opParams.add(new VariableParam("iHandle")); // handle for the required interface to be resolved here
				opParams.add(new VariableParam("cHandle")); // handle for the new component instance we create
				int j = 0;
				Map<CNFFormula, Monom> addList = new HashMap<>();
				Monom standardKnowledgeAboutNewComponent = new Monom("component(cHandle) & resolves(iHandle, '" + i + "', '" + cName + "'," + " cHandle" + ") & usedin('" + cName + "', iGroupHandle)");
				for (IParameter p : c.getParameters()) {
					String pName = p.getName();
					String paramIdentifier = "p" + (++j);
					opParams.add(new VariableParam(paramIdentifier));

					/* add the information about this parameter container */
					List<LiteralParam> literalParams = new ArrayList<>();
					literalParams.add(new ConstantParam(cName));
					literalParams.add(new ConstantParam(pName));
					literalParams.add(new VariableParam("cHandle"));
					literalParams.add(new VariableParam(paramIdentifier));
					standardKnowledgeAboutNewComponent.add(new Literal("parameterContainer", literalParams));

					/* add knowledge about initial value */
					List<LiteralParam> valParams = new ArrayList<>();
					valParams.add(new VariableParam(paramIdentifier));
					if (p.isNumeric()) {
						standardKnowledgeAboutNewComponent.add(new Literal("parameterFocus(cHandle, '" + pName + "', '" + paramRefinementConfig.getRefinement(c, p).getFocusPoint() + "')"));
						NumericParameterDomain np = (NumericParameterDomain) p.getDefaultDomain();
						valParams.add(new ConstantParam("[" + np.getMin() + "," + np.getMax() + "]"));
					} else {
						valParams.add(new ConstantParam(p.getDefaultValue().toString()));
					}
					standardKnowledgeAboutNewComponent.add(new Literal("val", valParams));
				}
				int r = 0;
				for (IRequiredInterfaceDefinition requiredInterface : c.getRequiredInterfaces()) {
					String reqIntIdentifier = "r" + (++r);
					String requiredInterfaceID = requiredInterface.getId();
					opParams.add(new VariableParam(reqIntIdentifier));
					List<LiteralParam> literalParams = new ArrayList<>();
					literalParams.add(new ConstantParam(cName));
					literalParams.add(new ConstantParam(requiredInterfaceID));
					literalParams.add(new VariableParam("cHandle"));
					literalParams.add(new VariableParam(reqIntIdentifier));
					standardKnowledgeAboutNewComponent.add(new Literal("interfaceGroup", literalParams));
				}
				addList.put(new CNFFormula(), standardKnowledgeAboutNewComponent);
				CEOCOperation newOp = new CEOCOperation(SATISFY_PREFIX + i + "With" + cName, opParams, new Monom(), addList, new HashMap<>(), new ArrayList<>());
				operations.add(newOp);
			}
		}

		/* operations to require uniqueness of components in group interface */
		Map<CNFFormula, Monom> addListUniqueness = new HashMap<>();
		addListUniqueness.put(new CNFFormula(), new Monom("uniqueComponents(iGroupHandle)"));
		operations.add(new CEOCOperation("1_requireUniqueness", "iGroupHandle", new Monom(), addListUniqueness, EMPTY_PRECONDITION, ""));

		/* operations for interface definitions */
		Map<CNFFormula, Monom> addList = new HashMap<>();
		addList.put(new CNFFormula(), new Monom("interfaceMember(iHandle, iGroupHandle, iIndex)"));
		CEOCOperation defInterfaceOp = new CEOCOperation("1_defineInterface", "iGroupHandle, iHandle, iIndex", new Monom(), addList, new HashMap<>(), "");
		operations.add(defInterfaceOp);


		/* create operations for parameter initialization */
		// redefValue(container, previousValue, newValue)
		Map<CNFFormula, Monom> redefOpAddList = new HashMap<>();
		redefOpAddList.put(new CNFFormula(), new Monom("val(container,newValue) & overwritten(container)"));
		Map<CNFFormula, Monom> redefOpDelList = new HashMap<>();
		redefOpDelList.put(new CNFFormula(), new Monom("val(container,previousValue)"));
		operations.add(new CEOCOperation(REDEF_VALUE_PREFIX, "container,previousValue,newValue", new Monom("val(container,previousValue)"), redefOpAddList, redefOpDelList, ""));

		// declareClosed(container)
		Map<CNFFormula, Monom> closeOpAddList = new HashMap<>();
		closeOpAddList.put(new CNFFormula(), new Monom("closed(container)"));
		operations.add(new CEOCOperation(DECLARE_CLOSED_PREFIX, "container", new Monom(), closeOpAddList, new HashMap<>(), ""));

		// omitResolution(c1, i , c2)
		Map<CNFFormula, Monom> omitResolutionOpAddList = new HashMap<>();
		omitResolutionOpAddList.put(new CNFFormula(), new Monom("anyOmitted(iGroupHandle) & omitted(cHandle)"));
		operations.add(new CEOCOperation(OMIT_RESOLUTION_PREFIX, "iGroupHandle,iHandle,cHandle", new Monom(), omitResolutionOpAddList, new HashMap<>(), ""));

		return operations;
	}

	public static List<OCIPMethod> getParameterRefinementMethods(final Collection<? extends IComponent> components) {

		List<OCIPMethod> methods = new ArrayList<>();

		// Non-list interfaces methods
		for (IComponent c : components) {
			String cName = c.getName();

			/* go, in an ordering that is consistent with the pre-order on the params imposed by the dependencies, over the set of params */
			if (CONFIGURE_PARAMS) {

				/* create methods for choosing/refining parameters */
				List<VariableParam> paramRefinementsParams = new ArrayList<>();
				paramRefinementsParams.add(new VariableParam("c2"));
				List<Literal> networkForRefinements = new ArrayList<>();
				StringBuilder refinementArgumentsSB = new StringBuilder();
				int j = 0;
				for (IParameter p : c.getParameters()) {
					String pName = p.getName();
					String pIdent = "p" + (++j);
					refinementArgumentsSB.append(", " + pIdent);
					paramRefinementsParams.add(new VariableParam(pIdent));
					networkForRefinements.add(new Literal(REFINE_PARAMETER_PREFIX + pName + "Of" + cName + "(c2, " + pIdent + ")"));
					// ignoreParamRefinementFor<p>Of<c>(object, container, curval)
					methods.add(getMethodIgnoreParamRefinement(cName, pName));
					// refineParam<p>Of<c>(c2, p1, ..., pm)
					methods.add(getMethodRefineParam(cName, pName));
				}
				networkForRefinements.add(new Literal(REFINE_PARAMETERS_PREFIX + cName + "(c2" + refinementArgumentsSB.toString() + ")"));
				// refineParamsOf<c>(c2, p1, ..., pm)
				methods.add(new OCIPMethod("refineParamsOf" + cName, paramRefinementsParams, new Literal(REFINE_PARAMETERS_PREFIX + cName + "(c2" + refinementArgumentsSB.toString() + ")"), new Monom(COMPONENT_OF_C2),
						new TaskNetwork(networkForRefinements), false, new ArrayList<>(), new Monom("!refinementCompleted('" + cName + "', c2)")));
				// closeRefinementOfParamsOf<c>(c2, p1, ..., pm)
				methods.add(new OCIPMethod("closeRefinementOfParamsOf" + cName, paramRefinementsParams, new Literal(REFINE_PARAMETERS_PREFIX + cName + "(c2" + refinementArgumentsSB.toString() + ")"), new Monom(COMPONENT_OF_C2),
						new TaskNetwork(), false, new ArrayList<>(), new Monom("refinementCompleted('" + cName + "', c2)")));
			}
		}
		return methods;
	}

	public static List<OCIPMethod> getMethodsToResolveInterfaceWithComponent(final Collection<? extends IComponent> components) {

		List<OCIPMethod> methods = new ArrayList<>();
		// Non-list interfaces methods
		for (IComponent c : components) {
			String cName = c.getName();

			// resolve<i>With<c>(c1; c2, p1, ..., pm, r1, ..., rn)
			/* create methods for the refinement of the interfaces offered by this component */
			for (String i : c.getProvidedInterfaces()) {
				List<VariableParam> inputParams = Arrays.asList(new VariableParam("iGroupHandle"), new VariableParam("iHandle"), new VariableParam("cHandle"));
				List<VariableParam> outputParams = new ArrayList<>();
				Collection<IRequiredInterfaceDefinition> requiredInterfaces = c.getRequiredInterfaces();

				/* create string for the arguments of this operation */
				StringBuilder satisfyOpArgumentsSB = new StringBuilder();
				if (CONFIGURE_PARAMS) {
					for (int j = 1; j <= c.getParameters().size(); j++) {
						String paramIdentifier = "p" + j;
						satisfyOpArgumentsSB.append(", " + paramIdentifier);
					}
				}
				for (int r = 1; r <= requiredInterfaces.size(); r++) {
					satisfyOpArgumentsSB.append(",iSubGroup_" + r);
				}

				/* configure task network for this method */
				List<Literal> network = new ArrayList<>();
				network.add(new Literal(SATISFY_PREFIX + i + "With" + cName + "(iGroupHandle, iHandle, cHandle" + satisfyOpArgumentsSB.toString() + ")"));
				int r = 0;
				for (IRequiredInterfaceDefinition ir : requiredInterfaces) {
					outputParams.add(new VariableParam("iSubGroup_" + (r+1)));
					network.add(new Literal(RESOLVE_IFACE_GROUP_PREFIX + ir.getName() + "(cHandle, iSubGroup_" + (r + 1) + ")"));
					r ++;
				}

				/* */
				StringBuilder refinementArgumentsSB = new StringBuilder();
				if (CONFIGURE_PARAMS) {
					for (int j = 1; j <= c.getParameters().size(); j++) {
						String paramIdentifier = "p" + j;
						outputParams.add(new VariableParam(paramIdentifier));
						refinementArgumentsSB.append(", " + paramIdentifier);
					}
				}
				network.add(new Literal(REFINE_PARAMETERS_PREFIX + cName + "(cHandle" + refinementArgumentsSB.toString() + ")"));

				/* create the outputs of this method and add the method to the collection */
				List<VariableParam> methodParams = new ArrayList<>();
				methodParams.addAll(inputParams);
				methodParams.addAll(outputParams);
				methods.add(new OCIPMethod("resolve" + i + "With" + cName, methodParams, new Literal(RESOLVE_SINGLE + i + "(iGroupHandle, iHandle, cHandle)"), new Monom("!uniqueComponents(iGroupHandle)"), new TaskNetwork(network), false, outputParams, new Monom()));
				methods.add(new OCIPMethod("resolveUniquely" + i + "With" + cName, methodParams, new Literal(RESOLVE_SINGLE + i + "(iGroupHandle, iHandle, cHandle)"), new Monom("uniqueComponents(iGroupHandle) & !usedin('" + c.getName() + "', iGroupHandle)"), new TaskNetwork(network), false, outputParams, new Monom()));
			}
		}
		return methods;
	}

	public static List<OCIPMethod> getMethodsToResolveInterfaceGroup(final Collection<? extends IComponent> components) {
		List<OCIPMethod> methods = new ArrayList<>();

		// Non-list interfaces methods
		for (IComponent c : components) {
			for (IRequiredInterfaceDefinition ri : c.getRequiredInterfaces()) {
				List<VariableParam> methodInputs = Arrays.asList(new VariableParam("cHandle"), new VariableParam("iGroupHandle"));
				List<VariableParam> methodOutputs = new ArrayList<>();
				List<Literal> network = new ArrayList<>();
				if (ri.isUniqueComponents()) {
					network.add(new Literal("1_requireUniqueness(iGroupHandle)"));
				}
				for (int j = 1; j <= ri.getMax(); j++) {
					methodOutputs.add(new VariableParam("ri_" + j));
					methodOutputs.add(new VariableParam("cHandle_" + j));
					network.add(new Literal("1_defineInterface(iGroupHandle, ri_" + j +", '" + j + "')"));
				}

				// Tasks: tResolveSingle<i>(c1, c2_1)... tResolveSingle<i>(c1, c2_<min(I)>)
				for (int j = 1; j <= ri.getMin(); j++) {
					network.add(new Literal(RESOLVE_SINGLE + ri.getName() + "(iGroupHandle, ri_" + j + ", cHandle_" + j + ")"));
				}

				// Tasks: tResolveSingleOptional<i>(c1, c2_<min(I)+1>)... tResolveSingleOptional<i>(c1, c2_<max(I)>)
				for (int j = ri.getMin() + 1; j <= ri.getMax(); j++) {
					network.add(new Literal(RESOLVE_SINGLE_OPTIONAL + ri.getName() + "(iGroupHandle, ri_" + j + ", cHandle_" + j + ")"));
				}

				List<VariableParam> methodParams = new ArrayList<>();
				methodParams.addAll(methodInputs);
				methodParams.addAll(methodOutputs);
				methods.add(new OCIPMethod("resolveGroup" + ri.getName(), methodParams, new Literal(RESOLVE_IFACE_GROUP_PREFIX + ri.getName() + "(cHandle, iGroupHandle)"), new Monom(), new TaskNetwork(network), false, methodOutputs, new Monom()));
				if (ri.isOptional()) {
					methods.add(new OCIPMethod("ignoreGroup" + ri.getName(), methodParams, new Literal(RESOLVE_IFACE_GROUP_PREFIX + ri.getName() + "(cHandle, iGroupHandle)"), new Monom(), new TaskNetwork(), false, methodOutputs, new Monom()));
				}
			}
		}
		return methods;
	}

	public static List<OCIPMethod> getInterfaceResolutionMethods(final Collection<? extends IComponent> components) {
		List<OCIPMethod> methods = new ArrayList<>();

		methods.addAll(getMethodsToResolveInterfaceWithComponent(components));
		methods.addAll(getMethodsToResolveInterfaceGroup(components));

		// Non-list interfaces methods
		for (IComponent c : components) {
			for (IRequiredInterfaceDefinition ri : c.getRequiredInterfaces()) {
				List<VariableParam> methodParams = new ArrayList<>();
				List<Literal> network = new ArrayList<>();
				List<VariableParam> methodOutputs = new ArrayList<>();

				// <<=| doResolve<i>(c1, c2) |=>>
				methodParams.add(new VariableParam("iGroupHandle"));
				methodParams.add(new VariableParam("iHandle"));
				methodParams.add(new VariableParam("cHandle"));

				network.add(new Literal(RESOLVE_SINGLE + ri.getName() + "(iGroupHandle, iHandle, cHandle)"));

				String condition = "!anyOmitted(iGroupHandle)";
				methods.add(
						new OCIPMethod("doResolve" + ri.getName(), methodParams, new Literal(RESOLVE_SINGLE_OPTIONAL + ri.getName() + "(iGroupHandle, iHandle, cHandle)"), new Monom(condition), new TaskNetwork(network), false, methodOutputs, new Monom()));
				network = new ArrayList<>();

				// <<=| doNotResolve<i>(c1, c2) |=>>
				network.add(new Literal(OMIT_RESOLUTION_PREFIX + "(iGroupHandle, iHandle, cHandle)"));
				methods.add(new OCIPMethod("doNotResolve" + ri.getName(), methodParams, new Literal(RESOLVE_SINGLE_OPTIONAL + ri.getName() + "(iGroupHandle, iHandle, cHandle)"), new Monom(), new TaskNetwork(network), false, methodOutputs, new Monom()));
			}
		}

		return methods;
	}

	public static List<OCIPMethod> getMethods(final Collection<? extends IComponent> components) {
		List<OCIPMethod> methods = new ArrayList<>();
		methods.addAll(getInterfaceResolutionMethods(components));
		methods.addAll(getParameterRefinementMethods(components));
		return methods;
	}

	public static OCIPMethod getMethodIgnoreParamRefinement(final String cName, final String pName) {
		return new OCIPMethod("ignoreParamRefinementFor" + pName + "Of" + cName, "object, container, curval", new Literal(REFINE_PARAMETER_PREFIX + pName + "Of" + cName + "(object,container)"),
				new Monom("parameterContainer('" + cName + "', '" + pName + "', object, container) & val(container,curval) & overwritten(container)"), new TaskNetwork(DECLARE_CLOSED_PREFIX + "(container)"), false, "",
				new Monom("notRefinable('" + cName + "', object, '" + pName + "', container, curval)"));
	}

	public static OCIPMethod getMethodRefineParam(final String cName, final String pName) {
		return new OCIPMethod("refineParam" + pName + "Of" + cName, "object, container, curval, newval", new Literal(REFINE_PARAMETER_PREFIX + pName + "Of" + cName + "(object,container)"),
				new Monom("parameterContainer('" + cName + "', '" + pName + "', object, container) & val(container,curval)"), new TaskNetwork(REDEF_VALUE_PREFIX + "(container,curval,newval)"), false, "",
				new Monom("isValidParameterRangeRefinement('" + cName + "', object, '" + pName + "', container, curval, newval)"));
	}

	public CEOCIPSTNPlanningDomain getPlanningDomain() {
		return new CEOCIPSTNPlanningDomain(getOperations(this.components, this.paramRefinementConfig), getMethods(this.components));
	}

	public CEOCIPSTNPlanningProblem getPlanningProblem(final CEOCIPSTNPlanningDomain domain, final CNFFormula knowledge, final Monom init) {
		Map<String, EvaluablePredicate> evaluablePredicates = new HashMap<>();
		evaluablePredicates.put("isValidParameterRangeRefinement", new IsValidParameterRangeRefinementPredicate(this.components, this.paramRefinementConfig));
		evaluablePredicates.put("notRefinable", new IsNotRefinablePredicate(this.components, this.paramRefinementConfig));
		evaluablePredicates.put("refinementCompleted", new IsRefinementCompletedPredicate(this.components, this.paramRefinementConfig));
		return new CEOCIPSTNPlanningProblem(domain, knowledge, init, new TaskNetwork(RESOLVE_SINGLE + this.originalProblem.getRequiredInterface() + "('rGroup', 'request', 'solution')"), evaluablePredicates, new HashMap<>());
	}

	public CEOCIPSTNPlanningProblem getPlanningProblem() {
		return this.getPlanningProblem(this.getPlanningDomain(), new CNFFormula(), getInitState());
	}

	/**
	 * This method is a utility for everybody who wants to work on the graph obtained from HASCO's reduction but without using the search logic of HASCO
	 *
	 * @param plannerFactory
	 * @return
	 */
	public <T, A> IGraphGenerator<T, A> getGraphGeneratorUsedByHASCOForSpecificPlanner(final IHASCOPlanningReduction<T, A> transformer) {
		return transformer.encodeProblem(this.getPlanningProblem()).getGraphGenerator();
	}

	@Override
	public CostSensitiveHTNPlanningProblem<CEOCIPSTNPlanningProblem, V> encodeProblem(final RefinementConfiguredSoftwareConfigurationProblem<V> problem) {

		if (problem.getRequiredInterface() == null) {
			throw new IllegalArgumentException("No required interface defined in the problem!");
		}

		/* set object variables that will be important for several methods in the reduction */
		this.originalProblem = problem;
		this.components = this.originalProblem.getComponents();
		this.paramRefinementConfig = this.originalProblem.getParamRefinementConfig();

		/* build the cost insensitive planning problem */
		CEOCIPSTNPlanningProblem planningProblem = this.getPlanningProblem();

		/* derive a plan evaluator from the configuration evaluator */
		return new CostSensitiveHTNPlanningProblem<>(planningProblem, new HASCOReductionSolutionEvaluator<>(problem, this));
	}

	@Override
	public ComponentInstance decodeSolution(final IEvaluatedPlan<V> solution) {
		return this.decodeSolution((IPlan) solution);
	}

	public ComponentInstance decodeSolution(final IPlan plan) {
		return HASCOUtil.getSolutionCompositionForPlan(HASCOReduction.this.components, getInitState(), plan, true);
	}
}
