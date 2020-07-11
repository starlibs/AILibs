package ai.libs.automl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;
import ai.libs.jaicore.ml.core.evaluation.evaluator.MonteCarloCrossValidationEvaluator;
import ai.libs.jaicore.ml.regression.loss.dataset.AsymmetricLoss;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.BestFirst;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.AlternativeNodeEvaluator;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomCompletionBasedNodeEvaluator;
import ai.libs.mlplan.core.AbstractMLPlanBuilder;
import ai.libs.mlplan.core.IProblemType;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.core.PipelineValidityCheckingNodeEvaluator;
import ai.libs.mlplan.core.PreferenceBasedNodeEvaluator;
import ai.libs.mlplan.multiclass.MLPlanClassifierConfig;

@RunWith(Parameterized.class)
public abstract class AbstractMLPlanBuilderTest {

	public abstract AbstractMLPlanBuilder<?, ?> getBuilder() throws Exception;

	@Parameter(0)
	public IProblemType<?> problemType;

	protected MLPlan<?> getMLPlanDefinedByBuilderAndProblemType() throws DatasetDeserializationFailedException, Exception {
		return this.getMLPlanForBuilder(this.getBuilder());
	}

	protected MLPlan<?> getMLPlanForBuilder(final AbstractMLPlanBuilder<?, ?> builder) throws DatasetDeserializationFailedException, Exception {
		return builder.withDataset(OpenMLDatasetReader.deserializeDataset(3)).build(); // test builds with the kr-vs-kp dataset
	}

	protected BestFirst<?, TFDNode, String, Double> getSearch(final MLPlan<?> mlplan) {
		return (BestFirst<?, TFDNode, String, Double>)mlplan.getSearch();
	}

	protected List<IPathEvaluator<TFDNode, String, Double>> getNodeEvaluatorChain(final AlternativeNodeEvaluator<TFDNode, String, Double> ane) {
		List<IPathEvaluator<TFDNode, String, Double>> list = new ArrayList<>();
		AlternativeNodeEvaluator<TFDNode, String, Double> current = ane;
		while (current.getPrimaryNodeEvaluator() instanceof AlternativeNodeEvaluator) {
			list.add(0, current.getEvaluator());
			current = (AlternativeNodeEvaluator<TFDNode, String, Double>)current.getPrimaryNodeEvaluator();
		}
		list.add(0, current.getEvaluator());
		list.add(0, current.getPrimaryNodeEvaluator());
		return list;
	}

	@Test
	public void testProblemTypeCompleteness() {
		assertNotNull(this.problemType.getLearnerFactory());
	}

	@Test
	public void testProperBuilderInitialization() throws Exception {
		AbstractMLPlanBuilder<?, ?> builder = this.getBuilder();
		assertEquals(this.problemType.getLearnerFactory(), builder.getLearnerFactory());
		assertEquals(this.problemType.getPerformanceMetricForSearchPhase(), builder.getMetricForSearchPhase());
		assertEquals(this.problemType.getPerformanceMetricForSelectionPhase(), builder.getMetricForSelectionPhase());
		assertEquals(FileUtil.readFileAsList(FileUtil.getExistingFileWithHighestPriority(this.problemType.getPreferredComponentListFromResource())), this.getBuilder().getPreferredComponents());
		Collection<Component> components = this.getBuilder().getComponents();
		for (String cName : this.getBuilder().getPreferredComponents()) {
			assertTrue("Preferred component with name " + cName + " does not exist in component repository.", components.stream().anyMatch(c -> c.getName().equals(cName)));
		}
		assertEquals(this.problemType.getRequestedInterface(), builder.getRequestedInterface());
		assertEquals(this.problemType.getSearchSelectionDatasetSplitter(), builder.getSearchSelectionDatasetSplitter());
		assertTrue("There should be no preferred node evaluators by default.", builder.getPreferredNodeEvaluators().isEmpty());
	}

	protected void checkThatFirstAndLastAreProperlyConfigured(final List<IPathEvaluator<TFDNode, String, Double>> nodeEvaluators) {
		assertTrue(nodeEvaluators.get(0) instanceof PipelineValidityCheckingNodeEvaluator);
		if (this.problemType.getPreferredComponentListFromFileSystem() != null || this.problemType.getPreferredComponentListFromResource() != null) {
			assertEquals(PreferenceBasedNodeEvaluator.class, nodeEvaluators.get(1).getClass());
		}
		else {
			assertTrue(nodeEvaluators.stream().noneMatch(ne -> ne.getClass() == PreferenceBasedNodeEvaluator.class)); // if the problem type does not specify preferred components, we do not want to see them here
		}
		assertEquals(RandomCompletionBasedNodeEvaluator.class, nodeEvaluators.get(nodeEvaluators.size() - 1).getClass());
	}

