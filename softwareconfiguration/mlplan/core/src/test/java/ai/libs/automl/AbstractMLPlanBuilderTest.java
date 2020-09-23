package ai.libs.automl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;
import org.api4.java.algorithm.Timeout;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.basic.IOwnerBasedRandomConfig;
import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;
import ai.libs.jaicore.ml.core.evaluation.evaluator.MonteCarloCrossValidationEvaluator;
import ai.libs.jaicore.ml.regression.loss.dataset.AsymmetricLoss;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.BestFirst;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.AlternativeNodeEvaluator;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomCompletionBasedNodeEvaluator;
import ai.libs.mlplan.core.AMLPlanBuilder;
import ai.libs.mlplan.core.IProblemType;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.core.PipelineValidityCheckingNodeEvaluator;
import ai.libs.mlplan.core.PreferenceBasedNodeEvaluator;
import ai.libs.mlplan.multiclass.MLPlanClassifierConfig;

@RunWith(Parameterized.class)
public abstract class AbstractMLPlanBuilderTest {

	public abstract AMLPlanBuilder<?, ?> getBuilder() throws Exception;

	@Parameter(0)
	public IProblemType<?> problemType;

	protected MLPlan<?> getMLPlanDefinedByBuilderAndProblemType() throws DatasetDeserializationFailedException, Exception {
		return this.getMLPlanForBuilder(this.getBuilder());
	}

	protected MLPlan<?> getMLPlanForBuilder(final AMLPlanBuilder<?, ?> builder) throws DatasetDeserializationFailedException, Exception {
		return builder.withDataset(OpenMLDatasetReader.deserializeDataset(3)).build(); // test builds with the kr-vs-kp dataset
	}

	protected BestFirst<?, TFDNode, String, Double> getSearch(final MLPlan<?> mlplan) {
		return (BestFirst<?, TFDNode, String, Double>) mlplan.getSearch();
	}

	protected List<IPathEvaluator<TFDNode, String, Double>> getNodeEvaluatorChain(final AlternativeNodeEvaluator<TFDNode, String, Double> ane) {
		List<IPathEvaluator<TFDNode, String, Double>> list = new ArrayList<>();
		AlternativeNodeEvaluator<TFDNode, String, Double> current = ane;
		while (current.getPrimaryNodeEvaluator() instanceof AlternativeNodeEvaluator) {
			list.add(0, current.getEvaluator());
			current = (AlternativeNodeEvaluator<TFDNode, String, Double>) current.getPrimaryNodeEvaluator();
		}
		list.add(0, current.getEvaluator());
		list.add(0, current.getPrimaryNodeEvaluator());
		return list;
	}

	@Test
	public void testProblemTypeCompleteness() {
		assertNotNull("No learner factory defined for problem type " + this.problemType, this.problemType.getLearnerFactory());
		assertNotNull("No search/select splitter defined for problem type " + this.problemType, this.problemType.getSearchSelectionDatasetSplitter());
		assertNotNull("No search space config (resource) defined for problem type " + this.problemType, this.problemType.getSearchSpaceConfigFileFromResource());
		assertNotNull("No HASCO method 1 defined for problem type " + this.problemType, this.problemType.getLastHASCOMethodPriorToParameterRefinementOfBareLearner());
		assertNotNull("No HASCO method 2 defined for problem type " + this.problemType, this.problemType.getLastHASCOMethodPriorToParameterRefinementOfPipeline());
		assertNotNull("No required interface defined for problem type " + this.problemType, this.problemType.getRequestedInterface());
	}

