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
			applicableDerivedActions.addAll(getPossibleOperationGroundingsForState(state, (StripsOperation)op));
		}
		return applicableDerivedActions;
	}
	
	public static Collection<StripsAction> getPossibleOperationGroundingsForState(Monom state, StripsOperation operation) {
		Collection<StripsAction> applicableDerivedActions = new ArrayList<>();
		
		/* decompose premise in positive and negative literals */
		Collection<Map<VariableParam, LiteralParam>> groundings = LogicUtil.getSubstitutionsThatEnableForwardChainingUnderCWA(state, operation.getPrecondition());
		logger.info("Computed {} groundings that yield valid deductions of premise {} in state {}", groundings.size(), operation.getPrecondition(), state);
		
//		/* implement groundings here */
//		try {
//			
//			/* compute type-compatible groundings */
//			Map<Type,Collection<VariableParam>> paramsPerType = SetUtil.groupCollectionByAttribute(operation.getParams(), p -> p.getType());
//			Map<Type,Collection<ConstantParam>> constantsPerType = SetUtil.groupCollectionByAttribute(state.getConstantParams(), p -> p.getType());
//			List<Collection<Map<VariableParam,ConstantParam>>> groundingsPerType = new ArrayList<>();
//			for (Type t : paramsPerType.keySet()) {
//				if (!constantsPerType.containsKey(t)) {
//					logger.warn("There is a parameter of type {} in the operation {}, but no constant has that type!", t, operation.getName());
//					return new ArrayList<>();
//				}
//				int m = constantsPerType.get(t).size();
//				int n = paramsPerType.get(t).size();
//				logger.info("Computing {} total mappings from operation with {} params of type {} to state with {} constants of type {}", Math.pow(m, n), n, t, m, t);
//				groundingsPerType.add(SetUtil.allTotalMappings(paramsPerType.get(t), constantsPerType.get(t)));
//			}
//			
//			/* walk over the cartesian product */
//			Collection<List<Map<VariableParam,ConstantParam>>> cartesianProductofGroundings = SetUtil.cartesianProduct(groundingsPerType);
//			logger.info("Walking over {} groundings ...", cartesianProductofGroundings.size());
//			for (List<Map<VariableParam,ConstantParam>> decomposedGrounding : cartesianProductofGroundings) {
//				Map<VariableParam,ConstantParam> grounding = new HashMap<>();
//				decomposedGrounding.forEach(g -> grounding.putAll(g));
			for (Map<VariableParam, LiteralParam> grounding : groundings) {
//				Monom groundPrecondition = new Monom(operation.getPrecondition(), grounding);
//				List<Literal> positiveLiterals = groundPrecondition.stream().filter(l -> l.isPositive()).collect(Collectors.toList());
//				List<Literal> negativeLiterals = groundPrecondition.stream().filter(l -> l.isNegated()).map(l -> l.clone().toggleNegation()).collect(Collectors.toList());
//				if (state.containsAll(positiveLiterals) && SetUtil.intersection(state, negativeLiterals).isEmpty()) {
//					logger.trace("Grounding {} is applicable, because state contains positive literals {} and does not contain negative literals {}", grounding, positiveLiterals, negativeLiterals);
//					
					/* refactor grounding to constants only and add the respective action */
					Map<VariableParam, ConstantParam> rGrounding = new HashMap<>();
					for (VariableParam p : grounding.keySet()) {
						ConstantParam cp = (ConstantParam)grounding.get(p);
						rGrounding.put(p, cp);
					}
					StripsAction a = new StripsAction(operation, rGrounding);
					applicableDerivedActions.add(a);
					logger.debug("Found action {} to be applicable.", a.getEncoding());
//				}
//				else if (logger.isTraceEnabled()) {
//					Collection<Literal> missingPositiveLiterals = SetUtil.difference(positiveLiterals, state);
//					Collection<Literal> containedNegativeLiterals = SetUtil.intersection(negativeLiterals, state);
//					logger.trace("Grounding {} is NOT applicable. Missing positive literals not contained in state: {}. Negative literals contained in state: {}.", grounding, missingPositiveLiterals, containedNegativeLiterals);
//				}
			}
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		
		return applicableDerivedActions;
	}
	

	public static void updateState(Monom state, Action appliedAction) {

		// assert state.containsAll(appliedAction.getPrecondition().stream().filter(lit -> lit.isPositive()).collect(Collectors.toList())) && SetUtil.disjoint(state,
		// appliedAction.getPrecondition().stream().filter(lit -> lit.isNegated()).collect(Collectors.toList())) : ("Action " + appliedAction + " is supposed to be aplpicable in state " + state + "
		// but it is not!");
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

	public static void main(String[] args) {
		System.out.println("NON-PRIMITIVE!");
	}
}
