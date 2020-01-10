package ai.libs.jaicore.ml.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassificationPredictionBatch;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.serialization.UnsupportedAttributeTypeException;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import ai.libs.jaicore.ml.core.dataset.SparseInstance;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetAdapterTest;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.jaicore.ml.experiments.OpenMLProblemSet;
import ai.libs.jaicore.ml.weka.classification.learner.WekaClassifier;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstance;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstances;
import ai.libs.jaicore.ml.weka.dataset.WekaInstance;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import ai.libs.jaicore.ml.weka.dataset.WekaInstancesUtil;
import weka.classifiers.rules.ZeroR;
import weka.core.Instances;

@RunWith(Parameterized.class)
public class WekaInstancesUtilTester {

	// creates the test data
	@Parameters(name = "{0}")
	public static Collection<OpenMLProblemSet[]> data() throws Exception {
		return OpenMLDatasetAdapterTest.data();
	}

	private static final double DELTA = 0.00001;

	@Parameter(0)
	public OpenMLProblemSet problemSet;

	@Test
	public void testUtilConversion() throws DatasetDeserializationFailedException, InterruptedException, UnsupportedAttributeTypeException {
		ILabeledDataset<?> dataset = OpenMLDatasetReader.deserializeDataset(this.problemSet.getId());
		Instances wekaInstances = WekaInstancesUtil.datasetToWekaInstances(dataset);
		assertEquals(dataset.size(), wekaInstances.size());
		assertEquals(dataset.getNumAttributes(), wekaInstances.numAttributes() - (long)1);
	}

	@Test
	public void testWekaInstancesConstructor() throws DatasetDeserializationFailedException, InterruptedException {
		ILabeledDataset<?> dataset = OpenMLDatasetReader.deserializeDataset(this.problemSet.getId());
		IWekaInstances wekaInstances = new WekaInstances(dataset);
		assertEquals(dataset.size(), wekaInstances.size());
		assertEquals(dataset.getNumAttributes(), wekaInstances.getNumAttributes());
		int n = dataset.size();
		for (int i = 0; i < n; i++) {
			assertEquals(Double.valueOf(dataset.get(i).getLabel().toString()), Double.valueOf(wekaInstances.get(i).getLabel().toString()), DELTA);
			assertEquals(Double.valueOf(dataset.get(i).getLabel().toString()), wekaInstances.getInstances().get(i).classValue(), DELTA);
		}
	}

	@Test
	public void testWekaInstanceConstructor() throws DatasetDeserializationFailedException, InterruptedException, UnsupportedAttributeTypeException {
		ILabeledDataset<?> dataset = OpenMLDatasetReader.deserializeDataset(this.problemSet.getId());
		for (ILabeledInstance i : dataset) {
			IWekaInstance wekaInstance = new WekaInstance(dataset.getInstanceSchema(), i);
			assertEquals(dataset.getNumAttributes(), wekaInstance.getNumAttributes());
		}
	}

	@Test
	public void testSparse2SparseAndDense2Dense() throws DatasetDeserializationFailedException, InterruptedException {
		ILabeledDataset<?> dataset = OpenMLDatasetReader.deserializeDataset(this.problemSet.getId());
		for (ILabeledInstance i : dataset) {
			assertTrue((i instanceof SparseInstance) == (((WekaInstance)i).getElement() instanceof weka.core.SparseInstance));
		}
	}

	@Test
	public void testTrainingAndPrediction() throws DatasetDeserializationFailedException, InterruptedException, TrainingException, SplitFailedException, PredictionException {
		ILabeledDataset<?> dataset = OpenMLDatasetReader.deserializeDataset(this.problemSet.getId());
		List<ILabeledDataset<?>> split = SplitterUtil.getSimpleTrainTestSplit(dataset, 0, .7);
		WekaClassifier c = new WekaClassifier(new ZeroR());
		ISingleLabelClassificationPredictionBatch batch = c.fitAndPredict(split.get(0), split.get(1));
		assertEquals(split.get(1).size(), batch.size());
	}
}
