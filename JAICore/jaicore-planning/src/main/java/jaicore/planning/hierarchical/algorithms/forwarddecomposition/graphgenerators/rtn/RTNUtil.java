package jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.rtn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jaicore.graph.LabeledGraph;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;
import jaicore.planning.classical.problems.ceoc.CEOCAction;
import jaicore.planning.classical.problems.ceoc.CEOCOperation;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.planning.hierarchical.problems.stn.MethodInstance;

public class RTNUtil {

	public static List<TFDNode> serializeGraph(LabeledGraph<RTNNode, RTNEdge> g) {
		RTNNode root = g.getSources().iterator().next();
		return serializeGraphUnderNode(g, root, new HashMap<>(), null, null);
	}

	private static List<TFDNode> serializeGraphUnderNode(LabeledGraph<RTNNode, RTNEdge> g, RTNNode n, Map<ConstantParam,ConstantParam> context, MethodInstance nextAppliedMethodInstance, CEOCAction nextAppliedAction) {
		List<TFDNode> serialization = new ArrayList<>();
		for (RTNNode np : g.getSuccessors(n)) {
			
			/* define context */
			Map<ConstantParam, ConstantParam> mapExtension = g.getEdgeLabel(n, np).getContextRecreator();
			Map<ConstantParam,ConstantParam> newContext = new HashMap<>();
			newContext.putAll(context);
			if (mapExtension != null)
				newContext.putAll(mapExtension);
			
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
				for (VariableParam p : grounding.keySet()) {
					if (newContext.containsKey(grounding.get(p)))
						grounding.put(p, newContext.get(grounding.get(p)));
				}
				appliedMethodInstance = new MethodInstance(edge.getMethodInstance().getMethod(), grounding);
			}
			if (edge.getAppliedAction() != null) {
				Map<VariableParam,ConstantParam> grounding = new HashMap<>(edge.getAppliedAction().getGrounding());
				for (VariableParam p : grounding.keySet()) {
					if (newContext.containsKey(grounding.get(p)))
						grounding.put(p, newContext.get(grounding.get(p)));
				}
				appliedAction = new CEOCAction((CEOCOperation)edge.getAppliedAction().getOperation(), grounding);
			}
			
			/* now FIRST insert this node (and the attributes on the edge will be attributed to the successor node) */
			serialization.add(new TFDNode(state, tasks, nextAppliedMethodInstance, nextAppliedAction));
			serialization.addAll(serializeGraphUnderNode(g, np, newContext, appliedMethodInstance, appliedAction));
		}
		return serialization;
	}
	
	public static Collection<String> getClassesThatRemainToBeSeparated(RTNNode node) {
		if (!getClustersThatWillBeRefined(node).isEmpty())
			return getClassesThatAreNotIsolated(node);
		Collection<String> itemToDeclare = node.getRemainingTasks().stream().filter(l -> l.getPropertyName().contains("declareClusterRepresentant")).map(l -> l.getConstantParams().get(1).getName()).collect(Collectors.toList());
		return itemToDeclare;
	}
	
	public static Collection<String> getClassesThatAreInAnyCluster(RTNNode node) {
		return getClassesThatExistInState(node).stream().filter(c -> node.getState().stream().filter(l -> l.getPropertyName().equals("in") && l.getConstantParams().get(0).getName().equals(c)).findAny().isPresent()).collect(Collectors.toList());
	}
	
	public static Collection<String> getClassesThatAreInAClusterThatNeedsToBeRefined(RTNNode node) {
		final Collection<String> clustersToBeRefined = getClustersThatWillBeRefined(node);
		Collection<String> remainingClasses = new HashSet<>();
		for (Literal l : node.getState()) {
			if (l.getPropertyName().equals("in") && clustersToBeRefined.contains(l.getConstantParams().get(1).getName()))
				remainingClasses.add(l.getConstantParams().get(0).getName());
		}
		return remainingClasses;
	}
	
	public static Collection<String> getClassesThatAreNotIsolated(RTNNode node) {
		return getClassesThatAreInAnyCluster(node).stream().filter(c -> !node.getState().stream().filter(l -> l.getPropertyName().equals("represents") && l.getConstantParams().get(0).getName().equals(c)).findAny().isPresent()).collect(Collectors.toList());
	}
	
	public static Collection<String> getClassesThatAreIsolated(RTNNode node) {
		return getClassesThatAreInAnyCluster(node).stream().filter(c -> node.getState().stream().filter(l -> l.getPropertyName().equals("represents") && l.getConstantParams().get(0).getName().equals(c)).findAny().isPresent()).collect(Collectors.toList());
	}
	
	public static Collection<String> getClassesThatExistInState(RTNNode node) {
		return node.getState().stream().filter(l -> l.getPropertyName().equals("in")).map(l -> l.getConstantParams().get(0).getName()).collect(Collectors.toList());
	}
	
	public static Collection<String> getClustersThatExistInState(RTNNode node) {
		return node.getState().stream().filter(l -> l.getPropertyName().equals("in")).map(l -> l.getConstantParams().get(1).getName()).collect(Collectors.toList());
	}
	
	public static Collection<String> getClustersThatWillBeRefined(RTNNode node) {
		Collection<String> clusters = new ArrayList<>();
		for (Literal l : node.getRemainingTasks()) {
			if (l.getPropertyName().contains("refine"))
				clusters.add(l.getConstantParams().get(0).getName());
		}
		return clusters;
	}
}
