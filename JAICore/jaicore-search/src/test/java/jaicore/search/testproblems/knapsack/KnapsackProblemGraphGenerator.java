package jaicore.search.testproblems.knapsack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.ILoggingCustomizable;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableGraphGenerator;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.model.travesaltree.NodeType;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SingleSuccessorGenerator;
import jaicore.testproblems.knapsack.KnapsackConfiguration;
import jaicore.testproblems.knapsack.KnapsackProblem;

public class KnapsackProblemGraphGenerator implements SerializableGraphGenerator<KnapsackConfiguration, String>, ILoggingCustomizable {

	private static final long serialVersionUID = 1L;

	private Logger logger = LoggerFactory.getLogger(KnapsackProblemGraphGenerator.class);
	private final KnapsackProblem problem;

	public KnapsackProblemGraphGenerator(KnapsackProblem problem) {
		super();
		this.problem = problem;
	}

	@Override
	public SingleRootGenerator<KnapsackConfiguration> getRootGenerator() {
		return () -> new KnapsackConfiguration(new LinkedList<>(), problem.getObjects(), 0.0);
	}

	@Override
	public SingleSuccessorGenerator<KnapsackConfiguration, String> getSuccessorGenerator() {

		return new SingleSuccessorGenerator<KnapsackConfiguration, String>() {

			private Map<KnapsackConfiguration, Set<Integer>> expandedChildren = new HashMap<>();

			private List<String> getPossiblePackingObjects(KnapsackConfiguration n) {
				List<String> possibleObjects = new ArrayList<>();
				Optional<String> objectWithHighestName = n.getPackedObjects().stream().max((o1,o2) -> o1.compareTo(o2));
//				if (objectWithHighestName.isPresent())
//				System.out.println(objectWithHighestName.get());
				for (String object : n.getRemainingObjects()) {
					if ((!objectWithHighestName.isPresent() || objectWithHighestName.get().compareTo(object) <= 0) && n.getUsedCapacity() + problem.getWeights().get(object) <= problem.getKnapsackCapacity()) {
						possibleObjects.add(object);
					}
				}
				return possibleObjects;
			}

			@Override
			public List<NodeExpansionDescription<KnapsackConfiguration, String>> generateSuccessors(KnapsackConfiguration node) throws InterruptedException {
				List<NodeExpansionDescription<KnapsackConfiguration, String>> l = new ArrayList<>();
				List<String> possibleDestinations = getPossiblePackingObjects(node);
				int n = possibleDestinations.size();
				for (int i = 0; i < n; i++) {
					l.add(generateSuccessor(node, possibleDestinations, i));
				}
				return l;
			}

			public NodeExpansionDescription<KnapsackConfiguration, String> generateSuccessor(KnapsackConfiguration node, List<String> objetcs, int i) throws InterruptedException {
				if (Thread.currentThread().isInterrupted()) {
					logger.info("Successor generation has been interrupted.");
					throw new InterruptedException("Successor generation interrupted");
				}
				if (!expandedChildren.containsKey(node))
					expandedChildren.put(node, new HashSet<>());
				int n = objetcs.size();
				int j = i % n;
				expandedChildren.get(node).add(j);
				String object = objetcs.get(j);
				Set<String> remainingObjects = new HashSet<>(node.getRemainingObjects());
				remainingObjects.remove(object);
				List<String> packedObjects = new ArrayList<>(node.getPackedObjects());
				packedObjects.add(object);
				double usedCapacity = node.getUsedCapacity() + problem.getWeights().get(object);
				KnapsackConfiguration newNode = new KnapsackConfiguration(packedObjects, remainingObjects, usedCapacity);
				return new NodeExpansionDescription<>(node, newNode, "(" + node.getPackedObjects().toString() + ", " + object + ")", NodeType.OR);
			}

			@Override
			public NodeExpansionDescription<KnapsackConfiguration, String> generateSuccessor(KnapsackConfiguration node, int i) throws InterruptedException {
				return generateSuccessor(node, getPossiblePackingObjects(node), i);
			}

			@Override
			public boolean allSuccessorsComputed(KnapsackConfiguration node) {
				return getPossiblePackingObjects(node).size() == expandedChildren.get(node).size();
			}
		};
	}

	@Override
	public NodeGoalTester<KnapsackConfiguration> getGoalTester() {
		return n -> {
			for (String object : problem.getObjects()) {
				if (!n.getPackedObjects().contains(object) && (n.getUsedCapacity() + problem.getWeights().get(object) <= problem.getKnapsackCapacity())) {
					return false;
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

		/* this is not relevant for this node generator */
	}

	@Override
	public String getLoggerName() {
		return logger.getName();
	}

	@Override
	public void setLoggerName(String name) {
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Switched logger name to {}", name);
	}
}