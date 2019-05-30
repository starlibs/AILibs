package jaicore.planning.classical.algorithms.strips.forward;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.Clause;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.LiteralParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;
import jaicore.logic.fol.util.ForwardChainer;
import jaicore.logic.fol.util.ForwardChainingProblem;
import jaicore.logic.fol.util.NextBindingFoundEvent;
import jaicore.planning.classical.problems.ce.CEAction;
import jaicore.planning.classical.problems.ce.CEOperation;
import jaicore.planning.classical.problems.strips.Operation;
import jaicore.planning.classical.problems.strips.StripsAction;
import jaicore.planning.classical.problems.strips.StripsOperation;
import jaicore.planning.classical.problems.strips.StripsPlanningDomain;
import jaicore.planning.core.Action;
import jaicore.planning.core.Plan;

public class StripsUtil {

	private static final Logger logger = LoggerFactory.getLogger(StripsUtil.class);

	public static List<StripsAction> getApplicableActionsInState(Monom state, StripsPlanningDomain domain) {
		return getApplicableActionsInState(state, domain, false, -1);
	}
	
	public static List<StripsAction> getApplicableActionsInState(Monom state, StripsPlanningDomain domain, boolean randomized, final int pLimit)  {
		long start = System.currentTimeMillis();
		int limit = pLimit;
		logger.debug("Computing applicable actions for state with {} items (activate TRACE for exact state)", state.size());
		logger.trace("Exact state is {}", state);
		List<StripsAction> applicableDerivedActions = new ArrayList<>();
		Collection<Operation> operations = domain.getOperations();
		
		/* if the computation should be randomized, shuffle operations first */
		if (randomized) {
			if (!(operations instanceof List)) {
				operations = new ArrayList<>(operations);
			}
			Collections.shuffle((List<Operation>)operations);
		}
		
		/* now walk over the operations and collect actions until the limit is reached */
		long timeToOrderOps = System.currentTimeMillis() - start;
		for (Operation op : domain.getOperations()) {
			Collection<StripsAction> candidates = getPossibleOperationGroundingsForState(state, (StripsOperation) op, limit);
			applicableDerivedActions.addAll(candidates);
			if (limit >= 0)
				limit = Math.max(0, limit - candidates.size());
		}
		long duration = System.currentTimeMillis() - start;
		logger.debug("Done. Computation of {} applicable actions took {}ms of which {}ms were used to order the operations", applicableDerivedActions.size(), duration, timeToOrderOps);
		return applicableDerivedActions;
	}

	public static Collection<StripsAction> getPossibleOperationGroundingsForState(Monom state, StripsOperation operation, int limit)  {
		Collection<StripsAction> applicableDerivedActions = new ArrayList<>();

		/* decompose premise in positive and negative literals */
		logger.debug("Compute all groundings of {}-premise that can be inferred from state with {} items (activate TRACE for exact premise and state)", operation.getPrecondition().size(), state.size());
		logger.trace("Exact premise is {}", operation.getPrecondition());
		logger.trace("Exact state is {}", state);
		long start = System.currentTimeMillis();
		ForwardChainer fc = new ForwardChainer(new ForwardChainingProblem(state, operation.getPrecondition(), true));
//		Collection<Map<VariableParam, LiteralParam>> groundings = LogicUtil.getSubstitutionsThatEnableForwardChainingUnderCWA(state, operation.getPrecondition());
//		long duration = System.currentTimeMillis() - start;
//		logger.debug("Done. Computation of {} groundings took {}ms", groundings.size(), duration);
		long fcPreparationTime = System.currentTimeMillis() - start;
		NextBindingFoundEvent event;
		try {
			int i = 0;
			while ((event = fc.nextBinding()) != null && (limit < 0 || (i++ < limit))) {
				
				Map<VariableParam, LiteralParam> grounding = event.getGrounding();
				
				/* refactor grounding to constants only and add the respective action */
				Map<VariableParam, ConstantParam> rGrounding = new HashMap<>();
				for (VariableParam p : grounding.keySet()) {
					ConstantParam cp = (ConstantParam) grounding.get(p);
					rGrounding.put(p, cp);
				}
				StripsAction a = new StripsAction(operation, rGrounding);
				applicableDerivedActions.add(a);
				logger.debug("Found action {} to be applicable after {}ms.", a.getEncoding(), System.currentTimeMillis() - start);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("Determined {}/{} applicable actions within {}ms of which preparing the FC algorithm consumed {}ms.", applicableDerivedActions.size(), limit, System.currentTimeMillis() - start, fcPreparationTime);
		return applicableDerivedActions;
	}

	public static void updateState(Monom state, Action appliedAction) {

		/* apply effects of action (STRIPS) */
		if (appliedAction.getOperation() instanceof StripsOperation) {
			Action a = new StripsAction((StripsOperation) appliedAction.getOperation(), appliedAction.getGrounding());
			state.removeAll(((StripsAction) a).getDeleteList());
			state.addAll(((StripsAction) a).getAddList());
		}

		/* apply effects of action (ConditionalEffect operations) */
		else if (appliedAction.getOperation() instanceof CEOperation) {
			CEAction a = new CEAction((CEOperation) appliedAction.getOperation(), appliedAction.getGrounding());
			Map<CNFFormula, Monom> addLists = a.getAddLists();

			/* determine literals to remove */
			Map<CNFFormula, Monom> deleteLists = a.getDeleteLists();
			Collection<Literal> toRemove = new ArrayList<>();
			for (CNFFormula condition : deleteLists.keySet()) {
				if (condition.entailedBy(state)) {
					toRemove.addAll(deleteLists.get(condition));
				}
			}

			/* determine literals to add */
			Collection<Literal> toAdd = new ArrayList<>();
			for (CNFFormula condition : addLists.keySet()) {

				/* evaluate interpreted predicates */
				CNFFormula modifiedCondition = new CNFFormula();
				boolean conditionIsSatisfiable = true;
				for (Clause c : condition) {
					Clause modifiedClause = new Clause();
					boolean clauseContainsTrue = false;
					for (Literal l : c) {
						modifiedClause.add(l);

						/* if the clause is not empty, add it to the condition */
						if (!clauseContainsTrue) {
							if (!modifiedClause.isEmpty())
								modifiedCondition.add(modifiedClause);
							else {
								conditionIsSatisfiable = false;
								break;
							}
						}
					}
				}
				if (conditionIsSatisfiable && modifiedCondition.entailedBy(state)) {
					toAdd.addAll(addLists.get(condition));
				}
			}

			/* now conduct update */
			state.removeAll(toRemove);
			state.addAll(toAdd);

		} else {
			System.err.println("No support for operations of class " + appliedAction.getOperation().getClass());
		}
	}

	public static Monom getStateAfterPlanExecution(Monom initState, Plan plan) {
		Monom state = new Monom(initState);
		plan.getActions().forEach(a -> updateState(state, a));
		return state;
	}

	public static void main(String[] args) {
		System.out.println("NON-PRIMITIVE!");
	}
}