	@Test
	public void testProperBuilderInitialization() throws Exception {
		AMLPlanBuilder<?, ?> builder = this.getBuilder();
		assertEquals(this.problemType.getLearnerFactory(), builder.getLearnerFactory());
		assertEquals(this.problemType.getPerformanceMetricForSearchPhase(), builder.getMetricForSearchPhase());
		assertEquals(this.problemType.getPerformanceMetricForSelectionPhase(), builder.getMetricForSelectionPhase());
		if (this.doesProblemTypeAdoptAvailablePreferredComponents()) {
			assertEquals(FileUtil.readFileAsList(FileUtil.getExistingFileWithHighestPriority(this.problemType.getPreferredComponentListFromResource())), this.getBuilder().getPreferredComponents());
		}
		Collection<IComponent> components = this.getBuilder().getComponents();
		if (this.getBuilder().getPreferredComponents() != null) {
			for (String cName : this.getBuilder().getPreferredComponents()) {
				assertTrue("Preferred component with name " + cName + " does not exist in component repository.", components.stream().anyMatch(c -> c.getName().equals(cName)));
			}
		}
		assertEquals(this.problemType.getRequestedInterface(), builder.getRequestedInterface());
		assertEquals(this.problemType.getSearchSelectionDatasetSplitter(), builder.getSearchSelectionDatasetSplitter());
		assertTrue("There should be no preferred node evaluators by default.", builder.getPreferredNodeEvaluators().isEmpty());
	}

	protected boolean doesProblemTypeAdoptAvailablePreferredComponents() {
		return this.problemType.getPreferredComponentListFromResource() != null || (this.problemType.getPreferredComponentListFromFileSystem() != null && new File(this.problemType.getPreferredComponentListFromFileSystem()).exists());
	}

	protected void checkThatFirstAndLastAreProperlyConfigured(final List<IPathEvaluator<TFDNode, String, Double>> nodeEvaluators) {
		int indexWherePreferredNodeEvaluatorIsExpected;
		if (this.problemType.getValidityCheckingNodeEvaluator() != null) {
			assertTrue(nodeEvaluators.get(0) instanceof PipelineValidityCheckingNodeEvaluator);
			indexWherePreferredNodeEvaluatorIsExpected = 1;
		} else {
			indexWherePreferredNodeEvaluatorIsExpected = 0;
		}
		if (this.doesProblemTypeAdoptAvailablePreferredComponents()) {
			assertEquals(PreferenceBasedNodeEvaluator.class, nodeEvaluators.get(indexWherePreferredNodeEvaluatorIsExpected).getClass());
		} else {
			assertTrue(nodeEvaluators.stream().noneMatch(ne -> ne.getClass() == PreferenceBasedNodeEvaluator.class)); // if the problem type does not specify preferred components, we do not want to see them here
		}
		assertEquals(RandomCompletionBasedNodeEvaluator.class, nodeEvaluators.get(nodeEvaluators.size() - 1).getClass());
	}

	/**
	 * Potential core node evaluators: validity checker, component preferences, and the RCNE
	 *
	 * @return
	 */
	public int getNumberOfCoreNodeEvaluators() {
		int numOfExpectedNodeEvaluators = 1; // random completion based is always there
		if (this.problemType.getValidityCheckingNodeEvaluator() != null) {
			numOfExpectedNodeEvaluators++;
		}
		if (this.doesProblemTypeAdoptAvailablePreferredComponents()) {
			numOfExpectedNodeEvaluators++;
		}
		return numOfExpectedNodeEvaluators;
	}

	@Test
	public void testBuildAndProperInitialization() throws Exception {
		MLPlan<?> mlplan = this.getMLPlanDefinedByBuilderAndProblemType();
		assertNotNull(mlplan);
		mlplan.next(); // initialize ML-Plan

		/* check node evaluators */
		int numOfExpectedNodeEvaluators = this.getNumberOfCoreNodeEvaluators();
		IPathEvaluator<TFDNode, String, Double> ne = this.getSearch(mlplan).getNodeEvaluator();
		if (numOfExpectedNodeEvaluators == 1) {
			assertEquals(RandomCompletionBasedNodeEvaluator.class, ne.getClass());
		} else {
			assertEquals(AlternativeNodeEvaluator.class, ne.getClass());
			AlternativeNodeEvaluator<TFDNode, String, Double> mainNodeEvaluator = (AlternativeNodeEvaluator<TFDNode, String, Double>) ne;
			assertEquals(AlternativeNodeEvaluator.class, mainNodeEvaluator.getClass());
			List<IPathEvaluator<TFDNode, String, Double>> nodeEvaluators = this.getNodeEvaluatorChain(mainNodeEvaluator);
			assertEquals(numOfExpectedNodeEvaluators, nodeEvaluators.size());
			this.checkThatFirstAndLastAreProperlyConfigured(nodeEvaluators);
		}
	}

