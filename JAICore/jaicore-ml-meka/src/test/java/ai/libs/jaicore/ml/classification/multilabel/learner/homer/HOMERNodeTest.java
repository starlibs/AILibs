package ai.libs.jaicore.ml.classification.multilabel.learner.homer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import org.junit.BeforeClass;
import org.junit.Test;

import ai.libs.jaicore.ml.weka.WekaUtil;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstances;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import meka.core.MLUtils;
import weka.core.Instance;
import weka.core.Instances;

public class HOMERNodeTest {

	private static HOMERNode node;

	@BeforeClass
	public static void setup() {
		HOMERLeaf l1 = new HOMERLeaf(1);
		HOMERLeaf l2 = new HOMERLeaf(2);
		HOMERLeaf l5 = new HOMERLeaf(5);
		HOMERLeaf l7 = new HOMERLeaf(7);
		node = new HOMERNode(new HOMERNode(l1, l2), l5, l7);
	}

	@Test
	public void testToString() {
		assertEquals("toString is not giving the correct result.", "J48(J48(1,2),5,7)", node.toString());
	}

	@Test
	public void testGetLabels() {
		assertTrue("Node does not contain all labels", node.getLabels().containsAll(Arrays.asList(1, 2, 5)));
	}

	@Test
	public void testFitting() throws Exception {
		Instances data = new Instances(new FileReader(new File("../../../datasets/classification/multi-label/flags.arff")));
		MLUtils.prepareData(data);

		List<IWekaInstances> split = WekaUtil.realizeSplit(new WekaInstances(data), WekaUtil.getArbitrarySplit(new WekaInstances(data), new Random(42), .7));
		node.buildClassifier(split.get(0).getInstances());

		boolean predictionsWorked = false;
		for (Instance i : split.get(1).getInstances()) {
			double[] dist = node.distributionForInstance(i);
			System.out.println(Arrays.toString(dist) + " " + Arrays.toString(this.extractLabels(i)));
			predictionsWorked = true;
		}

		assertTrue("Could not make any predictions.", !predictionsWorked);
	}

	public double[] extractLabels(final Instance i) {
		return IntStream.range(0, i.classIndex()).mapToDouble(x -> i.value(x)).toArray();
	}

}
