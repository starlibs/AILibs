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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.logging.LoggerUtil;
import ai.libs.jaicore.ml.core.dataset.DatasetTestUtil;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.jaicore.ml.experiments.OpenMLProblemSet;

@RunWith(Parameterized.class)
public class OpenMLDatasetAdapterTest {

	private static final boolean IGNORE_BIG_DATASETS = false;
	protected Logger logger = LoggerFactory.getLogger(LoggerUtil.LOGGER_NAME_TESTER);

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
				6, // letter
				9, // autos
				12, // mfeat-factors
				14, // mfeat-fourier
				16, // mfeat-karhunen
				18, // mfeat-morph
				21, // car
				22, // mfeat-zernike
				23, //cmc
				24, // mushroom
				26, // nursey
				28, // optdigits
				30, // page-blocks
				31, // credit-g
				32, // pendigits
				36, // segment
				38, // sick
				39, // ecoli
				44, // spambase
				46, // splice
				57, // hypothyroid
				60, // waveform-5000
				61, // iris
				149, // CovPokElec
				155, // pokerhand
				179, // adult
				180, // covertype
				181, // yeast
				182, // satimage
				183, // abalone
				184, // kropt
				185, // baseball
				273, // IMDB Drama
				293, // covertype
				300, // isolet
				351, // codrna
				354, // poker
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
				1515, // micro-mass
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
						OpenMLProblemSet set = OpenMLDatasetAdapterTest.register(t);
						if (IGNORE_BIG_DATASETS && (numInstances.get(t) * numFeatures.get(t) > 1000000)) {
							return; // ignore big datasets here
						}
						problemSets.add(set);
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
	private static int lastCachedId; // this is to detect whether the current dataset hold in dataset fits the current problem set
	private static ILabeledDataset<?> dataset;

	private void assureThatCorrectDatasetIsLoaded() throws DatasetDeserializationFailedException, InterruptedException {
		int id = this.problemSet.getId();
		if (lastCachedId != id) {
			dataset = this.read(id, numInstances.get(id), numFeatures.get(id), numClasses.get(id));
			lastCachedId = id;
		}
	}

	public ILabeledDataset<ILabeledInstance> read(final int id, final int expectedInstances, final int expectedAttributes, final int expectedClasses) throws DatasetDeserializationFailedException, InterruptedException {
		this.logger.debug("Reading in dataset with id {}", id);
		ILabeledDataset<ILabeledInstance> data = OpenMLDatasetReader.deserializeDataset(id);
		this.logger.debug("Dataset read, now checking general properties.");
		assertEquals("Incorrect number of instances.", expectedInstances, data.size());
		assertEquals("Incorrect number of attributes.", expectedAttributes, data.getNumAttributes());
		DatasetTestUtil.checkDatasetCoherence(data);
		return data;
	}

	@Test
	public void testReconstructibilityOfStratifiedSplit() throws DatasetDeserializationFailedException, InterruptedException, ReconstructionException, SplitFailedException {
		this.assureThatCorrectDatasetIsLoaded();
		if (!splitTests.contains(this.problemSet.getId())) {
			return;
		}
		System.gc();

		/* create stratified split and test that folds are reproducible */
		this.logger.info("Creating a stratified split.");
		List<ILabeledDataset<?>> split = SplitterUtil.getLabelStratifiedTrainTestSplit(this.dataset, 0, .7);
		ILabeledDataset<?> reproducedFirstFold = (ILabeledDataset<?>) ((IReconstructible) split.get(0)).getConstructionPlan().reconstructObject();
		ILabeledDataset<?> reproducedSecondFold = (ILabeledDataset<?>) ((IReconstructible) split.get(1)).getConstructionPlan().reconstructObject();
		this.logger.info("Testing that folds are reconstructible.");
		this.testReproduction(split.get(0), reproducedFirstFold);
		this.testReproduction(split.get(1), reproducedSecondFold);
	}

	@Test
	public void testWrite() throws IOException, DatasetDeserializationFailedException, InterruptedException {
		this.assureThatCorrectDatasetIsLoaded();
		File outFile = new File("tmp/test.arff");
		outFile.getParentFile().mkdirs();
		this.logger.debug("Reading in dataset.", outFile);
		ILabeledDataset<?> ds = this.dataset;
		this.logger.debug("Serializing dataset.");
		ArffDatasetAdapter.serializeDataset(outFile, ds);
		this.logger.debug("Re-reading dataset from tmp file {}", outFile);
		ILabeledDataset<?> reread = ArffDatasetAdapter.readDataset(outFile);
		assertNotNull("Could not read in dataset again after writing", reread);
		assertEquals("Datasets have different sizes!", ds.size(), reread.size());
		int n = reread.size();
		for (int i = 0; i < n; i++) {
			ILabeledInstance expected = ds.get(i);
			ILabeledInstance actual = reread.get(i);
			assertEquals("Mismatch in " + i + "-th instance of dataset.", expected, actual);
		}
		assertEquals("Datasets are not the same.", ds, reread);
	}

	@Test
	public void testReconstructibility() throws DatasetDeserializationFailedException, InterruptedException, ReconstructionException {
		this.assureThatCorrectDatasetIsLoaded();
		if (!(this.dataset instanceof IReconstructible)) {
			fail("Dataset not reconstructible");
		}

		/* test reproducibility of the dataset itself */
		IReconstructible rDataset = (IReconstructible) this.dataset;
		this.logger.info("Creating reconstruction plan.");
		IReconstructionPlan plan = rDataset.getConstructionPlan();
		this.logger.info("Recovering object from reconstruction plan.");
		ILabeledDataset<?> reproducedDataset = (ILabeledDataset<?>) plan.reconstructObject();
		this.logger.info("Testing equalness of the reproduced object");
		this.testReproduction(this.dataset, reproducedDataset);
	}

	private void testReproduction(final ILabeledDataset<?> expected, final ILabeledDataset<?> actual) {
		IReconstructible cExpected = (IReconstructible) expected;
		assertEquals(expected.get(0), actual.get(0)); // first check that the first instance is equal in both cases. This is of course covered by later assertions but simplifies debugging in case of failure
		assertEquals(expected.getInstanceSchema(), actual.getInstanceSchema()); // check that schema are identically. Again, this is checked for convenience
		assertEquals(cExpected, actual); // full equality check
	}
}
