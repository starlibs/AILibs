package ai.libs.jaicore.ml.weka.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.common.reconstruction.ReconstructionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ai.libs.jaicore.basic.ATest;
import ai.libs.jaicore.ml.core.dataset.serialization.CSVDatasetAdapter;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetAdapterTest;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;
import ai.libs.jaicore.ml.experiments.OpenMLProblemSet;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstances;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;

public class OpenMLWekaDatasetAdapterTest extends ATest {

	public static Stream<Arguments> getDatasets() throws Exception {
		return Stream.concat(OpenMLDatasetAdapterTest.getSmallDatasets(), OpenMLDatasetAdapterTest.getMediumDatasets());
	}

	@BeforeEach
	public void runGC() {
		System.gc();
	}

	@ParameterizedTest(name="Test Castability to WekaInstance on {0}")
	@MethodSource("getDatasets")
	public void testCastabilityToWekaInstances(final OpenMLProblemSet problemSet) throws DatasetDeserializationFailedException, InterruptedException, ReconstructionException {
		ILabeledDataset<?> ds = OpenMLDatasetReader.deserializeDataset(problemSet.getId());
		WekaInstances inst = new WekaInstances(ds);
		assertEquals(ds.size(), inst.size());
		assertEquals(ds.getNumAttributes(), inst.getNumAttributes());
	}

	@ParameterizedTest(name="Test CSV-Serializability to WekaInstance on {0}")
	@MethodSource("getDatasets")
	public void testCSVSerializability(final OpenMLProblemSet problemSet) throws DatasetDeserializationFailedException, InterruptedException, ReconstructionException, IOException {
		this.logger.info("Reading in dataset {} ({})", problemSet.getId(), problemSet.getName());
		ILabeledDataset<ILabeledInstance> dataset = OpenMLDatasetReader.deserializeDataset(problemSet.getId());
		this.logger.info("Read in dataset {} ({}). Now starting conversion to IWekaInstances.", problemSet.getId(), problemSet.getName());
		IWekaInstances instances = new WekaInstances(dataset);
		this.logger.info("WekaInstances object ready. Now serializing it.");
		File f = new File("tmp/test/" + problemSet.getId() + ".csv");
		f.delete();
		org.apache.commons.io.FileUtils.forceMkdir(f.getParentFile());
		CSVDatasetAdapter.writeDataset(f, instances);
		this.logger.info("Finished serialization. Checking that it's there.");
		assertTrue(f.exists());
	}
}
