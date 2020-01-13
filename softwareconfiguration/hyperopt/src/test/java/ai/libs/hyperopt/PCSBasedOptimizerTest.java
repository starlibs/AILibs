package ai.libs.hyperopt;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.junit.Before;
import org.junit.Test;

import ai.libs.hasco.model.Component;
import ai.libs.hasco.serialization.ComponentLoader;
import ai.libs.hyperopt.optimizer.BOHBOptimizer;
import ai.libs.hyperopt.optimizer.HyperBandOptimizer;
import ai.libs.hyperopt.optimizer.SMACOptimizer;
import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.graphvisualizer.events.graph.bus.AlgorithmEventListener;
import ai.libs.mlplan.core.ILearnerFactory;
import ai.libs.mlplan.multiclass.wekamlplan.weka.WekaPipelineFactory;

/**
 *
 * @author kadirayk
 *
 */
public class PCSBasedOptimizerTest {

	private static final File HASCOFileInput = new File("../mlplan/resources/automl/searchmodels/weka/autoweka.json");

	PCSBasedOptimizerInput input;
	WekaComponentInstanceEvaluator evaluator;

	@Before
	public void init() throws SplitFailedException {
		ComponentLoader cl = null;
		try {
			cl = new ComponentLoader(HASCOFileInput);
		} catch (IOException e) {

		}
		Collection<Component> components = cl.getComponents();
		String requestedInterface = "BaseClassifier";
		this.input = new PCSBasedOptimizerInput(components, requestedInterface);
		ILearnerFactory classifierFactory = new WekaPipelineFactory();
		this.evaluator = new WekaComponentInstanceEvaluator(classifierFactory, "testrsc/iris.arff", "algorithmID");
	}

	@Test
	public void tHASCOToPCSConversionTest() throws Exception {
		HASCOToPCSConverter.generatePCSFile(this.input, "output/");
		File pcsFile = new File("output/StackingEstimator.pcs");
		assertTrue(pcsFile.exists());
		String content = FileUtil.readFileAsString(pcsFile);
		assertTrue(content.contains(
				"estimator {sklearn.naive_bayes.GaussianNB,sklearn.naive_bayes.BernoulliNB,sklearn.naive_bayes.MultinomialNB,sklearn.tree.DecisionTreeClassifier,sklearn.ensemble.RandomForestClassifier,sklearn.ensemble.GradientBoostingClassifier,sklearn.neighbors.KNeighborsClassifier,sklearn.svm.LinearSVC}"));
		assertTrue(content.contains("sklearn.naive_bayes.BernoulliNB.fit_prior|estimator in {sklearn.naive_bayes.BernoulliNB}"));
	}

	@Test
	public void tPCSFileFormatTest() throws Exception {
		HASCOToPCSConverter.generatePCSFile(this.input, "output/");
		File pcsFile = new File("output/StackingEstimator.pcs");
		List<String> content = FileUtil.readFileAsList(pcsFile);
		for (String line : content) {
			if (line.contains("Conditionals:")) {
				return;
			}
			if (line.indexOf("{") != -1) { // categorical
				int curlyOpen = line.indexOf("{");
				assertEquals(line.charAt(curlyOpen - 1), " ".toCharArray()[0]); // there must be a space before opening
				// curly braces
				int curlyClose = line.indexOf("}");
				assertNotEquals(-1, curlyClose); // there must be a closing curly brace
				assertTrue(curlyClose > curlyOpen); // closing must come after opening
				int squareOpen = line.indexOf("[");
				int squareClose = line.indexOf("]");
				assertNotEquals(-1, squareOpen); // each line should have an opening square bracket
				assertNotEquals(-1, squareClose); // and a closing one
				assertTrue(squareClose > squareOpen); // closing must be after opening
			}
		}

	}

	@Test(expected = OptimizationException.class)
	public void tSMACOptimizationExceptionTest() throws Exception {
		HASCOToPCSConverter.generatePCSFile(this.input, "PCSBasedOptimizerScripts/SMACOptimizer/");
		SMACOptimizer smacOptimizer = SMACOptimizer.getSMACOptimizerBuilder(this.input, this.evaluator).executionPath("wrongPath").algoRunsTimelimit(99).runCountLimit(11).build();
		smacOptimizer.optimize("weka.classifiers.functions.Logistic");
	}

	@Test(expected = OptimizationException.class)
	public void tHyperBandOptimizationExceptionTest() throws Exception {
		HASCOToPCSConverter.generatePCSFile(this.input, "PCSBasedOptimizerScripts/HyperBandOptimizer/");
		HyperBandOptimizer optimizer = HyperBandOptimizer.getHyperBandOptimizerBuilder(this.input, this.evaluator).executionPath("wrongPath").maxBudget(230.0).minBudget(9.0).nIterations(4).build();
		optimizer.optimize("weka.classifiers.functions.Logistic");

	}

