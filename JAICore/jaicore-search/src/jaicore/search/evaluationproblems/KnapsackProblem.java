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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((packedObjects == null) ? 0 : packedObjects.hashCode());
			long temp;
			temp = Double.doubleToLongBits(usedCapacity);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			KnapsackNode other = (KnapsackNode) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (packedObjects == null) {
				if (other.packedObjects != null)
					return false;
			} else if (!packedObjects.equals(other.packedObjects))
				return false;
			if (Double.doubleToLongBits(usedCapacity) != Double.doubleToLongBits(other.usedCapacity))
				return false;
			return true;
		}

		private KnapsackProblem getOuterType() {
			return KnapsackProblem.this;
		}

	}
	
	private Set<String> objects;
	private Map<String, Double> values;
	private Map<String, Double> weights;
	private Map<Set<String>, Double> bonusPoints;
	private double knapsackCapacity;
	
	public KnapsackProblem(Set<String> objects, Map<String, Double> values, Map<String, Double> weights, Map<Set<String>, Double> bonusPoints, double knapsackCapacity) {
		this.objects = objects;
		this.values = values;
		this.weights = weights;
		this.bonusPoints = bonusPoints;
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
					for (Set<String> bonusCombination : bonusPoints.keySet()) {
						boolean allContained = true;
						for (String object : bonusCombination ) {
							if (!packedKnapsack.getPackedObjects().contains(object)) {
								allContained = false;
								break;
							}
						}
						if (allContained) {
							packedValue += bonusPoints.get(bonusCombination);
						}
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
