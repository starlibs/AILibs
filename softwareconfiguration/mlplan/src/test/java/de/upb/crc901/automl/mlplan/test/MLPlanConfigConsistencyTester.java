package de.upb.crc901.automl.mlplan.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import de.upb.crc901.mlplan.core.MLPlan;
import de.upb.crc901.mlplan.core.MLPlanBuilder;
import de.upb.crc901.mlplan.core.SearchPhasePipelineEvaluator;
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
		data = new Instances(new FileReader(new File("testrsc/car.arff")));
		data.setClassIndex(data.numAttributes() - 1);
	}

	@Test
	public void testEvaluationTimeoutsForRCNE() throws IOException {
		MLPlanBuilder builder = new MLPlanBuilder().withAutoWEKAConfiguration().withRandomCompletionBasedBestFirstSearch();
		builder.withTimeoutForNodeEvaluation(timeoutForNodeEvaluation);
		builder.withTimeoutForSingleSolutionEvaluation(timeoutForSingleSolutionEvaluation);
		MLPlan mlplan = new MLPlan(builder, data);
		AlgorithmEvent event = mlplan.next();
		assertTrue(event instanceof AlgorithmInitializedEvent);
		TwoPhaseHASCO twoPhaseHasco = (TwoPhaseHASCO)mlplan.getOptimizingFactory().getOptimizer();
		BestFirst search = (BestFirst)twoPhaseHasco.getHasco().getSearch();
		RandomCompletionBasedNodeEvaluator rcne = (RandomCompletionBasedNodeEvaluator)((AlternativeNodeEvaluator)search.getNodeEvaluator()).getEvaluator();
		assertEquals(timeoutForNodeEvaluation.milliseconds(), rcne.getTimeoutForNodeEvaluationInMS());
	}
	
	@Test
	public void testEvaluationTimeoutsInSearchPhaseEvaluator() throws IOException {
		MLPlanBuilder builder = new MLPlanBuilder().withAutoWEKAConfiguration().withRandomCompletionBasedBestFirstSearch();
		builder.withTimeoutForNodeEvaluation(timeoutForNodeEvaluation);
		builder.withTimeoutForSingleSolutionEvaluation(timeoutForSingleSolutionEvaluation);
		MLPlan mlplan = new MLPlan(builder, data);
		AlgorithmEvent event = mlplan.next();
		assertTrue(event instanceof AlgorithmInitializedEvent);
		TwoPhaseHASCO twoPhaseHasco = (TwoPhaseHASCO)mlplan.getOptimizingFactory().getOptimizer();
		SearchPhasePipelineEvaluator evaluator = (SearchPhasePipelineEvaluator)((RefinementConfiguredSoftwareConfigurationProblem)twoPhaseHasco.getHasco().getInput()).getCompositionEvaluator();
		assertEquals(timeoutForSingleSolutionEvaluation.milliseconds(), evaluator.getConfig().getTimeoutForSolutionEvaluation());
	}
}
