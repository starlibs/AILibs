package ai.libs.jaicore.ml.core.dataset.splitter;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ai.libs.jaicore.ml.core.dataset.serialization.ArffDatasetAdapter;

public class DatasetSplitSetTest {

	private static final File TEST_ARFF_FILE = new File("testrsc/dataset/arff/numeric_only_with_classindex.arff");
	private static ILabeledDataset<ILabeledInstance> d;

	private List<List<ILabeledDataset<ILabeledInstance>>> testSplits;
	private DatasetSplitSet<ILabeledDataset<ILabeledInstance>> splitSet;

	@BeforeClass
	public static void setup() throws DatasetDeserializationFailedException, InterruptedException {
		d = ArffDatasetAdapter.readDataset(TEST_ARFF_FILE);
	}

	@Before
	public void loadSplits() {
		this.testSplits = Arrays.asList(Arrays.asList(d, d, d), Arrays.asList(d, d));
		this.splitSet = new DatasetSplitSet<>(this.testSplits);
	}

	@Test
	public void testAddSplit() {
		List<ILabeledDataset<ILabeledInstance>> newSplit = Arrays.asList(d, d, d, d);
		this.splitSet.addSplit(newSplit);
		assertEquals("Number of splits does not match", this.testSplits.size() + 1, this.splitSet.getNumberOfSplits());
		assertEquals("Last split does not match the shape of the added split", this.splitSet.getNumberOfFoldsForSplit(this.splitSet.getNumberOfSplits() - 1), newSplit.size());
	}

	@Test
	public void testGetNumberofFoldsPerSplit() {
		assertEquals("The number of folds does not match for the first split.", this.testSplits.get(0).size(), this.splitSet.getNumberOfFoldsPerSplit());
	}

	@Test
	public void testGetFolds() {
		for (int i = 0; i < this.splitSet.getNumberOfSplits(); i++) {
			assertEquals("The folds of split " + i + " do not match.", this.testSplits.get(i), this.splitSet.getFolds(i));
		}
	}

}
