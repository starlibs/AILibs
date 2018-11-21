package jaicore.planning.model.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.sets.SetUtil;
import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.Clause;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.LiteralParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;
import jaicore.logic.fol.util.LogicUtil;
import jaicore.planning.model.conditional.CEAction;
import jaicore.planning.model.conditional.CEOperation;
import jaicore.planning.model.strips.StripsAction;
import jaicore.planning.model.strips.StripsOperation;
import jaicore.planning.model.strips.StripsPlanningDomain;

public class PlannerUtil {

	private static final Logger logger = LoggerFactory.getLogger(PlannerUtil.class);

	public static Collection<StripsAction> getApplicableActionsInState(Monom state, StripsPlanningDomain domain) {
		Collection<StripsAction> applicableDerivedActions = new ArrayList<>();
		for (Operation op : domain.getOperations()) {
			applicableDerivedActions.addAll(getPossibleOperationGroundingsForState(state, (StripsOperation) op));
		}
		return applicableDerivedActions;
	}

	public static Collection<StripsAction> getPossibleOperationGroundingsForState(Monom state,
			StripsOperation operation) {
		Collection<StripsAction> applicableDerivedActions = new ArrayList<>();

		/* decompose premise in positive and negative literals */
		Collection<Map<VariableParam, LiteralParam>> groundings = LogicUtil
				.getSubstitutionsThatEnableForwardChainingUnderCWA(state, operation.getPrecondition());
		logger.info("Computed {} groundings that yield valid deductions of premise {} in state {}", groundings.size(),
				operation.getPrecondition(), state);

		for (Map<VariableParam, LiteralParam> grounding : groundings) {
			/* refactor grounding to constants only and add the respective action */
			Map<VariableParam, ConstantParam> rGrounding = new HashMap<>();
			for (VariableParam p : grounding.keySet()) {
				ConstantParam cp = (ConstantParam) grounding.get(p);
				rGrounding.put(p, cp);
			}
			StripsAction a = new StripsAction(operation, rGrounding);
			applicableDerivedActions.add(a);
			logger.debug("Found action {} to be applicable.", a.getEncoding());

		}
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
	
	public static Monom getStateAfterPlanExecution(Monom initState, Plan<?> plan) {
		Monom state = new Monom(initState);
		plan.getActions().forEach(a -> updateState(state, a));
		return state;
	}

	public static void main(String[] args) {
		System.out.println("NON-PRIMITIVE!");
	}
}
