package ai.libs.jaicore.ml.core.dataset.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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

import ai.libs.jaicore.ml.core.dataset.DatasetTestUtil;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.jaicore.ml.experiments.OpenMLProblemSet;

@RunWith(Parameterized.class)
public class OpenMLDatasetAdapterTest {

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

	// creates the test data
	@Parameters(name = "{0}")
	public static Collection<OpenMLProblemSet[]> data() throws IOException, Exception {
		List<OpenMLProblemSet> problemSets = new ArrayList<>();
		problemSets.add(register(3, 3196, 36, 2, true)); // kr-vs-kp
		problemSets.add(register(23512, 98050, 28, 2, true)); // higgs
		problemSets.add(register(1457, 1500, 10000, 50, true)); // amazon
		problemSets.add(register(42123, 3615, 6, 3169, true)); // articleinfluence
		problemSets.add(register(554, 70000, 784, 10, true)); // MNIST
		problemSets.add(register(4136, 600, 20000, 2, true)); // dexter
		problemSets.add(register(4137, 1150, 100000, 2, true)); // dorothea
		problemSets.add(register(41026, 7000, 5000, 2, false)); // gisette
		problemSets.add(register(40927, 60000, 3072, 10, false)); // CIFAR-10
		problemSets.add(register(183, 4177, 8, 28, true)); // abalone
		problemSets.add(register(181, 1484, 8, 10, true)); // yeast
		problemSets.add(register(1501, 1593, 256, 10, true)); // semeion
		problemSets.add(register(41064, 58000, 784, 2, true)); // convex
		problemSets.add(register(41066, 1567, 590, 2, true)); // secom
		problemSets.add(register(41065, 62000, 784, 10, true)); // mnist rotate
		problemSets.add(register(273, 120919, 1001, 2, true)); // IMDB drama
		problemSets.add(register(1156, 275, 10936, 2, true)); // AP_Omentum_Ovary
//		problemSets.add(register(40594, 2000, 250, 12, true)); // Reuters (multi-label classification dataset; not yet supported)

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
