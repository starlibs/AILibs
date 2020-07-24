package ai.libs.jaicore.ml.core.dataset.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.common.reconstruction.IReconstructible;
import org.api4.java.common.reconstruction.IReconstructionPlan;
import org.api4.java.common.reconstruction.ReconstructionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataQuality;

import ai.libs.jaicore.ml.core.dataset.DatasetTestUtil;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.jaicore.ml.experiments.OpenMLProblemSet;

@RunWith(Parameterized.class)
public class OpenMLDatasetAdapterTest {

	private static OpenmlConnector con = new OpenmlConnector();

	private static Map<Integer, Integer> numInstances = new HashMap<>();
	private static Map<Integer, Integer> numFeatures = new HashMap<>();
	private static Map<Integer, Integer> numClasses = new HashMap<>();
	private static Collection<Integer> splitTests = new HashSet<>(); // the set of datasets for which splitting and reproducibility in splitting is tested

	private static OpenMLProblemSet register(final int id, final int pNumInstances, final int pNumFeatures, final int pNumClasses, final boolean conductSplitTest) throws Exception {
		numInstances.put(id, pNumInstances);
		numFeatures.put(id, pNumFeatures);
		numClasses.put(id, pNumClasses);
		if (conductSplitTest) {
			splitTests.add(id);
		}
		return new OpenMLProblemSet(id);
	}

	private static OpenMLProblemSet register(final int id) throws Exception {
		DataQuality quality = con.dataQualities(id, 1);
		numClasses.put(id, quality.getQualitiesMap().get("NumberOfClasses").intValue());
		numInstances.put(id, quality.getQualitiesMap().get("NumberOfInstances").intValue());
		numFeatures.put(id, quality.getQualitiesMap().get("NumberOfFeatures").intValue() - 1);
		return new OpenMLProblemSet(id);
	}

	// creates the test data
	@Parameters(name = "{0}")
	public static Collection<OpenMLProblemSet[]> data() throws IOException, Exception {
		List<OpenMLProblemSet> problemSets = new ArrayList<>();
		Arrays.asList(//
				3, // kr-vs-kp
				9, // autos
				24, // mushroom
				39, // ecoli
				44, // spambase
				60, // waveform-5000
				61, // iris
				149, // CovPokElec
				155, // pokerhand
				181, // yeast
				182, // satimage
				183, // abalone
				273, // IMDB Drama
				554, // MNIST
				1039, // hiva_agnostic
				1101, // lymphoma_2classes
				1104, // leukemia
				1150, // AP_Breast_Lung
				1152, // AP_Prostate_Ovary
				1156, // AP_Omentum_Ovary
				1240, // AirlinesCodmaAdult
				1457, // amazon
				1501, // semeion
				1590, // adult
				4136, // dexter
				4137, // dorothea
				23512, // higgs
//				40594, // Reuters => Multi target
				40668, // connect-4
				40691, // wine-quality-red
				40927, // CIFAR-10
				41026, // gisette
				41064, // convex
				41065, // mnist rotation
				41066 // secom
//				42123 // articleinfluence => string attribute
		).stream().forEach(t -> {
			try {
				problemSets.add(OpenMLDatasetAdapterTest.register(t));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		OpenMLProblemSet[][] data = new OpenMLProblemSet[problemSets.size()][1];
		for (int i = 0; i < data.length; i++) {
			data[i][0] = problemSets.get(i);
		}
		return Arrays.asList(data);
	}

	@Parameter(0)
	public OpenMLProblemSet problemSet;

	@Test
	public void testReadAndReconstructibility() throws DatasetDeserializationFailedException, InterruptedException, ReconstructionException {
		System.gc();
		int id = this.problemSet.getId();
		int expectedInstances = numInstances.get(id);
		int expectedAttributes = numFeatures.get(id);
		int expectedClasses = numClasses.get(id);
		this.testReconstructibility(this.read(id, expectedInstances, expectedAttributes, expectedClasses));
	}

	public ILabeledDataset<ILabeledInstance> read(final int id, final int expectedInstances, final int expectedAttributes, final int expectedClasses) throws DatasetDeserializationFailedException, InterruptedException {
		ILabeledDataset<ILabeledInstance> data = OpenMLDatasetReader.deserializeDataset(id);
		assertEquals("Incorrect number of instances.", expectedInstances, data.size());
		assertEquals("Incorrect number of attributes.", expectedAttributes, data.getNumAttributes());
		DatasetTestUtil.checkDatasetCoherence(data);
		return data;
	}

	@Test
	public void testReconstructibilityOfStratifiedSplit() throws DatasetDeserializationFailedException, InterruptedException, ReconstructionException, SplitFailedException {
		if (!splitTests.contains(this.problemSet.getId())) {
			return;
		}
		System.gc();
		ILabeledDataset<?> dataset = this.problemSet.getDataset();

		/* create stratified split and test that folds are reproducible */
		System.out.println("Creating a stratified split.");
		List<ILabeledDataset<?>> split = SplitterUtil.getLabelStratifiedTrainTestSplit(dataset, 0, .7);
		ILabeledDataset<?> reproducedFirstFold = (ILabeledDataset<?>) ((IReconstructible) split.get(0)).getConstructionPlan().reconstructObject();
		ILabeledDataset<?> reproducedSecondFold = (ILabeledDataset<?>) ((IReconstructible) split.get(1)).getConstructionPlan().reconstructObject();
		System.out.println("Testing that folds are reconstructible.");
		this.testReproduction(split.get(0), reproducedFirstFold);
		this.testReproduction(split.get(1), reproducedSecondFold);
	}

	@Test
	public void testWrite() throws IOException, DatasetDeserializationFailedException, InterruptedException {
		File outFile = new File("tmp/test.arff");
		outFile.getParentFile().mkdirs();
		ArffDatasetAdapter.serializeDataset(outFile, this.problemSet.getDataset());
		ILabeledDataset<?> reread = ArffDatasetAdapter.readDataset(outFile);
		assertNotNull("Could not read in dataset again after writing", reread);
	}

	public void testReconstructibility(final ILabeledDataset<?> dataset) throws DatasetDeserializationFailedException, InterruptedException, ReconstructionException {
		if (!(dataset instanceof IReconstructible)) {
			fail("Dataset not reconstructible");
		}

		/* test reproducibility of the dataset itself */
		IReconstructible rDataset = (IReconstructible) dataset;
		System.out.println("Creating reconstruction plan.");
		IReconstructionPlan plan = rDataset.getConstructionPlan();
		System.out.println("Recovering object from reconstruction plan.");
		ILabeledDataset<?> reproducedDataset = (ILabeledDataset<?>) plan.reconstructObject();
		System.out.println("Testing equalness of the reproduced object");
		this.testReproduction(dataset, reproducedDataset);
	}

	private void testReproduction(final ILabeledDataset<?> expected, final ILabeledDataset<?> actual) {
		IReconstructible cExpected = (IReconstructible) expected;
		assertEquals(expected.get(0), actual.get(0)); // first check that the first instance is equal in both cases. This is of course covered by later assertions but simplifies debugging in case of failure
		assertEquals(expected.getInstanceSchema(), actual.getInstanceSchema()); // check that schema are identically. Again, this is checked for convenience
		assertEquals(cExpected, actual); // full equality check
	}
}
