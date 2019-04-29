package jaicore.search.algorithms.standard.bestfirst;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.TimeAwareNodeEvaluator;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.testproblems.nqueens.QueenNode;
import jaicore.timing.TimedComputation;

public abstract class TimeAwareNodeEvaluatorTester<T extends TimeAwareNodeEvaluator<QueenNode, Double>> extends NodeEvaluatorTester<T> {

	private static final Logger logger = LoggerFactory.getLogger(TimeAwareNodeEvaluatorTester.class);

	private static final int TIMEOUT = 3000;
	private static final int TOLERANCE = 50;

	public abstract T getTimedNodeEvaluator(int timeoutInMS);

	@Test
	public void testTimeoutAdherence() throws InterruptedException, ExecutionException, TimeoutException {
		T ne = this.getTimedNodeEvaluator(TIMEOUT);
		ne.setLoggerName("testednodeevaluator");
		for (Node<QueenNode, Double> node : this.getNodesToTest(ne)) {
			long start = System.currentTimeMillis();
			logger.info("Starting computation of score for node with hash code {} with timeout {}", node.hashCode(), TIMEOUT);
			TimedComputation.compute(() -> ne.f(node), (long) TIMEOUT + TOLERANCE, "Timeout Test");
			logger.info("Finished computation. Runtime was {}ms", System.currentTimeMillis() - start);
		}
	}
}
