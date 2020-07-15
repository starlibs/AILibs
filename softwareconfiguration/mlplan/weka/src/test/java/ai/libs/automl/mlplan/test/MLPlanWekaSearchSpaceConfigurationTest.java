package ai.libs.automl.mlplan.test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.algorithm.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ai.libs.automl.AbstractSearchSpaceConfigurationTest;
import ai.libs.jaicore.components.exceptions.ComponentInstantiationFailedException;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.ml.core.dataset.serialization.ArffDatasetAdapter;
import ai.libs.jaicore.ml.core.evaluation.evaluator.MonteCarloCrossValidationEvaluator;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.MonteCarloCrossValidationEvaluatorFactory;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.jaicore.timing.TimedComputation;
import ai.libs.mlplan.core.ILearnerFactory;
import ai.libs.mlplan.multiclass.wekamlplan.EMLPlanWekaProblemType;

@RunWith(Parameterized.class)
public class MLPlanWekaSearchSpaceConfigurationTest extends AbstractSearchSpaceConfigurationTest {

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { //
				//				{ EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS, "testrsc/car.arff" }, //
				{ EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS_TINY, "testrsc/car.arff" }, //
		});
	}

	private ILearnerFactory<IWekaClassifier> factory;
	private MonteCarloCrossValidationEvaluator evaluator;

	public MLPlanWekaSearchSpaceConfigurationTest(final EMLPlanWekaProblemType problemType, final String dataPath) throws DatasetDeserializationFailedException, IOException {
		super(problemType);
		ILabeledDataset<ILabeledInstance> data = ArffDatasetAdapter.readDataset(new File(dataPath));

		this.factory = problemType.getLearnerFactory();
		this.evaluator = new MonteCarloCrossValidationEvaluatorFactory().withData(data).withNumMCIterations(1).withTrainFoldSize(0.7).withMeasure(this.problemType.getPerformanceMetricForSearchPhase()).withRandom(new Random(42))
				.getLearnerEvaluator();
	}

	@Override
	protected boolean doesExecutionFail(final ComponentInstance componentInstance) throws ComponentInstantiationFailedException, InterruptedException {
		IWekaClassifier model = this.factory.getComponentInstantiation(componentInstance);

		try {
			TimedComputation.compute(new Callable<Double>() {
				@Override
				public Double call() throws Exception {
					return MLPlanWekaSearchSpaceConfigurationTest.this.evaluator.evaluate(model);
				}
			}, new Timeout(30, TimeUnit.SECONDS), "Evaluation timed out.");
		} catch (Exception e) {
			this.stringBuilder.append("\n\n========================================================================================\n");
			this.stringBuilder.append("Could not execute pipeline:\n");
			this.stringBuilder.append(model);
			this.stringBuilder.append("\n");
			this.stringBuilder.append("Unknown Reason\n" + ExceptionUtils.getStackTrace(e));
			return true;
		}
		return false;
	}
}
