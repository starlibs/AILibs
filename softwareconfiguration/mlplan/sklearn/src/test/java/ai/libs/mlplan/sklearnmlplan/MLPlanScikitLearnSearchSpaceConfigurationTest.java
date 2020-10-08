package ai.libs.mlplan.sklearnmlplan;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
import org.api4.java.algorithm.Timeout;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.automl.AbstractSearchSpaceConfigurationTest;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.exceptions.ComponentInstantiationFailedException;
import ai.libs.jaicore.ml.core.dataset.serialization.ArffDatasetAdapter;
import ai.libs.jaicore.ml.core.evaluation.evaluator.MonteCarloCrossValidationEvaluator;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.MonteCarloCrossValidationEvaluatorFactory;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.jaicore.ml.scikitwrapper.ScikitLearnWrapper;
import ai.libs.jaicore.timing.TimedComputation;
import ai.libs.mlplan.core.IProblemType;
import ai.libs.mlplan.sklearn.AScikitLearnLearnerFactory;
import ai.libs.mlplan.sklearn.EMLPlanScikitLearnProblemType;

public class MLPlanScikitLearnSearchSpaceConfigurationTest extends AbstractSearchSpaceConfigurationTest {

	public static Stream<EMLPlanScikitLearnProblemType> getProblemTypes() {
		return Stream.of(EMLPlanScikitLearnProblemType.CLASSIFICATION_MULTICLASS, EMLPlanScikitLearnProblemType.CLASSIFICATION_MULTICLASS_UNLIMITED_LENGTH_PIPELINES, EMLPlanScikitLearnProblemType.RUL);
	}

	public static final String MSG_DISABLED = "This test is disabled for WEKA, because it does not make sense";

	public static final String DATASET_DEFAULT = "testrsc/car.arff";
	public static final String DATASET_RUL = "testrsc/rul_smallExample.arff";

	private AScikitLearnLearnerFactory factory;
	private MonteCarloCrossValidationEvaluator evaluator;

	@Override
	public void prepare(final IProblemType<?> problemTypeOrig) throws DatasetDeserializationFailedException, IOException, SplitFailedException, InterruptedException {
		EMLPlanScikitLearnProblemType problemType = (EMLPlanScikitLearnProblemType) problemTypeOrig;
		String dataPath = (problemType == EMLPlanScikitLearnProblemType.RUL) ? DATASET_RUL : DATASET_DEFAULT;
		ILabeledDataset<ILabeledInstance> data = ArffDatasetAdapter.readDataset(new File(dataPath));

		if (data.size() > 20) {// ensure that the dataset has at maximum 20 instances (for speedup)
			data = (ILabeledDataset<ILabeledInstance>) SplitterUtil.getSimpleTrainTestSplit(data, 0, 20.0 / data.size()).get(0);
		}

		this.factory = problemType.getLearnerFactory();
		this.evaluator = new MonteCarloCrossValidationEvaluatorFactory().withData(data).withNumMCIterations(1).withTrainFoldSize(0.7).withMeasure(problemType.getPerformanceMetricForSearchPhase()).withRandom(new Random(42))
				.getLearnerEvaluator();
	}

	@Override
	public void execute(final IComponentInstance componentInstance) throws ComponentInstantiationFailedException, InterruptedException, AlgorithmTimeoutedException, ExecutionException {
		ScikitLearnWrapper<IPrediction, IPredictionBatch> model = this.factory.getComponentInstantiation(componentInstance);
		TimedComputation.compute(new Callable<Double>() {
			@Override
			public Double call() throws Exception {
				return MLPlanScikitLearnSearchSpaceConfigurationTest.this.evaluator.evaluate(model);
			}
		}, new Timeout(30, TimeUnit.SECONDS), "Evaluation timed out.");
	}

	@Override
	public String getReasonForFailure(final Exception e) {
		String stackTrace = ExceptionUtils.getStackTrace(e);
		String reason = "Unknown Reason";
		if (stackTrace.contains("ValueError")) {
			String valueError = stackTrace.substring(stackTrace.indexOf("ValueError") + 11);
			valueError = valueError.substring(0, valueError.indexOf("\n")).trim();
			reason = "Unknown value " + valueError;
		} else if (stackTrace.contains("KeyError")) {
			String key = stackTrace.substring(stackTrace.indexOf("KeyError") + 9);
			key = key.substring(0, key.indexOf("\n")).trim();
			reason = "Unknown key " + key;
		} else if (stackTrace.contains("TypeError")) {
			String type = stackTrace.substring(stackTrace.indexOf("TypeError") + 10);
			type = type.substring(0, type.indexOf("\n")).trim();
			reason = "Unknown type: " + type;
		}
		return reason;
	}

	@Override
	public void testExecutabilityOfDefaultConfigs(final IProblemType<?> problemType) throws Exception {
		if (problemType == EMLPlanScikitLearnProblemType.CLASSIFICATION_MULTICLASS_UNLIMITED_LENGTH_PIPELINES) {
			this.logger.warn(MSG_DISABLED); // unlimited length pipelines cannot be tested this way!
			return;
		}
		super.testExecutabilityOfDefaultConfigs(problemType);
	}

	@Override
	public void testExecutabilityOfMinConfigs(final IProblemType<?> problemType) throws Exception {
		this.logger.warn(MSG_DISABLED);
		assertTrue(true);
	}

	@Override
	public void testExecutabilityOfMaxConfigs(final IProblemType<?> problemType) throws Exception {
		this.logger.warn(MSG_DISABLED);
		assertTrue(true);
	}

	@Override
	public void testExecutabilityOfCatConfigs(final IProblemType<?> problemType) throws Exception {
		this.logger.warn(MSG_DISABLED);
		assertTrue(true);
	}
}
