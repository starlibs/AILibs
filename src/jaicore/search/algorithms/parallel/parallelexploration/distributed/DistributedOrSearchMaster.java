package jaicore.search.algorithms.parallel.parallelexploration.distributed;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.SetUtil;
import jaicore.search.algorithms.standard.core.NodeEvaluator;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.graphgenerator.events.NodeTypeSwitchEvent;

public class DistributedOrSearchMaster<T, A, V extends Comparable<V>> extends ORGraphSearch<T, A, V> {

	private static final Logger logger = LoggerFactory.getLogger(DistributedOrSearchMaster.class);
	private final DistributedSearchMaintainer<T, V> coworkerInterface;
	private final Set<String> pendingCoworkers = new HashSet<>();
	private final Set<Node<T,V>> nodesUnderExaminationInThisProcess = new HashSet<>();
	private final Map<String, Collection<Node<T, V>>> coworkerJobs = new HashMap<>();
	private final int numberOfThreads;
	private final ExecutorService threadPool;

	public DistributedOrSearchMaster(GraphGenerator<T, A> graphGenerator, NodeEvaluator<T, V> pNodeEvaluator, DistributedSearchMaintainer<T, V> coworkerInterface,
			int numberOfThreads) {
		super(graphGenerator, pNodeEvaluator);
		this.coworkerInterface = coworkerInterface;
		this.numberOfThreads = numberOfThreads;
		this.threadPool = (numberOfThreads > 0) ? Executors.newFixedThreadPool(numberOfThreads) : null;
	}
	
	@Override
	protected void afterInitialization() {
		for (int i = 0; i < numberOfThreads; i++) {
			threadPool.submit(new Runnable() {

				@Override
				public void run() {
					runMainLoop(false);
				}
			});
		}
	}

	public void beforeSelection() {
		attachCoworkersAndProcessResults();
	}
	
	public void afterSelection(Node<T,V> node) {
		nodesUnderExaminationInThisProcess.add(node);
	}

	public void afterExpansion(Node<T,V> node) {
		nodesUnderExaminationInThisProcess.remove(node);
		distributeNodesRemotely();
	};

	@Override
	protected boolean terminates() {
		if (!super.terminates())
			return false;
		return coworkerJobs.isEmpty() && nodesUnderExaminationInThisProcess.isEmpty();
	}

	public void afterTermination() {
		threadPool.shutdown();
	}

	public void cancel() {
		super.cancel();
		for (String coworker : SetUtil.union(pendingCoworkers, coworkerJobs.keySet())) {
			coworkerJobs.remove(coworker);
			coworkerInterface.detachCoworker(coworker);
		}
	}

	private void attachCoworkersAndProcessResults() {
		if (coworkerInterface == null)
			return;
		
		/* detect new coworkers */
		for (String newCoworker : coworkerInterface.detectNewCoworkers()) {
			coworkerInterface.attachCoworker(newCoworker);
			logger.info("Attaching {}", newCoworker);
			pendingCoworkers.add(newCoworker);
		}

		/* process results */
		for (String busyCoworker : new ArrayList<>(coworkerJobs.keySet())) {
			DistributedComputationResult<T, V> result;
			if ((result = coworkerInterface.readResult(busyCoworker)) != null) {

				logger.info("Received result with {} open nodes and {} solution(s)", result.getOpen().size(), result.getSolutions().size());

				/* mark the nodes that was outsourced to this coworker as closed and update status */
				for (Node<T, V> node : coworkerJobs.get(busyCoworker)) {
					getEventBus().post(new NodeTypeSwitchEvent<Node<T, V>>(node, "or_closed"));
				}
				coworkerJobs.remove(busyCoworker);
				pendingCoworkers.add(busyCoworker);

				/* if neither a solution was found nor we add any nodes to open, report that no change was made to the node set */
				if (result.getOpen().isEmpty() && result.getSolutions().isEmpty())
					continue;

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
		}
	}
	
	private void distributeNodesRemotely() {

		Collection<Node<T, V>> nodesToBeSolved;
		/* now create jobs for all pending coworkers */
		for (String coworker : new ArrayList<>(pendingCoworkers)) {
			if ((nodesToBeSolved = pollNodesForDistribution(getNumberOfHelpers())) != null) {
				coworkerJobs.put(coworker, nodesToBeSolved);
				pendingCoworkers.remove(coworker);
				for (Node<T, V> node : coworkerJobs.get(coworker)) {
					assert node.getPoint().equals(fromString(toString((Serializable) node.getPoint()))) : "Objects of class " + node.getPoint().getClass().getName()
					+ " cannot be serialized and unserialized without losing equality. Check equals method of " + node.getPoint().getClass().getName();
					assert ext2int.containsKey(node.getPoint()) : "Outsourced node that has not been registered: " + node.getPoint();
					getEventBus().post(new NodeTypeSwitchEvent<Node<T, V>>(node, "or_distributed"));
				}
				coworkerInterface.createNewJobForCoworker(coworker, nodesToBeSolved);
			}
		}
	}

	protected int getNumberOfHelpers() {
		return pendingCoworkers.size() + coworkerJobs.size() + numberOfThreads;
	}

	private Collection<Node<T, V>> pollNodesForDistribution(int helpers) {
		Collection<Node<T, V>> nodes = new ArrayList<>();
		int max;
		if (open.isEmpty())
			return null;
		int i = 0;
		max = open.size();
		List<Node<T, V>> toPutBack = new ArrayList<>();
		while (!open.isEmpty() && nodes.size() <= 100000) {
			if (i % helpers == 0)
				nodes.add(open.poll());
			else
				toPutBack.add(open.poll());
			i++;
		}

		/* return all the not used paths to open */
		open.addAll(toPutBack);
		logger.info("Distributing " + nodes.size() + " of " + max);
		return nodes;
	}

	/**
	 * @Return The number of newly inserted nodes
	 **/
	private void insertPathIntoLocalGraph(List<Node<T, V>> path) {
		Node<T, V> newNode = null;
		for (Node<T, V> node : path) {
			if (!ext2int.containsKey(node.getPoint())) {
				assert node.getParent() != null : "Want to insert a new node that has no parent. That must not be the case! Affected node is: " + node.getPoint();
				newNode = newNode(node.getParent(), node.getPoint(), node.getInternalLabel());
				logger.info("Created new node {} as a local copy of {}", newNode, node);
				if (!newNode.isGoal())
					this.getEventBus().post(new NodeTypeSwitchEvent<Node<T, V>>(newNode, "or_closed"));
			}
		}
	}
	
	private Node<T,V> getLocalVersionOfNode(Node<T,V> node) {
		return ext2int.get(node.getPoint());
	}

	/** Read the object from Base64 string. */
	private static Object fromString(String s) {
		try {
			byte[] data = Base64.getDecoder().decode(s);
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
			Object o = ois.readObject();
			ois.close();
			return o;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/** Write the object to a Base64 string. */
	private static String toString(Serializable o) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(o);
			oos.close();
			return Base64.getEncoder().encodeToString(baos.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
