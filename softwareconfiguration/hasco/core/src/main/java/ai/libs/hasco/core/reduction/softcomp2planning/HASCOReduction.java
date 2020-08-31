package ai.libs.hasco.core.reduction.softcomp2planning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.api4.java.datastructure.graph.implicit.IGraphGenerator;

import ai.libs.hasco.core.HASCOUtil;
import ai.libs.hasco.core.predicate.IsNotRefinablePredicate;
import ai.libs.hasco.core.predicate.IsRefinementCompletedPredicate;
import ai.libs.hasco.core.predicate.IsValidParameterRangeRefinementPredicate;
import ai.libs.hasco.core.reduction.planning2search.IHASCOPlanningReduction;
import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.Interface;
import ai.libs.jaicore.components.model.NumericParameterDomain;
import ai.libs.jaicore.components.model.Parameter;
import ai.libs.jaicore.components.model.ParameterRefinementConfiguration;
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

	// component selection
	private static final String RESOLVE_COMPONENT_IFACE_PREFIX = "1_tResolve";
	private static final String SATISFY_PREFIX = "1_satisfy";

	// component configuration
	private static final String REFINE_PARAMETERS_PREFIX = "2_tRefineParamsOf";
	private static final String REFINE_PARAMETER_PREFIX = "2_tRefineParam";
	private static final String DECLARE_CLOSED_PREFIX = "2_declareClosed";
	private static final String REDEF_VALUE_PREFIX = "2_redefValue";

	private static final String COMPONENT_OF_C1 = "component(c1)";
	private static final String COMPONENT_OF_C2 = "component(c2)";

	private RefinementConfiguredSoftwareConfigurationProblem<V> originalProblem;

	/* working variables */
	private Collection<Component> components;
	private Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramRefinementConfig;

	public static Monom getInitState() {
		return new Monom("component('request')");
	}

	public static List<CEOCOperation> getOperations(final Collection<Component> components, final Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramRefinementConfig) {
		List<CEOCOperation> operations = new ArrayList<>();
		for (Component c : components) {
			String cName = c.getName();
			for (String i : c.getProvidedInterfaces()) {
				List<VariableParam> opParams = new ArrayList<>();
				opParams.add(new VariableParam("c1"));
				opParams.add(new VariableParam("c2"));
				int j = 0;
				Map<CNFFormula, Monom> addList = new HashMap<>();
				Monom standardKnowledgeAboutNewComponent = new Monom("component(c2) & resolves(c1, '" + i + "', '" + cName + "'," + " c2" + ")");
				for (Parameter p : c.getParameters()) {
					String pName = p.getName();
					String paramIdentifier = "p" + (++j);
					opParams.add(new VariableParam(paramIdentifier));

					/* add the information about this parameter container */
					List<LiteralParam> literalParams = new ArrayList<>();
					literalParams.add(new ConstantParam(cName));
					literalParams.add(new ConstantParam(pName));
					literalParams.add(new VariableParam("c2"));
					literalParams.add(new VariableParam(paramIdentifier));
					standardKnowledgeAboutNewComponent.add(new Literal("parameterContainer", literalParams));

					/* add knowledge about initial value */
					List<LiteralParam> valParams = new ArrayList<>();
					valParams.add(new VariableParam(paramIdentifier));
					if (p.isNumeric()) {
						standardKnowledgeAboutNewComponent.add(new Literal("parameterFocus(c2, '" + pName + "', '" + paramRefinementConfig.get(c).get(p).getFocusPoint() + "')"));
						NumericParameterDomain np = (NumericParameterDomain) p.getDefaultDomain();
						valParams.add(new ConstantParam("[" + np.getMin() + "," + np.getMax() + "]"));
					} else {
						valParams.add(new ConstantParam(p.getDefaultValue().toString()));
					}
					standardKnowledgeAboutNewComponent.add(new Literal("val", valParams));
				}
				int r = 0;
				for (Interface requiredInterface : c.getRequiredInterfaces()) {
					String reqIntIdentifier = "r" + (++r);
					String requiredInterfaceID = requiredInterface.getId();
					opParams.add(new VariableParam(reqIntIdentifier));
					List<LiteralParam> literalParams = new ArrayList<>();
					literalParams.add(new ConstantParam(cName));
					literalParams.add(new ConstantParam(requiredInterfaceID));
					literalParams.add(new VariableParam("c2"));
					literalParams.add(new VariableParam(reqIntIdentifier));
					standardKnowledgeAboutNewComponent.add(new Literal("interfaceIdentifier", literalParams));
				}
				addList.put(new CNFFormula(), standardKnowledgeAboutNewComponent);
				CEOCOperation newOp = new CEOCOperation(SATISFY_PREFIX + i + "With" + cName, opParams, new Monom(COMPONENT_OF_C1), addList, new HashMap<>(), new ArrayList<>());
				operations.add(newOp);
			}
		}

		/* create operations for parameter initialization */
		Map<CNFFormula, Monom> redefOpAddList = new HashMap<>();
		redefOpAddList.put(new CNFFormula(), new Monom("val(container,newValue) & overwritten(container)"));
		Map<CNFFormula, Monom> redefOpDelList = new HashMap<>();
		redefOpDelList.put(new CNFFormula(), new Monom("val(container,previousValue)"));
		operations.add(new CEOCOperation(REDEF_VALUE_PREFIX, "container,previousValue,newValue", new Monom("val(container,previousValue)"), redefOpAddList, redefOpDelList, ""));
		Map<CNFFormula, Monom> closeOpAddList = new HashMap<>();
		closeOpAddList.put(new CNFFormula(), new Monom("closed(container)"));
		operations.add(new CEOCOperation(DECLARE_CLOSED_PREFIX, "container", new Monom(), closeOpAddList, new HashMap<>(), ""));
		return operations;
	}

	public static List<OCIPMethod> getMethodsList(final Collection<Component> components){
		List<OCIPMethod> methods = new ArrayList<>();
		
		// Recorro los componentes
		for(Component c: components) {
			String cname = c.getName();
			
			//Recorro las interfaces provistas de cada componente
			for (Interface i : c.getRequiredInterfaces()) {
				String methodParams = "c1";
				
				//Configuro el tasknetwork del metodo resolve<i>(c1; c2_1,..,c2_<max(I)>)
				List<Literal> network = new ArrayList();
				//String output = "";
				String condition ="!anyOmitted(c1,'"+i+"')";
				
				//Recorro la cantidad de interfaces requeridas mínimas
				for(int x = 1; x <= i.getMin(); x++) {
					network.add(new Literal("1_tResolveSingle" + i + "("+cname+", c2_"+x+")"));	
					VariableParam output = new VariableParam("c2_"+x);
					methodOutputs.add(output);
				}
				
				//Recorro la cantidad de interfaces requeridas opcionales
				for(int x = i.getMin() + 1; x <= i.getMax(); x++) {
					network.add(new Literal("1_tResolveSingleOptional"+i+"("+cname+",c2_"+x+")"));
					VariableParam output = new VariableParam("c2_"+x);
					methodOutputs.add(output);
				}
				
				methods.add(new OCIPMethod("resolve" + i, methodParams, new Literal("1_tResolve" + i + "(c1)"), new Monom(COMPONENT_OF_C1), new TaskNetwork(network), false, methodOutputs, new Monom()));				
				//doResolve
				methodParams.add(new VariableParam("c2"));
				
				methodOutputs.clear();
				
				network.add(new Literal("1_tResolveSingle" + i + "("+cname+", c2)"));	
				
				//Falta agregar la condición de AnyOmitted
				methods.add(new OCIPMethod("doResolve" + i, methodParams, new Literal("1_tResolveSingleOptional" + i + "("+cname+"c2"+")"), new Monom(COMPONENT_OF_C1 + " & " + condition), new TaskNetwork(network), false, methodOutputs, new Monom()));	
				
				
				//network.add(new Literal("omitResolution("+cname+", "+ "'"+i+"'"+", c2)"));	
				
				methods.add(new OCIPMethod("doNotResolve" + i, methodParams, new Literal("1_tResolveSingleOptional" + i + "("+cname+"c2"+")"), new Monom(COMPONENT_OF_C1), new TaskNetwork(network), false, methodOutputs, new Monom()));	
			}
			
			/* create methods for the refinement of the interfaces offered by this component */
			for (String i : c.getProvidedInterfaces()) {
				List<VariableParam> methodParams = new ArrayList<>();
				VariableParam inputParam = new VariableParam("c1");
				methodParams.add(inputParam);
				methodParams.add(new VariableParam("c2"));
				List<Interface> requiredInterfaces = c.getRequiredInterfaces();
				String condition ="!anyOmitted(c1,'"+i+"')";
				
				/* create string for the arguments of this operation */
				StringBuilder satisfyOpArgumentsSB = new StringBuilder();
				if (CONFIGURE_PARAMS) {
					for (int j = 1; j <= c.getParameters().size(); j++) {
						String paramIdentifier = "p" + j;
						satisfyOpArgumentsSB.append(", " + paramIdentifier);
					}
				}
				for (int r = 1; r <= requiredInterfaces.size(); r++) {
					satisfyOpArgumentsSB.append(",r" + r);
				}

				/* configure task network for this method */
				List<Literal> network = new ArrayList<>();
				int r = 0;
				network.add(new Literal(SATISFY_PREFIX + i + "With" + cname + "(c1, c2" + satisfyOpArgumentsSB.toString() + ")"));
				for (Interface requiredInterface : requiredInterfaces) {
					String paramName = "r" + (++r);
					methodParams.add(new VariableParam(paramName));
					network.add(new Literal(RESOLVE_COMPONENT_IFACE_PREFIX + requiredInterface.getName() + "(c2," + paramName + ")"));
				}
				StringBuilder refinementArgumentsSB = new StringBuilder();
				if (CONFIGURE_PARAMS) {
					for (int j = 1; j <= c.getParameters().size(); j++) {
						String paramIdentifier = "p" + j;
						methodParams.add(new VariableParam(paramIdentifier));
						refinementArgumentsSB.append(", " + paramIdentifier);
					}
				}
				network.add(new Literal(REFINE_PARAMETERS_PREFIX + cname + "(c2" + refinementArgumentsSB.toString() + ")"));

				/* create the outputs of this method and add the method to the collection */
				List<VariableParam> outputs = methodParams.stream().filter(p -> !p.equals(inputParam)).collect(Collectors.toList());
				
				//Falta agregar la nueva condición
				methods.add(new OCIPMethod("resolve" + i + "With" + cname, methodParams, new Literal("tResolveSingle" + i + "(c1,c2)"), new Monom(COMPONENT_OF_C1 + " & " + condition), new TaskNetwork(network), false, outputs, new Monom()));
			}
						
		}
		 
		return methods;
	}
	
	public static List<OCIPMethod> getMethods(final Collection<Component> components) {
		List<OCIPMethod> methods = new ArrayList<>();
		for (Component c : components) {
			String cName = c.getName();

			/* create methods for the refinement of the interfaces offered by this component */
			for (String i : c.getProvidedInterfaces()) {
				List<VariableParam> methodParams = new ArrayList<>();
				VariableParam inputParam = new VariableParam("c1");
				methodParams.add(inputParam);
				methodParams.add(new VariableParam("c2"));
				List<Interface> requiredInterfaces = c.getRequiredInterfaces();

				/* create string for the arguments of this operation */
				StringBuilder satisfyOpArgumentsSB = new StringBuilder();
				if (CONFIGURE_PARAMS) {
					for (int j = 1; j <= c.getParameters().size(); j++) {
						String paramIdentifier = "p" + j;
						satisfyOpArgumentsSB.append(", " + paramIdentifier);
					}
				}
				for (int r = 1; r <= requiredInterfaces.size(); r++) {
					satisfyOpArgumentsSB.append(",r" + r);
				}

				/* configure task network for this method */
				List<Literal> network = new ArrayList<>();
				int r = 0;
				network.add(new Literal(SATISFY_PREFIX + i + "With" + cName + "(c1, c2" + satisfyOpArgumentsSB.toString() + ")"));
				for (Interface requiredInterface : requiredInterfaces) {
					String paramName = "r" + (++r);
					methodParams.add(new VariableParam(paramName));
					network.add(new Literal(RESOLVE_COMPONENT_IFACE_PREFIX + requiredInterface.getName() + "(c2," + paramName + ")"));
				}
				StringBuilder refinementArgumentsSB = new StringBuilder();
				if (CONFIGURE_PARAMS) {
					for (int j = 1; j <= c.getParameters().size(); j++) {
						String paramIdentifier = "p" + j;
						methodParams.add(new VariableParam(paramIdentifier));
						refinementArgumentsSB.append(", " + paramIdentifier);
					}
				}
				network.add(new Literal(REFINE_PARAMETERS_PREFIX + cName + "(c2" + refinementArgumentsSB.toString() + ")"));

				/* create the outputs of this method and add the method to the collection */
				List<VariableParam> outputs = methodParams.stream().filter(p -> !p.equals(inputParam)).collect(Collectors.toList());
				methods.add(new OCIPMethod("resolve" + i + "With" + cName, methodParams, new Literal(RESOLVE_COMPONENT_IFACE_PREFIX + i + "(c1,c2)"), new Monom(COMPONENT_OF_C1), new TaskNetwork(network), false, outputs, new Monom()));
			}

			/* go, in an ordering that is consistent with the pre-order on the params imposed by the dependencies, over the set of params */
			if (CONFIGURE_PARAMS) {

				/* create methods for choosing/refining parameters */
				List<VariableParam> paramRefinementsParams = new ArrayList<>();
				paramRefinementsParams.add(new VariableParam("c2"));
				List<Literal> networkForRefinements = new ArrayList<>();
				StringBuilder refinementArgumentsSB = new StringBuilder();
				int j = 0;
				for (Parameter p : c.getParameters()) {
					String pName = p.getName();
					String pIdent = "p" + (++j);
					refinementArgumentsSB.append(", " + pIdent);
					paramRefinementsParams.add(new VariableParam(pIdent));
					networkForRefinements.add(new Literal(REFINE_PARAMETER_PREFIX + pName + "Of" + cName + "(c2, " + pIdent + ")"));
					methods.add(getMethodIgnoreParamRefinement(cName, pName));
					methods.add(getMethodRefineParam(cName, pName));
				}
				networkForRefinements.add(new Literal(REFINE_PARAMETERS_PREFIX + cName + "(c2" + refinementArgumentsSB.toString() + ")"));
				methods.add(new OCIPMethod("refineParamsOf" + cName, paramRefinementsParams, new Literal(REFINE_PARAMETERS_PREFIX + cName + "(c2" + refinementArgumentsSB.toString() + ")"), new Monom(COMPONENT_OF_C2),
						new TaskNetwork(networkForRefinements), false, new ArrayList<>(), new Monom("!refinementCompleted('" + cName + "', c2)")));
				methods.add(new OCIPMethod("closeRefinementOfParamsOf" + cName, paramRefinementsParams, new Literal(REFINE_PARAMETERS_PREFIX + cName + "(c2" + refinementArgumentsSB.toString() + ")"), new Monom(COMPONENT_OF_C2),
						new TaskNetwork(), false, new ArrayList<>(), new Monom("refinementCompleted('" + cName + "', c2)")));
			}
		}
		return methods;
	}

	public static OCIPMethod getMethodIgnoreParamRefinement(final String cName, final String pName) {
		return new OCIPMethod("ignoreParamRefinementFor" + pName + "Of" + cName, "object, container, curval", new Literal(REFINE_PARAMETER_PREFIX + pName + "Of" + cName + "(object,container)"),
				new Monom("parameterContainer('" + cName + "', '" + pName + "', object, container) & val(container,curval) & overwritten(container)"), new TaskNetwork(DECLARE_CLOSED_PREFIX + "(container)"), false, "",
				new Monom("notRefinable('" + cName + "', object, '" + pName + "', container, curval)"));
	}

	public static  OCIPMethod getMethodRefineParam(final String cName, final String pName) {
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
		return new CEOCIPSTNPlanningProblem(domain, knowledge, init, new TaskNetwork(RESOLVE_COMPONENT_IFACE_PREFIX + this.originalProblem.getRequiredInterface() + "('request', 'solution')"), evaluablePredicates, new HashMap<>());
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
