package jaicore.planning.graphgenerators.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.sets.SetUtil;
import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.LiteralParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;
import jaicore.logic.fol.theories.EvaluablePredicate;
import jaicore.logic.fol.util.LogicUtil;
import jaicore.planning.model.ceoc.CEOCAction;
import jaicore.planning.model.ceoc.CEOCOperation;
import jaicore.planning.model.core.Action;
import jaicore.planning.model.core.Operation;
import jaicore.planning.model.task.ceocipstn.OCIPMethod;
import jaicore.planning.model.task.ceocstn.OCMethod;
import jaicore.planning.model.task.stn.Method;
import jaicore.planning.model.task.stn.MethodInstance;
import jaicore.planning.model.task.stn.STNPlanningDomain;
import jaicore.planning.model.task.stn.TaskNetwork;

public class TaskPlannerUtil {

	private static final Logger logger = LoggerFactory.getLogger(TaskPlannerUtil.class);

	private Map<String, EvaluablePredicate> evaluablePlanningPredicates;

	public TaskPlannerUtil(Map<String, EvaluablePredicate> evaluablePlanningPredicates) {
		super();
		this.evaluablePlanningPredicates = evaluablePlanningPredicates;
	}

	public Collection<MethodInstance> getMethodInstancesForTaskThatAreApplicableInState(CNFFormula knowledge, Collection<? extends Method> methods, Literal task, Monom state,
			List<Literal> remainingProblems) {
		Collection<MethodInstance> applicableDerivedMethods = new ArrayList<>();
		for (Method m : methods) {
			if (m.getTask().getPropertyName().equals(task.getPropertyName())) {
				applicableDerivedMethods.addAll(getMethodInstancesForTaskThatAreApplicableInState(knowledge, m, task, state, remainingProblems));
			}
		}
		return applicableDerivedMethods;
	}

