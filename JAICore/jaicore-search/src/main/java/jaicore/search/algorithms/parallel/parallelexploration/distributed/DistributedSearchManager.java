package jaicore.search.algorithms.parallel.parallelexploration.distributed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import jaicore.basic.sets.SetUtil;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.events.NodePassedToCoworkerEvent;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.DistributedSearchCommunicationLayer;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.DistributionSearchAdapter;
import jaicore.search.model.travesaltree.Node;

public class DistributedSearchManager<T, A, V extends Comparable<V>> {
	private static final Logger logger = LoggerFactory.getLogger(DistributedSearchManager.class);
	private final DistributedSearchCommunicationLayer<T, A, V> communicationLayer;
	private final DistributionSearchAdapter<T, V> distAdapter;
//	private final BlockingQueue<Collection<Node<T, V>>> unprocessedJobs = new LinkedBlockingQueue<>();
	private final BlockingQueue<String> idleCoworkers = new LinkedBlockingQueue<>();
	private final Set<String> pendingCoworkers = Collections.synchronizedSet(new HashSet<>());
	private final Map<String, Collection<Node<T, V>>> coworkerJobs = Collections.synchronizedMap(new HashMap<>());
	private final EventBus eventBus = new EventBus();
	private final List<Thread> auxThreads = new ArrayList<>();
	
	public EventBus getEventBus() {
		return eventBus;
	}

	private class CoworkerSynchronizerWorker extends Thread {
		public void run() {
			try {
				while (!Thread.interrupted()) {
					logger.info("Scanning for new/removed coworkers ...");
					detectNewCoworkers();
					detectUnattachedCoworkers();
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) { }
		}
	}

	private class JobWriterWorker extends Thread {

		public void run() {

			try {
				while (!Thread.interrupted()) {
					Collection<Node<T, V>> nodes = null;
					
					/* get next nodes */
					while ((nodes = distAdapter.nextJob())==null)
						Thread.sleep(1000);
					
					/* now create jobs for all pending coworkers */
					logger.info("Waiting for the next available coworker ...");
					String coworker = idleCoworkers.take();
					pendingCoworkers.add(coworker);
					
					logger.info("Assigning next job to {}", coworker);
					pendingCoworkers.remove(coworker);
					coworkerJobs.put(coworker, nodes);
					idleCoworkers.remove(coworker);
					communicationLayer.createNewJobForCoworker(coworker, nodes);
					for (Node<T,V> node : nodes)
						eventBus.post(new NodePassedToCoworkerEvent<>(node));
				}
			} catch (InterruptedException e) { }
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
							distAdapter.processResult(coworkerJobs.get(busyCoworker), result);
							coworkerJobs.remove(busyCoworker);
							idleCoworkers.add(busyCoworker);
						}
					}

					/* wait some time to see whether new results are there */
					Thread.sleep(500);
				}
			} catch (InterruptedException e) { }
		}
	}

	public DistributedSearchManager(DistributedSearchCommunicationLayer<T, A, V> communicationLayer, DistributionSearchAdapter<T, V> resultProcessor) {
		super();

		/* sanity check */
		if (communicationLayer == null)
			throw new IllegalArgumentException("communication layer must not be null!");
		if (resultProcessor == null)
			throw new IllegalArgumentException("result processor must not be null!");

		/* set up communication layer and run the workers */
		this.communicationLayer = communicationLayer;
		this.distAdapter = resultProcessor;
		auxThreads.add(new CoworkerSynchronizerWorker());
		auxThreads.add(new JobWriterWorker());
		auxThreads.add(new ResultReaderWorker());
		for (Thread t : auxThreads)
			t.start();
	}

//	public void distributeNodesRemotely(Collection<Node<T, V>> nodes) {
//
//		/* sanity check */
//		if (nodes == null)
//			throw new IllegalArgumentException("node collection to be distributed is NULL.");
//		else if (nodes.isEmpty())
//			throw new IllegalArgumentException("node collection to be distributed is empty.");
//
//		/* inject job */
//		try {
//			logger.info("Adding job to unprocessed jobs ...");
//			unprocessedJobs.put(nodes);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//	}

	private void detectNewCoworkers() {
		for (String newCoworker : communicationLayer.detectNewCoworkers()) {
			logger.info("Detected new coworker {}. Now trying to attach it ...", newCoworker);
			communicationLayer.attachCoworker(newCoworker);
			logger.info("Attached new coworker {}", newCoworker);
			idleCoworkers.add(newCoworker);
		}
	}
	
	private void detectUnattachedCoworkers() {
		
		/* check unattached idle coworkers */
		for (String coworker : new ArrayList<>(idleCoworkers)) {
			if (!communicationLayer.isAttached(coworker)) {
				logger.info("Coworker {} was detached!", coworker);
				idleCoworkers.remove(coworker);
			}
		}
		
		/* check unattached coworkers that were busy before */
		for (String coworker : new ArrayList<>(coworkerJobs.keySet())) {
			if (!communicationLayer.isAttached(coworker)) {
				Collection<Node<T,V>> job = coworkerJobs.get(coworker);
				coworkerJobs.remove(coworker);
//				distributeNodesRemotely(job);
				logger.warn("Busy coworker {} was detached. Resubmitting his job {}.", coworker, job);
			}
		}
	}

	public int getNumberOfHelpers() {
		return idleCoworkers.size() + pendingCoworkers.size() + coworkerJobs.size();
	}
	
	public int getNumbetOfIdleCoworkers() {
		return idleCoworkers.size();
	}
	
	public int getNumbetOfPendingCoworkers() {
		return pendingCoworkers.size();
	}

//	public int getNumberOfUnprocessedJobs() {
//		return unprocessedJobs.size();
//	}
	
	public boolean isBusy() {
		return !coworkerJobs.isEmpty();
	}

	public void shutdown() {
		for (String coworker : SetUtil.union(idleCoworkers, coworkerJobs.keySet())) {
			coworkerJobs.remove(coworker);
			communicationLayer.detachCoworker(coworker);
		}
		
		for (Thread t : auxThreads) {
			logger.info("Shutting down {}.", t);
			t.interrupt();
			try {
				t.join();
				logger.info("Shutdown of {} complete.", t);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
