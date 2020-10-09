package ai.libs.automl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;
import org.api4.java.algorithm.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

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

public abstract class AbstractMLPlanBuilderTest {

	public abstract AMLPlanBuilder<?, ?> getBuilder(final IProblemType<?> problemType) throws Exception;

	protected MLPlan<?> getMLPlanDefinedByBuilderAndProblemType(final IProblemType<?> problemType) throws DatasetDeserializationFailedException, Exception {
		return this.getMLPlanForBuilder(this.getBuilder(problemType));
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

	@ParameterizedTest
	@MethodSource("getProblemTypes")
	public void testProblemTypeCompleteness(final IProblemType<?> problemType) {
		assertNotNull(problemType.getLearnerFactory(), "No learner factory defined for problem type " + problemType);
		assertNotNull(problemType.getSearchSelectionDatasetSplitter(), "No search/select splitter defined for problem type " + problemType);
		assertNotNull("No search space config (resource) defined for problem type " + problemType, problemType.getSearchSpaceConfigFileFromResource());
		assertNotNull("No HASCO method 1 defined for problem type " + problemType, problemType.getLastHASCOMethodPriorToParameterRefinementOfBareLearner());
		assertNotNull("No HASCO method 2 defined for problem type " + problemType, problemType.getLastHASCOMethodPriorToParameterRefinementOfPipeline());
		assertNotNull("No required interface defined for problem type " + problemType, problemType.getRequestedInterface());
	}

	@ParameterizedTest
	@MethodSource("getProblemTypes")
	public void testProperBuilderInitialization(final IProblemType<?> problemType) throws Exception {
		AMLPlanBuilder<?, ?> builder = this.getBuilder(problemType);
		assertEquals(problemType.getLearnerFactory(), builder.getLearnerFactory());
		assertEquals(problemType.getPerformanceMetricForSearchPhase(), builder.getMetricForSearchPhase());
		assertEquals(problemType.getPerformanceMetricForSelectionPhase(), builder.getMetricForSelectionPhase());
		if (this.doesProblemTypeAdoptAvailablePreferredComponents(problemType)) {
			assertEquals(FileUtil.readFileAsList(FileUtil.getExistingFileWithHighestPriority(problemType.getPreferredComponentListFromResource())), builder.getPreferredComponents());
		}
		Collection<IComponent> components = builder.getComponents();
		//		if (builder.getPreferredComponents() != null) {
		//			for (String cName : builder.getPreferredComponents()) {
		//				assertTrue(components.stream().anyMatch(c -> c.getName().equals(cName)), "Preferred component with name " + cName + " does not exist in component repository.");
		//			}
		//		}
		assertEquals(problemType.getRequestedInterface(), builder.getRequestedInterface());
		assertEquals(problemType.getSearchSelectionDatasetSplitter(), builder.getSearchSelectionDatasetSplitter());
		assertTrue(builder.getPreferredNodeEvaluators().isEmpty(), "There should be no preferred node evaluators by default.");
	}

	protected boolean doesProblemTypeAdoptAvailablePreferredComponents(final IProblemType<?> problemType) {
		return problemType.getPreferredComponentListFromResource() != null || (problemType.getPreferredComponentListFromFileSystem() != null && new File(problemType.getPreferredComponentListFromFileSystem()).exists());
	}

	protected void checkThatFirstAndLastAreProperlyConfigured(final IProblemType<?> problemType, final List<IPathEvaluator<TFDNode, String, Double>> nodeEvaluators) {
		int indexWherePreferredNodeEvaluatorIsExpected;
		if (problemType.getValidityCheckingNodeEvaluator() != null) {
			assertTrue(nodeEvaluators.get(0) instanceof PipelineValidityCheckingNodeEvaluator);
			indexWherePreferredNodeEvaluatorIsExpected = 1;
		} else {
			indexWherePreferredNodeEvaluatorIsExpected = 0;
		}
		if (this.doesProblemTypeAdoptAvailablePreferredComponents(problemType)) {
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
	public int getNumberOfCoreNodeEvaluators(final IProblemType<?> problemType) {
		int numOfExpectedNodeEvaluators = 1; // random completion based is always there
		if (problemType.getValidityCheckingNodeEvaluator() != null) {
			numOfExpectedNodeEvaluators++;
		}
		if (this.doesProblemTypeAdoptAvailablePreferredComponents(problemType)) {
			numOfExpectedNodeEvaluators++;
		}
		return numOfExpectedNodeEvaluators;
	}

	@ParameterizedTest
	@MethodSource("getProblemTypes")
	public void testBuildAndProperInitialization(final IProblemType<?> problemType) throws Exception {
		MLPlan<?> mlplan = this.getMLPlanDefinedByBuilderAndProblemType(problemType);
		assertNotNull(mlplan);
		mlplan.next(); // initialize ML-Plan

		/* check node evaluators */
		int numOfExpectedNodeEvaluators = this.getNumberOfCoreNodeEvaluators(problemType);
		IPathEvaluator<TFDNode, String, Double> ne = this.getSearch(mlplan).getNodeEvaluator();
		if (numOfExpectedNodeEvaluators == 1) {
			assertEquals(RandomCompletionBasedNodeEvaluator.class, ne.getClass());
		} else {
			assertEquals(AlternativeNodeEvaluator.class, ne.getClass());
			AlternativeNodeEvaluator<TFDNode, String, Double> mainNodeEvaluator = (AlternativeNodeEvaluator<TFDNode, String, Double>) ne;
			assertEquals(AlternativeNodeEvaluator.class, mainNodeEvaluator.getClass());
			List<IPathEvaluator<TFDNode, String, Double>> nodeEvaluators = this.getNodeEvaluatorChain(mainNodeEvaluator);
			assertEquals(numOfExpectedNodeEvaluators, nodeEvaluators.size());
			this.checkThatFirstAndLastAreProperlyConfigured(problemType, nodeEvaluators);
		}
	}

	@ParameterizedTest
	@MethodSource("getProblemTypes")
	public void testSettingSeed(final IProblemType<?> problemType) throws Exception {
		long seed = 99;
		AMLPlanBuilder<?, ?> builder = this.getBuilder(problemType).withSeed(seed);
		assertEquals(seed, Long.parseLong(builder.getAlgorithmConfig().getProperty(IOwnerBasedRandomConfig.K_SEED)), 0.00001);
	}

	@ParameterizedTest
	@MethodSource("getProblemTypes")
	public void testSettingPortionOfDataReservedForSelection(final IProblemType<?> problemType) throws Exception {
		double portionOfDataReservedForSelection = 0.456;
		AMLPlanBuilder<?, ?> builder = this.getBuilder(problemType).withPortionOfDataReservedForSelection(portionOfDataReservedForSelection);
		assertEquals(portionOfDataReservedForSelection, Double.parseDouble(builder.getAlgorithmConfig().getProperty(MLPlanClassifierConfig.SELECTION_PORTION)), 0.00001);
	}

	@ParameterizedTest
	@MethodSource("getProblemTypes")
	public void testSettingPerformanceMeasureForSearchPhase(final IProblemType<?> problemType) throws Exception {
		AMLPlanBuilder<?, ?> builder = this.getBuilder(problemType);
		IDeterministicPredictionPerformanceMeasure<?, ?> customMeasure = new AsymmetricLoss();
		builder.withPerformanceMeasureForSearchPhase(customMeasure);
		assertEquals(customMeasure, builder.getMetricForSearchPhase());
		MLPlan<?> mlplan = this.getMLPlanForBuilder(builder);
		mlplan.next(); // initialize
		assertEquals(customMeasure, ((MonteCarloCrossValidationEvaluator) mlplan.getClassifierEvaluatorForSearch().getBenchmark()).getMetric().getBaseMeasure());
	}

	@ParameterizedTest
	@MethodSource("getProblemTypes")
	public void testSettingPerformanceMeasureForSelectionPhase(final IProblemType<?> problemType) throws Exception {
		AMLPlanBuilder<?, ?> builder = this.getBuilder(problemType);
		IDeterministicPredictionPerformanceMeasure<?, ?> customMeasure = new AsymmetricLoss();
		builder.withPerformanceMeasureForSelectionPhase(customMeasure);
		assertEquals(customMeasure, builder.getMetricForSelectionPhase());
		MLPlan<?> mlplan = this.getMLPlanForBuilder(builder);
		mlplan.next(); // initialize
		assertEquals(customMeasure, ((MonteCarloCrossValidationEvaluator) mlplan.getClassifierEvaluatorForSelection().getBenchmark()).getMetric().getBaseMeasure());
	}

	@ParameterizedTest
	@MethodSource("getProblemTypes")
	public void testSettingTimeout(final IProblemType<?> problemType) throws Exception {
		AMLPlanBuilder<?, ?> builder = this.getBuilder(problemType);
		Timeout to = new Timeout(4711, TimeUnit.SECONDS);
		builder.withTimeOut(to);
		assertEquals(to.milliseconds(), builder.getTimeOut().milliseconds());
		MLPlan<?> mlplan = this.getMLPlanForBuilder(builder);
		assertEquals(to.milliseconds(), mlplan.getTimeout().milliseconds());

		/* now test that the timeout for HASCO is in the range of the original timeout and that timeout minus 3 seconds */
		mlplan.next(); // initialize
		assertTrue(to.milliseconds() > mlplan.getHASCO().getTimeout().milliseconds());
		int precautionOffset = mlplan.getConfig().precautionOffset() * 1000;
		assertTrue(to.milliseconds() - precautionOffset - 2000 <= mlplan.getHASCO().getTimeout().milliseconds(), "Precaution offset has not been considered correctly. Required is " + to.milliseconds() + "ms minus precaution offset " + precautionOffset + " minus 2s fixed. Observed timeout in HASCO is " + mlplan.getHASCO().getTimeout().milliseconds());
	}

	@ParameterizedTest
	@MethodSource("getProblemTypes")
	public void testSettingCPUs(final IProblemType<?> problemType) throws Exception {
		AMLPlanBuilder<?, ?> builder = this.getBuilder(problemType);
		int numCPUs = 99;
		builder.withNumCpus(numCPUs);
		assertEquals(numCPUs, builder.getAlgorithmConfig().cpus());
		assertEquals(numCPUs, this.getMLPlanForBuilder(builder).getNumCPUs());
	}

	@ParameterizedTest
	@MethodSource("getProblemTypes")
	public void testSettingOnePreferredNodeEvaluator(final IProblemType<?> problemType) throws Exception {
		AMLPlanBuilder<?, ?> builder = this.getBuilder(problemType);
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
		int numCoreNEs = this.getNumberOfCoreNodeEvaluators(problemType);
		assertEquals(numCoreNEs + 1, nodeEvaluators.size());
		this.checkThatFirstAndLastAreProperlyConfigured(problemType, nodeEvaluators);
		assertEquals(ne1, nodeEvaluators.get(nodeEvaluators.size() - 2));
	}

	@ParameterizedTest
	@MethodSource("getProblemTypes")
	public void testSettingTwoPreferredNodeEvaluators(final IProblemType<?> problemType) throws Exception {
		AMLPlanBuilder<?, ?> builder = this.getBuilder(problemType);
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
		int numCoreNEs = this.getNumberOfCoreNodeEvaluators(problemType);
		assertEquals(numCoreNEs + 2, nodeEvaluators.size());
		this.checkThatFirstAndLastAreProperlyConfigured(problemType, nodeEvaluators);
		assertEquals(ne1, nodeEvaluators.get(nodeEvaluators.size() - 3));
		assertEquals(ne2, nodeEvaluators.get(nodeEvaluators.size() - 2));
	}
}