	public Collection<MethodInstance> getMethodInstancesForTaskThatAreApplicableInState(CNFFormula knowledge, Method method, Literal task, Monom state,
			List<Literal> remainingProblems) {
		Collection<MethodInstance> applicableDerivedMethodInstances = new ArrayList<>();
		Collection<Map<VariableParam, LiteralParam>> maps = getMappingsThatMatchTasksAndMakesItApplicable(knowledge, method.getTask(), task, method.getPrecondition(), state);
		for (Map<VariableParam, LiteralParam> grounding : maps) {
			
			/* create a copy of the grounding */
			Map<VariableParam, ConstantParam> basicConstantGrounding = new HashMap<>();
			for (VariableParam key : grounding.keySet()) {
				basicConstantGrounding.put(key, (ConstantParam) grounding.get(key));
			}

			/* up to where, we only have considered the "normal" parameters. Now check whether additional inputs bindings are indicated by interpreted predicates */
			Collection<Map<VariableParam, ConstantParam>> extendedGroundings = new ArrayList<>();
			
			/* this block is to cater for methods that have interpreted predicates and need to be oracled for valid groundings */
			if (method instanceof OCIPMethod) {
				OCIPMethod castedMethod = (OCIPMethod) method;
				Collection<VariableParam> ungroundParamsInEvaluablePrecondition = SetUtil.difference(castedMethod.getEvaluablePrecondition().getVariableParams(),
						basicConstantGrounding.keySet());

				Map<Literal, EvaluablePredicate> evaluablePredicatesForLiterals = new HashMap<>();
				for (Literal l : castedMethod.getEvaluablePrecondition()) {
					if (evaluablePlanningPredicates == null || !evaluablePlanningPredicates.containsKey(l.getPropertyName()))
						throw new IllegalArgumentException("The literal " + l + " is used in an evaluated precondition, but no evaluator has been specified for it.");
					evaluablePredicatesForLiterals.put(l, evaluablePlanningPredicates.get(l.getPropertyName()));
				}

				/* now try to ground still unground parameters */
				if (!ungroundParamsInEvaluablePrecondition.isEmpty()) {

					/*
					 * We first try that by using oracles sort the literals in the precondition by (a) oracable vs. not oracable and (b) among the oracable, sort by number of params that need to be
					 * oracled this will allow us to use oracle results of earlier predicates for later ones
					 **/
					Queue<Literal> literalsOrderedByOracability = new LinkedList<>(castedMethod.getEvaluablePrecondition().stream().sorted((l1, l2) -> {
						EvaluablePredicate el1 = evaluablePredicatesForLiterals.get(l1);
						EvaluablePredicate el2 = evaluablePredicatesForLiterals.get(l2);
						if (el1.isOracable() != el2.isOracable()) {
							return el1.isOracable() ? -1 : 1;
						}
						if (!el1.isOracable())
							return 0;
						int ungroundParamsInL1 = SetUtil.intersection(ungroundParamsInEvaluablePrecondition, l1.getParameters()).size();
						int ungroundParamsInL2 = SetUtil.intersection(ungroundParamsInEvaluablePrecondition, l2.getParameters()).size();
						return ungroundParamsInL1 - ungroundParamsInL2;
					}).collect(Collectors.toList()));

					/* just a brief check whether there are oracable literals */
					if (!evaluablePredicatesForLiterals.get(literalsOrderedByOracability.peek()).isOracable())
						throw new IllegalArgumentException("None of the literals " + literalsOrderedByOracability + " is oracable");

					List<Map<VariableParam, ConstantParam>> oracleGroundings = new ArrayList<>();
					oracleGroundings.add(basicConstantGrounding);
					getOracleGroundings(ungroundParamsInEvaluablePrecondition, literalsOrderedByOracability, state, new HashSet<>(), oracleGroundings, basicConstantGrounding);
					extendedGroundings.addAll(oracleGroundings);
				}
				
				/* all parameters are ground, we just need to test the predicate */
				else {
					boolean allSatisfied = true;
					for (Literal l : castedMethod.getEvaluablePrecondition()) {
						ConstantParam[] params = new ConstantParam[l.getParameters().size()];
						for (int i = 0; i < params.length; i++) {
							LiteralParam param = l.getParameters().get(i);
							params[i] = (param instanceof ConstantParam) ? (ConstantParam)param : basicConstantGrounding.get(param);
						}
						if (evaluablePredicatesForLiterals.get(l).test(state, params) != l.isPositive()) {
							allSatisfied = false;
							break;
						}
					}
					if (allSatisfied)
						extendedGroundings.add(basicConstantGrounding);
				}
			} else
				extendedGroundings.add(basicConstantGrounding);

			/* now add a method application for each of the extended groundings */
			for (Map<VariableParam, ConstantParam> extendedGrounding : extendedGroundings) {

				/* create new objects for unassigned open output variables */
				Set<ConstantParam> knownConstants = new HashSet<>(state.getConstantParams());
				for (Literal l : remainingProblems) {
					knownConstants.addAll(l.getConstantParams());
				}
				Collection<VariableParam> unboundParams = SetUtil.difference(method.getParameters(), extendedGrounding.keySet());
				
				
				if (method instanceof OCMethod) {
					Collection<VariableParam> unboundOutputParams = SetUtil.intersection(unboundParams, ((OCMethod) method).getOutputs());
					assert unboundOutputParams.equals(unboundParams) : "Some of the inputs of method " + method.getName() + " have not been ground. Unground inputs: " + SetUtil.difference(unboundParams, unboundOutputParams);
					int indexForNewVariable = 1;
					for (VariableParam v : unboundOutputParams) {
						ConstantParam p;
						do {
							p = new ConstantParam("newVar" + (indexForNewVariable++));
						} while (knownConstants.contains(p));
						extendedGrounding.put(v, p);
						unboundParams.remove(v);
					}
					assert unboundParams.isEmpty() : "Method " + method.getName() + " must be ground completely before processing. Here, " + unboundParams + " are unground.";
				}
				else if (!unboundParams.isEmpty()) {
					throw new IllegalStateException("Could not compute a complete grounding for method " + method.getName() + ". The following parameters were not ground until the end: " + unboundParams);
				}
				
				applicableDerivedMethodInstances.add(new MethodInstance(method, extendedGrounding));
			}
		}
		return applicableDerivedMethodInstances;
	}