	@Test(expected = OptimizationException.class)
	public void tBOHBOptimizationExceptionTest() throws Exception {
		HASCOToPCSConverter.generatePCSFile(this.input, "PCSBasedOptimizerScripts/BOHBOptimizer/");
		BOHBOptimizer optimizer = BOHBOptimizer.getBOHBOptimizerBuilder(this.input, this.evaluator).executionPath("wrongPath").maxBudget(230.0).minBudget(9.0).nIterations(4).build();
		optimizer.optimize("weka.classifiers.functions.Logistic");

	}

	@Test
	public void spawnSMACTest() throws Exception {
		HASCOToPCSConverter.generatePCSFile(this.input, "PCSBasedOptimizerScripts/SMACOptimizer/");
		SMACOptimizer smacOptimizer = SMACOptimizer.getSMACOptimizerBuilder(this.input, this.evaluator).executionPath("PCSBasedOptimizerScripts/SMACOptimizer").algoRunsTimelimit(99).runCountLimit(11).build();
		smacOptimizer.optimize("weka.classifiers.functions.Logistic");

		// assert values from log
		List<Double> expectedValues = new ArrayList<>();
		expectedValues.add(84.4444);
		expectedValues.add(93.3333);
		expectedValues.add(82.2222);
		expectedValues.add(84.4444);
		List<String> smacOutLines = FileUtil.readFileAsList("testrsc/smac.log");
		List<Double> actualValues = new ArrayList<>();
		boolean isFinal = false;
		Double bestFoundParameterValue = 0.0;
		Double finalScore = 0.0;
		for (String line : smacOutLines) {
			if (line.contains("Challenger") && line.contains("better than incumbent")) {
				int start = line.indexOf("(");
				int end = line.indexOf(")");
				Double val = Double.valueOf(line.substring(start + 1, end));
				actualValues.add(val);
				start = line.lastIndexOf("(");
				end = line.lastIndexOf(")");
				Double val2 = Double.valueOf(line.substring(start + 1, end));
				actualValues.add(val2);
			}
			if (line.contains("INFO:	Final Incumbent: Configuration:")) {
				isFinal = true;
				continue;
			}
			if (isFinal && line.contains("weka.classifiers.functions.Logistic.R")) {
				int start = "weka.classifiers.functions.Logistic.R, Value: ".length();
				bestFoundParameterValue = Double.parseDouble(line.substring(start + 2));
			}
			if (line.contains("INFO:	Estimated cost of incumbent: ")) {
				int start = "INFO:	Estimated cost of incumbent: ".length();
				finalScore = Double.parseDouble(line.substring(start));
			}

		}
		assertArrayEquals(expectedValues.toArray(), actualValues.toArray());

		assertEquals(new Double(9.627494166784153), bestFoundParameterValue);

		// check final score is minimum
		Double minScore = actualValues.stream().min(Comparator.comparing(Double::valueOf)).get();
		minScore = Math.floor(minScore * 100) / 100; // 2 numbers after the dot
		finalScore = Math.floor(finalScore * 100) / 100;
		assertEquals(finalScore, minScore);

	}

	@Test
	public void spawnHyperBandTest() throws Exception {
		HASCOToPCSConverter.generatePCSFile(this.input, "PCSBasedOptimizerScripts/HyperBandOptimizer/");
		HyperBandOptimizer optimizer = HyperBandOptimizer.getHyperBandOptimizerBuilder(this.input, this.evaluator).executionPath("PCSBasedOptimizerScripts/HyperBandOptimizer").maxBudget(230.0).minBudget(9.0).nIterations(4).build();
		optimizer.optimize("weka.classifiers.functions.Logistic");
		List<String> hpbandOutLines = FileUtil.readFileAsList("testrsc/hpband.log");
		List<Double> expectedValues = new ArrayList<>();
		expectedValues.add(24.0);
		expectedValues.add(33.0);
		expectedValues.add(11.0);
		List<Double> actualValues = new ArrayList<>();
		List<Double> scores = new ArrayList<>();
		boolean afterScores = false;
		for (String line : hpbandOutLines) {
			if (line.startsWith("Best found ")) {
				afterScores = true;
			}
			if (!afterScores) {
				scores.add(Double.parseDouble(line.trim()));
			}
			if (line.contains("unique configurations where sampled")) {
				int start = "A total of ".length();
				int end = line.indexOf(" unique");
				double val = Double.parseDouble(line.substring(start, end));
				actualValues.add(val);
			}
			if (line.contains("runs where executed")) {
				int start = "A total of ".length();
				int end = line.indexOf(" runs");
				double val = Double.parseDouble(line.substring(start, end));
				actualValues.add(val);
			}
			if (line.contains("full function evaluations")) {
				int start = "Total budget corresponds to ".length();
				int end = line.indexOf(" full");
				double val = Double.parseDouble(line.substring(start, end));
				actualValues.add(val);
			}
		}
		assertArrayEquals(expectedValues.toArray(), actualValues.toArray());

		// check final score is minimum
		Double finalScore = scores.get(scores.size() - 1);
		Double minScore = scores.stream().min(Comparator.comparing(Double::valueOf)).get();
		assertEquals(finalScore, minScore);
	}

