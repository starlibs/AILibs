package ai.libs.automl.mlplan.test;

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
import ai.libs.mlplan.multiclass.wekamlplan.MLPlanWekaBuilder;

public class MLPlanWekaBuilderTest extends AbstractMLPlanBuilderTest {

	private static final File CLASSIFICATION_TEST_SEARCHSPACE = new File("resources/automl/searchmodels/weka/test-weka-classification-pipeline.json");
	private static final File CLASSIFICATION_TEST_DATASET = new File("testrsc/car.arff");
	private static final String CLASSIFICATION_REQUESTED_INTERFACE = "MLPipeline";
	private static final String CLASSIFICATION_PIPELINE = "WekaClassifier [name=ai.libs.jaicore.ml.weka.classification.pipeline.MLPipeline, options=[-W, weka.classifiers.trees.J48, --, -C, 0.25, -M, 2, -do-not-check-capabilities], wrappedClassifier=[SupervisedFilterSelector [searcher=weka.attributeSelection.BestFirst- [-D, 1, -N, 5], evaluator=weka.attributeSelection.CfsSubsetEval- [-P, 1, -E, 1]]] (preprocessors), weka.classifiers.trees.J48- [-C, 0.25, -M, 2, -do-not-check-capabilities] (classifier)]";

	@Override
	public MLPlanWekaBuilder getBuilder() throws IOException {
		return new MLPlanWekaBuilder();
	}

	@Override
	public EClassificationPerformanceMeasure getPerformanceMeasure() throws Exception {
		return EClassificationPerformanceMeasure.F1_WITH_1_POSITIVE;
	}

	@Test
	public void testProblemTypeClassificationAsDefault() throws Exception {
		MLPlanWekaBuilder builder = this.getBuilder();
		builder.withDataset(ArffDatasetAdapter.readDataset(CLASSIFICATION_TEST_DATASET));

		this.checkBuilderConfiguration(builder, EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS, "car");

		builder.withSearchSpaceConfigFile(CLASSIFICATION_TEST_SEARCHSPACE);
		assertEquals(CLASSIFICATION_TEST_SEARCHSPACE.getPath(), builder.getSearchSpaceConfigFile().getPath());

		builder.withRequestedInterface(CLASSIFICATION_REQUESTED_INTERFACE);
		builder.withTimeOut(new Timeout(15, TimeUnit.SECONDS));
		builder.withNodeEvaluationTimeOut(new Timeout(15, TimeUnit.SECONDS));
		builder.withCandidateEvaluationTimeOut(new Timeout(10, TimeUnit.SECONDS));

		MLPlan<?> mlplan = builder.build();
		try {
			ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> model = mlplan.call();
			assertEquals(CLASSIFICATION_PIPELINE, model.toString());
		} catch (NoSuchElementException e) {
			assertTrue(false);
		}
	}

}
