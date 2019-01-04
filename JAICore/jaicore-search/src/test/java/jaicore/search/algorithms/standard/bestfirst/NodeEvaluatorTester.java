package jaicore.search.algorithms.standard.bestfirst;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Timer;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.concurrent.InterruptionTimerTask;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import jaicore.search.testproblems.nqueens.NQueenGenerator;
import jaicore.search.testproblems.nqueens.QueenNode;

public abstract class NodeEvaluatorTester<N extends INodeEvaluator<QueenNode, Double>> {

	private static final int INTERRUPT_TRIGGER = 3000;
	private static final int INTERRUPT_TOLERANCE = 50;

	public abstract N getNodeEvaluator();

	public abstract N getBusyNodeEvaluator();

	public abstract Collection<Node<QueenNode, Double>> getNodesToTest(N nodeEvaluator);

	@Test
	public void testInterruptibility()
			throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {

		N ne = getBusyNodeEvaluator();
		for (Node<QueenNode, Double> node : getNodesToTest(ne)) {

			Timer t = new Timer();
			t.schedule(new InterruptionTimerTask("Interrupting busy evaluator"), INTERRUPT_TRIGGER);
			long start = System.currentTimeMillis();
			try {
				System.out.println("Starting evaluation of root");
				ne.f(node);
				assert false : "Either the node evaluation has caught and suppressed the InterruptedException, or the evaluation only took "
						+ (System.currentTimeMillis() - start) + "ms, which was not enough to trigger the interrupt.";
			} catch (InterruptedException e) {
				long runtime = System.currentTimeMillis() - start;
				assertTrue("The interrupt took " + (runtime - INTERRUPT_TRIGGER) + "ms to be processed.",
						runtime < INTERRUPT_TRIGGER + INTERRUPT_TOLERANCE);
				System.out.println("Interruption registered. Runtime was " + runtime + "ms");
			}
			t.cancel();
		}
	}

	public StandardBestFirst<QueenNode, String, Double> getBF(INodeEvaluator<QueenNode, Double> ne) {
		GraphSearchWithSubpathEvaluationsInput<QueenNode, String, Double> input = new GraphSearchWithSubpathEvaluationsInput<>(
				new NQueenGenerator(5), ne); // there will be 10 solutions
		StandardBestFirst<QueenNode, String, Double> bf = new StandardBestFirst<>(input);
		bf.setNumCPUs(1);
		return bf;
	}
}
