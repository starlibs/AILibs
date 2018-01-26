package jaicore.planning.graphgenerators.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.SetUtil;
import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.LiteralParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;
import jaicore.logic.fol.util.LogicUtil;
import jaicore.planning.graphgenerators.task.ceociptfd.EvaluablePredicate;
import jaicore.planning.model.core.Action;
import jaicore.planning.model.core.Operation;
import jaicore.planning.model.task.ceocipstn.OCIPMethod;
import jaicore.planning.model.task.ceocstn.OCMethod;
import jaicore.planning.model.task.stn.Method;
import jaicore.planning.model.task.stn.MethodInstance;
import jaicore.planning.model.task.stn.TaskNetwork;

public class TaskPlannerUtil {

	private static final Logger logger = LoggerFactory.getLogger(TaskPlannerUtil.class);
	
	private Map<String, EvaluablePredicate> evaluablePlanningPredicates;
	
	public TaskPlannerUtil(Map<String, EvaluablePredicate> evaluablePlanningPredicates) {
		super();
		this.evaluablePlanningPredicates = evaluablePlanningPredicates;
	}

	public Collection<MethodInstance> getMethodInstancesForTaskThatAreApplicableInState(CNFFormula knowledge, Collection<? extends Method> methods, Literal task, Monom state, List<Literal> remainingProblems) {
		Collection<MethodInstance> applicableDerivedMethods = new ArrayList<>();
		for (Method m : methods) {
			if (m.getTask().getPropertyName().equals(task.getPropertyName())) {
				applicableDerivedMethods.addAll(getMethodInstancesForTaskThatAreApplicableInState(knowledge, m, task, state, remainingProblems));
			}
		}
		return applicableDerivedMethods;
	}

