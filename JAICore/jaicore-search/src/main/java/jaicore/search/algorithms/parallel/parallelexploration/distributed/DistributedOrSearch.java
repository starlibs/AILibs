//package jaicore.search.algorithms.parallel.parallelexploration.distributed;
//
//import java.util.ArrayList;
//import java.util.Collection;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.google.common.eventbus.Subscribe;
//
//import jaicore.graphvisualizer.events.graphEvents.NodeTypeSwitchEvent;
//import jaicore.search.algorithms.parallel.parallelexploration.distributed.events.NodePassedToCoworkerEvent;
//import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.DistributedSearchCommunicationLayer;
//import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.DistributionSearchAdapter;
//import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableGraphGenerator;
//import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableNodeEvaluator;
//import jaicore.search.algorithms.standard.bestfirst.BestFirst;
//import jaicore.search.algorithms.standard.bestfirst.model.Node;
//
//public class DistributedOrSearch<T, A, V extends Comparable<V>> extends BestFirst<T, A, V> implements DistributionSearchAdapter<T, V> {
//
//	private static final Logger logger = LoggerFactory.getLogger(DistributedOrSearch.class);
//	private final DistributedSearchManager<T, A, V> manager;
//
//	public DistributedOrSearch(SerializableGraphGenerator<T, A> graphGenerator, SerializableNodeEvaluator<T, V> pNodeEvaluator,
//			DistributedSearchCommunicationLayer<T, A, V> communicationLayer) {
//		super(graphGenerator, pNodeEvaluator);
//		
//		/* initialize communication layer */
//		try {
//			communicationLayer.init();
//			communicationLayer.setGraphGenerator(graphGenerator);
//			communicationLayer.setNodeEvaluator(pNodeEvaluator);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		/* now start distributed manager */
//		this.manager = new DistributedSearchManager<>(communicationLayer, this);
//		this.manager.getEventBus().register(this);
//	}
//	
//	@Override
//	protected boolean beforeSelection() {
//		if (!super.beforeSelection())
//			return false;
//		if (this.manager.getNumberOfHelpers() == this.manager.getNumbetOfIdleCoworkers()) {
//			logger.info("There are no (busy) coworkers, continue exploring on my own.");
//			return true;
//		}
//		logger.info("No further local exploration since there are sufficient busy coworkers ...");
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		return false;
//	}
//	
//	@Subscribe
//	public void receiveNodeEvent(NodePassedToCoworkerEvent<Node<T,V>> e) {
//		this.graphEventBus.post(new NodeTypeSwitchEvent<>(e.getNode(), "or_distributed"));
//	}
//	
//	@Override
//	public Collection<Node<T,V>> nextJob() {
//		if (open.size() > 1) {
////			int helpers = manager.getNumbetOfIdleCoworkers() + manager.getNumbetOfPendingCoworkers();
////			int pendingTasks = manager.getNumberOfUnprocessedJobs();
////			int nodesToOutsource = Math.min(open.size() -1, helpers - pendingTasks);
////			logger.info("Finished node expansion, distributing min({} - 1, {} - {}) = {} nodes ...", open.size() - 1, helpers, pendingTasks, nodesToOutsource);
//			Collection<Node<T,V>> nextJob = pollNodesForDistribution(1);
//			logger.info("Passing next job with {} node(s) to the DistributedSearchManager.", nextJob.size());
//			return nextJob;
//		}
//		return null;
//	}
//
//	@Override
//	protected boolean terminates() {
//		if (!super.terminates())
//			return false;
//		if (manager.isBusy())
//			return false;
//		return true;
//	}
//
//	public void cancel() {
//		super.cancel();
//		manager.shutdown();
//	}
//
//	private Collection<Node<T, V>> pollNodesForDistribution(int helpers) {
//		Collection<Node<T, V>> nodes = new ArrayList<>();
//		if (open.isEmpty())
//			return null;
////		int i = 0;
////		max = open.size();
////		List<Node<T, V>> toPutBack = new ArrayList<>();
////		while (!open.isEmpty() && nodes.size() <= 100000) {
////			if (i % helpers == 0)
////				nodes.add(open.poll());
////			else
////				toPutBack.add(open.poll());
////			i++;
////		}
//
//		/* return all the not used paths to open */
////		open.addAll(toPutBack);
////		logger.info("Distributing " + nodes.size() + " of " + max);
//		nodes.add(open.peek());
//		open.remove(open.peek());
//		return nodes;
//	}
//
//	@Override
//	public void processResult(Collection<Node<T, V>> job, DistributedComputationResult<T, V> result) {
//
//		logger.info("Processing result ...");
//
//		/* mark the nodes that was outsourced to this coworker as closed and update status */
//		for (Node<T, V> node : job) {
//			getEventBus().post(new NodeTypeSwitchEvent<Node<T, V>>(node, "or_closed"));
//		}
//
//		/* if neither a solution was found nor we add any nodes to open, report that no change was made to the node set */
//		if (result.getOpen().isEmpty() && result.getSolutions().isEmpty())
//			return;
//
//		/* special hints if */
//		if (result.getOpen().isEmpty()) {
//			logger.warn("No OPEN nodes were returned in this result. This produces a dead end node!");
//		}
//		else {
//			/* append open nodes */
//			for (Node<T, V> p : result.getOpen()) {
//				insertNodeIntoLocalGraph(p);
//				open.add(getLocalVersionOfNode(p));
//			}
//			logger.info("Added {} nodes to open (and a respective number was added in order to make them reachable).", result.getOpen().size());
//		}
//
//		/* create solution graphs */
//		for (Node<T, V> solution : result.getSolutions()) {
//			insertNodeIntoLocalGraph(solution); // they will be automatically added to the set of solutions by this
//		}
//	}
//}
