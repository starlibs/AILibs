package ai.libs.jaicore.ml.weka.core;

import static org.junit.Assert.assertEquals;

import java.util.stream.Stream;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.common.reconstruction.ReconstructionException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ai.libs.jaicore.basic.ATest;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetAdapterTest;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;
import ai.libs.jaicore.ml.experiments.OpenMLProblemSet;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;

public class OpenMLWekaDatasetAdapterTest extends ATest {

	public static Stream<Arguments> getDatasets() throws Exception {
		return OpenMLDatasetAdapterTest.getSmallDatasets();
	}

	@ParameterizedTest(name="Test Castability to WekaInstance on {0}")
	@MethodSource("getDatasets")
	public void testCastabilityToWekaInstances(final OpenMLProblemSet problemSet) throws DatasetDeserializationFailedException, InterruptedException, ReconstructionException {
		System.gc();
		ILabeledDataset<?> ds = OpenMLDatasetReader.deserializeDataset(problemSet.getId());
		WekaInstances inst = new WekaInstances(ds);
		assertEquals(ds.size(), inst.size());
		assertEquals(ds.getNumAttributes(), inst.getNumAttributes());
	}
}
