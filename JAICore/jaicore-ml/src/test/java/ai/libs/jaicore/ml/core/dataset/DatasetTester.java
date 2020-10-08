package ai.libs.jaicore.ml.core.dataset;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;
import ai.libs.jaicore.test.LongTest;
import ai.libs.jaicore.test.MediumTest;

public class DatasetTester {

	@Test
	@MediumTest
	public void testColumnRemovalForDenseInstances() throws DatasetDeserializationFailedException {
		this.testColumnRemoval((Dataset) OpenMLDatasetReader.deserializeDataset(60));
	}

	@Test
	@LongTest
	public void testColumnRemovalForSparseInstances() throws DatasetDeserializationFailedException {
		this.testColumnRemoval((Dataset) OpenMLDatasetReader.deserializeDataset(4137));
	}

	public void testColumnRemoval(final Dataset ds) {
		int numColumnsBefore = ds.getNumAttributes();
		int numColumnsExpectedAfter = numColumnsBefore - 10;
		Random r = new Random();
		while (ds.getNumAttributes() > numColumnsExpectedAfter) {
			ds.removeColumn(r.nextInt(ds.getNumAttributes()));
		}
		assertEquals(numColumnsExpectedAfter, ds.getNumAttributes());
		for (ILabeledInstance i : ds) {
			assertEquals(numColumnsExpectedAfter, i.getNumAttributes());
		}
	}
}
