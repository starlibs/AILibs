package ai.libs.automl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.algorithm.Timeout;
import org.junit.jupiter.api.Test;

import com.google.common.eventbus.Subscribe;

import ai.libs.hasco.core.events.TwoPhaseHASCOPhaseSwitchEvent;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;
import ai.libs.jaicore.ml.core.evaluation.evaluator.events.TrainTestSplitEvaluationCompletedEvent;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.core.MLPlanSimpleBuilder;

/**
 * Here we test, that certain behavior special to ML-Plan is conducted correctly
 *
 * @author Felix Mohr
 *
 */
public class MLPlanProprietaryPropertyTester {

	@Test
	public void testThatSelectionPhaseUsesSupersetOfSearchData() throws Exception {
		final ILabeledDataset<?> ds = OpenMLDatasetReader.deserializeDataset(30);
		MLPlanSimpleBuilder builder = new MLPlanSimpleBuilder().withPortionOfDataReservedForSelection(.3).withTimeOut(new Timeout(30, TimeUnit.SECONDS)).withDataset(ds);
		final MLPlan<?> mlplan = builder.build();
		final int dataSizeSearchPhase = (int) Math.round(ds.size() * .7);
		final int dataSizeSelectionPhase = ds.size();
		final int trainDataSizeSearchPhase = (int) Math.round(dataSizeSearchPhase * .7);
		final int testDataSizeSearchPhase = dataSizeSearchPhase - trainDataSizeSearchPhase;
		final int trainDataSizeSelectionPhase = (int) Math.round(dataSizeSelectionPhase * .7);
		final int testDataSizeSelectionPhase = dataSizeSelectionPhase - trainDataSizeSelectionPhase;
		System.out.println("Expecting data size during search being " + dataSizeSearchPhase + " with splits " + trainDataSizeSearchPhase + "/" + testDataSizeSearchPhase);
		System.out.println("Expecting data size during selection being " + dataSizeSelectionPhase + " with splits " + trainDataSizeSelectionPhase + "/" + testDataSizeSelectionPhase);
		mlplan.setLoggerName("testedalgorithm");

		AtomicBoolean observedFailure = new AtomicBoolean();

		/* register a listener  */
		mlplan.registerListener(new Object() {

			private boolean isInSelectionPhase;

			@Subscribe
			public void receiveEvent(final TwoPhaseHASCOPhaseSwitchEvent e) {
				this.isInSelectionPhase = true;
				System.out.println("SWITCHING TO SELECTION PHASE!");
			}

			@Subscribe
			public void receiveEvent(final TrainTestSplitEvaluationCompletedEvent<?, ?> e) { // this event is fired whenever any pipeline is evaluated successfully
				if (!this.isInSelectionPhase) {
					assertEquals(trainDataSizeSearchPhase, e.getReport().getTrainSet().size());
					assertEquals(testDataSizeSearchPhase, e.getReport().getTestSet().size());
				} else {
					try {
						assertEquals(trainDataSizeSelectionPhase, e.getReport().getTrainSet().size());
						assertEquals(testDataSizeSelectionPhase, e.getReport().getTestSet().size());
					} catch (Exception ex) {
						observedFailure.set(true);
					}
				}
			}
		});
		mlplan.call();
		if (observedFailure.get()) {
			fail("Failure observed.");
		}
	}
}