	@Test
	public void spawnBOHBTest() throws Exception {
		HASCOToPCSConverter.generatePCSFile(this.input, "PCSBasedOptimizerScripts/BOHBOptimizer/");
		BOHBOptimizer optimizer = BOHBOptimizer.getBOHBOptimizerBuilder(this.input, this.evaluator).executionPath("PCSBasedOptimizerScripts/BOHBOptimizer").maxBudget(230.0).minBudget(9.0).nIterations(4).build();
		optimizer.optimize("weka.classifiers.functions.Logistic");
		List<String> bohbOutLines = FileUtil.readFileAsList("testrsc/bohb.log");
		List<Double> expectedValues = new ArrayList<>();
		expectedValues.add(24.0);
		expectedValues.add(33.0);
		expectedValues.add(11.0);
		List<Double> actualValues = new ArrayList<>();
		List<Double> scores = new ArrayList<>();
		boolean afterScores = false;
		for (String line : bohbOutLines) {
			if (line.startsWith("Best found ")) {
				afterScores = true;
			}
			if (!afterScores) {
				scores.add(Double.parseDouble(line.trim()));
			}

			if (line.contains("unique configurations where sampled")) {
				int start = "A total of ".length();
				int end = line.indexOf(" unique");
				double val = Double.parseDouble(line.substring(start, end));
				actualValues.add(val);
			}
			if (line.contains("runs where executed")) {
				int start = "A total of ".length();
				int end = line.indexOf(" runs");
				double val = Double.parseDouble(line.substring(start, end));
				actualValues.add(val);
			}
			if (line.contains("full function evaluations")) {
				int start = "Total budget corresponds to ".length();
				int end = line.indexOf(" full");
				double val = Double.parseDouble(line.substring(start, end));
				actualValues.add(val);
			}
		}
		assertArrayEquals(expectedValues.toArray(), actualValues.toArray());

		// check final score is minimum
		Double finalScore = scores.get(scores.size() - 1);
		Double minScore = scores.stream().min(Comparator.comparing(Double::valueOf)).get();
		assertEquals(finalScore, minScore);
	}

	@Test
	public void tSMACBuilderTest() {
		SMACOptimizer smacOptimizer = SMACOptimizer.getSMACOptimizerBuilder(this.input, this.evaluator).executionPath("testrsc").algoRunsTimelimit(99).runCountLimit(11).alwaysRaceDefault(0).costForCrash(10.0).cutoff(0.0).deterministic(1)
				.memoryLimit(256).runCountLimit(10).wallClockLimit(10.0).build();
		smacOptimizer.setOptions();
		Map<String, String> params = ScenarioFileUtil.readAsKeyValuePairs(smacOptimizer.getExecutionPath());

		assertEquals(params.get("algo_runs_timelimit"), "99");
		assertEquals(params.get("always_race_default"), "0");
		assertEquals(params.get("cost_for_crash"), "10.0");
		assertEquals(params.get("cutoff"), "0.0");
		assertEquals(params.get("deterministic"), "1");
		assertEquals(params.get("memory_limit"), "256");
		assertEquals(params.get("runcount_limit"), "10");
		assertEquals(params.get("wallclock_limit"), "10.0");
	}

	@Test
	public void tHyperBandBuilderTest() {
		HyperBandOptimizer optimizer = HyperBandOptimizer.getHyperBandOptimizerBuilder(this.input, this.evaluator).executionPath("testsrc").minBudget(10.0).maxBudget(100.0).nIterations(10).build();
		String command = optimizer.setOptions();
		assertEquals("python HpBandSterOptimizer.py --min_budget 10.0 --max_budget 100.0 --n_iterations 10", command);
	}

	@Test
	public void tBOHBBuilderTest() {
		BOHBOptimizer optimizer = BOHBOptimizer.getBOHBOptimizerBuilder(this.input, this.evaluator).executionPath("testrsc").minBudget(10.0).maxBudget(100.0).nIterations(10).build();
		String command = optimizer.setOptions();
		assertEquals("python BOHBOptimizerRunner.py --min_budget 10.0 --max_budget 100.0 --n_iterations 10", command);
	}

	@Test
	public void tEventBusTest() throws Exception {
		HASCOToPCSConverter.generatePCSFile(this.input, "PCSBasedOptimizerScripts/HyperBandOptimizer/");
		AlgorithmEventListener listener = new PCSBasedOptimizationEventListener();
		this.evaluator.registerListener(listener);
		HyperBandOptimizer optimizer = HyperBandOptimizer.getHyperBandOptimizerBuilder(this.input, this.evaluator).executionPath("PCSBasedOptimizerScripts/HyperBandOptimizer").maxBudget(230.0).minBudget(9.0).nIterations(4).build();
		optimizer.optimize("weka.classifiers.functions.Logistic");
	}

}