	@Test
	public void testSettingSeed() throws Exception {
		long seed = 99;
		AMLPlanBuilder<?, ?> builder = this.getBuilder().withSeed(seed);
		assertEquals(seed, Long.parseLong(builder.getAlgorithmConfig().getProperty(IOwnerBasedRandomConfig.K_SEED)), 0.00001);
	}

	@Test
	public void testSettingPortionOfDataReservedForSelection() throws Exception {
		double portionOfDataReservedForSelection = 0.456;
		AMLPlanBuilder<?, ?> builder = this.getBuilder().withPortionOfDataReservedForSelection(portionOfDataReservedForSelection);
		assertEquals(portionOfDataReservedForSelection, Double.parseDouble(builder.getAlgorithmConfig().getProperty(MLPlanClassifierConfig.SELECTION_PORTION)), 0.00001);
	}

	@Test
	public void testSettingPerformanceMeasureForSearchPhase() throws Exception {
		AMLPlanBuilder<?, ?> builder = this.getBuilder();
		IDeterministicPredictionPerformanceMeasure<?, ?> customMeasure = new AsymmetricLoss();
		builder.withPerformanceMeasureForSearchPhase(customMeasure);
		assertEquals(customMeasure, builder.getMetricForSearchPhase());
		MLPlan<?> mlplan = this.getMLPlanForBuilder(builder);
		mlplan.next(); // initialize
		assertEquals(customMeasure, ((MonteCarloCrossValidationEvaluator) mlplan.getClassifierEvaluatorForSearch().getBenchmark()).getMetric().getBaseMeasure());
	}

	@Test
	public void testSettingPerformanceMeasureForSelectionPhase() throws Exception {
		AMLPlanBuilder<?, ?> builder = this.getBuilder();
		IDeterministicPredictionPerformanceMeasure<?, ?> customMeasure = new AsymmetricLoss();
		builder.withPerformanceMeasureForSelectionPhase(customMeasure);
		assertEquals(customMeasure, builder.getMetricForSelectionPhase());
		MLPlan<?> mlplan = this.getMLPlanForBuilder(builder);
		mlplan.next(); // initialize
		assertEquals(customMeasure, ((MonteCarloCrossValidationEvaluator) mlplan.getClassifierEvaluatorForSelection().getBenchmark()).getMetric().getBaseMeasure());
	}

	@Test
	public void testSettingTimeout() throws Exception {
		AMLPlanBuilder<?, ?> builder = this.getBuilder();
		Timeout to = new Timeout(4711, TimeUnit.SECONDS);
		builder.withTimeOut(to);
		assertEquals(to.milliseconds(), builder.getTimeOut().milliseconds());
		MLPlan<?> mlplan = this.getMLPlanForBuilder(builder);
		assertEquals(to.milliseconds(), mlplan.getTimeout().milliseconds());

		/* now test that the timeout for HASCO is in the range of the original timeout and that timeout minus 3 seconds */
		mlplan.next(); // initialize
		assertTrue(to.milliseconds() > mlplan.getHASCO().getTimeout().milliseconds());
		int precautionOffset = mlplan.getConfig().precautionOffset() * 1000;
		assertTrue(to.milliseconds() - precautionOffset - 2000 <= mlplan.getHASCO().getTimeout().milliseconds());
	}

	@Test
	public void testSettingCPUs() throws Exception {
		AMLPlanBuilder<?, ?> builder = this.getBuilder();
		int numCPUs = 99;
		builder.withNumCpus(numCPUs);
		assertEquals(numCPUs, builder.getAlgorithmConfig().cpus());
		assertEquals(numCPUs, this.getMLPlanForBuilder(builder).getNumCPUs());
	}

