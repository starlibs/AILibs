package ai.libs.jaicore.ml.scikitwrapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.ai.ml.core.evaluation.execution.LearnerExecutionFailedException;
import org.api4.java.ai.ml.regression.evaluation.IRegressionPrediction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;
import ai.libs.jaicore.ml.core.evaluation.evaluator.SupervisedLearnerExecutor;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.jaicore.ml.regression.loss.ERegressionPerformanceMeasure;
import ai.libs.jaicore.ml.scikitwrapper.simple.SimpleScikitLearnRegressor;

public class ScikitLearnRegressionTest {

	private static ILabeledDataset<ILabeledInstance> datasetLaser;

	@BeforeAll
	public static void setup() throws DatasetDeserializationFailedException {
		datasetLaser = new OpenMLDatasetReader().deserializeDataset(42364);
	}

	@Test
	public void testRegressionRMSEofRFOnMIP() throws IOException, SplitFailedException, InterruptedException, LearnerExecutionFailedException {
		SimpleScikitLearnRegressor slw = new SimpleScikitLearnRegressor("RandomForestRegressor()", "from sklearn.ensemble import RandomForestRegressor");
		List<ILabeledDataset<?>> split = SplitterUtil.getSimpleTrainTestSplit(datasetLaser, 42, .7);
		ILearnerRunReport report = new SupervisedLearnerExecutor().execute(slw, split.get(0), split.get(1));
		double loss = ERegressionPerformanceMeasure.RMSE.loss(report.getPredictionDiffList().getCastedView(Double.class, IRegressionPrediction.class));
		assertTrue(loss < 40);
	}

}
