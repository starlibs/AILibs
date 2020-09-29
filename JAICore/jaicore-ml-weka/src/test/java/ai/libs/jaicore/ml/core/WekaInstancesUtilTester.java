package ai.libs.jaicore.ml.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Stream;

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

import ai.libs.jaicore.basic.Tester;
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

public class WekaInstancesUtilTester extends Tester {

	public static Stream<Arguments> getDatasets() throws Exception {
		return OpenMLDatasetAdapterTest.getSmallDatasets();
	}

	private static final double DELTA = 0.00001;

	public WekaInstances getWekaDataset(final OpenMLProblemSet problemSet) throws DatasetDeserializationFailedException, InterruptedException {
		return new WekaInstances(problemSet.getDataset());
	}

	@ParameterizedTest
	@MethodSource("getDatasets")
	public void testUtilConversion(final OpenMLProblemSet problemSet) throws DatasetDeserializationFailedException, InterruptedException, UnsupportedAttributeTypeException {
		WekaInstances dataset = this.getWekaDataset(problemSet);
		Instances wekaInstances = WekaInstancesUtil.datasetToWekaInstances(dataset);
		assertEquals(dataset.size(), wekaInstances.size());
		assertEquals(dataset.getNumAttributes(), wekaInstances.numAttributes() - (long)1);
	}

	@ParameterizedTest
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

	@ParameterizedTest
	@MethodSource("getDatasets")
	public void testWekaInstanceConstructor(final OpenMLProblemSet problemSet) throws DatasetDeserializationFailedException, InterruptedException, UnsupportedAttributeTypeException {
		WekaInstances dataset = this.getWekaDataset(problemSet);
		for (ILabeledInstance i : dataset) {
			long start = System.currentTimeMillis();
			IWekaInstance wekaInstance = new WekaInstance(dataset.getInstanceSchema(), i);
			long constructionTime = System.currentTimeMillis() - start;
			assertEquals(dataset.getNumAttributes(), wekaInstance.getNumAttributes());
			assertTrue("The construction time for the instance was " + constructionTime + " but at most 1ms is allowed.", constructionTime <= 5);
		}
	}

	@ParameterizedTest
	@MethodSource("getDatasets")
	public void testSparse2SparseAndDense2Dense(final OpenMLProblemSet problemSet) throws DatasetDeserializationFailedException, InterruptedException {
		WekaInstances dataset = this.getWekaDataset(problemSet);
		for (ILabeledInstance i : dataset) {
			assertTrue((i instanceof SparseInstance) == (((WekaInstance)i).getElement() instanceof weka.core.SparseInstance));
		}
	}

	@ParameterizedTest
	@MethodSource("getDatasets")
	public void testTrainingAndPrediction(final OpenMLProblemSet problemSet) throws DatasetDeserializationFailedException, InterruptedException, TrainingException, SplitFailedException, PredictionException {
		WekaInstances dataset = this.getWekaDataset(problemSet);
		List<ILabeledDataset<?>> split = SplitterUtil.getSimpleTrainTestSplit(dataset, 0, .7);
		WekaClassifier c = new WekaClassifier(new ZeroR());
		ISingleLabelClassificationPredictionBatch batch = c.fitAndPredict(split.get(0), split.get(1));
		assertEquals(split.get(1).size(), batch.size());
	}
}