	@Test
	public void testBuildAndProperInitialization() throws Exception {
		MLPlan<?> mlplan = this.getMLPlanDefinedByBuilderAndProblemType();
		assertNotNull(mlplan);
		mlplan.next(); // initialize ML-Plan
		AlternativeNodeEvaluator<TFDNode, String, Double> mainNodeEvaluator = (AlternativeNodeEvaluator<TFDNode, String, Double>)this.getSearch(mlplan).getNodeEvaluator();
		assertEquals(AlternativeNodeEvaluator.class, mainNodeEvaluator.getClass());
		List<IPathEvaluator<TFDNode, String, Double>> nodeEvaluators = this.getNodeEvaluatorChain(mainNodeEvaluator);
		assertEquals(3, nodeEvaluators.size());
		this.checkThatFirstAndLastAreProperlyConfigured(nodeEvaluators);
	}

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
		AbstractMLPlanBuilder<?, ?> builder = this.getBuilder();
		IDeterministicPredictionPerformanceMeasure<?, ?> customMeasure = new AsymmetricLoss();
		builder.withPerformanceMeasureForSearchPhase(customMeasure);
		assertEquals(customMeasure, builder.getMetricForSearchPhase());
		MLPlan<?> mlplan = this.getMLPlanForBuilder(builder);
		mlplan.next(); // initialize
		assertEquals(customMeasure, ((MonteCarloCrossValidationEvaluator)mlplan.getClassifierEvaluatorForSearch().getBenchmark()).getMetric().getBaseMeasure());
	}

	@Test
	public void testSettingPerformanceMeasureForSelectionPhase() throws Exception {
		AbstractMLPlanBuilder<?, ?> builder = this.getBuilder();
		IDeterministicPredictionPerformanceMeasure<?, ?> customMeasure = new AsymmetricLoss();
		builder.withPerformanceMeasureForSelectionPhase(customMeasure);
		assertEquals(customMeasure, builder.getMetricForSelectionPhase());
		MLPlan<?> mlplan = this.getMLPlanForBuilder(builder);
		mlplan.next(); // initialize
		assertEquals(customMeasure, ((MonteCarloCrossValidationEvaluator)mlplan.getClassifierEvaluatorForSelection().getBenchmark()).getMetric().getBaseMeasure());
	}

	@Test
	public void testSettingTimeout() throws Exception {
		AbstractMLPlanBuilder<?, ?> builder = this.getBuilder();
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
		AbstractMLPlanBuilder<?, ?> builder = this.getBuilder();
		int numCPUs = 99;
		builder.withNumCpus(numCPUs);
		assertEquals(numCPUs, builder.getAlgorithmConfig().cpus());
		assertEquals(numCPUs, this.getMLPlanForBuilder(builder).getNumCPUs());
	}

	@Test
	public void testSettingOnePreferredNodeEvaluator() throws Exception {
		AbstractMLPlanBuilder<?, ?> builder = this.getBuilder();
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
		BestFirst<?, TFDNode, String, Double> bf = (BestFirst<?, TFDNode, String, Double>)mlplan.getSearch();
		AlternativeNodeEvaluator<TFDNode, String, Double> mainNodeEvaluator = (AlternativeNodeEvaluator<TFDNode, String, Double>)bf.getNodeEvaluator();
		assertEquals(AlternativeNodeEvaluator.class, mainNodeEvaluator.getClass());
		List<IPathEvaluator<TFDNode, String, Double>> nodeEvaluators = this.getNodeEvaluatorChain(mainNodeEvaluator);
		assertEquals(4, nodeEvaluators.size());
		this.checkThatFirstAndLastAreProperlyConfigured(nodeEvaluators);
		assertEquals(ne1, nodeEvaluators.get(2));
	}

	@Test
	public void testSettingTwoPreferredNodeEvaluators() throws Exception {
		AbstractMLPlanBuilder<?, ?> builder = this.getBuilder();
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
		BestFirst<?, TFDNode, String, Double> bf = (BestFirst<?, TFDNode, String, Double>)mlplan.getSearch();
		AlternativeNodeEvaluator<TFDNode, String, Double> mainNodeEvaluator = (AlternativeNodeEvaluator<TFDNode, String, Double>)bf.getNodeEvaluator();
		assertEquals(AlternativeNodeEvaluator.class, mainNodeEvaluator.getClass());
		List<IPathEvaluator<TFDNode, String, Double>> nodeEvaluators = this.getNodeEvaluatorChain(mainNodeEvaluator);
		assertEquals(5, nodeEvaluators.size());
		this.checkThatFirstAndLastAreProperlyConfigured(nodeEvaluators);
		assertEquals(ne1, nodeEvaluators.get(2));
		assertEquals(ne2, nodeEvaluators.get(3));
	}
}
