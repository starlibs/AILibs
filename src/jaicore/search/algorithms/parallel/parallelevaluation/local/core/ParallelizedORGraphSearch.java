package jaicore.search.algorithms.parallel.parallelevaluation.local.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.concurrent.TimeoutTimer;
import jaicore.concurrent.TimeoutTimer.TimeoutSubmitter;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.events.NodeTypeSwitchEvent;

public class ParallelizedORGraphSearch<T, A, V extends Comparable<V>> extends ORGraphSearch<T, A, V> {

	private static final Logger logger = LoggerFactory.getLogger(ParallelizedORGraphSearch.class);
	private final Semaphore fComputationTickets;
	private final ExecutorService pool;
	private final AtomicInteger activeJobs = new AtomicInteger(0);
	private final ITimeoutNodeEvaluator<T, V> timeoutNodeEvaluator;
	private final TimeoutSubmitter timeoutSubmitter = TimeoutTimer.getInstance().getSubmitter();
	private final int timeout;

	public ParallelizedORGraphSearch(GraphGenerator<T, A> graphGenerator, INodeEvaluator<T, V> pNodeEvaluatorForNodeEvaluation, int numThreads, int timeout) {
		this(graphGenerator, pNodeEvaluatorForNodeEvaluation, numThreads, n -> null, timeout);
	}

	public ParallelizedORGraphSearch(GraphGenerator<T, A> graphGenerator, INodeEvaluator<T, V> pNodeEvaluator, int numThreadsForNodeEvaluation,
			ITimeoutNodeEvaluator<T, V> timeouter, int timeoutInMS) {
		super(graphGenerator, pNodeEvaluator);
		this.timeoutNodeEvaluator = timeouter;
		if (numThreadsForNodeEvaluation < 1)
			throw new IllegalArgumentException("Number of threads should be at least 1 for " + this.getClass().getName());
		this.fComputationTickets = new Semaphore(numThreadsForNodeEvaluation);
		this.pool = Executors.newFixedThreadPool(numThreadsForNodeEvaluation, r -> {
			Thread t = new Thread(r);
			t.setName("ParallelizedORGraphSearch-worker");
			return t;
		});
		this.timeout = timeoutInMS;
	}

	protected boolean terminates() {
		if (activeJobs.get() > 0)
			return false;
		return super.terminates();
	}

	@Override
	public void cancel() {
		super.cancel();
		logger.info("shutting down OR search");
		synchronized (pool) {
			pool.shutdownNow();
		}
		fComputationTickets.release(1000);
		timeoutSubmitter.close();
		logger.info("Cancel accomplished.");
	}
	
	protected boolean labelNode(Node<T, V> node) {
		synchronized (pool) {
			if (pool.isShutdown())
				return false;
		}
		try {
			fComputationTickets.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		/* check once again just in case that the shutdown was accomplished when we just left */
		synchronized (pool) {
			if (pool.isShutdown())
				return false;
		}
		activeJobs.incrementAndGet();
		Runnable r = new Runnable() {

			@Override
			public void run() {
				
				if (interrupted)
					throw new IllegalStateException("Must not compute any f-values after service shutdown.");
				
				graphEventBus.post(new NodeTypeSwitchEvent<>(node, "or_computingF"));

				/* set timeout on thread that interrupts it after the timeout */
				int taskId = timeoutSubmitter.interruptMeAfterMS(timeout);
				
				/* compute f-value for node; possibly use the escape value from the timeout node evaluator */
				V label = null;
				try {
					label = nodeEvaluator.f(node);
				} catch (InterruptedException e) {
					graphEventBus.post(new NodeTypeSwitchEvent<>(node, "or_timedout"));
					label = timeoutNodeEvaluator.f(node);
				} catch (Exception e) {
					graphEventBus.post(new NodeTypeSwitchEvent<>(node, "or_ffail"));
					e.printStackTrace();
				}
				timeoutSubmitter.cancelTimeout(taskId);

				/* only if we have a non-null label, update the node label and insert the node into open */
				if (label != null) {
					node.setInternalLabel(label);
					
					/* since the label-computation returns false, ORSearchGraph will not insert it into OPEN. We must do this now */
					if (!node.isGoal()) {
						open.add(node);
						graphEventBus.post(new NodeTypeSwitchEvent<>(node, "or_open"));
					}
					else
						graphEventBus.post(new NodeTypeSwitchEvent<>(node, "or_solution"));
				}

				/* in any case, free the resources to compute f-values */
				activeJobs.decrementAndGet();
				fComputationTickets.release();
			}
		};
		synchronized (pool) {
			pool.submit(r);
		}
		return false;
	}
}
