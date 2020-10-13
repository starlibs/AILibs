package ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.logic.fol.structure.CNFFormula;
import ai.libs.jaicore.logic.fol.structure.ConstantParam;
import ai.libs.jaicore.logic.fol.structure.Literal;
import ai.libs.jaicore.logic.fol.structure.LiteralParam;
import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.logic.fol.structure.VariableParam;
import ai.libs.jaicore.logic.fol.theories.EvaluablePredicate;
import ai.libs.jaicore.logic.fol.util.ForwardChainer;
import ai.libs.jaicore.logic.fol.util.ForwardChainingProblem;
import ai.libs.jaicore.planning.classical.problems.ceoc.CEOCAction;
import ai.libs.jaicore.planning.classical.problems.ceoc.CEOCOperation;
import ai.libs.jaicore.planning.classical.problems.strips.Operation;
import ai.libs.jaicore.planning.core.Action;
import ai.libs.jaicore.planning.hierarchical.problems.ceocipstn.OCIPMethod;
import ai.libs.jaicore.planning.hierarchical.problems.ceocstn.OCMethod;
import ai.libs.jaicore.planning.hierarchical.problems.stn.Method;
import ai.libs.jaicore.planning.hierarchical.problems.stn.MethodInstance;
import ai.libs.jaicore.planning.hierarchical.problems.stn.STNPlanningDomain;
import ai.libs.jaicore.planning.hierarchical.problems.stn.TaskNetwork;

