package ai.libs.jaicore.ml.classification.multilabel.learner.homer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

import ai.libs.jaicore.ml.classification.multilabel.dataset.IMekaInstances;
import ai.libs.jaicore.ml.classification.multilabel.dataset.MekaInstances;
import meka.core.MLUtils;
import weka.core.Instances;

public class HOMERNodeTest {

	private static HOMERNode node;

	@BeforeClass
	public static void setup() {
		HOMERLeaf l1 = new HOMERLeaf(1);
		HOMERLeaf l2 = new HOMERLeaf(2);
		HOMERLeaf l5 = new HOMERLeaf(5);
		node = new HOMERNode(new HOMERNode(l1, l2), l5);
	}

	@Test
	public void testToString() {
		assertEquals("toString is not giving the correct result.", "((1),(2)),(5)", node.toString());
	}

	@Test
	public void testGetLabels() {
		assertTrue("Node does not contain all labels", node.getLabels().containsAll(Arrays.asList(1, 2, 5)));
	}

	@Test
	public void testFitting() throws Exception {
		Instances data = new Instances(new FileReader(new File("../../datasets/classification/multi-label/flags.arff")));
		MLUtils.prepareData(data);
		IMekaInstances instances = new MekaInstances(data);

	}

}
