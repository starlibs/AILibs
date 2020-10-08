package ai.libs.jaicore.ml.weka.classification;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.ml.weka.WekaUtil;
import ai.libs.jaicore.ml.weka.classification.learner.WekaClassifier;
import ai.libs.jaicore.test.MediumParameterizedTest;

public class WekaClassifierInterruptionTest {

	private static final List<Integer> MS_TO_INTERRUPT = Arrays.asList(500, 1000, 5000, 10000);

	public static Stream<Arguments> getProblemSets() {
		List<Integer> timeouts = new ArrayList<>();
		timeouts.addAll(Arrays.asList(250, 500, 750, 1000));
		// IntStream.range(0, 10).map(x -> ((int) (new Random().nextDouble() * 9500.0) + 500)).forEach(timeouts::add);

		// return Stream.of(Arguments.of(new WekaClassifierProblemSet("weka.classifiers.functions.Logistic")));
		List<Arguments> argumentList = new ArrayList<>();
		for (String bl : WekaUtil.getBasicLearners()) {
			for (Integer msInterrupt : timeouts) {
				argumentList.add(Arguments.of(new WekaClassifierProblemSet(bl), msInterrupt));
			}
		}
		return argumentList.stream();
	}

	private Map<String, WekaClassifier> model = new HashMap<>();

	@MediumParameterizedTest
	@MethodSource("getProblemSets")
	public void testFitInterrupt(final WekaClassifierProblemSet problemSet, final int interrupt) throws Exception {
		Pair<String, ILabeledDataset<ILabeledInstance>> ps = problemSet.getDifficultProblemInputForGeneralTestPurposes();

		WekaClassifier classifier;
		if (!this.model.containsKey(ps.getX())) {
			classifier = new WekaClassifier(ps.getX(), new String[] {});
			try {
				classifier.fit(problemSet.getSimpleProblemInputForGeneralTestPurposes().getY());
				this.model.put(ps.getX(), classifier);
			} catch (Exception e) {
				return;
			}
		} else {
			classifier = this.model.get(ps.getX());
		}

		AtomicBoolean interruptWorked = new AtomicBoolean(false);
		Thread t = new Thread(() -> {
			try {
				long startTime = System.currentTimeMillis();
				classifier.predict(ps.getY());
				System.out.println("Took " + (System.currentTimeMillis() - startTime) + "ms to make predictions with " + ps.getX() + " for the entire batch.");
				interruptWorked.set(true);
			} catch (InterruptedException e) {
				interruptWorked.set(true);
			} catch (IllegalStateException e) {
				if (e.getCause() instanceof InterruptedException) {
					interruptWorked.set(true);
				}
			} catch (Exception e) {
				System.out.println("could not predict with model " + ps.getX());
				e.printStackTrace();
			}
		}, "Weka Classifier Fit " + ps.getX() + "-" + interrupt);
		t.start();
		Thread.sleep(interrupt);
		t.interrupt();
		Thread.sleep(200);

		if (t.isAlive()) {
			System.out.println("Still alive");
		}

		assertFalse(t.isAlive(), "Could not interrupt learner on time.");
	}

}
