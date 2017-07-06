package jaicore.search.algorithms.parallel.parallelevaluation.local.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import jaicore.search.algorithms.standard.core.NodeEvaluator;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;

public class ParallelizedORGraphSearch<T, A, V extends Comparable<V>> extends ORGraphSearch<T, A, V> {

	private final int NUM_THREADS = 4;
	private final Semaphore fComputationTickets = new Semaphore(NUM_THREADS);
	private final ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);
	private final AtomicInteger activeJobs = new AtomicInteger(0);

	public ParallelizedORGraphSearch(GraphGenerator<T, A> graphGenerator, NodeEvaluator<T, V> pNodeEvaluator) {
		super(graphGenerator, pNodeEvaluator);
	}

	protected boolean terminates() {
		if (activeJobs.get() > 0)
			return false;
		return super.terminates();
	}

	protected boolean beforeInsertionIntoOpen(Node<T, V> node) {

		try {
			fComputationTickets.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		activeJobs.incrementAndGet();
		pool.submit(new Runnable() {

			@Override
			public void run() {
				labelNode(node);
				open.add(node);
				activeJobs.decrementAndGet();
				fComputationTickets.release();
			}
		});
		return false;
	}
}
