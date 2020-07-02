package ai.libs.mlplan.examples.multiclass.sklearn;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;
import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassificationPredictionBatch;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;
import org.api4.java.algorithm.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.jaicore.ml.scikitwrapper.ScikitLearnWrapper;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.multiclass.sklearn.MLPlanSKLearnBuilder;

public class MLPlanSKLearnExample {

	private static final Logger L = LoggerFactory.getLogger(MLPlanSKLearnExample.class);

	private static final File DATASET = new File("testrsc/car.arff");
	private static final IDeterministicPredictionPerformanceMeasure<Integer, ISingleLabelClassification> LOSS_MEASURE = EClassificationPerformanceMeasure.ERRORRATE;

	private static final Timeout TIMEOUT = new Timeout(300, TimeUnit.SECONDS);

	public static void main(final String[] args) throws Exception {
		ILabeledDataset<?> ds = OpenMLDatasetReader.deserializeDataset(60);
		List<ILabeledDataset<?>> testSplit = SplitterUtil.getLabelStratifiedTrainTestSplit(ds, new Random(0), .7);

		MLPlanSKLearnBuilder<ISingleLabelClassification, ISingleLabelClassificationPredictionBatch> builder = new MLPlanSKLearnBuilder<>();
		builder.withTimeOut(TIMEOUT);
		builder.withNodeEvaluationTimeOut(new Timeout(90, TimeUnit.SECONDS));
		builder.withCandidateEvaluationTimeOut(new Timeout(30, TimeUnit.SECONDS));
		builder.withDataset(testSplit.get(0));

		MLPlan<ScikitLearnWrapper<ISingleLabelClassification, ISingleLabelClassificationPredictionBatch>> mlplan = builder.build();
		mlplan.setLoggerName("sklmlplanc");

		ScikitLearnWrapper<ISingleLabelClassification, ISingleLabelClassificationPredictionBatch> wrapper = mlplan.call();

		List<ISingleLabelClassification> predicted = wrapper.predict(testSplit.get(1)).stream().collect(Collectors.toList());
		List<Integer> expected = testSplit.get(1).stream().map(x -> (int) x.getLabel()).collect(Collectors.toList());
		double loss = LOSS_MEASURE.loss(expected, predicted);
		L.info("ML-Plan classifier has been chosen for dataset {} and framework SK-Learn. The measured test loss of the selected classifier is {}", DATASET.getAbsolutePath(), loss);
	}

}
