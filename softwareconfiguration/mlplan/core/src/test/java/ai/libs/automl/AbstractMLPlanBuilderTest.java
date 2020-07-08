package ai.libs.automl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.Test;

import ai.libs.jaicore.basic.IOwnerBasedRandomConfig;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.MonteCarloCrossValidationEvaluatorFactory;
import ai.libs.mlplan.core.AbstractMLPlanBuilder;
import ai.libs.mlplan.core.IProblemType;
import ai.libs.mlplan.multiclass.MLPlanClassifierConfig;

public abstract class AbstractMLPlanBuilderTest {

	public abstract AbstractMLPlanBuilder<?, ?> getBuilder() throws Exception;

	public abstract IDeterministicPredictionPerformanceMeasure<?, ?> getPerformanceMeasure() throws Exception;

	@Test
	public void testSettingSeed() throws Exception {
		long seed = 99;
		AbstractMLPlanBuilder<?, ?> builder = this.getBuilder().withSeed(seed);
		assertEquals(seed, Long.parseLong(builder.getAlgorithmConfig().getProperty(IOwnerBasedRandomConfig.K_SEED)), 0.00001);
	}

	@Test
	public void testSettingPortionOfDataReservedForSelection() throws Exception {
		double portionOfDataReservedForSelection = 0.456;
		AbstractMLPlanBuilder<?, ?> builder = this.getBuilder().withPortionOfDataReservedForSelection(portionOfDataReservedForSelection);
		assertEquals(portionOfDataReservedForSelection, Double.parseDouble(builder.getAlgorithmConfig().getProperty(MLPlanClassifierConfig.SELECTION_PORTION)), 0.00001);
	}

	@Test
	public void testSettingPerformanceMeasureForSearchPhase() throws Exception {
		AbstractMLPlanBuilder<?, ?> builder = this.getBuilder().withPerformanceMeasureForSearchPhase(this.getPerformanceMeasure());
		assertEquals(this.getPerformanceMeasure(), ((MonteCarloCrossValidationEvaluatorFactory) builder.getLearnerEvaluationFactoryForSearchPhase()).getMeasure());
	}

	@Test
	public void testSettingPerformanceMeasureForSelectionPhase() throws Exception {
		AbstractMLPlanBuilder<?, ?> builder = this.getBuilder().withPerformanceMeasureForSelectionPhase(this.getPerformanceMeasure());
		assertEquals(this.getPerformanceMeasure(), ((MonteCarloCrossValidationEvaluatorFactory) builder.getLearnerEvaluationFactoryForSelectionPhase()).getMeasure());
	}

	protected void checkBuilderConfiguration(final AbstractMLPlanBuilder<?, ?> builder, final IProblemType problemType, final String datasetName)
			throws AlgorithmTimeoutedException, AlgorithmException, InterruptedException, AlgorithmExecutionCanceledException {
		assertEquals(String.format("Expected: %s, but was: %s", problemType.getName(), builder.getProblemType().getName()), problemType.getName(), builder.getProblemType().getName());
		assertTrue(builder.getSearchSpaceConfigFile().getPath().endsWith(problemType.getSearchSpaceConfigFileFromResource()));
		assertEquals(String.format("Expected: %s, but was: %s", problemType.getRequestedInterface(), builder.getRequestedInterface()), problemType.getRequestedInterface(), builder.getRequestedInterface());
		assertTrue(String.format("Expected: %s, but was: %s", problemType.getPreferredComponentListFromResource(), builder.getPreferredComponentsFiles()),
				builder.getPreferredComponentsFiles().endsWith(problemType.getPreferredComponentListFromResource()));
		assertEquals(String.format("Expected: %s, but was: %s", problemType.getPortionOfDataReservedForSelectionPhase(), builder.getPortionOfDataReservedForSelectionPhase()), problemType.getPortionOfDataReservedForSelectionPhase(),
				builder.getPortionOfDataReservedForSelectionPhase(), 0.00001);
		assertEquals(String.format("Expected: %s, but was: %s", problemType.getPerformanceMetricForSearchPhase(), ((MonteCarloCrossValidationEvaluatorFactory) builder.getLearnerEvaluationFactoryForSearchPhase()).getMeasure()),
				problemType.getPerformanceMetricForSearchPhase(), ((MonteCarloCrossValidationEvaluatorFactory) builder.getLearnerEvaluationFactoryForSearchPhase()).getMeasure());
		assertEquals(String.format("Expected: %s, but was: %s", problemType.getPerformanceMetricForSelectionPhase(), ((MonteCarloCrossValidationEvaluatorFactory) builder.getLearnerEvaluationFactoryForSelectionPhase()).getMeasure()),
				problemType.getPerformanceMetricForSelectionPhase(), ((MonteCarloCrossValidationEvaluatorFactory) builder.getLearnerEvaluationFactoryForSelectionPhase()).getMeasure());
		assertEquals(String.format("Expected: %s, but was: %s", datasetName, builder.getDataset().getRelationName()), datasetName, builder.getDataset().getRelationName());
	}

}
