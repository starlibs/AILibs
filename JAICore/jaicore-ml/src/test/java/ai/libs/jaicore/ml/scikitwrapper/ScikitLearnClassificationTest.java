package ai.libs.jaicore.ml.scikitwrapper;

import java.io.IOException;
import java.util.List;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.ai.ml.core.evaluation.execution.LearnerExecutionFailedException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;
import ai.libs.jaicore.ml.core.evaluation.evaluator.SupervisedLearnerExecutor;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.jaicore.ml.scikitwrapper.simple.SimpleScikitLearnClassifier;

public class ScikitLearnClassificationTest {

	private static ILabeledDataset<ILabeledInstance> datasetIris;

	@BeforeAll
	public static void setup() throws DatasetDeserializationFailedException {
		datasetIris = OpenMLDatasetReader.deserializeDataset(40975);
	}

	@Test
	public void testClassificationAccuracyRFOnIris() throws IOException, SplitFailedException, InterruptedException, LearnerExecutionFailedException {
		SimpleScikitLearnClassifier slw = new SimpleScikitLearnClassifier("RandomForestClassifier()", "from sklearn.ensemble import RandomForestClassifier");
		List<ILabeledDataset<?>> split = SplitterUtil.getSimpleTrainTestSplit(datasetIris, 42, .7);
		ILearnerRunReport report = new SupervisedLearnerExecutor().execute(slw, split.get(0), split.get(1));

		System.out.println(EClassificationPerformanceMeasure.ERRORRATE.loss(report.getPredictionDiffList().getCastedView(Integer.class, ISingleLabelClassification.class)));
	}

}
