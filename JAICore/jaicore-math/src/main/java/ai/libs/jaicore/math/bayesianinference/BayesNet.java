package ai.libs.jaicore.math.bayesianinference;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.graph.Graph;

public class BayesNet implements ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(BayesNet.class);
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

	public boolean isWellDefined() throws InterruptedException {
		for (String node : this.net.getItems()) {
			if (!this.isProbabilityTableOfNodeWellDefined(node)) {
				return false;
			}
		}
		return true;
	}

	public boolean isProbabilityTableOfNodeWellDefined(final String node) throws InterruptedException {

		/* compute cartesian product of parent values */
		Collection<String> parents = this.net.getPredecessors(node);
		if (parents.isEmpty()) {
			if (!this.map.containsKey(node) || this.map.get(node).size() != 1) {
				this.logger.error("{} has no probability map associated", node);
				return false;
			}
			double prob = this.map.get(node).get(new HashSet<>());
			return prob >= 0 && prob <= 1;
		}
		else {
			Collection<Collection<String>> powerset = SetUtil.powerset(parents);

			/* for each tuple, check whether the probability is defined */
			if (!this.map.containsKey(node)) {
				this.logger.error("{} has no probability map associated", node);
				return false;
			}
			Map<Set<String>, Double> tableOfNode = this.map.get(node);
			for (Collection<String> activeParents : powerset) {
				Set<String> activeParentsAsSet = new HashSet<>(activeParents);
				if (!tableOfNode.containsKey(activeParentsAsSet)) {
					this.logger.error("Entry {} not contained.", activeParents);
					return false;
				}
				double prob = tableOfNode.get(activeParentsAsSet);
				if (prob < 0 || prob > 1) {
					this.logger.error("Invalid probability {}", prob);
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

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}
}
