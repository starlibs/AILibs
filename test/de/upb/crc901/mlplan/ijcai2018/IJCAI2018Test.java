package de.upb.crc901.mlplan.ijcai2018;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import com.fasterxml.jackson.databind.node.ArrayNode;

import de.upb.crc901.mlplan.classifiers.TwoPhaseHTNBasedPipelineSearcher;
import de.upb.crc901.mlplan.core.MySQLExperimentLogger;
import de.upb.crc901.mlplan.search.algorithms.GraphBasedPipelineSearcher;
import de.upb.crc901.mlplan.search.evaluators.BalancedRandomCompletionEvaluator;
import de.upb.crc901.mlplan.search.evaluators.MonteCarloCrossValidationEvaluator;
import jaicore.ml.experiments.ExperimentRunner;
import weka.classifiers.Classifier;

public class IJCAI2018Test extends ExperimentRunner {

	MySQLExperimentLogger expLogger = new MySQLExperimentLogger("isys-db.cs.upb.de", "mlplan", "UMJXI4WlNqbS968X", "mlplan_results");
	
	public IJCAI2018Test(File datasetFolder) {
		super(datasetFolder);
	}

	@Override
	protected String[] getClassifierNames() {
		return new String[] {"MLPlan-noprevent-"};
	}

	@Override
	protected String[] getSetupNames() {
		return new String[] { "3-70-MCCV"};
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
		expLogger.createAndSetRun(dataset, rowsForSearch, algoName, seed, timeout, numCPUs, setupName);
	}
	
	@Override
	protected void logExperimentResult(String dataset, ArrayNode rowsForSearch, String algoName, int seed, int timeout, int numCPUs, String setupName, Classifier c, double errorRate) {
		expLogger.addResultEntry(((GraphBasedPipelineSearcher<?,?,?>)c).getSelectedModel(), errorRate);
		expLogger.close();
	}

	@Override
	protected Classifier getConfiguredClassifier(int seed, String setupName, String algoName, int timeout) {
		try {
			Random random = new Random(seed);
//			TwoPhasePipelineSearcher<Double> bs = new BalancedSearcher(random, 1000 * timeout);
			TwoPhaseHTNBasedPipelineSearcher<Double> bs = new TwoPhaseHTNBasedPipelineSearcher<>();
			bs.setHtnSearchSpaceFile(new File("testrsc/automl-reduction.testset"));
			bs.setEvaluablePredicateFile(new File("testrsc/automl-reduction.evaluablepredicates"));
			bs.setRandom(random);
			bs.setTimeout(1000 * timeout);
			bs.setNumberOfCPUs(4);
			MonteCarloCrossValidationEvaluator solutionEvaluator = new MonteCarloCrossValidationEvaluator(3, .7f);
			bs.setSolutionEvaluator(solutionEvaluator);
			bs.setRce(new BalancedRandomCompletionEvaluator(random, 3, solutionEvaluator));
			bs.setTimeoutPerNodeFComputation(1000 * (timeout == 60 ? 15 : 300));
//			bs.setTooltipGenerator(new TFDTooltipGenerator<>());
			bs.setPortionOfDataForPhase2(.7f);
			bs.setExperimentLogger(expLogger);
			solutionEvaluator.getEvaluator().getMeasurementEventBus().register(expLogger);
			return bs;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		File folder = new File(args[0]);
		int k = Integer.valueOf(args[1]);
		ExperimentRunner runner = new IJCAI2018Test(folder);
		runner.run(k);
		System.exit(0);
	}

	@Override
	protected int[] getTimeouts() {
		return new int[] {60, 3600};
	}

	@Override
	protected int getNumberOfCPUS() {
		return 4;
	}
}
