package ai.libs.automl.mlplan.multiclass.wekamlplan.sklearn;

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
import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
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
import ai.libs.jaicore.ml.scikitwrapper.ScikitLearnWrapper;
import ai.libs.jaicore.timing.TimedComputation;
import ai.libs.mlplan.multiclass.sklearn.AScikitLearnLearnerFactory;
import ai.libs.mlplan.multiclass.sklearn.EMLPlanScikitLearnProblemType;

@RunWith(Parameterized.class)
public class MLPlanScikitLearnSearchSpaceConfigurationTest extends AbstractSearchSpaceConfigurationTest {

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { //
				{ EMLPlanScikitLearnProblemType.CLASSIFICATION_MULTICLASS, "testrsc/car.arff" }, //
				{ EMLPlanScikitLearnProblemType.CLASSIFICATION_MULTICLASS_UNLIMITED_LENGTH_PIPELINES, "testrsc/car.arff" }, //
				{ EMLPlanScikitLearnProblemType.RUL, "testrsc/rul_smallExample.arff" } //
		});
	}

	private AScikitLearnLearnerFactory factory;
	private MonteCarloCrossValidationEvaluator evaluator;

	public MLPlanScikitLearnSearchSpaceConfigurationTest(final EMLPlanScikitLearnProblemType problemType, final String dataPath) throws DatasetDeserializationFailedException, IOException {
		super(problemType);
		ILabeledDataset<ILabeledInstance> data = ArffDatasetAdapter.readDataset(new File(dataPath));

		this.factory = problemType.getLearnerFactory();
		this.evaluator = new MonteCarloCrossValidationEvaluatorFactory().withData(data).withNumMCIterations(1).withTrainFoldSize(0.7).withMeasure(this.problemType.getPerformanceMetricForSearchPhase()).withRandom(new Random(42))
				.getLearnerEvaluator();
	}

	@Override
	protected boolean doesExecutionFail(final ComponentInstance componentInstance) throws ComponentInstantiationFailedException, InterruptedException {
		ScikitLearnWrapper<IPrediction, IPredictionBatch> model = this.factory.getComponentInstantiation(componentInstance);
		try {
			TimedComputation.compute(new Callable<Double>() {
				@Override
				public Double call() throws Exception {
					return MLPlanScikitLearnSearchSpaceConfigurationTest.this.evaluator.evaluate(model);
				}
			}, new Timeout(30, TimeUnit.SECONDS), "Evaluation timed out.");

		} catch (Exception e) {
			this.stringBuilder.append("\n\n========================================================================================\n");
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

			if (reason.equals("Unknown Reason")) {
				this.stringBuilder.append("Could not execute pipeline:\n");
				reason += "\n" + stackTrace;
			} else {
				this.stringBuilder.append("Invalid parameters found:\n");
			}

			this.stringBuilder.append(model);
			this.stringBuilder.append("\n");
			this.stringBuilder.append(reason);

			return true;
		}
		return false;
	}
}
