package ai.libs.automl.mlplan.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.algorithm.Timeout;

import ai.libs.automl.AutoMLAlgorithmForClassificationResultProductionTester;
import ai.libs.jaicore.basic.algorithm.AlgorithmCreationException;
import ai.libs.jaicore.ml.classification.singlelabel.learner.MajorityClassifier;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.BestFirst;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.AlternativeNodeEvaluator;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.multiclass.wekamlplan.MLPlanWekaBuilder;
import ai.libs.mlplan.multiclass.wekamlplan.weka.WekaPipelineValidityCheckingNodeEvaluator;

public class WekaClassificationMLPlanResultDeliveryTester extends AutoMLAlgorithmForClassificationResultProductionTester {

	@Override
	public IAlgorithm<ILabeledDataset<?>, IWekaClassifier> getAutoMLAlgorithm(final ILabeledDataset<?> data) throws AlgorithmCreationException {
		try {
			this.logger.info("Creating ML-Plan instance.");
			MLPlanWekaBuilder builder = new MLPlanWekaBuilder();
			int baseTime = Math.max(5, (int) Math.ceil(1.2 * this.getTrainTimeOfMajorityClassifier(data) / 1000.0));
			assertTrue("The majority classifier already needs too much time: " + baseTime, baseTime < 60);
			Timeout totalTimeout = new Timeout(Math.min(90, (data.size() + data.getNumAttributes()) / 1000 + 10 * baseTime), TimeUnit.SECONDS);
			builder.withTimeOut(totalTimeout); // time out at most 90 seconds
			builder.withCandidateEvaluationTimeOut(new Timeout(totalTimeout.seconds() / 2, TimeUnit.SECONDS));
			builder.withNodeEvaluationTimeOut(new Timeout(totalTimeout.seconds(), TimeUnit.SECONDS));

			MLPlan<IWekaClassifier> mlplan = builder.withDataset(data).build();
			this.logger.info("Built of ML-Plan complete");
			return mlplan;
		} catch (IOException | TrainingException | DatasetDeserializationFailedException e) {
			throw new AlgorithmCreationException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt(); // re-interrupt
			fail("Thread has been interrupted.");
			return null;
		}
	}

	@Override
	public void afterInitHook(final IAlgorithm<ILabeledDataset<?>, ? extends ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>>> algorithm) {
		MLPlan<IWekaClassifier> mlplan = (MLPlan<IWekaClassifier>) algorithm;
		BestFirst<?, ?, ?, ?> bf = ((BestFirst<?, ?, ?, ?>) mlplan.getSearch());
		assertNotNull(bf);
		IPathEvaluator<?, ?, ?> pe = bf.getNodeEvaluator();
		assertTrue("Expecting the node evaluator to be an alternative node evaluator, but is of type: " + pe.getClass(), pe instanceof AlternativeNodeEvaluator);
		AlternativeNodeEvaluator<?, ?, ?> ape = (AlternativeNodeEvaluator<?, ?, ?>) pe;
		while (ape.getPrimaryNodeEvaluator() instanceof AlternativeNodeEvaluator<?, ?, ?>) {
			ape = (AlternativeNodeEvaluator<?, ?, ?>) ape.getPrimaryNodeEvaluator();
		}
		assertTrue("The ML-Plan searcher uses a node evaluator that does not use " + WekaPipelineValidityCheckingNodeEvaluator.class.getName() + " as its eventual primary node evaluator. The evaluator is: " + ape,
				ape.getPrimaryNodeEvaluator() instanceof WekaPipelineValidityCheckingNodeEvaluator);
	}

	public int getTrainTimeOfMajorityClassifier(final ILabeledDataset<?> data) throws TrainingException, InterruptedException, DatasetDeserializationFailedException {
		long start = System.currentTimeMillis();
		new MajorityClassifier().fit(data);
		return (int) (System.currentTimeMillis() - start);
	}
}
