package ai.libs.mlplan.cli.report;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.ai.ml.core.evaluation.execution.LearnerExecutionFailedException;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;
import ai.libs.jaicore.ml.core.evaluation.evaluator.SupervisedLearnerExecutor;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.jaicore.ml.weka.classification.learner.WekaClassifier;
import ai.libs.jaicore.ml.weka.regression.learner.WekaRegressor;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;

public class OpenMLAutoMLBenchmarkReportTest {

	private static final File BASE_DIR = new File("testrsc/report/openml-automlbenchmark");
	private static final File EXP_BINARY = new File(BASE_DIR, "binary.txt");
	private static final File EXP_MULTINOMIAL = new File(BASE_DIR, "multinomial.txt");
	private static final File EXP_REGRESSION = new File(BASE_DIR, "regression.txt");

	@Test
	public void testBinaryClassification() throws LearnerExecutionFailedException, SplitFailedException, InterruptedException, DatasetDeserializationFailedException, IOException {
		List<ILabeledDataset> split = SplitterUtil.getLabelStratifiedTrainTestSplit(OpenMLDatasetReader.deserializeDataset(31), 0, .7);
		ILearnerRunReport runReport = new SupervisedLearnerExecutor().execute(new WekaClassifier(new J48()), split.get(0), split.get(1));
		this.testReportOutput(EXP_BINARY, runReport);
	}

	@Test
	public void testMultinomialClassification() throws LearnerExecutionFailedException, SplitFailedException, InterruptedException, DatasetDeserializationFailedException, IOException {
		List<ILabeledDataset> split = SplitterUtil.getLabelStratifiedTrainTestSplit(OpenMLDatasetReader.deserializeDataset(307), 0, .7);
		ILearnerRunReport runReport = new SupervisedLearnerExecutor().execute(new WekaClassifier(new J48()), split.get(0), split.get(1));
		this.testReportOutput(EXP_MULTINOMIAL, runReport);
	}

	@Test
	public void testRegression() throws LearnerExecutionFailedException, SplitFailedException, InterruptedException, DatasetDeserializationFailedException, IOException {
		List<ILabeledDataset<?>> split = SplitterUtil.getSimpleTrainTestSplit(OpenMLDatasetReader.deserializeDataset(42364), 0, .7);
		ILearnerRunReport runReport = new SupervisedLearnerExecutor().execute(new WekaRegressor(new RandomForest()), split.get(0), split.get(1));
		System.out.println(new OpenMLAutoMLBenchmarkReport(runReport));
		this.testReportOutput(EXP_REGRESSION, runReport);
	}

	private void testReportOutput(final File expectedOutput, final ILearnerRunReport runReport) throws IOException {
		OpenMLAutoMLBenchmarkReport analysisReport = new OpenMLAutoMLBenchmarkReport(runReport);
		assertEquals(FileUtil.readFileAsString(expectedOutput).trim(), analysisReport.toString().trim(), expectedOutput.getAbsolutePath() + " does not match the returned report.");
	}

}