	private void getOracleGroundings(Collection<VariableParam> ungroundParamsInEvaluablePrecondition, Queue<Literal> literalsOrderedByOracability, Monom state,
			Set<VariableParam> paramsGroundSoFar, Collection<Map<VariableParam, ConstantParam>> groundingsFixedSoFar, Map<VariableParam, ConstantParam> basicConstantGrounding) {
		if (literalsOrderedByOracability.isEmpty())
			return;

		Literal l = literalsOrderedByOracability.poll();

		/* check whether the literal only needs to be queried for one param */
		Collection<LiteralParam> paramsThatNeedGrounding = SetUtil.intersection(SetUtil.difference(ungroundParamsInEvaluablePrecondition, paramsGroundSoFar), l.getParameters());
		logger.info("Now checking validity of {}. Set of params that still need grounding: {}", l, paramsThatNeedGrounding);
		if (paramsThatNeedGrounding.size() > 1)
			throw new UnsupportedOperationException("Currently only support for at most one unground variable! Here, the following variables of \"" + l + "\"need grounding: " + paramsThatNeedGrounding);

		/* now go over all previous groundings and check them */
		List<Map<VariableParam, ConstantParam>> localCopyOfCurrentGrounding = new ArrayList<>(groundingsFixedSoFar);
		VariableParam paramToBeGround = null;

		/* create an array with the parameters of the literal defined by the basic grounding, and determine the one to be oracled */
		ConstantParam[] params = new ConstantParam[l.getParameters().size()];
		int indexOfParam = -1;
		Map<VariableParam, Integer> positionsOfVariableParams = new HashMap<>();
		for (int i = 0; i < params.length; i++) {
			LiteralParam param = l.getParameters().get(i);
			boolean parameterIsConstant = param instanceof ConstantParam;
			boolean parameterIsGround = basicConstantGrounding.containsKey(param);
			boolean parameterHasBeenDecidedByOracle = !(parameterIsConstant || parameterIsGround) && paramsGroundSoFar.contains(param);
			if (parameterIsConstant) {
				params[i] = (ConstantParam)param;
			}
			else if (parameterIsGround) {
				params[i] = basicConstantGrounding.get(param);
			} else {
				positionsOfVariableParams.put((VariableParam) param, i);
				if (!parameterHasBeenDecidedByOracle) {
					indexOfParam = i;
					paramToBeGround = (VariableParam) l.getParameters().get(i);
				}
			}
		}
		
		/* update list of solutions */
		groundingsFixedSoFar.clear();
		for (Map<VariableParam, ConstantParam> previouslyOracledGrounding : localCopyOfCurrentGrounding) {

			logger.info("Considering combination of previously fixed oracle decisions: {}", previouslyOracledGrounding);

			/* completing the param array */
			for (VariableParam oracledParam : paramsGroundSoFar) {
				if (!positionsOfVariableParams.containsKey(oracledParam)) {
					logger.debug("Ignoring ground value {} of param {}, because this param does not occur in the literal", previouslyOracledGrounding.get(oracledParam), oracledParam);
					continue;
				}
				logger.debug("Inserting {} at position {} in the param array.", previouslyOracledGrounding.get(oracledParam), positionsOfVariableParams.get(oracledParam));
				params[positionsOfVariableParams.get(oracledParam)] = previouslyOracledGrounding.get(oracledParam);
			}
			logger.info("Params for literal are {}", Arrays.toString(params));

			/* recover the parameter to ground */
			final int finalizedIndexOfParam = indexOfParam;
			if ((finalizedIndexOfParam >= 0) != (paramToBeGround != null))
				throw new IllegalStateException("Param to be ground is " + paramToBeGround + ", but the index in the literal is " + finalizedIndexOfParam);

			/* determine currently valid candidates */
			EvaluablePredicate predicate = evaluablePlanningPredicates.get(l.getPropertyName());

			/* if this param is checked for the first time, aquire an oracle */
			if (paramToBeGround != null) {
				logger.info("No valid grounding for param {} are known, so apply oracle.", paramToBeGround);
				Collection<List<ConstantParam>> possibleGroundingsOfThisPredicate = l.isPositive() ? predicate.getParamsForPositiveEvaluation(state, params)
						: predicate.getParamsForNegativeEvaluation(state, params);
				if (possibleGroundingsOfThisPredicate == null) {
					logger.warn("Predicate {} returned NULL for params {} in state {}. Canceling grounding process.", l.getPropertyName(), params, state);
					return;
				}
				Collection<ConstantParam> possibleValuesForNewParamInThisGrounding = possibleGroundingsOfThisPredicate.stream().map(s -> s.get(finalizedIndexOfParam))
						.collect(Collectors.toSet());
				for (ConstantParam oracledParamOfThisLiteral : possibleValuesForNewParamInThisGrounding) {
					Map<VariableParam, ConstantParam> extendedOracleGrounding = new HashMap<>(previouslyOracledGrounding);
					extendedOracleGrounding.put(paramToBeGround, oracledParamOfThisLiteral);
					groundingsFixedSoFar.add(extendedOracleGrounding);
				}
				logger.info("Candidates for grounding is now {}", groundingsFixedSoFar);
				paramsGroundSoFar.add(paramToBeGround);
			}

			/* otherwise just test the predicate against the choices already made */
			else {
				logger.info("No new parameters to ground. Only testing {} (evaluated by {}) against params {} given groundings {}.", l, predicate.getClass().getName(), Arrays.toString(params), groundingsFixedSoFar);
				localCopyOfCurrentGrounding.stream().filter(grounding -> predicate.test(state, params) == l.isPositive()).forEach(g -> {groundingsFixedSoFar.add(g);});
			}
		}
		logger.info("Proceeding with extended oracle grounding: {}", groundingsFixedSoFar);
		getOracleGroundings(ungroundParamsInEvaluablePrecondition, literalsOrderedByOracability, state, paramsGroundSoFar, groundingsFixedSoFar, basicConstantGrounding);
	}

