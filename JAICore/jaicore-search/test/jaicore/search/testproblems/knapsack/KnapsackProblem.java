package jaicore.search.testproblems.knapsack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableGraphGenerator;
import jaicore.search.core.interfaces.ISolutionEvaluator;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.model.travesaltree.NodeType;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SingleSuccessorGenerator;

public class KnapsackProblem {

	public class KnapsackNode {

		private List<String> packedObjects;
		private Set<String> remainingObjects;
		private double usedCapacity;

		public KnapsackNode() {
			this.remainingObjects = new HashSet<>(objects);
			this.packedObjects = new LinkedList<>();
			this.usedCapacity = 0.0d;
		}

		public KnapsackNode(List<String> packedObjects, Set<String> remainingObjects, String newObject) {
			this.remainingObjects = new HashSet<>(remainingObjects);
			this.remainingObjects.remove(newObject);
			this.packedObjects = new LinkedList<>();
			this.usedCapacity = 0.0d;
			for (String object : packedObjects) {
				this.packedObjects.add(object);
				this.usedCapacity += weights.get(object);
			}
			this.packedObjects.add(newObject);
			this.usedCapacity += weights.get(newObject);
		}

		public List<String> getPackedObjects() {
			return this.packedObjects;
		}

		public double getUsedCapacity() {
			return this.usedCapacity;
		}

		public Set<String> getRemainingObjects() {
			return remainingObjects;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((packedObjects == null) ? 0 : packedObjects.hashCode());
			result = prime * result + ((remainingObjects == null) ? 0 : remainingObjects.hashCode());
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
			if (remainingObjects == null) {
				if (other.remainingObjects != null)
					return false;
			} else if (!remainingObjects.equals(other.remainingObjects))
				return false;
			if (Double.doubleToLongBits(usedCapacity) != Double.doubleToLongBits(other.usedCapacity))
				return false;
			return true;
		}

		@Override
		public String toString() {
			String s = "[";
			Iterator<String> it = packedObjects.iterator();
			while (it.hasNext()) {
				s += it.next();
				if (it.hasNext()) {
					s += ", ";
				}
			}
			s += "]-<" + usedCapacity + "/" + KnapsackProblem.this.knapsackCapacity + ">";
			return s;
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

	public double getKnapsackCapacity() {
		return knapsackCapacity;
	}

	public SerializableGraphGenerator<KnapsackNode, String> getGraphGenerator() {
		return new SerializableGraphGenerator<KnapsackNode, String>() {

			private static final long serialVersionUID = 1L;

			@Override
			public SingleRootGenerator<KnapsackNode> getRootGenerator() {
				return () -> new KnapsackNode();
			}

			@Override
			public SingleSuccessorGenerator<KnapsackNode, String> getSuccessorGenerator() {

				return new SingleSuccessorGenerator<KnapsackNode, String>() {

					private List<String> getPossiblePackingObjects(KnapsackNode n) {
						List<String> possibleObjects = new ArrayList<>();
						for (String object : n.getRemainingObjects()) {
							if (n.getUsedCapacity() + weights.get(object) <= knapsackCapacity) {
								possibleObjects.add(object);
							}
						}
						return possibleObjects;
					}

					@Override
					public List<NodeExpansionDescription<KnapsackNode, String>> generateSuccessors(KnapsackNode node) throws InterruptedException {
						List<NodeExpansionDescription<KnapsackNode, String>> l = new ArrayList<>();
						List<String> possibleDestinations = getPossiblePackingObjects(node);
						int N = possibleDestinations.size();
						for (int i = 0; i < N; i++) {
							l.add(generateSuccessor(node, possibleDestinations, i));
						}
						return l;
					}

					public NodeExpansionDescription<KnapsackNode, String> generateSuccessor(KnapsackNode n, List<String> objetcs, int i) throws InterruptedException {
						if (Thread.currentThread().isInterrupted()) {
							throw new InterruptedException("Successor generation interrupted");
						}
						int N = objetcs.size();
						String object = objetcs.get(i % N);
						KnapsackNode newNode = new KnapsackNode(n.getPackedObjects(), n.getRemainingObjects(), object);
						return new NodeExpansionDescription<KnapsackNode, String>(n, newNode, "(" + n.getPackedObjects().toString() + ", " + object + ")", NodeType.OR);
					}

					@Override
					public NodeExpansionDescription<KnapsackNode, String> generateSuccessor(KnapsackNode node, int i) throws InterruptedException {
						return generateSuccessor(node, getPossiblePackingObjects(node), i);
					}
				};
			}

			@Override
			public NodeGoalTester<KnapsackNode> getGoalTester() {
				return n -> {
					for (String object : objects) {
						if (!n.getPackedObjects().contains(object)) {
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
					return Double.MAX_VALUE;
				} else {
					double packedValue = 0.0d;
					for (String object : packedKnapsack.getPackedObjects()) {
						packedValue += values.get(object);
					}
					for (Set<String> bonusCombination : bonusPoints.keySet()) {
						boolean allContained = true;
						for (String object : bonusCombination) {
							if (!packedKnapsack.getPackedObjects().contains(object)) {
								allContained = false;
								break;
							}
						}
						if (allContained) {
							packedValue += bonusPoints.get(bonusCombination);
						}
					}
					return packedValue * -1;
				}
			}

			@Override
			public boolean doesLastActionAffectScoreOfAnySubsequentSolution(List<KnapsackNode> partialSolutionPath) {
				return true;
			}

			@Override
			public void cancel() {
				/* nothing to do */
			}
		};
	}

}
