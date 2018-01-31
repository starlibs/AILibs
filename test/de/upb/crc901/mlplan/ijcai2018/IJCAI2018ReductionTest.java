package de.upb.crc901.mlplan.ijcai2018;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import com.fasterxml.jackson.databind.node.ArrayNode;

import de.upb.crc901.mlplan.core.MySQLReductionExperimentLogger;
import jaicore.basic.MathExt;
import jaicore.basic.SetUtil;
import jaicore.ml.WekaUtil;
import jaicore.ml.classification.multiclass.reduction.MCTreeNodeReD;
import jaicore.ml.classification.multiclass.reduction.splitters.RPNDSplitter;
import jaicore.ml.experiments.ExperimentRunner;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.GaussianProcesses;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

public class IJCAI2018ReductionTest extends ExperimentRunner {

	MySQLReductionExperimentLogger expLogger = new MySQLReductionExperimentLogger("isys-db.cs.upb.de", "reduction", "ao4Dkf9QZw9nXWgG", "results_reduction");

	public IJCAI2018ReductionTest(File datasetFolder) {
		super(datasetFolder);
	}

	@Override
	protected String[] getClassifierNames() {
		return new String[] { "MLPlan-noprevent-" };
	}

	@Override
	protected String[] getSetupNames() {
		return new String[] { "3MCCV" };
	}

	@Override
	protected int getNumberOfRunsPerExperiment() {
		return 15;
	}

	@Override
	protected float getTrainingPortion() {
		return 0.7f;
	}

	@Override
	protected void logExperimentStart(String dataset, ArrayNode rowsForSearch, String algoName, int seed, int timeout, int numCPUs, String setupName) {
		// expLogger.createAndSetRun(dataset, rowsForSearch, algoName, seed, timeout, numCPUs, setupName);
	}

	@Override
	protected void logExperimentResult(String dataset, ArrayNode rowsForSearch, String algoName, int seed, int timeout, int numCPUs, String setupName, Classifier c,
			double errorRate) {
		// expLogger.addResultEntry(((GraphBasedPipelineSearcher<?,?,?>)c).getSelectedModel(), errorRate);
		expLogger.close();
	}

	@Override
	protected Classifier getConfiguredClassifier(int seed, String setupName, String algoName, int timeout) {
		try {

			Classifier allMCTreeNodeReDComputer = new Classifier() {

				private MCTreeNodeReD currentlyBest;

				@Override
				public Capabilities getCapabilities() {
					return null;
				}

				@Override
				public double[] distributionForInstance(Instance instance) throws Exception {
					return currentlyBest.distributionForInstance(instance);
				}

				@Override
				public double classifyInstance(Instance instance) throws Exception {
					return currentlyBest.classifyInstance(instance);
				}

				@Override
				public void buildClassifier(Instances data) throws Exception {

					Collection<String> classes = WekaUtil.getClassesActuallyContainedInDataset(data);
					Collection<String> classifiers = WekaUtil.getBasicLearners();
					Collection<List<String>> classifierCombos = SetUtil.cartesianProduct(classifiers, 3);

					ExecutorService pool = Executors.newFixedThreadPool(3);
					Random rand = new Random(seed);

					/* evaluate all combos */
					AtomicInteger comboNr = new AtomicInteger(0);
					classifierCombos.stream().forEach(combo -> pool.submit(new Runnable() {

						@Override
						public void run() {
							System.out.println((comboNr.incrementAndGet()) + "/" + classifierCombos.size() + ": " + combo);

							DescriptiveStatistics errorRates = new DescriptiveStatistics();
							DescriptiveStatistics runTimes = new DescriptiveStatistics();
							for (int i = 0; i < 10; i++) {
								try {
									System.out.print("\t" + i + ": ");
									Classifier leftClassifier = AbstractClassifier.forName(combo.get(0), null);
									Classifier rightClassifier = AbstractClassifier.forName(combo.get(2), null);

									/* determine split */
									long start = System.currentTimeMillis();
									RPNDSplitter splitter = new RPNDSplitter(data, rand);
									List<Collection<String>> classSplit = new ArrayList<>(splitter.split(classes, AbstractClassifier.forName(combo.get(1), null)));
									if ((leftClassifier instanceof GaussianProcesses && classSplit.get(0).size() > 2)
											|| (rightClassifier instanceof GaussianProcesses && classSplit.get(1).size() > 2)) {
										System.out.println("Skip, because Gaussian Processes can only handle binary");
										break;
									}

									/* create node and conduct mccv */
									MCTreeNodeReD classifier = new MCTreeNodeReD(combo.get(1), classSplit.get(0), leftClassifier, classSplit.get(1), rightClassifier);
									DescriptiveStatistics mccv = new DescriptiveStatistics();
									for (int k = 0; k < 10; k++) {
										List<Instances> dataSplit = WekaUtil.getStratifiedSplit(data, rand, .7f);
										classifier.buildClassifier(dataSplit.get(0));
										Evaluation eval = new Evaluation(dataSplit.get(0));
										eval.evaluateModel(classifier, dataSplit.get(1));
										mccv.addValue((100 - eval.pctCorrect()) / 100f);
									}

									/*  */
									double errorRate = mccv.getMean();
									long runtime = System.currentTimeMillis() - start;
									System.out.println(errorRate);
									errorRates.addValue(errorRate);
									runTimes.addValue(runtime);
								} catch (Exception e) {
									// System.err.println(e.getClass().getName() + ": " + e.getMessage());
									System.out.println("NaN");
								}
							}
							if (errorRates.getN() > 0) {
								System.out.println("Mean: " + errorRates.getMean());
								expLogger.addEvaluationEntry(data.relationName(), combo.get(0), combo.get(1), combo.get(2), errorRates, runTimes);
							} else {
								expLogger.addEvaluationEntry(data.relationName(), combo.get(0), combo.get(1), combo.get(2), errorRates, runTimes);
							}
							System.out.println(MathExt.round((comboNr.get() * 100f) / classifierCombos.size(), 2) + "% ready");
						}
					}));
					pool.shutdown();
					pool.awaitTermination(24, TimeUnit.DAYS);
				}
			};
			return allMCTreeNodeReDComputer;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
		// solutionEvaluator.getEvaluator().getMeasurementEventBus().register(expLogger);
		// return bs;
	}

	public static void main(String[] args) throws Exception {
		File folder = new File(args[0]);
		int k = Integer.valueOf(args[1]);
		ExperimentRunner runner = new IJCAI2018ReductionTest(folder);
		runner.run(k);
		System.exit(0);
	}

	@Override
	protected int[] getTimeouts() {
		return new int[] { 86400 };
	}

	@Override
	protected int getNumberOfCPUS() {
		return 4;
	}
}
