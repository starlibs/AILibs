package ai.libs.automl.mlplan.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.api4.java.algorithm.Timeout;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.junit.Before;
import org.junit.Test;

import ai.libs.hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.hasco.variants.forwarddecomposition.twophase.TwoPhaseHASCO;
import ai.libs.jaicore.basic.algorithm.AlgorithmInitializedEvent;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.BestFirst;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.AlternativeNodeEvaluator;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomCompletionBasedNodeEvaluator;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.core.PipelineEvaluator;
import ai.libs.mlplan.multiclass.MLPlanClassifierConfig;
import ai.libs.mlplan.multiclass.wekamlplan.MLPlanWekaBuilder;
import weka.core.Instances;

/**
 * This tester has the purpose to check whether all configuration parameters communicated to ML-Plan are really used.
 *
 * @author fmohr
 *
 */
public class MLPlanConfigConsistencyTester {

	private final Timeout timeoutForNodeEvaluation = new Timeout(180, TimeUnit.SECONDS);
	private final Timeout timeoutForSingleSolutionEvaluation = new Timeout(60, TimeUnit.SECONDS);
	private Instances data;

	@Before
	public void init() throws IOException {
		this.data = new Instances(new FileReader(new File("testrsc/car.arff"))); // read the file intentionally into the Instances format!
		this.data.setClassIndex(this.data.numAttributes() - 1);
		System.out.println("Loaded dataset with " + this.data.size() + " instances.");
	}

	@Test
	public void testEvaluationTimeoutsForRCNEIfSetWithBuilder() throws IOException {
		MLPlanWekaBuilder builder = new MLPlanWekaBuilder().withNodeEvaluationTimeOut(this.timeoutForNodeEvaluation).withCandidateEvaluationTimeOut(this.timeoutForSingleSolutionEvaluation);
		MLPlan<IWekaClassifier> mlplan = builder.withDataset(new WekaInstances(this.data)).build();
		IAlgorithmEvent event = mlplan.next();
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
		MLPlanWekaBuilder builder = new MLPlanWekaBuilder().withNodeEvaluationTimeOut(this.timeoutForNodeEvaluation).withCandidateEvaluationTimeOut(this.timeoutForSingleSolutionEvaluation);
		MLPlan<IWekaClassifier> mlplan = builder.withDataset(new WekaInstances(this.data)).build();
		mlplan.getConfig().setProperty(MLPlanClassifierConfig.K_RANDOM_COMPLETIONS_TIMEOUT_NODE, "" + this.timeoutForNodeEvaluation.milliseconds());
		mlplan.getConfig().setProperty(MLPlanClassifierConfig.K_RANDOM_COMPLETIONS_TIMEOUT_PATH, "" + this.timeoutForSingleSolutionEvaluation.milliseconds());
		IAlgorithmEvent event = mlplan.next();
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
		MLPlanWekaBuilder builder = new MLPlanWekaBuilder().withNodeEvaluationTimeOut(this.timeoutForNodeEvaluation).withCandidateEvaluationTimeOut(this.timeoutForSingleSolutionEvaluation);
		MLPlan<IWekaClassifier> mlplan = builder.withDataset(new WekaInstances(this.data)).build();
		IAlgorithmEvent event = mlplan.next();
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
			MLPlanWekaBuilder builder = new MLPlanWekaBuilder();
			MLPlan<IWekaClassifier> mlplan = builder.withDataset(new WekaInstances(this.data)).build();
			mlplan.getConfig().setProperty(MLPlanClassifierConfig.K_BLOWUP_SELECTION, "" + blowUpSelection);
			mlplan.getConfig().setProperty(MLPlanClassifierConfig.K_BLOWUP_POSTPROCESS, "" + blowUpPostProcessing);

			IAlgorithmEvent event = mlplan.next();
			assertTrue(event instanceof AlgorithmInitializedEvent);
			TwoPhaseHASCO twoPhaseHasco = (TwoPhaseHASCO) mlplan.getOptimizingFactory().getOptimizer();
			PipelineEvaluator evaluator = (PipelineEvaluator) ((RefinementConfiguredSoftwareConfigurationProblem) twoPhaseHasco.getHasco().getInput()).getCompositionEvaluator();
			assertEquals(blowUpSelection, mlplan.getConfig().expectedBlowupInSelection(), .001);
			assertEquals(blowUpPostProcessing, mlplan.getConfig().expectedBlowupInPostprocessing(), .001);
		}
	}
}
