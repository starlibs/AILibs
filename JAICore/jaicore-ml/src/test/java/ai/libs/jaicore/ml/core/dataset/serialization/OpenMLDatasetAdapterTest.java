package ai.libs.jaicore.ml.core.dataset.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.common.reconstruction.IReconstructible;
import org.api4.java.common.reconstruction.IReconstructionPlan;
import org.api4.java.common.reconstruction.ReconstructionException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataQuality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.logging.LoggerUtil;
import ai.libs.jaicore.ml.core.dataset.DatasetTestUtil;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.jaicore.ml.experiments.OpenMLProblemSet;
import ai.libs.jaicore.test.MediumTest;

public class OpenMLDatasetAdapterTest {

	public static final File TMP_FOLDER = new File("testrsc/tmp/openmlreader");

	protected Logger logger = LoggerFactory.getLogger(LoggerUtil.LOGGER_NAME_TESTER);

	private static OpenmlConnector con = new OpenmlConnector();

	private static Map<Integer, Integer> numInstances = new HashMap<>();
	private static Map<Integer, Integer> numFeatures = new HashMap<>();
	private static Map<Integer, Integer> numClasses = new HashMap<>();

	private static OpenMLProblemSet register(final int id) throws Exception {
		DataQuality quality = con.dataQualities(id, 1);
		numClasses.put(id, quality.getQualitiesMap().get("NumberOfClasses").intValue());
		numInstances.put(id, quality.getQualitiesMap().get("NumberOfInstances").intValue());
		numFeatures.put(id, quality.getQualitiesMap().get("NumberOfFeatures").intValue() - 1);
		return new OpenMLProblemSet(id);
	}

	@AfterAll
	public static void eraseTmpFolder() throws IOException {
		FileUtil.deleteFolderRecursively(TMP_FOLDER);
	}

	// creates the test data
	public static Stream<Arguments> readDatasets(final List<Integer> ids) {
		return ids.stream().map(t -> {
			try {
				return Arguments.of(OpenMLDatasetAdapterTest.register(t));
			} catch (Exception e) {
				e.printStackTrace();
				throw new IllegalStateException(e);
			}
		});
	}

	public static Stream<Arguments> getSmallDatasets() throws IOException, Exception {
		return readDatasets(Arrays.asList(3, // kr-vs-kp
				6, // letter
				9, // autos
				12, // mfeat-factors
				14, // mfeat-fourier
				16, // mfeat-karhunen
				18, // mfeat-morph
				21, // car
				22, // mfeat-zernike
				23, // cmc
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
				// /* */ 46 // splice => contains ignore attribute which we cannot deal with yet.
				57, // hypothyroid
				//				60, // waveform-5000; this dataset has a -0.0 entry and can hence not be recovered appropriately
				61, // iris
				179, // adult
				181, // yeast
				182, // satimage
				183, // abalone
				184, // kropt
				185, // baseball
				1101, // lymphoma_2classes
				1104, // leukemia
				1501, // semeion
				1515, // micro-mass
				1590, // adult
				40691, // wine-quality-red
				41066 // secom
				));
	}

	public static Stream<Arguments> getMediumDatasets() throws IOException, Exception {
		return readDatasets(Arrays.asList(
				149, // CovPokElec
				155, // pokerhand
				180, // covertype
				273, // IMDB Drama
				293, // covertype
				300, // isolet
				351, // codrna
				354, // poker
				554, // MNIST
				1039, // hiva_agnostic
				1150, // AP_Breast_Lung
				1152, // AP_Prostate_Ovary
				1156, // AP_Omentum_Ovary
				1240, // AirlinesCodmaAdult
				1457, // amazon
				4136, // dexter
				4137, // dorothea
				23512, // higgs
				// /**/ 40594, // Reuters => Multi target
				40668, // connect-4
				41064 // convex
				// /**/ 42123 // articleinfluence => string attribute
				));
	}

