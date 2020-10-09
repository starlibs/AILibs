package ai.libs.mlplan.wekamlplan;

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
import org.api4.java.algorithm.Timeout;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.automl.AbstractSearchSpaceConfigurationTest;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.exceptions.ComponentInstantiationFailedException;
import ai.libs.jaicore.ml.core.dataset.serialization.ArffDatasetAdapter;
import ai.libs.jaicore.ml.core.evaluation.evaluator.MonteCarloCrossValidationEvaluator;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.MonteCarloCrossValidationEvaluatorFactory;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.jaicore.timing.TimedComputation;
import ai.libs.mlplan.core.ILearnerFactory;
import ai.libs.mlplan.core.IProblemType;
import ai.libs.mlplan.weka.EMLPlanWekaProblemType;

public class MLPlanWekaSearchSpaceConfigurationTest extends AbstractSearchSpaceConfigurationTest {

	public static final String dataPath = "testrsc/car.arff";
	public static final String MSG_DISABLED = "This test is disabled for WEKA, because it does not make sense";

	public static Stream<EMLPlanWekaProblemType> getProblemTypes() {
		return Stream.of(EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS_BASE, EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS_TINY);
	}

	private ILearnerFactory<IWekaClassifier> factory;
	private MonteCarloCrossValidationEvaluator evaluator;

	@Override
	public void prepare(final IProblemType<?> problemTypeOrig) throws DatasetDeserializationFailedException, IOException, SplitFailedException, InterruptedException {
		EMLPlanWekaProblemType problemType = (EMLPlanWekaProblemType) problemTypeOrig;
		ILabeledDataset<ILabeledInstance> data = ArffDatasetAdapter.readDataset(new File(dataPath));
		data = (ILabeledDataset<ILabeledInstance>) SplitterUtil.getSimpleTrainTestSplit(data, 0, 20.0 / data.size()).get(0); // work with only 20 instances for speedup

		this.factory = problemType.getLearnerFactory();
		this.evaluator = new MonteCarloCrossValidationEvaluatorFactory().withData(data).withNumMCIterations(1).withTrainFoldSize(0.7).withMeasure(problemType.getPerformanceMetricForSearchPhase()).withRandom(new Random(42))
				.getLearnerEvaluator();
	}

	@Override
	public void execute(final IComponentInstance componentInstance) throws ComponentInstantiationFailedException, InterruptedException, AlgorithmTimeoutedException, ExecutionException {
		IWekaClassifier model = this.factory.getComponentInstantiation(componentInstance);
		this.logger.info("Evaluting classifier {}", model);
		TimedComputation.compute(new Callable<Double>() {
			@Override
			public Double call() throws Exception {
				return MLPlanWekaSearchSpaceConfigurationTest.this.evaluator.evaluate(model);
			}
		}, new Timeout(30, TimeUnit.SECONDS), "Evaluation timed out.");
	}

	@Override
	public String getReasonForFailure(final Exception e) {
		return ExceptionUtils.getStackTrace(e);
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
