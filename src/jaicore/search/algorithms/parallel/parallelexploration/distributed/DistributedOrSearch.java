package jaicore.search.algorithms.parallel.parallelexploration.distributed;

import java.util.ArrayList;
import java.util.Collection;

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

public class DistributedOrSearch<T, A, V extends Comparable<V>> extends ORGraphSearch<T, A, V> implements DistributionSearchResultProcessor<T, V> {

	private static final Logger logger = LoggerFactory.getLogger(DistributedOrSearch.class);
	private final DistributedSearchManager<T, A, V> manager;

	public DistributedOrSearch(SerializableGraphGenerator<T, A> graphGenerator, SerializableNodeEvaluator<T, V> pNodeEvaluator,
			DistributedSearchCommunicationLayer<T, A, V> communicationLayer) {
		super(graphGenerator, pNodeEvaluator);
		
		/* initialize communication layer */
		try {
			communicationLayer.init();
			communicationLayer.setGraphGenerator(graphGenerator);
			communicationLayer.setNodeEvaluator(pNodeEvaluator);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/* now start distributed manager */
		this.manager = new DistributedSearchManager<>(communicationLayer, this);
		this.manager.getEventBus().register(this);
	}
	
	@Subscribe
	public void receiveNodeEvent(NodePassedToCoworkerEvent<Node<T,V>> e) {
		this.eventBus.post(new NodeTypeSwitchEvent<>(e.getNode(), "or_distributed"));
	}
	
	@Override
	public void afterExpansion(Node<T, V> node) {
		if (open.size() > 1) {
			int helpers = manager.getNumbetOfIdleCoworkers() + manager.getNumbetOfPendingCoworkers();
			int pendingTasks = manager.getNumberOfUnprocessedJobs();
			int nodesToOutsource = Math.min(open.size() -1, helpers - pendingTasks);
			logger.info("Finished node expansion, distributing min({} - 1, {} - {}) = {} nodes ...", open.size() - 1, helpers, pendingTasks, nodesToOutsource);
			for (int i = 0; i < nodesToOutsource; i++) {
				manager.distributeNodesRemotely(pollNodesForDistribution(nodesToOutsource));
			}
		}
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
		for (Node<T, V> p : result.getOpen()) {
			insertNodeIntoLocalGraph(p);
			open.add(getLocalVersionOfNode(p));
		}
		logger.info("Added {} nodes to open (and a respective number was added in order to make them reachable).", result.getOpen().size());

		/* create solution graphs */
		for (Node<T, V> solution : result.getSolutions()) {
			insertNodeIntoLocalGraph(solution);
			solutions.add(solution.externalPath());
		}
	}
}
