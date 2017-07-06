package jaicore.planning.graphgenerators.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.SetUtil;
import jaicore.logic.CNFFormula;
import jaicore.logic.ConstantParam;
import jaicore.logic.Literal;
import jaicore.logic.LiteralParam;
import jaicore.logic.LogicUtil;
import jaicore.logic.Monom;
import jaicore.logic.VariableParam;
import jaicore.planning.model.core.Action;
import jaicore.planning.model.core.Operation;
import jaicore.planning.model.task.ceocstn.OCMethod;
import jaicore.planning.model.task.stn.Method;
import jaicore.planning.model.task.stn.MethodInstance;
import jaicore.planning.model.task.stn.TaskNetwork;

public class TaskPlannerUtil {

	private static final Logger logger = LoggerFactory.getLogger(TaskPlannerUtil.class);
	private static int newVarCounter = 1;

	public static Collection<MethodInstance> getMethodInstancesForTaskThatAreApplicableInState(CNFFormula knowledge, Collection<? extends Method> methods, Literal task, Monom state) {
		Collection<MethodInstance> applicableDerivedMethods = new ArrayList<>();
		for (Method m : methods) {
			if (m.getTask().getPropertyName().equals(task.getPropertyName())) {
				applicableDerivedMethods.addAll(getMethodInstancesForTaskThatAreApplicableInState(knowledge, m, task, state));
			}
		}
		return applicableDerivedMethods;
	}

	public static Collection<MethodInstance> getMethodInstancesForTaskThatAreApplicableInState(CNFFormula knowledge, Method method, Literal task, Monom state) {
		Collection<MethodInstance> applicableDerivedMethodInstances = new ArrayList<>();
		Collection<Map<VariableParam, LiteralParam>> maps = getMappingsThatMatchTasksAndMakesItApplicable(knowledge, method.getTask(), task, method.getPrecondition(), state);
		for (Map<VariableParam, LiteralParam> grounding : maps) {
			Map<VariableParam, ConstantParam> constantGrounding = new HashMap<>();
			for (VariableParam key : grounding.keySet()) {
				constantGrounding.put(key, (ConstantParam) grounding.get(key));
			}

			/* create new objects for unassigned open output variables */
			Collection<VariableParam> unboundParams = SetUtil.difference(method.getParameters(), constantGrounding.keySet());
			if (method instanceof OCMethod) {
				Collection<VariableParam> unboundOutputParams = SetUtil.intersection(unboundParams, ((OCMethod) method).getOutputs());
				for (VariableParam v : unboundOutputParams) {
					constantGrounding.put(v, new ConstantParam("newVar" + (newVarCounter++)));
					unboundParams.remove(v);
				}
			}
			
			assert unboundParams.isEmpty() : "Method " + method.getName() + " must be ground completely before processing. Here, " + unboundParams + " are unground.";
			applicableDerivedMethodInstances.add(new MethodInstance(method, constantGrounding));
		}
		return applicableDerivedMethodInstances;
	}

	public static Collection<Action> getActionsForPrimitiveTaskThatAreApplicableInState(CNFFormula knowledge, Operation op, Literal task, Monom state) {
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

	private static Collection<Map<VariableParam, LiteralParam>> getMappingsThatMatchTasksAndMakesItApplicable(CNFFormula knowledge, Literal methodOrPrimitiveTask, Literal target,
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

	public static List<Literal> getTaskChainOfTotallyOrderedNetwork(TaskNetwork network) {
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
}
