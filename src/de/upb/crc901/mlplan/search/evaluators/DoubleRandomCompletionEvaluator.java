package de.upb.crc901.mlplan.search.evaluators;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.core.MLUtil;
import de.upb.crc901.mlplan.core.SolutionEvaluator;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.model.ceoc.CEOCAction;
import jaicore.search.structure.core.Node;

public class DoubleRandomCompletionEvaluator extends RandomCompletionEvaluator<Double> {

	private final static Logger logger = LoggerFactory.getLogger(DoubleRandomCompletionEvaluator.class);
	private static final List<String> classifierRanking = Arrays.asList(new String[] { "IBk", "NaiveBayesMultinomial", "RandomTree", "NaiveBayes", "RandomForest", "SimpleLogistic",
			"MultiLayerPerceptron", "VotedPerceptron", "J48", "SMO", "Logistic" });
	
	public DoubleRandomCompletionEvaluator(Random random, int samples, SolutionEvaluator evaluator) {
		super(random, samples, evaluator);
	}
	
	@Override
	public Double computeEvaluationPriorToCompletion(Node<TFDNode,?> n, List<TFDNode> path, List<CEOCAction> plan, List<String> currentProgram) throws Exception {
		
		/*
		 * if we have an f-value belonging to this plan, store it (this can be if we get asked for the f of a node we generated internally but that does not even belong to the main search
		 * graph)
		 */

		/* check whether a filter and a classifier have been defined */
		Optional<String> classifierLine = currentProgram.stream().filter(line -> line.contains("new") && line.contains("classifiers")).findAny();
		if (!classifierLine.isPresent()) {
			String nextLiteralName = n.getPoint().getRemainingTasks().get(0).getPropertyName();
			if (nextLiteralName.endsWith("__construct") && nextLiteralName.contains("classifiers")) {
				String classifierName = nextLiteralName.substring(nextLiteralName.lastIndexOf(".") + 1, nextLiteralName.indexOf(":"));
				if (!classifierRanking.contains(classifierName))
					return (double)(classifierRanking.size() * 2);
				return (double)(classifierRanking.indexOf(classifierName) + classifierRanking.size());
			}
			logger.info("No classifier defined yet, so returning 0.");
			return 0.0;
		}

		/* if the classifier has just been defined, check for the standard configuration */
		if (currentProgram.get(currentProgram.size() - 1).equals(classifierLine.get())) {
			logger.info("Classifier has just been chosen, now try its standard configuration.");
			Integer f = evaluator.getSolutionScore(MLUtil.extractPipelineFromPlan(plan));
			if (f != null) {
				fValues.put(n, (double)f);
			} else {
				fValues.put(n, Double.MAX_VALUE);
			}

			/* return the value right now, because we have no completion for this node */
			return fValues.get(n);
		}
		return null;
	}

	@Override
	protected Double convertErrorRateToNodeEvaluation(Integer errorRate) {
		return (double)errorRate;
	}

	@Override
	protected double getExpectedUpperBoundForRelativeDistanceToOptimalSolution(Node<TFDNode, ?> n, List<TFDNode> path, List<CEOCAction> partialPlan, List<String> currentProgram) {
		// TODO Auto-generated method stub
		return 0;
	}
}
