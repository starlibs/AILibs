package ai.libs.automl;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.api4.java.ai.ml.classification.IClassifier;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.algorithm.Timeout;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.Test;

import com.google.common.eventbus.Subscribe;

import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.core.MLPlanSimpleBuilder;
import ai.libs.mlplan.core.events.MLPlanPhaseSwitchedEvent;

public class MLPlanCoreFunctionalityTester extends AutoMLAlgorithmCoreFunctionalityTester {

	@Override
	public IAlgorithm getAutoMLAlgorithm(final ILabeledDataset data) {
		return new MLPlanSimpleBuilder().withDataset(data).build();
	}

	@Test
	public void testThatPhaseSwitchEventIsSent() throws DatasetDeserializationFailedException, AlgorithmTimeoutedException, AlgorithmException, InterruptedException, AlgorithmExecutionCanceledException {
		MLPlan<IClassifier> mlplan = new MLPlanSimpleBuilder().withDataset(OpenMLDatasetReader.deserializeDataset(3)).build();
		mlplan.setTimeout(new Timeout(20, TimeUnit.SECONDS));
		AtomicBoolean eventSeen = new AtomicBoolean(false);
		mlplan.registerListener(new Object() {

			@Subscribe
			public void receivePhaseChangeEvent(final MLPlanPhaseSwitchedEvent e) {
				eventSeen.set(true);
			}
		});
		mlplan.call();
		assertTrue(eventSeen.get());
	}
}
