package ai.libs.automl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.execution.LearnerExecutionFailedException;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.algorithm.Timeout;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.mlplan.core.AMLPlanBuilder;
import ai.libs.mlplan.core.MLPlan;

public abstract class MLPlanResultWithMinimumQualityTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(MLPlanResultWithMinimumQualityTest.class);

	public MLPlanResultWithMinimumQualityTest() {
	}

	@ParameterizedTest(name = "Test that ML-Plan delivers a model on {0} (openml: {1})")
	@MethodSource("getBenchmark")
	public void testMinimumQualityAfter5Min(final String benchmarkName, final int openmlid, final double minQuality)
			throws SplitFailedException, InterruptedException, AlgorithmTimeoutedException, LearnerExecutionFailedException, AlgorithmException, AlgorithmExecutionCanceledException, IOException, DatasetDeserializationFailedException {
		LOGGER.info("Execute minimum quality test run for {}", benchmarkName);
		List<ILabeledDataset<? extends ILabeledInstance>> split = SplitterUtil.getLabelStratifiedTrainTestSplit(new OpenMLDatasetReader().deserializeDataset(openmlid), 0, 0.7);
		AMLPlanBuilder builder = this.getBuilder();
		builder.withDataset(split.get(0));
		builder.withTimeOut(new Timeout(300, TimeUnit.SECONDS));
		MLPlan<ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>>> mlplan = builder.build();
		ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> learner = mlplan.call();
		LOGGER.info("Evaluate ML-PLan for dataset {} with a timeout of {}s, whether it can achieve at least a loss of {}", benchmarkName, 300, minQuality);
		LOGGER.info("Loss achieved for {} is {} (min quality required: {})", benchmarkName, mlplan.getInternalValidationErrorOfSelectedClassifier(), minQuality);
		assertTrue(mlplan.getInternalValidationErrorOfSelectedClassifier() <= minQuality, "Could not achieve the required maximum loss within 300s.");
	}

	public static Stream<Arguments> getBenchmark() {
		return Stream.of();
	}

	public abstract AMLPlanBuilder getBuilder() throws IOException;

}
