package jaicore.search.algorithms.standard.bestfirst;

import static org.junit.Assert.assertTrue;

import java.util.Timer;

import org.junit.Test;

import jaicore.concurrent.InterruptionTimerTask;
import jaicore.search.algorithms.standard.bestfirst.exceptions.NodeEvaluationException;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.TimeAwareNodeEvaluator;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.testproblems.nqueens.QueenNode;

public abstract class TimeAwareNodeEvaluatorTester<T extends TimeAwareNodeEvaluator<QueenNode, Double>>
		extends NodeEvaluatorTester<T> {

	private static final int TIMEOUT = 3000;
	private static final int TOLERANCE = 50;

	public abstract T getTimedNodeEvaluator(int timeoutInMS);

	@Test
	public void testTimeoutAdherence() throws InterruptedException, NodeEvaluationException {

		T ne = getTimedNodeEvaluator(TIMEOUT);
		for (Node<QueenNode, Double> node : getNodesToTest(ne)) {
			Timer t = new Timer();
			t.schedule(
					new InterruptionTimerTask("Interruptor", () -> System.out.println("Interrupting busy evaluator")),
					TIMEOUT + TOLERANCE);
			long start = System.currentTimeMillis();
			System.out.println("Starting computation with timeout " + TIMEOUT);
			ne.f(node);
			System.out.println("Finished computation");
			long runtime = System.currentTimeMillis() - start;
			System.out.println("Interruption registered. Runtime was " + runtime + "ms");
			assertTrue("The interrupt took " + (runtime - TIMEOUT) + "ms to be processed.",
					runtime < TIMEOUT + TOLERANCE);
			t.cancel();
		}
	}
}
