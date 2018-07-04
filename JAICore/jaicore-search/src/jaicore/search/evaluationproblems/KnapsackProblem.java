package jaicore.search.evaluationproblems;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jaicore.search.algorithms.interfaces.ISolutionEvaluator;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableGraphGenerator;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.core.NodeType;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class KnapsackProblem {
	
	public class KnapsackNode {
		
		private Set<String> packedObjects;
		private double usedCapacity;
		
		public KnapsackNode() {
			this.packedObjects = new HashSet<>();
			this.usedCapacity = 0.0d;
		}
		
		public KnapsackNode(Set<String> packedObjects, String newObject) {
			this.packedObjects = new HashSet<>();
			this.usedCapacity = 0.0d;
			for (String object : packedObjects) {
				this.packedObjects.add(object);
				this.usedCapacity += weights.get(object);
			}
			this.packedObjects.add(newObject);
			this.usedCapacity += weights.get(newObject);
		}
		
		public Set<String> getPackedObjects() {
			return this.packedObjects;
		}
		
		public double getUsedCapacity() {
			return this.usedCapacity;
		}
	}
	
	private Set<String> objects;
	private Map<String, Double> values;
	private Map<String, Double> weights;
	private double knapsackCapacity;
	
	public KnapsackProblem(Set<String> objects, Map<String, Double> values, Map<String, Double> weights, double knapsackCapacity) {
		this.objects = objects;
		this.values = values;
		this.weights = weights;
		this.knapsackCapacity = knapsackCapacity;
	}
	
	public SerializableGraphGenerator<KnapsackNode, String> getGraphGenerator() {
		return new SerializableGraphGenerator<KnapsackProblem.KnapsackNode, String>() {

			private static final long serialVersionUID = 1L;

			@Override
			public SingleRootGenerator<KnapsackNode> getRootGenerator() {
				return () -> new KnapsackNode();
			}

			@Override
			public SuccessorGenerator<KnapsackNode, String> getSuccessorGenerator() {
				return n -> {
					List<NodeExpansionDescription<KnapsackNode,String>> l = new ArrayList<>();
					for (String object : objects) {
						if(!n.getPackedObjects().contains(object)) {
							if (n.getUsedCapacity() + weights.get(object) <= knapsackCapacity) {
								l.add(new NodeExpansionDescription<>(n, new KnapsackNode(n.getPackedObjects(), object), "(" + n.getPackedObjects().toString() + ", " + object + ")", NodeType.OR));
							}
						}
					}
					return l;
				};
			}

			@Override
			public NodeGoalTester<KnapsackNode> getGoalTester() {
				return n -> {
					for (String object : objects) {
						if(!n.getPackedObjects().contains(object)) {
							if (n.getUsedCapacity() + weights.get(object) <= knapsackCapacity) {
								return false;
							}
						}
					}
					return true;
				};
			}

			@Override
			public boolean isSelfContained() {
				return true;
			}

			@Override
			public void setNodeNumbering(boolean nodenumbering) {
			}
		};
	}
	
	public ISolutionEvaluator<KnapsackNode, Double> getSolutionEvaluator() {
		return new ISolutionEvaluator<KnapsackProblem.KnapsackNode, Double>() {

			@Override
			public Double evaluateSolution(List<KnapsackNode> solutionPath) throws Exception {
				KnapsackNode packedKnapsack = solutionPath.get(solutionPath.size() - 1);
				if (packedKnapsack == null || packedKnapsack.usedCapacity > knapsackCapacity) {
					return 0.0d;
				} else {
					double packedValue = 0.0d;
					for (String object : packedKnapsack.getPackedObjects()) {
						packedValue += values.get(object);
					}
					return packedValue;
				}
			}

			@Override
			public boolean doesLastActionAffectScoreOfAnySubsequentSolution(List<KnapsackNode> partialSolutionPath) {
				return true;
			}
		};
	}
	
}
