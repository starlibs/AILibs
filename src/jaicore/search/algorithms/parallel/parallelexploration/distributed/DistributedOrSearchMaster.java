package jaicore.search.algorithms.parallel.parallelexploration.distributed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import jaicore.search.algorithms.parallel.parallelexploration.distributed.events.NodePassedToCoworkerEvent;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.DistributedSearchCommunicationLayer;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.DistributionSearchResultProcessor;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableGraphGenerator;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableNodeEvaluator;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.events.NodeTypeSwitchEvent;

public class DistributedOrSearchMaster<T, A, V extends Comparable<V>> extends ORGraphSearch<T, A, V> implements DistributionSearchResultProcessor<T, V> {

	private static final Logger logger = LoggerFactory.getLogger(DistributedOrSearchMaster.class);
	private final DistributedSearchManager<T, A, V> manager;

	public DistributedOrSearchMaster(SerializableGraphGenerator<T, A> graphGenerator, SerializableNodeEvaluator<T, V> pNodeEvaluator,
			DistributedSearchCommunicationLayer<T, A, V> communicationLayer, int numberOfThreads) {
		super(graphGenerator, pNodeEvaluator);
		this.manager = new DistributedSearchManager<>(communicationLayer, this);
		this.manager.getEventBus().register(this);
		try {
			communicationLayer.init();
			communicationLayer.setGraphGenerator(graphGenerator);
			communicationLayer.setNodeEvaluator(pNodeEvaluator);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Subscribe
	public void receiveNodeEvent(NodePassedToCoworkerEvent<Node<T,V>> e) {
		this.eventBus.post(new NodeTypeSwitchEvent<>(e.getNode(), "or_distributed"));
	}
	
	public void afterInitialization() {

		while (!Thread.interrupted()) {
			if (!open.isEmpty())
				distributeNodes();

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		cancel();
	}

	@Override
	protected boolean terminates() {
		if (!super.terminates())
			return false;
		if (manager.isBusy())
			return false;
		return true;
	}

	public void cancel() {
		super.cancel();
		manager.shutdown();
	}

	private Node<T, V> getLocalVersionOfNode(Node<T, V> node) {
		return ext2int.get(node.getPoint());
	}

	private void distributeNodes() {
		logger.info("Distributing nodes ...");
		manager.distributeNodesRemotely(pollNodesForDistribution(Math.max(1, manager.getNumberOfHelpers())));
	}

	private Collection<Node<T, V>> pollNodesForDistribution(int helpers) {
		Collection<Node<T, V>> nodes = new ArrayList<>();
		if (open.isEmpty())
			return null;
//		int i = 0;
//		max = open.size();
//		List<Node<T, V>> toPutBack = new ArrayList<>();
//		while (!open.isEmpty() && nodes.size() <= 100000) {
//			if (i % helpers == 0)
//				nodes.add(open.poll());
//			else
//				toPutBack.add(open.poll());
//			i++;
//		}

		/* return all the not used paths to open */
//		open.addAll(toPutBack);
//		logger.info("Distributing " + nodes.size() + " of " + max);
		nodes.add(open.poll());
		return nodes;
	}

	@Override
	public void processResult(Collection<Node<T, V>> job, DistributedComputationResult<T, V> result) {

		logger.info("Processing result ...");

		/* mark the nodes that was outsourced to this coworker as closed and update status */
		for (Node<T, V> node : job) {
			getEventBus().post(new NodeTypeSwitchEvent<Node<T, V>>(node, "or_closed"));
		}

		/* if neither a solution was found nor we add any nodes to open, report that no change was made to the node set */
		if (result.getOpen().isEmpty() && result.getSolutions().isEmpty())
			return;

		/* append open nodes */
		for (List<Node<T, V>> p : result.getOpen()) {
			insertPathIntoLocalGraph(p);
			open.add(getLocalVersionOfNode(p.get(p.size() - 1)));
		}
		logger.info("Added {} nodes to open (and a respective number was added in order to make them reachable).", result.getOpen().size());

		/* create solution graphs */
		for (List<Node<T, V>> solution : result.getSolutions()) {
			insertPathIntoLocalGraph(solution);
		}
	}

	private void insertPathIntoLocalGraph(List<Node<T, V>> path) {
		Node<T, V> localVersionOfParent = null;
		Node<T, V> leaf = path.get(path.size() - 1);
		for (Node<T, V> node : path) {
			if (!ext2int.containsKey(node.getPoint())) {
				assert node.getParent() != null : "Want to insert a new node that has no parent. That must not be the case! Affected node is: " + node.getPoint();
				assert ext2int.containsKey(node.getParent().getPoint()) : "Want to insert a node whose parent is unknown locally";
				Node<T, V> newNode = newNode(localVersionOfParent, node.getPoint(), node.getInternalLabel());
				logger.info("Created new node {} as a local copy of {}", newNode, node);
				if (!newNode.isGoal() && !newNode.getPoint().equals(leaf.getPoint()))
					this.getEventBus().post(new NodeTypeSwitchEvent<Node<T, V>>(newNode, "or_closed"));
				localVersionOfParent = newNode;
			} else
				localVersionOfParent = getLocalVersionOfNode(node);
		}
	}
}
