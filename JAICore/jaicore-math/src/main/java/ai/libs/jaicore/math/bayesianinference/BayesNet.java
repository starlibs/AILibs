package ai.libs.jaicore.math.bayesianinference;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.graph.Graph;

public class BayesNet {

	private Map<String, Map<Set<String>, Double>> map = new HashMap<>();

	private Graph<String> net = new Graph<>();

	public void addNode(final String name) {
		this.net.addItem(name);
	}

	public void addDependency(final String child, final String parent) {
		this.net.addEdge(parent, child);
	}

	public void addProbability(final String node, final Collection<String> parentsThatAreTrue, final double probability) {
		if (!this.net.hasItem(node)) {
			throw new IllegalArgumentException("Cannot add probability for unknown node " + node);
		}
		this.map.computeIfAbsent(node, n -> new HashMap<>()).put(new HashSet<>(parentsThatAreTrue), probability);
	}

	public void addProbability(final String node, final double probability) {
		if (!this.net.getPredecessors(node).isEmpty()) {
			throw new IllegalArgumentException("Cannot define prior on non-root node " + node);
		}
		this.addProbability(node, new HashSet<>(), probability);
	}

	public boolean isWellDefined() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException {
		for (String node : this.net.getItems()) {
			if (!this.isProbabilityTableOfNodeWellDefined(node)) {
				return false;
			}
		}
		return true;
	}

	public boolean isProbabilityTableOfNodeWellDefined(final String node) throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException {

		/* compute cartesian product of parent values */
		Collection<String> parents = this.net.getPredecessors(node);
		if (parents.isEmpty()) {
			if (!this.map.containsKey(node) || this.map.get(node).size() != 1) {
				System.err.println(node + " has no probability map associated");
				return false;
			}
			double prob = this.map.get(node).get(new HashSet<>());
			return prob >= 0 && prob <= 1;
		}
		else {
			Collection<Collection<String>> powerset = SetUtil.powerset(parents);

			/* for each tuple, check whether the probability is defined */
			if (!this.map.containsKey(node)) {
				System.err.println(node + " has no probability map associated");
				return false;
			}
			Map<Set<String>, Double> tableOfNode = this.map.get(node);
			for (Collection<String> activeParents : powerset) {
				Set<String> activeParentsAsSet = new HashSet<>(activeParents);
				if (!tableOfNode.containsKey(activeParentsAsSet)) {
					System.err.println("Entry " + activeParents + " not contained.");
					return false;
				}
				double prob = tableOfNode.get(activeParentsAsSet);
				if (prob < 0 || prob > 1) {
					System.err.println("Invalid probability " + prob);
					return false;
				}
			}
			return true;
		}
	}

	public Map<String, Map<Set<String>, Double>> getMap() {
		return this.map;
	}

	public Graph<String> getNet() {
		return this.net;
	}

	public double getProbabilityOfPositiveEvent(final String node, final Set<String> event) {
		Set<String> relevantSubset = new HashSet<>(SetUtil.intersection(event, this.net.getPredecessors(node)));
		return this.map.get(node).get(relevantSubset);
	}
}
