package ai.libs.jaicore.ml.core;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collection;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.common.reconstruction.ReconstructionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import ai.libs.jaicore.basic.Tester;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetAdapterTest;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;
import ai.libs.jaicore.ml.experiments.OpenMLProblemSet;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;

@RunWith(Parameterized.class)
public class OpenMLWekaDatasetAdapterTest extends Tester {

	// creates the test data
	@Parameters(name = "{0}")
	public static Collection<OpenMLProblemSet[]> data() throws IOException, Exception {
		return OpenMLDatasetAdapterTest.data();
	}

	@Parameter(0)
	public OpenMLProblemSet problemSet;

	@Test
	public void testCastabilityToWekaInstances() throws DatasetDeserializationFailedException, InterruptedException, ReconstructionException {
		System.gc();
		ILabeledDataset<?> ds = OpenMLDatasetReader.deserializeDataset(this.problemSet.getId());
		WekaInstances inst = new WekaInstances(ds);
		assertEquals(ds.size(), inst.size());
		assertEquals(ds.getNumAttributes(), inst.getNumAttributes());
	}
}
