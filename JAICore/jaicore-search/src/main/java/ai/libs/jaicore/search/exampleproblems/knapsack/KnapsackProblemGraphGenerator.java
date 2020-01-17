package ai.libs.jaicore.search.exampleproblems.knapsack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.ILazySuccessorGenerator;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;
import org.api4.java.datastructure.graph.implicit.ISingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.ISuccessorGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.MappingIterator;
import ai.libs.jaicore.problems.knapsack.KnapsackConfiguration;
import ai.libs.jaicore.problems.knapsack.KnapsackProblem;
import ai.libs.jaicore.search.model.NodeExpansionDescription;

public class KnapsackProblemGraphGenerator implements IGraphGenerator<KnapsackConfiguration, String>, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(KnapsackProblemGraphGenerator.class);
	private final KnapsackProblem problem;

	public KnapsackProblemGraphGenerator(final KnapsackProblem problem) {
		super();
		this.problem = problem;
	}

	@Override
	public ISingleRootGenerator<KnapsackConfiguration> getRootGenerator() {
		return () -> new KnapsackConfiguration(new HashSet<>(), this.problem.getObjects(), 0.0);
	}

	class KnapsackSuccessorGenerator implements ILazySuccessorGenerator<KnapsackConfiguration, String> {
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
		public List<INewNodeDescription<KnapsackConfiguration, String>> generateSuccessors(final KnapsackConfiguration node) throws InterruptedException {
			List<INewNodeDescription<KnapsackConfiguration, String>> l = new ArrayList<>();
			List<String> possibleDestinations = this.getPossiblePackingObjects(node);
			int n = possibleDestinations.size();
			Thread.sleep(1);
			long lastSleep = System.currentTimeMillis();
			for (int i = 0; i < n; i++) {
				if (System.currentTimeMillis() - lastSleep > 10) {
					if (Thread.interrupted()) { // reset interrupted flag prior to throwing the exception (Java convention)
						KnapsackProblemGraphGenerator.this.logger.info("Successor generation has been interrupted.");
						throw new InterruptedException("Successor generation interrupted");
					}
					Thread.sleep(1);
					lastSleep = System.currentTimeMillis();
					KnapsackProblemGraphGenerator.this.logger.info("Sleeping");
				}
				l.add(this.generateSuccessor(node, possibleDestinations, i));
			}
			return l;
		}

		public INewNodeDescription<KnapsackConfiguration, String> generateSuccessor(final KnapsackConfiguration node, final List<String> objetcs, final int i) {
			KnapsackProblemGraphGenerator.this.logger.debug("Generating successor #{} of {}", i, node);
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
			return new NodeExpansionDescription<>(newNode, "(" + node.getPackedObjects() + ", " + object + ")");
		}

		@Override
		public Iterator<INewNodeDescription<KnapsackConfiguration, String>> getIterativeGenerator(final KnapsackConfiguration node) {
			List<String> possibleObjects = this.getPossiblePackingObjects(node);
			return new MappingIterator<>(IntStream.range(0, possibleObjects.size()).iterator(), i -> this.generateSuccessor(node, possibleObjects, i));
		}
	}

	@Override
	public ISuccessorGenerator<KnapsackConfiguration, String> getSuccessorGenerator() {
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