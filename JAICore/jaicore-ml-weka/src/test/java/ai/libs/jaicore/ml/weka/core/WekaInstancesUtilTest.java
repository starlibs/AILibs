package ai.libs.jaicore.ml.weka.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassificationPredictionBatch;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.serialization.UnsupportedAttributeTypeException;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ai.libs.jaicore.basic.ATest;
import ai.libs.jaicore.ml.core.dataset.SparseInstance;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetAdapterTest;
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

public class WekaInstancesUtilTest extends ATest {

	public static Stream<Arguments> getDatasets() throws Exception {
		return OpenMLDatasetAdapterTest.getSmallDatasets();
	}

	private static final double DELTA = 0.00001;

	public WekaInstances getWekaDataset(final OpenMLProblemSet problemSet) throws DatasetDeserializationFailedException, InterruptedException {
		return new WekaInstances(problemSet.getDataset());
	}

	@ParameterizedTest(name="Test util converstion on {0}")
	@MethodSource("getDatasets")
	public void testUtilConversion(final OpenMLProblemSet problemSet) throws DatasetDeserializationFailedException, InterruptedException, UnsupportedAttributeTypeException {
		WekaInstances dataset = this.getWekaDataset(problemSet);
		Instances wekaInstances = WekaInstancesUtil.datasetToWekaInstances(dataset);
		assertEquals(dataset.size(), wekaInstances.size());
		assertEquals(dataset.getNumAttributes(), wekaInstances.numAttributes() - (long) 1);
	}

	@ParameterizedTest(name="Test instances constructor on {0}")
	@MethodSource("getDatasets")
	public void testWekaInstancesConstructor(final OpenMLProblemSet problemSet) throws DatasetDeserializationFailedException, InterruptedException {
		WekaInstances dataset = this.getWekaDataset(problemSet);
		IWekaInstances wekaInstances = new WekaInstances(dataset);
		assertEquals(dataset.size(), wekaInstances.size());
		assertEquals(dataset.getNumAttributes(), wekaInstances.getNumAttributes());
		int n = dataset.size();
		for (int i = 0; i < n; i++) {
			assertEquals(Double.valueOf(dataset.get(i).getLabel().toString()), Double.valueOf(wekaInstances.get(i).getLabel().toString()), DELTA);
			assertEquals(Double.valueOf(dataset.get(i).getLabel().toString()), wekaInstances.getInstances().get(i).classValue(), DELTA);
		}
	}

	@ParameterizedTest(name="Test instances constructor on {0}")
	@MethodSource("getDatasets")
	public void testWekaInstanceConstructor(final OpenMLProblemSet problemSet) throws DatasetDeserializationFailedException, InterruptedException, UnsupportedAttributeTypeException {
		WekaInstances dataset = this.getWekaDataset(problemSet);
		DescriptiveStatistics runtimeStats = new DescriptiveStatistics();
		for (ILabeledInstance i : dataset) {
			long start = System.currentTimeMillis();
			IWekaInstance wekaInstance = new WekaInstance(dataset.getInstanceSchema(), i);
			long constructionTime = System.currentTimeMillis() - start;
			assertEquals(dataset.getNumAttributes(), wekaInstance.getNumAttributes());
			runtimeStats.addValue(constructionTime);
		}
		assertTrue("The average instance construction time was " + runtimeStats.getMean() + " but at most 10ms is allowed.", runtimeStats.getMean() <= 10);
	}

	@ParameterizedTest(name="Test sparse2sparse and dense2dense on {0}")
	@MethodSource("getDatasets")
	public void testSparse2SparseAndDense2Dense(final OpenMLProblemSet problemSet) throws DatasetDeserializationFailedException, InterruptedException {
		WekaInstances dataset = this.getWekaDataset(problemSet);
		for (ILabeledInstance i : dataset) {
			assertEquals((i instanceof SparseInstance), (((WekaInstance) i).getElement() instanceof weka.core.SparseInstance));
		}
	}

	@ParameterizedTest(name="Test training and prediction with ZeroR on {0}")
	@MethodSource("getDatasets")
	public void testTrainingAndPrediction(final OpenMLProblemSet problemSet) throws DatasetDeserializationFailedException, InterruptedException, TrainingException, SplitFailedException, PredictionException {
		WekaInstances dataset = this.getWekaDataset(problemSet);
		List<ILabeledDataset<?>> split = SplitterUtil.getSimpleTrainTestSplit(dataset, 0, .7);
		WekaClassifier c = new WekaClassifier(new ZeroR());
		ISingleLabelClassificationPredictionBatch batch = c.fitAndPredict(split.get(0), split.get(1));
		assertEquals(split.get(1).size(), batch.size());
	}
}