	@Test
	public void testSettingOnePreferredNodeEvaluator() throws Exception {
		AMLPlanBuilder<?, ?> builder = this.getBuilder();
		IPathEvaluator<TFDNode, String, Double> ne1 = n -> 0.0;

		/* test that the preferred node evaluators is set in the builder */
		builder.withPreferredNodeEvaluator(ne1);
		assertEquals(1, builder.getPreferredNodeEvaluators().size());
		assertEquals(ne1, builder.getPreferredNodeEvaluators().get(0));

		/* init ML-Plan */
		MLPlan<?> mlplan = this.getMLPlanForBuilder(builder);
		mlplan.next(); // initialize

		/* now check the node evaluators */
		@SuppressWarnings("unchecked")
		BestFirst<?, TFDNode, String, Double> bf = (BestFirst<?, TFDNode, String, Double>) mlplan.getSearch();
		AlternativeNodeEvaluator<TFDNode, String, Double> mainNodeEvaluator = (AlternativeNodeEvaluator<TFDNode, String, Double>) bf.getNodeEvaluator();
		assertEquals(AlternativeNodeEvaluator.class, mainNodeEvaluator.getClass());
		List<IPathEvaluator<TFDNode, String, Double>> nodeEvaluators = this.getNodeEvaluatorChain(mainNodeEvaluator);
		int numCoreNEs = this.getNumberOfCoreNodeEvaluators();
		assertEquals(numCoreNEs + 1, nodeEvaluators.size());
		this.checkThatFirstAndLastAreProperlyConfigured(nodeEvaluators);
		assertEquals(ne1, nodeEvaluators.get(nodeEvaluators.size() - 2));
	}

	@Test
	public void testSettingTwoPreferredNodeEvaluators() throws Exception {
		AMLPlanBuilder<?, ?> builder = this.getBuilder();
		IPathEvaluator<TFDNode, String, Double> ne1 = n -> 0.0;
		IPathEvaluator<TFDNode, String, Double> ne2 = n -> 1.0;

		/* test that preferred node evaluators are set in the builder */
		builder.withPreferredNodeEvaluator(ne1);
		assertEquals(1, builder.getPreferredNodeEvaluators().size());
		assertEquals(ne1, builder.getPreferredNodeEvaluators().get(0));
		builder.withPreferredNodeEvaluator(ne2);
		assertEquals(2, builder.getPreferredNodeEvaluators().size());
		assertEquals(ne1, builder.getPreferredNodeEvaluators().get(0));
		assertEquals(ne2, builder.getPreferredNodeEvaluators().get(1));

		/* init ML-Plan */
		MLPlan<?> mlplan = this.getMLPlanForBuilder(builder);
		mlplan.next(); // initialize

		/* check node evaluators */
		BestFirst<?, TFDNode, String, Double> bf = (BestFirst<?, TFDNode, String, Double>) mlplan.getSearch();
		AlternativeNodeEvaluator<TFDNode, String, Double> mainNodeEvaluator = (AlternativeNodeEvaluator<TFDNode, String, Double>) bf.getNodeEvaluator();
		assertEquals(AlternativeNodeEvaluator.class, mainNodeEvaluator.getClass());
		List<IPathEvaluator<TFDNode, String, Double>> nodeEvaluators = this.getNodeEvaluatorChain(mainNodeEvaluator);
		int numCoreNEs = this.getNumberOfCoreNodeEvaluators();
		assertEquals(numCoreNEs + 2, nodeEvaluators.size());
		this.checkThatFirstAndLastAreProperlyConfigured(nodeEvaluators);
		assertEquals(ne1, nodeEvaluators.get(nodeEvaluators.size() - 3));
		assertEquals(ne2, nodeEvaluators.get(nodeEvaluators.size() - 2));
	}
}
