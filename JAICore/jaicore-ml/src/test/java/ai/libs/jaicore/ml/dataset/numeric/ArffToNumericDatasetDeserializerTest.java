package ai.libs.jaicore.ml.dataset.numeric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.ml.dataset.IAttribute;

public class ArffToNumericDatasetDeserializerTest {

	private static final File D1_NUMERIC_ONLY_WO_CI_ARFF = new File("testrsc/dataset/arff/numeric_only_without_classindex.arff");
	private static final String D1_RELATION_NAME = "numeric-only-without-class-index-flag";
	private static final String[] D1_TRAIN_ATTRIBUTE_NAMES = { "att0", "att1" };
	private static final String[] D1_TEST_ATTRIBUTE_NAMES = { "class" };
	private static final double[][] D1_EXPECTED_X = { { 0.0, 1.0 }, { 0.4, 0.5 } };
	private static final double[][] D1_EXPECTED_Y = { { 2.0 }, { 0.5 } };

	private static final File D2_NUMERIC_ONLY_W_CI_ARFF = new File("testrsc/dataset/arff/numeric_only_with_classindex.arff");
	private static final String D2_RELATION_NAME = "numeric-only-with-class-index-flag";
	private static final String[] D2_TRAIN_ATTRIBUTE_NAMES = { "att0", "att1" };
	private static final String[] D2_TEST_ATTRIBUTE_NAMES = { "class" };
	private static final double[][] D2_EXPECTED_X = { { 1.0, 2.0 }, { 0.5, 0.5 } };
	private static final double[][] D2_EXPECTED_Y = { { 0.0 }, { 0.4 } };

	private static final File D3_NUMERIC_ONLY_W_CI_ARFF = new File("testrsc/dataset/arff/numeric_only_with_classindex.arff");
	private static final String D3_RELATION_NAME = "numeric-only-with-class-index-flag";
	private static final String[] D3_TRAIN_ATTRIBUTE_NAMES = { "att0", "att1" };
	private static final String[] D3_TEST_ATTRIBUTE_NAMES = { "class" };
	private static final double[][] D3_EXPECTED_X = { { 1.0, 2.0 }, { 0.5, 0.5 } };
	private static final double[][] D3_EXPECTED_Y = { { 0.0 }, { 0.4 } };

	private ArffToNumericDatasetDeserializer deserializer = new ArffToNumericDatasetDeserializer();

	@Test
	public void testDeserializationOfD1() {
		NumericDataset dataset = this.deserializer.deserializeDataset(D1_NUMERIC_ONLY_WO_CI_ARFF);
		assertEquals("The relation name is not as expected", D1_RELATION_NAME, dataset.getRelationName());
		this.checkCorrectInstances(dataset, D1_EXPECTED_X, D1_EXPECTED_Y, D1_TRAIN_ATTRIBUTE_NAMES, D1_TEST_ATTRIBUTE_NAMES);
	}

	@Test
	public void testDeserializationOfD2() {
		NumericDataset dataset = this.deserializer.deserializeDataset(D2_NUMERIC_ONLY_W_CI_ARFF);
		assertEquals("The relation name is not as expected", D2_RELATION_NAME, dataset.getRelationName());
		this.checkCorrectInstances(dataset, D2_EXPECTED_X, D2_EXPECTED_Y, D2_TRAIN_ATTRIBUTE_NAMES, D2_TEST_ATTRIBUTE_NAMES);
	}

	@Test
	public void testDeserializationOfD3() {
		NumericDataset dataset = this.deserializer.deserializeDataset(D3_NUMERIC_ONLY_W_CI_ARFF);
		assertEquals("The relation name is not as expected", D3_RELATION_NAME, dataset.getRelationName());
		this.checkCorrectInstances(dataset, D3_EXPECTED_X, D3_EXPECTED_Y, D3_TRAIN_ATTRIBUTE_NAMES, D3_TEST_ATTRIBUTE_NAMES);
	}

	private void checkCorrectInstances(final NumericDataset dataset, final double[][] expectedX, final double[][] expectedY, final String[] featureNames, final String[] targetNames) {
		this.checkAttributeNames("Instance attribute names do not match!", featureNames, dataset.getInstanceAttributes());
		this.checkAttributeNames("Target attribute names do not match!", targetNames, dataset.getTargetAttributes());
		for (int i = 0; i < dataset.size(); i++) {
			Pair<double[], double[]> instance = dataset.getInstance(i);
			assertTrue("Instance description at position " + i + " is not as expected. It was " + Arrays.toString(instance.getX()) + " but expected was " + Arrays.toString(expectedX[i]), Arrays.equals(instance.getX(), expectedX[i]));
			assertTrue("Target description at position " + i + " is not as expected. It was " + Arrays.toString(instance.getY()) + " but expected was " + Arrays.toString(expectedY[i]), Arrays.equals(instance.getY(), expectedY[i]));
		}
	}

	private void checkAttributeNames(final String msg, final String[] expectedNames, final List<IAttribute> attributes) {
		for (int i = 0; i < attributes.size(); i++) {
			assertEquals(msg, expectedNames[i], attributes.get(i).getName());
		}
	}

}
