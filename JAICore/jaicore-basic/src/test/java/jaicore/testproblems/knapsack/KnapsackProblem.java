package jaicore.testproblems.knapsack;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import jaicore.basic.IObjectEvaluator;

@SuppressWarnings("serial")
public class KnapsackProblem implements Serializable {
	private final Set<String> objects;
	private final Map<String, Double> values;
	private final Map<String, Double> weights;
	private final Map<Set<String>, Double> bonusPoints;
	private final double knapsackCapacity;

	public KnapsackProblem(final Set<String> objects, final Map<String, Double> values, final Map<String, Double> weights, final Map<Set<String>, Double> bonusPoints, final double knapsackCapacity) {
		this.objects = objects;
		this.values = values;
		this.weights = weights;
		this.bonusPoints = bonusPoints;
		this.knapsackCapacity = knapsackCapacity;
	}

	public double getKnapsackCapacity() {
		return this.knapsackCapacity;
	}

	public IObjectEvaluator<KnapsackConfiguration, Double> getSolutionEvaluator() {
		return packedKnapsack -> {
			if (packedKnapsack == null || packedKnapsack.getUsedCapacity() > KnapsackProblem.this.knapsackCapacity) {
				return Double.MAX_VALUE;
			} else {
				double packedValue = 0.0d;
				for (String object : packedKnapsack.getPackedObjects()) {
					packedValue += KnapsackProblem.this.values.get(object);
				}
				for (Set<String> bonusCombination : KnapsackProblem.this.bonusPoints.keySet()) {
					boolean allContained = true;
					for (String object : bonusCombination) {
						if (!packedKnapsack.getPackedObjects().contains(object)) {
							allContained = false;
							break;
						}
					}
					if (allContained) {
						packedValue += KnapsackProblem.this.bonusPoints.get(bonusCombination);
					}
				}
				return packedValue * -1;
			}
		};
	}

	public Set<String> getObjects() {
		return this.objects;
	}

	public Map<String, Double> getValues() {
		return this.values;
	}

	public Map<String, Double> getWeights() {
		return this.weights;
	}

	public Map<Set<String>, Double> getBonusPoints() {
		return this.bonusPoints;
	}

	@Override
	public String toString() {
		return "KnapsackProblem [objects=" + this.objects + ", values=" + this.values + ", weights=" + this.weights + ", bonusPoints=" + this.bonusPoints + ", knapsackCapacity=" + this.knapsackCapacity + "]";
	}
}
