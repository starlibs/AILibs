package ai.libs.jaicore.math.bayesianinference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ai.libs.jaicore.basic.sets.SetUtil;

public class DiscreteProbabilityDistribution {
	private final List<String> variables = new ArrayList<>();
	private final Map<Set<String>, Double> probabilities = new HashMap<>();

	public DiscreteProbabilityDistribution() {
		super();
	}

	public void addProbability(final Collection<String> variablesThatAreTrue, final double probability) {
		for (String newVar : SetUtil.difference(variablesThatAreTrue, this.variables)) {
			this.variables.add(newVar);
		}
		this.probabilities.put(variablesThatAreTrue instanceof Set ? (Set<String>)variablesThatAreTrue : new HashSet<>(variablesThatAreTrue), probability);
	}
	public Map<Set<String>, Double> getProbabilities() {
		return this.probabilities;
	}

	public List<String> getVariables() {
		return this.variables;
	}

	public DiscreteProbabilityDistribution getNormalizedCopy() {
		/* prepare coefficients for linear equation system (with one constraint) */
		double sum = 0;
		List<Set<String>> assignments = new ArrayList<>();
		for (Entry<Set<String>, Double> prob : this.probabilities.entrySet()) {
			sum += prob.getValue();
			assignments.add(prob.getKey());
		}
		if (sum == 0) {
			throw new IllegalStateException("Cannot normalize a distribution with zero mass.");
		}

		/* compute alpha */
		double alpha = 1.0 / sum;
		DiscreteProbabilityDistribution newDist = new DiscreteProbabilityDistribution();
		for (Set<String> assignment : assignments) {
			newDist.addProbability(assignment, this.probabilities.get(assignment) * alpha);
		}
		return newDist;
	}
}