public class TaskPlannerUtil implements ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(TaskPlannerUtil.class);

	private Map<String, EvaluablePredicate> evaluablePlanningPredicates;

	public TaskPlannerUtil(final Map<String, EvaluablePredicate> evaluablePlanningPredicates) {
		super();
		this.evaluablePlanningPredicates = evaluablePlanningPredicates;
	}

	public Collection<MethodInstance> getMethodInstancesForTaskThatAreApplicableInState(final CNFFormula knowledge, final Collection<? extends Method> methods, final Literal task, final Monom state, final List<Literal> remainingProblems)
			throws InterruptedException {
		Collection<MethodInstance> applicableDerivedMethods = new ArrayList<>();
		List<Method> potentiallySuitableMethod = methods.stream().filter(m -> m.getTask().getPropertyName().equals(task.getPropertyName())).collect(Collectors.toList());
		if (potentiallySuitableMethod.isEmpty()) {
			this.logger.warn("There are NO methods that can resolve the task {}. This points to an ill-defined planning problem!", task);
			return applicableDerivedMethods;
		}
		this.logger.debug("Identified {} methods that are suitable based on their name.", potentiallySuitableMethod.size());
		for (Method m : potentiallySuitableMethod) {
			this.logger.debug("Method {} is potentially suited to solve this task. Checking its applicability.", m.getName());
			Collection<MethodInstance> additionalInstances = this.getMethodInstancesForTaskThatAreApplicableInState(knowledge, m, task, state, remainingProblems);
			assert !m.isLonely() || additionalInstances.size() <= 1 : "Computed more than one instantiations for lonely method: \n\t" + additionalInstances.stream().map(MethodInstance::toString).collect(Collectors.joining("\n\t"));
			applicableDerivedMethods.addAll(additionalInstances);
		}
		return applicableDerivedMethods;
	}

	public Collection<MethodInstance> getMethodInstancesForTaskThatAreApplicableInState(final CNFFormula knowledge, final Method method, final Literal task, final Monom state, final List<Literal> remainingProblems)
			throws InterruptedException {

		this.logger.info("Determine instances of method {} that are applicable in current state for task {}. Complete agenda: {}. Enable TRACE to see current state.", method.getName(), task, remainingProblems);
		this.logger.trace("State is {}", state);
		Collection<MethodInstance> applicableDerivedMethodInstances = new ArrayList<>();
		Collection<Map<VariableParam, LiteralParam>> maps = this.getMappingsThatMatchTasksAndMakesItApplicable(knowledge, method.getTask(), task, method.getPrecondition(), state);
		for (Map<VariableParam, LiteralParam> grounding : maps) {
			this.logger.debug("Now considering partial grounding {}", grounding);

			/* create a copy of the grounding */
			Map<VariableParam, ConstantParam> basicConstantGrounding = new HashMap<>();
			for (Entry<VariableParam,LiteralParam> groundingEntry : grounding.entrySet()) {
				basicConstantGrounding.put(groundingEntry.getKey(), (ConstantParam) groundingEntry.getValue());
			}

			/* up to where, we only have considered the "normal" parameters. Now check whether additional inputs bindings are indicated by interpreted predicates */
			Collection<Map<VariableParam, ConstantParam>> extendedGroundings = new ArrayList<>();

			/* this block is to cater for methods that have interpreted predicates and need to be oracled for valid groundings */
			if (method instanceof OCIPMethod) {
				OCIPMethod castedMethod = (OCIPMethod) method;
				Collection<VariableParam> ungroundParamsInEvaluablePrecondition = SetUtil.difference(castedMethod.getEvaluablePrecondition().getVariableParams(), basicConstantGrounding.keySet());

				Map<Literal, EvaluablePredicate> evaluablePredicatesForLiterals = new HashMap<>();
				for (Literal l : castedMethod.getEvaluablePrecondition()) {
					if (this.evaluablePlanningPredicates == null || !this.evaluablePlanningPredicates.containsKey(l.getPropertyName())) {
						throw new IllegalArgumentException("The literal " + l + " is used in an evaluated precondition, but no evaluator has been specified for it.");
					}
					evaluablePredicatesForLiterals.put(l, this.evaluablePlanningPredicates.get(l.getPropertyName()));
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
						if (!el1.isOracable()) {
							return 0;
						}
						int ungroundParamsInL1 = SetUtil.intersection(ungroundParamsInEvaluablePrecondition, l1.getParameters()).size();
						int ungroundParamsInL2 = SetUtil.intersection(ungroundParamsInEvaluablePrecondition, l2.getParameters()).size();
						return ungroundParamsInL1 - ungroundParamsInL2;
					}).collect(Collectors.toList()));

					/* just a brief check whether there are oracable literals */
					if (!evaluablePredicatesForLiterals.get(literalsOrderedByOracability.peek()).isOracable()) {
						throw new IllegalArgumentException("None of the literals " + literalsOrderedByOracability + " is oracable");
					}

					List<Map<VariableParam, ConstantParam>> oracleGroundings = new ArrayList<>();
					oracleGroundings.add(basicConstantGrounding);
					this.getOracleGroundings(ungroundParamsInEvaluablePrecondition, literalsOrderedByOracability, state, new HashSet<>(), oracleGroundings, basicConstantGrounding);
					extendedGroundings.addAll(oracleGroundings);
				}

				/* all parameters are ground, we just need to test the predicate */
				else {
					boolean allSatisfied = true;
					for (Literal l : castedMethod.getEvaluablePrecondition()) {
						ConstantParam[] params = new ConstantParam[l.getParameters().size()];
						for (int i = 0; i < params.length; i++) {
							LiteralParam param = l.getParameters().get(i);
							params[i] = (param instanceof ConstantParam) ? (ConstantParam) param : basicConstantGrounding.get(param);
						}
						if (evaluablePredicatesForLiterals.get(l).test(state, params) != l.isPositive()) {
							allSatisfied = false;
							break;
						}
					}
					if (allSatisfied) {
						extendedGroundings.add(basicConstantGrounding);
					}
				}
			} else {
				extendedGroundings.add(basicConstantGrounding);
			}

			/* now add a method application for each of the extended groundings */
			for (Map<VariableParam, ConstantParam> extendedGrounding : extendedGroundings) {

				/* create new objects for unassigned open output variables */
				Set<ConstantParam> knownConstants = new HashSet<>(state.getConstantParams());
				knownConstants.addAll(extendedGrounding.values());
				for (Literal l : remainingProblems) {
					knownConstants.addAll(l.getConstantParams());
				}
				Collection<VariableParam> unboundParams = SetUtil.difference(method.getParameters(), extendedGrounding.keySet());
				this.logger.debug("Unbound parameters at this point: {}. Known constants: {}", unboundParams, knownConstants);

				if (method instanceof OCMethod) {
					Collection<VariableParam> unboundOutputParams = SetUtil.intersection(unboundParams, ((OCMethod) method).getOutputs());
					if (!unboundOutputParams.equals(unboundParams)) {
						throw new IllegalStateException("Some of the inputs of method " + method.getName() + " have not been ground. Unground inputs: " + SetUtil.difference(unboundParams, unboundOutputParams));
					}
					int indexForNewVariable = 1;
					for (VariableParam v : unboundOutputParams) {
						ConstantParam p;
						do {
							p = new ConstantParam("newVar" + (indexForNewVariable++));
						} while (knownConstants.contains(p));
						extendedGrounding.put(v, p);
						unboundParams.remove(v);
					}
					if (!unboundParams.isEmpty()) {
						throw new IllegalStateException("Method " + method.getName() + " must be ground completely before processing. Here, " + unboundParams + " are unground.");
					}
				} else if (!unboundParams.isEmpty()) {
					throw new IllegalStateException("Could not compute a complete grounding for method " + method.getName() + ". The following parameters were not ground until the end: " + unboundParams);
				}

				applicableDerivedMethodInstances.add(new MethodInstance(method, extendedGrounding));
				if (method.isLonely()) {
					this.logger.info("Determined {} applicable method instances of method {} for task {}", applicableDerivedMethodInstances.size(), method.getName(), task);
					return applicableDerivedMethodInstances;
				}
			}
		}
		this.logger.info("Determined {} applicable method instances of method {} for task {}", applicableDerivedMethodInstances.size(), method.getName(), task);
		return applicableDerivedMethodInstances;
	}

	private void getOracleGroundings(final Collection<VariableParam> ungroundParamsInEvaluablePrecondition, final Queue<Literal> literalsOrderedByOracability, final Monom state, final Set<VariableParam> paramsGroundSoFar,
			final Collection<Map<VariableParam, ConstantParam>> groundingsFixedSoFar, final Map<VariableParam, ConstantParam> basicConstantGrounding) {
		if (literalsOrderedByOracability.isEmpty()) {
			return;
		}

		Literal l = literalsOrderedByOracability.poll();

		/* check whether the literal only needs to be queried for one param */
		Collection<LiteralParam> paramsThatNeedGrounding = SetUtil.intersection(SetUtil.difference(ungroundParamsInEvaluablePrecondition, paramsGroundSoFar), l.getParameters());
		this.logger.info("Now checking validity of {}. Set of params that still need grounding: {}", l, paramsThatNeedGrounding);
		if (paramsThatNeedGrounding.size() > 1) {
			throw new UnsupportedOperationException("Currently only support for at most one unground variable! Here, the following variables of \"" + l + "\"need grounding: " + paramsThatNeedGrounding);
		}

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
				params[i] = (ConstantParam) param;
			} else if (parameterIsGround) {
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

			this.logger.info("Considering combination of previously fixed oracle decisions: {}", previouslyOracledGrounding);

			/* completing the param array */
			for (VariableParam oracledParam : paramsGroundSoFar) {
				if (!positionsOfVariableParams.containsKey(oracledParam)) {
					this.logger.debug("Ignoring ground value {} of param {}, because this param does not occur in the literal", previouslyOracledGrounding.get(oracledParam), oracledParam);
					continue;
				}
				this.logger.debug("Inserting {} at position {} in the param array.", previouslyOracledGrounding.get(oracledParam), positionsOfVariableParams.get(oracledParam));
				params[positionsOfVariableParams.get(oracledParam)] = previouslyOracledGrounding.get(oracledParam);
			}
			if (this.logger.isInfoEnabled()) {
				this.logger.info("Params for literal are {}", Arrays.toString(params));
			}

			/* recover the parameter to ground */
			final int finalizedIndexOfParam = indexOfParam;
			if ((finalizedIndexOfParam >= 0) != (paramToBeGround != null)) {
				throw new IllegalStateException("Param to be ground is " + paramToBeGround + ", but the index in the literal is " + finalizedIndexOfParam);
			}

			/* determine currently valid candidates */
			EvaluablePredicate predicate = this.evaluablePlanningPredicates.get(l.getPropertyName());

			/* if this param is checked for the first time, aquire an oracle */
			if (paramToBeGround != null) {
				this.logger.info("No valid grounding for param {} are known, so apply oracle.", paramToBeGround);
				Collection<List<ConstantParam>> possibleGroundingsOfThisPredicate = l.isPositive() ? predicate.getParamsForPositiveEvaluation(state, params) : predicate.getParamsForNegativeEvaluation(state, params);
				if (possibleGroundingsOfThisPredicate == null) {
					this.logger.warn("Predicate {} returned NULL for params {} in state {}. Canceling grounding process.", l.getPropertyName(), params, state);
					return;
				}
				Collection<ConstantParam> possibleValuesForNewParamInThisGrounding = possibleGroundingsOfThisPredicate.stream().map(s -> s.get(finalizedIndexOfParam)).collect(Collectors.toSet());
				for (ConstantParam oracledParamOfThisLiteral : possibleValuesForNewParamInThisGrounding) {
					Map<VariableParam, ConstantParam> extendedOracleGrounding = new HashMap<>(previouslyOracledGrounding);
					extendedOracleGrounding.put(paramToBeGround, oracledParamOfThisLiteral);
					groundingsFixedSoFar.add(extendedOracleGrounding);
				}
				this.logger.info("Candidates for grounding is now {}", groundingsFixedSoFar);
				paramsGroundSoFar.add(paramToBeGround);
			}

			/* otherwise just test the predicate against the choices already made */
			else {
				if (this.logger.isInfoEnabled()) {
					this.logger.info("No new parameters to ground. Only testing {} (evaluated by {}) against params {} given groundings {}.", l, predicate.getClass().getName(), Arrays.toString(params), groundingsFixedSoFar);
				}
				localCopyOfCurrentGrounding.stream().filter(grounding -> predicate.test(state, params) == l.isPositive()).forEach(groundingsFixedSoFar::add);
			}
		}
		this.logger.info("Proceeding with extended oracle grounding: {}", groundingsFixedSoFar);
		this.getOracleGroundings(ungroundParamsInEvaluablePrecondition, literalsOrderedByOracability, state, paramsGroundSoFar, groundingsFixedSoFar, basicConstantGrounding);
	}

	public Collection<Action> getActionsForPrimitiveTaskThatAreApplicableInState(final CNFFormula knowledge, final Operation op, final Literal task, final Monom state) throws InterruptedException {
		Collection<Action> applicableDerivedActions = new ArrayList<>();
		List<VariableParam> allParams = new ArrayList<>();
		allParams.addAll(op.getParams());
		StringBuilder sbTaskNameOfOperation = new StringBuilder(op.getName());
		sbTaskNameOfOperation.append("(");
		for (int i = 0; i < allParams.size(); i++) {
			if (i > 0) {
				sbTaskNameOfOperation.append(", ");
			}
			sbTaskNameOfOperation.append(allParams.get(i).getName());
		}
		sbTaskNameOfOperation.append(")");
		Literal taskOfOperation = new Literal(sbTaskNameOfOperation.toString());
		for (Map<VariableParam, LiteralParam> grounding : this.getMappingsThatMatchTasksAndMakesItApplicable(knowledge, taskOfOperation, task, op.getPrecondition(), state)) {
			Map<VariableParam, ConstantParam> constantGrounding = new HashMap<>();
			for (Entry<VariableParam,LiteralParam> groundingEntry : grounding.entrySet()) {
				constantGrounding.put(groundingEntry.getKey(), (ConstantParam) groundingEntry.getValue());
			}
			if (op instanceof CEOCOperation) {
				applicableDerivedActions.add(new CEOCAction((CEOCOperation) op, constantGrounding));
			} else {
				applicableDerivedActions.add(new Action(op, constantGrounding));
			}
		}
		return applicableDerivedActions;
	}

	private Collection<Map<VariableParam, LiteralParam>> getMappingsThatMatchTasksAndMakesItApplicable(final CNFFormula knowledge, final Literal methodOrPrimitiveTask, final Literal target, final Monom preconditionOfMethodOrPrimitive,
			final Monom state) throws InterruptedException {
		assert preconditionOfMethodOrPrimitive != null : "precondition of methode or primitive task " + methodOrPrimitiveTask + " is null";
		this.logger.info("Now computing the possible applications of method {} for task {}", methodOrPrimitiveTask, target);

		/* if no precondition is to be matched, just match the params and return this binding */
		if (preconditionOfMethodOrPrimitive.isEmpty()) {
			int numParams = target.getParameters().size();
			if (methodOrPrimitiveTask.getParameters().size() != numParams) {
				throw new IllegalArgumentException("The target " + target + " has " + numParams + " parameters but the method or primitive task " + methodOrPrimitiveTask + " has " + methodOrPrimitiveTask.getParameters().size());
			}
			Map<VariableParam, LiteralParam> grounding = new HashMap<>();
			for (int i = 0; i < numParams; i++) {
				VariableParam paramOfPreconditionLiteral = (VariableParam) methodOrPrimitiveTask.getParameters().get(i);
				LiteralParam targetParam = target.getParameters().get(i);
				grounding.put(paramOfPreconditionLiteral, targetParam);
			}
			Collection<Map<VariableParam, LiteralParam>> groundings = new ArrayList<>();
			groundings.add(grounding);
			this.logger.debug("There are no preconditions, so the valid groundings are {}", groundings);
			return groundings;
		}

		/* consistency check */
		if (!methodOrPrimitiveTask.getPropertyName().equals(target.getPropertyName())) {
			throw new IllegalArgumentException("The method used to refine task \"" + target + "\" must be compatible with it, i.e. designed for that task, but it is designed for \"" + methodOrPrimitiveTask.getPropertyName() + "\"");
		}

		/*
		 * compute map between argument names of the method or primitive task literal and the target literal. Primitive tasks are completely bound here, but methods may have other parameters that do
		 * not occur in their task.
		 */
		final List<LiteralParam> taskParams = target.getParameters();
		final List<VariableParam> methodTaskParams = methodOrPrimitiveTask.getVariableParams(); // there should be no constants actually
		if (taskParams.size() != methodTaskParams.size() || methodTaskParams.size() != methodOrPrimitiveTask.getParameters().size()) {
			throw new IllegalArgumentException("A method or operation associated with task \"" + methodOrPrimitiveTask + "\" is used to refine task \"" + target + "\". There is a parameter count clash!");
		}
		final Map<VariableParam, LiteralParam> taskParameterMapping = new HashMap<>();
		for (int i = 0; i < taskParams.size(); i++) {
			taskParameterMapping.put(methodTaskParams.get(i), taskParams.get(i));
		}
		final List<Map<VariableParam, LiteralParam>> groundings = new ArrayList<>();

		/* create knowledge for the check */
		assert knowledge == null || !knowledge.hasDisjunctions() : "Currently no support for non-factbase knowledge!";
		Monom unitedKnowledge = new Monom(state);
		if (knowledge != null) {
			unitedKnowledge.addAll(knowledge.extractMonom());
		}

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
				if (correspondingVarInTaskLiteral instanceof ConstantParam) {
					groundingForMethodOrPrimitiveTask.put(var, (ConstantParam) correspondingVarInTaskLiteral);
				} else if (targetTaskGrounding.containsKey(correspondingVarInTaskLiteral)) {
					groundingForMethodOrPrimitiveTask.put(var, targetTaskGrounding.get(correspondingVarInTaskLiteral));
				}
			}

			/* now create the part of the grounding of the METHOD related to params NOT occurring in the task. if no such exists, consider just one empty completion */
			Monom positiveRequirements = new Monom(preconditionOfMethodOrPrimitive.stream().filter(Literal::isPositive).collect(Collectors.toList()), groundingForMethodOrPrimitiveTask);
			Collection<Map<VariableParam, LiteralParam>> restMaps;
			if (!positiveRequirements.isEmpty()) {
				ForwardChainer fc = new ForwardChainer(new ForwardChainingProblem(unitedKnowledge, positiveRequirements, true));
				try {
					restMaps = fc.call();
				} catch (AlgorithmExecutionCanceledException | TimeoutException e) {
					this.logger.warn("The forward chainer was canceled or timed out, so maybe not all bindings could be computed!");
					return groundings;
				}
			} else {
				restMaps = new ArrayList<>();
			}
			if (restMaps.isEmpty()) {
				restMaps.add(new HashMap<>());
			}

			/* now compute the resulting complete groundings */
			for (Map<VariableParam, LiteralParam> restMap : restMaps) {
				Map<VariableParam, LiteralParam> completeGroundingMethod = new HashMap<>();
				completeGroundingMethod.putAll(groundingForMethodOrPrimitiveTask);
				completeGroundingMethod.putAll(restMap);

				/* now check applicability of the GROUND method */
				this.logger.debug("Now considering grounding {}", completeGroundingMethod);
				Monom precondition = new Monom(preconditionOfMethodOrPrimitive, completeGroundingMethod);
				if (precondition.isContradictory()) {
					this.logger.debug("Ignoring this grounding because it makes the precondition contradictory.");
					continue;
				}
				List<Literal> positiveLiterals = precondition.stream().filter(Literal::isPositive).collect(Collectors.toList());
				List<Literal> negativeLiterals = precondition.stream().filter(Literal::isNegated).map(l -> l.clone().toggleNegation()).collect(Collectors.toList());
				if (unitedKnowledge.containsAll(positiveLiterals) && SetUtil.intersection(unitedKnowledge, negativeLiterals).isEmpty()) {
					this.logger.debug("Adding the grounding.");
					groundings.add(completeGroundingMethod);
				} else if (this.logger.isDebugEnabled()) {
					for (Literal l : positiveLiterals) {
						if (!unitedKnowledge.contains(l)) {
							this.logger.debug("Ignoring this grounding because the united knowledge {} does not contain the positive literal {}", unitedKnowledge, l);
							if (this.logger.isTraceEnabled()) {
								for (Literal l2 : unitedKnowledge) {
									this.logger.trace("Comparing {} of signature {}{} with {} of signature{}{}: {}/{}", l, l.getClass().getName(), l.getParameters().stream().map(p -> p.getName() + ":" + p.getType()).collect(Collectors.toList()),
											l2, l2.getClass().getName(), l2.getParameters().stream().map(p -> p.getName() + ":" + p.getType()).collect(Collectors.toList()), l.equals(l2), l2.equals(l));
								}
							}
							break;
						}
					}
					if (!SetUtil.intersection(unitedKnowledge, negativeLiterals).isEmpty()) {
						this.logger.debug("Ignoring this grounding because of an non-empty intersection of the united knowledge {} and the negative literals {}", unitedKnowledge, negativeLiterals);
					}
				}
			}
		}
		this.logger.info("Admissible groundings for {} with precondition {} on {} are: {}", methodOrPrimitiveTask, preconditionOfMethodOrPrimitive, target, groundings);
		return groundings;
	}

	public List<Literal> getTaskChainOfTotallyOrderedNetwork(final TaskNetwork network) {
		List<Literal> taskSequence = new ArrayList<>();
		if (network.getSources().isEmpty()) {
			return taskSequence;
		}
		Literal current = network.getSources().iterator().next();
		while (current != null) {
			taskSequence.add(current);
			Collection<Literal> successors = network.getSuccessors(current);
			current = successors.isEmpty() ? null : successors.iterator().next();
		}
		return taskSequence;
	}

	public Map<String, EvaluablePredicate> getEvaluablePlanningPredicates() {
		return this.evaluablePlanningPredicates;
	}

	public void setEvaluablePlanningPredicates(final Map<String, EvaluablePredicate> evaluablePlanningPredicates) {
		this.evaluablePlanningPredicates = evaluablePlanningPredicates;
	}

	public Optional<? extends Operation> getOperationWithName(final STNPlanningDomain domain, final String nameOfOperation) {
		Objects.requireNonNull(domain);
		Objects.requireNonNull(nameOfOperation);
		return domain.getOperations().stream().filter(o -> o.getName().equals(nameOfOperation)).findAny();
	}

	public List<CEOCAction> recoverPlanFromActionEncoding(final STNPlanningDomain domain, final List<String> actionEncodings) {
		List<CEOCAction> plan = new ArrayList<>();
		Pattern p = Pattern.compile("([^(]+)\\(([^,]*|([^,]*(?:,[^,]*)+))\\)");
		for (String actionEncoding : actionEncodings) {
			Matcher m = p.matcher(actionEncoding);
			if (!m.find()) {
				throw new IllegalArgumentException("Cannot match the action encoding " + actionEncoding);
			}

			/* compute operation */
			Optional<? extends Operation> op = this.getOperationWithName(domain, m.group(1));
			if (!op.isPresent()) {
				throw new IllegalArgumentException("Invalid action " + actionEncoding + ", because no operation with name \"" + m.group(1) + "\" is known in the given domain.");
			}

			/* compute grounding */
			List<ConstantParam> args = Arrays.asList(m.group(2).split(",")).stream().map(param -> new ConstantParam(param.trim())).collect(Collectors.toList());
			Map<VariableParam, ConstantParam> grounding = new HashMap<>();
			List<VariableParam> params = op.get().getParams();
			for (int i = 0; i < params.size(); i++) {
				grounding.put(params.get(i), args.get(i));
			}
			plan.add(new CEOCAction((CEOCOperation) op.get(), grounding));
		}
		return plan;
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}
}
