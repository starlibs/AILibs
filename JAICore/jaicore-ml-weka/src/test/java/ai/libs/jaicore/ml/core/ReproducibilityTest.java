package ai.libs.jaicore.ml.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.exception.DatasetTraceInstructionFailedException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.ml.core.dataset.cache.InstructionGraph;
import ai.libs.jaicore.ml.core.dataset.cache.InstructionNode;
import ai.libs.jaicore.ml.weka.WekaUtil;
import ai.libs.jaicore.ml.weka.dataset.ReproducibleInstances;

public class ReproducibilityTest {

	private static Logger logger = LoggerFactory.getLogger(ReproducibilityTest.class);

	@Test
	public void testLoadingARFF() throws DatasetTraceInstructionFailedException, InterruptedException {
		ReproducibleInstances instances = ReproducibleInstances.fromARFF(new File("testrsc/ml/orig/letter.arff"));
		assertNotNull(instances);
		assertEquals(20000, instances.size());
		assertEquals(26, instances.numClasses());
	}

	@Test
	public void testLoadingOpenML() throws DatasetTraceInstructionFailedException, InterruptedException {
		ReproducibleInstances instances = ReproducibleInstances.fromOpenML(6, "");
		assertNotNull(instances);
		assertEquals(20000, instances.size());
		assertEquals(26, instances.numClasses());
	}

	@Test
	public void testStratifiedSplit() throws DatasetTraceInstructionFailedException, InterruptedException, SplitFailedException {
		ReproducibleInstances instances = ReproducibleInstances.fromOpenML(3, "");
		int n = instances.size();
		List<ReproducibleInstances> outerSplit = WekaUtil.getStratifiedSplit(instances, 5, 0.7);
		List<ReproducibleInstances> innerSplit = WekaUtil.getStratifiedSplit(outerSplit.get(1), 3, 0.7);

		/* check history, size and disjointness of outer split */
		InstructionGraph historyOfFirstInOuter = outerSplit.get(0).getInstructions();
		InstructionGraph historyOfSecondInOuter = outerSplit.get(0).getInstructions();
		InstructionNode lastNodeOfFirstInOuter = historyOfFirstInOuter.get(historyOfFirstInOuter.size() - 1);
		assertEquals("stratified split", lastNodeOfFirstInOuter.getName());
		assertEquals(new Pair<>("stratified split", 0), outerSplit.get(0).getOutputUnit());
		assertEquals(new Pair<>("stratified split", 1), outerSplit.get(1).getOutputUnit());
		assertTrue(n >= outerSplit.get(0).size());
		assertTrue(n >= outerSplit.get(1).size());
		assertEquals(n, outerSplit.get(0).size() + outerSplit.get(1).size());
		assertEquals((int) Math.ceil(n * 0.7), outerSplit.get(0).size());
		assertEquals((int) Math.floor(n * 0.3), outerSplit.get(1).size());

		innerSplit.get(0).getInstructions().forEach(i -> logger.info("Commands: {}({})", i.getName(), i.getInstruction()));
		logger.info("First fold has {}/{} instances", innerSplit.get(0).size(), instances.size());
	}

}
