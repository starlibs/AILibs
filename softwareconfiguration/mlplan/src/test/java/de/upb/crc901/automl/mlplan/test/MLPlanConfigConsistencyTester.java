package de.upb.crc901.automl.mlplan.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import de.upb.crc901.mlplan.core.AbstractMLPlanBuilder;
import de.upb.crc901.mlplan.core.MLPlan;
import de.upb.crc901.mlplan.core.PipelineEvaluator;
import de.upb.crc901.mlplan.multiclass.MLPlanClassifierConfig;
import hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import hasco.variants.forwarddecomposition.twophase.TwoPhaseHASCO;
import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.events.AlgorithmInitializedEvent;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.AlternativeNodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomCompletionBasedNodeEvaluator;
import weka.core.Instances;

/**
 * This tester has the purpose to check whether all configuration parameters communicated to ML-Plan are really used.
 *
 * @author fmohr
 *
 */
public class MLPlanConfigConsistencyTester {

	private final TimeOut timeoutForNodeEvaluation = new TimeOut(180, TimeUnit.SECONDS);
	private final TimeOut timeoutForSingleSolutionEvaluation = new TimeOut(60, TimeUnit.SECONDS);
	private Instances data;

	@Before
	public void init() throws IOException {
		this.data = new Instances(new FileReader(new File("testrsc/car.arff")));
		this.data.setClassIndex(this.data.numAttributes() - 1);
	}

	@Test
	public void testEvaluationTimeoutsForRCNEIfSetWithBuilder() throws IOException {
		AbstractMLPlanBuilder builder = AbstractMLPlanBuilder.forWeka().withNodeEvaluationTimeOut(this.timeoutForNodeEvaluation).withCandidateEvaluationTimeOut(this.timeoutForSingleSolutionEvaluation);
		MLPlan mlplan = new MLPlan(builder, this.data);
		AlgorithmEvent event = mlplan.next();
		assertTrue(event instanceof AlgorithmInitializedEvent);
		TwoPhaseHASCO twoPhaseHasco = (TwoPhaseHASCO) mlplan.getOptimizingFactory().getOptimizer();
		RefinementConfiguredSoftwareConfigurationProblem<Double> problem = (RefinementConfiguredSoftwareConfigurationProblem<Double>) twoPhaseHasco.getHasco().getInput();
		BestFirst search = (BestFirst) twoPhaseHasco.getHasco().getSearch();
		RandomCompletionBasedNodeEvaluator rcne = (RandomCompletionBasedNodeEvaluator) ((AlternativeNodeEvaluator) search.getNodeEvaluator()).getEvaluator();
		PipelineEvaluator pe = (PipelineEvaluator) problem.getCompositionEvaluator();
		assertEquals(this.timeoutForNodeEvaluation.milliseconds(), rcne.getTimeoutForNodeEvaluationInMS());
		assertEquals(this.timeoutForSingleSolutionEvaluation.milliseconds(), pe.getTimeout(null));
	}

	@Test
	public void testEvaluationTimeoutsForRCNEIfSetInMLPlan() throws IOException {
		AbstractMLPlanBuilder builder = AbstractMLPlanBuilder.forWeka().withNodeEvaluationTimeOut(this.timeoutForNodeEvaluation).withCandidateEvaluationTimeOut(this.timeoutForSingleSolutionEvaluation);
		MLPlan mlplan = new MLPlan(builder, this.data);
		mlplan.getConfig().setProperty(MLPlanClassifierConfig.K_RANDOM_COMPLETIONS_TIMEOUT_NODE, "" + this.timeoutForNodeEvaluation.milliseconds());
		mlplan.getConfig().setProperty(MLPlanClassifierConfig.K_RANDOM_COMPLETIONS_TIMEOUT_PATH, "" + this.timeoutForSingleSolutionEvaluation.milliseconds());
		AlgorithmEvent event = mlplan.next();
		assertTrue(event instanceof AlgorithmInitializedEvent);
		TwoPhaseHASCO twoPhaseHasco = (TwoPhaseHASCO) mlplan.getOptimizingFactory().getOptimizer();
		RefinementConfiguredSoftwareConfigurationProblem<Double> problem = (RefinementConfiguredSoftwareConfigurationProblem<Double>) twoPhaseHasco.getHasco().getInput();
		BestFirst search = (BestFirst) twoPhaseHasco.getHasco().getSearch();
		RandomCompletionBasedNodeEvaluator rcne = (RandomCompletionBasedNodeEvaluator) ((AlternativeNodeEvaluator) search.getNodeEvaluator()).getEvaluator();
		PipelineEvaluator pe = (PipelineEvaluator) problem.getCompositionEvaluator();
		assertEquals(this.timeoutForNodeEvaluation.milliseconds(), rcne.getTimeoutForNodeEvaluationInMS());
		assertEquals(this.timeoutForSingleSolutionEvaluation.milliseconds(), pe.getTimeout(null));
	}

	@Test
	public void testEvaluationTimeoutsInSearchPhaseEvaluator() throws IOException {
		AbstractMLPlanBuilder builder = AbstractMLPlanBuilder.forWeka().withNodeEvaluationTimeOut(this.timeoutForNodeEvaluation).withCandidateEvaluationTimeOut(this.timeoutForSingleSolutionEvaluation);
		MLPlan mlplan = new MLPlan(builder, this.data);
		AlgorithmEvent event = mlplan.next();
		assertTrue(event instanceof AlgorithmInitializedEvent);
		TwoPhaseHASCO twoPhaseHasco = (TwoPhaseHASCO) mlplan.getOptimizingFactory().getOptimizer();
		PipelineEvaluator evaluator = (PipelineEvaluator) ((RefinementConfiguredSoftwareConfigurationProblem) twoPhaseHasco.getHasco().getInput()).getCompositionEvaluator();
		assertEquals(this.timeoutForSingleSolutionEvaluation.milliseconds(), evaluator.getTimeout(null));
	}

	@Test
	public void testThatBlowupValuesAreConsidered() throws IOException {
		for (int i = 0; i < 10; i++) {
			double blowUpSelection = Math.sqrt(i);
			double blowUpPostProcessing = Math.sqrt(i / 2.0);
			AbstractMLPlanBuilder builder = AbstractMLPlanBuilder.forWeka();
			MLPlan mlplan = new MLPlan(builder, this.data);
			mlplan.getConfig().setProperty(MLPlanClassifierConfig.K_BLOWUP_SELECTION, "" + blowUpSelection);
			mlplan.getConfig().setProperty(MLPlanClassifierConfig.K_BLOWUP_POSTPROCESS, "" + blowUpPostProcessing);

			AlgorithmEvent event = mlplan.next();
			assertTrue(event instanceof AlgorithmInitializedEvent);
			TwoPhaseHASCO twoPhaseHasco = (TwoPhaseHASCO) mlplan.getOptimizingFactory().getOptimizer();
			PipelineEvaluator evaluator = (PipelineEvaluator) ((RefinementConfiguredSoftwareConfigurationProblem) twoPhaseHasco.getHasco().getInput()).getCompositionEvaluator();
			assertEquals(blowUpSelection, mlplan.getConfig().expectedBlowupInSelection(), .001);
			assertEquals(blowUpPostProcessing, mlplan.getConfig().expectedBlowupInPostprocessing(), .001);
		}
	}
}
