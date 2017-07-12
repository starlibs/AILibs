package jaicore.search.algorithms.parallel.parallelevaluation.local.core;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import jaicore.search.algorithms.standard.core.NodeEvaluator;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.events.NodeTypeSwitchEvent;

public class ParallelizedORGraphSearch<T, A, V extends Comparable<V>> extends ORGraphSearch<T, A, V> {

	private final Semaphore fComputationTickets;
	private final ExecutorService pool;
	private final AtomicInteger activeJobs = new AtomicInteger(0);
	private final Timer timeouter = new Timer();
	private final ITimeoutNodeEvaluator<T, V> timeoutNodeEvaluator;
	private final int timeout;

	public ParallelizedORGraphSearch(GraphGenerator<T, A> graphGenerator, NodeEvaluator<T, V> pNodeEvaluator, int numThreads, int timeout) {
		this(graphGenerator, pNodeEvaluator, numThreads, n -> null, timeout);
	}

	public ParallelizedORGraphSearch(GraphGenerator<T, A> graphGenerator, NodeEvaluator<T, V> pNodeEvaluator, int numThreads, ITimeoutNodeEvaluator<T, V> timeouter, int timeout) {
		super(graphGenerator, pNodeEvaluator);
		this.timeoutNodeEvaluator = timeouter;
		this.fComputationTickets = new Semaphore(numThreads);
		this.pool = Executors.newFixedThreadPool(numThreads);
		this.timeout = timeout;
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
		final Future<?> job = pool.submit(new Runnable() {

			@Override
			public void run() {

				/* compute f-value for node; possibly use the escape value from the timeout node evaluator */
				V label = null;
				try {
					label = nodeEvaluator.f(node);
				} catch (InterruptedException e) {
					eventBus.post(new NodeTypeSwitchEvent<>(node, "or_timedout"));
					label = timeoutNodeEvaluator.f(node);
				} catch (Exception e) {
					eventBus.post(new NodeTypeSwitchEvent<>(node, "or_ffail"));
					e.printStackTrace();
				}

				/* only if we have a non-null label, update the node label and insert the node into open */
				if (label != null) {
					node.setInternalLabel(label);
					open.add(node);
				}

				/* in any case, free the resources to compute f-values */
				activeJobs.decrementAndGet();
				fComputationTickets.release();
			}
		});

		/* set timeout for the job */
		TimerTask t = new TimerTask() {

			@Override
			public void run() {
				if (!job.isDone()) {
					job.cancel(true);
				}
			}
		};
		timeouter.schedule(t, timeout);
		return false;
	}
}
