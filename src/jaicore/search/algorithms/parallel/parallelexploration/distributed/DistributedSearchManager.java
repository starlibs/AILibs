package jaicore.search.algorithms.parallel.parallelexploration.distributed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import jaicore.basic.SetUtil;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.events.NodePassedToCoworkerEvent;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.DistributedSearchCommunicationLayer;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.DistributionSearchResultProcessor;
import jaicore.search.structure.core.Node;

public class DistributedSearchManager<T, A, V extends Comparable<V>> {
	private static final Logger logger = LoggerFactory.getLogger(DistributedSearchManager.class);
	private final DistributedSearchCommunicationLayer<T, A, V> communicationLayer;
	private final DistributionSearchResultProcessor<T, V> resultProcessor;
	private final BlockingQueue<Collection<Node<T, V>>> unprocessedJobs = new LinkedBlockingQueue<>();
	private final BlockingQueue<String> idleCoworkers = new LinkedBlockingQueue<>();
	private final Map<String, Collection<Node<T, V>>> coworkerJobs = new HashMap<>();
	private final EventBus eventBus = new EventBus();
	
	public EventBus getEventBus() {
		return eventBus;
	}

	private class CoworkerDetectorWorker extends Thread {
		public void run() {
			try {
				while (!Thread.interrupted()) {
					detectCoworkers();
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private class JobWriterWorker extends Thread {

		public void run() {

			try {
				while (!Thread.interrupted()) {
					Collection<Node<T, V>> nodes = null;

					logger.info("Waiting for new jobs ...");
					nodes = unprocessedJobs.take();

					/* now create jobs for all pending coworkers */
					logger.info("Registered new job. Waiting for the next available coworker ...");
					String coworker = idleCoworkers.take();
					coworkerJobs.put(coworker, nodes);
					idleCoworkers.remove(coworker);
					// for (Node<T, V> node : coworkerJobs.get(coworker)) {
					// assert node.getPoint().equals(fromString(toString((Serializable) node.getPoint()))) : "Objects of class " + node.getPoint().getClass().getName()
					// + " cannot be serialized and unserialized without losing equality. Check equals method of " + node.getPoint().getClass().getName();
					// assert ext2int.containsKey(node.getPoint()) : "Outsourced node that has not been registered: " + node.getPoint();
					// getEventBus().post(new NodeTypeSwitchEvent<Node<T, V>>(node, "or_distributed"));
					// }
					communicationLayer.createNewJobForCoworker(coworker, nodes);
					for (Node<T,V> node : nodes)
						eventBus.post(new NodePassedToCoworkerEvent<>(node));
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private class ResultReaderWorker extends Thread {

		public void run() {

			try {
				while (!Thread.interrupted()) {

					/* process results */
					for (String busyCoworker : new ArrayList<>(coworkerJobs.keySet())) {
						DistributedComputationResult<T, V> result;
						if ((result = communicationLayer.readResult(busyCoworker)) != null) {
							logger.info("Received result with {} open nodes and {} solution(s)", result.getOpen().size(), result.getSolutions().size());
							resultProcessor.processResult(coworkerJobs.get(busyCoworker), result);
							coworkerJobs.remove(busyCoworker);
							idleCoworkers.add(busyCoworker);
						}
					}

					/* wait some time to see whether new results are there */
					Thread.sleep(500);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public DistributedSearchManager(DistributedSearchCommunicationLayer<T, A, V> communicationLayer, DistributionSearchResultProcessor<T, V> resultProcessor) {
		super();

		/* sanity check */
		if (communicationLayer == null)
			throw new IllegalArgumentException("communication layer must not be null!");
		if (resultProcessor == null)
			throw new IllegalArgumentException("result processor must not be null!");

		/* set up communication layer and run the workers */
		this.communicationLayer = communicationLayer;
		this.resultProcessor = resultProcessor;
		new CoworkerDetectorWorker().start();
		new JobWriterWorker().start();
		new ResultReaderWorker().start();
	}

	public void distributeNodesRemotely(Collection<Node<T, V>> nodes) {

		/* sanity check */
		if (nodes == null)
			throw new IllegalArgumentException("node collection to be distributed is NULL.");
		else if (nodes.isEmpty())
			throw new IllegalArgumentException("node collection to be distributed is empty.");

		/* inject job */
		try {
			logger.info("Adding job to unprocessed jobs ...");
			unprocessedJobs.put(nodes);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void detectCoworkers() {
		for (String newCoworker : communicationLayer.detectNewCoworkers()) {
			communicationLayer.attachCoworker(newCoworker);
			logger.info("Attaching {}", newCoworker);
			idleCoworkers.add(newCoworker);
		}
	}

	protected int getNumberOfHelpers() {
		return idleCoworkers.size() + coworkerJobs.size();
	}

	/** Read the object from Base64 string. */

	public boolean isBusy() {
		return !coworkerJobs.isEmpty();
	}

	public void shutdown() {
		for (String coworker : SetUtil.union(idleCoworkers, coworkerJobs.keySet())) {
			coworkerJobs.remove(coworker);
			communicationLayer.detachCoworker(coworker);
		}
	}
}
