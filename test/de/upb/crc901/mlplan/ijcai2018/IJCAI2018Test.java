package de.upb.crc901.mlplan.ijcai2018;

import java.io.File;
import java.util.Random;

import com.fasterxml.jackson.databind.node.ArrayNode;

import de.upb.crc901.automl.search.algorithms.GraphBasedPipelineSearcher;
import de.upb.crc901.mlplan.multiclass.DefaultPreorder;
import de.upb.crc901.mlplan.multiclass.MLPlanMySQLConnector;
import de.upb.crc901.mlplan.multiclass.classifiers.TwoPhaseHTNBasedPipelineSearcher;
import jaicore.ml.evaluation.MonteCarloCrossValidationEvaluator;
import jaicore.ml.evaluation.MulticlassEvaluator;
import jaicore.ml.experiments.MultiClassClassificationExperimentRunner;
import weka.classifiers.Classifier;

public class IJCAI2018Test extends MultiClassClassificationExperimentRunner {

	MLPlanMySQLConnector expLogger = new MLPlanMySQLConnector("isys-db.cs.upb.de", "mlplan", "UMJXI4WlNqbS968X", "mlplan_results");
	
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
			MonteCarloCrossValidationEvaluator solutionEvaluator = new MonteCarloCrossValidationEvaluator(new MulticlassEvaluator(random), 3, .7f);
			bs.setSolutionEvaluatorFactory4Search(() -> solutionEvaluator);
			bs.setSolutionEvaluatorFactory4Selection(() -> new MonteCarloCrossValidationEvaluator(new MulticlassEvaluator(random), 10, .7f));
			bs.setRce(new DefaultPreorder(random, 3, solutionEvaluator));
			bs.setTimeoutPerNodeFComputation(1000 * (timeout == 60 ? 15 : 300));
//			bs.setTooltipGenerator(new TFDTooltipGenerator<>());
			bs.setPortionOfDataForPhase2(.7f);
			bs.setExperimentLogger(expLogger);
			((MulticlassEvaluator)solutionEvaluator.getEvaluator()).getMeasurementEventBus().register(expLogger);
			return bs;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		File folder = new File(args[0]);
		int k = Integer.valueOf(args[1]);
		MultiClassClassificationExperimentRunner runner = new IJCAI2018Test(folder);
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
