package ai.libs.automl;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.api4.java.ai.ml.classification.IClassifier;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.algorithm.Timeout;
import org.junit.Test;

import ai.libs.jaicore.graphvisualizer.events.recorder.AlgorithmEventHistory;
import ai.libs.jaicore.graphvisualizer.events.recorder.AlgorithmEventHistoryRecorder;
import ai.libs.jaicore.graphvisualizer.events.recorder.AlgorithmEventHistorySerializer;
import ai.libs.jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoAlgorithmEventPropertyComputer;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.jaicore.search.gui.plugins.rollouthistograms.RolloutInfoAlgorithmEventPropertyComputer;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.core.MLPlanSimpleBuilder;

public class MLPlanAlgorithmEventHistorySerializationTest {

	@Test
	public void testSerializationAndDeserializationOfAlgorithmEventHistoryOfMLPlan() throws Exception {

		/* load data for segment dataset and create a train-test-split */
		ILabeledDataset<?> ds = OpenMLDatasetReader.deserializeDataset(30);
		List<ILabeledDataset<?>> split = SplitterUtil.getLabelStratifiedTrainTestSplit(ds, new Random(0), .7);

		/* initialize mlplan with a tiny search space, and let it run for 30 seconds */
		MLPlanSimpleBuilder builder = new MLPlanSimpleBuilder();
		builder.withNodeEvaluationTimeOut(new Timeout(30, TimeUnit.SECONDS));
		builder.withCandidateEvaluationTimeOut(new Timeout(10, TimeUnit.SECONDS));
		builder.withTimeOut(new Timeout(90, TimeUnit.SECONDS));
		builder.withNumCpus(2);

		MLPlan<IClassifier> mlplan = builder.withDataset(split.get(0)).build();
		mlplan.setPortionOfDataForPhase2(0f);
		mlplan.setLoggerName("mlplan");

		NodeInfoAlgorithmEventPropertyComputer nodeInfoAlgorithmEventPropertyComputer = new NodeInfoAlgorithmEventPropertyComputer();
		List<AlgorithmEventPropertyComputer> algorithmEventPropertyComputers = Arrays.asList(nodeInfoAlgorithmEventPropertyComputer, new RolloutInfoAlgorithmEventPropertyComputer());

		AlgorithmEventHistoryRecorder recorder = new AlgorithmEventHistoryRecorder(algorithmEventPropertyComputers);
		mlplan.registerListener(recorder);

		mlplan.call();

		AlgorithmEventHistorySerializer eventHistorySerializer = new AlgorithmEventHistorySerializer();
		String serializedAlgorithmEventHistory = eventHistorySerializer.serializeAlgorithmEventHistory(recorder.getHistory());

		AlgorithmEventHistory deserializedAlgorithmEventHistory = new AlgorithmEventHistorySerializer().deserializeAlgorithmEventHistory(serializedAlgorithmEventHistory);

		assertEquals(recorder.getHistory(), deserializedAlgorithmEventHistory);
	}
}
