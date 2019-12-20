package ai.libs.automl.mlplan.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.ml.classification.IClassifier;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.algorithm.TimeOut;

import ai.libs.automl.AutoMLAlgorithmResultProductionTester;
import ai.libs.jaicore.ml.classification.singlelabel.learner.MajorityClassifier;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.BestFirst;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.AlternativeNodeEvaluator;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.multiclass.wekamlplan.MLPlanWekaBuilder;
import ai.libs.mlplan.multiclass.wekamlplan.weka.WekaPipelineValidityCheckingNodeEvaluator;

public class WekaMLPlanResultDeliveryTester extends AutoMLAlgorithmResultProductionTester {

	@Override
	public IAlgorithm<ILabeledDataset<?>, IWekaClassifier> getAutoMLAlgorithm(final ILabeledDataset<?> data) {
		try {
			MLPlanWekaBuilder builder = new MLPlanWekaBuilder();
			int baseTime = Math.max(5, (int)Math.ceil(1.2 * this.getTrainTimeOfMajorityClassifier(data) / 1000.0));
			builder.withNodeEvaluationTimeOut(new TimeOut(baseTime * 12, TimeUnit.SECONDS));
			builder.withCandidateEvaluationTimeOut(new TimeOut(baseTime * 6, TimeUnit.SECONDS));
			builder.withNumCpus(8);
			builder.withTimeOut(new TimeOut(5 * (int)Math.pow(baseTime, 2), TimeUnit.SECONDS));
			builder.withTinyWekaSearchSpace();
			builder.withSeed(1);
			builder.withPortionOfDataReservedForSelection(.0f);
			MLPlan<IWekaClassifier> mlplan = builder.withDataset(data).build();

			/* check that ML-Plan will use a validating node evaluator */
			return mlplan;
		} catch (IOException | TrainingException | InterruptedException | DatasetDeserializationFailedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void afterInitHook(final IAlgorithm<ILabeledDataset<?>, ? extends IClassifier> algorithm) {
		MLPlan<IWekaClassifier> mlplan = (MLPlan<IWekaClassifier>)algorithm;
		BestFirst<?,?,?,?> bf = ((BestFirst<?,?,?,?>)mlplan.getSearch());
		assertNotNull(bf);
		IPathEvaluator<?, ?, ?> pe = bf.getNodeEvaluator();
		assertTrue(pe instanceof AlternativeNodeEvaluator);
		AlternativeNodeEvaluator<?, ?, ?> ape = (AlternativeNodeEvaluator<?, ?, ?>)pe;
		while (ape.getPrimaryNodeEvaluator() instanceof AlternativeNodeEvaluator<?, ?, ?>) {
			ape = (AlternativeNodeEvaluator<?, ?, ?>)ape.getPrimaryNodeEvaluator();
		}
		assertTrue("The ML-Plan searcher uses a node evaluator that does not use " + WekaPipelineValidityCheckingNodeEvaluator.class.getName() + " as its eventual primary node evaluator. The evaluator is: " + ape, ape.getPrimaryNodeEvaluator() instanceof WekaPipelineValidityCheckingNodeEvaluator);
	}

	public int getTrainTimeOfMajorityClassifier(final ILabeledDataset<?> data) throws TrainingException, InterruptedException, DatasetDeserializationFailedException {
		long start = System.currentTimeMillis();
		new MajorityClassifier().fit(data);
		return (int)(System.currentTimeMillis() - start);
	}
}
