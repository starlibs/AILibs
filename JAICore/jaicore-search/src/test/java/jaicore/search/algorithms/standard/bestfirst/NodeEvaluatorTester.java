package jaicore.search.algorithms.standard.bestfirst;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.interrupt.Interrupter;
import jaicore.interrupt.InterruptionTimerTask;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import jaicore.search.testproblems.enhancedttsp.EnhancedTTSPGraphGenerator;
import jaicore.testproblems.enhancedttsp.EnhancedTTSPGenerator;
import jaicore.testproblems.enhancedttsp.EnhancedTTSPNode;

public abstract class NodeEvaluatorTester<N extends INodeEvaluator<EnhancedTTSPNode, Double>> {

	private static final Logger logger = LoggerFactory.getLogger(NodeEvaluatorTester.class);
	private static final int INTERRUPT_TRIGGER = 3000;
	private static final int INTERRUPT_TOLERANCE = 50;

	public abstract N getNodeEvaluator();

	public abstract N getBusyNodeEvaluator();

	public abstract Collection<Node<EnhancedTTSPNode, Double>> getNodesToTestInDifficultProblem(int numNodes);

	@Test
	public void testInterruptibility() throws InterruptedException, AlgorithmException {
		for (Node<EnhancedTTSPNode, Double> node : this.getNodesToTestInDifficultProblem(1)) {

			/* create a new node evaluator */
			N ne = this.getBusyNodeEvaluator();
			if (ne instanceof ILoggingCustomizable) {
				((ILoggingCustomizable) ne).setLoggerName("testednodeevaluator");
			}
			
			/* start the timed job */
			Timer t = new Timer();
			TimerTask task = new InterruptionTimerTask("Interrupting busy evaluator");
			t.schedule(task, INTERRUPT_TRIGGER);
			long start = System.currentTimeMillis();
			try {
				logger.info("Starting evaluation of root");
				Double score= ne.f(node);
				fail("Obtained score " + score + " instead of interrupt. Either the node evaluation has caught and suppressed the InterruptedException, or the evaluation only took " + (System.currentTimeMillis() - start) + "ms, which was not enough to trigger the interrupt.");
			} catch (InterruptedException e) {
				if (Interrupter.get().hasCurrentThreadBeenInterruptedWithReason(task)) {
					long runtime = System.currentTimeMillis() - start;
					assertTrue("The interrupt took " + (runtime - INTERRUPT_TRIGGER) + "ms to be processed.", runtime < INTERRUPT_TRIGGER + INTERRUPT_TOLERANCE);
					logger.info("Interruption registered. Runtime was {}ms", runtime);
				} else {
					throw e;
				}
			}
			t.cancel();
		}
	}

	public StandardBestFirst<EnhancedTTSPNode, String, Double> getBF(int problemSize, final INodeEvaluator<EnhancedTTSPNode, Double> ne) {
		GraphSearchWithSubpathEvaluationsInput<EnhancedTTSPNode, String, Double> input = new GraphSearchWithSubpathEvaluationsInput<>(new EnhancedTTSPGraphGenerator(new EnhancedTTSPGenerator().generate(problemSize, 100)), ne); // there will
																																																									// be 10
																																																									// solutions
		StandardBestFirst<EnhancedTTSPNode, String, Double> bf = new StandardBestFirst<>(input);
		bf.setNumCPUs(1);
		return bf;
	}
}
