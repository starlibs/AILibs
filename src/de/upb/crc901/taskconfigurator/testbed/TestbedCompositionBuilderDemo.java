package de.upb.crc901.taskconfigurator.testbed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Random;

import de.upb.crc901.taskconfigurator.core.MLPipeline;
import de.upb.crc901.taskconfigurator.core.MLUtil;
import de.upb.crc901.taskconfigurator.core.TaskProblemGenerator;
import jaicore.ml.WekaUtil;
import jaicore.planning.graphgenerators.task.ceociptfd.CEOCIPTFDGraphGenerator;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.model.ceoc.CEOCAction;
import jaicore.planning.model.task.ceocipstn.CEOCIPSTNPlanningProblem;
import jaicore.planning.model.task.ceocstn.CEOCSTNUtil;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import weka.core.Instance;
import weka.core.Instances;

/**
 * 
 * @author Felix
 *
 */
public class TestbedCompositionBuilderDemo {

	final static Random rand = new Random(1);
	final static String dataset = "autowekasets/yeast/train";

	public static void main(String[] args) throws Exception {

		/* create graph generator and composition algorithm */
		TaskProblemGenerator pg = new TaskProblemGenerator();
		CEOCIPSTNPlanningProblem problem = pg.getProblem(new File("testrsc/automl-testbed.testset"));
		CEOCIPTFDGraphGenerator generator = new CEOCIPTFDGraphGenerator(problem, null, null);

		BestFirst<TFDNode, String> compositionAlgorithm = new BestFirst<>(generator, n -> 0);

		/* now derive the first solution (as a plan) */
		List<TFDNode> solution = compositionAlgorithm.nextSolution();
		List<CEOCAction> plan = CEOCSTNUtil.extractPlanFromSolutionPath(solution);
		System.out.println(
				"Found the following solution from which need need to create the composition service (extract the configured classifier and the filter) \n-------------------------------------");
		for (CEOCAction a : plan) {
			System.out.println(a.getEncoding());
		}

		/* now derive the composition from the plan */
		MLPipeline composition = MLUtil.extractPipelineFromPlan(plan);

		/* read data and split it into training and test */
		Instances data = new Instances(new BufferedReader(new FileReader("testrsc/" + dataset + ".arff")));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, rand, .6f);

		/* train the composition */
		composition.buildClassifier(split.get(0)); 

		/**
		 * the composition is now productive. To test it, we call its predict
		 * method on the test data
		 **/
		System.out.println("\n\nNow evaluating the composition\n-------------------------------------");
		for (Instance testInstance : split.get(1)) {
			double predictedClass = composition.classifyInstance(testInstance);
			double trueClass = testInstance.classValue();
			System.out.println(predictedClass + " (true label was: " + trueClass + "; "
					+ (predictedClass == trueClass ? "OK" : "FAIL") + ")");
		}

	}
}
