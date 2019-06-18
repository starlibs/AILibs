package ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.rtn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import ai.libs.jaicore.graph.LabeledGraph;
import ai.libs.jaicore.logic.fol.structure.ConstantParam;
import ai.libs.jaicore.logic.fol.structure.Literal;
import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.logic.fol.structure.VariableParam;
import ai.libs.jaicore.planning.classical.problems.ceoc.CEOCAction;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.planning.hierarchical.problems.stn.MethodInstance;

public class RTNUtil {

	private RTNUtil() {
		/* no instantiation desired */
	}

	public static List<TFDNode> serializeGraph(final LabeledGraph<RTNNode, RTNEdge> g) {
		RTNNode root = g.getSources().iterator().next();
		return serializeGraphUnderNode(g, root, new HashMap<>(), null, null);
	}

	private static List<TFDNode> serializeGraphUnderNode(final LabeledGraph<RTNNode, RTNEdge> g, final RTNNode n, final Map<ConstantParam,ConstantParam> context, final MethodInstance nextAppliedMethodInstance, final CEOCAction nextAppliedAction) {
		List<TFDNode> serialization = new ArrayList<>();
		for (RTNNode np : g.getSuccessors(n)) {

			/* define context */
			Map<ConstantParam, ConstantParam> mapExtension = g.getEdgeLabel(n, np).getContextRecreator();
			Map<ConstantParam,ConstantParam> newContext = new HashMap<>();
			newContext.putAll(context);
			if (mapExtension != null) {
				newContext.putAll(mapExtension);
			}

			Monom state = new Monom();
			for (Literal l : n.getState()) {
				state.add(new Literal(l, newContext));
			}
			List<Literal> tasks = new ArrayList<>();
			for (Literal l : n.getRemainingTasks()) {
				tasks.add(new Literal(l, newContext));
			}

			/* recover actual grounding for the action/method */
			RTNEdge edge = g.getEdgeLabel(n, np);
			MethodInstance appliedMethodInstance = null;
			CEOCAction appliedAction = null;
			if (edge.getMethodInstance() != null) {
				Map<VariableParam,ConstantParam> grounding = new HashMap<>(edge.getMethodInstance().getGrounding());
				for (Entry<VariableParam,ConstantParam> groundingEntry : grounding.entrySet()) {
					if (newContext.containsKey(groundingEntry.getValue())) {
						grounding.put(groundingEntry.getKey(), newContext.get(groundingEntry.getValue()));
					}
				}
				appliedMethodInstance = new MethodInstance(edge.getMethodInstance().getMethod(), grounding);
			}
			if (edge.getAppliedAction() != null) {
				Map<VariableParam,ConstantParam> grounding = new HashMap<>(edge.getAppliedAction().getGrounding());
				for (Entry<VariableParam, ConstantParam> groundingEntry : grounding.entrySet()) {
					if (newContext.containsKey(groundingEntry.getValue())) {
						grounding.put(groundingEntry.getKey(), newContext.get(groundingEntry.getValue()));
					}
				}
				appliedAction = new CEOCAction(edge.getAppliedAction().getOperation(), grounding);
			}

			/* now FIRST insert this node (and the attributes on the edge will be attributed to the successor node) */
			serialization.add(new TFDNode(state, tasks, nextAppliedMethodInstance, nextAppliedAction));
			serialization.addAll(serializeGraphUnderNode(g, np, newContext, appliedMethodInstance, appliedAction));
		}
		return serialization;
	}

	public static Collection<String> getClassesThatRemainToBeSeparated(final RTNNode node) {
		if (!getClustersThatWillBeRefined(node).isEmpty()) {
			return getClassesThatAreNotIsolated(node);
		}
		return node.getRemainingTasks().stream().filter(l -> l.getPropertyName().contains("declareClusterRepresentant")).map(l -> l.getConstantParams().get(1).getName()).collect(Collectors.toList());
	}

	public static Collection<String> getClassesThatAreInAnyCluster(final RTNNode node) {
		return getClassesThatExistInState(node).stream().filter(c -> node.getState().stream().anyMatch(l -> l.getPropertyName().equals("in") && l.getConstantParams().get(0).getName().equals(c))).collect(Collectors.toList());
	}

	public static Collection<String> getClassesThatAreInAClusterThatNeedsToBeRefined(final RTNNode node) {
		final Collection<String> clustersToBeRefined = getClustersThatWillBeRefined(node);
		Collection<String> remainingClasses = new HashSet<>();
		for (Literal l : node.getState()) {
			if (l.getPropertyName().equals("in") && clustersToBeRefined.contains(l.getConstantParams().get(1).getName())) {
				remainingClasses.add(l.getConstantParams().get(0).getName());
			}
		}
		return remainingClasses;
	}

	public static Collection<String> getClassesThatAreNotIsolated(final RTNNode node) {
		return getClassesThatAreInAnyCluster(node).stream().filter(c -> node.getState().stream().noneMatch(l -> l.getPropertyName().equals("represents") && l.getConstantParams().get(0).getName().equals(c))).collect(Collectors.toList());
	}

	public static Collection<String> getClassesThatAreIsolated(final RTNNode node) {
		return getClassesThatAreInAnyCluster(node).stream().filter(c -> node.getState().stream().anyMatch(l -> l.getPropertyName().equals("represents") && l.getConstantParams().get(0).getName().equals(c))).collect(Collectors.toList());
	}

	public static Collection<String> getClassesThatExistInState(final RTNNode node) {
		return node.getState().stream().filter(l -> l.getPropertyName().equals("in")).map(l -> l.getConstantParams().get(0).getName()).collect(Collectors.toList());
	}

	public static Collection<String> getClustersThatExistInState(final RTNNode node) {
		return node.getState().stream().filter(l -> l.getPropertyName().equals("in")).map(l -> l.getConstantParams().get(1).getName()).collect(Collectors.toList());
	}

	public static Collection<String> getClustersThatWillBeRefined(final RTNNode node) {
		Collection<String> clusters = new ArrayList<>();
		for (Literal l : node.getRemainingTasks()) {
			if (l.getPropertyName().contains("refine")) {
				clusters.add(l.getConstantParams().get(0).getName());
			}
		}
		return clusters;
	}
}
