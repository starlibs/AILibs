package jaicore.search.algorithms.standard.bestfirst;

import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.concurrent.InterruptionTimerTask;
import jaicore.interrupt.Interrupt;
import jaicore.interrupt.Interrupter;
import jaicore.search.algorithms.standard.bestfirst.exceptions.NodeEvaluationException;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.TimeAwareNodeEvaluator;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.testproblems.nqueens.QueenNode;

public abstract class TimeAwareNodeEvaluatorTester<T extends TimeAwareNodeEvaluator<QueenNode, Double>> extends NodeEvaluatorTester<T> {

	private static final Logger logger = LoggerFactory.getLogger(TimeAwareNodeEvaluatorTester.class);

	private static final int TIMEOUT = 3000;
	private static final int TOLERANCE = 50;

	public abstract T getTimedNodeEvaluator(int timeoutInMS);

	@Test
	public void testTimeoutAdherence() throws InterruptedException, NodeEvaluationException {

		T ne = this.getTimedNodeEvaluator(TIMEOUT);
		for (Node<QueenNode, Double> node : this.getNodesToTest(ne)) {
			Timer t = new Timer();
			TimerTask task = new InterruptionTimerTask("Interruptor", () -> logger.info("Interrupting busy evaluator"));
			t.schedule(task, (long)TIMEOUT + TOLERANCE);
			long start = System.currentTimeMillis();
			logger.info("Starting computation with timeout {}", TIMEOUT);
			try {
				ne.f(node);
				logger.info("Finished computation. Runtime was {}ms", System.currentTimeMillis() - start);
			} catch (InterruptedException e) {
				Optional<Interrupt> interrupt = Interrupter.get().getInterruptOfCurrentThreadWithReason(task);
				if (interrupt.isPresent()) {
					Interrupter.get().markInterruptOnCurrentThreadAsResolved(task);
				} else {
					throw e;
				}
			} finally {
				t.cancel();
			}
		}
	}
}