	public static Stream<Arguments> getBigDatasets() throws IOException, Exception {
		return readDatasets(Arrays.asList(
				40927, // CIFAR-10 // this is even TOO big for a 4GB test
				41026, // gisette // this is even TOO big for a 4GB test
				41065 // mnist rotation // this in fact just fits into a 4GB test but sometime can cause problems; its a borderline case
				));
	}

	private static int lastCachedId; // this is to detect whether the current dataset hold in dataset fits the current problem set
	private static ILabeledDataset<?> dataset;

	private void assureThatCorrectDatasetIsLoaded(final OpenMLProblemSet problemSet, final boolean checkProperties) throws DatasetDeserializationFailedException, InterruptedException, ClassNotFoundException, IOException {
		int id = problemSet.getId();
		if (lastCachedId != id) {
			dataset = this.read(id, numInstances.get(id), numFeatures.get(id), numClasses.get(id), checkProperties);
			lastCachedId = id;
		}
	}

	public ILabeledDataset<ILabeledInstance> read(final int id, final int expectedInstances, final int expectedAttributes, final int expectedClasses, final boolean checkProperties) throws DatasetDeserializationFailedException, InterruptedException, IOException, ClassNotFoundException {
		File file = new File(TMP_FOLDER + File.separator + "openmlid-" + id);
		if (!file.exists()) {
			this.logger.info("Reading in dataset with id {}", id);
			ILabeledDataset<ILabeledInstance> data = OpenMLDatasetReader.deserializeDataset(id);
			if (checkProperties) {
				this.logger.info("Dataset read, now checking general properties.");
				assertEquals("Incorrect number of instances.", expectedInstances, data.size());
				this.logger.debug("Number of instance correct. Now checking number of attributes.");
				assertEquals("Incorrect number of attributes.", expectedAttributes, data.getNumAttributes());
				this.logger.debug("Number of attributes correct, now checking data coherence.");
				DatasetTestUtil.checkDatasetCoherence(data);
				this.logger.info("Dataset valid. Serializing it.");
			}
			else {
				this.logger.debug("Dataset read. Not checking properties.");
			}
			FileUtil.serializeObject(data, file.getAbsolutePath());
			return data;
		}
		else {
			this.logger.info("Re-using previously checked serialization of dataset {}.", id);
			return (ILabeledDataset<ILabeledInstance>)FileUtil.unserializeObject(file.getAbsolutePath());
		}
	}

	@ParameterizedTest(name="test reconstructibility of stratified split on {0}")
	@MethodSource("getSmallDatasets")
	public void testReconstructibilityOfStratifiedSplitOnSmallDataset(final OpenMLProblemSet problemSet) throws DatasetDeserializationFailedException, InterruptedException, ReconstructionException, SplitFailedException, ClassNotFoundException, IOException {
		this.testReconstructibilityOfStratifiedSplit(problemSet);
	}

	@MediumTest
	@ParameterizedTest(name="test reconstructibility of stratified split on {0}")
	@MethodSource("getMediumDatasets")
	public void testReconstructibilityOfStratifiedSplitOnBigDataset(final OpenMLProblemSet problemSet) throws DatasetDeserializationFailedException, InterruptedException, ReconstructionException, SplitFailedException, ClassNotFoundException, IOException {
		this.testReconstructibilityOfStratifiedSplit(problemSet);
	}

	@MediumTest
	@ParameterizedTest(name="test writing {0}")
	@MethodSource("getSmallDatasets")
	public void testWriteOnSmallDataset(final OpenMLProblemSet problemSet) throws IOException, DatasetDeserializationFailedException, InterruptedException, ClassNotFoundException {
		this.testWrite(problemSet);
	}

	@MediumTest
	@ParameterizedTest(name="test writing {0}")
	@MethodSource("getMediumDatasets")
	public void testWriteOnBigDataset(final OpenMLProblemSet problemSet) throws IOException, DatasetDeserializationFailedException, InterruptedException, ClassNotFoundException {
		this.testWrite(problemSet);
	}

