package ai.libs.jaicore.ml.core;

import static org.junit.Assert.assertEquals;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.serialization.UnsupportedAttributeTypeException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.junit.Test;

import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;
import ai.libs.jaicore.ml.weka.dataset.WekaInstancesUtil;
import weka.core.Instances;

public class WekaInstancesUtilTester {

	@Test
	public void testConverstion() throws DatasetDeserializationFailedException, InterruptedException, UnsupportedAttributeTypeException {
		ILabeledDataset<?> dataset = OpenMLDatasetReader.deserializeDataset(3);
		Instances wekaInstances = WekaInstancesUtil.datasetToWekaInstances(dataset);
		assertEquals(dataset.size(), wekaInstances.size());
		assertEquals(dataset.getNumAttributes(), wekaInstances.numAttributes() - 1);
	}
}