	public Collection<Action> getActionsForPrimitiveTaskThatAreApplicableInState(CNFFormula knowledge, Operation op, Literal task, Monom state) {
		Collection<Action> applicableDerivedActions = new ArrayList<>();
		List<VariableParam> allParams = new ArrayList<>();
		allParams.addAll(op.getParams());
		String taskNameOfOperation = op.getName() + "(";
		for (int i = 0; i < allParams.size(); i++) {
			if (i > 0)
				taskNameOfOperation += ", ";
			taskNameOfOperation += allParams.get(i).getName();
		}
		taskNameOfOperation += ")";
		Literal taskOfOperation = new Literal(taskNameOfOperation);
		for (Map<VariableParam, LiteralParam> grounding : getMappingsThatMatchTasksAndMakesItApplicable(knowledge, taskOfOperation, task, op.getPrecondition(), state)) {
			Map<VariableParam, ConstantParam> constantGrounding = new HashMap<>();
			for (VariableParam key : grounding.keySet()) {
				constantGrounding.put(key, (ConstantParam) grounding.get(key));
			}
			if (op instanceof CEOCOperation)
				applicableDerivedActions.add(new CEOCAction((CEOCOperation)op, constantGrounding));
			else
				applicableDerivedActions.add(new Action(op, constantGrounding));
		}
		return applicableDerivedActions;
	}

