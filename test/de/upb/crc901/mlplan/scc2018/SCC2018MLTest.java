package de.upb.crc901.mlplan.scc2018;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.upb.crc901.mlplan.classifiers.TwoPhaseHTNBasedPipelineSearcher;
import de.upb.crc901.mlplan.core.MLUtil;
import de.upb.crc901.mlplan.core.MySQLMLPlanExperimentLogger;
import de.upb.crc901.mlplan.search.evaluators.BalancedRandomCompletionEvaluator;
import de.upb.crc901.mlplan.search.evaluators.MonteCarloCrossValidationEvaluator;
import de.upb.crc901.mlplan.search.evaluators.MulticlassEvaluator;
import de.upb.crc901.services.core.HttpServiceServer;
import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.ml.experiments.MultiClassClassificationExperimentRunner;
import jaicore.ml.measures.PMMulticlass;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.graphgenerators.task.tfd.TFDTooltipGenerator;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import weka.classifiers.Classifier;

public class SCC2018MLTest extends MultiClassClassificationExperimentRunner {
	
	private static final int[] timeouts = new int[] { 60 };
	private static final int seeds = 10;
	private static final float trainingPortion = 0.7f;

	private static final int numCPUs = 5;
	private static final int memoryInMB = 1000+(int)(Math.random()*2000);
	
	private final MySQLMLPlanExperimentLogger logger; // we want to have the logger, because we also send 
	
	protected static String[] getClassifierNames() {
		return new String[] { "MLS-Plan" };
	}

	protected static Map<String,String[]> getSetupNames() {
		Map<String,String[]> algoModes = new HashMap<>();
		algoModes.put("MLS-Plan", new String[] { "3-70-MCCV" });
		return algoModes;
	}
	
	public SCC2018MLTest(File datasetFolder) throws IOException {
		super(datasetFolder, getClassifierNames(), getSetupNames(), timeouts, seeds, trainingPortion, numCPUs, memoryInMB, PMMulticlass.errorRate, new MySQLMLPlanExperimentLogger("isys-db.cs.upb.de", "mlplan", "UMJXI4WlNqbS968X", "mlplan_results_test"));
		this.logger = (MySQLMLPlanExperimentLogger)getLogger();
	}

	@Override
	protected Classifier getConfiguredClassifier(int seed, String algoName, String algoMode, int timeoutInSeconds, int numberOfCPUs, int memoryInMB, PMMulticlass performanceMeasure) {
		try {
			switch (algoName) {
			case "MLS-Plan": {

				File evaluablePredicatFile = new File("testrsc/services/automl.evaluablepredicates");
				File searchSpaceFile = new File("testrsc/services/automl-services.searchspace");
				TwoPhaseHTNBasedPipelineSearcher<Double> bs = new TwoPhaseHTNBasedPipelineSearcher<>();
				
				logicalDerivationTree(searchSpaceFile, evaluablePredicatFile);
				
				Random random = new Random(seed);
				bs.setHtnSearchSpaceFile(searchSpaceFile);
				//bs.setHtnSearchSpaceFile(new File("testrsc/automl3.testset"));
				bs.setEvaluablePredicateFile(evaluablePredicatFile);
				bs.setRandom(random);
				bs.setTimeout(1000 * timeoutInSeconds);
				bs.setNumberOfCPUs(numberOfCPUs);
				bs.setMemory(memoryInMB);
				MulticlassEvaluator evaluator = new MulticlassEvaluator(random);
				bs.setSolutionEvaluatorFactory4Search(() -> new MonteCarloCrossValidationEvaluator(evaluator, 3, .7f));
				bs.setSolutionEvaluatorFactory4Selection(() -> new MonteCarloCrossValidationEvaluator(evaluator, 10, .7f));
				bs.setRce(new BalancedRandomCompletionEvaluator(random, 3, new MonteCarloCrossValidationEvaluator(evaluator, 3, .7f)));
//				bs.setTimeoutPerNodeFComputation(1000 * (timeoutInSeconds == 60 ? 15 : 300));
				bs.setTimeoutPerNodeFComputation(3000);
				bs.setTooltipGenerator(new TFDTooltipGenerator<>());
				bs.setPortionOfDataForPhase2(.3f);
				
				bs.setExperimentLogger(logger);
				evaluator.getMeasurementEventBus().register(logger);
				return bs;
			}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	private void logicalDerivationTree(File searchSpaceFile, File evaluablePredicatFile) throws IOException {

		 ORGraphSearch<TFDNode, String, Double> bf = new BestFirst<>(MLUtil.getGraphGenerator(searchSpaceFile, evaluablePredicatFile, null, null), n
		 -> 0.0);
		 new SimpleGraphVisualizationWindow<>(bf.getEventBus()).getPanel().setTooltipGenerator(new TFDTooltipGenerator<>());;
		
		 while(bf.nextSolution() != null);
	}
	
	public static void main(String[] args) throws Exception {
		HttpServiceServer server = HttpServiceServer.TEST_SERVER();
		try {
						
			File folder = new File(args[0]);
			SCC2018MLTest runner = new SCC2018MLTest(folder);
			runner.runSpecific(1);
		} finally {
			server.shutdown();
		}
	}
}