	public Collection<MethodInstance> getMethodInstancesForTaskThatAreApplicableInState(CNFFormula knowledge, Method method, Literal task, Monom state, List<Literal> remainingProblems) {
		Collection<MethodInstance> applicableDerivedMethodInstances = new ArrayList<>();
		Collection<Map<VariableParam, LiteralParam>> maps = getMappingsThatMatchTasksAndMakesItApplicable(knowledge, method.getTask(), task, method.getPrecondition(), state);
		for (Map<VariableParam, LiteralParam> grounding : maps) {
			Map<VariableParam, ConstantParam> basicConstantGrounding = new HashMap<>();
			for (VariableParam key : grounding.keySet()) {
				basicConstantGrounding.put(key, (ConstantParam) grounding.get(key));
			}
			
			/* up to where, we only have considered the "normal" parameters. Now check whether additional inputs bindings are indicated by interpreted predicates */
			Collection<Map<VariableParam,ConstantParam>> extendedGroundings = new ArrayList<>();
			
			/* this block is to cater for methods that have interpreted predicates and need to be oracled for valid groundings */
			if (method instanceof OCIPMethod) {
				OCIPMethod castedMethod = (OCIPMethod)method;
				Collection<VariableParam> ungroundParamsInEvaluablePrecondition = SetUtil.difference(castedMethod.getEvaluablePrecondition().getVariableParams(), basicConstantGrounding.keySet());
				
				Map<Literal,EvaluablePredicate> evaluablePredicatesForLiterals = new HashMap<>();
				for (Literal l : castedMethod.getEvaluablePrecondition()) {
					if (!evaluablePlanningPredicates.containsKey(l.getPropertyName()))
						throw new IllegalArgumentException("The literal " + l + " is used in an evaluated precondition, but not evaluator was specified.");
					evaluablePredicatesForLiterals.put(l, evaluablePlanningPredicates.get(l.getPropertyName()));
				}
				List<Literal> literalsOrderedByOracability = castedMethod.getEvaluablePrecondition().stream().sorted((l1,l2) -> (evaluablePredicatesForLiterals.get(l2).isOracable() ? 1 : 0) - (evaluablePredicatesForLiterals.get(l1).isOracable() ? 1 : 0)).collect(Collectors.toList());
				
				if (!ungroundParamsInEvaluablePrecondition.isEmpty()) {
					if (ungroundParamsInEvaluablePrecondition.size() > 1)
						throw new UnsupportedOperationException("Currently only support for at most one unground variable!");
					if (!evaluablePredicatesForLiterals.get(literalsOrderedByOracability.get(0)).isOracable())
						throw new IllegalArgumentException("None of the literals " + literalsOrderedByOracability + " is oracable");
					
					VariableParam paramToBeGround = ungroundParamsInEvaluablePrecondition.iterator().next();
					Collection<ConstantParam> candidatesForGrounding = null;
					boolean oracleAcquired = false;
					for (Literal l : literalsOrderedByOracability) {
						if (l == null)
							throw new IllegalArgumentException("Evaluable precondition " + castedMethod.getEvaluablePrecondition() + " contains NULL predicate!");
						int indexOfParam = l.getParameters().indexOf(paramToBeGround);
						ConstantParam[] params = new ConstantParam[l.getParameters().size()];
						for (int i = 0; i < params.length; i++) {
							params[i] = (i != indexOfParam) ? basicConstantGrounding.get(l.getParameters().get(i)) : null;
						}
						
						if (!evaluablePlanningPredicates.containsKey(l.getPropertyName()))
							throw new IllegalArgumentException("No theory for predicate " + l.getPropertyName() + " defined!");
						EvaluablePredicate predicate = evaluablePlanningPredicates.get(l.getPropertyName());
						Collection<ConstantParam> candidatesForThisLiteral;
						
						/* if this is the first time an oracle is acquired, get the possible groundings */
						if (!oracleAcquired) {
							Collection<List<ConstantParam>> possibleGroundingsOfThisPredicate = l.isPositive() ? predicate.getParamsForPositiveEvaluation(state, params) : predicate.getParamsForNegativeEvaluation(state, params);
							candidatesForThisLiteral = possibleGroundingsOfThisPredicate.stream().map(s -> s.get(indexOfParam)).collect(Collectors.toSet());
							candidatesForGrounding = candidatesForThisLiteral;
							oracleAcquired = true;
						}
						
						/* otherwise just use the remaining candidates as possible inputs */
						else {
							candidatesForThisLiteral = new HashSet<>();
							for (ConstantParam param : candidatesForGrounding) {
								params[indexOfParam] = param;
								boolean test = predicate.test(state, params);
								if (test == l.isPositive()) {
									candidatesForThisLiteral.add(param);
								}
							}
						}
						candidatesForGrounding = SetUtil.intersection(candidatesForGrounding, candidatesForThisLiteral);
					}
					for (ConstantParam param : candidatesForGrounding) {
						Map<VariableParam, ConstantParam> extendedGrounding = new HashMap<>(basicConstantGrounding);
						extendedGrounding.put(paramToBeGround, param);
						extendedGroundings.add(extendedGrounding);
					}
				}
				else {
					boolean allSatisfied = true;
					for (Literal l : castedMethod.getEvaluablePrecondition()) {
						ConstantParam[] params = new ConstantParam[l.getParameters().size()];
						for (int i = 0; i < params.length; i++)
							params[i] = basicConstantGrounding.get(l.getParameters().get(i));
						if (!evaluablePredicatesForLiterals.get(l).test(state, params)) {
							allSatisfied = false;
							break;
						}
					}
					if (allSatisfied)
						extendedGroundings.add(basicConstantGrounding);
				}
			}
			else
				extendedGroundings.add(basicConstantGrounding);
			
			/* now add a method application for each of the extended groundings */
			for (Map<VariableParam,ConstantParam> extendedGrounding : extendedGroundings) {

				/* create new objects for unassigned open output variables */
				Set<ConstantParam> knownConstants = new HashSet<>(state.getConstantParams());
				for (Literal l : remainingProblems) {
					knownConstants.addAll(l.getConstantParams());
				}
				Collection<VariableParam> unboundParams = SetUtil.difference(method.getParameters(), extendedGrounding.keySet());
				if (method instanceof OCMethod) {
					Collection<VariableParam> unboundOutputParams = SetUtil.intersection(unboundParams, ((OCMethod) method).getOutputs());
					int indexForNewVariable = 1;
					for (VariableParam v : unboundOutputParams) {
						ConstantParam p;
						do {
							p = new ConstantParam("newVar" + (indexForNewVariable++));
						}
						while (knownConstants.contains(p));
						extendedGrounding.put(v, p);
						unboundParams.remove(v);
					}
				}
				
				assert unboundParams.isEmpty() : "Method " + method.getName() + " must be ground completely before processing. Here, " + unboundParams + " are unground.";
				applicableDerivedMethodInstances.add(new MethodInstance(method, extendedGrounding));
			}
		}
		return applicableDerivedMethodInstances;
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
			applicableDerivedActions.add(new Action(op, constantGrounding));
		}
		return applicableDerivedActions;
	}

	private Collection<Map<VariableParam, LiteralParam>> getMappingsThatMatchTasksAndMakesItApplicable(CNFFormula knowledge, Literal methodOrPrimitiveTask, Literal target,
			Monom preconditionOfMethodOrPrimitive, Monom state) {
		
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
					"A method associated with task \"" + methodOrPrimitiveTask + "\" is used to refine task \"" + target + "\". There is a parameter count clash!");
		final Map<VariableParam, LiteralParam> taskParameterMapping = new HashMap<>();
		for (int i = 0; i < taskParams.size(); i++) {
			taskParameterMapping.put(methodTaskParams.get(i), taskParams.get(i));
		}
		final List<Map<VariableParam, LiteralParam>> groundings = new ArrayList<>();
		
		/* create knowledge for the check */
		assert !knowledge.hasDisjunctions() : "Currently no support for non-factbase knowledge!";
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
				Monom positiveRequirements = new Monom(preconditionOfMethodOrPrimitive.stream().filter(l -> l.isPositive()).collect(Collectors.toList()), groundingForMethodOrPrimitiveTask);
				
				final Collection<Map<VariableParam, LiteralParam>> restMaps = LogicUtil.getSubstitutionsThatEnableForwardChaining(unitedKnowledge, positiveRequirements);
				if (restMaps.isEmpty())
					restMaps.add(new HashMap<>());
				
				/* now compute the resulting complete groundings */
				for (Map<VariableParam, LiteralParam> restMap : restMaps) {
					Map<VariableParam, LiteralParam> completeGroundingMethod = new HashMap<>();
					completeGroundingMethod.putAll(groundingForMethodOrPrimitiveTask);
					completeGroundingMethod.putAll(restMap);
					
					/* now check applicability of the GROUND method */					
					Monom precondition = new Monom(preconditionOfMethodOrPrimitive, completeGroundingMethod);
					if (precondition.isContradictory())
						continue;
					List<Literal> positiveLiterals = precondition.stream().filter(l -> l.isPositive()).collect(Collectors.toList());
					List<Literal> negativeLiterals = precondition.stream().filter(l -> l.isNegated()).map(l -> l.clone().toggleNegation()).collect(Collectors.toList());
					if (unitedKnowledge.containsAll(positiveLiterals) && SetUtil.intersection(unitedKnowledge, negativeLiterals).isEmpty()) {
						groundings.add(completeGroundingMethod);
					}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		logger.info("Admissible groundings for {} with precondition {} on {} in state {} are: {}", methodOrPrimitiveTask, preconditionOfMethodOrPrimitive, target, state, groundings);
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
}