	@ParameterizedTest(name="test reconstructibility of {0}")
	@MethodSource("getSmallDatasets")
	public void testWriteOnSmallDatasetOnSmallDataset(final OpenMLProblemSet problemSet) throws IOException, DatasetDeserializationFailedException, InterruptedException, ReconstructionException, ClassNotFoundException {
		this.testReconstructibility(problemSet);
	}

	@MediumTest
	@ParameterizedTest(name="test reconstructibility of {0}")
	@MethodSource("getMediumDatasets")
	public void testReconstructibilityOnBigDataset(final OpenMLProblemSet problemSet) throws IOException, DatasetDeserializationFailedException, InterruptedException, ReconstructionException, ClassNotFoundException {
		this.testReconstructibility(problemSet);
	}

	private void testReconstructibilityOfStratifiedSplit(final OpenMLProblemSet problemSet) throws DatasetDeserializationFailedException, InterruptedException, ReconstructionException, SplitFailedException, ClassNotFoundException, IOException {
		this.assureThatCorrectDatasetIsLoaded(problemSet, true);
		Objects.requireNonNull(dataset);

		/* create stratified split and test that folds are reproducible */
		this.logger.info("Creating a stratified split.");
		List<ILabeledDataset<?>> split = SplitterUtil.getLabelStratifiedTrainTestSplit(dataset, 0, .7);
		ILabeledDataset<?> reproducedFirstFold = (ILabeledDataset<?>) ((IReconstructible) split.get(0)).getConstructionPlan().reconstructObject();
		ILabeledDataset<?> reproducedSecondFold = (ILabeledDataset<?>) ((IReconstructible) split.get(1)).getConstructionPlan().reconstructObject();
		this.logger.info("Testing that folds are reconstructible.");
		this.testReproduction(split.get(0), reproducedFirstFold);
		this.testReproduction(split.get(1), reproducedSecondFold);
	}

	private void testWrite(final OpenMLProblemSet problemSet) throws IOException, DatasetDeserializationFailedException, InterruptedException, ClassNotFoundException {
		this.assureThatCorrectDatasetIsLoaded(problemSet, true);
		File outFile = new File("tmp/test.arff");
		outFile.getParentFile().mkdirs();
		this.logger.debug("Reading in dataset.", outFile);
		ILabeledDataset<?> ds = dataset;
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

	private void testReconstructibility(final OpenMLProblemSet problemSet) throws DatasetDeserializationFailedException, InterruptedException, ReconstructionException, ClassNotFoundException, IOException {
		this.assureThatCorrectDatasetIsLoaded(problemSet, true);
		Objects.requireNonNull(dataset);
		if (!(dataset instanceof IReconstructible)) {
			fail("Dataset not reconstructible");
		}

		/* test reproducibility of the dataset itself */
		IReconstructible rDataset = (IReconstructible) dataset;
		this.logger.info("Creating reconstruction plan.");
		IReconstructionPlan plan = rDataset.getConstructionPlan();
		this.logger.info("Recovering object from reconstruction plan.");
		ILabeledDataset<?> reproducedDataset = (ILabeledDataset<?>) plan.reconstructObject();
		this.logger.info("Testing equalness of the reproduced object");
		this.testReproduction(dataset, reproducedDataset);
	}

	private void testReproduction(final ILabeledDataset<?> expected, final ILabeledDataset<?> actual) {
		IReconstructible cExpected = (IReconstructible) expected;
		assertEquals(expected.get(0), actual.get(0)); // first check that the first instance is equal in both cases. This is of course covered by later assertions but simplifies debugging in case of failure
		assertEquals(expected.getInstanceSchema(), actual.getInstanceSchema()); // check that schema are identically. Again, this is checked for convenience
		assertEquals(cExpected, actual); // full equality check
	}
}
