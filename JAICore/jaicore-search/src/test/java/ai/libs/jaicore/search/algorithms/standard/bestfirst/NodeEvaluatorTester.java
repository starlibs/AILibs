package ai.libs.jaicore.search.algorithms.standard.bestfirst;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.interrupt.Interrupter;
import ai.libs.jaicore.interrupt.InterruptionTimerTask;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSP;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSPGenerator;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSPState;
import ai.libs.jaicore.problems.enhancedttsp.locationgenerator.RandomLocationGenerator;
import ai.libs.jaicore.search.exampleproblems.enhancedttsp.EnhancedTTSPSimpleGraphGenerator;
import ai.libs.jaicore.search.exampleproblems.enhancedttsp.EnhancedTTSPSimpleSolutionPredicate;
import ai.libs.jaicore.search.model.travesaltree.BackPointerPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import ai.libs.jaicore.test.MediumTest;

public abstract class NodeEvaluatorTester<N extends IPathEvaluator<EnhancedTTSPState, String, Double>> {

	private static final Logger logger = LoggerFactory.getLogger(NodeEvaluatorTester.class);
	private static final int INTERRUPT_TRIGGER = 3000;
	private static final int INTERRUPT_TOLERANCE = 100;

	public abstract N getNodeEvaluator();

	public abstract N getBusyNodeEvaluator();

	public abstract Collection<BackPointerPath<EnhancedTTSPState, String, Double>> getNodesToTestInDifficultProblem(int numNodes);

	@Test
	@MediumTest
	public void testInterruptibility() throws InterruptedException, AlgorithmException, PathEvaluationException {
		for (BackPointerPath<EnhancedTTSPState, String, Double> node : this.getNodesToTestInDifficultProblem(1)) {

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
				Double score = ne.evaluate(node);
				fail("Obtained score " + score + " instead of interrupt. Either the node evaluation has caught and suppressed the InterruptedException, or the evaluation only took " + (System.currentTimeMillis() - start)
						+ "ms, which was not enough to trigger the interrupt.");
			} catch (InterruptedException e) {
				if (Interrupter.get().hasCurrentThreadBeenInterruptedWithReason(task)) {
					long runtime = System.currentTimeMillis() - start;
					assertTrue("The interrupt took " + (runtime - INTERRUPT_TRIGGER) + "ms to be processed.", runtime < INTERRUPT_TRIGGER + INTERRUPT_TOLERANCE);
					logger.info("Interruption registered. Runtime was {}ms", runtime);
					Interrupter.get().markInterruptOnCurrentThreadAsResolved(task);
				} else {
					throw e;
				}
			}
			t.cancel();
		}
	}

	public StandardBestFirst<EnhancedTTSPState, String, Double> getBF(final int problemSize, final IPathEvaluator<EnhancedTTSPState, String, Double> ne) {
		EnhancedTTSP problem = new EnhancedTTSPGenerator(new RandomLocationGenerator(new Random(0))).generate(problemSize, 100, 0);
		GraphSearchWithSubpathEvaluationsInput<EnhancedTTSPState, String, Double> input = new GraphSearchWithSubpathEvaluationsInput<>(new EnhancedTTSPSimpleGraphGenerator(problem), new EnhancedTTSPSimpleSolutionPredicate(problem), ne); // there will
		// be 10
		// solutions
		StandardBestFirst<EnhancedTTSPState, String, Double> bf = new StandardBestFirst<>(input);
		bf.setNumCPUs(1);
		return bf;
	}
}
