package ai.libs.jaicore.ml.core.dataset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.ml.WekaUtil;
import ai.libs.jaicore.ml.cache.InstructionFailedException;
import ai.libs.jaicore.ml.cache.InstructionGraph;
import ai.libs.jaicore.ml.cache.InstructionNode;
import ai.libs.jaicore.ml.cache.ReproducibleInstances;
import ai.libs.jaicore.ml.weka.dataset.splitter.SplitFailedException;

public class ReproducibilityTest {

	private static Logger logger = LoggerFactory.getLogger(ReproducibilityTest.class);

	@Test
	public void testLoadingARFF() throws InstructionFailedException, InterruptedException {
		ReproducibleInstances instances = ReproducibleInstances.fromARFF(new File("testrsc/ml/orig/letter.arff"));
	}

	@Test
	public void testLoadingOpenML() throws InstructionFailedException, InterruptedException {
		ReproducibleInstances instances = ReproducibleInstances.fromOpenML(3, "");
	}

	@Test
	public void testStratifiedSplit() throws InstructionFailedException, InterruptedException, SplitFailedException {
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
		assertEquals((int)Math.ceil(n * 0.7), outerSplit.get(0).size());
		assertEquals((int)Math.floor(n * 0.3), outerSplit.get(1).size());

		innerSplit.get(0).getInstructions().forEach(i -> logger.info("Commands: {}({})", i.getName(), i.getInstruction()));
		logger.info("First fold has {}/{} instances", innerSplit.get(0).size(), instances.size());
	}

}