	private Collection<Map<VariableParam, LiteralParam>> getMappingsThatMatchTasksAndMakesItApplicable(CNFFormula knowledge, Literal methodOrPrimitiveTask, Literal target,
			Monom preconditionOfMethodOrPrimitive, Monom state) {
		
		logger.info("Now computing the possible applications of method {} for task {}", methodOrPrimitiveTask, target);

		/* consistency check */
		if (!methodOrPrimitiveTask.getPropertyName().equals(target.getPropertyName()))
			throw new IllegalArgumentException("The method used to refine task \"" + target
					+ "\" must be compatible with it, i.e. designed for that task, but it is designed for \"" + methodOrPrimitiveTask.getPropertyName() + "\"");

		/*
		 * compute map between argument names of the method or primitive task literal and the target literal. Primitive tasks are completely bound here, but methods may have other parameters that do
		 * not occur in their task.
		 */
		final List<LiteralParam> taskParams = target.getParameters();
		final List<VariableParam> methodTaskParams = methodOrPrimitiveTask.getVariableParams(); // there should be no constants actually
		if (taskParams.size() != methodTaskParams.size() || methodTaskParams.size() != methodOrPrimitiveTask.getParameters().size())
			throw new IllegalArgumentException(
					"A method or operation associated with task \"" + methodOrPrimitiveTask + "\" is used to refine task \"" + target + "\". There is a parameter count clash!");
		final Map<VariableParam, LiteralParam> taskParameterMapping = new HashMap<>();
		for (int i = 0; i < taskParams.size(); i++) {
			taskParameterMapping.put(methodTaskParams.get(i), taskParams.get(i));
		}
		final List<Map<VariableParam, LiteralParam>> groundings = new ArrayList<>();

		/* create knowledge for the check */
		assert knowledge == null || !knowledge.hasDisjunctions() : "Currently no support for non-factbase knowledge!";
		Monom unitedKnowledge = new Monom(state);
		if (knowledge != null)
			unitedKnowledge.addAll(knowledge.extractMonom());
		try {

			/* determine potential output parameters of the task */
			final Collection<VariableParam> outputs = SetUtil.difference(target.getVariableParams(), preconditionOfMethodOrPrimitive.getVariableParams());
			final Collection<VariableParam> parametersThatNeedGrounding = SetUtil.difference(target.getVariableParams(), outputs);

			/* first compute the possible groundings of the TASK to the state objects; if the task is already ground, add the empty completion */
			final Collection<Map<VariableParam, ConstantParam>> groundingsOfTargetTask = SetUtil.allTotalMappings(parametersThatNeedGrounding, unitedKnowledge.getConstantParams());
			if (groundingsOfTargetTask.isEmpty()) {
				groundingsOfTargetTask.add(new HashMap<>());
			}

			/* now check the instances of the method for each grounding completion */
			for (Map<VariableParam, ConstantParam> targetTaskGrounding : groundingsOfTargetTask) {

				/*
				 * transfer the grounding of the target predicate to the parameters occurring in the method task/primitive task respectively. NOTE: There may be parameters of the target task (e.g.
				 * outputs) that will not be ground here. So also the instance will be ground only partially.
				 */
				final Map<VariableParam, ConstantParam> groundingForMethodOrPrimitiveTask = new HashMap<>();
				for (VariableParam var : methodTaskParams) {
					LiteralParam correspondingVarInTaskLiteral = taskParameterMapping.get(var);
					if (correspondingVarInTaskLiteral instanceof ConstantParam)
						groundingForMethodOrPrimitiveTask.put(var, (ConstantParam) correspondingVarInTaskLiteral);
					else if (targetTaskGrounding.containsKey(correspondingVarInTaskLiteral))
						groundingForMethodOrPrimitiveTask.put(var, targetTaskGrounding.get(correspondingVarInTaskLiteral));
				}

				/* now create the part of the grounding of the METHOD related to params NOT occurring in the task. if no such exists, consider just one empty completion */
				Monom positiveRequirements = new Monom(preconditionOfMethodOrPrimitive.stream().filter(l -> l.isPositive()).collect(Collectors.toList()),
						groundingForMethodOrPrimitiveTask);

				final Collection<Map<VariableParam, LiteralParam>> restMaps = LogicUtil.getSubstitutionsThatEnableForwardChaining(unitedKnowledge, positiveRequirements);
				if (restMaps.isEmpty())
					restMaps.add(new HashMap<>());

				/* now compute the resulting complete groundings */
				for (Map<VariableParam, LiteralParam> restMap : restMaps) {
					Map<VariableParam, LiteralParam> completeGroundingMethod = new HashMap<>();
					completeGroundingMethod.putAll(groundingForMethodOrPrimitiveTask);
					completeGroundingMethod.putAll(restMap);

					/* now check applicability of the GROUND method */
					logger.debug("Now considering grounding {}", completeGroundingMethod);
					Monom precondition = new Monom(preconditionOfMethodOrPrimitive, completeGroundingMethod);
					if (precondition.isContradictory()) {
						logger.debug("Ignoring this grounding because it makes the precondition contradictory.");
						continue;
					}
					List<Literal> positiveLiterals = precondition.stream().filter(l -> l.isPositive()).collect(Collectors.toList());
					List<Literal> negativeLiterals = precondition.stream().filter(l -> l.isNegated()).map(l -> l.clone().toggleNegation()).collect(Collectors.toList());
					if (unitedKnowledge.containsAll(positiveLiterals) && SetUtil.intersection(unitedKnowledge, negativeLiterals).isEmpty()) {
						logger.debug("Adding the grounding.");
						groundings.add(completeGroundingMethod);
					}
					else if (logger.isDebugEnabled()) {
						for (Literal l : positiveLiterals) {
							if (!unitedKnowledge.contains(l)) {
								logger.debug("Ignoring this grounding because the united knowledge {} does not contain the positive literal {}", unitedKnowledge, l);
								if (logger.isTraceEnabled()) {
									for (Literal l2 : unitedKnowledge) {
										logger.trace("Comparing {} of signature {}{} with {} of signature{}{}: {}/{}", l, l.getClass().getName(), l.getParameters().stream().map(p -> p.getName() + ":" + p.getType()).collect(Collectors.toList()), l2, l2.getClass().getName(), l2.getParameters().stream().map(p -> p.getName() + ":" + p.getType()).collect(Collectors.toList()), l.equals(l2), l2.equals(l));
									}
								}
								break;
							}
						}
						if (!SetUtil.intersection(unitedKnowledge, negativeLiterals).isEmpty())
							logger.debug("Ignoring this grounding because of an non-empty intersection of the united knowledge {} and the negative literals {}", unitedKnowledge, negativeLiterals);
					}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		logger.info("Admissible groundings for {} with precondition {} on {} in state {} are: {}", methodOrPrimitiveTask, preconditionOfMethodOrPrimitive, target, state,
				groundings);
		return groundings;
	}

	public List<Literal> getTaskChainOfTotallyOrderedNetwork(TaskNetwork network) {
		List<Literal> taskSequence = new ArrayList<>();
		if (network.getSources().isEmpty())
			return taskSequence;
		Literal current = network.getSources().iterator().next();
		while (current != null) {
			taskSequence.add(current);
			Collection<Literal> successors = network.getSuccessors(current);
			current = successors.isEmpty() ? null : successors.iterator().next();
		}
		return taskSequence;
	}

	public Map<String, EvaluablePredicate> getEvaluablePlanningPredicates() {
		return evaluablePlanningPredicates;
	}

	public void setEvaluablePlanningPredicates(Map<String, EvaluablePredicate> evaluablePlanningPredicates) {
		this.evaluablePlanningPredicates = evaluablePlanningPredicates;
	}

	public <O extends Operation> Optional<O> getOperationWithName(STNPlanningDomain<O,?> domain, String nameOfOperation) {
		Objects.requireNonNull(domain);
		Objects.requireNonNull(nameOfOperation);
		return domain.getOperations().stream().filter(o -> o.getName().equals(nameOfOperation)).findAny();
	}
	
	public List<CEOCAction> recoverPlanFromActionEncoding(STNPlanningDomain domain, List<String> actionEncodings) {
		List<CEOCAction> plan = new ArrayList<>();
		Pattern p = Pattern.compile("([^(]+)\\(([^,]*|([^,]*(?:,[^,]*)+))\\)");
		for (String actionEncoding : actionEncodings) {
			Matcher m = p.matcher(actionEncoding);
			if (!m.find())
				throw new IllegalArgumentException("Cannot match the action encoding " + actionEncoding);
			
			/* compute operation */
			Optional<? extends Operation> op = getOperationWithName(domain, m.group(1));
			if (!op.isPresent())
				throw new IllegalArgumentException("Invalid action " + actionEncoding + ", because no operation with name \"" + m.group(1) + "\" is known in the given domain.");
			
			/* compute grounding */
			List<ConstantParam> args = Arrays.asList(m.group(2).split(",")).stream().map(param -> new ConstantParam(param.trim())).collect(Collectors.toList());
			Map<VariableParam,ConstantParam> grounding = new HashMap<>();
			List<VariableParam> params = op.get().getParams();
			for (int i = 0; i < params.size(); i++) {
				grounding.put(params.get(i), args.get(i));
			}
			plan.add(new CEOCAction((CEOCOperation)op.get(), grounding));
		}
		return plan;
	}
}
