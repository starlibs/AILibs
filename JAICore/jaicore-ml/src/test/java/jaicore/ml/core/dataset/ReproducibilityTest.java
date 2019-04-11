package jaicore.ml.core.dataset;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import jaicore.ml.WekaUtil;
import jaicore.ml.cache.ReproducibleInstances;

public class ReproducibilityTest {

	@Test
	public void test() throws IOException {
		ReproducibleInstances instances = ReproducibleInstances.fromARFF("testrsc/ml/orig/letter.arff");
		List<ReproducibleInstances> outerSplit = WekaUtil.getStratifiedSplit(instances, 5, 0.7);
		List<ReproducibleInstances> innerSplit = WekaUtil.getStratifiedSplit(outerSplit.get(1), 3, 0.7);

		innerSplit.get(0).getInstructions().forEach(i -> System.out.println(i.getCommand() + i.getInputs()));
		System.out.println(innerSplit.get(0).size() + "/" + instances.size());
	}

}
