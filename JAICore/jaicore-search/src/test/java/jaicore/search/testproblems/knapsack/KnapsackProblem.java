package jaicore.search.testproblems.knapsack;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jaicore.search.core.interfaces.ISolutionEvaluator;

@SuppressWarnings("serial")
public class KnapsackProblem implements Serializable {
	private final Set<String> objects;
	private final Map<String, Double> values;
	private final Map<String, Double> weights;
	private final Map<Set<String>, Double> bonusPoints;
	private final double knapsackCapacity;

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

	public ISolutionEvaluator<KnapsackNode, Double> getSolutionEvaluator() {
		return new ISolutionEvaluator<KnapsackNode, Double>() {

			@Override
			public Double evaluateSolution(List<KnapsackNode> solutionPath) {
				KnapsackNode packedKnapsack = solutionPath.get(solutionPath.size() - 1);
				if (packedKnapsack == null || packedKnapsack.getUsedCapacity() > knapsackCapacity) {
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

	public Set<String> getObjects() {
		return objects;
	}

	public Map<String, Double> getValues() {
		return values;
	}

	public Map<String, Double> getWeights() {
		return weights;
	}

	public Map<Set<String>, Double> getBonusPoints() {
		return bonusPoints;
	}
}
