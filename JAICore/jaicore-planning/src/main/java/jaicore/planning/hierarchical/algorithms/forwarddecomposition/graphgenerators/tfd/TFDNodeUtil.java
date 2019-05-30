package jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jaicore.basic.StringUtil;
import jaicore.basic.sets.SetUtil;
import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.theories.EvaluablePredicate;
import jaicore.planning.classical.algorithms.strips.forward.StripsUtil;
import jaicore.planning.core.Action;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.TaskPlannerUtil;
import jaicore.planning.hierarchical.problems.stn.MethodInstance;

public class TFDNodeUtil {

	private static Map<List<TFDNode>, Integer> cache = new HashMap<>();
	private final TaskPlannerUtil util;
	
	public TFDNodeUtil(Map<String, EvaluablePredicate> evaluablePlanningPredicates) {
		super();
		util = new TaskPlannerUtil(evaluablePlanningPredicates);
	}

	private boolean checkDoubleRestProblemComputationOccurrence(List<TFDNode> path) {
		if (cache.containsKey(path)) {
			System.out.println("already seen path " + cache.get(path) + " times");
			return false;
		}
		cache.put(path, 0);
		return true;
	}
	
	public List<TFDNode> getPathOfNode(TFDNode node, Map<TFDNode, TFDNode> parentMap) {
		
		/* compute path for node */
		List<TFDNode> path = new ArrayList<>();
		TFDNode current = node;
		while (current != null) {
			assert !path.contains(current) : "There is a loop in the path! Node " + node + " has been visited twice!\n\tThe full path is: \n\t\t" + StringUtil.implode(SetUtil.getInvertedCopyOfList(SetUtil.addAndGet(path, current)).stream().map(n -> n.toString()).collect(Collectors.toList()), "\n\t\t");
			path.add(current);
			current = parentMap.get(current);
		}
		Collections.reverse(path);
		return path;
	}

	public TFDRestProblem getRestProblem(List<TFDNode> path) {

		/* get last node in list with explicit rest problem formulation */
		assert checkDoubleRestProblemComputationOccurrence(path) : "We must not generate the information of a node twice!";

		/* identify latest node that has an explicit rest problem attached */
		TFDNode latest = null;
		for (TFDNode n : path) {
			if (n.getProblem() != null)
				latest = n;
		}

		/* set iterator to the last check point node */
		Iterator<TFDNode> i = path.iterator();
		TFDNode init = null;
		do {
			TFDNode n = i.next();
			if (n == latest)
				init = n;
		} while (init == null);

		/* compute the rest problem going from there */
		Monom state = new Monom(init.getState(), false);
		List<Literal> remainingTasks = new ArrayList<>(init.getRemainingTasks());
		while (i.hasNext()) {
			TFDNode n = i.next();

			/* compute updated state */
			Action appliedAction = n.getAppliedAction();
			if (appliedAction != null) {
				StripsUtil.updateState(state, appliedAction);
			}

			/* compute remaining tasks */
			remainingTasks.remove(0);
			MethodInstance appliedMethodInstance = n.getAppliedMethodInstance();
			if (appliedMethodInstance != null) {
				int j = 0;
				for (Literal remainingTask : util.getTaskChainOfTotallyOrderedNetwork(appliedMethodInstance.getNetwork())) {
					remainingTasks.add(j++, remainingTask);
				}
			}
		}
		return new TFDRestProblem(state, new ArrayList<>(remainingTasks));
	}

	public Monom getState(List<TFDNode> path) {
		return getRestProblem(path).getState();
	}

	@SuppressWarnings("unused")
	private boolean checkConsistency(Monom state, Map<CNFFormula, Monom> addLists) {
		for (Literal lit : state) {
			if (lit.getPropertyName().equals("cluster")) {
				String clusterName = lit.getConstantParams().get(0).getName();
				boolean foundSmallest = false;
				boolean foundRepresentant = false;
				for (Literal lit2 : state) {
					if (lit2.getPropertyName().equals("smallest") && lit2.getConstantParams().get(1).getName().equals(clusterName)) {
						foundSmallest = true;
						String smallestItem = lit2.getConstantParams().get(0).getName();
						List<ConstantParam> params = new ArrayList<>();
						params.add(new ConstantParam(smallestItem));
						params.add(new ConstantParam(clusterName));
						Literal lit3 = new Literal("in", params);
						if (!state.contains(lit3)) {
							throw new IllegalStateException(
									"Smallest item in cluster " + clusterName + " is " + smallestItem + ", which is not even contained according to state " + state + "!");
						}
						for (Literal lit4 : state) {
							if (lit4.getPropertyName().equals("in") && lit4.getConstantParams().get(1).getName().equals(clusterName)) {
								if (state.contains(new Literal("bigger('" + smallestItem + "','" + lit4.getConstantParams().get(0).getName() + "')"))) {
									throw new IllegalStateException("Cluster " + clusterName + " has " + smallestItem + " as smallest item, but "
											+ lit4.getConstantParams().get(0).getName() + " is smaller");
								}
							}
						}
						break;
					}
					if (lit2.getPropertyName().equals("represents") && lit2.getConstantParams().get(1).getName().equals(clusterName)) {
						foundRepresentant = true;
						break;
					}
				}
				if (!foundSmallest && !foundRepresentant) {
					// for (CNFFormula condition : addLists.keySet()) {
					// CNFFormula evaledCondition = LogicUtil.evalEqualityLiteralsUnderUNA(condition);
					// System.out.println(evaledCondition + ": " + (evaledCondition.isConsistent() && state.containsAll(evaledCondition)));
					// if (evaledCondition.isConsistent())
					// System.out.println(state);
					// }
					// System.err.println(logger.toString());
					throw new IllegalStateException("State " + state + " does not specify a smallest element for cluster " + clusterName + " after applying addList " + addLists);
				}
			}
		}
		return true;
	}

	public List<Literal> getRemainingTasks(List<TFDNode> path) {
		return getRestProblem(path).getRemainingTasks();
	}
}
