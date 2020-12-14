package ai.libs.mlplan.sklearnmlplan;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.algorithm.Timeout;

import ai.libs.automl.AutoMLAlgorithmForClassificationResultProductionTester;
import ai.libs.jaicore.basic.algorithm.AlgorithmCreationException;
import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.exceptions.ComponentInstantiationFailedException;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.sklearn.ScikitLearnClassifierFactory;
import ai.libs.mlplan.sklearn.builder.MLPlanScikitLearnBuilder;

public class MLPlanScikitLearnClassificationResultDeliveryTester extends AutoMLAlgorithmForClassificationResultProductionTester {

	@Override
	public IAlgorithm<ILabeledDataset<?>, ? extends ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>>> getAutoMLAlgorithm(final ILabeledDataset<?> data) throws AlgorithmCreationException, IOException {
		this.logger.info("Creating ML-Plan instance.");
		MLPlanScikitLearnBuilder builder = MLPlanScikitLearnBuilder.forClassification();
		int baseTime;
		try {
			baseTime = Math.max(5, (int) Math.ceil(1.2 * this.getTrainTimeOfMajorityClassifier(data) / 1000.0));
		} catch (TrainingException | PredictionException | InterruptedException | DatasetDeserializationFailedException | SplitFailedException | ComponentInstantiationFailedException e) {
			throw new AlgorithmCreationException(e);
		}
		assertTrue(baseTime < 60, "The majority classifier already needs too much time: " + baseTime);
		Timeout totalTimeout = new Timeout(Math.min(160, 10 + (data.size() + data.getNumAttributes()) / 1000 + 15 * baseTime), TimeUnit.SECONDS);
		this.logger.debug("Majority classifier evaluation took {}s. Total timeout will be set to {}", baseTime, totalTimeout);
		builder.withTimeOut(totalTimeout); // time out at most 100 seconds
		builder.withCandidateEvaluationTimeOut(new Timeout(totalTimeout.seconds() / 2, TimeUnit.SECONDS));
		builder.withNodeEvaluationTimeOut(new Timeout(totalTimeout.seconds(), TimeUnit.SECONDS));

		MLPlan mlplan = builder.withDataset(data).build();
		this.logger.info("Built of ML-Plan complete");
		return mlplan;
	}

	public int getTrainTimeOfMajorityClassifier(final ILabeledDataset<?> data) throws TrainingException, InterruptedException, DatasetDeserializationFailedException, SplitFailedException, PredictionException, IOException, ComponentInstantiationFailedException {
		IComponent comp = MLPlanScikitLearnBuilder.forClassification().getComponents().getComponent("sklearn.dummy.DummyClassifier");
		Map<String, String> params = new HashMap<>();
		params.put("strategy", "stratified");
		IComponentInstance inst = new ComponentInstance(comp, params, new HashMap<>());
		long start = System.currentTimeMillis();
		List<ILabeledDataset> ds = SplitterUtil.getLabelStratifiedTrainTestSplit(data, 0, .7);
		new ScikitLearnClassifierFactory().getComponentInstantiation(inst).fitAndPredict(ds.get(0), ds.get(1));
		return (int) (System.currentTimeMillis() - start);
	}

}
