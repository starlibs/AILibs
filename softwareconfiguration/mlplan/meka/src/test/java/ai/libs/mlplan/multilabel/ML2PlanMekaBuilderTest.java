package ai.libs.mlplan.multilabel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.algorithm.Timeout;
import org.junit.Test;

import ai.libs.automl.AbstractMLPlanBuilderTest;
import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.core.dataset.serialization.ArffDatasetAdapter;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.multiclass.wekamlplan.EMLPlanWekaProblemType;
import ai.libs.mlplan.multilabel.mekamlplan.ML2PlanMekaBuilder;

public class ML2PlanMekaBuilderTest extends AbstractMLPlanBuilderTest {

	private static final File TEST_SEARCHSPACE = new File("resources/automl/searchmodels/meka/test-meka-classification-pipeline.json");
	private static final File TEST_DATASET = new File("testrsc/flags.arff");
	private static final String PIPELINE = "MekaClassifier [name=ai.libs.jaicore.ml.weka.classification.pipeline.MLPipeline, options=[-W, weka.classifiers.trees.J48, --, -C, 0.25, -M, 2, -do-not-check-capabilities], wrappedClassifier=[SupervisedFilterSelector [searcher=weka.attributeSelection.BestFirst- [-D, 1, -N, 5], evaluator=weka.attributeSelection.CfsSubsetEval- [-P, 1, -E, 1]]] (preprocessors), weka.classifiers.trees.J48- [-C, 0.25, -M, 2, -do-not-check-capabilities] (classifier)]";

	@Override
	public ML2PlanMekaBuilder getBuilder() throws IOException {
		return new ML2PlanMekaBuilder();
	}

	@Override
	public EClassificationPerformanceMeasure getPerformanceMeasure() throws Exception {
		return EClassificationPerformanceMeasure.F1_WITH_1_POSITIVE;
	}

	@Test
	public void testProblemTypeClassificationAsDefault() throws Exception {
		ML2PlanMekaBuilder builder = this.getBuilder();
		builder.withDataset(ArffDatasetAdapter.readDataset(TEST_DATASET));

		this.checkBuilderConfiguration(builder, EMLPlanWekaProblemType.CLASSIFICATION_MULTILABEL, "\"flags_ml: -C -12\"");

		builder.withSearchSpaceConfigFile(TEST_SEARCHSPACE);
		assertEquals(TEST_SEARCHSPACE.getPath(), builder.getSearchSpaceConfigFile().getPath());

		builder.withTimeOut(new Timeout(18, TimeUnit.MINUTES));
		builder.withNodeEvaluationTimeOut(new Timeout(18, TimeUnit.MINUTES));
		builder.withCandidateEvaluationTimeOut(new Timeout(6, TimeUnit.MINUTES));

		MLPlan<?> mlplan = builder.build();
		try {
			ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> model = mlplan.call();
			assertEquals(PIPELINE, model.toString());
		} catch (NoSuchElementException e) {
			assertTrue(false);
		}
	}
}
