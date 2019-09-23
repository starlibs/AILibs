package ai.libs.jaicore.search.testproblems.knapsack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.implicit.NodeExpansionDescription;
import org.api4.java.datastructure.graph.implicit.NodeType;
import org.api4.java.datastructure.graph.implicit.SerializableGraphGenerator;
import org.api4.java.datastructure.graph.implicit.SingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.SingleSuccessorGenerator;
import org.api4.java.datastructure.graph.implicit.SuccessorGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.problems.knapsack.KnapsackConfiguration;
import ai.libs.jaicore.problems.knapsack.KnapsackProblem;

public class KnapsackProblemGraphGenerator implements SerializableGraphGenerator<KnapsackConfiguration, String>, ILoggingCustomizable {

	private static final long serialVersionUID = 1L;

	private transient Logger logger = LoggerFactory.getLogger(KnapsackProblemGraphGenerator.class);
	private final KnapsackProblem problem;

	public KnapsackProblemGraphGenerator(final KnapsackProblem problem) {
		super();
		this.problem = problem;
	}

	@Override
	public SingleRootGenerator<KnapsackConfiguration> getRootGenerator() {
		return () -> new KnapsackConfiguration(new HashSet<>(), this.problem.getObjects(), 0.0);
	}

	class KnapsackSuccessorGenerator implements SingleSuccessorGenerator<KnapsackConfiguration, String> {
		private Map<KnapsackConfiguration, Set<Integer>> expandedChildren = new HashMap<>();

		private List<String> getPossiblePackingObjects(final KnapsackConfiguration n) {
			List<String> possibleObjects = new ArrayList<>();
			Optional<String> objectWithHighestName = n.getPackedObjects().stream().max((o1, o2) -> o1.compareTo(o2));
			for (String object : n.getRemainingObjects()) {
				if ((!objectWithHighestName.isPresent() || objectWithHighestName.get().compareTo(object) <= 0)
						&& n.getUsedCapacity() + KnapsackProblemGraphGenerator.this.problem.getWeights().get(object) <= KnapsackProblemGraphGenerator.this.problem.getKnapsackCapacity()) {
					possibleObjects.add(object);
				}
			}
			return possibleObjects;
		}

		@Override
		public List<NodeExpansionDescription<KnapsackConfiguration, String>> generateSuccessors(final KnapsackConfiguration node) throws InterruptedException {
			List<NodeExpansionDescription<KnapsackConfiguration, String>> l = new ArrayList<>();
			List<String> possibleDestinations = this.getPossiblePackingObjects(node);
			int n = possibleDestinations.size();
			Thread.sleep(1);
			long lastSleep = System.currentTimeMillis();
			for (int i = 0; i < n; i++) {
				if (System.currentTimeMillis() - lastSleep > 10) {
					Thread.sleep(1);
					lastSleep = System.currentTimeMillis();
					KnapsackProblemGraphGenerator.this.logger.info("Sleeping");
				}
				l.add(this.generateSuccessor(node, possibleDestinations, i));
			}
			return l;
		}

		public NodeExpansionDescription<KnapsackConfiguration, String> generateSuccessor(final KnapsackConfiguration node, final List<String> objetcs, final int i) throws InterruptedException {
			KnapsackProblemGraphGenerator.this.logger.debug("Generating successor #{} of {}", i, node);
			if (Thread.interrupted()) { // reset interrupted flag prior to throwing the exception (Java convention)
				KnapsackProblemGraphGenerator.this.logger.info("Successor generation has been interrupted.");
				throw new InterruptedException("Successor generation interrupted");
			}
			if (!this.expandedChildren.containsKey(node)) {
				this.expandedChildren.put(node, new HashSet<>());
			}
			int n = objetcs.size();
			if (n == 0) {
				KnapsackProblemGraphGenerator.this.logger.debug("No objects left, quitting.");
				return null;
			}
			int j = i % n;
			this.expandedChildren.get(node).add(j);
			String object = objetcs.get(j);
			KnapsackProblemGraphGenerator.this.logger.trace("Creating set of remaining objects when choosing {}.", object);
			Set<String> packedObjects = new HashSet<>();
			Set<String> remainingObjects = new HashSet<>();
			boolean foundRemoved = false;
			for (String item : node.getRemainingObjects()) {
				Thread.sleep(1);
				if (!foundRemoved && item.equals(object)) {
					foundRemoved = true;
					packedObjects.add(item);
				}
				else {
					remainingObjects.add(item);
				}
			}
			packedObjects.addAll(node.getPackedObjects());
			KnapsackProblemGraphGenerator.this.logger.trace("Ready.");

			double usedCapacity = node.getUsedCapacity() + KnapsackProblemGraphGenerator.this.problem.getWeights().get(object);
			KnapsackConfiguration newNode = new KnapsackConfiguration(packedObjects, remainingObjects, usedCapacity);
			return new NodeExpansionDescription<>(newNode, "(" + node.getPackedObjects() + ", " + object + ")", NodeType.OR);
		}

		@Override
		public NodeExpansionDescription<KnapsackConfiguration, String> generateSuccessor(final KnapsackConfiguration node, final int i) throws InterruptedException {
			return this.generateSuccessor(node, this.getPossiblePackingObjects(node), i);
		}

		@Override
		public boolean allSuccessorsComputed(final KnapsackConfiguration node) {
			return this.getPossiblePackingObjects(node).size() == this.expandedChildren.get(node).size();
		}
	}

	@Override
	public SuccessorGenerator<KnapsackConfiguration, String> getSuccessorGenerator() {
		return new KnapsackSuccessorGenerator();
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Switched logger name to {}", name);
	}
}